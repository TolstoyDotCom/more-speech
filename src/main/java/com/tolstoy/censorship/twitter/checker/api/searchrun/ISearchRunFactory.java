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

import java.time.Instant;
import java.util.Map;

import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;

public interface ISearchRunFactory {
	ISearchRunReplies makeSearchRunReplies( final long id, final ITweetUser user, final Instant startTime, final Instant endTime );
	ISearchRunReplies makeSearchRunReplies( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
											final ISnapshotUserPageTimeline timeline,
											final Map<Long,IReplyThread> replies );
	ISearchRunReplies makeSearchRunReplies( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
											final Map<String,String> attributes );
	ISearchRunReplies makeSearchRunReplies( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
											final ISnapshotUserPageTimeline timeline,
											final Map<Long,IReplyThread> replies,
											final Map<String,String> attributes );

	ISearchRunTimeline makeSearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime );
	ISearchRunTimeline makeSearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
												final ISnapshotUserPageTimeline timeline,
												final Map<Long,ISnapshotUserPageIndividualTweet> replies );
	ISearchRunTimeline makeSearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
												final Map<String,String> attributes );
	ISearchRunTimeline makeSearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
												final ISnapshotUserPageTimeline timeline,
												final Map<Long,ISnapshotUserPageIndividualTweet> replies,
												final Map<String,String> attributes );

	ISearchRunItinerary makeSearchRunItineraryFromJSON( final String jsonData ) throws Exception;
}
