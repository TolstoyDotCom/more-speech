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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.jbus.JBus;

import com.tolstoy.basic.api.installation.DebugLevel;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.storage.StorageEmbeddedDerby;
import com.tolstoy.basic.app.tweet.TweetFactory;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.app.utils.ResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyFactory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
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
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtensionFactory;
import com.tolstoy.censorship.twitter.checker.app.installation.BrowserExtensionFactory;
import com.tolstoy.censorship.twitter.checker.app.helpers.IOverridePreferences;
import com.tolstoy.censorship.twitter.checker.app.helpers.OverridePreferencesFromEmbedPathsLinux;
import com.tolstoy.censorship.twitter.checker.app.helpers.OverridePreferencesFromEmbedPathsWindows;
import com.tolstoy.censorship.twitter.checker.app.helpers.OverridePreferencesFromSystemProperties;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunProcessorInsertNewToStorage;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunProcessorUploadDataJson;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunProcessorWriteReport;
import com.tolstoy.censorship.twitter.checker.app.installation.BrowserScriptFactory;
import com.tolstoy.censorship.twitter.checker.app.preferences.PreferencesFactory;
import com.tolstoy.censorship.twitter.checker.app.searchrun.SearchRunFactory;
import com.tolstoy.censorship.twitter.checker.app.snapshot.SnapshotFactory;
import com.tolstoy.censorship.twitter.checker.app.webdriver.WebDriverFactoryFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersFactory;
import com.tolstoy.censorship.twitter.checker.app.webdriver.PageParametersFactory;

public final class Start {
	private static final Logger logger = LogManager.getLogger( Start.class );

	private static final String[] TABLE_NAMES = { "searchrun", "preferences" };

	private static final String[] PREFERENCES_OVERRIDEABLE_BY_SYSTEM_PROPERTIES = { "prefs.firefox_path_app", "prefs.firefox_path_profile" };

	private IResourceBundleWithFormatting bundle = null;

