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

public enum AnalysisReportItemBasicTimelineRepliesStatus {
	/**
	 * Most of the tweets on a reply page are visible and not suppressed.
	 */
	VISIBLE_MOST( "timeline_replies_status_most_visible" ),

	/**
	 * Many of the tweets on a reply page are visible and not suppressed.
	 */
	VISIBLE_MANY( "timeline_replies_status_many_visible" ),

	/**
	 * Most of the tweets on a reply page are hidden or suppressed.
	 */
	SUPPRESSED_MANY( "timeline_replies_status_many_suppressed" ),

	/**
	 * Default value.
	 */
	UNKNOWN( "timeline_replies_status_unknown" );

	private String key;

	AnalysisReportItemBasicTimelineRepliesStatus( String key ) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}

