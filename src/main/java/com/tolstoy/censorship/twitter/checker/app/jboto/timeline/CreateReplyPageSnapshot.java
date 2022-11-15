/*
 * Copyright 2022 Chris Kelly
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
package com.tolstoy.censorship.twitter.checker.app.jboto.timeline;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.Point;

import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeMetadata;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.jboto.api.framework.IFrameworkFactory;
import com.tolstoy.jboto.api.framework.IFramework;
import com.tolstoy.jboto.app.framework.FrameworkFactory;
import com.tolstoy.jboto.api.IProduct;
import com.tolstoy.jboto.api.IEnvironment;
import com.tolstoy.jboto.api.IForeachCommand;
import com.tolstoy.jboto.api.IIfCommand;
import com.tolstoy.jboto.api.IBasicCommand;
import com.tolstoy.censorship.twitter.checker.app.jboto.OurEnvironment;

public class CreateReplyPageSnapshot implements IBasicCommand {
	private static final Logger logger = LogManager.getLogger( CreateReplyPageSnapshot.class );

	public CreateReplyPageSnapshot() {
	}

	public void run( IProduct product, IEnvironment env, Object extra, int index ) throws Exception {
		SearchRunTimelineData searchRunTimelineData = (SearchRunTimelineData) product;
		OurEnvironment ourEnv = (OurEnvironment) env;
		ITweet tweet = (ITweet) extra;

		final ITweetCollection tweetCollection = searchRunTimelineData.getIndividualPageJIC( tweet.getID() ).getTweetCollection();

		final List<ITweet> tweets = tweetCollection.getTweets();
		if ( tweets == null || tweets.isEmpty() ) {
			throw new RuntimeException( "makeSnapshotUserPageIndividualTweetFromURL: individual page must have at least one tweet" );
		}

		final ISnapshotUserPageIndividualTweet snapshot = ourEnv.getSnapshotFactory().makeSnapshotUserPageIndividualTweet( searchRunTimelineData.getIndividualPageURL( tweet.getID() ), searchRunTimelineData.getStartTime() );

		searchRunTimelineData.setIndividualPage( tweet.getID(), snapshot );

		final ITweet individualTweet = tweets.remove( 0 );
		snapshot.setIndividualTweet( individualTweet );
		snapshot.setUser( individualTweet.getUser() );
		snapshot.setTweetID( individualTweet.getID() );

		tweetCollection.setTweets( tweets );
		snapshot.setTweetCollection( tweetCollection );

		snapshot.setTitle( ourEnv.getWebDriver().getTitle() );

		final JavascriptInterchangeMetadata meta = searchRunTimelineData.getIndividualPageJIC( tweet.getID() ).getMetadata();

		snapshot.setComplete( meta != null ? meta.isCompleted() : false );

		snapshot.setNumRetweets( Utils.parseIntDefault( individualTweet.getAttribute( "retweetcount" ) ) );
		snapshot.setNumLikes( Utils.parseIntDefault( individualTweet.getAttribute( "favoritecount" ) ) );
		snapshot.setNumReplies( Utils.parseIntDefault( individualTweet.getAttribute( "replycount" ) ) );

		final ITweetUser user = snapshot.getUser();
		if ( user == null || Utils.isEmpty( user.getHandle() ) ) {
			throw new RuntimeException( "reply page snapshot does not have a user" );
		}

		logger.info( "user=" + user.toDebugString( "" ) );

		if ( tweetCollection == null || tweetCollection.getTweets() == null || tweetCollection.getTweets().isEmpty() ) {
			ourEnv.logWarn( logger, ourEnv.getBundle().getString( "srb_bad_timeline", searchRunTimelineData.getTimelineURL() ) );
		}
		else {
			ourEnv.logInfo( logger, ourEnv.getBundle().getString( "srb_loaded_timeline", tweetCollection.getTweets().size() ) );
		}
	}
}
