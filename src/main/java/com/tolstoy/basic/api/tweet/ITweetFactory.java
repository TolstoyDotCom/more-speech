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

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.Instant;
import com.tolstoy.basic.app.utils.StringList;

public interface ITweetFactory {
	ITweet makeTweet();
	ITweet makeTweet( long id, Map<String,String> attributes, StringList classes, StringList mentions, ITweetUser user );

	ITweetCollection makeTweetCollection();
	ITweetCollection makeTweetCollection( List<ITweet> tweets, Instant retrievalTime, Map<String,String> attributes );

	ITweetUser makeTweetUser( String handle );
	ITweetUser makeTweetUser( String handle, long id );
	ITweetUser makeTweetUser( String handle, long id, String displayName );
	ITweetUser makeTweetUser( String handle, long id, String displayName, TweetUserVerifiedStatus verifiedStatus );
	ITweetUser makeTweetUser( String handle, long id, String displayName, TweetUserVerifiedStatus verifiedStatus, String avatarURL );
}
