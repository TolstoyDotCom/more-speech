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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.installation.DebugLevel;
import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.api.storage.IStorable;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.storage.StorageOrdering;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.api.utils.IArchiveDirectory;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.app.utils.ArchiveDirectory;
import com.tolstoy.basic.gui.ElementDescriptor;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyFactory;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferencesFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRun;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunItinerary;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunProcessor;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunRepliesItinerary;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunTimeline;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactoryFactory;
import com.tolstoy.censorship.twitter.checker.app.gui.MainGUI;
import com.tolstoy.censorship.twitter.checker.app.gui.PreferencesEvent;
import com.tolstoy.censorship.twitter.checker.app.gui.PreferencesEventListener;
import com.tolstoy.censorship.twitter.checker.app.gui.RunEvent;
import com.tolstoy.censorship.twitter.checker.app.gui.RunEventListener;
import com.tolstoy.censorship.twitter.checker.app.gui.RunItineraryEvent;
import com.tolstoy.censorship.twitter.checker.app.gui.RunItineraryEventListener;
import com.tolstoy.censorship.twitter.checker.app.gui.WindowClosingEvent;
import com.tolstoy.censorship.twitter.checker.app.gui.WindowClosingEventListener;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunProcessorWriteReport;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunRepliesBuilder;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunRepliesFromItineraryBuilder;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunTimelineBuilder;
import com.tolstoy.censorship.twitter.checker.app.storage.StorageTable;

public class AppGUI implements RunEventListener, PreferencesEventListener, RunItineraryEventListener, WindowClosingEventListener {
	private static final Logger logger = LogManager.getLogger( AppGUI.class );

	private final IResourceBundleWithFormatting bundle;
	private final IStorage storage;
	private final IPreferencesFactory prefsFactory;
	private final IPreferences prefs;
	private final IWebDriverFactoryFactory webDriverFactoryFactory;
	private final ISearchRunFactory searchRunFactory;
	private final ISnapshotFactory snapshotFactory;
	private final ITweetFactory tweetFactory;
	private final IAnalysisReportFactory analysisReportFactory;
	private final IBrowserProxyFactory browserProxyFactory;
	private final IAppDirectories appDirectories;
	private final List<ISearchRunProcessor> searchRunProcessors;
	private List<ElementDescriptor> guiElements;
	private MainGUI gui;

	abstract class WorkerBase<T> extends SwingWorker<T, StatusMessage> implements IStatusMessageReceiver {
		@Override
		protected void process( final List<StatusMessage> messages ) {
			for ( final StatusMessage message : messages ) {
				if ( message == null ) {
					gui.clearMessages();
				}
				else {
					gui.addMessage( message );
				}
			}
		}

		@Override
		public void addMessage( final StatusMessage message ) {
			publish( message );
		}

		@Override
		public void clearMessages() {
			publish( (StatusMessage) null );
		}
	}

	abstract class WorkerProcessingBase<T extends ISearchRun> extends WorkerBase<T> implements IStatusMessageReceiver {
		@Override
		public void done() {
			try {
				final T searchRun = get();
				//logger.info( searchRun );

				if ( searchRun != null ) {
					for ( final ISearchRunProcessor processor : searchRunProcessors ) {
						logger.info( "WorkerProcessingBase: calling " + processor );
						try {
							processor.process( searchRun, this );
						}
						catch ( final Exception e ) {
							final String s = bundle.getString( "exc_srp", processor.getDescription(), e.getMessage() );
							Utils.logException( logger, s, e );
							gui.addMessage( new StatusMessage( s, StatusMessageSeverity.ERROR ) );
						}
					}
				}
			}
			catch ( final Exception e ) {
				final String s = bundle.getString( "exc_getresults", e.getMessage() );
				Utils.logException( logger, s, e );
				gui.addMessage( new StatusMessage( s, StatusMessageSeverity.ERROR ) );
			}

			gui.enableRunFunction( true );
			gui.enablePreferencesFunction( true );
		}
	}

