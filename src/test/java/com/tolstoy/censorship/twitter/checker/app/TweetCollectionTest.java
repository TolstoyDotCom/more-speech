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
package com.tolstoy.censorship.twitter.checker.app;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.tweet.*;
import com.tolstoy.basic.app.utils.*;
import com.tolstoy.basic.app.tweet.TweetFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class TweetCollectionTest extends TestCase {
	private static final Logger logger = LogManager.getLogger( TweetCollectionTest.class );

	private ITweetFactory tweetFactory;

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public TweetCollectionTest( String testName ) {
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite( TweetCollectionTest.class );
	}

	protected void setUp() throws Exception {
		tweetFactory = new TweetFactory();
	}

	protected void tearDown() throws Exception {
		tweetFactory = null;
	}

	/**
	 */
	public void testRawTweetCollection() throws Exception {
		ITweetCollection tweetCollection;
		List<ITweet> tweets;
		Map<String,String> attributes;
		ITweetUser user;
		ITweet tempTweet;

		tweetCollection = tweetFactory.makeTweetCollection();

		attributes = new HashMap<String,String>( 1 );
		attributes.put( "handle", "test1" );
		attributes.put( "user", "user1" );
		user = tweetFactory.makeTweetUser( "testuser1" );
		tempTweet = tweetFactory.makeTweet( 123, attributes, new StringList( "class1a class1b" ),
											new StringList( "mention1a mention1b" ), user );
		tweetCollection.addTweet( tempTweet );

		attributes = new HashMap<String,String>( 1 );
		attributes.put( "handle", "test2" );
		attributes.put( "user", "user2" );
		user = tweetFactory.makeTweetUser( "testuser2" );
		tempTweet = tweetFactory.makeTweet( 456, attributes, new StringList( "class2a class2b" ),
											new StringList( "mention2a mention2b" ), user );
		tweetCollection.addTweet( tempTweet );

		String json = Utils.getDefaultObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString( tweetCollection );

		tweetCollection = (ITweetCollection) Utils.getDefaultObjectMapper().readValue( json, Object.class );

		tweets = tweetCollection.getTweets();
		for ( ITweet tweet : tweets ) {
			if ( tweet.getID() == 123 ) {
				assertEquals( "test1", tweet.getAttribute( "handle" ) );
				assertEquals( "user1", tweet.getAttribute( "user" ) );
				assertEquals( "class1a,class1b", tweet.getClasses().getOriginal() );
				assertEquals( "mention1a,mention1b", tweet.getMentions().getOriginal() );
			}
			else if ( tweet.getID() == 456 ) {
				assertEquals( "test2", tweet.getAttribute( "handle" ) );
				assertEquals( "user2", tweet.getAttribute( "user" ) );
				assertEquals( "class2a,class2b", tweet.getClasses().getOriginal() );
				assertEquals( "mention2a,mention2b", tweet.getMentions().getOriginal() );
			}
			else {
				fail( "unexpected tweet in list" );
			}
		}
	}
}
