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

import java.util.*;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.censorship.twitter.checker.api.searchrun.*;
import com.tolstoy.censorship.twitter.checker.api.snapshot.*;
import com.tolstoy.basic.api.tweet.ITweetUser;

public class SearchRunFactory implements ISearchRunFactory {
	private static final Logger logger = LogManager.getLogger( SearchRunFactory.class );

	public SearchRunFactory() {
	}

	@Override
	public ISearchRunReplies makeSearchRunReplies( long id, ITweetUser user, Instant startTime, Instant endTime ) {
		return new SearchRunReplies( id, user, startTime, endTime );
	}

	@Override
	public ISearchRunReplies makeSearchRunReplies( long id, ITweetUser user, Instant startTime, Instant endTime,
											ISnapshotUserPageTimeline timeline,
											Map<Long,ISnapshotUserPageIndividualTweet> replies ) {
		return new SearchRunReplies( id, user, startTime, endTime, timeline, replies );
	}

	@Override
	public ISearchRunReplies makeSearchRunReplies( long id, ITweetUser user, Instant startTime, Instant endTime,
													Map<String,String> attributes ) {
		return new SearchRunReplies( id, user, startTime, endTime, attributes );
	}

	@Override
	public ISearchRunReplies makeSearchRunReplies( long id, ITweetUser user, Instant startTime, Instant endTime,
											ISnapshotUserPageTimeline timeline,
											Map<Long,ISnapshotUserPageIndividualTweet> replies,
											Map<String,String> attributes ) {
		return new SearchRunReplies( id, user, startTime, endTime, timeline, replies, attributes );
	}
}
