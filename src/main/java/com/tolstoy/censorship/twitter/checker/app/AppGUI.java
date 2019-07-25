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

import java.util.*;
import java.io.File;
import javax.swing.*;
import java.time.Instant;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.storage.*;
import com.tolstoy.basic.api.tweet.*;
import com.tolstoy.basic.api.utils.*;
import com.tolstoy.basic.api.statusmessage.*;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.preferences.*;
import com.tolstoy.censorship.twitter.checker.api.webdriver.*;
import com.tolstoy.censorship.twitter.checker.api.snapshot.*;
import com.tolstoy.censorship.twitter.checker.api.searchrun.*;
import com.tolstoy.censorship.twitter.checker.app.gui.*;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunRepliesBuilder;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunTimelineBuilder;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunRepliesFromItineraryBuilder;
import com.tolstoy.censorship.twitter.checker.app.helpers.SearchRunProcessorWriteReport;
import com.tolstoy.censorship.twitter.checker.app.helpers.IAppDirectories;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.app.storage.StorageTable;
import com.tolstoy.basic.gui.ElementDescriptor;

public class AppGUI implements RunEventListener, PreferencesEventListener, RunItineraryEventListener, WindowClosingEventListener {
	private static final Logger logger = LogManager.getLogger( AppGUI.class );

	private IResourceBundleWithFormatting bundle;
	private IStorage storage;
	private IPreferencesFactory prefsFactory;
	private IPreferences prefs;
	private IWebDriverFactory webDriverFactory;
	private ISearchRunFactory searchRunFactory;
	private ISnapshotFactory snapshotFactory;
	private ITweetFactory tweetFactory;
	private IAnalysisReportFactory analysisReportFactory;
	private IAppDirectories appDirectories;
	private List<ISearchRunProcessor> searchRunProcessors;
	private List<ElementDescriptor> guiElements;
	private MainGUI gui;

	abstract class WorkerBase<T> extends SwingWorker<T, StatusMessage> implements IStatusMessageReceiver {
		@Override
		protected void process( List<StatusMessage> messages ) {
			for ( StatusMessage message : messages ) {
				if ( message == null ) {
					gui.clearMessages();
				}
				else {
					gui.addMessage( message );
				}
			}
		}

