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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.statusmessage.*;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.app.reportwriter.*;
import com.tolstoy.censorship.twitter.checker.api.searchrun.*;
import com.tolstoy.censorship.twitter.checker.api.analyzer.*;

public class SearchRunRepliesProcessorWriteReport implements ISearchRunRepliesProcessor {
	private static final Logger logger = LogManager.getLogger( SearchRunRepliesProcessorWriteReport.class );

	private IResourceBundleWithFormatting bundle;
	private IPreferences prefs;
	private IAppDirectories appDirectories;
	private IAnalysisReportFactory analysisReportFactory;
	private boolean debugFlag;

	public SearchRunRepliesProcessorWriteReport( IResourceBundleWithFormatting bundle, IPreferences prefs, IAppDirectories appDirectories,
											IAnalysisReportFactory analysisReportFactory, boolean debugFlag ) {
		this.bundle = bundle;
		this.prefs = prefs;
		this.appDirectories = appDirectories;
		this.analysisReportFactory = analysisReportFactory;
		this.debugFlag = debugFlag;
	}

	@Override
	public ISearchRunReplies process( ISearchRunReplies searchRun, IStatusMessageReceiver statusMessageReceiver ) throws Exception {
		IAnalysisReportRepliesBasic basicReport = analysisReportFactory.createAnalysisReportRepliesBasic( searchRun );

		basicReport.run();

		ReportWriterRepliesBasic reportWriter = new ReportWriterRepliesBasic( prefs, bundle, appDirectories, true );

		reportWriter.writeReport( basicReport );

		statusMessageReceiver.addMessage( new StatusMessage( "Wrote report to " + reportWriter.getFilename(), StatusMessageSeverity.INFO ) );

		return searchRun;
	}

	@Override
	public String getDescription() {
		return bundle.getString( "srp_write_report" );
	}
}
