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
package com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs;

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.collections4.MapUtils;

import com.tolstoy.basic.app.utils.Utils;

public final class JavascriptInterchangeMetadata {
	private static final Logger logger = LogManager.getLogger( JavascriptInterchangeMetadata.class );

	private final static String[] knownKeys = {
		"url",
		"request_date",
		"last_compound",
		"tweet_selector",
		"show_hidden_replies",
		"show_hidden_replies2",
		"completed",
		"error_code",
		"error_message"
	};

	private final Map<String, String> map, remainderMap;

	JavascriptInterchangeMetadata( final Map<String,String> input ) throws Exception {
		if ( input == null ) {
			throw new IllegalArgumentException( "input is null" );
		}

		this.map = new HashMap<String, String>( input );
		this.remainderMap = new HashMap<String, String>( input );

		for ( String key : knownKeys ) {
			this.remainderMap.remove( key );
		}
	}

	public boolean hasError() {
		return !Utils.isEmpty( getErrorCode() ) || !Utils.isEmpty( getErrorMessage() );
	}

	public String getURL() {
		return getValue( "url" );
	}

	public String getRequestDateString() {
		return getValue( "request_date" );
	}

	public String getLastCompound() {
		return getValue( "last_compound" );
	}

	public String getTweetSelector() {
		return getValue( "tweet_selector" );
	}

	public String getShowHiddenReplies() {
		return getValue( "show_hidden_replies" );
	}

	public String getShowHiddenReplies2() {
		return getValue( "show_hidden_replies2" );
	}

	public boolean isCompleted() {
		return Utils.parseBoolean( getValue( "completed" ) );
	}

	public String getErrorCode() {
		return getValue( "error_code" );
	}

	public String getErrorMessage() {
		return getValue( "error_message" );
	}

	public String getValue( String key ) {
		return MapUtils.getString( map, key, "" );
	}

	public String getValue( String key, String defaultValue ) {
		return MapUtils.getString( map, key, defaultValue );
	}

	public Map<String,String> getMap() {
		return new HashMap<String, String>( map );
	}

	public Map<String,String> getRemainder() {
		return new HashMap<String, String>( remainderMap );
	}

	public String toDebugString( String indent ) {
		return indent +
				"map:\n" +
				Utils.prettyPrintMap( indent + "  ", map ) +
				"\nremainder:\n" +
				Utils.prettyPrintMap( indent + "  ", remainderMap );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "map", map )
		.toString();
	}
}
