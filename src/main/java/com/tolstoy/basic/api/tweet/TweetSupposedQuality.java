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
package com.tolstoy.basic.api.tweet;

public enum TweetSupposedQuality {
	HIGH( "high_quality", "HighQuality", "high", false ),
	LOW( "low_quality", "LowQuality", "low", true ),
	ABUSIVE( "abusive_quality", "AbusiveQuality", "abusive", true ),
	UNKNOWN( "unknown_quality", "UnknownQuality", "unknown", true );

	private final String key, htmlName, matchSubstring;
	private final boolean censored;

	public static TweetSupposedQuality getMatching( final String quality ) {
		if ( quality == null || quality.length() < 1 ) {
			return UNKNOWN;
		}

		final String qualityLowercase = quality.toLowerCase();

		for ( final TweetSupposedQuality tweetSupposedQuality : values() ) {
			if ( qualityLowercase.indexOf( tweetSupposedQuality.getMatchSubstring() ) > -1 ) {
				return tweetSupposedQuality;
			}
		}

		return UNKNOWN;
	}

	TweetSupposedQuality( final String key, final String htmlName, final String matchSubstring, final boolean censored ) {
		this.key = key;
		this.htmlName = htmlName;
		this.matchSubstring = matchSubstring;
		this.censored = censored;
	}

	public String getKey() {
		return key;
	}

	public String getHtmlName() {
		return htmlName;
	}

	public String getMatchSubstring() {
		return matchSubstring;
	}

	public boolean getCensored() {
		return censored;
	}
}
