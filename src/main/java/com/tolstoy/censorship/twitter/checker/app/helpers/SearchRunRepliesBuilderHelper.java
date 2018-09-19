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
package com.tolstoy.censorship.twitter.checker.app.helpers;

import java.util.*;
import java.sql.*;
import java.time.Instant;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.storage.*;
import com.tolstoy.basic.api.tweet.*;
import com.tolstoy.basic.api.utils.*;
import com.tolstoy.basic.app.utils.*;
import com.tolstoy.censorship.twitter.checker.api.preferences.*;
import com.tolstoy.censorship.twitter.checker.api.webdriver.*;
import com.tolstoy.censorship.twitter.checker.api.snapshot.*;
import com.tolstoy.censorship.twitter.checker.api.searchrun.*;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.app.gui.*;
import com.tolstoy.basic.api.statusmessage.*;

/**
 * Contains methods used by SearchRunRepliesBuilder, SearchRunRepliesFromItineraryBuilder, etc.
 */
public class SearchRunRepliesBuilderHelper {
	private static final Logger logger = LogManager.getLogger( SearchRunRepliesBuilderHelper.class );

	private IResourceBundleWithFormatting bundle;
	private IStorage storage;
	private IPreferencesFactory prefsFactory;
	private IPreferences prefs;
	private IWebDriverFactory webDriverFactory;
	private ISearchRunFactory searchRunFactory;
	private ISnapshotFactory snapshotFactory;
	private ITweetFactory tweetFactory;
	private IStatusMessageReceiver statusMessageReceiver;
	private ISearchRunRepliesItinerary itinerary;

	public SearchRunRepliesBuilderHelper( IResourceBundleWithFormatting bundle,
						IStorage storage,
						IPreferencesFactory prefsFactory,
						IPreferences prefs,
						IWebDriverFactory webDriverFactory,
						ISearchRunFactory searchRunFactory,
						ISnapshotFactory snapshotFactory,
						ITweetFactory tweetFactory,
						IStatusMessageReceiver statusMessageReceiver ) throws Exception {
		this.bundle = bundle;
		this.storage = storage;
		this.prefsFactory = prefsFactory;
		this.prefs = prefs;
		this.webDriverFactory = webDriverFactory;
		this.searchRunFactory = searchRunFactory;
		this.snapshotFactory = snapshotFactory;
		this.tweetFactory = tweetFactory;
		this.statusMessageReceiver = statusMessageReceiver;
	}

	Map<Long,IReplyThread> getReplyPages( WebDriver webDriver, IWebDriverUtils webDriverUtils,
																		List<ITweet> tweets, ITweetUser user,
																		int numberOfReplyPagesToCheck, int maxReplies )
																		throws Exception {
		Map<Long,IReplyThread> replies = new HashMap<Long,IReplyThread>();

		String handle = user.getHandle();

		for ( ITweet tweet : tweets ) {
				//	if it's a reply and not a self-reply
			if ( tweet.getRepliedToTweetID() != 0 && !handle.equals( Utils.trimDefault( tweet.getRepliedToHandle() ).toLowerCase() ) ) {
				IReplyThread thread = getReplyThread( webDriver, webDriverUtils, tweet, user, numberOfReplyPagesToCheck );

				if ( thread != null ) {
					replies.put( tweet.getID(), thread );
				}

				if ( replies.size() >= maxReplies ) {
					break;
				}
			}
		}

		return replies;
	}

	IReplyThread getReplyThread( WebDriver webDriver, IWebDriverUtils webDriverUtils, ITweet sourceTweet,
											ITweetUser user, int numberOfReplyPagesToCheck )
											throws Exception {
		if ( sourceTweet.getRepliedToTweetID() == 0 ) {
			return null;
		}

		try {
			ISnapshotUserPageIndividualTweet replyPage = getReplyPage( webDriver, webDriverUtils, sourceTweet.getRepliedToTweetID(),
																		sourceTweet.getRepliedToHandle(), user, numberOfReplyPagesToCheck );

			IReplyThread defaultReplyThread = snapshotFactory.makeReplyThread( ReplyThreadType.DIRECT,
																				sourceTweet,
																				replyPage.getIndividualTweet(),
																				replyPage,
																				null );

				//	don't bother loading another page to look for the user's reply if:
				//		we found the user's reply, or
				//		we didn't get all the tweets.
				//	in the second case it might be further down or it might not be; the user
				//	will have to increase numberOfReplyPagesToCheck to find out.
			if ( replyPage.getTweetCollection().getTweetByID( sourceTweet.getID() ) != null || !replyPage.getComplete() ) {
				return defaultReplyThread;
			}

			//	the conversation id given in the source tweet wasn't the actual tweet
			//	that the user replied to. So, we have to in effect click the time ago
			//	link on their timeline and look for the actual tweet they replied to
			//	there. Then, we need to load the individual page for that actual tweet.

			logInfo( bundle.getString( "srb_tweetnotfound_so_userreplypage", sourceTweet.getID(), sourceTweet.getRepliedToTweetID() ) );

			try {
				return getConversationReplyThread( webDriver, webDriverUtils, sourceTweet, user, numberOfReplyPagesToCheck );
			}
			catch ( Exception e ) {
			}

			//	something went wrong building the conversation reply thread, just return the default

			return defaultReplyThread;
		}
		catch ( Exception e ) {
			logWarn( bundle.getString( "srb_bad_userreplypage", sourceTweet.getRepliedToTweetID() ), e );
			return null;
		}
	}

