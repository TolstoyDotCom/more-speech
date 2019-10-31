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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.TweetComparatorDirection;
import com.tolstoy.basic.api.tweet.TweetDateComparator;
import com.tolstoy.basic.api.tweet.TweetInteractionComparator;
import com.tolstoy.basic.api.tweet.TweetSupposedQuality;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.analyzer.AnalysisReportItemBasicTweetStatus;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportRepliesBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportRepliesItemBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.ITweetRanker;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ReplyThreadType;

class AnalysisReportRepliesBasic extends AnalysisReportBasicBase implements IAnalysisReportRepliesBasic {
	private static final Logger logger = LogManager.getLogger( AnalysisReportRepliesBasic.class );

	private static final int BOOST_REPLIES = 4;
	private static final int BOOST_RETWEETS = 2;
	private static final int BOOST_FAVORITES = 1;

	private final ISearchRunReplies searchRun;
	private final ITweetRanker tweetRanker;
	private final List<IAnalysisReportRepliesItemBasic> reportItems;
	private final Map<String,String> attributes;
	private final DateTimeFormatter nameDateFormatter;

	AnalysisReportRepliesBasic( final ISearchRunReplies searchRun, final ITweetRanker tweetRanker, final IAnalysisReportFactory analysisReportFactory,
								final ITweetFactory tweetFactory, final IPreferences prefs, final IResourceBundleWithFormatting bundle ) {
		super( analysisReportFactory, tweetFactory, prefs, bundle );

		this.searchRun = searchRun;
		this.tweetRanker = tweetRanker;
		this.attributes = new HashMap<String,String>();
		this.reportItems = new ArrayList<IAnalysisReportRepliesItemBasic>();
		this.nameDateFormatter = DateTimeFormatter.ofPattern( getBundle().getString( "rpt_name_dateformat" ) );
	}

	@Override
	public void run() throws Exception {
		reportItems.clear();

		final ITweetCollection tweetColTimeline = searchRun.getTimeline().getTweetCollection();

		final Set<Long> sourceTweetIDs = searchRun.getSourceTweetIDs();

		for ( final Long sourceTweetID : sourceTweetIDs ) {
			final ITweet sourceTweet = tweetColTimeline.getTweetByID( sourceTweetID );
			final IReplyThread replyThread = searchRun.getReplyThreadBySourceTweetID( sourceTweetID );

			//logger.info( "sourceTweet=" + sourceTweet.getSummary() );
			//logger.info( "replyThread=" + replyThread );

			if ( sourceTweet != null && replyThread != null ) {
				reportItems.add( createReportItem( sourceTweet, replyThread ) );
			}
		}
	}

