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

import java.io.File;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scijava.util.ClassUtils;
import org.scijava.util.FileUtils;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.utils.*;
import com.tolstoy.basic.app.utils.*;
import com.tolstoy.basic.app.tweet.TweetFactory;
import com.tolstoy.basic.app.storage.StorageEmbeddedDerby;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferencesFactory;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunProcessor;
import com.tolstoy.censorship.twitter.checker.api.analyzer.*;
import com.tolstoy.censorship.twitter.checker.app.preferences.PreferencesFactory;
import com.tolstoy.censorship.twitter.checker.app.webdriver.WebDriverFactoryJS;
import com.tolstoy.censorship.twitter.checker.app.snapshot.SnapshotFactory;
import com.tolstoy.censorship.twitter.checker.app.analyzer.AnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.app.searchrun.*;
import com.tolstoy.censorship.twitter.checker.app.gui.*;
import com.tolstoy.censorship.twitter.checker.app.helpers.*;

public final class Start {
	private static final Logger logger = LogManager.getLogger( Start.class );

		//	NOTE: if running via Maven, make this 2 otherwise the database
		//	and the reports will be deleted when you do a 'mvn clean'
	private static final int DIRECTORIES_LEVEL_UP = 1;

	private static final String[] TABLE_NAMES = { "searchrun", "preferences" };

	private static final String[] PREFERENCES_OVERRIDEABLE_BY_SYSTEM_PROPERTIES = { "prefs.firefox_path_app", "prefs.firefox_path_profile" };

	private static final boolean DEBUG_MODE = true;

	private IResourceBundleWithFormatting bundle = null;

	private Start() {
		Properties props = null;
		Map<String,String> defaultAppPrefs = null;
		IStorage storage = null;
		IPreferencesFactory prefsFactory = null;
		IPreferences prefs = null;
		IWebDriverFactory webDriverFactory = null;
		ISearchRunFactory searchRunFactory = null;
		ISnapshotFactory snapshotFactory = null;
		ITweetFactory tweetFactory = null;
		IAnalysisReportFactory analysisReportFactory = null;
		List<ISearchRunProcessor> searchRunProcessors = null;
		IAppDirectories appDirectories = null;
		String databaseConnectionString = null;

		try {
			props = new Properties();
			props.load( getClass().getClassLoader().getResourceAsStream( "app.properties" ) );
			defaultAppPrefs = new HashMap<String,String>();
			for ( Object key : props.keySet() ) {
				String keyString = String.valueOf( key );
				defaultAppPrefs.put( keyString, String.valueOf( props.getProperty( keyString ) ) );
			}

			//logger.info( "defaultAppPrefs=" + Utils.sanitizeMap( defaultAppPrefs ) );

			bundle = new ResourceBundleWithFormatting( "GUI" );
		}
		catch ( Exception e ) {
			handleError( true, "Could not initialize properties", e );
		}

		try {
			appDirectories = new AppDirectories( DIRECTORIES_LEVEL_UP,
													defaultAppPrefs.get( "storage.derby.dir_name" ),
													defaultAppPrefs.get( "storage.derby.db_name" ),
													defaultAppPrefs.get( "reports.dir_name" ) );

			if ( appDirectories.getDatabaseParentDirectory() == null || appDirectories.getReportsDirectory() == null || appDirectories.getDatabaseDirectory() == null ) {
				handleError( true, bundle.getString( "exc_db_dir", appDirectories.getInstallDirectory() ) );
			}

			databaseConnectionString = defaultAppPrefs.get( "storage.derby.connstring.start" ) +
										appDirectories.getDatabaseDirectory() +
										defaultAppPrefs.get( "storage.derby.connstring.end" );
		}
		catch ( Exception e ) {
			handleError( true, bundle.getString( "exc_install_loc" ), e );
		}

		try {
			storage = new StorageEmbeddedDerby( databaseConnectionString, Arrays.asList( TABLE_NAMES ) );

			storage.connect();
			storage.ensureTables();
		}
		catch ( Exception e ) {
			handleError( true, bundle.getString( "exc_db_init", databaseConnectionString ), e );
		}

		try {
			prefsFactory = new PreferencesFactory( storage, defaultAppPrefs );
			prefs = prefsFactory.getAppPreferences();

			IOverridePreferences[] overrides = {
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

			//logger.info( "prefs=" + Utils.sanitizeMap( prefs.getValues() ) );
		}
		catch ( Exception e ) {
			handleError( true, bundle.getString( "exc_prefs_init" ), e );
		}

		try {
			tweetFactory = new TweetFactory();
			snapshotFactory = new SnapshotFactory();
			searchRunFactory = new SearchRunFactory( tweetFactory );
			analysisReportFactory = new AnalysisReportFactory( tweetFactory, appDirectories, prefs, bundle );
		}
		catch ( Exception e ) {
			handleError( false, bundle.getString( "exc_tweetsnapshotfactory_init" ), e );
		}

		try {
			searchRunProcessors = new ArrayList<ISearchRunProcessor>( 2 );

			searchRunProcessors.add( new SearchRunProcessorInsertNewToStorage( bundle, prefs, storage ) );

			searchRunProcessors.add( new SearchRunProcessorUploadDataJson( bundle, prefs ) );

			searchRunProcessors.add( new SearchRunProcessorWriteReport( bundle, prefs, appDirectories, analysisReportFactory, DEBUG_MODE ) );
		}
		catch ( Exception e ) {
			handleError( false, bundle.getString( "exc_searchrunprocessors_init" ), e );
		}

		try {
			webDriverFactory = new WebDriverFactoryJS( snapshotFactory, tweetFactory, prefs, bundle );
		}
		catch ( Exception e ) {
			handleError( false, bundle.getString( "exc_webdriver_init" ), e );
		}

		if ( false ) {
			//	possible command line version
		}
		else {
			try {
				AppGUI app = new AppGUI( bundle,
											storage,
											prefsFactory,
											prefs,
											webDriverFactory,
											searchRunFactory,
											snapshotFactory,
											tweetFactory,
											analysisReportFactory,
											appDirectories,
											searchRunProcessors );
				app.run();
			}
			catch ( Exception e ) {
				handleError( false, bundle.getString( "exc_start", e.getMessage() ), e );
			}
		}
	}

	private void handleError( boolean closeOnExit, String msg, Exception e ) {
		logger.error( msg, e );
		showErrorMessage( closeOnExit, msg );
	}

	private void handleError( boolean closeOnExit, String msg ) {
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

		JFrame frame = new JFrame();
		JOptionPane optionPane = new JOptionPane( msg, JOptionPane.ERROR_MESSAGE );
		optionPane.addPropertyChangeListener( new PropertyChangeListener() {
			public void propertyChange( PropertyChangeEvent propertyChangeEvent ) {
				if ( JOptionPane.VALUE_PROPERTY.equals( propertyChangeEvent.getPropertyName() ) ) {
					if ( closeOnExit ) {
						System.exit( -1 );
					}
				}
			}
		});

		JDialog dialog = new JDialog( frame, dialogTitle, true );
		dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		dialog.setContentPane( optionPane );
		dialog.pack();
		dialog.setVisible( true );
	}

	public static void main(String[] args) {
		new Start();
	}
}

