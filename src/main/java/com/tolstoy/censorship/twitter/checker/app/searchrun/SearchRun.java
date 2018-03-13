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
import com.tolstoy.basic.api.storage.IStorable;
import com.tolstoy.basic.api.tweet.*;

@JsonIgnoreProperties(ignoreUnknown=true)
class SearchRun implements ISearchRun, IStorable {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( SearchRun.class );

	@JsonProperty
	private ITweetUser user;

	@JsonProperty
	private Map<String,String> attributes;

	@JsonProperty
	private Instant startTime;

	@JsonProperty
	private Instant endTime;

	@JsonProperty
	private long id;

	@JsonProperty
	private Instant createTime;

	@JsonProperty
	private Instant modifyTime;

	SearchRun( long id, ITweetUser user ) {
		this.id = id;
		this.user = user;
		this.startTime = Instant.now();
		this.endTime = Instant.now();
		this.attributes = new HashMap<String,String>();
	}

	SearchRun( long id, ITweetUser user, Instant startTime, Instant endTime ) {
		this.id = id;
		this.user = user;
		this.startTime = startTime;
		this.endTime = endTime;
		this.attributes = new HashMap<String,String>();
	}

	SearchRun( long id, ITweetUser user, Instant startTime, Instant endTime, Map<String,String> attributes ) {
		this.id = id;
		this.user = user;
		this.startTime = startTime;
		this.endTime = endTime;
		this.attributes = attributes;
	}

	@Override
	public long getID() {
		return id;
	}

	@Override
	public void setID( long id ) {
		this.id = id;
	}

	@Override
	public ITweetUser getInitiatingUser() {
		return user;
	}

	@Override
	public void setInitiatingUser( ITweetUser user ) {
		this.user = user;
	}

	@Override
	public Instant getStartTime() {
		return startTime;
	}

	@Override
	public void setStartTime( Instant startTime ) {
		this.startTime = startTime;
	}

	@Override
	public Instant getEndTime() {
		return endTime;
	}

	@Override
	public void setEndTime( Instant endTime ) {
		this.endTime = endTime;
	}

	@Override
	public Instant getCreateTime() {
		return createTime;
	}

	@Override
	public Instant getModifyTime() {
		return modifyTime;
	}

	@Override
	public String getSearchKey() {
		return user != null ? user.getHandle() : "unknown";
	}

	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes( Map<String,String> attributes ) {
		this.attributes = attributes;
	}

	@JsonIgnore
	@Override
	public String getAttribute( String key ) {
		return attributes.get( key );
	}

	@JsonIgnore
	@Override
	public void setAttribute( String key, String value ) {
		attributes.put( key, value );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "id", id )
		.append( "createTime", createTime )
		.append( "modifyTime", modifyTime )
		.append( "startTime", startTime )
		.append( "endTime", endTime )
		.append( "user", user )
		.append( "attributes", attributes )
		.toString();
	}
}
