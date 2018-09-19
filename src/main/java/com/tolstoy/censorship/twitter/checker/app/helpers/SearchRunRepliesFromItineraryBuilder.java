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

public class SearchRunRepliesFromItineraryBuilder {
	private static final Logger logger = LogManager.getLogger( SearchRunRepliesFromItineraryBuilder.class );

	private SearchRunRepliesBuilderHelper helper;
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
	private String handleToCheck;

	public SearchRunRepliesFromItineraryBuilder( IResourceBundleWithFormatting bundle,
						IStorage storage,
						IPreferencesFactory prefsFactory,
						IPreferences prefs,
						IWebDriverFactory webDriverFactory,
						ISearchRunFactory searchRunFactory,
						ISnapshotFactory snapshotFactory,
						ITweetFactory tweetFactory,
						IStatusMessageReceiver statusMessageReceiver,
						ISearchRunRepliesItinerary itinerary ) throws Exception {
		this.bundle = bundle;
		this.storage = storage;
		this.prefsFactory = prefsFactory;
		this.prefs = prefs;
		this.webDriverFactory = webDriverFactory;
		this.searchRunFactory = searchRunFactory;
		this.snapshotFactory = snapshotFactory;
		this.tweetFactory = tweetFactory;
		this.statusMessageReceiver = statusMessageReceiver;
		this.itinerary = itinerary;
		this.handleToCheck = Utils.trimDefault( itinerary.getInitiatingUser().getHandle() ).toLowerCase();

		this.helper = new SearchRunRepliesBuilderHelper( bundle,
															storage,
															prefsFactory,
															prefs,
															webDriverFactory,
															searchRunFactory,
															snapshotFactory,
															tweetFactory,
															statusMessageReceiver );
	}

	public ISearchRunReplies buildSearchRunReplies( int numberOfTimelinePagesToCheck, int numberOfReplyPagesToCheck, int maxReplies ) throws Exception {
		WebDriver webDriver = null;
		IWebDriverUtils webDriverUtils = null;
		LoginToSite loginToSite = null;

		if ( webDriverFactory == null ) {
			throw new RuntimeException( bundle.getString( "exc_no_webdriverfactory" ) );
		}

		statusMessageReceiver.addMessage( new StatusMessage( bundle.getString( "srrfib_msg_handle", handleToCheck ), StatusMessageSeverity.INFO ) );

		try {
			webDriver = webDriverFactory.makeWebDriver();
			int positionX = Utils.parseIntDefault( prefs.getValue( "prefs.firefox_screen_position_x" ) );
			int positionY = Utils.parseIntDefault( prefs.getValue( "prefs.firefox_screen_position_y" ) );
			webDriver.manage().window().setPosition( new Point( positionX, positionY ) );
		}
		catch ( Exception e ) {
			logger.error( "cannot create webDriver", e );
			statusMessageReceiver.addMessage( new StatusMessage( "cannot create webDriver", StatusMessageSeverity.ERROR ) );
			throw e;
		}

		try {
			webDriverUtils = webDriverFactory.makeWebDriverUtils( webDriver );

			String loginName = prefs.getValue( "prefs.testing_account_name_private" );
			String loginPassword = prefs.getValue( "prefs.testing_account_password_private" );

			if ( !Utils.isEmpty( loginName ) && !Utils.isEmpty( loginPassword ) ) {
				loginToSite = new LoginToSite( loginName, loginPassword, prefs );
				loginToSite.perform( webDriver, webDriverUtils );
			}

			ISearchRunReplies ret = buildSearchRunRepliesInternal( webDriver, webDriverUtils, numberOfTimelinePagesToCheck, numberOfReplyPagesToCheck, maxReplies );

			ret.setAttribute( "handle_to_check", handleToCheck );
			ret.setAttribute( "loggedin", loginToSite != null ? "true" : "false" );

			return ret;
		}
		catch ( Exception e ) {
			logger.error( "error logging in or building searchRun", e );
			throw e;
		}
		finally {
			if ( webDriver != null ) {
				try {
					webDriver.close();
				}
				catch ( Exception e ) {
					logger.error( "cannot close webDriver", e );
					statusMessageReceiver.addMessage( new StatusMessage( "cannot close webDriver", StatusMessageSeverity.ERROR ) );
				}
			}

			logInfo( bundle.getString( "srb_done", handleToCheck ) );
		}
	}

	protected ISearchRunReplies buildSearchRunRepliesInternal( WebDriver webDriver, IWebDriverUtils webDriverUtils,
																int numberOfTimelinePagesToCheck, int numberOfReplyPagesToCheck, int maxReplies ) throws Exception {
		Instant startTime = Instant.now();

		String url = String.format( prefs.getValue( "targetsite.pattern.timeline" ), handleToCheck );

		ISnapshotUserPageTimeline timeline = snapshotFactory.makeSnapshotUserPageTimeline( url, startTime );

		ITweetUser user = itinerary.getInitiatingUser();
		if ( user == null || Utils.isEmpty( user.getHandle() ) ) {
			throw new RuntimeException( "timeline does not have a user" );
		}

		timeline.setUser( user );
		timeline.setTweetCollection( itinerary.getTimelineTweetCollection() );
		timeline.setNumTotalTweets( itinerary.getInitiatingUser().getNumTotalTweets() );
		timeline.setNumFollowers( itinerary.getInitiatingUser().getNumFollowers() );
		timeline.setNumFollowing( itinerary.getInitiatingUser().getNumFollowing() );

		Map<Long,IReplyThread> replies;
		ITweetCollection tweetCollection = timeline.getTweetCollection();
		if ( tweetCollection == null || tweetCollection.getTweets() == null || tweetCollection.getTweets().size() < 1 ) {
			logWarn( bundle.getString( "srb_bad_timeline", url ) );
			replies = new HashMap<Long,IReplyThread>();
		}
		else {
			logInfo( bundle.getString( "srb_loaded_timeline", tweetCollection.getTweets().size() ) );
			replies = helper.getReplyPages( webDriver, webDriverUtils, tweetCollection.getTweets(),
										user, numberOfReplyPagesToCheck, maxReplies );
		}

		ISearchRunReplies ret = searchRunFactory.makeSearchRunReplies( 0, user, startTime, Instant.now(), timeline, replies );

		return ret;
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