	protected IAnalysisReportRepliesItemBasic createReportItem( final ITweet sourceTweet, final IReplyThread replyThread ) {
		final AnalysisReportRepliesItemBasic ret = new AnalysisReportRepliesItemBasic( getTweetFactory(), sourceTweet, replyThread );

		final ISnapshotUserPageIndividualTweet replyPage = replyThread.getReplyPage();

		if ( replyThread.getReplyThreadType() == ReplyThreadType.INDIRECT &&
				replyThread.getConversationTweetCollection() != null &&
				!replyThread.getConversationTweetCollection().getTweets().isEmpty() ) {
			ret.setAttribute( "initial conversation", summarizeTweetList( replyThread.getConversationTweetCollection().getTweets() ) );
			ret.setAttribute( "initial conversation id", "" + replyThread.getSourceTweet().getRepliedToTweetID() );
		}

		ret.setAttribute( "reply thread type", "" + replyThread.getReplyThreadType() );

		ret.setAttribute( "total replies", "" + replyPage.getNumReplies() );
		ret.setAttribute( "totalReplies", "" + ret.getTotalReplies() );
		ret.setAttribute( "totalRepliesActual", "" + ret.getTotalRepliesActual() );

		final List<ITweet> tweets = replyPage.getTweetCollection().getTweets();
		ret.setAttribute( "_sourcetweets", summarizeTweetList( tweets ) );

		final int numNewerTweets = countNewerTweets( sourceTweet, tweets );
		final int percentNewerTweets = Utils.makePercentInt( numNewerTweets, replyPage.getNumReplies() );
		final int percentComplete = Utils.makePercentInt( tweets.size(), replyPage.getNumReplies() );

		ret.setAttribute( "numNewerTweets", "" + numNewerTweets );
		ret.setAttribute( "percentNewerTweets", "" + percentNewerTweets );
		ret.setAttribute( "percentComplete", "" + percentComplete );

		final ITweet foundSourceTweet = replyPage.getTweetCollection().getTweetByID( sourceTweet.getID() );

		ret.setAttribute( "foundSourceTweet", ( foundSourceTweet != null ? foundSourceTweet.getSummary() : " IS NULL" ) );

			//	the reply isn't in the page. It might have been censored, or it just might be further
			//	down the page if we don't have all the tweets from the page. Set the status accordingly.
		if ( foundSourceTweet == null ) {
			ret.setTweetStatus( getTweetNotFoundStatus( sourceTweet, percentNewerTweets, percentComplete, replyPage ) );
			ret.setAttribute( "TWEETNOTFOUND, status set to ", "" + ret.getTweetStatus() );
			return ret;
		}

		final int pageOrder = replyPage.getTweetCollection().getTweetOrderByID( sourceTweet.getID() );
		final int interactionOrder = getTweetInteractionOrder( ret, tweets, sourceTweet.getID() );
		final int dateOrder = getTweetDateOrder( ret, tweets, sourceTweet.getID() );

		final int percentComparedToInteractionOrder = Utils.makePercentInt( interactionOrder - pageOrder, replyPage.getNumReplies() );
		final int percentComparedToDateOrder = Utils.makePercentInt( dateOrder - pageOrder, replyPage.getNumReplies() );

		ret.setAttribute( "pageOrder", "" + pageOrder );
		ret.setAttribute( "interactionOrder", "" + interactionOrder );
		ret.setAttribute( "dateOrder", "" + dateOrder );
		ret.setAttribute( "percentComparedToInteractionOrder", "" + percentComparedToInteractionOrder );
		ret.setAttribute( "percentComparedToDateOrder", "" + percentComparedToDateOrder );

		ret.setRank( pageOrder );
		ret.setExpectedRankByInteraction( interactionOrder );
		ret.setExpectedRankByDate( dateOrder );

		if ( foundSourceTweet.getSupposedQuality() == TweetSupposedQuality.ABUSIVE ) {
				//	the reply is hidden behind a "may contain offensive content" link
			ret.setTweetStatus( AnalysisReportItemBasicTweetStatus.CENSORED_ABUSIVE );
		}
		else if ( foundSourceTweet.getSupposedQuality().getCensored() ) {
				//	the reply is hidden behind a "Show more replies" link
			ret.setTweetStatus( AnalysisReportItemBasicTweetStatus.CENSORED_HIDDEN );
		}
		else if ( pageOrder <= 2 ) {
				//	first two tweets, no matter how many
			ret.setTweetStatus( AnalysisReportItemBasicTweetStatus.VISIBLE_BEST );
		}
		else if ( percentComparedToInteractionOrder > 50 && percentComparedToDateOrder > 50 ) {
				//	better than 50% of similar tweets
			ret.setTweetStatus( AnalysisReportItemBasicTweetStatus.VISIBLE_BETTER );
		}
		else if ( percentComparedToInteractionOrder >= 0 && percentComparedToDateOrder >= 0 ) {
				//	better than 0% to 50% of similar tweets
			ret.setTweetStatus( AnalysisReportItemBasicTweetStatus.VISIBLE_NORMAL );
		}
		else if ( percentComparedToInteractionOrder >= -50 && percentComparedToDateOrder >= -50 ) {
				//	worse than 0% to 50% of similar tweets
			ret.setTweetStatus( AnalysisReportItemBasicTweetStatus.VISIBLE_WORSE );
		}
		else {
				//	the basement
			ret.setTweetStatus( AnalysisReportItemBasicTweetStatus.VISIBLE_WORST );
		}

		ret.setAttribute( "setTweetStatus", "" + ret.getTweetStatus() );

		return ret;
	}

	protected AnalysisReportItemBasicTweetStatus getTweetNotFoundStatus( final ITweet sourceTweet, final int percentNewerTweets, final int percentComplete,
																			final ISnapshotUserPageIndividualTweet replyPage ) {
		if ( replyPage.getComplete() ) {
				//	tweet isn't there and replyPage is complete
			return AnalysisReportItemBasicTweetStatus.CENSORED_NOTFOUND;
		}
		else if ( percentNewerTweets < 30 && percentComplete < 30 ) {
			return AnalysisReportItemBasicTweetStatus.SUPPRESSED_NORMAL;
		}
		else if ( percentNewerTweets < 70 && percentComplete < 70 ) {
			return AnalysisReportItemBasicTweetStatus.SUPPRESSED_WORSE;
		}
		else {
			return AnalysisReportItemBasicTweetStatus.SUPPRESSED_WORST;
		}
	}

	protected int getTweetDateOrder( final AnalysisReportRepliesItemBasic ret, final List<ITweet> tweets, final long tweetID ) {
		final List<ITweet> tempList = new ArrayList<ITweet>( tweets );

		Collections.sort( tempList, new TweetDateComparator( TweetComparatorDirection.ASC ) );

		ret.setAttribute( "tweetsInDateOrder", summarizeTweetList( tempList ) );

		return getTweetOrder( tempList, tweetID );
	}

	protected int getTweetInteractionOrder( final AnalysisReportRepliesItemBasic ret, final List<ITweet> tweets, final long tweetID ) {
		final List<ITweet> tempList = new ArrayList<ITweet>( tweets );

		Collections.sort( tempList, new TweetInteractionComparator( BOOST_REPLIES, BOOST_RETWEETS, BOOST_FAVORITES, TweetComparatorDirection.DESC ) );

		ret.setAttribute( "tweetsInInteractionOrder", summarizeTweetList( tempList ) );

		return getTweetOrder( tempList, tweetID );
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
	public ISearchRunReplies getSearchRun() {
		return searchRun;
	}

	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}

	@Override
	public List<IAnalysisReportRepliesItemBasic> getItems() {
		return reportItems;
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "reportItems", reportItems )
		.append( "attributes", attributes )
		.toString();
	}
}

