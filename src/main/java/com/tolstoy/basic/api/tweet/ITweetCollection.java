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

import java.time.Instant;
import java.util.List;
import java.util.Map;

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
	void setTweets( final List<ITweet> tweets );

	/**
	 * Get a unique list of all the tweetUsers on this collection's tweets.
	 * The order is random.
	 * tweetUsers that are null or that have a 0 ID aren't included.
	 * @return the list
	 */
	List<ITweetUser> getTweetUsers();

	/**
	 * Find a tweet in the list given its ID
	 * @return the tweet, or null if not found
	 */
	ITweet getTweetByID( final long id );

	/**
	 * Find the ordinal position of tweet in the list given its ID
	 * @return the order (starting at 1) or 0 if the tweet is not in the list
	 */
	int getTweetOrderByID( final long id );

	/**
	 * Add a tweet to the end of the list
	 * @param tweet the tweet to add
	 */
	void addTweet( final ITweet tweet );

	/**
	 * Removes all tweets that have the given ID
	 * @param id the ID to match
	 */
	void removeTweetByID( final long id );

	/**
	 * Calls supplementFrom() on each tweet in this collection that
	 * has a corresponding tweet in the given collection.
	 */
	List<String> supplementFrom( final ITweetCollection otherCollection );

	Instant getRetrievalTime();
	void setRetrievalTime( final Instant retrievalTime );

	/**
	 * Optional, free-form metadata such as "handle", "userid", "url", etc.
	 * Don't rely on any of those being available.
	 */
	Map<String,String> getAttributes();
	void setAttributes( final Map<String,String> attributes );

	String getAttribute( final String key );
	void setAttribute( final String key, String value );

	/** Return a debug string.
	*/
	String toDebugString( String indent );
}
