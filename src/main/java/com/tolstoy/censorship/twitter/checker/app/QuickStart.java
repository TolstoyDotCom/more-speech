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
package com.tolstoy.censorship.twitter.checker.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.jbus.JBus;
import org.scijava.util.ClassUtils;

import com.tolstoy.basic.api.installation.DebugLevel;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.TargetPageType;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.storage.StorageEmbeddedDerby;
import com.tolstoy.basic.app.tweet.TweetFactory;
import com.tolstoy.basic.app.utils.ResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyFactory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseEvent;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseListener;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferencesFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunProcessor;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactoryFactory;
import com.tolstoy.censorship.twitter.checker.app.analyzer.AnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.app.browserproxy.BrowserProxyFactory;
import com.tolstoy.censorship.twitter.checker.app.helpers.AppDirectories;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;
import com.tolstoy.censorship.twitter.checker.app.helpers.IOverridePreferences;
import com.tolstoy.censorship.twitter.checker.app.helpers.OverridePreferencesFromEmbedPathsLinux;
import com.tolstoy.censorship.twitter.checker.app.helpers.OverridePreferencesFromEmbedPathsWindows;
import com.tolstoy.censorship.twitter.checker.app.helpers.OverridePreferencesFromSystemProperties;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunProcessorUploadDataJson;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunProcessorWriteReport;
import com.tolstoy.censorship.twitter.checker.app.installation.BrowserScriptFactory;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtensionFactory;
import com.tolstoy.censorship.twitter.checker.app.installation.BrowserExtensionFactory;
import com.tolstoy.censorship.twitter.checker.app.preferences.PreferencesFactory;
import com.tolstoy.censorship.twitter.checker.app.searchrun.SearchRunFactory;
import com.tolstoy.censorship.twitter.checker.app.snapshot.SnapshotFactory;
import com.tolstoy.censorship.twitter.checker.app.webdriver.WebDriverFactoryFactory;
import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPage;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunRepliesBuilder;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.basic.api.utils.IArchiveDirectory;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.app.utils.ArchiveDirectory;
import com.tolstoy.censorship.twitter.checker.app.helpers.LoginToSite;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.WebDriverFactoryType;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.InfiniteScrollingActivatorType;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IInfiniteScrollingActivator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Point;

public final class QuickStart implements IStatusMessageReceiver {
	private static final Logger logger = LogManager.getLogger( QuickStart.class );

	private static final String[] TABLE_NAMES = { "searchrun", "preferences" };
	private static final int WEBDRIVER_CLOSE_DELAY_MILLIS = 5000;

	private final IResourceBundleWithFormatting bundle;
	private final Map<String,String> defaultAppPrefs;
	private final IStorage storage;
	private final IPreferencesFactory prefsFactory;
	private final IPreferences prefs;
	private final IWebDriverFactoryFactory webDriverFactoryFactory;
	private final ISearchRunFactory searchRunFactory;
	private final ISnapshotFactory snapshotFactory;
	private final ITweetFactory tweetFactory;
	private final IAnalysisReportFactory analysisReportFactory;
	private final IAppDirectories appDirectories;
	private final IBrowserScriptFactory browserScriptFactory;
	private final IBrowserExtensionFactory browserExtensionFactory;
	private final IBrowserProxyFactory browserProxyFactory;
	private final IBrowserProxy browserProxy;
	private final IArchiveDirectory archiveDirectory;
	private final String databaseConnectionString;

	@SuppressWarnings("unused")
	private QuickStart() throws Exception {
		this.defaultAppPrefs = createDefaultAppPreferences();

		//logger.info( "defaultAppPrefs=" + Utils.sanitizeMap( defaultAppPrefs ) );

		this.bundle = new ResourceBundleWithFormatting( "GUI" );

		this.appDirectories = new AppDirectories( this.defaultAppPrefs.get( "storage.derby.dir_name" ),
												this.defaultAppPrefs.get( "storage.derby.db_name" ),
												this.defaultAppPrefs.get( "reports.dir_name" ) );

		this.browserScriptFactory = new BrowserScriptFactory( this.appDirectories.getSubdirectory( "stockscripts" ) );
		this.browserExtensionFactory = new BrowserExtensionFactory();

		this.archiveDirectory = new ArchiveDirectory( this.appDirectories.getReportsDirectory(), "json-", "", "", ".json" );

		this.databaseConnectionString = this.defaultAppPrefs.get( "storage.derby.connstring.start" ) +
									this.appDirectories.getDatabaseDirectory() +
									this.defaultAppPrefs.get( "storage.derby.connstring.end" );

		this.storage = new StorageEmbeddedDerby( this.databaseConnectionString, Arrays.asList( TABLE_NAMES ) );

		this.storage.connect();
		this.storage.ensureTables();

		this.prefsFactory = new PreferencesFactory( this.storage, this.defaultAppPrefs );
		this.prefs = this.prefsFactory.createPreferences( this.defaultAppPrefs );

		this.tweetFactory = new TweetFactory();
		this.snapshotFactory = new SnapshotFactory();
		this.searchRunFactory = new SearchRunFactory( this.tweetFactory );
		this.analysisReportFactory = new AnalysisReportFactory( this.tweetFactory, this.appDirectories, this.prefs, this.bundle );

		this.browserProxyFactory = new BrowserProxyFactory( this.prefs, this.bundle, new JBus(), browserScriptFactory );

		this.browserProxy = browserProxyFactory.makeBrowserDataRecorder();

		this.browserProxy.start();

		this.webDriverFactoryFactory = new WebDriverFactoryFactory( this.snapshotFactory,
																	this.tweetFactory,
																	this.appDirectories,
																	this.browserScriptFactory,
																	this.browserExtensionFactory,
																	this.prefs,
																	this.bundle,
																	DebugLevel.TERSE );

		final TargetPageType pageType = TargetPageType.getMatching( this.prefs.getValue( "prefs.quickstart_page_type" ) );
		final String url = TargetPageType.TIMELINE == pageType ? this.prefs.getValue( "prefs.quickstart_timeline_url" ) : this.prefs.getValue( "prefs.quickstart_replypage_url" );

		IWebDriverFactory webDriverFactory = this.webDriverFactoryFactory.makeWebDriverFactory( WebDriverFactoryType.NEWTWITTER_WITH_JAVASCRIPT );

		final ISnapshotUserPage snapshot = buildSearchRun( webDriverFactory, url, pageType );

		//logger.info( searchRunReplies );
		logger.info( "VALUENEXT" );
		logger.info( Utils.getDefaultObjectMapper().writeValueAsString( snapshot ) );
		logger.info( "DONE" );

		System.exit( -1 );
	}

