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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.censorship.twitter.checker.api.analyzer.AnalysisReportItemBasicTimelineRepliesStatus;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportTimelineItemBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalyzedTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;

class AnalysisReportTimelineItemBasic implements IAnalysisReportTimelineItemBasic {
	private static final Logger logger = LogManager.getLogger( AnalysisReportTimelineItemBasic.class );

	private final ITweetFactory tweetFactory;
	private final ITweet sourceTweet;
	private final ISnapshotUserPageIndividualTweet individualPage;
	private List<IAnalyzedTweet> anomalousElevatedTweets, anomalousSuppressedOrHiddenTweets, hiddenTweets;
	private AnalysisReportItemBasicTimelineRepliesStatus status;
	private Map<String,String> attributes;
	private final int totalReplies, totalRepliesActual;

	private int numSuppressed;

	private int numHidden;
	private final boolean isComplete;

	AnalysisReportTimelineItemBasic( final ITweetFactory tweetFactory, final ITweet sourceTweet, final ISnapshotUserPageIndividualTweet individualPage ) {
		this.tweetFactory = tweetFactory;
		this.sourceTweet = sourceTweet;
		this.individualPage = individualPage;
		this.totalReplies = individualPage.getNumReplies();
		this.totalRepliesActual = individualPage.getTweetCollection().getTweets().size();
		this.isComplete = individualPage.getComplete();
		this.numSuppressed = 0;
		this.numHidden = 0;
		this.status = AnalysisReportItemBasicTimelineRepliesStatus.UNKNOWN;
		this.anomalousElevatedTweets = new ArrayList<IAnalyzedTweet>();
		this.anomalousSuppressedOrHiddenTweets = new ArrayList<IAnalyzedTweet>();
		this.hiddenTweets = new ArrayList<IAnalyzedTweet>();
		this.attributes = new HashMap<String,String>();
	}

	@Override
	public ITweet getSourceTweet() {
		return sourceTweet;
	}

	@Override
	public ISnapshotUserPageIndividualTweet getIndividualPage() {
		return individualPage;
	}

	@Override
	public AnalysisReportItemBasicTimelineRepliesStatus getTimelineRepliesStatus() {
		return status;
	}

	@Override
	public void setTimelineRepliesStatus( final AnalysisReportItemBasicTimelineRepliesStatus status ) {
		this.status = status;
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
	public int getNumSuppressed() {
		return numSuppressed;
	}

	@Override
	public void setNumSuppressed( final int numSuppressed ) {
		this.numSuppressed = numSuppressed;
	}

	@Override
	public int getNumHidden() {
		return numHidden;
	}

	@Override
	public void setNumHidden( final int numHidden ) {
		this.numHidden = numHidden;
	}

	@Override
	public List<IAnalyzedTweet> getAnomalousElevatedTweets() {
		return anomalousElevatedTweets;
	}

	@Override
	public void setAnomalousElevatedTweets( final List<IAnalyzedTweet> anomalousElevatedTweets ) {
		this.anomalousElevatedTweets = anomalousElevatedTweets;
	}

	@Override
	public List<IAnalyzedTweet> getAnomalousSuppressedOrHiddenTweets() {
		return anomalousSuppressedOrHiddenTweets;
	}

	@Override
	public void setAnomalousSuppressedOrHiddenTweets( final List<IAnalyzedTweet> anomalousSuppressedOrHiddenTweets ) {
		this.anomalousSuppressedOrHiddenTweets = anomalousSuppressedOrHiddenTweets;
	}

	@Override
	public List<IAnalyzedTweet> getHiddenTweets() {
		return hiddenTweets;
	}

	@Override
	public void setHiddenTweets( final List<IAnalyzedTweet> hiddenTweets ) {
		this.hiddenTweets = hiddenTweets;
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
		.append( "individualPage", individualPage )
		.append( "isComplete", isComplete )
		.append( "numSuppressed", numSuppressed )
		.append( "numHidden", numHidden )
		.append( "anomalousElevatedTweets", anomalousElevatedTweets )
		.append( "anomalousSuppressedOrHiddenTweets", anomalousSuppressedOrHiddenTweets )
		.append( "hiddenTweets", hiddenTweets )
		.append( "attributes", attributes )
		.toString();
	}
}

