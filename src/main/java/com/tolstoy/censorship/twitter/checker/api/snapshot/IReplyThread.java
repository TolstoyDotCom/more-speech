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

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;

public interface IReplyThread {
	ReplyThreadType getReplyThreadType();
	void setReplyThreadType( final ReplyThreadType replyThreadType );

	ISnapshotUserPageIndividualTweet getReplyPage();
	void setReplyPage( final ISnapshotUserPageIndividualTweet replyPage );

	ITweet getSourceTweet();
	void setSourceTweet( final ITweet sourceTweet );

	ITweet getRepliedToTweet();
	void setRepliedToTweet( final ITweet repliedToTweet );

	ITweetCollection getConversationTweetCollection();
	void setConversationTweetCollection( final ITweetCollection conversationTweetCollection );
}
