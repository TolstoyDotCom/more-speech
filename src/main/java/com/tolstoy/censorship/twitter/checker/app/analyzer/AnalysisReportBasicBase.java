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
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
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
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportRepliesBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportRepliesItemBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.AnalysisReportItemBasicTweetStatus;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ReplyThreadType;

class AnalysisReportBasicBase {
	private static final Logger logger = LogManager.getLogger( AnalysisReportBasicBase.class );

	private final IAnalysisReportFactory analysisReportFactory;
	private final ITweetFactory tweetFactory;
	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;
	private DateTimeFormatter nameDateFormatter;

	AnalysisReportBasicBase( IAnalysisReportFactory analysisReportFactory, ITweetFactory tweetFactory,
								IPreferences prefs, IResourceBundleWithFormatting bundle ) {
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

	protected int getTweetOrder( List<ITweet> tweets, long tweetID ) {
		int order = 1;
		for ( ITweet tweet : tweets ) {
			if ( tweet.getID() == tweetID ) {
				return order;
			}
			order++;
		}

		return 0;
	}

	protected String summarizeTweetList( List<ITweet> tweets ) {
		List<String> temp = new ArrayList<String>( tweets.size() );

		for ( ITweet tweet : tweets ) {
			temp.add( tweet.getSummary() );
		}

		return "\n" + StringUtils.join( temp, "\n" );
	}

	protected int countNewerTweets( ITweet testTweet, List<ITweet> tweets ) {
		int time = Utils.parseIntDefault( testTweet.getAttribute( "time" ) );
		int count = 0;

		for ( ITweet tweet : tweets ) {
			int tempTime = Utils.parseIntDefault( tweet.getAttribute( "time" ) );
			if ( tempTime > time ) {
				count++;
			}
		}

		return count;
	}
}
