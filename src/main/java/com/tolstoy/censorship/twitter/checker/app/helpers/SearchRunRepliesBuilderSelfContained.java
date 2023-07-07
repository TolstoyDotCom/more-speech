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

import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.api.utils.IArchiveDirectory;
import com.tolstoy.basic.app.utils.KeyedLists;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyFactory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferencesFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IInfiniteScrollingActivator;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactoryFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.InfiniteScrollingActivatorType;
import com.tolstoy.censorship.twitter.checker.api.webdriver.WebDriverFactoryType;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersSet;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParameters;

/**
 * Utility that uses WebDriver to build an ISearchRunReplies object.
 *
 * First, read the tweets on the given user's timeline. Then for each
 * of those, if it's a reply, look on the replied-to page to see
 * where the tweet appears (if it does).
 *
 * This is complicated because of Twitter's conversations setup. Whether
 * intentionally or not, some tweets in the timeline don't have the actual
 * ID of the tweet that was replied to. Instead, they have a conversation
 * ID that's could be the ID of the tweet that was replied to, or it could
 * be the ID of the initial tweet in a series. If someone else replies to
 * their own tweet and you reply to their reply, the HTML of your timeline
 * will have the initial tweet's ID.
 *
 * What that means is that in some cases this class has to load an additional
 * page to find the right tweet listing. First the individual tweet page
 * with the reply is loaded to get the ID of the tweet that was replied to.
 * Then the individual tweet page is loaded per usual to get the correct
 * tweet listing. There may be cases where even that doesn't work, or the
 * logic used to select the actual replied-to tweet doesn't work.
 */
final public class SearchRunRepliesBuilderSelfContained {
	private static final Logger logger = LogManager.getLogger( SearchRunRepliesBuilderSelfContained.class );
	private static final int WEBDRIVER_CLOSE_DELAY_MILLIS = 5000;

	private final IResourceBundleWithFormatting bundle;
	private final IStorage storage;
	private final IPreferencesFactory prefsFactory;
	private final IPreferences prefs;
	private final IWebDriverFactoryFactory webDriverFactoryFactory;
	private final ISearchRunFactory searchRunFactory;
	private final ISnapshotFactory snapshotFactory;
	private final ITweetFactory tweetFactory;
	private final IBrowserProxyFactory browserProxyFactory;
	private final IArchiveDirectory archiveDirectory;
	private final IStatusMessageReceiver statusMessageReceiver;
	private final String handleToCheck;

	public SearchRunRepliesBuilderSelfContained( final IResourceBundleWithFormatting bundle,
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
		this.browserProxyFactory = browserProxyFactory;
		this.archiveDirectory = archiveDirectory;
		this.statusMessageReceiver = statusMessageReceiver;
		this.handleToCheck = handleToCheck;

		if ( webDriverFactoryFactory == null ) {
			throw new RuntimeException( bundle.getString( "exc_no_webdriverfactory" ) );
		}
	}

	public ISearchRunReplies buildSearchRunReplies( final IPageParametersSet pageParametersSet ) throws Exception {
		IWebDriverFactory webDriverFactory = null;
		WebDriver webDriver = null;
		IBrowserProxy browserProxy = null;
		IWebDriverUtils webDriverUtils = null;
		LoginToSite loginToSite = null;
		String loginName = null;
		String loginPassword = null;
		boolean bSkipLogin = false, bUsingLogin = false;

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
			logger.error( "error getting testing_account_name or creating webDriverFactory", e );
			throw e;
		}

		try {
			browserProxy = browserProxyFactory.makeBrowserDataRecorder();

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
																			browserProxy,
																			webDriverUtils,
																			pageParametersSet );

			ret.setAttribute( "handle_to_check", handleToCheck );
			ret.setAttribute( "loggedin", ( bUsingLogin || bSkipLogin ) ? "true" : "false" );

			return ret;
		}
		catch ( final Exception e ) {
			logger.error( "error logging in or building searchRun", e );
			throw e;
		}
		finally {
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
																final IBrowserProxy browserProxy,
																final IWebDriverUtils webDriverUtils,
																final IPageParametersSet pageParametersSet ) throws Exception {
		final Instant startTime = Instant.now();

		final String url = String.format( prefs.getValue( "targetsite.pattern.timeline" ), handleToCheck );

		logInfo( bundle.getString( "srb_loading_timeline", url ) );

		webDriver.get( url );

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

		final IInfiniteScrollingActivator scroller = webDriverFactory.makeInfiniteScrollingActivator( webDriver,
																										webDriverUtils,
																										InfiniteScrollingActivatorType.TIMELINE );

		final ISnapshotUserPageTimeline timeline = webDriverFactory.makeSnapshotUserPageTimelineFromURL( webDriver,
																											webDriverUtils,
																											scroller,
																											browserProxy,
																											archiveDirectory,
																											url,
																											pageParametersSet.getTimeline().getPagesToScroll(),
																											pageParametersSet.getTimeline().getItemsToProcess() );

		final ITweetUser user = timeline.getUser();
		if ( user == null || Utils.isEmpty( user.getHandle() ) ) {
			throw new RuntimeException( "timeline does not have a user" );
		}

		Map<Long,IReplyThread> replies;
		final ITweetCollection tweetCollection = timeline.getTweetCollection();
		if ( tweetCollection == null || tweetCollection.getTweets() == null || tweetCollection.getTweets().isEmpty() ) {
			logWarn( bundle.getString( "srb_bad_timeline", url ) );
			replies = new HashMap<Long,IReplyThread>();
		}
		else {
			logInfo( bundle.getString( "srb_loaded_timeline", tweetCollection.getTweets().size() ) );
			replies = helper.getReplyPages( webDriver, webDriverUtils, tweetCollection.getTweets(), user, pageParametersSet );
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
