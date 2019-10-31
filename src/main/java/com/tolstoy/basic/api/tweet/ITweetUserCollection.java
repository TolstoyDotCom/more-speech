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
 * Represents an ordered list of ITweetUser objects, with a retrieval time and optional
 * attributes.
 */
public interface ITweetUserCollection {
	/**
	 * Get the list of tweetUsers.
	 * @return the list
	 */
	List<ITweetUser> getTweetUsers();

	/**
	 * Set the list of tweetUsers.
	 * @return the list
	 */
	void setTweetUsers( final List<ITweetUser> tweetUsers );

	/**
	 * Find a tweetUser in the list given its ID.
	 * @return the tweetUser, or null if not found
	 */
	ITweetUser getTweetUserByID( final long id );

	/**
	 * Find a tweetUser in the list given its handle.
	 * @return the tweetUser, or null if not found
	 */
	ITweetUser getTweetUserByHandle( final String handle );

	/**
	 * Find the ordinal position of a tweetUser in the list given its ID
	 * @return the order (starting at 1) or 0 if the tweetUser is not in the list
	 */
	int getTweetUserOrderByID( final long id );

	/**
	 * Add a tweetUser to the end of the list
	 * @param tweetUser the tweetUser to add
	 */
	void addTweetUser( final ITweetUser tweetUser );

	/**
	 * Removes all tweetUsers that have the given ID
	 * @param id the ID to match
	 */
	void removeTweetUserByID( final long id );

	/**
	 * Calls supplementFrom() on each tweetUser in this collection that
	 * has a corresponding tweetUser in the given collection.
	 */
	List<String> supplementFrom( final ITweetUserCollection otherCollection );

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