		@Override
		public void addMessage( StatusMessage message ) {
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
				T searchRun = get();
				//logger.info( searchRun );

				if ( searchRun != null ) {
					for ( ISearchRunProcessor processor : searchRunProcessors ) {
						try {
							processor.process( searchRun, this );
						}
						catch ( Exception e ) {
							String s = bundle.getString( "exc_srp", processor.getDescription(), e.getMessage() );
							logger.error( s, e );
							gui.addMessage( new StatusMessage( s, StatusMessageSeverity.ERROR ) );
						}
					}
				}
			}
			catch ( Exception e ) {
				String s = bundle.getString( "exc_getresults", e.getMessage() );
				logger.error( s, e );
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
				SearchRunRepliesBuilder builder = new SearchRunRepliesBuilder( bundle,
												storage,
												prefsFactory,
												prefs,
												webDriverFactory,
												searchRunFactory,
												snapshotFactory,
												tweetFactory,
												this,
												prefs.getValue( "prefs.handle_to_check" ) );

				int numTimelinePagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_timeline_pages_to_check" ), 1 );
				int numIndividualPagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_individual_pages_to_check" ), 3 );
				int maxTweets = Utils.parseIntDefault( prefs.getValue( "prefs.num_tweets_to_check" ), 5 );

				ISearchRunReplies searchRunReplies = builder.buildSearchRunReplies( numTimelinePagesToCheck,
																					numIndividualPagesToCheck,
																					maxTweets );

				//logger.info( searchRunReplies );
				logger.info( "VALUENEXT" );
				logger.info( Utils.getDefaultObjectMapper().writeValueAsString( searchRunReplies ) );
				return searchRunReplies;
			}
			catch ( Exception e ) {
				logger.error( bundle.getString( "exc_start", e.getMessage() ), e );
				publish( new StatusMessage( bundle.getString( "exc_start", e.getMessage() ), StatusMessageSeverity.ERROR ) );

				return null;
			}
		}
	}

	class TimelineWorker extends WorkerProcessingBase<ISearchRunTimeline> {
		@Override
		public ISearchRunTimeline doInBackground() {
			try {
				SearchRunTimelineBuilder builder = new SearchRunTimelineBuilder( bundle,
																					storage,
																					prefsFactory,
																					prefs,
																					webDriverFactory,
																					searchRunFactory,
																					snapshotFactory,
																					tweetFactory,
																					this,
																					prefs.getValue( "prefs.handle_to_check" ) );

				int numTimelinePagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_timeline_pages_to_check" ), 1 );
				int numIndividualPagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_individual_pages_to_check" ), 3 );
				int maxTweets = Utils.parseIntDefault( prefs.getValue( "prefs.num_tweets_to_check" ), 5 );

				ISearchRunTimeline searchRunTimeline = builder.buildSearchRunTimeline( numTimelinePagesToCheck,
																						numIndividualPagesToCheck,
																						maxTweets );

				//logger.info( searchRunTimeline );
				logger.info( "VALUENEXT" );
				logger.info( Utils.getDefaultObjectMapper().writeValueAsString( searchRunTimeline ) );
				return searchRunTimeline;
			}
			catch ( Exception e ) {
				logger.error( bundle.getString( "exc_start", e.getMessage() ), e );
				publish( new StatusMessage( bundle.getString( "exc_start", e.getMessage() ), StatusMessageSeverity.ERROR ) );

				return null;
			}
		}
	}

	class ItineraryRepliesWorker extends WorkerProcessingBase<ISearchRunReplies> {
		private ISearchRunRepliesItinerary itinerary;

		public ItineraryRepliesWorker( ISearchRunRepliesItinerary itinerary ) {
			this.itinerary = itinerary;
		}

		@Override
		public ISearchRunReplies doInBackground() {
			try {
				SearchRunRepliesFromItineraryBuilder builder = new SearchRunRepliesFromItineraryBuilder( bundle,
																storage,
																prefsFactory,
																prefs,
																webDriverFactory,
																searchRunFactory,
																snapshotFactory,
																tweetFactory,
																this,
																itinerary );

				int numTimelinePagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_timeline_pages_to_check" ), 1 );
				int numIndividualPagesToCheck = Utils.parseIntDefault( prefs.getValue( "prefs.num_individual_pages_to_check" ), 3 );
				int maxTweets = Utils.parseIntDefault( prefs.getValue( "prefs.num_tweets_to_check" ), 5 );

				ISearchRunReplies searchRunReplies = builder.buildSearchRunReplies( numTimelinePagesToCheck,
																					numIndividualPagesToCheck,
																					maxTweets );

				//logger.info( searchRunReplies );
				logger.info( "VALUENEXT" );
				logger.info( Utils.getDefaultObjectMapper().writeValueAsString( searchRunReplies ) );
				return searchRunReplies;
			}
			catch ( Exception e ) {
				logger.error( bundle.getString( "exc_start", e.getMessage() ), e );
				publish( new StatusMessage( bundle.getString( "exc_start", e.getMessage() ), StatusMessageSeverity.ERROR ) );

				return null;
			}
		}
	}

	class RewriteWorker<Void> extends WorkerBase<Void> implements IStatusMessageReceiver {
		@Override
		public Void doInBackground() {
			try {
				List<IStorable> storables = storage.getRecords( StorageTable.SEARCHRUN, StorageOrdering.DESC, 1 );
				if ( storables.size() > 0 ) {
					IStorable storable = storables.get( 0 );
					ISearchRun searchRun = (ISearchRun) storable;

					SearchRunProcessorWriteReport writeReport = new SearchRunProcessorWriteReport( bundle, prefs, appDirectories,
																									analysisReportFactory, true );

					writeReport.process( searchRun, this );
				}
			}
			catch ( Exception e ) {
				logger.error( bundle.getString( "exc_start", e.getMessage() ), e );
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

	public AppGUI( IResourceBundleWithFormatting bundle,
					IStorage storage,
					IPreferencesFactory prefsFactory,
					IPreferences prefs,
					IWebDriverFactory webDriverFactory,
					ISearchRunFactory searchRunFactory,
					ISnapshotFactory snapshotFactory,
					ITweetFactory tweetFactory,
					IAnalysisReportFactory analysisReportFactory,
					IAppDirectories appDirectories,
					List<ISearchRunProcessor> searchRunProcessors ) throws Exception {
		this.bundle = bundle;
		this.storage = storage;
		this.prefsFactory = prefsFactory;
		this.prefs = prefs;
		this.webDriverFactory = webDriverFactory;
		this.searchRunFactory = searchRunFactory;
		this.snapshotFactory = snapshotFactory;
		this.tweetFactory = tweetFactory;
		this.analysisReportFactory = analysisReportFactory;
		this.appDirectories = appDirectories;
		this.searchRunProcessors = searchRunProcessors;
	}

	public void run() throws Exception {
		try {
			//UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			UIManager.setLookAndFeel( "javax.swing.plaf.nimbus.NimbusLookAndFeel" );
			//UIManager.setLookAndFeel( "com.pagosoft.plaf.PgsLookAndFeel" );
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}

		buildGUIElements();

		gui = new MainGUI( bundle, prefs, guiElements );

		gui.addRunEventListener( this );
		gui.addPreferencesEventListener( this );
		gui.addRunItineraryEventListener( this );
		gui.addWindowClosingEventListener( this );

		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				gui.showGUI();
				validatePreferences();
			}
		});
	}

	@Override
	public void runEventFired( RunEvent runEvent ) {
		gui.enableRunFunction( false );
		gui.enablePreferencesFunction( false );

		if ( MainGUI.ACTION_REPLIES.equals( runEvent.getActionName() ) ) {
			RepliesWorker repliesWorker = new RepliesWorker();

			repliesWorker.execute();
		}
		else if ( MainGUI.ACTION_TIMELINE.equals( runEvent.getActionName() ) ) {
			TimelineWorker timelineWorker = new TimelineWorker();

			timelineWorker.execute();
		}
		else if ( MainGUI.ACTION_REWRITE_LAST_REPORT.equals( runEvent.getActionName() ) ) {
			RewriteWorker rewriteWorker = new RewriteWorker();

			rewriteWorker.execute();
		}
	}

	@Override
	public void preferencesEventFired( PreferencesEvent preferencesEvent ) {
		Map<String,String> map = preferencesEvent.getUserdata();
		for ( String key : map.keySet() ) {
			prefs.setValue( key, map.get( key ) );
		}

		try {
			prefs.save();
			validatePreferences();
		}
		catch ( Exception e ) {
			String s = bundle.getString( "exc_cannot_save_prefs", "" + Utils.sanitizeMap( prefs.getValues() ) );
			logger.error( s, e );
			gui.addMessage( new StatusMessage( s, StatusMessageSeverity.ERROR ) );
		}
	}

	@Override
	public void runItineraryEventFired( RunItineraryEvent runItineraryEvent ) {
		gui.enableRunFunction( false );
		gui.enablePreferencesFunction( false );

		ISearchRunItinerary itinerary;

		File itineraryFile = runItineraryEvent.getItineraryFile();

		try {
			String jsonData = FileUtils.readFileToString( itineraryFile, Charset.defaultCharset() );
			itinerary = searchRunFactory.makeSearchRunItineraryFromJSON( jsonData );
		}
		catch ( Exception e ) {
			String s = bundle.getString( "exc_bad_itinerary_file", itineraryFile.getAbsolutePath() );
			logger.error( s, e );
			gui.addMessage( new StatusMessage( s, StatusMessageSeverity.ERROR ) );
			gui.enableRunFunction( true );
			gui.enablePreferencesFunction( true );

			return;
		}

		if ( !( itinerary instanceof ISearchRunRepliesItinerary ) ) {
			String s = bundle.getString( "exc_bad_itinerary_file", itineraryFile.getAbsolutePath() );
			gui.addMessage( new StatusMessage( s, StatusMessageSeverity.ERROR ) );
			gui.enableRunFunction( true );
			gui.enablePreferencesFunction( true );

			return;
		}

		ItineraryRepliesWorker itineraryWorker = new ItineraryRepliesWorker( (ISearchRunRepliesItinerary) itinerary );

		itineraryWorker.execute();
	}

	@Override
	public void windowClosingEventFired( WindowClosingEvent windowClosingEvent ) {
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

		String handle = prefs.getValue( "prefs.handle_to_check" );
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
		guiElements.add( new ElementDescriptor( "textfield", "prefs.firefox_screen_position_x",
													bundle.getString( "prefs_element_firefox_screen_position_x_name" ),
													bundle.getString( "prefs_element_firefox_screen_position_x_help" ), 30 ) );
		guiElements.add( new ElementDescriptor( "textfield", "prefs.firefox_screen_position_y",
													bundle.getString( "prefs_element_firefox_screen_position_y_name" ),
													bundle.getString( "prefs_element_firefox_screen_position_y_help" ), 30 ) );
	}
}

