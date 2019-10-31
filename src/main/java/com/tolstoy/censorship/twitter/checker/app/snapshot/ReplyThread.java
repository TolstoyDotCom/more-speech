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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ReplyThreadType;

@JsonIgnoreProperties(ignoreUnknown=true)
class ReplyThread implements IReplyThread {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( ReplyThread.class );

	@JsonProperty
	private ReplyThreadType replyThreadType;

	@JsonProperty
	private ISnapshotUserPageIndividualTweet replyPage;

	@JsonProperty
	private ITweetCollection conversationTweetCollection;

	@JsonProperty
	private ITweet sourceTweet;

	@JsonProperty
	private ITweet repliedToTweet;

	ReplyThread() {
		this.replyThreadType = ReplyThreadType.DIRECT;
		this.replyPage = null;
		this.sourceTweet = null;
		this.repliedToTweet = null;
		this.conversationTweetCollection = null;
	}

	ReplyThread( final ReplyThreadType replyThreadType,
					final ITweet sourceTweet,
					final ITweet repliedToTweet,
					final ISnapshotUserPageIndividualTweet replyPage,
					final ITweetCollection conversationTweetCollection ) {
		this.replyThreadType = replyThreadType;
		this.sourceTweet = sourceTweet;
		this.repliedToTweet = repliedToTweet;
		this.replyPage = replyPage;
		this.conversationTweetCollection = conversationTweetCollection;
	}

	@Override
	public ReplyThreadType getReplyThreadType() {
		return replyThreadType;
	}

	@Override
	public void setReplyThreadType( final ReplyThreadType replyThreadType ) {
		this.replyThreadType = replyThreadType;
	}

	@Override
	public ISnapshotUserPageIndividualTweet getReplyPage() {
		return replyPage;
	}

	@Override
	public void setReplyPage( final ISnapshotUserPageIndividualTweet replyPage ) {
		this.replyPage = replyPage;
	}

	@Override
	public ITweet getSourceTweet() {
		return sourceTweet;
	}

	@Override
	public void setSourceTweet( final ITweet sourceTweet ) {
		this.sourceTweet = sourceTweet;
	}

	@Override
	public ITweet getRepliedToTweet() {
		return repliedToTweet;
	}

	@Override
	public void setRepliedToTweet( final ITweet repliedToTweet ) {
		this.repliedToTweet = repliedToTweet;
	}

	@Override
	public ITweetCollection getConversationTweetCollection() {
		return conversationTweetCollection;
	}

	@Override
	public void setConversationTweetCollection( final ITweetCollection conversationTweetCollection ) {
		this.conversationTweetCollection = conversationTweetCollection;
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "replyThreadType", replyThreadType )
		.append( "sourceTweet", sourceTweet )
		.append( "repliedToTweet", repliedToTweet )
		.append( "replyPage", replyPage )
		.append( "conversationTweetCollection", conversationTweetCollection )
		.toString();
	}
}
