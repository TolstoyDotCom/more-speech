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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweet;

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

	TweetCollection( List<ITweet> tweets, Instant retrievalTime, Map<String,String> attributes ) {
		this.tweets = tweets;
		this.retrievalTime = retrievalTime;
		this.attributes = attributes;
	}

	@Override
	public ITweet getTweetByID( long id ) {
		for ( ITweet tweet : tweets ) {
			if ( tweet.getID() == id ) {
				return tweet;
			}
		}

		return null;
	}

	@Override
	public int getTweetOrderByID( long id ) {
		int order = 1;
		for ( ITweet tweet : tweets ) {
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
	public void setTweets( List<ITweet> tweets ) {
		this.tweets = tweets;
	}

	@Override
	public void addTweet( ITweet tweet ) {
		tweets.add( tweet );
	}

	@Override
	public void removeTweetByID( long id ) {
		Iterator<ITweet> iter = tweets.iterator();

		while ( iter.hasNext() ) {
			ITweet tweet = iter.next();

			if ( tweet.getID() == id ) {
				iter.remove();
			}
		}
	}

	@Override
	public Instant getRetrievalTime() {
		return retrievalTime;
	}

	@Override
	public void setRetrievalTime( Instant retrievalTime ) {
		this.retrievalTime = retrievalTime;
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
		List<String> temp = new ArrayList<String>( tweets.size() + 1 );

		temp.add( "TweetCollection: retrievalTime=" + retrievalTime + ", attributes=" + attributes + ", tweets:" );

		for ( ITweet tweet : getTweets() ) {
			temp.add( "  " + tweet );
		}

		return StringUtils.join( temp, "\n" );
	}
}
