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
package com.tolstoy.censorship.twitter.checker.app.preferences;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tolstoy.basic.api.storage.IStorable;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.app.storage.StorageTable;

@JsonIgnoreProperties(ignoreUnknown=true)
class Preferences implements IPreferences, IStorable {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( Preferences.class );

	@JsonProperty
	private final Map<String,String> map;

	@JsonProperty
	private long id;

	@JsonProperty
	private final Instant createTime;

	@JsonProperty
	private Instant modifyTime;

	@JsonIgnore
	private final IStorage storage;

	Preferences() {
		this.map = new HashMap<String,String>( 50 );
		this.storage = null;

		this.id = 0;
		this.createTime = this.modifyTime = Instant.now();
	}

	Preferences( final Map<String,String> defaults ) {
		this.map = new HashMap<String,String>( defaults );
		this.storage = null;

		this.id = 0;
		this.createTime = this.modifyTime = Instant.now();
	}

	Preferences( final IStorage storage, final Map<String,String> defaults ) throws Exception {
		this.storage = storage;

		this.map = new HashMap<String,String>( defaults );

		this.id = 0;
		this.createTime = this.modifyTime = Instant.now();
	}

	Preferences( final IStorage storage, final long id, final Instant createTime, final Instant modifyTime,
						final Map<String,String> overrides, final Map<String,String> defaults ) throws Exception {
		this.storage = storage;

		this.map = new HashMap<String,String>( defaults );

		this.map.putAll( overrides );

		this.id = id;
		this.createTime = createTime;
		this.modifyTime = modifyTime;
	}

	@Override
	public void save() throws Exception {
		if ( storage == null ) {
			throw new RuntimeException( "this object was not loaded from storage and cannot be saved" );
		}

		modifyTime = Instant.now();

		storage.saveRecord( StorageTable.PREFS, this );
	}

	@JsonIgnore
	@Override
	public Map<String,String> getValues() {
		return new HashMap<String,String>( map );
	}

	@JsonIgnore
	@Override
	public String getValue( final String key ) {
		return map.get( key );
	}

	@JsonIgnore
	@Override
	public boolean isEmpty( final String key ) {
		return Utils.isEmpty( map.get( key ) );
	}

	@JsonIgnore
	@Override
	public void setValue( final String key, final String value ) {
		map.put( key, value );
	}

	@Override
	public long getID() {
		return id;
	}

	@Override
	public void setID( final long id ) {
		this.id = id;
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
		return "prefs";
	}

	public void setSearchKey( final String s ) {
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "id", id )
		.append( "createTime", createTime )
		.append( "modifyTime", modifyTime )
		.append( "data", map )
		.toString();
	}
}