	class RepliesWorker extends WorkerProcessingBase<ISearchRunReplies> {
		@Override
		public ISearchRunReplies doInBackground() {
			try {
				IArchiveDirectory archiveDirectory = new ArchiveDirectory( appDirectories.getReportsDirectory(), "json-", "", "", ".json" );

				final SearchRunRepliesBuilder builder = new SearchRunRepliesBuilder( bundle,
												storage,
												prefsFactory,
												prefs,
												webDriverFactoryFactory,
												searchRunFactory,
												snapshotFactory,
												tweetFactory,
												browserProxyFactory,
												archiveDirectory,
												this,
												prefs.getValue( "prefs.handle_to_check" ) );

				final int numTimelinePagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_timeline_pages_to_check" ), 1 );
				final int numIndividualPagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_individual_pages_to_check" ), 3 );
				final int maxTweets = Utils.parseIntDefault( prefs.getValue( "prefs.num_tweets_to_check" ), 5 );

				final ISearchRunReplies searchRunReplies = builder.buildSearchRunReplies( numTimelinePagesToCheck,
																					numIndividualPagesToCheck,
																					maxTweets );

				//logger.info( searchRunReplies );
				logger.info( "VALUENEXT" );
				logger.info( Utils.getDefaultObjectMapper().writeValueAsString( searchRunReplies ) );
				return searchRunReplies;
			}
			catch ( final Exception e ) {
				Utils.logException( logger, bundle.getString( "exc_start", e.getMessage() ), e );
				publish( new StatusMessage( bundle.getString( "exc_start", e.getMessage() ), StatusMessageSeverity.ERROR ) );

				return null;
			}
		}
	}

	class TimelineWorker extends WorkerProcessingBase<ISearchRunTimeline> {
		@Override
		public ISearchRunTimeline doInBackground() {
			try {
				IArchiveDirectory archiveDirectory = new ArchiveDirectory( appDirectories.getReportsDirectory(), "json-", "", "", ".json" );

				final SearchRunTimelineBuilder builder = new SearchRunTimelineBuilder( bundle,
																					storage,
																					prefsFactory,
																					prefs,
																					webDriverFactoryFactory,
																					searchRunFactory,
																					snapshotFactory,
																					tweetFactory,
																					browserProxyFactory,
																					archiveDirectory,
																					this,
																					prefs.getValue( "prefs.handle_to_check" ) );

				final int numTimelinePagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_timeline_pages_to_check" ), 1 );
				final int numIndividualPagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_individual_pages_to_check" ), 3 );
				final int maxTweets = Utils.parseIntDefault( prefs.getValue( "prefs.num_tweets_to_check" ), 5 );
				final int numTimelineTweetsToSkip = Utils.parseIntDefault( prefs.getValue( "prefs.num_timeline_tweets_to_skip" ), 0 );

				final ISearchRunTimeline searchRunTimeline = builder.buildSearchRunTimeline( numTimelinePagesToCheck,
																						numIndividualPagesToCheck,
																						maxTweets,
																						numTimelineTweetsToSkip );

				//logger.info( searchRunTimeline );
				logger.info( "VALUENEXT" );
				logger.info( Utils.getDefaultObjectMapper().writeValueAsString( searchRunTimeline ) );
				return searchRunTimeline;
			}
			catch ( final Exception e ) {
				Utils.logException( logger, bundle.getString( "exc_start", e.getMessage() ), e );
				publish( new StatusMessage( bundle.getString( "exc_start", e.getMessage() ), StatusMessageSeverity.ERROR ) );

				return null;
			}
		}
	}

	class ItineraryRepliesWorker extends WorkerProcessingBase<ISearchRunReplies> {
		private final ISearchRunRepliesItinerary itinerary;

		public ItineraryRepliesWorker( final ISearchRunRepliesItinerary itinerary ) {
			this.itinerary = itinerary;
		}

