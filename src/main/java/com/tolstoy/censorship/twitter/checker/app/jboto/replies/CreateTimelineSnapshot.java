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
package com.tolstoy.censorship.twitter.checker.app.jboto.replies;

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
		RepliesProduct product = (RepliesProduct) prod;
		OurEnvironment ourEnv = (OurEnvironment) env;

		final ISnapshotUserPageTimeline timeline = ourEnv.getSnapshotFactory().makeSnapshotUserPageTimeline( product.getTimelineURL(), product.getStartTime() );

		product.setTimeline( timeline );

		final ITweetUser user = product.getItinerary().getInitiatingUser();
		timeline.setUser( user );

		timeline.setTweetCollection( product.getItinerary().getTimelineTweetCollection() );
		timeline.setNumTotalTweets( product.getItinerary().getInitiatingUser().getNumTotalTweets() );
		timeline.setNumFollowers( product.getItinerary().getInitiatingUser().getNumFollowers() );
		timeline.setNumFollowing( product.getItinerary().getInitiatingUser().getNumFollowing() );

		//timeline.setTitle( ourEnv.getWebDriver().getTitle() );
		//timeline.setComplete( meta != null ? meta.isCompleted() : false );

		logger.info( "user=" + user.toDebugString( "" ) );
	}
}
