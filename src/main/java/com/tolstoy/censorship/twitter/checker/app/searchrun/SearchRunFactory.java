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
import com.tolstoy.basic.api.tweet.*;
import com.tolstoy.basic.app.utils.Utils;

public class SearchRunFactory implements ISearchRunFactory {
	private static final Logger logger = LogManager.getLogger( SearchRunFactory.class );

	private ITweetFactory tweetFactory;

	public SearchRunFactory( ITweetFactory tweetFactory ) {
		this.tweetFactory = tweetFactory;
	}

	@Override
	public ISearchRunReplies makeSearchRunReplies( long id, ITweetUser user, Instant startTime, Instant endTime ) {
		return new SearchRunReplies( id, user, startTime, endTime );
	}

	@Override
	public ISearchRunReplies makeSearchRunReplies( long id, ITweetUser user, Instant startTime, Instant endTime,
											ISnapshotUserPageTimeline timeline,
											Map<Long,IReplyThread> replies ) {
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
											Map<Long,IReplyThread> replies,
											Map<String,String> attributes ) {
		return new SearchRunReplies( id, user, startTime, endTime, timeline, replies, attributes );
	}

	@Override
	public ISearchRunTimeline makeSearchRunTimeline( long id, ITweetUser user, Instant startTime, Instant endTime ) {
		return new SearchRunTimeline( id, user, startTime, endTime );
	}

	@Override
	public ISearchRunTimeline makeSearchRunTimeline( long id, ITweetUser user, Instant startTime, Instant endTime,
											ISnapshotUserPageTimeline timeline,
											Map<Long,ISnapshotUserPageIndividualTweet> replies ) {
		return new SearchRunTimeline( id, user, startTime, endTime, timeline, replies );
	}

	@Override
	public ISearchRunTimeline makeSearchRunTimeline( long id, ITweetUser user, Instant startTime, Instant endTime,
													Map<String,String> attributes ) {
		return new SearchRunTimeline( id, user, startTime, endTime, attributes );
	}

	@Override
	public ISearchRunTimeline makeSearchRunTimeline( long id, ITweetUser user, Instant startTime, Instant endTime,
											ISnapshotUserPageTimeline timeline,
											Map<Long,ISnapshotUserPageIndividualTweet> replies,
											Map<String,String> attributes ) {
		return new SearchRunTimeline( id, user, startTime, endTime, timeline, replies, attributes );
	}

	private static class TemporaryItinerary {
		public Map<String,String> meta;
		public List<Map<String,String>> data;
	}

	@Override
	public ISearchRunItinerary makeSearchRunItineraryFromJSON( String jsonData ) throws Exception {
		TemporaryItinerary tempItinerary = (TemporaryItinerary) Utils.getPlainObjectMapper().readValue( jsonData, TemporaryItinerary.class );

		if ( !"checkreplies".equals( tempItinerary.meta.get( "action" ) ) ) {
			throw new IllegalArgumentException( "File type not known" );
		}

		if ( tempItinerary.data == null || tempItinerary.data.size() < 1 ) {
			throw new IllegalArgumentException( "No tweets" );
		}

		ITweetUser initiatingUser = tweetFactory.makeTweetUser( tempItinerary.meta, "originating_user_" );
		logger.info( "loading jsonData for " + initiatingUser );

		List<ITweet> tweets = new ArrayList<ITweet>( tempItinerary.data.size() );

		for ( Map<String,String> map : tempItinerary.data ) {
			tweets.add( tweetFactory.makeTweet( map, "tweetkey_", "attrkey_", "userkey_" ) );
		}

		logger.info( "loading " + tweets.size() + " tweets" );

		Instant retrievalTime = Instant.ofEpochSecond( Long.valueOf( tempItinerary.meta.get( "collection_date" ) ) );
		Map<String,String> attributes = new HashMap<String,String>();

		ITweetCollection tweetCollection = tweetFactory.makeTweetCollection( tweets, retrievalTime, attributes );

		ISearchRunItinerary obj = new SearchRunRepliesItinerary( initiatingUser, tweetCollection );

		return obj;
	}
}
