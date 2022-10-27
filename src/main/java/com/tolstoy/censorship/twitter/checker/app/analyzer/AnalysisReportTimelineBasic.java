/*
 * Copyright 2018 Chris Kelly
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.tolstoy.censorship.twitter.checker.app.analyzer;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.analyzer.AnalysisReportItemBasicTimelineRepliesStatus;
import com.tolstoy.censorship.twitter.checker.api.analyzer.AnalyzedTweetComparatorDirection;
import com.tolstoy.censorship.twitter.checker.api.analyzer.AnalyzedTweetDateComparator;
import com.tolstoy.censorship.twitter.checker.api.analyzer.AnalyzedTweetRankingComparator;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportTimelineBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportTimelineItemBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalyzedTweet;
import com.tolstoy.censorship.twitter.checker.api.analyzer.ITweetRanker;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunTimeline;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;

class AnalysisReportTimelineBasic extends AnalysisReportBasicBase implements IAnalysisReportTimelineBasic {
	private static final Logger logger = LogManager.getLogger( AnalysisReportTimelineBasic.class );

	private static final int NUMBER_OF_SECTIONS = 3;
	private static final int SUPPRESSED_MANY_SUPPRESSED_CUTOFF = 35;
	private static final int SUPPRESSED_MANY_HIDDEN_CUTOFF = 30;
	private static final int VISIBLE_MANY_SUPPRESSED_CUTOFF = 20;
	private static final int VISIBLE_MANY_HIDDEN_CUTOFF = 20;

	private final ISearchRunTimeline searchRun;
	private final ITweetRanker tweetRanker;
	private final List<IAnalysisReportTimelineItemBasic> reportItems;
	private final Map<String,String> attributes;
	private final DateTimeFormatter nameDateFormatter;

	private static class ReportItemComparator implements Comparator<IAnalysisReportTimelineItemBasic>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3967298488291325461L;

		@Override
		public int compare( final IAnalysisReportTimelineItemBasic a, final IAnalysisReportTimelineItemBasic b ) {
			final int dateA = Utils.parseIntDefault( a.getSourceTweet().getAttribute( "time" ) );
			final int dateB = Utils.parseIntDefault( b.getSourceTweet().getAttribute( "time" ) );

			return dateB - dateA;
		}
	}

	AnalysisReportTimelineBasic( final ISearchRunTimeline searchRun, final ITweetRanker tweetRanker,
									final IAnalysisReportFactory analysisReportFactory, final ITweetFactory tweetFactory,
									final IPreferences prefs, final IResourceBundleWithFormatting bundle ) {
		super( analysisReportFactory, tweetFactory, prefs, bundle );

		this.searchRun = searchRun;
		this.tweetRanker = tweetRanker;
		this.attributes = new HashMap<String,String>();
		this.reportItems = new ArrayList<IAnalysisReportTimelineItemBasic>();
		this.nameDateFormatter = DateTimeFormatter.ofPattern( getBundle().getString( "rpt_name_dateformat" ) );
	}

	@Override
	public void run() throws Exception {
		reportItems.clear();

		final ITweetCollection tweetColTimeline = searchRun.getTimeline().getTweetCollection();

		logger.info( "AnalysisReportTimelineBasic, timeline=\n" + tweetColTimeline.toDebugString( "  " ) );

		final Set<Long> sourceTweetIDs = searchRun.getSourceTweetIDs();

		for ( final Long sourceTweetID : sourceTweetIDs ) {
			final ITweet sourceTweet = tweetColTimeline.getTweetByID( sourceTweetID );
			final ISnapshotUserPageIndividualTweet individualPage = searchRun.getIndividualPageBySourceTweetID( sourceTweetID );

			//logger.info( "sourceTweet=" + sourceTweet.getSummary() );
			//logger.info( "individualPage=" + individualPage );

			if ( sourceTweet != null && individualPage != null ) {
				reportItems.add( createReportItem( sourceTweet, individualPage ) );
			}
		}

		attributes.put( "rankingFunctionName", tweetRanker.getFunctionName() );

		Collections.sort( reportItems, new ReportItemComparator() );
	}

	protected IAnalysisReportTimelineItemBasic createReportItem( final ITweet sourceTweet, final ISnapshotUserPageIndividualTweet individualPage )
	throws Exception {
		final AnalysisReportTimelineItemBasic ret = new AnalysisReportTimelineItemBasic( getTweetFactory(), sourceTweet, individualPage );

		final List<ITweet> replyTweets = individualPage.getTweetCollection().getTweets();
		ret.setAttribute( "_sourcetweets", summarizeTweetList( replyTweets ) );

		final IAnalyzedTweet analyzedSourceTweet = getAnalysisReportFactory().makeAnalyzedTweet( sourceTweet, 0, null );

		List<IAnalyzedTweet> analyzedReplies = new ArrayList<IAnalyzedTweet>();
		int order = 1;
		for ( final ITweet tweet : replyTweets ) {
			analyzedReplies.add( getAnalysisReportFactory().makeAnalyzedTweet( tweet, order, analyzedSourceTweet ) );
			order++;
		}

		setDateOrders( analyzedReplies );

		tweetRanker.rankTweets( analyzedReplies, analyzedSourceTweet );

		analyzedReplies = setRankingOrders( analyzedReplies );

		ret.setAttribute( "_rankedtweets", summarizeAnalyzedTweetList( analyzedReplies ) );

			//	Now, each IAnalyzedTweet in analyzedReplies has the original order as it appeared in the page,
			//	plus a date order and a ranking order. They're ordered by ranking in analyzedReplies.

		final List<IAnalyzedTweet> anomalousElevatedTweets = new ArrayList<IAnalyzedTweet>();
		final List<IAnalyzedTweet> anomalousSuppressedOrHiddenTweets = new ArrayList<IAnalyzedTweet>();
		final List<IAnalyzedTweet> hiddenTweets = new ArrayList<IAnalyzedTweet>();

			//	 "suppressed" = not hidden, but lower than expected in the list based on date and ranking
		int numSuppressed = 0;
		for ( final IAnalyzedTweet analyzedReply : analyzedReplies ) {
			if ( !analyzedReply.getTweet().getSupposedQuality().getCensored() &&
					analyzedReply.getOriginalOrder() > analyzedReply.getRankingOrder() &&
					analyzedReply.getOriginalOrder() > analyzedReply.getDateOrder() ) {
				numSuppressed++;
			}
		}

			//	first, add all the hidden tweets.
			//	then, if any hidden tweets are added to anomalousSuppressedOrHiddenTweets they'll
			//	be removed from this list
		int numHidden = 0;
		for ( final IAnalyzedTweet analyzedReply : analyzedReplies ) {
			if ( analyzedReply.getTweet().getSupposedQuality().getCensored() ) {
				hiddenTweets.add( analyzedReply );
				numHidden++;
			}
		}

			//	Divide the ranked list into NUMBER_OF_SECTIONS sections. The top section is the upper section,
			//	the bottom section is the lower section.
		if ( analyzedReplies.size() >= ( 2 * NUMBER_OF_SECTIONS ) ) {
			final int numReplies = analyzedReplies.size();
			final int upperSectionCutoff = numReplies / NUMBER_OF_SECTIONS;
			final int lowerSectionCutoff = ( NUMBER_OF_SECTIONS - 1 ) * upperSectionCutoff;

				//	Look at each tweet in the upper section of the ranked list, starting at the top and working down.
				//	Add tweets if they were in the bottom third of the original list or they were hidden
				//	also, to avoid duplicates in the hiddenTweets list, remove anything we add to
				//	anomalousSuppressedOrHiddenTweets from the hiddenTweets list.
			for ( int which = 0; which < upperSectionCutoff; which++ ) {
				final IAnalyzedTweet analyzedReply = analyzedReplies.get( which );
				final boolean bIsLowQuality = analyzedReply.getTweet().getSupposedQuality().getCensored();
				if ( bIsLowQuality || analyzedReply.getOriginalOrder() >= lowerSectionCutoff ) {
					anomalousSuppressedOrHiddenTweets.add( analyzedReply );

					if ( bIsLowQuality ) {
						removeAnalyzedTweetFromListByID( hiddenTweets, analyzedReply.getTweet().getID() );
					}
				}
			}

				//	Look at each tweet in the lower section of the ranked list, starting at the bottom and working up.
				//	Add tweets if they were in the top third of the original list & they aren't hidden.
			for ( int which = numReplies - 1; which > lowerSectionCutoff; which-- ) {
				final IAnalyzedTweet analyzedReply = analyzedReplies.get( which );
				if ( !analyzedReply.getTweet().getSupposedQuality().getCensored() && analyzedReply.getOriginalOrder() <= upperSectionCutoff ) {
					anomalousElevatedTweets.add( analyzedReply );
				}
			}
		}

		ret.setNumSuppressed( numSuppressed );
		ret.setNumHidden( numHidden );
		ret.setAnomalousElevatedTweets( anomalousElevatedTweets );
		ret.setAnomalousSuppressedOrHiddenTweets( anomalousSuppressedOrHiddenTweets );
		ret.setHiddenTweets( hiddenTweets );

		final int percentSuppressed = Utils.makePercentInt( numSuppressed, analyzedReplies.size() );
		final int percenHidden = Utils.makePercentInt( numHidden, analyzedReplies.size() );

		if ( percentSuppressed > SUPPRESSED_MANY_SUPPRESSED_CUTOFF || percenHidden > SUPPRESSED_MANY_HIDDEN_CUTOFF ) {
			ret.setTimelineRepliesStatus( AnalysisReportItemBasicTimelineRepliesStatus.SUPPRESSED_MANY );
		}
		else if ( percentSuppressed > VISIBLE_MANY_SUPPRESSED_CUTOFF || percenHidden > VISIBLE_MANY_HIDDEN_CUTOFF ) {
			ret.setTimelineRepliesStatus( AnalysisReportItemBasicTimelineRepliesStatus.VISIBLE_MANY );
		}
		else {
			ret.setTimelineRepliesStatus( AnalysisReportItemBasicTimelineRepliesStatus.VISIBLE_MOST );
		}

		ret.setAttribute( "rankingFunctionName", tweetRanker.getFunctionName() );

		return ret;
	}

	@Override
	public String getAnalysisType() {
		return "basic";
	}

	@Override
	public String getName() {
		final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant( searchRun.getStartTime(), ZoneId.systemDefault() );
		return getBundle().getString( "arb_name", searchRun.getInitiatingUser().getHandle(), zonedDateTime.format( nameDateFormatter ) );
	}

	@Override
	public String getDescription() {
		final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant( searchRun.getStartTime(), ZoneId.systemDefault() );
		return getBundle().getString( "arb_description", searchRun.getInitiatingUser().getHandle(), zonedDateTime.format( nameDateFormatter ) );
	}

	@Override
	public ISearchRunTimeline getSearchRun() {
		return searchRun;
	}

	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}

	@Override
	public List<IAnalysisReportTimelineItemBasic> getItems() {
		return reportItems;
	}

	protected List<IAnalyzedTweet> setDateOrders( final List<IAnalyzedTweet> analyzedTweets ) {
		final List<IAnalyzedTweet> temp = new ArrayList<IAnalyzedTweet>( analyzedTweets );
		Collections.sort( temp, new AnalyzedTweetDateComparator( AnalyzedTweetComparatorDirection.ASC ) );
		int dateOrder = 1;
		for ( final IAnalyzedTweet analyzedTweet : temp ) {
			analyzedTweet.setDateOrder( dateOrder );
			dateOrder++;
		}

		return temp;
	}

	protected List<IAnalyzedTweet> setRankingOrders( final List<IAnalyzedTweet> analyzedTweets ) {
		final List<IAnalyzedTweet> temp = new ArrayList<IAnalyzedTweet>( analyzedTweets );
		Collections.sort( temp, new AnalyzedTweetRankingComparator( AnalyzedTweetComparatorDirection.DESC ) );
		int rankingOrder = 1;
		for ( final IAnalyzedTweet analyzedTweet : temp ) {
			analyzedTweet.setRankingOrder( rankingOrder );
			rankingOrder++;
		}

		return temp;
	}

	protected String summarizeAnalyzedTweetList( final List<IAnalyzedTweet> analyzedTweets ) {
		final List<String> temp = new ArrayList<String>( analyzedTweets.size() );

		for ( final IAnalyzedTweet analyzedTweet : analyzedTweets ) {
			temp.add( analyzedTweet.getSummary() );
		}

		return "\n" + StringUtils.join( temp, "\n" );
	}

	protected void removeAnalyzedTweetFromListByID( final List<IAnalyzedTweet> analyzedTweets, final long ID ) {
		final Iterator<IAnalyzedTweet> iter = analyzedTweets.iterator();

		while ( iter.hasNext() ) {
			final IAnalyzedTweet analyzedTweet = iter.next();

			if ( analyzedTweet.getTweet().getID() == ID ) {
				iter.remove();
			}
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "reportItems", reportItems )
		.append( "attributes", attributes )
		.toString();
	}
}
