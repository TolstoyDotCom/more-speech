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
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.ObjectUtils;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.ITweetUserCollection;
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
	public ITweet makeTweet( final long id, final Map<String,String> attributes, final StringList classes, final StringList mentions, final ITweetUser user ) {
		return new Tweet( id, attributes, classes, mentions, user );
	}

	@Override
	public ITweet makeTweet( final Map<String,String> data, final String tweetKey, final String attributesKey, final String userKey ) throws Exception {
		String idKey = tweetKey + "id";

		try {
			return makeTweet( Long.parseLong( data.get( idKey ) ),
								Utils.copyMapWithMatchingKeys( data, attributesKey ),
								new StringList( data.get( tweetKey + "classes" ) ),
								new StringList( data.get( tweetKey + "mentions" ) ),
								makeTweetUser( data, userKey ) );
		}
		catch ( NumberFormatException e ) {
			logger.info( "makeTweet cannot parse a number, idKey=" + idKey + ", data=\n" + Utils.prettyPrintMap( "  ", data ) );
			throw e;
		}
	}

	@Override
	public ITweetCollection makeTweetCollection() {
		return new TweetCollection();
	}

	@Override
	public ITweetCollection makeTweetCollection( final List<ITweet> tweets, final Instant retrievalTime, final Map<String,String> attributes ) {
		return new TweetCollection( tweets, retrievalTime, attributes );
	}

	@Override
	public ITweetUserCollection makeTweetUserCollection() {
		return new TweetUserCollection();
	}

	@Override
	public ITweetUserCollection makeTweetUserCollection( final List<ITweetUser> tweetUsers, final Instant retrievalTime, final Map<String,String> attributes ) {
		return new TweetUserCollection( tweetUsers, retrievalTime, attributes );
	}

	@Override
	public ITweetUser makeTweetUser( final String handle ) {
		return new TweetUser( handle, 0, null, TweetUserVerifiedStatus.UNKNOWN, "" );
	}

	@Override
	public ITweetUser makeTweetUser( final String handle, final long id ) {
		return new TweetUser( handle, id, null, TweetUserVerifiedStatus.UNKNOWN, "" );
	}

	@Override
	public ITweetUser makeTweetUser( final String handle, final long id, final String displayName ) {
		return new TweetUser( handle, id, displayName, TweetUserVerifiedStatus.UNKNOWN, "" );
	}

	@Override
	public ITweetUser makeTweetUser( final String handle, final long id, final String displayName, final TweetUserVerifiedStatus verifiedStatus ) {
		return new TweetUser( handle, id, displayName, verifiedStatus, "" );
	}

	@Override
	public ITweetUser makeTweetUser( final String handle, final long id, final String displayName, final TweetUserVerifiedStatus verifiedStatus, final String avatarURL ) {
		return new TweetUser( handle, id, displayName, verifiedStatus, avatarURL );
	}

	@Override
	public ITweetUser makeTweetUser( final String handle, final long id, final String displayName, final TweetUserVerifiedStatus verifiedStatus, final String avatarURL, final int numTotalTweets, final int numFollowers, final int numFollowing ) {
		return new TweetUser( handle, id, displayName, verifiedStatus, avatarURL, numTotalTweets, numFollowers, numFollowing );
	}

	@Override
	public ITweetUser makeTweetUser( final Map<String,String> data, final String baseKey ) throws Exception {
		try {
			Map<String,String> map = Utils.copyMapWithMatchingKeys( data, baseKey );

			ITweetUser tweetUser = new TweetUser( map.get( "handle" ), Long.parseLong( map.get( "id" ) ), null, TweetUserVerifiedStatus.UNKNOWN, "" );

			tweetUser.loadFromMap( map );

			return tweetUser;
		}
		catch ( Exception e ) {
			logger.info( "makeTweetUser failed to create user, baseKey=" + baseKey + ", data=\n" + Utils.prettyPrintMap( "  ", data ) );
			throw e;
		}
	}
}
