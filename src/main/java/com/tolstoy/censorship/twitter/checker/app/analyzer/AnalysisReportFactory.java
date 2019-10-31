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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportRepliesBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportTimelineBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalyzedTweet;
import com.tolstoy.censorship.twitter.checker.api.analyzer.ITweetRanker;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunTimeline;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;

public class AnalysisReportFactory implements IAnalysisReportFactory {
	private static final Logger logger = LogManager.getLogger( AnalysisReportFactory.class );

	private final ITweetFactory tweetFactory;
	private final IAppDirectories appDirectories;
	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;

	public AnalysisReportFactory( final ITweetFactory tweetFactory, final IAppDirectories appDirectories, final IPreferences prefs, final IResourceBundleWithFormatting bundle ) {
		this.tweetFactory = tweetFactory;
		this.appDirectories = appDirectories;
		this.prefs = prefs;
		this.bundle = bundle;
	}

	@Override
	public IAnalysisReportRepliesBasic makeAnalysisReportRepliesBasic( final ISearchRunReplies searchRun, final ITweetRanker tweetRanker )
	throws Exception {
		return new AnalysisReportRepliesBasic( searchRun, tweetRanker, this, tweetFactory, prefs, bundle );
	}

	@Override
	public IAnalysisReportTimelineBasic makeAnalysisReportTimelineBasic( final ISearchRunTimeline searchRun, final ITweetRanker tweetRanker )
	throws Exception {
		return new AnalysisReportTimelineBasic( searchRun, tweetRanker, this, tweetFactory, prefs, bundle );
	}

	@Override
	public IAnalyzedTweet makeAnalyzedTweet( final ITweet tweet, final int order, final IAnalyzedTweet referenceTweet ) {
		return new AnalyzedTweet( tweet, order, referenceTweet );
	}

	@Override
	public ITweetRanker makeTweetRankerBasic() {
		return new TweetRankerBasic();
	}

	@Override
	public ITweetRanker makeTweetRankerJavascript() {
		try {
			return new TweetRankerJavascript( tweetFactory, appDirectories, prefs, bundle );
		}
		catch ( final Exception e ) {
			logger.error( "cannot create TweetRankerJavascript; this can be ignored unless you expected an external script to be used", e );
			return null;
		}
	}
}

