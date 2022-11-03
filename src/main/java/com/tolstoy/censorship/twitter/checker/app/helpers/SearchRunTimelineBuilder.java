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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import com.tolstoy.basic.api.installation.DebugLevel;
import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.api.utils.IArchiveDirectory;
import com.tolstoy.basic.app.utils.KeyedLists;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyFactory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseEvent;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseListener;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferencesFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunTimeline;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IInfiniteScrollingActivator;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactoryFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.InfiniteScrollingActivatorType;
import com.tolstoy.censorship.twitter.checker.api.webdriver.WebDriverFactoryType;

/**
 * Utility that uses WebDriver to build an ISearchRunTimeline object.
 *
 * First, read the tweets on the given user's timeline. Then for each
 * of those that aren't RTs, etc. build a ISnapshotUserPageIndividualTweet
 * for each tweet.
 *
 * This will show which replies to a specific user were elevated, suppressed, etc.
 */
final public class SearchRunTimelineBuilder /*implements IBrowserProxyResponseListener*/ {
	private static final Logger logger = LogManager.getLogger( SearchRunTimelineBuilder.class );
	private static final int WEBDRIVER_CLOSE_DELAY_MILLIS = 5000;

	private final IResourceBundleWithFormatting bundle;
	private final IStorage storage;
	private final IPreferencesFactory prefsFactory;
	private final IPreferences prefs;
	private final IWebDriverFactoryFactory webDriverFactoryFactory;
	private final ISearchRunFactory searchRunFactory;
	private final ISnapshotFactory snapshotFactory;
	private final ITweetFactory tweetFactory;
	private final IStatusMessageReceiver statusMessageReceiver;
	private final IBrowserProxyFactory browserProxyFactory;
	private final IArchiveDirectory archiveDirectory;
	/*private final KeyedLists<IBrowserProxyResponseEvent> proxyEvents;*/
	private final String handleToCheck;

	public SearchRunTimelineBuilder( final IResourceBundleWithFormatting bundle,
						final IStorage storage,
						final IPreferencesFactory prefsFactory,
						final IPreferences prefs,
						final IWebDriverFactoryFactory webDriverFactoryFactory,
						final ISearchRunFactory searchRunFactory,
						final ISnapshotFactory snapshotFactory,
						final ITweetFactory tweetFactory,
						final IBrowserProxyFactory browserProxyFactory,
						final IArchiveDirectory archiveDirectory,
						final IStatusMessageReceiver statusMessageReceiver,
						final String handleToCheck ) throws Exception {
		this.bundle = bundle;
		this.storage = storage;
		this.prefsFactory = prefsFactory;
		this.prefs = prefs;
		this.webDriverFactoryFactory = webDriverFactoryFactory;
		this.searchRunFactory = searchRunFactory;
		this.snapshotFactory = snapshotFactory;
		this.tweetFactory = tweetFactory;
		this.statusMessageReceiver = statusMessageReceiver;
		this.browserProxyFactory = browserProxyFactory;
		this.archiveDirectory = archiveDirectory;
		this.handleToCheck = Utils.trimDefault( handleToCheck ).toLowerCase();

		if ( webDriverFactoryFactory == null ) {
			throw new RuntimeException( bundle.getString( "exc_no_webdriverfactory" ) );
		}

		/*
		this.proxyEvents = new KeyedLists<IBrowserProxyResponseEvent>();
		this.browserProxy.addBrowserProxyResponseListener( this );
		*/
	}

