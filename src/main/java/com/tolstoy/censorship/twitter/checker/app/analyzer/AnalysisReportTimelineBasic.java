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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.io.Serializable;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.tweet.*;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.analyzer.*;
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
	private Map<String,String> attributes;
	private DateTimeFormatter nameDateFormatter;

	private static class ReportItemComparator implements Comparator<IAnalysisReportTimelineItemBasic>, Serializable {
		@Override
		public int compare( IAnalysisReportTimelineItemBasic a, IAnalysisReportTimelineItemBasic b ) {
			int dateA = Utils.parseIntDefault( a.getSourceTweet().getAttribute( "time" ) );
			int dateB = Utils.parseIntDefault( b.getSourceTweet().getAttribute( "time" ) );

			return dateB - dateA;
		}
	}

	AnalysisReportTimelineBasic( ISearchRunTimeline searchRun, ITweetRanker tweetRanker,
									IAnalysisReportFactory analysisReportFactory, ITweetFactory tweetFactory,
									IPreferences prefs, IResourceBundleWithFormatting bundle ) {
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

		ITweetCollection tweetColTimeline = searchRun.getTimeline().getTweetCollection();

		Set<Long> sourceTweetIDs = searchRun.getSourceTweetIDs();

		for ( Long sourceTweetID : sourceTweetIDs ) {
			ITweet sourceTweet = tweetColTimeline.getTweetByID( sourceTweetID );
			ISnapshotUserPageIndividualTweet individualPage = searchRun.getIndividualPageBySourceTweetID( sourceTweetID );

			//logger.info( "sourceTweet=" + sourceTweet.getSummary() );
			//logger.info( "individualPage=" + individualPage );

			if ( sourceTweet != null && individualPage != null ) {
				reportItems.add( createReportItem( sourceTweet, individualPage ) );
			}
		}

		attributes.put( "rankingFunctionName", tweetRanker.getFunctionName() );

		Collections.sort( reportItems, new ReportItemComparator() );
	}

	protected IAnalysisReportTimelineItemBasic createReportItem( ITweet sourceTweet, ISnapshotUserPageIndividualTweet individualPage )
	throws Exception {
		AnalysisReportTimelineItemBasic ret = new AnalysisReportTimelineItemBasic( getTweetFactory(), sourceTweet, individualPage );

		List<ITweet> replyTweets = individualPage.getTweetCollection().getTweets();
		ret.setAttribute( "_sourcetweets", summarizeTweetList( replyTweets ) );

		IAnalyzedTweet analyzedSourceTweet = getAnalysisReportFactory().makeAnalyzedTweet( sourceTweet, 0, null );

		List<IAnalyzedTweet> analyzedReplies = new ArrayList<IAnalyzedTweet>();
		int order = 1;
		for ( ITweet tweet : replyTweets ) {
			analyzedReplies.add( getAnalysisReportFactory().makeAnalyzedTweet( tweet, order, analyzedSourceTweet ) );
			order++;
		}

		setDateOrders( analyzedReplies );

		tweetRanker.rankTweets( analyzedReplies, analyzedSourceTweet );

		analyzedReplies = setRankingOrders( analyzedReplies );

		ret.setAttribute( "_rankedtweets", summarizeAnalyzedTweetList( analyzedReplies ) );

			//	Now, each IAnalyzedTweet in analyzedReplies has the original order as it appeared in the page,
			//	plus a date order and a ranking order. They're ordered by ranking in analyzedReplies.

		List<IAnalyzedTweet> anomalousElevatedTweets = new ArrayList<IAnalyzedTweet>();
		List<IAnalyzedTweet> anomalousSuppressedOrHiddenTweets = new ArrayList<IAnalyzedTweet>();
		List<IAnalyzedTweet> hiddenTweets = new ArrayList<IAnalyzedTweet>();

			//	 "suppressed" = not hidden, but lower than expected in the list based on date and ranking
		int numSuppressed = 0;
		for ( IAnalyzedTweet analyzedReply : analyzedReplies ) {
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
		for ( IAnalyzedTweet analyzedReply : analyzedReplies ) {
			if ( analyzedReply.getTweet().getSupposedQuality().getCensored() ) {
				hiddenTweets.add( analyzedReply );
				numHidden++;
			}
		}

			//	Divide the ranked list into NUMBER_OF_SECTIONS sections. The top section is the upper section,
			//	the bottom section is the lower section.
		if ( analyzedReplies.size() >= ( 2 * NUMBER_OF_SECTIONS ) ) {
			int numReplies = analyzedReplies.size();
			int upperSectionCutoff = (int) numReplies / NUMBER_OF_SECTIONS;
			int lowerSectionCutoff = ( NUMBER_OF_SECTIONS - 1 ) * upperSectionCutoff;

				//	Look at each tweet in the upper section of the ranked list, starting at the top and working down.
				//	Add tweets if they were in the bottom third of the original list or they were hidden
				//	also, to avoid duplicates in the hiddenTweets list, remove anything we add to
				//	anomalousSuppressedOrHiddenTweets from the hiddenTweets list.
			for ( int which = 0; which < upperSectionCutoff; which++ ) {
				IAnalyzedTweet analyzedReply = analyzedReplies.get( which );
				boolean bIsLowQuality = analyzedReply.getTweet().getSupposedQuality().getCensored();
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
				IAnalyzedTweet analyzedReply = analyzedReplies.get( which );
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

		int percentSuppressed = Utils.makePercentInt( numSuppressed, analyzedReplies.size() );
		int percenHidden = Utils.makePercentInt( numHidden, analyzedReplies.size() );

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

		ret.setAttribute( "totalReplies", "" + ret.getTotalReplies() );
		ret.setAttribute( "totalRepliesActual", "" + ret.getTotalRepliesActual() );

		return ret;
	}

	@Override
	public String getAnalysisType() {
		return "basic";
	}

	@Override
	public String getName() {
		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant( searchRun.getStartTime(), ZoneId.systemDefault() );
		return getBundle().getString( "arb_name", searchRun.getInitiatingUser().getHandle(), zonedDateTime.format( nameDateFormatter ) );
	}

	@Override
	public String getDescription() {
		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant( searchRun.getStartTime(), ZoneId.systemDefault() );
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

	protected List<IAnalyzedTweet> setDateOrders( List<IAnalyzedTweet> analyzedTweets ) {
		List<IAnalyzedTweet> temp = new ArrayList<IAnalyzedTweet>( analyzedTweets );
		Collections.sort( temp, new AnalyzedTweetDateComparator( AnalyzedTweetComparatorDirection.ASC ) );
		int dateOrder = 1;
		for ( IAnalyzedTweet analyzedTweet : temp ) {
			analyzedTweet.setDateOrder( dateOrder );
			dateOrder++;
		}

		return temp;
	}

	protected List<IAnalyzedTweet> setRankingOrders( List<IAnalyzedTweet> analyzedTweets ) {
		List<IAnalyzedTweet> temp = new ArrayList<IAnalyzedTweet>( analyzedTweets );
		Collections.sort( temp, new AnalyzedTweetRankingComparator( AnalyzedTweetComparatorDirection.DESC ) );
		int rankingOrder = 1;
		for ( IAnalyzedTweet analyzedTweet : temp ) {
			analyzedTweet.setRankingOrder( rankingOrder );
			rankingOrder++;
		}

		return temp;
	}

	protected String summarizeAnalyzedTweetList( List<IAnalyzedTweet> analyzedTweets ) {
		List<String> temp = new ArrayList<String>( analyzedTweets.size() );

		for ( IAnalyzedTweet analyzedTweet : analyzedTweets ) {
			temp.add( analyzedTweet.getSummary() );
		}

		return "\n" + StringUtils.join( temp, "\n" );
	}

	protected void removeAnalyzedTweetFromListByID( List<IAnalyzedTweet> analyzedTweets, long ID ) {
		Iterator<IAnalyzedTweet> iter = analyzedTweets.iterator();

		while ( iter.hasNext() ) {
			IAnalyzedTweet analyzedTweet = iter.next();

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