	private ISnapshotUserPage buildSearchRun( IWebDriverFactory webDriverFactory, String url, TargetPageType pageType ) throws Exception {
		WebDriver webDriver = null;
		IWebDriverUtils webDriverUtils = null;
		LoginToSite loginToSite = null;
		String loginName = null;
		String loginPassword = null;
		boolean bSkipLogin;

		try {
			loginName = prefs.getValue( "prefs.testing_account_name_private" );
			loginPassword = prefs.getValue( "prefs.testing_account_password_private" );

			bSkipLogin = Utils.isStringTrue( prefs.getValue( "prefs.skip_login" ) );

			webDriver = webDriverFactory.makeWebDriver( browserProxy );
			final int positionX = Utils.parseIntDefault( prefs.getValue( "prefs.firefox_screen_position_x" ) );
			final int positionY = Utils.parseIntDefault( prefs.getValue( "prefs.firefox_screen_position_y" ) );
			webDriver.manage().window().setPosition( new Point( positionX, positionY ) );

			webDriverUtils = webDriverFactory.makeWebDriverUtils( webDriver );

			if ( !bSkipLogin ) {
				loginToSite = new LoginToSite( loginName, loginPassword, prefs );
				loginToSite.perform( webDriver, webDriverUtils );
			}

			webDriver.get( url );

			IInfiniteScrollingActivator scroller = webDriverFactory.makeInfiniteScrollingActivator( webDriver,
																		webDriverUtils,
																		InfiniteScrollingActivatorType.INDIVIDUAL );

			ISnapshotUserPage snapshot;
			if ( pageType == TargetPageType.REPLYPAGE ) {
				snapshot = webDriverFactory.makeSnapshotUserPageIndividualTweetFromURL( webDriver,
																						webDriverUtils,
																						scroller,
																						browserProxy,
																						archiveDirectory,
																						url,
																						100,
																						100 );
			}
			else {
				snapshot = webDriverFactory.makeSnapshotUserPageTimelineFromURL( webDriver,
																					webDriverUtils,
																					scroller,
																					browserProxy,
																					archiveDirectory,
																					url,
																					100,
																					100 );
			}

			return snapshot;
		}
		catch ( final Exception e ) {
			logger.error( "error in buildSearchRun", e );
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
					logger.info( "about to close webDriver" );
					Utils.delay( WEBDRIVER_CLOSE_DELAY_MILLIS );
					webDriver.close();
					logger.info( "closed webDriver" );
				}
				catch ( final Exception e ) {
					logger.error( "cannot close webDriver", e );
				}
			}
		}
	}

	private Map<String,String> createDefaultAppPreferences() throws Exception {
		File startDirectory = org.scijava.util.FileUtils.urlToFile( ClassUtils.getLocation( getClass() ) );

		File prefsFile = Utils.findFileGoingUp( startDirectory, "quickstart.prefs.json" );

		if ( prefsFile == null ) {
			throw new RuntimeException( "Preferences not found starting from " + startDirectory + " and proceeding upwards." );
		}
		
		logger.info( "Using preferences in " + prefsFile );

		Map<String,String> appPrefs = Utils.makeStringMap( Utils.readJSONMap( FileUtils.readFileToString( prefsFile, StandardCharsets.UTF_8 ) ) );

		return appPrefs;
	}

	@Override
	public void addMessage( final StatusMessage message ) {
		logger.info( message );
	}

	@Override
	public void clearMessages() {
	}

	public static void main(final String[] args) {
		try {
			new QuickStart();
			logger.info( "main ending" );
		}
		catch ( final Exception e ) {
			logger.error( "Error", e );
			e.printStackTrace();
			System.exit( -1 );
		}
	}
}