	public ISearchRunTimeline buildSearchRunTimeline( final int numberOfTimelinePagesToCheck,
														final int numberOfReplyPagesToCheck,
														final int maxReplies,
														final int numberOfTimelineTweetsToSkip ) throws Exception {
		IWebDriverFactory webDriverFactory = null;
		WebDriver webDriver = null;
		IBrowserProxy browserProxy = null;
		IWebDriverUtils webDriverUtils = null;
		LoginToSite loginToSite = null;
		String loginName = null;
		String loginPassword = null;
		boolean bSkipLogin = false, bUsingLogin = false;

		logger.info( "SearchRunTimelineBuilder: buildSearchRunTimeline start" );

		try {
			loginName = prefs.getValue( "prefs.testing_account_name_private" );
			loginPassword = prefs.getValue( "prefs.testing_account_password_private" );

			bSkipLogin = Utils.isStringTrue( prefs.getValue( "prefs.skip_login" ) );

			if ( !Utils.isEmpty( loginName ) && !Utils.isEmpty( loginPassword ) ) {
				bUsingLogin = true;
			}

			if ( bUsingLogin || bSkipLogin ) {
				webDriverFactory = webDriverFactoryFactory.makeWebDriverFactory( WebDriverFactoryType.NEWTWITTER_WITH_JAVASCRIPT );
			}
			else {
				webDriverFactory = webDriverFactoryFactory.makeWebDriverFactory( WebDriverFactoryType.ORIGINAL_WITH_JAVASCRIPT );
			}
		}
		catch ( final Exception e ) {
			logWarn( "error getting testing_account_name or creating webDriverFactory", e );
			throw e;
		}

		try {
			browserProxy = browserProxyFactory.makeBrowserProxy();

			browserProxy.start();
		}
		catch ( final Exception e ) {
			logWarn( "cannot create browserProxy", e );
			statusMessageReceiver.addMessage( new StatusMessage( "cannot create browserProxy", StatusMessageSeverity.ERROR ) );
			throw e;
		}

		try {
			webDriver = webDriverFactory.makeWebDriver( browserProxy );
			final int positionX = Utils.parseIntDefault( prefs.getValue( "prefs.firefox_screen_position_x" ) );
			final int positionY = Utils.parseIntDefault( prefs.getValue( "prefs.firefox_screen_position_y" ) );
			webDriver.manage().window().setPosition( new Point( positionX, positionY ) );
		}
		catch ( final Exception e ) {
			logWarn( "cannot create webDriver", e );
			statusMessageReceiver.addMessage( new StatusMessage( "cannot create webDriver", StatusMessageSeverity.ERROR ) );
			throw e;
		}

		try {
			webDriverUtils = webDriverFactory.makeWebDriverUtils( webDriver );

			if ( bUsingLogin && !bSkipLogin ) {
				loginToSite = new LoginToSite( loginName, loginPassword, prefs );
				loginToSite.perform( webDriver, webDriverUtils );
			}

			final ISearchRunTimeline ret = buildSearchRunTimelineInternal( webDriverFactory,
																			webDriver,
																			browserProxy,
																			webDriverUtils,
																			numberOfTimelinePagesToCheck,
																			numberOfReplyPagesToCheck,
																			maxReplies,
																			numberOfTimelineTweetsToSkip );

			ret.setAttribute( "handle_to_check", handleToCheck );
			ret.setAttribute( "loggedin", ( bUsingLogin || bSkipLogin ) ? "true" : "false" );

			logger.info( "SearchRunTimelineBuilder: buildSearchRunTimeline end=" + ret );

			return ret;
		}
		catch ( final Exception e ) {
			logWarn( "error logging in or building searchRun", e );
			throw e;
		}
		finally {
			if ( browserProxy != null ) {
				try {
					logger.info( "about to stop browserProxy" );
					browserProxy.stop();
					logger.info( "stopped browserProxy" );
				}
				catch ( final Exception e ) {
					logWarn( "cannot stop browserProxy", e );
				}
			}

			if ( webDriver != null ) {
				try {
					Utils.delay( WEBDRIVER_CLOSE_DELAY_MILLIS );
					webDriver.close();
				}
				catch ( final Exception e ) {
					logWarn( "cannot close webDriver", e );
					statusMessageReceiver.addMessage( new StatusMessage( "cannot close webDriver", StatusMessageSeverity.ERROR ) );
				}
			}

			logInfo( bundle.getString( "srb_done", handleToCheck ) );
		}
	}

