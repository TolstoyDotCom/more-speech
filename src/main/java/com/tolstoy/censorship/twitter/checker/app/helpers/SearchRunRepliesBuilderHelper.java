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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.TargetPageType;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.api.utils.IArchiveDirectory;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferencesFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ReplyThreadType;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IInfiniteScrollingActivator;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.InfiniteScrollingActivatorType;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersSet;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParameters;

/**
 * Contains methods used by SearchRunRepliesBuilderSelfContained, SearchRunRepliesFromItineraryBuilder, etc.
 */
public class SearchRunRepliesBuilderHelper {
	private static final Logger logger = LogManager.getLogger( SearchRunRepliesBuilderHelper.class );

	private final IResourceBundleWithFormatting bundle;
	private final IStorage storage;
	private final IPreferencesFactory prefsFactory;
	private final IPreferences prefs;
	private final IWebDriverFactory webDriverFactory;
	private final ISearchRunFactory searchRunFactory;
	private final ISnapshotFactory snapshotFactory;
	private final ITweetFactory tweetFactory;
	private final IBrowserProxy browserProxy;
	private final IArchiveDirectory archiveDirectory;
	private final IStatusMessageReceiver statusMessageReceiver;

	public SearchRunRepliesBuilderHelper( final IResourceBundleWithFormatting bundle,
						final IStorage storage,
						final IPreferencesFactory prefsFactory,
						final IPreferences prefs,
						final IWebDriverFactory webDriverFactory,
						final ISearchRunFactory searchRunFactory,
						final ISnapshotFactory snapshotFactory,
						final ITweetFactory tweetFactory,
						final IBrowserProxy browserProxy,
						final IArchiveDirectory archiveDirectory,
						final IStatusMessageReceiver statusMessageReceiver ) throws Exception {
		this.bundle = bundle;
		this.storage = storage;
		this.prefsFactory = prefsFactory;
		this.prefs = prefs;
		this.webDriverFactory = webDriverFactory;
		this.searchRunFactory = searchRunFactory;
		this.snapshotFactory = snapshotFactory;
		this.tweetFactory = tweetFactory;
		this.browserProxy = browserProxy;
		this.archiveDirectory = archiveDirectory;
		this.statusMessageReceiver = statusMessageReceiver;
	}

	Map<Long,IReplyThread> getReplyPages( final WebDriver webDriver,
											final IWebDriverUtils webDriverUtils,
											final List<ITweet> tweets,
											final ITweetUser user,
											final IPageParametersSet pageParametersSet )
											throws Exception {
		final Map<Long,IReplyThread> replies = new HashMap<Long,IReplyThread>();

		final String handle = user.getHandle();

		for ( final ITweet tweet : tweets ) {
				//	if it's a reply and not a self-reply
			if ( tweet.getRepliedToTweetID() != 0 && !handle.equals( Utils.trimDefault( tweet.getRepliedToHandle() ).toLowerCase() ) ) {
				final IReplyThread thread = getReplyThread( webDriver, webDriverUtils, tweet, user, pageParametersSet );

				if ( thread != null ) {
					replies.put( tweet.getID(), thread );
				}

				if ( replies.size() >= pageParametersSet.getTimeline().getItemsToProcess() ) {
					break;
				}
			}
		}

		return replies;
	}

	IReplyThread getReplyThread( final WebDriver webDriver, final IWebDriverUtils webDriverUtils, final ITweet sourceTweet,
											final ITweetUser user, final IPageParametersSet pageParametersSet )
											throws Exception {
		if ( sourceTweet.getRepliedToTweetID() == 0 ) {
			return null;
		}

		try {
			final ISnapshotUserPageIndividualTweet replyPage = getReplyPage( webDriver, webDriverUtils, sourceTweet.getRepliedToTweetID(),
																		sourceTweet.getRepliedToHandle(), user, pageParametersSet );

			final IReplyThread defaultReplyThread = snapshotFactory.makeReplyThread( ReplyThreadType.DIRECT,
																				sourceTweet,
																				replyPage.getIndividualTweet(),
																				replyPage,
																				null );

				//	don't bother loading another page to look for the user's reply if:
				//		we found the user's reply, or
				//		we didn't get all the tweets.
				//	in the second case it might be further down or it might not be; the user
				//	will have to increase pageParametersSet.getTimeline().getItemsToProcess() to find out.
			if ( replyPage.getTweetCollection().getTweetByID( sourceTweet.getID() ) != null || !replyPage.getComplete() ) {
				return defaultReplyThread;
			}

			//	the conversation id given in the source tweet wasn't the actual tweet
			//	that the user replied to. So, we have to in effect click the time ago
			//	link on their timeline and look for the actual tweet they replied to
			//	there. Then, we need to load the individual page for that actual tweet.

			logInfo( bundle.getString( "srb_tweetnotfound_so_userreplypage", sourceTweet.getID(), sourceTweet.getRepliedToTweetID() ) );

			try {
				return getConversationReplyThread( webDriver, webDriverUtils, sourceTweet, user, pageParametersSet );
			}
			catch ( final Exception e ) {
			}

			//	something went wrong building the conversation reply thread, just return the default

			return defaultReplyThread;
		}
		catch ( final Exception e ) {
			logWarn( bundle.getString( "srb_bad_userreplypage", sourceTweet.getRepliedToTweetID() ), e );
			return null;
		}
	}

