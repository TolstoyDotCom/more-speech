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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.TargetPageType;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyLogEntry;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IInfiniteScrollingActivator;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.InfiniteScrollingActivatorType;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeContainer;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeMetadata;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptParams;
import com.tolstoy.jboto.api.framework.IFrameworkFactory;
import com.tolstoy.jboto.api.framework.IFramework;
import com.tolstoy.jboto.app.framework.FrameworkFactory;
import com.tolstoy.jboto.api.IProduct;
import com.tolstoy.jboto.api.IEnvironment;
import com.tolstoy.jboto.api.IForeachCommand;
import com.tolstoy.jboto.api.IIfCommand;
import com.tolstoy.jboto.api.IBasicCommand;
import com.tolstoy.censorship.twitter.checker.app.jboto.OurEnvironment;

public class RetrieveTimelineTweets implements IBasicCommand {
	private static final Logger logger = LogManager.getLogger( RetrieveTimelineTweets.class );

	public RetrieveTimelineTweets() {
	}

	public void run( IProduct prod, IEnvironment env, Object extra, int index ) throws Exception {
		SearchRunTimelineData product = (SearchRunTimelineData) prod;
		OurEnvironment ourEnv = (OurEnvironment) env;

		final IInfiniteScrollingActivator scroller = ourEnv.getWebDriverFactory().makeInfiniteScrollingActivator( ourEnv.getWebDriver(),
																										ourEnv.getWebDriverUtils(),
																										InfiniteScrollingActivatorType.TIMELINE );

		final JavascriptExecutor javascriptExecutor = (JavascriptExecutor) ourEnv.getWebDriver();

		ourEnv.getWebDriver().manage().timeouts().scriptTimeout( ourEnv.getGeneralTimeout() );
		ourEnv.getWebDriver().manage().window().maximize();

		logger.info( "makeTweetCollectionFromURL: calling SuedeDenim tweet_retriever script" );

		final JavascriptParams jsParams = new JavascriptParams( product.getTimelineURL(), TargetPageType.TIMELINE, ourEnv.getDebugLevel() );
		jsParams.setValue( "scrollerNumTimesToScroll", "" + ( 5 * product.getNumberOfTimesToScrollOnTimeline() ) );
		jsParams.setValue( "scrollerHeightMultiplier", "0.25" );

		final String suedeDenimRetrieverScript = ourEnv.getBrowserScriptFactory().getScript( "tweet_retriever" ).getScript();

		final List<? extends Object> rawInterchangeData = (List<? extends Object>) javascriptExecutor.executeAsyncScript( suedeDenimRetrieverScript, jsParams.getMap() );
		if ( rawInterchangeData == null ) {
			logger.info( "rawInterchangeData IS NULL, cannot retrieve tweets" );
			return;
		}

		product.setTimelineJIC( new JavascriptInterchangeContainer( rawInterchangeData, ourEnv.getTweetFactory(), ourEnv.getBundle() ) );

		logger.info( "makeTweetCollectionFromURL: SuedeDenim tweet_retriever script called, javascript interchange=\n" + product.getTimelineJIC().toDebugString( "  " ) );

		final ITweetCollection tweetCollection = product.getTimelineJIC().getTweetCollection();
		tweetCollection.setAttribute( "url", product.getTimelineURL() );
		tweetCollection.setAttribute( "numberOfPagesToCheck", "" + product.getNumberOfReplyPagesToCheck() );
		tweetCollection.setAttribute( "maxTweets", "" + product.getNumberOfTimesToScrollOnIndividualPages() );
	}
}
