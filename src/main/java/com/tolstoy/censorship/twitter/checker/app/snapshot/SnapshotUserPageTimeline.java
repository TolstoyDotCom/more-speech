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
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;

@JsonIgnoreProperties(ignoreUnknown=true)
class SnapshotUserPageTimeline extends SnapshotUserPage implements ISnapshotUserPageTimeline {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( SnapshotUserPageTimeline.class );

	@JsonProperty
	private int numTotalTweets;

	@JsonProperty
	private int numFollowers;

	@JsonProperty
	private int numFollowing;

	SnapshotUserPageTimeline() {
		super( "", Instant.now() );
	}

	SnapshotUserPageTimeline( final String url, final Instant retrievalTime ) {
		super( url, retrievalTime );
	}

	@Override
	public int getNumTotalTweets() {
		return numTotalTweets;
	}

	@Override
	public int getNumFollowers() {
		return numFollowers;
	}

	@Override
	public int getNumFollowing() {
		return numFollowing;
	}

	@Override
	public void setNumTotalTweets( final int numTotalTweets ) {
		this.numTotalTweets = numTotalTweets;
	}

	@Override
	public void setNumFollowers( final int numFollowers ) {
		this.numFollowers = numFollowers;
	}

	@Override
	public void setNumFollowing( final int numFollowing ) {
		this.numFollowing = numFollowing;
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.appendSuper( super.toString() )
		.append( "numTotalTweets", numTotalTweets )
		.append( "numFollowers", numFollowers )
		.append( "numFollowing", numFollowing )
		.toString();
	}
}