	ISnapshotUserPageIndividualTweet getReplyPage( final WebDriver webDriver, final IWebDriverUtils webDriverUtils,
																final long tweetID, final String userInURL, final ITweetUser user,
																final IPageParametersSet pageParametersSet )
																throws Exception {
		ISnapshotUserPageIndividualTweet replyPage;
		IInfiniteScrollingActivator scroller;

		String handle = userInURL;
		if ( Utils.isEmpty( handle ) ) {
				//	if we use the wrong user in the URL,
				//	it will be redirected to the correct user's page
			handle = user.getHandle();
		}

		final String url = String.format( prefs.getValue( "targetsite.pattern.individual" ), handle, tweetID );

		logInfo( bundle.getString( "srb_loading_replypage", url ) );

		webDriver.get( url );

		scroller = webDriverFactory.makeInfiniteScrollingActivator( webDriver,
																	webDriverUtils,
																	InfiniteScrollingActivatorType.INDIVIDUAL );

		try {
			replyPage = webDriverFactory.makeSnapshotUserPageIndividualTweetFromURL( webDriver,
																						webDriverUtils,
																						scroller,
																						browserProxy,
																						archiveDirectory,
																						url,
																						pageParametersSet.getTimeline().getItemsToProcess(),
																						0 );
			logInfo( bundle.getString( "srb_loaded_replypage", replyPage.getTweetCollection().getTweets().size(), url ) );
		}
		catch ( final Exception e ) {
			logWarn( bundle.getString( "srb_bad_replypage", url ), e );
			throw e;
		}

		return replyPage;
	}

	IReplyThread getConversationReplyThread( final WebDriver webDriver, final IWebDriverUtils webDriverUtils,
														final ITweet sourceTweet, final ITweetUser user, final IPageParametersSet pageParametersSet )
														throws Exception {
		ITweetCollection userReplyTweetCollection;
		IInfiniteScrollingActivator scroller;

		String userInURL = sourceTweet.getRepliedToHandle();
		if ( Utils.isEmpty( userInURL ) ) {
				//	if we use the wrong user in the URL,
				//	it will be redirected to the correct user's page
			userInURL = user.getHandle();
		}

		final String url = String.format( prefs.getValue( "targetsite.pattern.individual" ), user.getHandle(), sourceTweet.getID() );

		logInfo( bundle.getString( "srb_loading_usertweetpage", url ) );

		webDriver.get( url );

		scroller = webDriverFactory.makeInfiniteScrollingActivator( webDriver,
																	webDriverUtils,
																	InfiniteScrollingActivatorType.INDIVIDUAL );

		try {
			userReplyTweetCollection = webDriverFactory.makeTweetCollectionFromURL( webDriver, webDriverUtils, scroller, archiveDirectory,
																					url, TargetPageType.REPLYPAGE,
																					pageParametersSet.getTimeline().getItemsToProcess(), 0 );

			logInfo( bundle.getString( "srb_loaded_usertweetpage", userReplyTweetCollection.getTweets().size(), url ) );

			final int numUserReplyTweets = userReplyTweetCollection.getTweets().size();
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

			final ISnapshotUserPageIndividualTweet replyPage = getReplyPage( webDriver, webDriverUtils, actualTweet.getID(),
																		actualTweet.getUser().getHandle(), user,
																		pageParametersSet );

			return snapshotFactory.makeReplyThread( ReplyThreadType.INDIRECT,
													sourceTweet,
													actualTweet,
													replyPage,
													userReplyTweetCollection );
		}
		catch ( final Exception e ) {
			logWarn( bundle.getString( "srb_bad_usertweetpage", url ), e );
			throw e;
		}
	}

	private void logInfo( final String s ) {
		logger.info( s );
		statusMessageReceiver.addMessage( new StatusMessage( s, StatusMessageSeverity.INFO ) );
	}

	private void logWarn( final String s ) {
		logger.info( s );
		statusMessageReceiver.addMessage( new StatusMessage( s, StatusMessageSeverity.WARN ) );
	}

	private void logWarn( final String s, final Exception e ) {
		logger.error( s, e );
		statusMessageReceiver.addMessage( new StatusMessage( s, StatusMessageSeverity.WARN ) );
	}
}
