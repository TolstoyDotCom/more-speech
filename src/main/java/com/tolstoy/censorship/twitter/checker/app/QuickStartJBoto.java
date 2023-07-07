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
import java.nio.charset.Charset;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
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
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunRepliesItinerary;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseListener;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunTimeline;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunTimelineBuilder;
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
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtension;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtensionList;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtensionFactory;
import com.tolstoy.censorship.twitter.checker.app.installation.BrowserExtensionFactory;
import com.tolstoy.censorship.twitter.checker.app.helpers.IOverridePreferences;
import com.tolstoy.censorship.twitter.checker.app.helpers.OverridePreferencesFromEmbedPathsLinux;
import com.tolstoy.censorship.twitter.checker.app.helpers.OverridePreferencesFromEmbedPathsWindows;
import com.tolstoy.censorship.twitter.checker.app.helpers.OverridePreferencesFromSystemProperties;
import com.tolstoy.censorship.twitter.checker.app.installation.BrowserScriptFactory;
import com.tolstoy.censorship.twitter.checker.app.preferences.PreferencesFactory;
import com.tolstoy.censorship.twitter.checker.app.searchrun.SearchRunFactory;
import com.tolstoy.censorship.twitter.checker.app.snapshot.SnapshotFactory;
import com.tolstoy.censorship.twitter.checker.app.webdriver.WebDriverFactoryFactory;
import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPage;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunItinerary;
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
import com.tolstoy.censorship.twitter.checker.app.jboto.replies.RepliesProduct;
import com.tolstoy.censorship.twitter.checker.app.jboto.timeline.SearchRunTimelineData;
import com.tolstoy.censorship.twitter.checker.app.jboto.OurEnvironment;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersFactory;
import com.tolstoy.censorship.twitter.checker.app.webdriver.PageParametersFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersSet;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParameters;
import com.tolstoy.jboto.api.framework.IFrameworkFactory;
import com.tolstoy.jboto.api.framework.IFramework;
import com.tolstoy.jboto.app.framework.FrameworkFactory;
import com.tolstoy.jboto.api.framework.IPackageAlias;
import com.tolstoy.jboto.api.framework.IFQNResolverFactory;
import com.tolstoy.jboto.api.framework.IFQNResolver;
import com.tolstoy.jboto.app.framework.FQNResolverFactory;
import com.tolstoy.jboto.app.framework.FrameworkFactory;

public final class QuickStartJBoto implements IStatusMessageReceiver {
	private static final Logger logger = LogManager.getLogger( QuickStartJBoto.class );

	private static final String[] TABLE_NAMES = { "searchrun", "preferences" };
	private static final String JBOTO_DEFAULT_PACKAGE_NAME = "com.tolstoy.censorship.twitter.checker.app.jboto";
	private static final String JBOTO_COMMON_PACKAGE_NAME = "com.tolstoy.censorship.twitter.checker.app.jboto.common";
	private static final String JBOTO_REPLIES_PACKAGE_NAME = "com.tolstoy.censorship.twitter.checker.app.jboto.replies";
	private static final String JBOTO_TIMELINE_PACKAGE_NAME = "com.tolstoy.censorship.twitter.checker.app.jboto.timeline";
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
	private final IBrowserProxyFactory browserProxyFactory;
	private final IArchiveDirectory archiveDirectory;
	private final IBrowserExtensionFactory browserExtensionFactory;
	private final IPageParametersFactory pageParametersFactory;
	private final OurEnvironment env;
	private final String databaseConnectionString, handleToCheck;
	private final IPageParametersSet pageParametersSet;

