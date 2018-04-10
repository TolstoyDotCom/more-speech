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

import java.util.*;
import java.time.Instant;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tolstoy.censorship.twitter.checker.api.searchrun.*;
import com.tolstoy.censorship.twitter.checker.api.snapshot.*;
import com.tolstoy.basic.api.tweet.ITweetUser;

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

	SearchRunTimeline( long id, ITweetUser user ) {
		super( id, user );

		this.timeline = null;
		this.individualPages = new HashMap<Long,ISnapshotUserPageIndividualTweet>();
	}

	SearchRunTimeline( long id, ITweetUser user, Instant startTime, Instant endTime ) {
		super( id, user, startTime, endTime );

		this.timeline = null;
		this.individualPages = new HashMap<Long,ISnapshotUserPageIndividualTweet>();
	}

	SearchRunTimeline( long id, ITweetUser user, Instant startTime, Instant endTime, Map<String,String> attributes ) {
		super( id, user, startTime, endTime, attributes );

		this.timeline = null;
		this.individualPages = new HashMap<Long,ISnapshotUserPageIndividualTweet>();
	}

	SearchRunTimeline( long id, ITweetUser user, Instant startTime, Instant endTime,
								ISnapshotUserPageTimeline timeline,
								Map<Long,ISnapshotUserPageIndividualTweet> individualPages ) {
		super( id, user, startTime, endTime );

		this.timeline = timeline;
		this.individualPages = individualPages;
	}

	SearchRunTimeline( long id, ITweetUser user, Instant startTime, Instant endTime,
								ISnapshotUserPageTimeline timeline,
								Map<Long,ISnapshotUserPageIndividualTweet> individualPages,
								Map<String,String> attributes ) {
		super( id, user, startTime, endTime, attributes );

		this.timeline = timeline;
		this.individualPages = individualPages;
	}

	@Override
	public ISnapshotUserPageTimeline getTimeline() {
		return timeline;
	}

	@Override
	public void setSnapshotUserPageTimeline( ISnapshotUserPageTimeline timeline ) {
		this.timeline = timeline;
	}

	@Override
	public Map<Long,ISnapshotUserPageIndividualTweet> getIndividualPages() {
		return individualPages;
	}

	@Override
	public void setReplies( Map<Long,ISnapshotUserPageIndividualTweet> individualPages ) {
		this.individualPages = individualPages;
	}

	@JsonIgnore
	@Override
	public void setReply( long sourceTweetID, ISnapshotUserPageIndividualTweet individualPage ) {
		individualPages.put( sourceTweetID, individualPage );
	}

	@JsonIgnore
	@Override
	public Set<Long> getSourceTweetIDs() {
		return individualPages.keySet();
	}

	@JsonIgnore
	@Override
	public ISnapshotUserPageIndividualTweet getIndividualPageBySourceTweetID( long sourceTweetID ) {
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
