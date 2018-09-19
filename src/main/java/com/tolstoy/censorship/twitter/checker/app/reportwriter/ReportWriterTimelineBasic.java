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
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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

public class ReportWriterTimelineBasic {
	private static final Logger logger = LogManager.getLogger( ReportWriterTimelineBasic.class );

	private static final DateFormat filenameDateFormat = new SimpleDateFormat( "yyyy_MM_dd_hh_mm_ss" );

	private IResourceBundleWithFormatting bundle;
	private IPreferences prefs;
	private IAppDirectories appDirectories;
	private JtwigTemplate layoutTemplate, helpAreaTemplate, tableTemplate,
							tweetSourceTemplate, tweetReplyTemplate, numRepliesTemplate, statusTemplate;
	private DateFormat tweetDateFormat;
	private String filename;
	private boolean debugMode;

	public ReportWriterTimelineBasic( IPreferences prefs, IResourceBundleWithFormatting bundle, IAppDirectories appDirectories, boolean debugMode ) throws Exception {
		this.prefs = prefs;
		this.bundle = bundle;
		this.appDirectories = appDirectories;
		this.debugMode = debugMode;

		this.filename = "";
		this.layoutTemplate = JtwigTemplate.classpathTemplate( "templates/reportbasic_layout.twig" );
		this.helpAreaTemplate = JtwigTemplate.classpathTemplate( "templates/reporttimelinebasic_helparea.twig" );
		this.tableTemplate = JtwigTemplate.classpathTemplate( "templates/reporttimelinebasic_table.twig" );
		this.tweetSourceTemplate = JtwigTemplate.classpathTemplate( "templates/reporttimelinebasic_element_tweetsource.twig" );
		this.tweetReplyTemplate = JtwigTemplate.classpathTemplate( "templates/reporttimelinebasic_element_tweetreply.twig" );
		this.numRepliesTemplate = JtwigTemplate.classpathTemplate( "templates/reporttimelinebasic_element_numreplies.twig" );
		this.statusTemplate = JtwigTemplate.classpathTemplate( "templates/reporttimelinebasic_element_status.twig" );

		this.tweetDateFormat = new SimpleDateFormat( bundle.getString( "rpt_tweet_dateformat" ) );
	}

