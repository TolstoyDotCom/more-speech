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

import java.util.List;
import java.util.Map;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;

public interface IAnalysisReportTimelineItemBasic {
	ITweet getSourceTweet();
	ISnapshotUserPageIndividualTweet getIndividualPage();

	AnalysisReportItemBasicTimelineRepliesStatus getTimelineRepliesStatus();
	void setTimelineRepliesStatus( final AnalysisReportItemBasicTimelineRepliesStatus status );

	int getTotalReplies();
	int getTotalRepliesActual();
	boolean getListIsComplete();

	int getNumSuppressed();
	void setNumSuppressed( final int numSuppressed );

	int getNumHidden();
	void setNumHidden( final int numHidden );

	List<IAnalyzedTweet> getAnomalousElevatedTweets();
	void setAnomalousElevatedTweets( final List<IAnalyzedTweet> anomalousElevatedTweets );

	List<IAnalyzedTweet> getAnomalousSuppressedOrHiddenTweets();
	void setAnomalousSuppressedOrHiddenTweets( final List<IAnalyzedTweet> anomalousSuppressedOrHiddenTweets );

	List<IAnalyzedTweet> getHiddenTweets();
	void setHiddenTweets( final List<IAnalyzedTweet> hiddenTweets );

	/**
	 * Optional, free-form metadata such as related to the the computations, etc.
	 * Don't rely on any of those being available.
	 */
	Map<String,String> getAttributes();
	void setAttributes( final Map<String,String> attributes );

	String getAttribute( final String key );
	void setAttribute( final String key, final String value );
}

