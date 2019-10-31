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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;

class AnalysisReportBasicBase {
	private static final Logger logger = LogManager.getLogger( AnalysisReportBasicBase.class );

	private final IAnalysisReportFactory analysisReportFactory;
	private final ITweetFactory tweetFactory;
	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;
	private final DateTimeFormatter nameDateFormatter;

	AnalysisReportBasicBase( final IAnalysisReportFactory analysisReportFactory, final ITweetFactory tweetFactory,
								final IPreferences prefs, final IResourceBundleWithFormatting bundle ) {
		this.analysisReportFactory = analysisReportFactory;
		this.tweetFactory = tweetFactory;
		this.prefs = prefs;
		this.bundle = bundle;
		this.nameDateFormatter = DateTimeFormatter.ofPattern( bundle.getString( "rpt_name_dateformat" ) );
	}

	protected IAnalysisReportFactory getAnalysisReportFactory() {
		return analysisReportFactory;
	}

	protected ITweetFactory getTweetFactory() {
		return tweetFactory;
	}

	protected IPreferences getPrefs() {
		return prefs;
	}

	protected IResourceBundleWithFormatting getBundle() {
		return bundle;
	}

	protected DateTimeFormatter getDateTimeFormatter() {
		return nameDateFormatter;
	}

	protected int getTweetOrder( final List<ITweet> tweets, final long tweetID ) {
		int order = 1;
		for ( final ITweet tweet : tweets ) {
			if ( tweet.getID() == tweetID ) {
				return order;
			}
			order++;
		}

		return 0;
	}

	protected String summarizeTweetList( final List<ITweet> tweets ) {
		final List<String> temp = new ArrayList<String>( tweets.size() );

		for ( final ITweet tweet : tweets ) {
			temp.add( tweet.getSummary() );
		}

		return "\n" + StringUtils.join( temp, "\n" );
	}

	protected int countNewerTweets( final ITweet testTweet, final List<ITweet> tweets ) {
		final int time = Utils.parseIntDefault( testTweet.getAttribute( "time" ) );
		int count = 0;

		for ( final ITweet tweet : tweets ) {
			final int tempTime = Utils.parseIntDefault( tweet.getAttribute( "time" ) );
			if ( tempTime > time ) {
				count++;
			}
		}

		return count;
	}
}
