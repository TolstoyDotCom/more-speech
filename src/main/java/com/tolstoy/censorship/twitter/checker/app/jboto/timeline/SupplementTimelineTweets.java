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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.ITweetUserCollection;
import com.tolstoy.basic.api.tweet.TargetPageType;
import com.tolstoy.basic.api.tweet.TweetSupposedQuality;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeContainer;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeSupposedQualities;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptParams;
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

public class SupplementTimelineTweets implements IBasicCommand {
	private static final Logger logger = LogManager.getLogger( SupplementTimelineTweets.class );

	public SupplementTimelineTweets() {
	}

	public void run( IProduct prod, IEnvironment env, Object extra, int index ) throws Exception {
		SearchRunTimelineData product = (SearchRunTimelineData) prod;
		OurEnvironment ourEnv = (OurEnvironment) env;

		final ITweetCollection tweetCollection = product.getTimelineJIC().getTweetCollection();

		final JavascriptExecutor javascriptExecutor = (JavascriptExecutor) ourEnv.getWebDriver();

		ourEnv.getWebDriver().manage().timeouts().scriptTimeout( ourEnv.getGeneralTimeout() );

		logger.info( "calling SuedeDenim json_parser script" );

		final JavascriptParams jsParams = new JavascriptParams( product.getTimelineURL(), TargetPageType.TIMELINE, ourEnv.getDebugLevel() );

		final String suedeDenimJSONParserScript = ourEnv.getBrowserScriptFactory().getScript( "json_parser" ).getScript();

		final List<? extends Object> rawInterchangeData = (List<? extends Object>) javascriptExecutor.executeAsyncScript( suedeDenimJSONParserScript, jsParams.getMap(), product.getTimelineJSONStringList() );
		if ( rawInterchangeData == null ) {
			logger.info( "rawInterchangeData IS NULL, cannot supplement tweets" );
			return;
		}

		final JavascriptInterchangeContainer interchangeContainer = new JavascriptInterchangeContainer( rawInterchangeData, ourEnv.getTweetFactory(), ourEnv.getBundle() );

		logger.info( "\n\n\njson_parser script called, javascript interchange=\n" + interchangeContainer.toDebugString( "  " ) );

		final List<String> tweetSupplementMessages = tweetCollection.supplementFrom( interchangeContainer.getTweetCollection() );

		final ITweetUserCollection users = ourEnv.getTweetFactory().makeTweetUserCollection( tweetCollection.getTweetUsers(), product.getStartTime(), new HashMap<String,String>( 1 ) );

		users.supplementFrom( interchangeContainer.getTweetUserCollection() );

		if ( interchangeContainer.getSupposedQualities() != null ) {
			supplementSupposedQualities( tweetCollection.getTweets(), interchangeContainer.getSupposedQualities() );
		}

		supplementUserIDs( tweetCollection.getTweets() );

		supplementUserHandles( tweetCollection.getTweets() );

		logger.info( "\n\n\ntweetSupplementMessages=\n" + tweetSupplementMessages );
		logger.info( "\n\n\ntweets after supplementation=\n" + tweetCollection.toDebugString( "  " ) );
	}

	protected int supplementSupposedQualities( List<ITweet> tweets, JavascriptInterchangeSupposedQualities supposedQualities ) {
		TweetSupposedQuality supposedQuality;
		long tweetID;
		int count = 0;

		for ( final ITweet tweet : tweets ) {
			tweetID = tweet.getID();
			if ( tweetID == 0 ) {
				continue;
			}

			supposedQuality = supposedQualities.getSupposedQuality( tweetID );
			if ( supposedQuality != null ) {
				tweet.setSupposedQuality( supposedQuality );
				count++;
			}
		}

		return count;
	}

	protected void supplementUserIDs( List<ITweet> tweets ) {
		ITweetUser tweetUser;
		Set<Long> idSet;
		long userIDFromTweet;

		for ( final ITweet tweet : tweets ) {
			if ( tweet.getUser() == null ) {
				continue;
			}

			tweetUser = tweet.getUser();
			idSet = new HashSet<Long>( 2 );

			userIDFromTweet = Utils.parseLongDefault( tweet.getAttribute( "userid" ) );
			if ( userIDFromTweet != 0 ) {
				idSet.add( userIDFromTweet );
			}

			if ( tweetUser.getID() != 0 ) {
				idSet.add( tweetUser.getID() );
			}

			if ( idSet.isEmpty() ) {
				logger.info( "BOTH USER IDS ARE ZERO: " + tweet.toDebugString( "" ) );
			}
			else if ( idSet.size() == 1 ) {
				tweetUser.setID( idSet.iterator().next() );
				tweet.setAttribute( "userid", "" + tweetUser.getID() );
			}
			else {
				logger.info( "MISMATCHED USER IDS: " + tweet.toDebugString( "" ) );
			}
		}
	}

	protected void supplementUserHandles( List<ITweet> tweets ) {
		ITweetUser tweetUser;
		Set<String> handleSet;
		String handleFromTweet, handleFromUser;

		for ( final ITweet tweet : tweets ) {
			if ( tweet.getUser() == null ) {
				continue;
			}

			tweetUser = tweet.getUser();
			handleSet = new HashSet<String>( 2 );

			handleFromTweet = Utils.normalizeHandle( tweet.getAttribute( "username" ) );
			handleFromUser = Utils.normalizeHandle( tweetUser.getHandle() );

			if ( !Utils.isEmpty( handleFromTweet ) ) {
				handleSet.add( handleFromTweet );
			}

			if ( !Utils.isEmpty( handleFromUser ) ) {
				handleSet.add( handleFromUser );
			}

			if ( handleSet.isEmpty() ) {
				logger.info( "BOTH USER HANDLES ARE EMPTY: " + tweet.toDebugString( "" ) );
			}
			else if ( handleSet.size() == 1 ) {
				tweetUser.setHandle( handleSet.iterator().next() );
				tweet.setAttribute( "username", tweetUser.getHandle() );
			}
			else {
				logger.info( "MISMATCHED USER HANDLES: " + tweet.toDebugString( "" ) );
			}
		}
	}
}
