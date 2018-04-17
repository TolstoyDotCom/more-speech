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
package com.tolstoy.censorship.twitter.checker.api.analyzer;

import java.util.Map;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;

public interface IAnalysisReportRepliesItemBasic {
	ITweet getSourceTweet();
	ITweet getRepliedToTweet();
	IReplyThread getReplyThread();

	int getTotalReplies();
	int getTotalRepliesActual();
	boolean getListIsComplete();

	int getRank();
	void setRank( int rank );

	int getExpectedRankByInteraction();
	void setExpectedRankByInteraction( int expectedRankByInteraction );

	int getExpectedRankByDate();
	void setExpectedRankByDate( int expectedRankByDate );

	int getExpectedRankByOverallRanking();
	void setExpectedRankByOverallRanking( int expectedRankByOverallRanking );

	AnalysisReportItemBasicTweetStatus getTweetStatus();
	void setTweetStatus( AnalysisReportItemBasicTweetStatus status );

	ITweetCollection getAnomalousHigherTweets();
	void setAnomalousHigherTweets( ITweetCollection anomalousHigherTweets );

	ITweetCollection getAnomalousLowerTweets();
	void setAnomalousLowerTweets( ITweetCollection anomalousLowerTweets );

	ITweetCollection getSuppressedOrHiddenTweets();
	void setSuppressedOrHiddenTweets( ITweetCollection suppressedOrHiddenTweets );

	/**
	 * Optional, free-form metadata such as related to the the computations, etc.
	 * Don't rely on any of those being available.
	 */
	Map<String,String> getAttributes();
	void setAttributes( Map<String,String> attributes );

	String getAttribute( String key );
	void setAttribute( String key, String value );
}