	@SuppressWarnings("unused")
	private QuickStartJBoto() throws Exception {
		this.defaultAppPrefs = createDefaultAppPreferences();

		//logger.info( "defaultAppPrefs=" + Utils.sanitizeMap( defaultAppPrefs ) );

		this.bundle = new ResourceBundleWithFormatting( "GUI" );

		this.appDirectories = new AppDirectories( this.defaultAppPrefs.get( "storage.derby.dir_name" ),
												this.defaultAppPrefs.get( "storage.derby.db_name" ),
												this.defaultAppPrefs.get( "reports.dir_name" ) );

		this.browserScriptFactory = new BrowserScriptFactory( this.appDirectories.getSubdirectory( "stockscripts" ) );

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
		this.pageParametersFactory = new PageParametersFactory();

		this.browserProxyFactory = new BrowserProxyFactory( this.prefs, this.bundle, new JBus(), this.browserScriptFactory );

		this.pageParametersSet = buildPageParametersSet( prefs );

		//	@todo: make this a setting
		this.browserExtensionFactory = new BrowserExtensionFactory();
		final IBrowserExtensionList extensionsToInstall = this.browserExtensionFactory.makeBrowserExtensionList();
		extensionsToInstall.add( this.browserExtensionFactory.makeBrowserExtension( "har_export_trigger", "/har_export_trigger-0.6.1.xpi" ) );

		this.webDriverFactoryFactory = new WebDriverFactoryFactory( this.snapshotFactory,
																	this.tweetFactory,
																	this.appDirectories,
																	this.browserScriptFactory,
																	this.browserExtensionFactory,
																	this.prefs,
																	this.bundle,
																	DebugLevel.TERSE );

		this.handleToCheck = this.prefs.getValue( "prefs.handle_to_check" );

		this.env = new OurEnvironment( this.bundle,
										this.storage,
										this.prefsFactory,
										this.prefs,
										webDriverFactoryFactory,
										this.searchRunFactory,
										this.snapshotFactory,
										this.tweetFactory,
										this.browserProxyFactory,
										this.archiveDirectory,
										this,
										this.appDirectories,
										this.analysisReportFactory,
										this.browserScriptFactory,
										this.browserExtensionFactory,
										extensionsToInstall,
										DebugLevel.TERSE );
	}

	//final TargetPageType pageType = TargetPageType.getMatching( this.prefs.getValue( "prefs.quickstart_page_type" ) );
	//final String url = TargetPageType.TIMELINE == pageType ? this.prefs.getValue( "prefs.quickstart_timeline_url" ) : this.prefs.getValue( "prefs.quickstart_replypage_url" );

	private void doReplies( final File itineraryFile ) throws Exception {
		ISearchRunRepliesItinerary itinerary = createItinerary( itineraryFile );

		RepliesProduct product = new RepliesProduct( prefs,
														handleToCheck,
														itinerary,
														pageParametersSet );

		String testJSON = IOUtils.toString( getClass().getResource( "/jboto-replies-qs.json" ), StandardCharsets.UTF_8 );

		IFrameworkFactory factory = new FrameworkFactory( createResolver() );

		IFramework framework = factory.makeFrameworkFromJSON( "test", testJSON );

		logger.info( "\n" + framework.toDebugString( "" ) );

		logger.info( "product BEFORE: " + product );

		framework.run( product, env, null, 0 );

		logger.info( "product AFTER: " + product );

		System.exit( -1 );
	}

	private void doTimeline() throws Exception {
		SearchRunTimelineData product = new SearchRunTimelineData( prefs, handleToCheck, pageParametersSet );

		String testJSON = IOUtils.toString( getClass().getResource( "/jboto-timeline-qs.json" ), StandardCharsets.UTF_8 );

		IFrameworkFactory factory = new FrameworkFactory( createResolver() );

		IFramework framework = factory.makeFrameworkFromJSON( "test", testJSON );

		logger.info( "\n" + framework.toDebugString( "" ) );

		logger.info( "product BEFORE: " + product );

		framework.run( product, env, null, 0 );

		logger.info( "product AFTER: " + product );

		System.exit( -1 );
	}

	private Map<String,String> createDefaultAppPreferences() throws Exception {
		Properties systemProps = System.getProperties();

		Properties props = new Properties();
		props.load( getClass().getClassLoader().getResourceAsStream( "app.properties" ) );

		Map<String,String> map = new HashMap<String,String>();
		for ( final Object key : props.keySet() ) {
			final String keyString = String.valueOf( key );
			final Object systemValue = systemProps.getProperty( keyString );
			final String value = systemValue != null ? String.valueOf( systemValue ) : props.getProperty( keyString );
			logger.info( keyString + "=" + value );
			map.put( keyString, value );
		}

		return map;
	}