		@Override
		public ISearchRunReplies doInBackground() {
			try {
				IArchiveDirectory archiveDirectory = new ArchiveDirectory( appDirectories.getReportsDirectory(), "json-", "", "", ".json" );

				final SearchRunRepliesFromItineraryBuilder builder = new SearchRunRepliesFromItineraryBuilder( bundle,
																storage,
																prefsFactory,
																prefs,
																webDriverFactoryFactory,
																searchRunFactory,
																snapshotFactory,
																tweetFactory,
																browserProxyFactory,
																archiveDirectory,
																this,
																itinerary );

				final int numTimelinePagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_timeline_pages_to_check" ), 1 );
				final int numIndividualPagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_individual_pages_to_check" ), 3 );
				final int maxTweets = Utils.parseIntDefault( prefs.getValue( "prefs.num_tweets_to_check" ), 5 );

				final ISearchRunReplies searchRunReplies = builder.buildSearchRunReplies( numTimelinePagesToCheck,
																					numIndividualPagesToCheck,
																					maxTweets );

				//logger.info( searchRunReplies );
				logger.info( "VALUENEXT" );
				logger.info( Utils.getDefaultObjectMapper().writeValueAsString( searchRunReplies ) );
				return searchRunReplies;
			}
			catch ( final Exception e ) {
				Utils.logException( logger, bundle.getString( "exc_start", e.getMessage() ), e );
				publish( new StatusMessage( bundle.getString( "exc_start", e.getMessage() ), StatusMessageSeverity.ERROR ) );

				return null;
			}
		}
	}

	class RewriteWorker<Void> extends WorkerBase<Void> implements IStatusMessageReceiver {
		@Override
		public Void doInBackground() {
			try {
				final List<IStorable> storables = storage.getRecords( StorageTable.SEARCHRUN, StorageOrdering.DESC, 1 );
				if ( !storables.isEmpty() ) {
					final IStorable storable = storables.get( 0 );
					final ISearchRun searchRun = (ISearchRun) storable;

					final SearchRunProcessorWriteReport writeReport = new SearchRunProcessorWriteReport( bundle, prefs, appDirectories,
																											analysisReportFactory, DebugLevel.VERBOSE );

					writeReport.process( searchRun, this );
				}
			}
			catch ( final Exception e ) {
				Utils.logException( logger, bundle.getString( "exc_start", e.getMessage() ), e );
				publish( new StatusMessage( bundle.getString( "exc_start", e.getMessage() ), StatusMessageSeverity.ERROR ) );

			}

			return null;
		}

		@Override
		public void done() {
			gui.enableRunFunction( true );
			gui.enablePreferencesFunction( true );
		}
	}

	public AppGUI( final IResourceBundleWithFormatting bundle,
					final IStorage storage,
					final IPreferencesFactory prefsFactory,
					final IPreferences prefs,
					final IWebDriverFactoryFactory webDriverFactoryFactory,
					final ISearchRunFactory searchRunFactory,
					final ISnapshotFactory snapshotFactory,
					final ITweetFactory tweetFactory,
					final IAnalysisReportFactory analysisReportFactory,
					final IBrowserProxyFactory browserProxyFactory,
					final IAppDirectories appDirectories,
					final List<ISearchRunProcessor> searchRunProcessors ) throws Exception {
		this.bundle = bundle;
		this.storage = storage;
		this.prefsFactory = prefsFactory;
		this.prefs = prefs;
		this.webDriverFactoryFactory = webDriverFactoryFactory;
		this.searchRunFactory = searchRunFactory;
		this.snapshotFactory = snapshotFactory;
		this.tweetFactory = tweetFactory;
		this.analysisReportFactory = analysisReportFactory;
		this.browserProxyFactory = browserProxyFactory;
		this.appDirectories = appDirectories;
		this.searchRunProcessors = searchRunProcessors;
	}

