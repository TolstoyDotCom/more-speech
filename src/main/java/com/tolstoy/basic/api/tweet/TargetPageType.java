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

public enum TargetPageType {
	TIMELINE( "timeline" ),
	TIMELINE_WITH_REPLIES( "timeline_with_replies" ),
	REPLYPAGE( "replypage" ),
	SEARCH_RESULTS( "search_results" ),
	LIKESPAGE( "likespage" ),
	MEDIAPAGE( "mediapage" ),
	NOTIFICATIONS( "notifications" ),
	HASHTAGPAGE( "hashtagpage" ),
	MOMENTSPAGE( "momentspage" ),
	FOLLOWINGPAGE( "followingpage" ),
	UNKNOWN( "unknown" );

	private final String key;

	TargetPageType( final String key ) {
		this.key = key;
	}

	public static TargetPageType getMatching( final String pageType ) {
		if ( pageType == null || pageType.length() < 1 ) {
			return UNKNOWN;
		}

		final String pageTypeLowercase = pageType.toLowerCase();

		for ( final TargetPageType targetPageType : values() ) {
			if ( pageTypeLowercase.indexOf( targetPageType.getKey() ) > -1 ) {
				return targetPageType;
			}
		}

		return UNKNOWN;
	}

	public String getKey() {
		return key;
	}
}
