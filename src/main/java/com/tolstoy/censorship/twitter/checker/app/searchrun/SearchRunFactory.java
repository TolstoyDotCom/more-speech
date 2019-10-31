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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunItinerary;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunTimeline;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;

public class SearchRunFactory implements ISearchRunFactory {
	private static final Logger logger = LogManager.getLogger( SearchRunFactory.class );

	private final ITweetFactory tweetFactory;

	public SearchRunFactory( final ITweetFactory tweetFactory ) {
		this.tweetFactory = tweetFactory;
	}

	@Override
	public ISearchRunReplies makeSearchRunReplies( final long id, final ITweetUser user, final Instant startTime, final Instant endTime ) {
		return new SearchRunReplies( id, user, startTime, endTime );
	}

	@Override
	public ISearchRunReplies makeSearchRunReplies( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
											final ISnapshotUserPageTimeline timeline,
											final Map<Long,IReplyThread> replies ) {
		return new SearchRunReplies( id, user, startTime, endTime, timeline, replies );
	}

	@Override
	public ISearchRunReplies makeSearchRunReplies( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
													final Map<String,String> attributes ) {
		return new SearchRunReplies( id, user, startTime, endTime, attributes );
	}

	@Override
	public ISearchRunReplies makeSearchRunReplies( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
											final ISnapshotUserPageTimeline timeline,
											final Map<Long,IReplyThread> replies,
											final Map<String,String> attributes ) {
		return new SearchRunReplies( id, user, startTime, endTime, timeline, replies, attributes );
	}

	@Override
	public ISearchRunTimeline makeSearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime ) {
		return new SearchRunTimeline( id, user, startTime, endTime );
	}

	@Override
	public ISearchRunTimeline makeSearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
											final ISnapshotUserPageTimeline timeline,
											final Map<Long,ISnapshotUserPageIndividualTweet> replies ) {
		return new SearchRunTimeline( id, user, startTime, endTime, timeline, replies );
	}

	@Override
	public ISearchRunTimeline makeSearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
													final Map<String,String> attributes ) {
		return new SearchRunTimeline( id, user, startTime, endTime, attributes );
	}

	@Override
	public ISearchRunTimeline makeSearchRunTimeline( final long id, final ITweetUser user, final Instant startTime, final Instant endTime,
											final ISnapshotUserPageTimeline timeline,
											final Map<Long,ISnapshotUserPageIndividualTweet> replies,
											final Map<String,String> attributes ) {
		return new SearchRunTimeline( id, user, startTime, endTime, timeline, replies, attributes );
	}

	private static class TemporaryItinerary {
		public Map<String,String> meta;
		public List<Map<String,String>> data;
	}

	@Override
	public ISearchRunItinerary makeSearchRunItineraryFromJSON( final String jsonData ) throws Exception {
		final TemporaryItinerary tempItinerary = Utils.getPlainObjectMapper().readValue( jsonData, TemporaryItinerary.class );

		if ( !"checkreplies".equals( tempItinerary.meta.get( "action" ) ) ) {
			throw new IllegalArgumentException( "File type not known" );
		}

		if ( tempItinerary.data == null || tempItinerary.data.isEmpty() ) {
			throw new IllegalArgumentException( "No tweets" );
		}

		final ITweetUser initiatingUser = tweetFactory.makeTweetUser( tempItinerary.meta, "originating_user_" );
		logger.info( "loading jsonData for " + initiatingUser );

		final List<ITweet> tweets = new ArrayList<ITweet>( tempItinerary.data.size() );

		for ( final Map<String,String> map : tempItinerary.data ) {
			tweets.add( tweetFactory.makeTweet( map, "tweetkey_", "attrkey_", "userkey_" ) );
		}

		logger.info( "loading " + tweets.size() + " tweets" );

		final Instant retrievalTime = Instant.ofEpochSecond( Long.valueOf( tempItinerary.meta.get( "collection_date" ) ) );
		final Map<String,String> attributes = new HashMap<String,String>();

		final ITweetCollection tweetCollection = tweetFactory.makeTweetCollection( tweets, retrievalTime, attributes );

		final ISearchRunItinerary obj = new SearchRunRepliesItinerary( initiatingUser, tweetCollection );

		return obj;
	}
}
