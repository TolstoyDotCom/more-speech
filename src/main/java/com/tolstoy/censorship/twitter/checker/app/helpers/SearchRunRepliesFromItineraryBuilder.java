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
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import com.tolstoy.basic.api.installation.DebugLevel;
import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.api.utils.IArchiveDirectory;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyFactory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferencesFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunRepliesItinerary;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactoryFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.WebDriverFactoryType;

final public class SearchRunRepliesFromItineraryBuilder {
	private static final Logger logger = LogManager.getLogger( SearchRunRepliesFromItineraryBuilder.class );
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
	private final ISearchRunRepliesItinerary itinerary;
	private final String handleToCheck;

	public SearchRunRepliesFromItineraryBuilder( final IResourceBundleWithFormatting bundle,
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
						final ISearchRunRepliesItinerary itinerary ) throws Exception {
		this.bundle = bundle;
		this.storage = storage;
		this.prefsFactory = prefsFactory;
		this.prefs = prefs;
		this.webDriverFactoryFactory = webDriverFactoryFactory;
		this.searchRunFactory = searchRunFactory;
		this.snapshotFactory = snapshotFactory;
		this.tweetFactory = tweetFactory;
		this.browserProxyFactory = browserProxyFactory;
		this.archiveDirectory = archiveDirectory;
		this.statusMessageReceiver = statusMessageReceiver;
		this.itinerary = itinerary;
		this.handleToCheck = Utils.trimDefault( itinerary.getInitiatingUser().getHandle() ).toLowerCase();

		if ( webDriverFactoryFactory == null ) {
			throw new RuntimeException( bundle.getString( "exc_no_webdriverfactory" ) );
		}
	}

	public ISearchRunReplies buildSearchRunReplies( final int numberOfTimelinePagesToCheck, final int numberOfReplyPagesToCheck, final int maxReplies ) throws Exception {
		IWebDriverFactory webDriverFactory;
		final WebDriver webDriver;
		final IBrowserProxy browserProxy;
		final IWebDriverUtils webDriverUtils;
		final LoginToSite loginToSite;
		final String loginName;
		final String loginPassword;
		final boolean bSkipLogin, bUsingLogin;

		try {
			loginName = prefs.getValue( "prefs.testing_account_name_private" );
			loginPassword = prefs.getValue( "prefs.testing_account_password_private" );

			bSkipLogin = Utils.isStringTrue( prefs.getValue( "prefs.skip_login" ) );
			bUsingLogin = !Utils.isEmpty( loginName ) && !Utils.isEmpty( loginPassword );

			if ( bUsingLogin || bSkipLogin ) {
				webDriverFactory = webDriverFactoryFactory.makeWebDriverFactory( WebDriverFactoryType.NEWTWITTER_WITH_JAVASCRIPT );
			}
			else {
				webDriverFactory = webDriverFactoryFactory.makeWebDriverFactory( WebDriverFactoryType.ORIGINAL_WITH_JAVASCRIPT );
			}
		}
		catch ( final Exception e ) {
			logger.error( "error getting testing_account_name or creating webDriverFactory", e );
			throw e;
		}

		statusMessageReceiver.addMessage( new StatusMessage( bundle.getString( "srrfib_msg_handle", handleToCheck ), StatusMessageSeverity.INFO ) );

		try {
			browserProxy = browserProxyFactory.makeBrowserProxy();

			browserProxy.start();
		}
		catch ( final Exception e ) {
			logger.error( "cannot create browserProxy", e );
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
			logger.error( "cannot create webDriver", e );
			statusMessageReceiver.addMessage( new StatusMessage( "cannot create webDriver", StatusMessageSeverity.ERROR ) );
			throw e;
		}

		try {
			webDriverUtils = webDriverFactory.makeWebDriverUtils( webDriver );

			if ( bUsingLogin && !bSkipLogin ) {
				loginToSite = new LoginToSite( loginName, loginPassword, prefs );
				loginToSite.perform( webDriver, webDriverUtils );
			}

			final ISearchRunReplies ret = buildSearchRunRepliesInternal( webDriverFactory,
																			webDriver,
																			webDriverUtils,
																			browserProxy,
																			numberOfTimelinePagesToCheck,
																			numberOfReplyPagesToCheck,
																			maxReplies );

			ret.setAttribute( "handle_to_check", handleToCheck );
			ret.setAttribute( "loggedin", ( bUsingLogin || bSkipLogin ) ? "true" : "false" );

			return ret;
		}
		catch ( final Exception e ) {
			logger.error( "error logging in or building searchRun", e );
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
					logger.error( "cannot stop browserProxy", e );
				}
			}

			if ( webDriver != null ) {
				try {
					Utils.delay( WEBDRIVER_CLOSE_DELAY_MILLIS );
					webDriver.close();
				}
				catch ( final Exception e ) {
					logger.error( "cannot close webDriver", e );
					statusMessageReceiver.addMessage( new StatusMessage( "cannot close webDriver", StatusMessageSeverity.ERROR ) );
				}
			}

			logInfo( bundle.getString( "srb_done", handleToCheck ) );
		}
	}

	private ISearchRunReplies buildSearchRunRepliesInternal( final IWebDriverFactory webDriverFactory,
																final WebDriver webDriver,
																final IWebDriverUtils webDriverUtils,
																final IBrowserProxy browserProxy,
																final int numberOfTimelinePagesToCheck,
																final int numberOfReplyPagesToCheck,
																final int maxReplies ) throws Exception {
		final Instant startTime = Instant.now();

		final String url = String.format( prefs.getValue( "targetsite.pattern.timeline" ), handleToCheck );

		final ISnapshotUserPageTimeline timeline = snapshotFactory.makeSnapshotUserPageTimeline( url, startTime );

		final ITweetUser user = itinerary.getInitiatingUser();
		if ( user == null || Utils.isEmpty( user.getHandle() ) ) {
			throw new RuntimeException( "timeline does not have a user" );
		}

		timeline.setUser( user );
		timeline.setTweetCollection( itinerary.getTimelineTweetCollection() );
		timeline.setNumTotalTweets( itinerary.getInitiatingUser().getNumTotalTweets() );
		timeline.setNumFollowers( itinerary.getInitiatingUser().getNumFollowers() );
		timeline.setNumFollowing( itinerary.getInitiatingUser().getNumFollowing() );

		final SearchRunRepliesBuilderHelper helper = new SearchRunRepliesBuilderHelper( bundle,
																						storage,
																						prefsFactory,
																						prefs,
																						webDriverFactory,
																						searchRunFactory,
																						snapshotFactory,
																						tweetFactory,
																						browserProxy,
																						archiveDirectory,
																						statusMessageReceiver );

		Map<Long,IReplyThread> replies;
		final ITweetCollection tweetCollection = timeline.getTweetCollection();
		if ( tweetCollection == null || tweetCollection.getTweets() == null || tweetCollection.getTweets().isEmpty() ) {
			logWarn( bundle.getString( "srb_bad_timeline", url ) );
			replies = new HashMap<Long,IReplyThread>();
		}
		else {
			logInfo( bundle.getString( "srb_loaded_timeline", tweetCollection.getTweets().size() ) );
			replies = helper.getReplyPages( webDriver, webDriverUtils, tweetCollection.getTweets(),
										user, numberOfReplyPagesToCheck, maxReplies );
		}

		final ISearchRunReplies ret = searchRunFactory.makeSearchRunReplies( 0, user, startTime, Instant.now(), timeline, replies );

		return ret;
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