	private ISearchRunTimeline buildSearchRunTimelineInternal( final IWebDriverFactory webDriverFactory,
																final WebDriver webDriver,
																final IBrowserProxy browserProxy,
																final IWebDriverUtils webDriverUtils,
																final int numberOfTimelinePagesToCheck,
																final int numberOfReplyPagesToCheck,
																final int maxReplies,
																final int numberOfTimelineTweetsToSkip ) throws Exception {
		logger.info( "SearchRunTimelineBuilder: buildSearchRunTimelineInternal start" );

		final Instant startTime = Instant.now();

		final String url = String.format( prefs.getValue( "targetsite.pattern.timeline" ), handleToCheck );

		logInfo( bundle.getString( "srb_loading_timeline", url ) );

		webDriver.get( url );

		final IInfiniteScrollingActivator scroller = webDriverFactory.makeInfiniteScrollingActivator( webDriver,
																										webDriverUtils,
																										InfiniteScrollingActivatorType.TIMELINE );

		logger.info( "SearchRunTimelineBuilder: buildSearchRunTimelineInternal about to makeSnapshotUserPageTimelineFromURL" );

		final ISnapshotUserPageTimeline timeline = webDriverFactory.makeSnapshotUserPageTimelineFromURL( webDriver,
																											webDriverUtils,
																											scroller,
																											browserProxy,
																											archiveDirectory,
																											url,
																											numberOfTimelinePagesToCheck,
																											10 * maxReplies );

		logger.info( "SearchRunTimelineBuilder: buildSearchRunTimelineInternal done with makeSnapshotUserPageTimelineFromURL" );

		final ITweetUser user = timeline.getUser();
		if ( user == null || Utils.isEmpty( user.getHandle() ) ) {
			throw new RuntimeException( "timeline does not have a user" );
		}

		logger.info( "SearchRunTimelineBuilder: buildSearchRunTimelineInternal user=" + user.toDebugString( "" ) );

		Map<Long,ISnapshotUserPageIndividualTweet> individualPages;
		final ITweetCollection tweetCollection = timeline.getTweetCollection();
		if ( tweetCollection == null || tweetCollection.getTweets() == null || tweetCollection.getTweets().isEmpty() ) {
			logWarn( bundle.getString( "srb_bad_timeline", url ) );
			individualPages = new HashMap<Long,ISnapshotUserPageIndividualTweet>();
		}
		else {
			logInfo( bundle.getString( "srb_loaded_timeline", tweetCollection.getTweets().size() ) );

			logger.info( "SearchRunTimelineBuilder: buildSearchRunTimelineInternal about to getIndividualPages" );

			individualPages = getIndividualPages( webDriverFactory,
													webDriver,
													browserProxy,
													webDriverUtils,
													tweetCollection.getTweets(),
													user,
													numberOfReplyPagesToCheck,
													maxReplies,
													numberOfTimelineTweetsToSkip );

			logger.info( "SearchRunTimelineBuilder: buildSearchRunTimelineInternal got getIndividualPages=" + individualPages );
		}

		final ISearchRunTimeline ret = searchRunFactory.makeSearchRunTimeline( 0, user, startTime, Instant.now(), timeline, individualPages );

		logger.info( "SearchRunTimelineBuilder: buildSearchRunTimelineInternal build ISearchRunTimeline=" + ret );

		return ret;
	}

