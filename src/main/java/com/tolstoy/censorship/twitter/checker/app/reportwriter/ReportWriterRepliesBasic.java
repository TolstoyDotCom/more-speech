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
package com.tolstoy.censorship.twitter.checker.app.reportwriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.tweet.*;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.analyzer.*;
import com.tolstoy.censorship.twitter.checker.app.helpers.IAppDirectories;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ReplyThreadType;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

//	TODO: escaping: https://github.com/jtwig/jtwig/issues/331

public class ReportWriterRepliesBasic {
	private static final Logger logger = LogManager.getLogger( ReportWriterRepliesBasic.class );

	private static final DateFormat filenameDateFormat = new SimpleDateFormat( "yyyy_MM_dd_hh_mm_ss" );

	private IResourceBundleWithFormatting bundle;
	private IPreferences prefs;
	private IAppDirectories appDirectories;
	private JtwigTemplate layoutTemplate, tableTemplate, tweetOriginalTemplate, tweetReplyTemplate, rankTemplate, statusTemplate;
	private DateFormat tweetDateFormat;
	private String filename;
	private boolean debugMode;

	public ReportWriterRepliesBasic( IPreferences prefs, IResourceBundleWithFormatting bundle, IAppDirectories appDirectories, boolean debugMode ) throws Exception {
		this.prefs = prefs;
		this.bundle = bundle;
		this.appDirectories = appDirectories;
		this.debugMode = debugMode;

		this.filename = "";
		this.layoutTemplate = JtwigTemplate.classpathTemplate( "templates/reportbasic_layout.twig" );
		this.tableTemplate = JtwigTemplate.classpathTemplate( "templates/reportbasic_table.twig" );
		this.tweetOriginalTemplate = JtwigTemplate.classpathTemplate( "templates/reportbasic_element_tweetoriginal.twig" );
		this.tweetReplyTemplate = JtwigTemplate.classpathTemplate( "templates/reportbasic_element_tweetreply.twig" );
		this.rankTemplate = JtwigTemplate.classpathTemplate( "templates/reportbasic_element_rank.twig" );
		this.statusTemplate = JtwigTemplate.classpathTemplate( "templates/reportbasic_element_status.twig" );

		this.tweetDateFormat = new SimpleDateFormat( bundle.getString( "rpt_tweet_dateformat" ) );
	}

	public void writeReport( IAnalysisReportRepliesBasic report ) throws Exception {
		String htmlItems = "";

		List<IAnalysisReportRepliesItemBasic> items = report.getItems();

		for ( IAnalysisReportRepliesItemBasic item : items ) {
			htmlItems += makeItemHTML( item );
		}

		boolean bLoggedIn = Utils.isStringTrue( report.getSearchRun().getAttribute( "loggedin" ) );

		JtwigModel model = JtwigModel.newModel()
			.with( "reporttitle", report.getName() )
			.with( "loggedin", bLoggedIn )
			.with( "content", htmlItems );

		filename = String.format( "report_%s_%s_%s.html", report.getSearchRun().getInitiatingUser().getHandle(),
															filenameDateFormat.format( new Date() ),
															( bLoggedIn ? "LI" : "NLI" ) );

		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream( new File( appDirectories.getReportsDirectory(), filename ) );
			layoutTemplate.render( model, fos );
		}
		finally {
			if ( fos != null ) {
				fos.close();
			}
		}
	}

	public String getFilename() {
		return filename;
	}

	protected String makeItemHTML( IAnalysisReportRepliesItemBasic item ) {
		String debugData = "";

		if ( debugMode ) {
			Map<String,String> attributes = item.getAttributes();
			Set<String> keys = attributes.keySet();
			for ( String key : keys ) {
				debugData += key + "=" + attributes.get( key ) + "\n\n";
			}
		}

		JtwigModel model = JtwigModel.newModel()
			.with( "debugData", debugData )
			.with( "replytweet", makeTweetReplyElement( item.getRepliedToTweet() ) )
			.with( "sourcetweet", makeTweetSourceElement( item.getSourceTweet() ) )
			.with( "status", makeStatusElement( item.getTweetStatus() ) )
			.with( "rank", makeRankElement( item.getRank(), item.getTotalReplies(), item.getListIsComplete() ) )
			.with( "rank_by_interaction", item.getExpectedRankByInteraction() )
			.with( "rank_by_date", item.getExpectedRankByDate() );

		return tableTemplate.render( model );
	}

	protected String makeRankElement( int rank, int totalReplies, boolean isComplete ) {
		JtwigModel model = JtwigModel.newModel()
			.with( "rank", rank )
			.with( "totalReplies", totalReplies )
			.with( "isComplete", isComplete );

		return rankTemplate.render( model );
	}

	protected String makeTweetReplyElement( ITweet tweet ) {
		JtwigModel model = getTweetParams( tweet );

		return tweetReplyTemplate.render( model );
	}

	protected String makeTweetSourceElement( ITweet tweet ) {
		JtwigModel model = getTweetParams( tweet );

		return tweetOriginalTemplate.render( model );
	}

	protected String makeStatusElement( AnalysisReportItemBasicTweetStatus status ) {
		JtwigModel model = JtwigModel.newModel()
			.with( "statusName", bundle.getString( status.getKey() ) )
			.with( "statusColor", bundle.getString( status.getKey() + "_color" ) );

		return statusTemplate.render( model );
	}

	protected JtwigModel getTweetParams( ITweet tweet ) {
		return JtwigModel.newModel()
			.with( "handle", tweet.getUser().getHandle() )
			.with( "link", String.format( prefs.getValue( "targetsite.pattern.individual" ), tweet.getUser().getHandle(), tweet.getID() ) )
			.with( "dateStr", Utils.formatTimestampString( tweet.getAttribute( "time" ), "date unknown" ) )
			.with( "text", Utils.removeNewlines( Utils.trimDefault( Utils.removeAllEmojis( tweet.getAttribute( "tweettext" ) ) ) ) );
	}
}