	@SuppressWarnings("unused")
	private Start() {
		Properties props = null;
		Map<String,String> defaultAppPrefs = null;
		IStorage storage = null;
		IPreferencesFactory prefsFactory = null;
		IPreferences prefs = null;
		IWebDriverFactoryFactory webDriverFactoryFactory = null;
		ISearchRunFactory searchRunFactory = null;
		ISnapshotFactory snapshotFactory = null;
		ITweetFactory tweetFactory = null;
		IAnalysisReportFactory analysisReportFactory = null;
		List<ISearchRunProcessor> searchRunProcessors = null;
		IAppDirectories appDirectories = null;
		IBrowserScriptFactory browserScriptFactory = null;
		IBrowserExtensionFactory browserExtensionFactory = null;
		IPageParametersFactory pageParametersFactory = null;
		IBrowserProxyFactory browserProxyFactory = null;
		IBrowserProxy browserProxy = null;
		String databaseConnectionString = null;

		try {
			props = new Properties();
			props.load( getClass().getClassLoader().getResourceAsStream( "app.properties" ) );
			defaultAppPrefs = new HashMap<String,String>();
			for ( final Object key : props.keySet() ) {
				final String keyString = String.valueOf( key );
				defaultAppPrefs.put( keyString, String.valueOf( props.getProperty( keyString ) ) );
			}

			//logger.info( "defaultAppPrefs=" + Utils.sanitizeMap( defaultAppPrefs ) );

			bundle = new ResourceBundleWithFormatting( "GUI" );
		}
		catch ( final Exception e ) {
			handleError( true, "Could not initialize properties", e );
		}

		try {
			appDirectories = new AppDirectories( defaultAppPrefs.get( "storage.derby.dir_name" ),
													defaultAppPrefs.get( "storage.derby.db_name" ),
													defaultAppPrefs.get( "reports.dir_name" ) );

			databaseConnectionString = defaultAppPrefs.get( "storage.derby.connstring.start" ) +
										appDirectories.getDatabaseDirectory() +
										defaultAppPrefs.get( "storage.derby.connstring.end" );

			browserScriptFactory = new BrowserScriptFactory( appDirectories.getSubdirectory( "stockscripts" ) );

			browserExtensionFactory = new BrowserExtensionFactory();
		}
		catch ( final Exception e ) {
			handleError( true, bundle.getString( "exc_install_loc" ), e );
		}

		try {
			storage = new StorageEmbeddedDerby( databaseConnectionString, Arrays.asList( TABLE_NAMES ) );

			storage.connect();
			storage.ensureTables();
		}
		catch ( final Exception e ) {
			handleError( true, bundle.getString( "exc_db_init", databaseConnectionString ), e );
		}

		try {
			prefsFactory = new PreferencesFactory( storage, defaultAppPrefs );
			prefs = prefsFactory.getAppPreferences();

			logger.info( "prefs before override=" + Utils.prettyPrintMap( "", Utils.sanitizeMap( prefs.getValues() ) ) );

			final IOverridePreferences[] overrides = {
				new OverridePreferencesFromSystemProperties( PREFERENCES_OVERRIDEABLE_BY_SYSTEM_PROPERTIES ),
				new OverridePreferencesFromEmbedPathsLinux( appDirectories ),
				new OverridePreferencesFromEmbedPathsWindows( appDirectories )
			};

			boolean bNeedToSave = false;

			for ( int i = 0; i < overrides.length; i++ ) {
				if ( overrides[ i ].override( prefs, bundle ) ) {
					bNeedToSave = true;
				}
			}

			if ( bNeedToSave ) {
				prefs.save();
			}

			logger.info( "prefs after override=" + Utils.prettyPrintMap( "", Utils.sanitizeMap( prefs.getValues() ) ) );
		}
		catch ( final Exception e ) {
			handleError( true, bundle.getString( "exc_prefs_init" ), e );
		}

		try {
			tweetFactory = new TweetFactory();
			snapshotFactory = new SnapshotFactory();
			searchRunFactory = new SearchRunFactory( tweetFactory );
			analysisReportFactory = new AnalysisReportFactory( tweetFactory, appDirectories, prefs, bundle );
			pageParametersFactory = new PageParametersFactory();
		}
		catch ( final Exception e ) {
			handleError( false, bundle.getString( "exc_tweetsnapshotfactory_init" ), e );
		}

		try {
			searchRunProcessors = new ArrayList<ISearchRunProcessor>( 3 );

			searchRunProcessors.add( new SearchRunProcessorInsertNewToStorage( bundle, prefs, storage ) );

			searchRunProcessors.add( new SearchRunProcessorUploadDataJson( bundle, prefs ) );

			searchRunProcessors.add( new SearchRunProcessorWriteReport( bundle, prefs, appDirectories, analysisReportFactory, DebugLevel.TERSE ) );
		}
		catch ( final Exception e ) {
			handleError( false, bundle.getString( "exc_searchrunprocessors_init" ), e );
		}

		try {
			//	@todo: put all mentions of jbus into an interface
			browserProxyFactory = new BrowserProxyFactory( prefs, bundle, new JBus(), browserScriptFactory );

			webDriverFactoryFactory = new WebDriverFactoryFactory( snapshotFactory,
																	tweetFactory,
																	appDirectories,
																	browserScriptFactory,
																	browserExtensionFactory,
																	prefs,
																	bundle,
																	DebugLevel.TERSE );
		}
		catch ( final Exception e ) {
			handleError( false, bundle.getString( "exc_webdriver_init" ), e );
		}

		if ( false ) {
			//	possible command line version
		}
		else {
			try {
				final AppGUI app = new AppGUI( bundle,
												storage,
												prefsFactory,
												prefs,
												webDriverFactoryFactory,
												searchRunFactory,
												snapshotFactory,
												tweetFactory,
												analysisReportFactory,
												browserProxyFactory,
												appDirectories,
												browserScriptFactory,
												browserExtensionFactory,
												pageParametersFactory,
												searchRunProcessors );
				app.run();
			}
			catch ( final Exception e ) {
				handleError( false, bundle.getString( "exc_start", e.getMessage() ), e );
			}
		}
	}

	private void handleError( final boolean closeOnExit, final String msg, final Exception e ) {
		logger.error( msg, e );
		showErrorMessage( closeOnExit, msg );
	}

	private void handleError( final boolean closeOnExit, final String msg ) {
		logger.error( msg );
		showErrorMessage( closeOnExit, msg );
	}

	private void showErrorMessage( final boolean closeOnExit, String msg ) {
		String dialogTitle;

		if ( bundle != null ) {
			msg += bundle.getString( "notice_logfile" );
			dialogTitle = bundle.getString( "notice_title" );
		}
		else {
			msg += "\nPlease check the log file and post the details if the problem continues.";
			dialogTitle = "An error occurred";
		}

		final JFrame frame = new JFrame();
		final JOptionPane optionPane = new JOptionPane( msg, JOptionPane.ERROR_MESSAGE );
		optionPane.addPropertyChangeListener( new PropertyChangeListener() {
			@Override
			public void propertyChange( final PropertyChangeEvent propertyChangeEvent ) {
				if ( JOptionPane.VALUE_PROPERTY.equals( propertyChangeEvent.getPropertyName() ) ) {
					if ( closeOnExit ) {
						System.exit( -1 );
					}
				}
			}
		});

		final JDialog dialog = new JDialog( frame, dialogTitle, true );
		dialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		dialog.setContentPane( optionPane );
		dialog.pack();
		dialog.setVisible( true );
	}

	public static void main(final String[] args) {
		new Start();
	}
}

