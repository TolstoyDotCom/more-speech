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

public enum AnalysisReportItemBasicTweetStatus {
	/**
	 * "VISIBLE" values are used when a tweet is in the tweet page. Which
	 * value is used depends on a ranking algorithm.
	 */
	VISIBLE_BEST( "tweet_status_visible_best" ),
	VISIBLE_BETTER( "tweet_status_visible_better" ),
	VISIBLE_NORMAL( "tweet_status_visible_normal" ),
	VISIBLE_WORSE( "tweet_status_visible_worse" ),
	VISIBLE_WORST( "tweet_status_visible_worst" ),

	/**
	 * "SUPPRESSED" values are only used in the cases when a tweet page is
	 * not complete and the tweet could not be found in the page.
	 * The tweet might be further down the page.
	 * Which "SUPPRESSED" value is used depends on how complete the tweet
	 * page is and how many newer tweets there are in the page.
	 */
	SUPPRESSED_NORMAL( "tweet_status_suppressed_normal" ),
	SUPPRESSED_WORSE( "tweet_status_suppressed_worse" ),
	SUPPRESSED_WORST( "tweet_status_suppressed_worst" ),

	/**
	 * This value means a tweet is in the tweet page, but was put into the
	 * "LowQuality" section hidden behind a "Show more tweets" link.
	 */
	CENSORED_HIDDEN( "tweet_status_censored_hidden" ),

	/**
	 * This value means a tweet is in the tweet page, but was put into the
	 * "AbusiveQuality" section hidden behind a "may contain offensive content" link.
	 */
	CENSORED_ABUSIVE( "tweet_status_censored_abusive" ),

	/**
	 * This value is used when the list of tweets is complete and the tweet
	 * was not found.
	 */
	CENSORED_NOTFOUND( "tweet_status_censored_notfound" ),

	/**
	 * Default value.
	 */
	UNKNOWN( "tweet_status_unknown" );

	private final String key;

	AnalysisReportItemBasicTweetStatus( final String key ) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}

