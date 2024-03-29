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
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
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

public class CreateTimelineSnapshot implements IBasicCommand {
	private static final Logger logger = LogManager.getLogger( CreateTimelineSnapshot.class );

	public CreateTimelineSnapshot() {
	}

	public void run( IProduct prod, IEnvironment env, Object extra, int index ) throws Exception {
		SearchRunTimelineData product = (SearchRunTimelineData) prod;
		OurEnvironment ourEnv = (OurEnvironment) env;

		final ITweetCollection tweetCollection = product.getTimelineJIC().getTweetCollection();

		final ISnapshotUserPageTimeline snapshot = ourEnv.getSnapshotFactory().makeSnapshotUserPageTimeline( product.getTimelineURL(), product.getStartTime() );

		product.setTimeline( snapshot );

		snapshot.setTweetCollection( tweetCollection );

		snapshot.setTitle( ourEnv.getWebDriver().getTitle() );

		final JavascriptInterchangeMetadata meta = product.getTimelineJIC().getMetadata();

		snapshot.setComplete( meta != null ? meta.isCompleted() : false );

		ITweetUser tweetUser = getTimelineUser( product, tweetCollection );
		if ( tweetUser != null ) {
			snapshot.setUser( tweetUser );
			snapshot.setNumTotalTweets( tweetUser.getNumTotalTweets() );
			snapshot.setNumFollowers( tweetUser.getNumFollowers() );
			snapshot.setNumFollowing( tweetUser.getNumFollowing() );
		}

		final ITweetUser user = snapshot.getUser();
		if ( user == null || Utils.isEmpty( user.getHandle() ) ) {
			throw new RuntimeException( "Timeline snapshot does not have a user. Are there tweets from the user being checked on their timeline, or just RTs?" );
		}

		product.setUser( user );

		logger.info( "user=" + user.toDebugString( "" ) );

		if ( tweetCollection == null || tweetCollection.getTweets() == null || tweetCollection.getTweets().isEmpty() ) {
			ourEnv.logWarn( logger, ourEnv.getBundle().getString( "srb_bad_timeline", product.getTimelineURL() ) );
		}
		else {
			ourEnv.logInfo( logger, ourEnv.getBundle().getString( "srb_loaded_timeline", tweetCollection.getTweets().size() ) );
		}
	}

	protected ITweetUser getTimelineUser( SearchRunTimelineData product, ITweetCollection tweetCollection ) throws Exception {
		if ( tweetCollection.getTweets() == null || tweetCollection.getTweets().isEmpty() ) {
			return null;
		}

		for ( ITweet tweet : tweetCollection.getTweets() ) {
			ITweetUser tweetUser = tweet.getUser();
			if ( tweetUser == null ) {
				return null;
			}

			if ( product.getHandleToCheck().equals( tweetUser.getHandle() ) ) {
				return tweetUser;
			}
		}

		return null;
	}
}
