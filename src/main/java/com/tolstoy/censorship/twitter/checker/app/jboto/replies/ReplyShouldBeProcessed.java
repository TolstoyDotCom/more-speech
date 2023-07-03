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
import com.tolstoy.basic.api.tweet.ITweet;
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

public class ReplyShouldBeProcessed implements IIfCommand {
	private static final Logger logger = LogManager.getLogger( ReplyShouldBeProcessed.class );

	public ReplyShouldBeProcessed() {
	}

	public boolean test( IProduct prod, IEnvironment env, Object extra, int index ) throws Exception {
		RepliesProduct product = (RepliesProduct) prod;
		OurEnvironment ourEnv = (OurEnvironment) env;
		ITweet tweet = (ITweet) extra;

		if ( !Utils.isStringTrue( tweet.getAttribute( "hasparenttweet" ) ) || !Utils.isStringTrue( tweet.getAttribute( "isreplyto" ) ) ) {
			logger.info( "skipped because not a reply: " + tweet.toDebugString( "" ) );
			return false;
		}

		long tweetID1 = Utils.parseLongDefault( tweet.getAttribute( "itemid" ) );
		long tweetID2 = Utils.parseLongDefault( tweet.getAttribute( "conversationid" ) );

		if ( tweetID1 == 0 && tweetID2 == 0 ) {
			logger.info( "skipped because tweetIDs: tweetID1=" + tweetID1 + ", tweetID2=" + tweetID2 + " " + tweet.toDebugString( "" ) );
			return false;
		}

		String itineraryUserHandle = Utils.trimDefault( product.getUser().getHandle() ).toLowerCase();
		String tweetUserHandle = Utils.trimDefault( tweet.getRepliedToHandle() ).toLowerCase();

		if ( itineraryUserHandle == null ) {
			logger.info( "skipped because itineraryUserHandle is null: " + tweet.toDebugString( "" ) );
			return false;
		}

		if ( tweetUserHandle == null ) {
			logger.info( "skipped because tweetUserHandle is null: " + tweet.toDebugString( "" ) );
			return false;
		}

		if ( itineraryUserHandle.equals( tweetUserHandle ) ) {
			logger.info( "skipped because it's a self-reply: " + tweet.toDebugString( "" ) );
			return false;
		}

		return true;
	}
}
