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
package com.tolstoy.censorship.twitter.checker.app.searchrun;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunTimeline;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;

@JsonIgnoreProperties(ignoreUnknown=true)
class SearchRunTimeline extends SearchRun implements ISearchRunTimeline {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( SearchRunTimeline.class );

	@JsonProperty
	private ISnapshotUserPageTimeline timeline;

	@JsonProperty
	private Map<Long,ISnapshotUserPageIndividualTweet> individualPages;

	SearchRunTimeline() {
		super( 0, null );
	}

	SearchRunTimeline( final long id, final ITweetUser user ) {
		super( id, user );

		this.timeline = null;
		this.individualPages = new HashMap<Long,ISnapshotUserPageIndividualTweet>();
	}

	SearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime ) {
		super( id, user, startTime, endTime );

		this.timeline = null;
		this.individualPages = new HashMap<Long,ISnapshotUserPageIndividualTweet>();
	}

	SearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime, final Map<String,String> attributes ) {
		super( id, user, startTime, endTime, attributes );

		this.timeline = null;
		this.individualPages = new HashMap<Long,ISnapshotUserPageIndividualTweet>();
	}

	SearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
								final ISnapshotUserPageTimeline timeline,
								final Map<Long,ISnapshotUserPageIndividualTweet> individualPages ) {
		super( id, user, startTime, endTime );

		this.timeline = timeline;
		this.individualPages = individualPages;
	}

	SearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
								final ISnapshotUserPageTimeline timeline,
								final Map<Long,ISnapshotUserPageIndividualTweet> individualPages,
								final Map<String,String> attributes ) {
		super( id, user, startTime, endTime, attributes );

		this.timeline = timeline;
		this.individualPages = individualPages;
	}

	@Override
	public ISnapshotUserPageTimeline getTimeline() {
		return timeline;
	}

	@Override
	public void setSnapshotUserPageTimeline( final ISnapshotUserPageTimeline timeline ) {
		this.timeline = timeline;
	}

	@Override
	public Map<Long,ISnapshotUserPageIndividualTweet> getIndividualPages() {
		return individualPages;
	}

	@Override
	public void setReplies( final Map<Long,ISnapshotUserPageIndividualTweet> individualPages ) {
		this.individualPages = individualPages;
	}

	@JsonIgnore
	@Override
	public void setReply( final long sourceTweetID, final ISnapshotUserPageIndividualTweet individualPage ) {
		individualPages.put( sourceTweetID, individualPage );
	}

	@JsonIgnore
	@Override
	public Set<Long> getSourceTweetIDs() {
		return individualPages.keySet();
	}

	@JsonIgnore
	@Override
	public ISnapshotUserPageIndividualTweet getIndividualPageBySourceTweetID( final long sourceTweetID ) {
		return individualPages.get( sourceTweetID );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.appendSuper( super.toString() )
		.append( "timeline", timeline )
		.append( "individualPages", individualPages )
		.toString();
	}
}
