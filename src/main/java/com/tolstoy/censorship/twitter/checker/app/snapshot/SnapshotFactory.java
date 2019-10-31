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
package com.tolstoy.censorship.twitter.checker.app.snapshot;

import java.time.Instant;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ReplyThreadType;

public class SnapshotFactory implements ISnapshotFactory {
	@Override
	public ISnapshotUserPageTimeline makeSnapshotUserPageTimeline( final String url, final Instant retrievalTime ) {
		return new SnapshotUserPageTimeline( url, retrievalTime );
	}

	@Override
	public ISnapshotUserPageIndividualTweet makeSnapshotUserPageIndividualTweet( final String url, final Instant retrievalTime ) {
		return new SnapshotUserPageIndividualTweet( url, retrievalTime );
	}

	@Override
	public IReplyThread makeReplyThread( final ReplyThreadType replyThreadType,
											final ITweet sourceTweet,
											final ITweet repliedToTweet,
											final ISnapshotUserPageIndividualTweet replyPage,
											final ITweetCollection conversationTweetCollection ) {
		return new ReplyThread( replyThreadType, sourceTweet, repliedToTweet, replyPage, conversationTweetCollection );
	}
}
