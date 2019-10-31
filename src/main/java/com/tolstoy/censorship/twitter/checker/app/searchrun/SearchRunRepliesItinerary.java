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

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunRepliesItinerary;

class SearchRunRepliesItinerary extends SearchRunItinerary implements ISearchRunRepliesItinerary {
	private static final Logger logger = LogManager.getLogger( SearchRunRepliesItinerary.class );

	private ITweetCollection tweetCollection;

	SearchRunRepliesItinerary( final ITweetUser initiatingUser, final ITweetCollection tweetCollection ) {
		super( initiatingUser );
		this.tweetCollection = tweetCollection;
	}

	SearchRunRepliesItinerary( final ITweetUser initiatingUser, final ITweetCollection tweetCollection, final Map<String,String> attributes ) {
		super( initiatingUser, attributes );
		this.tweetCollection = tweetCollection;
	}

	@Override
	public ITweetCollection getTimelineTweetCollection() {
		return tweetCollection;
	}

	@Override
	public void setTimelineTweetCollection( final ITweetCollection tweetCollection ) {
		this.tweetCollection = tweetCollection;
	}
}
