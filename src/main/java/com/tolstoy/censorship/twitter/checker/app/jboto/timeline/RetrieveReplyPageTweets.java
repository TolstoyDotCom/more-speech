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
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IInfiniteScrollingActivator;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.InfiniteScrollingActivatorType;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptParams;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeContainer;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeMetadata;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyLogEntry;
import com.tolstoy.basic.api.tweet.TargetPageType;
import com.tolstoy.jboto.api.framework.IFrameworkFactory;
import com.tolstoy.jboto.api.framework.IFramework;
import com.tolstoy.jboto.app.framework.FrameworkFactory;
import com.tolstoy.jboto.api.IProduct;
import com.tolstoy.jboto.api.IEnvironment;
import com.tolstoy.jboto.api.IForeachCommand;
import com.tolstoy.jboto.api.IIfCommand;
import com.tolstoy.jboto.api.IBasicCommand;
import com.tolstoy.censorship.twitter.checker.app.jboto.OurEnvironment;

public class RetrieveReplyPageTweets implements IBasicCommand {
	private static final Logger logger = LogManager.getLogger( RetrieveReplyPageTweets.class );

	public RetrieveReplyPageTweets() {
	}

	public void run( IProduct prod, IEnvironment env, Object extra, int index ) throws Exception {
		SearchRunTimelineData product = (SearchRunTimelineData) prod;
		OurEnvironment ourEnv = (OurEnvironment) env;
		ITweet tweet = (ITweet) extra;

		ISnapshotUserPageIndividualTweet individualPage;

		final IInfiniteScrollingActivator scroller = ourEnv.getWebDriverFactory().makeInfiniteScrollingActivator( ourEnv.getWebDriver(),
																										ourEnv.getWebDriverUtils(),
																										InfiniteScrollingActivatorType.INDIVIDUAL );

		final JavascriptExecutor javascriptExecutor = (JavascriptExecutor) ourEnv.getWebDriver();

		ourEnv.getWebDriver().manage().timeouts().scriptTimeout( ourEnv.getGeneralTimeout() );
		ourEnv.getWebDriver().manage().window().maximize();

		logger.info( "makeTweetCollectionFromURL: calling SuedeDenim tweet_retriever script" );

		final JavascriptParams jsParams = new JavascriptParams( product.getIndividualPageURL( tweet.getID() ), TargetPageType.REPLYPAGE, ourEnv.getDebugLevel() );
		jsParams.setValue( "scrollerNumTimesToScroll", "" + ( 5 * product.getNumberOfTimesToScrollOnIndividualPages() ) );
		jsParams.setValue( "scrollerHeightMultiplier", "0.25" );

		final String suedeDenimRetrieverScript = ourEnv.getBrowserScriptFactory().getScript( "tweet_retriever" ).getScript();

		final List rawInterchangeData = (List) javascriptExecutor.executeAsyncScript( suedeDenimRetrieverScript, jsParams.getMap() );
		if ( rawInterchangeData == null ) {
			logger.info( "rawInterchangeData IS NULL, cannot retrieve tweets" );
			return;
		}

		JavascriptInterchangeContainer interchangeContainer = new JavascriptInterchangeContainer( rawInterchangeData, ourEnv.getTweetFactory(), ourEnv.getBundle() );
		product.setIndividualPageJIC( tweet.getID(), interchangeContainer );

		logger.info( "makeTweetCollectionFromURL: SuedeDenim tweet_retriever script called, javascript interchange=\n" + interchangeContainer.toDebugString( "  " ) );

		final ITweetCollection tweetCollection = interchangeContainer.getTweetCollection();
		tweetCollection.setAttribute( "url", product.getIndividualPageURL( tweet.getID() ) );
		tweetCollection.setAttribute( "numberOfPagesToCheck", "" + product.getNumberOfTimesToScrollOnIndividualPages() );
		tweetCollection.setAttribute( "maxTweets", "" + product.getNumberOfTimesToScrollOnIndividualPages() );
	}
}
