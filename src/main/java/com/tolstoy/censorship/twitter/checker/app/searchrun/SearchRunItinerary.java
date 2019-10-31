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

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunItinerary;

class SearchRunItinerary implements ISearchRunItinerary {
	private static final Logger logger = LogManager.getLogger( SearchRunItinerary.class );

	private ITweetUser initiatingUser;
	private Map<String,String> attributes;

	SearchRunItinerary( final ITweetUser initiatingUser ) {
		this.initiatingUser = initiatingUser;
		this.attributes = new HashMap<String,String>();
	}

	SearchRunItinerary( final ITweetUser initiatingUser, final Map<String,String> attributes ) {
		this.initiatingUser = initiatingUser;
		this.attributes = attributes;
	}

	@Override
	public ITweetUser getInitiatingUser() {
		return initiatingUser;
	}

	@Override
	public void setInitiatingUser( final ITweetUser initiatingUser ) {
		this.initiatingUser = initiatingUser;
	}

	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes( final Map<String,String> attributes ) {
		this.attributes = attributes;
	}

	@Override
	public String getAttribute( final String key ) {
		return attributes.get( key );
	}

	@Override
	public void setAttribute( final String key, final String value ) {
		attributes.put( key, value );
	}
}
