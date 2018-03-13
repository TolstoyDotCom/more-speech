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

import java.util.*;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.tweet.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;

@JsonIgnoreProperties(ignoreUnknown=true)
class SnapshotUserPageIndividualTweet extends SnapshotUserPage implements ISnapshotUserPageIndividualTweet {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( SnapshotUserPageIndividualTweet.class );

	@JsonProperty
	private ITweet individualTweet;

	@JsonProperty
	private long tweetID;

	@JsonProperty
	private int numRetweets;

	@JsonProperty
	private int numLikes;

	@JsonProperty
	private int numReplies;

	SnapshotUserPageIndividualTweet() {
		super( "", Instant.now() );
	}

	SnapshotUserPageIndividualTweet( String url, Instant retrievalTime ) {
		super( url, retrievalTime );
	}

	@Override
	public ITweet getIndividualTweet() {
		return individualTweet;
	}

	@Override
	public long getTweetID() {
		return tweetID;
	}

	@Override
	public int getNumRetweets() {
		return numRetweets;
	}

	@Override
	public int getNumLikes() {
		return numLikes;
	}

	@Override
	public int getNumReplies() {
		return numReplies;
	}

	@Override
	public void setIndividualTweet( ITweet individualTweet ) {
		this.individualTweet = individualTweet;
	}

	@Override
	public void setTweetID( long tweetID ) {
		this.tweetID = tweetID;
	}

	@Override
	public void setNumRetweets( int numRetweets ) {
		this.numRetweets = numRetweets;
	}

	@Override
	public void setNumLikes( int numLikes ) {
		this.numLikes = numLikes;
	}

	@Override
	public void setNumReplies( int numReplies ) {
		this.numReplies = numReplies;
	}
}
