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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.censorship.twitter.checker.api.analyzer.AnalysisReportItemBasicTweetStatus;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportRepliesItemBasic;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;

class AnalysisReportRepliesItemBasic implements IAnalysisReportRepliesItemBasic {
	private static final Logger logger = LogManager.getLogger( AnalysisReportRepliesItemBasic.class );

	private final ITweetFactory tweetFactory;
	private final ITweet sourceTweet, repliedToTweet;
	private final IReplyThread replyThread;
	private ITweetCollection anomalousHigherTweets, anomalousLowerTweets, suppressedOrHiddenTweets;
	private AnalysisReportItemBasicTweetStatus status;
	private Map<String,String> attributes;
	private final int totalReplies, totalRepliesActual;

	private int rank;

	private int expectedRankByInteraction;

	private int expectedRankByDate;

	private int expectedRankByOverallRanking;
	private final boolean isComplete;

	AnalysisReportRepliesItemBasic( final ITweetFactory tweetFactory, final ITweet sourceTweet, final IReplyThread replyThread ) {
		this.tweetFactory = tweetFactory;
		this.sourceTweet = sourceTweet;
		this.replyThread = replyThread;
		this.repliedToTweet = replyThread.getRepliedToTweet();
		this.totalReplies = replyThread.getReplyPage().getNumReplies();
		this.totalRepliesActual = replyThread.getReplyPage().getTweetCollection().getTweets().size();
		this.isComplete = replyThread.getReplyPage().getComplete();

		this.status = AnalysisReportItemBasicTweetStatus.UNKNOWN;
		this.rank = 0;
		this.expectedRankByInteraction = 0;
		this.expectedRankByDate = 0;
		this.expectedRankByOverallRanking = 0;
		this.anomalousHigherTweets = this.tweetFactory.makeTweetCollection();
		this.anomalousLowerTweets = this.tweetFactory.makeTweetCollection();
		this.suppressedOrHiddenTweets = this.tweetFactory.makeTweetCollection();
		this.attributes = new HashMap<String,String>();
	}

	@Override
	public ITweet getSourceTweet() {
		return sourceTweet;
	}

	@Override
	public ITweet getRepliedToTweet() {
		return repliedToTweet;
	}

	@Override
	public IReplyThread getReplyThread() {
		return replyThread;
	}

	@Override
	public int getTotalReplies() {
		return totalReplies;
	}

	@Override
	public int getTotalRepliesActual() {
		return totalRepliesActual;
	}

	@Override
	public boolean getListIsComplete() {
		return isComplete;
	}

	@Override
	public int getRank() {
		return rank;
	}

	@Override
	public void setRank( final int rank ) {
		this.rank = rank;
	}

	@Override
	public int getExpectedRankByInteraction() {
		return expectedRankByInteraction;
	}

	@Override
	public void setExpectedRankByInteraction( final int expectedRankByInteraction ) {
		this.expectedRankByInteraction = expectedRankByInteraction;
	}

	@Override
	public int getExpectedRankByDate() {
		return expectedRankByDate;
	}

	@Override
	public void setExpectedRankByDate( final int expectedRankByDate ) {
		this.expectedRankByDate = expectedRankByDate;
	}

	@Override
	public int getExpectedRankByOverallRanking() {
		return expectedRankByOverallRanking;
	}

	@Override
	public void setExpectedRankByOverallRanking( final int expectedRankByOverallRanking ) {
		this.expectedRankByOverallRanking = expectedRankByOverallRanking;
	}

	@Override
	public AnalysisReportItemBasicTweetStatus getTweetStatus() {
		return status;
	}

	@Override
	public void setTweetStatus( final AnalysisReportItemBasicTweetStatus status ) {
		this.status = status;
	}

	@Override
	public ITweetCollection getAnomalousHigherTweets() {
		return anomalousHigherTweets;
	}

	@Override
	public void setAnomalousHigherTweets( final ITweetCollection anomalousHigherTweets ) {
		this.anomalousHigherTweets = anomalousHigherTweets;
	}

	@Override
	public ITweetCollection getAnomalousLowerTweets() {
		return anomalousLowerTweets;
	}

	@Override
	public void setAnomalousLowerTweets( final ITweetCollection anomalousLowerTweets ) {
		this.anomalousLowerTweets = anomalousLowerTweets;
	}

	@Override
	public ITweetCollection getSuppressedOrHiddenTweets() {
		return suppressedOrHiddenTweets;
	}

	@Override
	public void setSuppressedOrHiddenTweets( final ITweetCollection suppressedOrHiddenTweets ) {
		this.suppressedOrHiddenTweets = suppressedOrHiddenTweets;
	}

	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes( final Map<String,String> attributes ) {
		this.attributes = attributes;
	}

	@Override
	public String getAttribute( final String key ) {
		return attributes.get( key );
	}

	@Override
	public void setAttribute( final String key, final String value ) {
		attributes.put( key, value );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "sourceTweet", sourceTweet )
		.append( "repliedToTweet", repliedToTweet )
		.append( "status", status )
		.append( "totalReplies", totalReplies )
		.append( "totalRepliesActual", totalRepliesActual )
		.append( "isComplete", isComplete )
		.append( "rank", rank )
		.append( "expectedRankByInteraction", expectedRankByInteraction )
		.append( "expectedRankByDate", expectedRankByDate )
		.append( "expectedRankByOverallRanking", expectedRankByOverallRanking )
		.append( "anomalousHigherTweets", anomalousHigherTweets )
		.append( "anomalousLowerTweets", anomalousLowerTweets )
		.append( "suppressedOrHiddenTweets", suppressedOrHiddenTweets )
		.append( "attributes", attributes )
		.toString();
	}
}

