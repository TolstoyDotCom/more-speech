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
import com.tolstoy.censorship.twitter.checker.api.snapshot.*;

/**
 * Represents a set of tweets retrieved from a user's timeline plus
 * the sets of tweets from the individual pages of the tweets the
 * user replied to.
 * <p>
 * For instance, {@literal @}NASA's timeline might contain a reply to {@literal @}space_station.
 * The @NASA tweet will be in the ISnapshotUserPageTimeline object.
 * And, the {@literal @}space_station tweet {@literal @}NASA replied to
 * will be one of the IReplyThread objects.
 * The latter object contains the tweet from {@literal @}space_station
 * and also the set of tweets representing the replies.
 */
public interface ISearchRunReplies extends ISearchRun {
	/** Get the tweets etc retrieved from a user's timeline.
	 * @return the timeline object
	 */
	ISnapshotUserPageTimeline getTimeline();

	/** Set the tweets etc retrieved from a user's timeline.
	 * @param timeline the timeline object
	 */
	void setSnapshotUserPageTimeline( ISnapshotUserPageTimeline timeline );

	/** Get the set of individual pages that should contain the user's replies.
	 * @return the individual pages in a map. The map key is the tweet ID
	 * of the replying user's original tweet. (The tweet ID of the tweet the
	 * user replied to can be obtained from the IReplyThread
	 * object.)
	 */
	Map<Long,IReplyThread> getReplies();

	/** Set the set of individual pages that should contain the user's replies.
	 * @param replies the individual pages in a map. The map key is the tweet ID
	 * of the replying user's original tweet. (The tweet ID of the tweet the
	 * user replied to can be obtained from the IReplyThread
	 * object.)
	 */
	void setReplies( Map<Long,IReplyThread> replies );

	/** Set a reply thread that should contain a user's reply.
	 * @param sourceTweetID the tweet ID of the user's original tweet
	 * @param replyThread a reply thread
	 */
	void setReply( long sourceTweetID, IReplyThread replyThread );

	/** Get the set of tweet IDs for the user's replies. If five replies
	 * from the timeline were saved, this will have the five IDs of those tweets.
	 * @return a set of tweet IDs
	 */
	Set<Long> getSourceTweetIDs();

	/** Get the individual page based on the user's original tweet ID.
	 * @param sourceTweetID the ID of a tweet, such as from the user's timeline.
	 * @return a reply thread, or null if there is no such page.
	 */
	IReplyThread getReplyThreadBySourceTweetID( long sourceTweetID );
}

