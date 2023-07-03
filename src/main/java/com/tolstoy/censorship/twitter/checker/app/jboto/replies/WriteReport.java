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
package com.tolstoy.censorship.twitter.checker.app.jboto.replies;

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
import com.tolstoy.jboto.api.IProduct;
import com.tolstoy.jboto.api.IEnvironment;
import com.tolstoy.jboto.api.IBasicCommand;
import com.tolstoy.censorship.twitter.checker.app.jboto.OurEnvironment;
import com.tolstoy.censorship.twitter.checker.app.jboto.timeline.SearchRunTimelineData;

public class WriteReport implements IBasicCommand {
	private static final Logger logger = LogManager.getLogger( WriteReport.class );

	public WriteReport() {
	}

	@Override
	public void run( IProduct prod, IEnvironment env, Object extra, int index ) throws Exception {
		RepliesProduct product = (RepliesProduct) prod;
		OurEnvironment ourEnv = (OurEnvironment) env;
		ITweetRanker tweetRanker;

		tweetRanker = ourEnv.getAnalysisReportFactory().makeTweetRankerJavascript();
		if ( tweetRanker == null ) {
			tweetRanker = ourEnv.getAnalysisReportFactory().makeTweetRankerBasic();
		}

		logger.info( "made ranker=" + tweetRanker );

		ISearchRunReplies searchRun = product.getSearchRun();

		IAnalysisReportRepliesBasic basicRepliesReport;

		basicRepliesReport = ourEnv.getAnalysisReportFactory().makeAnalysisReportRepliesBasic( (ISearchRunReplies) searchRun, tweetRanker );

		basicRepliesReport.run();

		final ReportWriterRepliesBasic reportWriterReplies = new ReportWriterRepliesBasic( ourEnv.getPrefs(), ourEnv.getBundle(), ourEnv.getAppDirectories(), true );

		reportWriterReplies.writeReport( basicRepliesReport );

		ourEnv.getStatusMessageReceiver().addMessage( new StatusMessage( "Wrote report to " + reportWriterReplies.getFilename(), StatusMessageSeverity.INFO ) );
	}
}
