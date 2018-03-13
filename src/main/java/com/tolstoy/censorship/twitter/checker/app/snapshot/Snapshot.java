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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshot;

@JsonIgnoreProperties(ignoreUnknown=true)
class Snapshot implements ISnapshot {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( Snapshot.class );

	@JsonProperty
	private String url;

	@JsonProperty
	private String title;

	@JsonProperty
	private Instant retrievalTime;

	@JsonProperty
	private boolean complete;

	Snapshot( String url, Instant retrievalTime ) {
		this.url = url;
		this.retrievalTime = retrievalTime;
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public Instant getRetrievalTime() {
		return retrievalTime;
	}

	@Override
	public boolean getComplete() {
		return complete;
	}

	@Override
	public void setURL( String url ) {
		this.url = url;
	}

	@Override
	public void setTitle( String title ) {
		this.title = title;
	}

	@Override
	public void setRetrievalTime( Instant retrievalTime ) {
		this.retrievalTime = retrievalTime;
	}

	@Override
	public void setComplete( boolean complete ) {
		this.complete = complete;
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "url", url )
		.append( "title", title )
		.append( "retrievalTime", retrievalTime )
		.append( "complete", complete )
		.toString();
	}
}
