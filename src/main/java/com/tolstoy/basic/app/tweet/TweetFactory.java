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
import java.util.Map;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.TweetUserVerifiedStatus;
import com.tolstoy.basic.app.utils.StringList;
import com.tolstoy.basic.app.utils.Utils;

public class TweetFactory implements ITweetFactory {
	private static final Logger logger = LogManager.getLogger( TweetFactory.class );

	public TweetFactory() {
	}

	@Override
	public ITweet makeTweet() {
		return new Tweet();
	}

	@Override
	public ITweet makeTweet( long id, Map<String,String> attributes, StringList classes, StringList mentions, ITweetUser user ) {
		return new Tweet( id, attributes, classes, mentions, user );
	}

	@Override
	public ITweet makeTweet( Map<String,String> data, String tweetKey, String attributesKey, String userKey ) throws Exception {
		return makeTweet( Long.parseLong( data.get( tweetKey + "id" ) ),
							Utils.copyMapWithMatchingKeys( data, attributesKey ),
							new StringList( data.get( tweetKey + "classes" ) ),
							new StringList( data.get( tweetKey + "mentions" ) ),
							makeTweetUser( data, userKey ) );
	}

	@Override
	public ITweetCollection makeTweetCollection() {
		return new TweetCollection();
	}

	@Override
	public ITweetCollection makeTweetCollection( List<ITweet> tweets, Instant retrievalTime, Map<String,String> attributes ) {
		return new TweetCollection( tweets, retrievalTime, attributes );
	}

	@Override
	public ITweetUser makeTweetUser( String handle ) {
		return new TweetUser( handle, 0, null, TweetUserVerifiedStatus.UNKNOWN, "" );
	}

	@Override
	public ITweetUser makeTweetUser( String handle, long id ) {
		return new TweetUser( handle, id, null, TweetUserVerifiedStatus.UNKNOWN, "" );
	}

	@Override
	public ITweetUser makeTweetUser( String handle, long id, String displayName ) {
		return new TweetUser( handle, id, displayName, TweetUserVerifiedStatus.UNKNOWN, "" );
	}

	@Override
	public ITweetUser makeTweetUser( String handle, long id, String displayName, TweetUserVerifiedStatus verifiedStatus ) {
		return new TweetUser( handle, id, displayName, verifiedStatus, "" );
	}

	@Override
	public ITweetUser makeTweetUser( String handle, long id, String displayName, TweetUserVerifiedStatus verifiedStatus, String avatarURL ) {
		return new TweetUser( handle, id, displayName, verifiedStatus, avatarURL );
	}

	@Override
	public ITweetUser makeTweetUser( String handle, long id, String displayName, TweetUserVerifiedStatus verifiedStatus, String avatarURL, int numTotalTweets, int numFollowers, int numFollowing ) {
		return new TweetUser( handle, id, displayName, verifiedStatus, avatarURL, numTotalTweets, numFollowers, numFollowing );
	}

	@Override
	public ITweetUser makeTweetUser( Map<String,String> data, String baseKey ) throws Exception {
		return new TweetUser( data.get( baseKey + "handle" ),
								Long.parseLong( data.get( baseKey + "id" ) ),
								Utils.trimDefault( data.get( baseKey + "display_name" ) ),
								TweetUserVerifiedStatus.valueOf( data.get( baseKey + "verified_status" ) ),
								Utils.trimDefault( data.get( baseKey + "avatar_url" ) ),
								Integer.parseInt( data.get( baseKey + "num_total_tweets" ) ),
								Integer.parseInt( data.get( baseKey + "num_followers" ) ),
								Integer.parseInt( data.get( baseKey + "num_following" ) ) );
	}
}