	private Map<Long,ISnapshotUserPageIndividualTweet> getIndividualPages( final IWebDriverFactory webDriverFactory,
																			final WebDriver webDriver,
																			final IBrowserProxy browserProxy,
																			final IWebDriverUtils webDriverUtils,
																			final List<ITweet> tweets,
																			final ITweetUser user,
																			final int numberOfReplyPagesToCheck,
																			final int maxReplies,
																			int numberOfTimelineTweetsToSkip )
																				throws Exception {
		final Map<Long,ISnapshotUserPageIndividualTweet> individualPages = new HashMap<Long,ISnapshotUserPageIndividualTweet>();

		final String handle = user.getHandle();

		logger.info( "SearchRunTimelineBuilder, handleToCheck=" + handleToCheck + ", handle=" + handle );

		for ( final ITweet tweet : tweets ) {
			if ( numberOfTimelineTweetsToSkip > 0 ) {
				numberOfTimelineTweetsToSkip--;
				continue;
			}

			String temp = Utils.trimDefault( tweet.getUser().getHandle() ).toLowerCase();

			logger.info( "SearchRunTimelineBuilder, comparing handle with " + temp + " and getting individual page for " + tweet.toDebugString( "" ) );

				//	if it's not an RT, etc.
			if ( !handleToCheck.equals( Utils.trimDefault( tweet.getUser().getHandle() ).toLowerCase() ) ) {
				continue;
			}

			if ( Utils.parseIntDefault( tweet.getAttribute( "replycount" ) ) < 1 ) {
				continue;
			}

			final ISnapshotUserPageIndividualTweet individualPage = getIndividualPage( webDriverFactory, webDriver, browserProxy, webDriverUtils,
																						tweet, user, numberOfReplyPagesToCheck );

			if ( individualPage != null ) {
				individualPages.put( tweet.getID(), individualPage );
			}

			if ( individualPages.size() >= maxReplies ) {
				break;
			}
		}

		return individualPages;
	}

	private ISnapshotUserPageIndividualTweet getIndividualPage( final IWebDriverFactory webDriverFactory,
																final WebDriver webDriver,
																final IBrowserProxy browserProxy,
																final IWebDriverUtils webDriverUtils,
																final ITweet sourceTweet,
																final ITweetUser user,
																final int numberOfReplyPagesToCheck )
																	throws Exception {
		ISnapshotUserPageIndividualTweet individualPage;

		final String url = String.format( prefs.getValue( "targetsite.pattern.individual" ), sourceTweet.getUser().getHandle(), sourceTweet.getID() );

		logInfo( bundle.getString( "srb_loading_replypage", url ) );

		webDriver.get( url );

		final IInfiniteScrollingActivator scroller = webDriverFactory.makeInfiniteScrollingActivator( webDriver,
																										webDriverUtils,
																										InfiniteScrollingActivatorType.INDIVIDUAL );

		try {
			individualPage = webDriverFactory.makeSnapshotUserPageIndividualTweetFromURL( webDriver,
																							webDriverUtils,
																							scroller,
																							browserProxy,
																							archiveDirectory,
																							url,
																							numberOfReplyPagesToCheck,
																							0 );
			logInfo( bundle.getString( "srb_loaded_replypage", individualPage.getTweetCollection().getTweets().size(), url ) );
		}
		catch ( final Exception e ) {
			logWarn( bundle.getString( "srb_bad_replypage", url ), e );
			throw e;
		}

		return individualPage;
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
		Utils.logException( logger, s, e );
		statusMessageReceiver.addMessage( new StatusMessage( s, StatusMessageSeverity.WARN ) );
	}

/*
	@Override
	public void responseEventFired( final IBrowserProxyResponseEvent event ) {
		if ( webDriver == null ) {
			return;
		}

		final String webDriverURL = webDriver.getCurrentUrl();
		if ( webDriverURL == null || webDriverURL.length() < 1 ) {
			logger.info( "rejected empty webDriverURL" );
			return;
		}

		if ( !event.isJSON() ) {
			logger.info( "rejected non JSON of type " + event.getContentType() );
			return;
		}

		final String requestURL = event.getRequestURL();
		if ( requestURL == null || requestURL.indexOf( "twitter.com" ) < 0 ) {
			logger.info( "rejected requestURL of " + requestURL );
			return;
		}

		proxyEvents.add( webDriverURL, event );
	}
*/
}
