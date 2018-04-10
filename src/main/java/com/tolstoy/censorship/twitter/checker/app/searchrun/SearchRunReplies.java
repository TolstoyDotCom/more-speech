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
class SearchRunReplies extends SearchRun implements ISearchRunReplies {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( SearchRunReplies.class );

	@JsonProperty
	private ISnapshotUserPageTimeline timeline;

	@JsonProperty
	private Map<Long,IReplyThread> replies;

	SearchRunReplies() {
		super( 0, null );
	}

	SearchRunReplies( long id, ITweetUser user ) {
		super( id, user );

		this.timeline = null;
		this.replies = new HashMap<Long,IReplyThread>();
	}

	SearchRunReplies( long id, ITweetUser user, Instant startTime, Instant endTime ) {
		super( id, user, startTime, endTime );

		this.timeline = null;
		this.replies = new HashMap<Long,IReplyThread>();
	}

	SearchRunReplies( long id, ITweetUser user, Instant startTime, Instant endTime, Map<String,String> attributes ) {
		super( id, user, startTime, endTime, attributes );

		this.timeline = null;
		this.replies = new HashMap<Long,IReplyThread>();
	}

	SearchRunReplies( long id, ITweetUser user, Instant startTime, Instant endTime,
								ISnapshotUserPageTimeline timeline,
								Map<Long,IReplyThread> replies ) {
		super( id, user, startTime, endTime );

		this.timeline = timeline;
		this.replies = replies;
	}

	SearchRunReplies( long id, ITweetUser user, Instant startTime, Instant endTime,
								ISnapshotUserPageTimeline timeline,
								Map<Long,IReplyThread> replies,
								Map<String,String> attributes ) {
		super( id, user, startTime, endTime, attributes );

		this.timeline = timeline;
		this.replies = replies;
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
	public Map<Long,IReplyThread> getReplies() {
		return replies;
	}

	@Override
	public void setReplies( Map<Long,IReplyThread> replies ) {
		this.replies = replies;
	}

	@JsonIgnore
	@Override
	public void setReply( long sourceTweetID, IReplyThread replyThread ) {
		replies.put( sourceTweetID, replyThread );
	}

	@JsonIgnore
	@Override
	public Set<Long> getSourceTweetIDs() {
		return replies.keySet();
	}

	@JsonIgnore
	@Override
	public IReplyThread getReplyThreadBySourceTweetID( long sourceTweetID ) {
		return replies.get( sourceTweetID );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.appendSuper( super.toString() )
		.append( "timeline", timeline )
		.append( "replies", replies )
		.toString();
	}
}
