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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetUserCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.TweetUserVerifiedStatus;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;

/*
 * The SuedeDenim Javascript code returns List<Map<String,String>>.
 * Each map has a map_type key: "tweet", "metadata", etc.
 * This converts that into a List<ITweet> built from the "tweet"
 * maps, and a JavascriptInterchangeMetadata (basically a map)
 * built from the "metadata" map.
 * 
 * The rest of the code doesn't need to worry about the format
 * of the data Javascript returns other than that it's a List
 * of some kind.
 */
public final class JavascriptInterchangeContainer {
	private static final Logger logger = LogManager.getLogger( JavascriptInterchangeContainer.class );

	private final JavascriptInterchangeMetadata metadata;
	private final JavascriptInterchangeSupposedQualities supposedQualities;
	private final ITweetCollection tweetCollection;
	private final ITweetUserCollection tweetUserCollection;
	private int numInputTweets = 0, numInputTweetUsers = 0;

	public JavascriptInterchangeContainer( final List rawData, final ITweetFactory tweetFactory, final IResourceBundleWithFormatting bundle ) throws Exception {
		if ( rawData == null ) {
			throw new IllegalArgumentException( "rawData is null" );
		}

		this.tweetCollection = tweetFactory.makeTweetCollection();
		this.tweetUserCollection = tweetFactory.makeTweetUserCollection();

		Map<String, String> metadataMap = null;
		Map<String, String> supposedQualitiesMap = null;

		logger.info( "\n\n\nJIC BEGINNING CTOR" );

		for ( Object temp : rawData ) {
			Map<String, String> map = Utils.makeStringMap( temp );
			String type = map.get( "map_type" );
			if ( type == null ) {
				continue;
			}

			map.remove( "map_type" );

			ITweet tweet;
			ITweetUser tweetUser;

			if ( "metadata".equals( type ) ) {
				metadataMap = map;
			}
			else if ( "tweetid_to_supposed_qualities".equals( type ) ) {
				supposedQualitiesMap = map;
			}
			else if ( "tweet".equals( type ) ) {
				try {
					numInputTweets++;

					Map<String,String> userMap = Utils.copyMapWithMatchingKeys( map, "user__" );
					tweetUser = tweetFactory.makeTweetUser( userMap.get( "handle" ) );
					tweetUser.loadFromMap( userMap );

					tweet = tweetFactory.makeTweet();
					tweet.setID( Long.parseLong( map.get( "tweetid" ) ) );
					tweet.loadFromMap( map );
					tweet.setUser( tweetUser );

					this.tweetCollection.addTweet( tweet );
					
					logger.info( "JIC added tweet: " + tweet.toDebugString( "" ) + " from\n" + Utils.prettyPrintMap( "  ", map ) );
					logger.info( "JIC added tweet user: " + tweetUser.toDebugString( "" ) + " from\n" + Utils.prettyPrintMap( "  ", userMap ) );
				}
				catch ( Exception e ) {
					logger.error( "Cannot create tweet from:\n" + Utils.prettyPrintMap( "  ", map ), e );
				}
			}
			else if ( "user".equals( type ) ) {
				try {
					numInputTweetUsers++;
					tweetUser = tweetFactory.makeTweetUser( map.get( "handle" ) );
					tweetUser.loadFromMap( map );
					this.tweetUserCollection.addTweetUser( tweetUser );

					logger.info( "JIC added user: " + tweetUser.toDebugString( "" ) + " from\n" + tweetMapToDebugString( map ) );
				}
				catch ( Exception e ) {
					logger.error( "Cannot create user from:\n" + Utils.prettyPrintMap( "  ", map ), e );
				}
			}
		}

		this.metadata = metadataMap != null ? new JavascriptInterchangeMetadata( metadataMap ) : null;
		this.supposedQualities = supposedQualitiesMap != null ? new JavascriptInterchangeSupposedQualities( supposedQualitiesMap ) : null;

		logger.info( "JIC ENDED CTOR\n\n\n" );
	}

	public JavascriptInterchangeMetadata getMetadata() {
		return metadata;
	}

	public JavascriptInterchangeSupposedQualities getSupposedQualities() {
		return supposedQualities;
	}

	public ITweetCollection getTweetCollection() {
		return tweetCollection;
	}

	public ITweetUserCollection getTweetUserCollection() {
		return tweetUserCollection;
	}

	public int getNumInputTweets() {
		return numInputTweets;
	}

	public int getNumTweetsProcessed() {
		return tweetCollection.getTweets().size();
	}

	public int getNumInputTweetUsers() {
		return numInputTweetUsers;
	}

	public int getNumTweetUsersProcessed() {
		return tweetUserCollection.getTweetUsers().size();
	}

	protected String tweetMapToDebugString( final Map<String,String> map ) {
		final Map<String,String> newMap = new HashMap<String,String>( map );
		String text;

		text = newMap.get( "tweettext" );
		if ( Utils.isEmpty( text ) ) {
			text = "[EMPTY]";
		}

		text = Utils.removeNewlines( Utils.trimDefault( StringEscapeUtils.escapeHtml4( Utils.removeAllEmojis( text ) ) ) );
		newMap.put( "tweettext", StringUtils.substring( text, 0, 20 ) );

		text = newMap.get( "tweethtml" );
		if ( Utils.isEmpty( text ) ) {
			text = "[EMPTY]";
		}

		text = Utils.removeNewlines( Utils.trimDefault( StringEscapeUtils.escapeHtml4( Utils.removeAllEmojis( text ) ) ) );
		newMap.put( "tweethtml", StringUtils.substring( text, 0, 20 ) );

		return Utils.prettyPrintMap( "  ", newMap );
	}

	public String toDebugString( String indent ) {
		List<String> list = new ArrayList<String>( 10 );

		list.add( indent + "numInputTweets=" + getNumInputTweets() );
		list.add( indent + "numTweetsProcessed=" + getNumTweetsProcessed() );
		list.add( indent + "numInputTweetUsers=" + getNumInputTweetUsers() );
		list.add( indent + "numTweetUsersProcessed=" + getNumTweetUsersProcessed() );

		if ( metadata != null ) {
			list.add( indent + "metadata=\n" + metadata.toDebugString( indent + "  " ) );
		}
		else {
			list.add( indent + "metadata=[none]" );
		}

		if ( supposedQualities != null ) {
			list.add( indent + "supposedQualities=\n" + supposedQualities.toDebugString( indent + "  " ) );
		}
		else {
			list.add( indent + "supposedQualities=[none]" );
		}

		list.add( indent + "tweetCollection=\n" + tweetCollection.toDebugString( indent + "  " ) );
		list.add( indent + "tweetUserCollection=\n" + tweetUserCollection.toDebugString( indent + "  " ) );

		return StringUtils.join( list, "\n" );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "numInputTweets", getNumInputTweets() )
		.append( "numTweetsProcessed", getNumTweetsProcessed() )
		.append( "numInputTweetUsers", getNumInputTweetUsers() )
		.append( "numTweetUsersProcessed", getNumTweetUsersProcessed() )
		.append( "metadata", metadata )
		.append( "supposedQualities", supposedQualities )
		.append( "tweetCollection", tweetCollection )
		.append( "tweetUserCollection", tweetUserCollection )
		.toString();
	}
}
