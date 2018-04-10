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

public class SearchRunProcessorWriteReport implements ISearchRunProcessor {
	private static final Logger logger = LogManager.getLogger( SearchRunProcessorWriteReport.class );

	private IResourceBundleWithFormatting bundle;
	private IPreferences prefs;
	private IAppDirectories appDirectories;
	private IAnalysisReportFactory analysisReportFactory;
	private boolean debugFlag;

	public SearchRunProcessorWriteReport( IResourceBundleWithFormatting bundle, IPreferences prefs, IAppDirectories appDirectories,
													IAnalysisReportFactory analysisReportFactory, boolean debugFlag ) {
		this.bundle = bundle;
		this.prefs = prefs;
		this.appDirectories = appDirectories;
		this.analysisReportFactory = analysisReportFactory;
		this.debugFlag = debugFlag;
	}

	@Override
	public ISearchRun process( ISearchRun searchRun, IStatusMessageReceiver statusMessageReceiver ) throws Exception {
		if ( searchRun instanceof ISearchRunReplies ) {
			IAnalysisReportRepliesBasic basicRepliesReport = analysisReportFactory.createAnalysisReportRepliesBasic( (ISearchRunReplies) searchRun );

			basicRepliesReport.run();

			ReportWriterRepliesBasic reportWriterReplies = new ReportWriterRepliesBasic( prefs, bundle, appDirectories, true );

			reportWriterReplies.writeReport( basicRepliesReport );

			statusMessageReceiver.addMessage( new StatusMessage( "Wrote report to " + reportWriterReplies.getFilename(), StatusMessageSeverity.INFO ) );
		}

		return searchRun;
	}

	@Override
	public String getDescription() {
		return bundle.getString( "srp_write_report" );
	}
}