	public void writeReport( IAnalysisReportTimelineBasic report ) throws Exception {
		String htmlItems = "";

		List<IAnalysisReportTimelineItemBasic> items = report.getItems();

		for ( IAnalysisReportTimelineItemBasic item : items ) {
			htmlItems += makeItemHTML( item );
		}

		boolean bLoggedIn = Utils.isStringTrue( report.getSearchRun().getAttribute( "loggedin" ) );

		JtwigModel model = JtwigModel.newModel()
			.with( "reporttype", "timeline/" + report.getAnalysisType() )
			.with( "reporttitle", report.getName() )
			.with( "helparea", makeHelpArea( report ) )
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

	protected List<String> makeTweetList( List<IAnalyzedTweet> list ) {
		List<String> ret = new ArrayList<String>();
		for ( IAnalyzedTweet analyzedTweet : list ) {
			ret.add( makeTweetReplyElement( analyzedTweet ) );
		}

		return ret;
	}

	protected String makeItemHTML( IAnalysisReportTimelineItemBasic item ) throws Exception {
		String debugData = "";

		if ( debugMode ) {
			Map<String,String> attributes = item.getAttributes();
			Set<String> keys = attributes.keySet();
			for ( String key : keys ) {
				debugData += key + "=" + attributes.get( key ) + "\n\n";
			}
		}

		Map<String,String> summaryMap = new HashMap<String,String>( 10 );

		summaryMap.put( "source_tweet_handle", item.getSourceTweet().getUser().getHandle() );
		summaryMap.put( "source_tweet_id", "" + item.getSourceTweet().getID() );
		summaryMap.put( "status", item.getTimelineRepliesStatus().getKey() );
		summaryMap.put( "num_replies", "" + item.getTotalReplies() );
		summaryMap.put( "num_replies_actual", "" + item.getTotalRepliesActual() );
		summaryMap.put( "num_replies_is_complete", ( item.getListIsComplete() ? "1" : "0" ) );
		summaryMap.put( "num_suppressed", "" + item.getNumSuppressed() );
		summaryMap.put( "num_hidden", "" + item.getNumHidden() );
		summaryMap.put( "num_anomalous_elevated_tweets", "" + item.getAnomalousElevatedTweets().size() );
		summaryMap.put( "num_anomalous_suppressed_tweets", "" + item.getAnomalousSuppressedOrHiddenTweets().size() );

		JtwigModel model = JtwigModel.newModel()
			.with( "jsonData", Utils.getPlainObjectMapper().writeValueAsString( summaryMap ) )
			.with( "debugData", debugData )
			.with( "sourcetweet", makeTweetSourceElement( item.getSourceTweet() ) )
			.with( "status", makeStatusElement( item.getTimelineRepliesStatus().getKey() ) )
			.with( "num_replies", makeNumRepliesElement( item.getTotalReplies(), item.getTotalRepliesActual(), item.getListIsComplete() ) )
			.with( "percent_available", Utils.makePercentInt( item.getTotalRepliesActual(), item.getTotalReplies() ) )
			.with( "percent_suppressed", Utils.makePercentInt( item.getNumSuppressed(), item.getTotalRepliesActual() ) )
			.with( "percent_hidden", Utils.makePercentInt( item.getNumHidden(), item.getTotalRepliesActual() ) )
			.with( "anomalous_elevated_tweets", makeTweetList( item.getAnomalousElevatedTweets() ) )
			.with( "anomalous_suppressed_tweets", makeTweetList( item.getAnomalousSuppressedOrHiddenTweets() ) )
			.with( "hidden_tweets", makeTweetList( item.getHiddenTweets() ) );

		return tableTemplate.render( model );
	}

	protected String makeNumRepliesElement( int totalReplies, int totalRepliesActual, boolean isComplete ) {
		JtwigModel model = JtwigModel.newModel()
			.with( "totalReplies", totalReplies )
			.with( "totalRepliesActual", totalRepliesActual )
			.with( "isComplete", isComplete );

		return numRepliesTemplate.render( model );
	}

	protected String makeTweetReplyElement( IAnalyzedTweet analyzedTweet ) {
		ITweet tweet = analyzedTweet.getTweet();

		JtwigModel model = JtwigModel.newModel()
			.with( "is_hidden", analyzedTweet.getTweet().getSupposedQuality().getCensored() )
			.with( "supposedquality", analyzedTweet.getTweet().getSupposedQuality().getKey() )
			.with( "ranking", analyzedTweet.getRanking() )
			.with( "original_order", analyzedTweet.getOriginalOrder() )
			.with( "date_order", analyzedTweet.getDateOrder() )
			.with( "ranking_order", analyzedTweet.getRankingOrder() )
			.with( "handle", tweet.getUser().getHandle() )
			.with( "link", String.format( prefs.getValue( "targetsite.pattern.individual" ), tweet.getUser().getHandle(), tweet.getID() ) )
			.with( "dateStr", Utils.formatTimestampString( tweet.getAttribute( "time" ), "date unknown" ) )
			.with( "text", Utils.removeNewlines( Utils.trimDefault( Utils.removeAllEmojis( tweet.getAttribute( "tweettext" ) ) ) ) );

		return tweetReplyTemplate.render( model );
	}

	protected String makeTweetSourceElement( ITweet tweet ) {
		JtwigModel model = getTweetParams( tweet );

		return tweetSourceTemplate.render( model );
	}

	protected String makeStatusElement( String key ) {
		JtwigModel model = JtwigModel.newModel()
			.with( "statusName", bundle.getString( key ) )
			.with( "statusColor", bundle.getString( key + "_color" ) );

		return statusTemplate.render( model );
	}

	protected String makeHelpArea( IAnalysisReportTimelineBasic report ) {
		JtwigModel model = JtwigModel.newModel()
			.with( "initiating_user", report.getSearchRun().getInitiatingUser().getHandle() )
			.with( "ranking_function_name", report.getAttributes().get( "rankingFunctionName" ) );

		return helpAreaTemplate.render( model );
	}

	protected JtwigModel getTweetParams( ITweet tweet ) {
		return JtwigModel.newModel()
			.with( "handle", tweet.getUser().getHandle() )
			.with( "link", String.format( prefs.getValue( "targetsite.pattern.individual" ), tweet.getUser().getHandle(), tweet.getID() ) )
			.with( "dateStr", Utils.formatTimestampString( tweet.getAttribute( "time" ), "date unknown" ) )
			.with( "text", Utils.removeNewlines( Utils.trimDefault( Utils.removeAllEmojis( tweet.getAttribute( "tweettext" ) ) ) ) );
	}
}