	ISnapshotUserPageIndividualTweet getReplyPage( WebDriver webDriver, IWebDriverUtils webDriverUtils,
																long tweetID, String userInURL, ITweetUser user, int numberOfReplyPagesToCheck )
																throws Exception {
		ISnapshotUserPageIndividualTweet replyPage;
		IInfiniteScrollingActivator scroller;

		if ( Utils.isEmpty( userInURL ) ) {
				//	if we use the wrong user in the URL,
				//	it will be redirected to the correct user's page
			userInURL = user.getHandle();
		}

		String url = String.format( prefs.getValue( "targetsite.pattern.individual" ), userInURL, tweetID );

		logInfo( bundle.getString( "srb_loading_replypage", url ) );

		webDriver.get( url );

		scroller = webDriverFactory.makeInfiniteScrollingActivator( webDriver,
																	webDriverUtils,
																	InfiniteScrollingActivatorType.INDIVIDUAL );

		try {
			replyPage = webDriverFactory.makeSnapshotUserPageIndividualTweetFromURL( webDriver,
																						webDriverUtils,
																						scroller,
																						url,
																						numberOfReplyPagesToCheck,
																						0 );
			logInfo( bundle.getString( "srb_loaded_replypage", replyPage.getTweetCollection().getTweets().size(), url ) );
		}
		catch ( Exception e ) {
			logWarn( bundle.getString( "srb_bad_replypage", url ), e );
			throw e;
		}

		return replyPage;
	}

	IReplyThread getConversationReplyThread( WebDriver webDriver, IWebDriverUtils webDriverUtils,
														ITweet sourceTweet, ITweetUser user, int numberOfReplyPagesToCheck )
														throws Exception {
		ITweetCollection userReplyTweetCollection;
		IInfiniteScrollingActivator scroller;

		String userInURL = sourceTweet.getRepliedToHandle();
		if ( Utils.isEmpty( userInURL ) ) {
				//	if we use the wrong user in the URL,
				//	it will be redirected to the correct user's page
			userInURL = user.getHandle();
		}

		String url = String.format( prefs.getValue( "targetsite.pattern.individual" ), user.getHandle(), sourceTweet.getID() );

		logInfo( bundle.getString( "srb_loading_usertweetpage", url ) );

		webDriver.get( url );

		scroller = webDriverFactory.makeInfiniteScrollingActivator( webDriver,
																	webDriverUtils,
																	InfiniteScrollingActivatorType.INDIVIDUAL );

		try {
			userReplyTweetCollection = webDriverFactory.makeTweetCollectionFromURL( webDriver, webDriverUtils, scroller,
																					url, numberOfReplyPagesToCheck, 0 );

			logInfo( bundle.getString( "srb_loaded_usertweetpage", userReplyTweetCollection.getTweets().size(), url ) );

			int numUserReplyTweets = userReplyTweetCollection.getTweets().size();
			ITweet actualTweet = null, tempTweet;
			for ( int i = 0; i < numUserReplyTweets; i++ ) {
				tempTweet = userReplyTweetCollection.getTweets().get( i );
				logger.info( "REPLYPAGETWEET=" + tempTweet.getSummary() );

				//	for now, assume the tweet just before the user's sourceTweet is the actual tweet that was replied to
				if ( tempTweet.getID() == sourceTweet.getID() ) {
					if ( i < 1 ) {
						throw new RuntimeException( bundle.getString( "srb_userreply_is_first" ) );
					}
					actualTweet = userReplyTweetCollection.getTweets().get( i - 1 );
					break;
				}
			}

			if ( actualTweet == null ) {
				throw new RuntimeException( bundle.getString( "srb_userreply_reply_not_found" ) );
			}

			logger.info( bundle.getString( "srb_userreply_switched", sourceTweet.getSummary(), actualTweet.getSummary() ) );

			ISnapshotUserPageIndividualTweet replyPage = getReplyPage( webDriver, webDriverUtils, actualTweet.getID(),
																		actualTweet.getUser().getHandle(), user, numberOfReplyPagesToCheck );

			return snapshotFactory.makeReplyThread( ReplyThreadType.INDIRECT,
													sourceTweet,
													actualTweet,
													replyPage,
													userReplyTweetCollection );
		}
		catch ( Exception e ) {
			logWarn( bundle.getString( "srb_bad_usertweetpage", url ), e );
			throw e;
		}
	}

	private void logInfo( String s ) {
		logger.info( s );
		statusMessageReceiver.addMessage( new StatusMessage( s, StatusMessageSeverity.INFO ) );
	}

	private void logWarn( String s ) {
		logger.info( s );
		statusMessageReceiver.addMessage( new StatusMessage( s, StatusMessageSeverity.WARN ) );
	}

	private void logWarn( String s, Exception e ) {
		logger.error( s, e );
		statusMessageReceiver.addMessage( new StatusMessage( s, StatusMessageSeverity.WARN ) );
	}
}
