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
import java.text.Format;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang.time.FastDateFormat;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.analyzer.AnalysisReportItemBasicTweetStatus;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportRepliesBasic;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportRepliesItemBasic;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;

//	TODO: escaping: https://github.com/jtwig/jtwig/issues/331

public class ReportWriterRepliesBasic {
	private static final Logger logger = LogManager.getLogger( ReportWriterRepliesBasic.class );

	private static final Format filenameDateFormat = FastDateFormat.getInstance( "yyyy_MM_dd_hh_mm_ss" );

	private final IResourceBundleWithFormatting bundle;
	private final IPreferences prefs;
	private final IAppDirectories appDirectories;
	private final JtwigTemplate layoutTemplate, helpAreaTemplate, tableTemplate,
							tweetSourceTemplate, tweetReplyTemplate, rankTemplate, statusTemplate;
	private final Format tweetDateFormat;
	private String filename;
	private final boolean debugMode;

	public ReportWriterRepliesBasic( final IPreferences prefs, final IResourceBundleWithFormatting bundle, final IAppDirectories appDirectories, final boolean debugMode ) throws Exception {
		this.prefs = prefs;
		this.bundle = bundle;
		this.appDirectories = appDirectories;
		this.debugMode = debugMode;

		this.filename = "";
		this.layoutTemplate = JtwigTemplate.classpathTemplate( "templates/reportbasic_layout.twig" );
		this.helpAreaTemplate = JtwigTemplate.classpathTemplate( "templates/reportrepliesbasic_helparea.twig" );
		this.tableTemplate = JtwigTemplate.classpathTemplate( "templates/reportrepliesbasic_table.twig" );
		this.tweetSourceTemplate = JtwigTemplate.classpathTemplate( "templates/reportrepliesbasic_element_tweetsource.twig" );
		this.tweetReplyTemplate = JtwigTemplate.classpathTemplate( "templates/reportrepliesbasic_element_tweetreply.twig" );
		this.rankTemplate = JtwigTemplate.classpathTemplate( "templates/reportrepliesbasic_element_rank.twig" );
		this.statusTemplate = JtwigTemplate.classpathTemplate( "templates/reportrepliesbasic_element_status.twig" );

		this.tweetDateFormat = FastDateFormat.getInstance( bundle.getString( "rpt_tweet_dateformat" ) );
	}

	public void writeReport( final IAnalysisReportRepliesBasic report ) throws Exception {
		String htmlItems = "";

		final List<IAnalysisReportRepliesItemBasic> items = report.getItems();

		for ( final IAnalysisReportRepliesItemBasic item : items ) {
			htmlItems += makeItemHTML( item );
		}

		final boolean bLoggedIn = Utils.isStringTrue( report.getSearchRun().getAttribute( "loggedin" ) );

		final JtwigModel model = JtwigModel.newModel()
			.with( "reporttype", "replies/" + report.getAnalysisType() )
			.with( "reporttitle", report.getName() )
			.with( "helparea", makeHelpArea( report ) )
			.with( "loggedin", bLoggedIn )
			.with( "content", htmlItems );

		filename = String.format( "report_%s_%s_%s.html", report.getSearchRun().getInitiatingUser().getHandle(),
															filenameDateFormat.format( new Date() ),
															( bLoggedIn ? "li" : "nli" ) );

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

	protected String makeItemHTML( final IAnalysisReportRepliesItemBasic item ) {
		String debugData = "";

		if ( debugMode ) {
			final Map<String,String> attributes = item.getAttributes();
			final Set<String> keys = attributes.keySet();
			for ( final String key : keys ) {
				debugData += key + "=" + attributes.get( key ) + "\n\n";
			}
		}

		final JtwigModel model = JtwigModel.newModel()
			.with( "debugData", debugData )
			.with( "replytweet", makeTweetReplyElement( item.getRepliedToTweet() ) )
			.with( "sourcetweet", makeTweetSourceElement( item.getSourceTweet() ) )
			.with( "status", makeStatusElement( item.getTweetStatus() ) )
			.with( "rank", makeRankElement( item.getRank(), item.getTotalReplies(), item.getTotalRepliesActual(), item.getListIsComplete() ) )
			.with( "rank_by_interaction", item.getExpectedRankByInteraction() )
			.with( "rank_by_date", item.getExpectedRankByDate() );

		return tableTemplate.render( model );
	}

	protected String makeRankElement( final int rank, final int totalReplies, final int totalRepliesActual, final boolean isComplete ) {
		final JtwigModel model = JtwigModel.newModel()
			.with( "rank", rank )
			.with( "totalReplies", totalReplies )
			.with( "totalRepliesActual", totalRepliesActual )
			.with( "isComplete", isComplete );

		return rankTemplate.render( model );
	}

	protected String makeTweetReplyElement( final ITweet tweet ) {
		final JtwigModel model = getTweetParams( tweet );

		return tweetReplyTemplate.render( model );
	}

	protected String makeTweetSourceElement( final ITweet tweet ) {
		final JtwigModel model = getTweetParams( tweet );

		return tweetSourceTemplate.render( model );
	}

	protected String makeStatusElement( final AnalysisReportItemBasicTweetStatus status ) {
		final JtwigModel model = JtwigModel.newModel()
			.with( "statusName", bundle.getString( status.getKey() ) )
			.with( "statusColor", bundle.getString( status.getKey() + "_color" ) );

		return statusTemplate.render( model );
	}

	protected String makeHelpArea( final IAnalysisReportRepliesBasic report ) {
		final JtwigModel model = JtwigModel.newModel()
			.with( "initiating_user", report.getSearchRun().getInitiatingUser().getHandle() )
			.with( "ranking_function_name", report.getAttributes().get( "rankingFunctionName" ) );

		return helpAreaTemplate.render( model );
	}

	protected JtwigModel getTweetParams( final ITweet tweet ) {
		return JtwigModel.newModel()
			.with( "handle", tweet.getUser().getHandle() )
			.with( "link", String.format( prefs.getValue( "targetsite.pattern.individual" ), tweet.getUser().getHandle(), tweet.getID() ) )
			.with( "dateStr", Utils.formatTimestampString( tweet.getAttribute( "time" ), "date unknown" ) )
			.with( "text", Utils.removeNewlines( Utils.trimDefault( Utils.removeAllEmojis( tweet.getAttribute( "tweettext" ) ) ) ) );
	}
}
