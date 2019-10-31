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
package com.tolstoy.censorship.twitter.checker.api.searchrun;

import java.util.Map;
import java.util.Set;

import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;

/**
 * Represents a set of tweets retrieved from a user's timeline plus
 * the individual pages for each of those tweets.
 *
 * The motivation is to show which replies to a specific user
 * were elevated, supprssed, and hidden.
 */
public interface ISearchRunTimeline extends ISearchRun {
	/** Get the tweets etc retrieved from a user's timeline.
	 * @return the timeline object
	 */
	ISnapshotUserPageTimeline getTimeline();

	/** Set the tweets etc retrieved from a user's timeline.
	 * @param timeline the timeline object
	 */
	void setSnapshotUserPageTimeline( final ISnapshotUserPageTimeline timeline );

	/** Get the set of individual pages that were retrieved for the tweets
	 * in the user's timeline. (The timeline might have more tweets than
	 * are in this map.)
	 * @return the individual pages in a map. The map key is the tweet ID
	 * of a user's source tweet.
	 */
	Map<Long,ISnapshotUserPageIndividualTweet> getIndividualPages();

	/** Set the set of individual pages for the tweets in the user's timeline.
	 * @param individualPages the individual pages in a map. The map key is the tweet ID
	 * of a user's source tweet.
	 */
	void setReplies( final Map<Long,ISnapshotUserPageIndividualTweet> individualPages );

	/** Set an individual page for one of the user's tweets.
	 * @param sourceTweetID the tweet ID of the user's source tweet from their timeline
	 * @param individualPage the individual page for that source tweet
	 */
	void setReply( final long sourceTweetID, final ISnapshotUserPageIndividualTweet individualPage );

	/** Get the set of tweet IDs for the user's source tweets that were
	 * retrieved from their timeline.
	 * @return a set of tweet IDs
	 */
	Set<Long> getSourceTweetIDs();

	/** Get the individual page based on the user's source tweet ID.
	 * @param sourceTweetID the ID of a tweet from the user's timeline.
	 * @return an individual page, or null if there is no such page.
	 */
	ISnapshotUserPageIndividualTweet getIndividualPageBySourceTweetID( final long sourceTweetID );
}