	public void run() throws Exception {
		try {
			//UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			UIManager.setLookAndFeel( "javax.swing.plaf.nimbus.NimbusLookAndFeel" );
			//UIManager.setLookAndFeel( "com.pagosoft.plaf.PgsLookAndFeel" );
		}
		catch ( final Exception e ) {
			e.printStackTrace();
		}

		buildGUIElements();

		gui = new MainGUI( bundle, prefs, guiElements );

		gui.addRunEventListener( this );
		gui.addPreferencesEventListener( this );
		gui.addRunItineraryEventListener( this );
		gui.addWindowClosingEventListener( this );

		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				gui.showGUI();
				validatePreferences();
			}
		});
	}

	@Override
	public void runEventFired( final RunEvent runEvent ) {
		gui.enableRunFunction( false );
		gui.enablePreferencesFunction( false );

		if ( MainGUI.ACTION_REPLIES.equals( runEvent.getActionName() ) ) {
			final RepliesWorker repliesWorker = new RepliesWorker();

			repliesWorker.execute();
		}
		else if ( MainGUI.ACTION_TIMELINE.equals( runEvent.getActionName() ) ) {
			final TimelineWorker timelineWorker = new TimelineWorker();

			timelineWorker.execute();
		}
		else if ( MainGUI.ACTION_REWRITE_LAST_REPORT.equals( runEvent.getActionName() ) ) {
			final RewriteWorker rewriteWorker = new RewriteWorker();

			rewriteWorker.execute();
		}
	}

	@Override
	public void preferencesEventFired( final PreferencesEvent preferencesEvent ) {
		final Map<String,String> map = preferencesEvent.getUserdata();
		for ( final String key : map.keySet() ) {
			prefs.setValue( key, map.get( key ) );
		}

		try {
			prefs.save();
			validatePreferences();
		}
		catch ( final Exception e ) {
			final String s = bundle.getString( "exc_cannot_save_prefs", "" + Utils.prettyPrintMap( "", Utils.sanitizeMap( prefs.getValues() ) ) );
			Utils.logException( logger, s, e );
			gui.addMessage( new StatusMessage( s, StatusMessageSeverity.ERROR ) );
		}
	}

	@Override
	public void runItineraryEventFired( final RunItineraryEvent runItineraryEvent ) {
		gui.enableRunFunction( false );
		gui.enablePreferencesFunction( false );

		ISearchRunItinerary itinerary;

		final File itineraryFile = runItineraryEvent.getItineraryFile();

		try {
			final String jsonData = FileUtils.readFileToString( itineraryFile, Charset.defaultCharset() );
			itinerary = searchRunFactory.makeSearchRunItineraryFromJSON( jsonData );
		}
		catch ( final Exception e ) {
			final String s = bundle.getString( "exc_bad_itinerary_file", itineraryFile.getAbsolutePath() );
			Utils.logException( logger, s, e );
			gui.addMessage( new StatusMessage( s, StatusMessageSeverity.ERROR ) );
			gui.enableRunFunction( true );
			gui.enablePreferencesFunction( true );

			return;
		}

		if ( !( itinerary instanceof ISearchRunRepliesItinerary ) ) {
			final String s = bundle.getString( "exc_bad_itinerary_file", itineraryFile.getAbsolutePath() );
			gui.addMessage( new StatusMessage( s, StatusMessageSeverity.ERROR ) );
			gui.enableRunFunction( true );
			gui.enablePreferencesFunction( true );

			return;
		}

		final ItineraryRepliesWorker itineraryWorker = new ItineraryRepliesWorker( (ISearchRunRepliesItinerary) itinerary );

		itineraryWorker.execute();
	}

	@Override
	public void windowClosingEventFired( final WindowClosingEvent windowClosingEvent ) {
		windowClosingEvent.getWindow().dispose();

		logger.info( "DONE" );
		System.exit( 0 );
	}

	private void validatePreferences() {
		if ( Utils.isEmpty( prefs.getValue( "prefs.testing_account_name_private" ) ) ||
				Utils.isEmpty( prefs.getValue( "prefs.testing_account_password_private" ) ) ) {
			gui.addMessage( new StatusMessage( bundle.getString( "prefs_msg_no_user" ), StatusMessageSeverity.WARN ) );
		}

		if ( Utils.isStringTrue( prefs.getValue( "prefs.upload_results" ) ) ) {
			gui.addMessage( new StatusMessage( bundle.getString( "prefs_msg_upload_results" ), StatusMessageSeverity.WARN ) );
		}

		if ( Utils.isStringTrue( prefs.getValue( "prefs.prefs.make_results_public" ) ) ) {
			gui.addMessage( new StatusMessage( bundle.getString( "prefs_msg_make_results_public" ), StatusMessageSeverity.WARN ) );
		}

		if ( Utils.isEmpty( prefs.getValue( "prefs.firefox_path_app" ) ) ||
				Utils.isEmpty( prefs.getValue( "prefs.firefox_path_profile" ) ) ) {
			gui.addMessage( new StatusMessage( bundle.getString( "prefs_msg_no_ffbin" ), StatusMessageSeverity.WARN ) );
		}

		final String handle = prefs.getValue( "prefs.handle_to_check" );
		if ( Utils.isEmpty( handle ) ) {
			gui.addMessage( new StatusMessage( bundle.getString( "prefs_msg_no_handle" ), StatusMessageSeverity.ERROR ) );
			gui.enableRunFunction( false );
			gui.enablePreferencesFunction( true );
		}
		else {
			gui.addMessage( new StatusMessage( bundle.getString( "prefs_msg_handle", handle ), StatusMessageSeverity.INFO ) );
			gui.enableRunFunction( true );
			gui.enablePreferencesFunction( true );
		}
	}

	private void buildGUIElements() throws Exception {
		guiElements = new ArrayList<ElementDescriptor>( 15 );

		guiElements.add( new ElementDescriptor( "textfield", "prefs.handle_to_check",
													bundle.getString( "prefs_element_handle_to_check_name" ),
													bundle.getString( "prefs_element_handle_to_check_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "checkbox", "prefs.skip_login",
													bundle.getString( "prefs_element_skip_login_name" ),
													bundle.getString( "prefs_element_skip_login_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.testing_account_name_private",
													bundle.getString( "prefs_element_testing_account_name" ),
													bundle.getString( "prefs_element_testing_account_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "password", "prefs.testing_account_password_private",
													bundle.getString( "prefs_element_testing_account_password_name" ),
													bundle.getString( "prefs_element_testing_account_password_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.num_tweets_to_check",
													bundle.getString( "prefs_element_num_tweets_to_check_name" ),
													bundle.getString( "prefs_element_num_tweets_to_check_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.num_timeline_pages_to_check",
													bundle.getString( "prefs_element_num_timeline_pages_to_check_name" ),
													bundle.getString( "prefs_element_num_timeline_pages_to_check_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.num_individual_pages_to_check",
													bundle.getString( "prefs_element_num_individual_pages_to_check_name" ),
													bundle.getString( "prefs_element_num_individual_pages_to_check_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.num_timeline_tweets_to_skip",
													bundle.getString( "prefs_element_num_timeline_tweets_to_skip_name" ),
													bundle.getString( "prefs_element_num_timeline_tweets_to_skip_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "checkbox", "prefs.upload_results",
													bundle.getString( "prefs_element_upload_results_name" ),
													bundle.getString( "prefs_element_upload_results_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "checkbox", "prefs.make_results_public",
													bundle.getString( "prefs_element_make_results_public_name" ),
													bundle.getString( "prefs_element_make_results_public_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.user_email",
													bundle.getString( "prefs_element_user_email_name" ),
													bundle.getString( "prefs_element_user_email_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.firefox_path_app",
													bundle.getString( "prefs_element_firefox_path_app_name" ),
													bundle.getString( "prefs_element_firefox_path_app_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.firefox_path_profile",
													bundle.getString( "prefs_element_firefox_path_profile_name" ),
													bundle.getString( "prefs_element_firefox_path_profile_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.firefox_name_profile",
													bundle.getString( "prefs_element_firefox_name_profile_name" ),
													bundle.getString( "prefs_element_firefox_name_profile_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.firefox_screen_position_x",
													bundle.getString( "prefs_element_firefox_screen_position_x_name" ),
													bundle.getString( "prefs_element_firefox_screen_position_x_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.firefox_screen_position_y",
													bundle.getString( "prefs_element_firefox_screen_position_y_name" ),
													bundle.getString( "prefs_element_firefox_screen_position_y_help" ), 30 ) );
	}
}
