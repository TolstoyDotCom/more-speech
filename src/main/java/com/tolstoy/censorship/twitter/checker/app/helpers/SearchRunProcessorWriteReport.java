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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.installation.DebugLevel;
import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportRepliesBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportTimelineBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.ITweetRanker;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRun;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunProcessor;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunTimeline;
import com.tolstoy.censorship.twitter.checker.app.reportwriter.ReportWriterRepliesBasic;
import com.tolstoy.censorship.twitter.checker.app.reportwriter.ReportWriterTimelineBasic;

public class SearchRunProcessorWriteReport implements ISearchRunProcessor {
	private static final Logger logger = LogManager.getLogger( SearchRunProcessorWriteReport.class );

	private final IResourceBundleWithFormatting bundle;
	private final IPreferences prefs;
	private final IAppDirectories appDirectories;
	private final IAnalysisReportFactory analysisReportFactory;
	private final DebugLevel debugLevel;

	public SearchRunProcessorWriteReport( final IResourceBundleWithFormatting bundle, final IPreferences prefs, final IAppDirectories appDirectories,
													final IAnalysisReportFactory analysisReportFactory, final DebugLevel debugLevel ) {
		this.bundle = bundle;
		this.prefs = prefs;
		this.appDirectories = appDirectories;
		this.analysisReportFactory = analysisReportFactory;
		this.debugLevel = debugLevel;
	}

	@Override
	public ISearchRun process( final ISearchRun searchRun, final IStatusMessageReceiver statusMessageReceiver ) throws Exception {
		ITweetRanker tweetRanker;

		tweetRanker = analysisReportFactory.makeTweetRankerJavascript();
		if ( tweetRanker == null ) {
			tweetRanker = analysisReportFactory.makeTweetRankerBasic();
		}

		logger.info( "SearchRunProcessorWriteReport: made ranker=" + tweetRanker );

		if ( searchRun instanceof ISearchRunReplies ) {
			IAnalysisReportRepliesBasic basicRepliesReport;

			basicRepliesReport = analysisReportFactory.makeAnalysisReportRepliesBasic( (ISearchRunReplies) searchRun, tweetRanker );

			basicRepliesReport.run();

			final ReportWriterRepliesBasic reportWriterReplies = new ReportWriterRepliesBasic( prefs, bundle, appDirectories, true );

			reportWriterReplies.writeReport( basicRepliesReport );

			statusMessageReceiver.addMessage( new StatusMessage( "Wrote report to " + reportWriterReplies.getFilename(), StatusMessageSeverity.INFO ) );
		}
		else if ( searchRun instanceof ISearchRunTimeline ) {
			IAnalysisReportTimelineBasic basicTimelineReport;

			logger.info( "SearchRunProcessorWriteReport: about to makeAnalysisReportTimelineBasic" );

			basicTimelineReport = analysisReportFactory.makeAnalysisReportTimelineBasic( (ISearchRunTimeline) searchRun, tweetRanker );

			logger.info( "SearchRunProcessorWriteReport: did makeAnalysisReportTimelineBasic, calling run" );

			basicTimelineReport.run();

			logger.info( "SearchRunProcessorWriteReport: did makeAnalysisReportTimelineBasic, called run" );

			final ReportWriterTimelineBasic reportWriterTimeline = new ReportWriterTimelineBasic( prefs, bundle, appDirectories, true );

			reportWriterTimeline.writeReport( basicTimelineReport );

			statusMessageReceiver.addMessage( new StatusMessage( "Wrote report to " + reportWriterTimeline.getFilename(), StatusMessageSeverity.INFO ) );
		}

		return searchRun;
	}

	@Override
	public String getDescription() {
		return bundle.getString( "srp_write_report" );
	}
}
