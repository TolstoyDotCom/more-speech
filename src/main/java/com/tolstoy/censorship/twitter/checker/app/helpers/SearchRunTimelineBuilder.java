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
package com.tolstoy.censorship.twitter.checker.app.helpers;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import com.tolstoy.basic.api.installation.DebugLevel;
import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.api.utils.IArchiveDirectory;
import com.tolstoy.basic.app.utils.KeyedLists;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyFactory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseEvent;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseListener;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferencesFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunTimeline;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IInfiniteScrollingActivator;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactoryFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.InfiniteScrollingActivatorType;
import com.tolstoy.censorship.twitter.checker.api.webdriver.WebDriverFactoryType;
import com.tolstoy.censorship.twitter.checker.app.jboto.timeline.SearchRunTimelineData;
import com.tolstoy.censorship.twitter.checker.app.jboto.OurEnvironment;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;
import com.tolstoy.jboto.api.framework.IFrameworkFactory;
import com.tolstoy.jboto.api.framework.IFramework;
import com.tolstoy.jboto.app.framework.FrameworkFactory;
import com.tolstoy.jboto.api.framework.IPackageAlias;
import com.tolstoy.jboto.api.framework.IFQNResolverFactory;
import com.tolstoy.jboto.api.framework.IFQNResolver;
import com.tolstoy.jboto.app.framework.FQNResolverFactory;
import com.tolstoy.jboto.app.framework.FrameworkFactory;

/**
 * Utility that uses WebDriver to build an ISearchRunTimeline object.
 *
 * First, read the tweets on the given user's timeline. Then for each
 * of those that aren't RTs, etc. build a ISnapshotUserPageIndividualTweet
 * for each tweet.
 *
 * This will show which replies to a specific user were elevated, suppressed, etc.
 */
final public class SearchRunTimelineBuilder /*implements IBrowserProxyResponseListener*/ {
	private static final Logger logger = LogManager.getLogger( SearchRunTimelineBuilder.class );
	private static final String JBOTO_DEFAULT_PACKAGE_NAME = "com.tolstoy.censorship.twitter.checker.app.jboto";
	private static final String JBOTO_COMMON_PACKAGE_NAME = "com.tolstoy.censorship.twitter.checker.app.jboto.common";
	private static final String JBOTO_TIMELINE_PACKAGE_NAME = "com.tolstoy.censorship.twitter.checker.app.jboto.timeline";

	private final IResourceBundleWithFormatting bundle;
	private final IStorage storage;
	private final IPreferencesFactory prefsFactory;
	private final IPreferences prefs;
	private final IWebDriverFactoryFactory webDriverFactoryFactory;
	private final ISearchRunFactory searchRunFactory;
	private final ISnapshotFactory snapshotFactory;
	private final ITweetFactory tweetFactory;
	private final IStatusMessageReceiver statusMessageReceiver;
	private final IBrowserProxyFactory browserProxyFactory;
	private final IArchiveDirectory archiveDirectory;
	/*private final KeyedLists<IBrowserProxyResponseEvent> proxyEvents;*/
	private final IBrowserScriptFactory browserScriptFactory;
	private final String handleToCheck;

	public SearchRunTimelineBuilder( final IResourceBundleWithFormatting bundle,
										final IStorage storage,
										final IPreferencesFactory prefsFactory,
										final IPreferences prefs,
										final IWebDriverFactoryFactory webDriverFactoryFactory,
										final ISearchRunFactory searchRunFactory,
										final ISnapshotFactory snapshotFactory,
										final ITweetFactory tweetFactory,
										final IBrowserProxyFactory browserProxyFactory,
										final IArchiveDirectory archiveDirectory,
										final IStatusMessageReceiver statusMessageReceiver,
										final IBrowserScriptFactory browserScriptFactory,
										final String handleToCheck ) throws Exception {
		this.bundle = bundle;
		this.storage = storage;
		this.prefsFactory = prefsFactory;
		this.prefs = prefs;
		this.webDriverFactoryFactory = webDriverFactoryFactory;
		this.searchRunFactory = searchRunFactory;
		this.snapshotFactory = snapshotFactory;
		this.tweetFactory = tweetFactory;
		this.statusMessageReceiver = statusMessageReceiver;
		this.browserProxyFactory = browserProxyFactory;
		this.archiveDirectory = archiveDirectory;
		this.browserScriptFactory = browserScriptFactory;
		this.handleToCheck = Utils.trimDefault( handleToCheck ).toLowerCase();

		if ( webDriverFactoryFactory == null ) {
			throw new RuntimeException( bundle.getString( "exc_no_webdriverfactory" ) );
		}
	}

