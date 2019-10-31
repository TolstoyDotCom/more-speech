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
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.app.utils.Utils;

@JsonIgnoreProperties(ignoreUnknown=true)
class TweetCollection implements ITweetCollection {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( TweetCollection.class );

	@JsonProperty
	private List<ITweet> tweets;

	@JsonProperty
	private Map<String,String> attributes;

	@JsonProperty
	private Instant retrievalTime;

	TweetCollection() {
		this.tweets = new ArrayList<ITweet>();
		this.retrievalTime = Instant.now();
		this.attributes = new HashMap<String,String>();
	}

	TweetCollection( final List<ITweet> tweets, final Instant retrievalTime, final Map<String,String> attributes ) {
		this.tweets = tweets;
		this.retrievalTime = retrievalTime;
		this.attributes = attributes;
	}

	@JsonIgnore
	@Override
	public ITweet getTweetByID( final long id ) {
		for ( final ITweet tweet : getTweets() ) {
			if ( tweet.getID() == id ) {
				return tweet;
			}
		}

		return null;
	}

	@JsonIgnore
	@Override
	public int getTweetOrderByID( final long id ) {
		int order = 1;
		for ( final ITweet tweet : getTweets() ) {
			if ( tweet.getID() == id ) {
				return order;
			}
			order++;
		}

		return 0;
	}

	@Override
	public List<ITweet> getTweets() {
		return tweets;
	}

	@Override
	public void setTweets( final List<ITweet> tweets ) {
		this.tweets = tweets;
	}

	@JsonIgnore
	@Override
	public List<ITweetUser> getTweetUsers() {
		final Set<ITweetUser> set = new HashSet<ITweetUser>( getTweets().size() );

		for ( final ITweet tweet : getTweets() ) {
			if ( tweet.getUser() != null ) {
				set.add( tweet.getUser() );
			}
		}

		return new ArrayList<ITweetUser>( set );
	}

	@JsonIgnore
	@Override
	public void addTweet( final ITweet tweet ) {
		tweets.add( tweet );
	}

	@JsonIgnore
	@Override
	public void removeTweetByID( final long id ) {
		final Iterator<ITweet> iter = getTweets().iterator();

		while ( iter.hasNext() ) {
			final ITweet tweet = iter.next();

			if ( tweet.getID() == id ) {
				iter.remove();
			}
		}
	}

	@JsonIgnore
	@Override
	public List<String> supplementFrom( final ITweetCollection otherCollection ) {
		if ( otherCollection == null || otherCollection.getTweets() == null || otherCollection.getTweets().isEmpty() ) {
			return new ArrayList<String>( 1 );
		}

		List<String> ret = new ArrayList<String>( 5 * otherCollection.getTweets().size() );

		for ( final ITweet tweet : getTweets() ) {
			long tweetID = tweet.getID();
			if ( tweetID == 0 ) {
				continue;
			}

			ITweet otherTweet = otherCollection.getTweetByID( tweetID );
			if ( otherTweet == null ) {
				continue;
			}

			List<String> messages = tweet.supplementFrom( otherTweet );

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
		final List<String> temp = new ArrayList<String>( tweets.size() + 1 );

		temp.add( indent + "retrievalTime=" + retrievalTime + ", tweets:" );

		for ( final ITweet tweet : getTweets() ) {
			temp.add( tweet.toDebugString( indent + "  " ) );
		}

		return StringUtils.join( temp, "\n" );
	}

	@JsonIgnore
	@Override
	public String toString() {
		final List<String> temp = new ArrayList<String>( getTweets().size() + 1 );

		temp.add( "TweetCollection: retrievalTime=" + retrievalTime + ", attributes=" + attributes + ", tweets:" );

		for ( final ITweet tweet : getTweets() ) {
			temp.add( "  " + tweet );
		}

		return StringUtils.join( temp, "\n" );
	}
}