	private ISearchRunRepliesItinerary createItinerary( final File itineraryFile ) throws Exception {
		ISearchRunItinerary itinerary;

		try {
			final String jsonData = FileUtils.readFileToString( itineraryFile, Charset.defaultCharset() );
			itinerary = searchRunFactory.makeSearchRunItineraryFromJSON( jsonData );
		}
		catch ( final Exception e ) {
			final String s = bundle.getString( "exc_bad_itinerary_file", itineraryFile.getAbsolutePath() );
			Utils.logException( logger, s, e );
			throw e;
		}

		if ( !( itinerary instanceof ISearchRunRepliesItinerary ) ) {
			final String s = bundle.getString( "exc_bad_itinerary_file", itineraryFile.getAbsolutePath() );
			throw new RuntimeException( s );
		}

		return (ISearchRunRepliesItinerary) itinerary;
	}

	private IPageParametersSet buildPageParametersSet( IPreferences prefs ) throws Exception {
		IPageParameters timeline = pageParametersFactory.makePageParametersBuilder().
			setItemsToSkip( Utils.parseIntDefault( prefs.getValue( "prefs.timeline_num_items_to_skip" ), 0 ) ).
			setItemsToProcess( Utils.parseIntDefault( prefs.getValue( "prefs.timeline_num_items_to_process" ), 0 ) ).
			setPagesToScroll( Utils.parseIntDefault( prefs.getValue( "prefs.timeline_num_pages_to_scroll" ), 0 ) ).
			build();

		IPageParameters individualPage = pageParametersFactory.makePageParametersBuilder().
			setItemsToSkip( 0 ).
			setItemsToProcess( 1000 ).
			setPagesToScroll( Utils.parseIntDefault( prefs.getValue( "prefs.individual_pages_num_pages_to_scroll" ), 0 ) ).
			build();

		return pageParametersFactory.makePageParametersSetBuilder().
			setTimeline( timeline ).
			setIndividualPage( individualPage ).
			build();
	}

	@Override
	public void addMessage( final StatusMessage message ) {
		logger.info( message );
	}

	@Override
	public void clearMessages() {
	}

	private IFQNResolver createResolver() throws Exception {
		IFQNResolverFactory resolverFactory = new FQNResolverFactory();
		List<IPackageAlias> aliases = new ArrayList<IPackageAlias>( 1 );

		aliases.add( resolverFactory.makePackageAlias( "common", JBOTO_COMMON_PACKAGE_NAME ) );
		aliases.add( resolverFactory.makePackageAlias( "replies", JBOTO_REPLIES_PACKAGE_NAME ) );
		aliases.add( resolverFactory.makePackageAlias( "timeline", JBOTO_TIMELINE_PACKAGE_NAME ) );

		IFQNResolver resolver = resolverFactory.makeResolver( JBOTO_DEFAULT_PACKAGE_NAME, aliases );

		return resolver;
	}

	public static void main( final String[] args ) throws Exception {
		Properties systemProps = System.getProperties();
		String cmd = (String) systemProps.getProperty( "prefs.run_type" );
		if ( cmd == null ) {
			throw new IllegalArgumentException( "You must provide 'prefs.run_type'" );
		}

		String itineraryFile = (String) systemProps.getProperty( "prefs.itinerary_file" );
		if ( "replies".equals( cmd ) && itineraryFile == null ) {
			throw new IllegalArgumentException( "You must provide 'prefs.itinerary_file' if 'prefs.itinerary_file' is 'replies'" );
		}

		try {
			QuickStartJBoto qs = new QuickStartJBoto();

			if ( "replies".equals( cmd ) ) {
				qs.doReplies( new File( itineraryFile ) );
			}
			else {
				qs.doTimeline();
			}

			logger.info( "main ending" );
		}
		catch ( final Exception e ) {
			logger.error( "Error", e );
			e.printStackTrace();
			System.exit( -1 );
		}
	}
}
