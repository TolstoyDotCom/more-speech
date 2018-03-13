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

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.Instant;

/**
 * Represents an ordered list of ITweet objects, with a retrieval time and optional
 * attributes.
 * <p>
 * For now, the tweets will probably be in "page order": the order the tweets were
 * retrieved from the page.
 * <p>
 * TODO (low priority): add sorting including page order so the tweets could, e.g.,
 * be sorted by date and then restored to the original page order.
 */
public interface ITweetCollection {
	/**
	 * Get the list of tweets.
	 * @return the list
	 */
	List<ITweet> getTweets();

	/**
	 * Set the list of tweets.
	 * @return the list
	 */
	void setTweets( List<ITweet> tweets );

	/**
	 * Find a tweet in the list given its ID
	 * @return the tweet, or null if not found
	 */
	ITweet getTweetByID( long id );

	/**
	 * Find the ordinal position of tweet in the list given its ID
	 * @return the order (starting at 1) or 0 if the tweet is not in the list
	 */
	int getTweetOrderByID( long id );

	/**
	 * Add a tweet to the end of the list
	 * @param tweet the tweet to add
	 */
	void addTweet( ITweet tweet );

	Instant getRetrievalTime();
	void setRetrievalTime( Instant retrievalTime );

	/**
	 * Optional, free-form metadata such as "handle", "userid", "url", etc.
	 * Don't rely on any of those being available.
	 */
	Map<String,String> getAttributes();
	void setAttributes( Map<String,String> attributes );

	String getAttribute( String key );
	void setAttribute( String key, String value );
}
