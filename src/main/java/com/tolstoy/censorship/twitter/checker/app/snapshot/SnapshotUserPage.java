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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPage;

@JsonIgnoreProperties(ignoreUnknown=true)
class SnapshotUserPage extends Snapshot implements ISnapshotUserPage {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( SnapshotUserPage.class );

	@JsonProperty
	private ITweetUser user;

	@JsonProperty
	private ITweetCollection tweetCollection;

	SnapshotUserPage() {
		super( "", Instant.now() );
	}

	SnapshotUserPage( final String url, final Instant retrievalTime ) {
		super( url, retrievalTime );
	}

	@Override
	public ITweetUser getUser() {
		return user;
	}

	@Override
	public ITweetCollection getTweetCollection() {
		return tweetCollection;
	}

	@Override
	public void setUser( final ITweetUser user ) {
		this.user = user;
	}

	@Override
	public void setTweetCollection( final ITweetCollection tweetCollection ) {
		this.tweetCollection = tweetCollection;
	}

	@Override
	public void addTweet( final ITweet tweet ) {
		tweetCollection.addTweet( tweet );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.appendSuper( super.toString() )
		.append( "user", user )
		.append( "tweetCollection", tweetCollection )
		.toString();
	}
}