	public ISearchRunTimeline buildSearchRunTimeline( final int numberOfTimelinePagesToScroll,
														final int numberOfIndividualPagesToScroll,
														final int numberOfReplyPagesToCheck,
														final int numberOfTimelineTweetsToSkip ) throws Exception {
		IWebDriverFactory webDriverFactory = null;
		WebDriver webDriver = null;
		IBrowserProxy browserProxy = null;
		IWebDriverUtils webDriverUtils = null;
		LoginToSite loginToSite = null;
		String loginName = null;
		String loginPassword = null;
		boolean bSkipLogin = false, bUsingLogin = false;

		try {
			SearchRunTimelineData searchRunTimelineData = new SearchRunTimelineData( prefs,
																				handleToCheck,
																				numberOfTimelinePagesToScroll,
																				numberOfIndividualPagesToScroll,
																				numberOfReplyPagesToCheck,
																				numberOfTimelineTweetsToSkip );

			OurEnvironment env = new OurEnvironment( bundle,
														storage,
														prefsFactory,
														prefs,
														webDriverFactoryFactory,
														searchRunFactory,
														snapshotFactory,
														tweetFactory,
														browserProxyFactory,
														archiveDirectory,
														statusMessageReceiver,
														browserScriptFactory,
														DebugLevel.VERBOSE );

			String testJSON = IOUtils.toString( getClass().getResource( "/jboto-timeline.json" ), StandardCharsets.UTF_8 );

			IFrameworkFactory factory = new FrameworkFactory( createResolver() );

			IFramework framework = factory.makeFrameworkFromJSON( "test", testJSON );

			logger.info( "\n" + framework.toDebugString( "" ) );

			logger.info( "searchRunTimelineData BEFORE: " + searchRunTimelineData );

			framework.run( searchRunTimelineData, env, null, 0 );

			logger.info( "searchRunTimelineData AFTER: " + searchRunTimelineData );

			final ISearchRunTimeline ret = searchRunFactory.makeSearchRunTimeline( 0,
																					searchRunTimelineData.getUser(),
																					searchRunTimelineData.getStartTime(),
																					Instant.now(),
																					searchRunTimelineData.getTimeline(),
																					searchRunTimelineData.getIndividualPages() );

			ret.setAttribute( "handle_to_check", handleToCheck );
			ret.setAttribute( "loggedin", ( searchRunTimelineData.isUsingLogin() || searchRunTimelineData.isSkipLogin() ) ? "true" : "false" );

			return ret;
		}
		catch ( final Exception e ) {
			String message = "error logging in or building searchRun";

			Utils.logException( logger, message, e );
			statusMessageReceiver.addMessage( new StatusMessage( message, StatusMessageSeverity.WARN ) );

			throw e;
		}
	}

	private IFQNResolver createResolver() throws Exception {
		IFQNResolverFactory resolverFactory = new FQNResolverFactory();
		List<IPackageAlias> aliases = new ArrayList<IPackageAlias>( 1 );

		aliases.add( resolverFactory.makePackageAlias( "common", JBOTO_COMMON_PACKAGE_NAME ) );
		aliases.add( resolverFactory.makePackageAlias( "timeline", JBOTO_TIMELINE_PACKAGE_NAME ) );

		IFQNResolver resolver = resolverFactory.makeResolver( JBOTO_DEFAULT_PACKAGE_NAME, aliases );

		return resolver;
	}
}
