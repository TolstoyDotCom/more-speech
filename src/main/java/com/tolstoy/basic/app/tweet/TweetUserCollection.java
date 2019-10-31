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
package com.tolstoy.basic.app.tweet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.ITweetUserCollection;
import com.tolstoy.basic.app.utils.Utils;

@JsonIgnoreProperties(ignoreUnknown=true)
class TweetUserCollection implements ITweetUserCollection {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( TweetUserCollection.class );

	@JsonProperty
	private List<ITweetUser> tweetUsers;

	@JsonProperty
	private Map<String,String> attributes;

	@JsonProperty
	private Instant retrievalTime;

	TweetUserCollection() {
		this.tweetUsers = new ArrayList<ITweetUser>();
		this.retrievalTime = Instant.now();
		this.attributes = new HashMap<String,String>();
	}

	TweetUserCollection( final List<ITweetUser> tweetUsers, final Instant retrievalTime, final Map<String,String> attributes ) {
		this.tweetUsers = tweetUsers;
		this.retrievalTime = retrievalTime;
		this.attributes = attributes;
	}

	@JsonIgnore
	@Override
	public ITweetUser getTweetUserByID( final long id ) {
		if ( id == 0 ) {
			return null;
		}

		for ( final ITweetUser tweetUser : tweetUsers ) {
			if ( tweetUser.getID() == id ) {
				return tweetUser;
			}
		}

		return null;
	}

	@JsonIgnore
	@Override
	public ITweetUser getTweetUserByHandle( final String handle ) {
		if ( Utils.isEmpty( handle ) ) {
			return null;
		}

		for ( final ITweetUser tweetUser : tweetUsers ) {
			if ( StringUtils.equalsIgnoreCase( handle, tweetUser.getAttribute( "handle" ) ) ) {
				return tweetUser;
			}
		}

		return null;
	}

	@JsonIgnore
	@Override
	public int getTweetUserOrderByID( final long id ) {
		int order = 1;
		for ( final ITweetUser tweetUser : tweetUsers ) {
			if ( tweetUser.getID() == id ) {
				return order;
			}
			order++;
		}

		return 0;
	}

	@Override
	public List<ITweetUser> getTweetUsers() {
		return tweetUsers;
	}

	@Override
	public void setTweetUsers( final List<ITweetUser> tweetUsers ) {
		this.tweetUsers = tweetUsers;
	}

	@JsonIgnore
	@Override
	public void addTweetUser( final ITweetUser tweetUser ) {
		tweetUsers.add( tweetUser );
	}

	@JsonIgnore
	@Override
	public void removeTweetUserByID( final long id ) {
		final Iterator<ITweetUser> iter = tweetUsers.iterator();

		while ( iter.hasNext() ) {
			final ITweetUser tweetUser = iter.next();

			if ( tweetUser.getID() == id ) {
				iter.remove();
			}
		}
	}

	@JsonIgnore
	@Override
	public List<String> supplementFrom( final ITweetUserCollection otherCollection ) {
		if ( otherCollection == null || otherCollection.getTweetUsers() == null || otherCollection.getTweetUsers().isEmpty() ) {
			return new ArrayList<String>( 1 );
		}

		List<String> ret = new ArrayList<String>( 5 * otherCollection.getTweetUsers().size() );

		ITweetUser otherTweetUser;

		for ( final ITweetUser tweetUser : getTweetUsers() ) {
			otherTweetUser = otherCollection.getTweetUserByID( tweetUser.getID() );

			if ( otherTweetUser == null ) {
				otherTweetUser = otherCollection.getTweetUserByHandle( tweetUser.getAttribute( "handle" ) );
			}

			if ( otherTweetUser == null ) {
				continue;
			}

			List<String> messages = tweetUser.supplementFrom( otherTweetUser );

			ret.addAll( messages );
		}

		return ret;
	}

	@Override
	public Instant getRetrievalTime() {
		return retrievalTime;
	}

	@Override
	public void setRetrievalTime( final Instant retrievalTime ) {
		this.retrievalTime = retrievalTime;
	}

	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes( final Map<String,String> attributes ) {
		this.attributes = attributes;
	}

	@JsonIgnore
	@Override
	public String getAttribute( final String key ) {
		return attributes.get( key );
	}

	@JsonIgnore
	@Override
	public void setAttribute( final String key, final String value ) {
		attributes.put( key, value );
	}

	@JsonIgnore
	@Override
	public String toDebugString( String indent ) {
		final List<String> temp = new ArrayList<String>( tweetUsers.size() + 1 );

		temp.add( indent + "retrievalTime=" + retrievalTime + ", tweetUsers:" );

		for ( final ITweetUser tweetUser : getTweetUsers() ) {
			temp.add( tweetUser.toDebugString( indent + "  " ) );
		}

		return StringUtils.join( temp, "\n" );
	}

	@JsonIgnore
	@Override
	public String toString() {
		final List<String> temp = new ArrayList<String>( tweetUsers.size() + 1 );

		temp.add( "TweetUserCollection: retrievalTime=" + retrievalTime + ", attributes=" + attributes + ", tweetUsers:" );

		for ( final ITweetUser tweetUser : getTweetUsers() ) {
			temp.add( "  " + tweetUser );
		}

		return StringUtils.join( temp, "\n" );
	}
}
