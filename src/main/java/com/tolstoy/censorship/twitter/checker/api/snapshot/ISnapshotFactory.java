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
package com.tolstoy.censorship.twitter.checker.api.snapshot;

import java.time.Instant;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;

public interface ISnapshotFactory {
	ISnapshotUserPageTimeline makeSnapshotUserPageTimeline( String url, Instant retrievalTime );
	ISnapshotUserPageIndividualTweet makeSnapshotUserPageIndividualTweet( String url, Instant retrievalTime );
	IReplyThread makeReplyThread( ReplyThreadType replyThreadType,
									ITweet sourceTweet,
									ITweet repliedToTweet,
									ISnapshotUserPageIndividualTweet replyPage,
									ITweetCollection conversationTweetCollection );
}

