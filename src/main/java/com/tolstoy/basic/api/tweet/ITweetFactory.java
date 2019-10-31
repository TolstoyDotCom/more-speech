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
package com.tolstoy.basic.api.tweet;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.tolstoy.basic.app.utils.StringList;

public interface ITweetFactory {
	ITweet makeTweet();
	ITweet makeTweet( final long id, final Map<String,String> attributes, final StringList classes, final StringList mentions, final ITweetUser user );
	ITweet makeTweet( final Map<String,String> data, final String tweetKey, final String attributesKey, final String userKey ) throws Exception;

	ITweetCollection makeTweetCollection();
	ITweetCollection makeTweetCollection( final List<ITweet> tweets, final Instant retrievalTime, final Map<String,String> attributes );

	ITweetUserCollection makeTweetUserCollection();
	ITweetUserCollection makeTweetUserCollection( final List<ITweetUser> tweetUsers, final Instant retrievalTime, final Map<String,String> attributes );

	ITweetUser makeTweetUser( final String handle );
	ITweetUser makeTweetUser( final String handle, final long id );
	ITweetUser makeTweetUser( final String handle, final long id, final String displayName );
	ITweetUser makeTweetUser( final String handle, final long id, final String displayName, final TweetUserVerifiedStatus verifiedStatus );
	ITweetUser makeTweetUser( final String handle, final long id, final String displayName, final TweetUserVerifiedStatus verifiedStatus, final String avatarURL );
	ITweetUser makeTweetUser( final String handle, final long id, final String displayName, final TweetUserVerifiedStatus verifiedStatus, final String avatarURL, final int numTotalTweets, final int numFollowers, final int numFollowing );

	/**
	 * Construct from a map containing fields in String form.
	 * @param data must contain these keys:
	 * <ul>
	 * <li>id (parseable as a Long)</li>
	 * <li>verifiedStatus @see TweetUserVerifiedStatus</li>
	 * <li>numTotalTweets (parseable as an Integer)</li>
	 * <li>numFollowers (parseable as an Integer)</li>
	 * <li>numFollowing (parseable as an Integer)</li>
	 * </ul>
	 * It can also contain handle, displayName, and avatarURL keys.
	 * @param baseKey the first part of each key of the map or an empty String
	 */
	ITweetUser makeTweetUser( final Map<String,String> data, final String baseKey ) throws Exception;
}
