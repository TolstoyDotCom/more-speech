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
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import com.tolstoy.basic.api.installation.DebugLevel;
import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.api.utils.IArchiveDirectory;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalysisReportFactory;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyFactory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtension;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtensionList;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtensionFactory;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferencesFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunFactory;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunRepliesItinerary;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactoryFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.WebDriverFactoryType;
import com.tolstoy.censorship.twitter.checker.app.jboto.replies.RepliesProduct;
import com.tolstoy.censorship.twitter.checker.app.jboto.OurEnvironment;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersSet;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParameters;
import com.tolstoy.jboto.api.framework.IFrameworkFactory;
import com.tolstoy.jboto.api.framework.IFramework;
import com.tolstoy.jboto.app.framework.FrameworkFactory;
import com.tolstoy.jboto.api.framework.IPackageAlias;
import com.tolstoy.jboto.api.framework.IFQNResolverFactory;
import com.tolstoy.jboto.api.framework.IFQNResolver;
import com.tolstoy.jboto.app.framework.FQNResolverFactory;
import com.tolstoy.jboto.app.framework.FrameworkFactory;

final public class SearchRunRepliesBuilderJBoto {
	private static final Logger logger = LogManager.getLogger( SearchRunRepliesBuilderJBoto.class );
	private static final String JBOTO_DEFAULT_PACKAGE_NAME = "com.tolstoy.censorship.twitter.checker.app.jboto";
	private static final String JBOTO_COMMON_PACKAGE_NAME = "com.tolstoy.censorship.twitter.checker.app.jboto.common";
	private static final String JBOTO_REPLIES_PACKAGE_NAME = "com.tolstoy.censorship.twitter.checker.app.jboto.replies";

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
	private final IBrowserScriptFactory browserScriptFactory;
	private final IBrowserExtensionFactory browserExtensionFactory;
	private final ISearchRunRepliesItinerary itinerary;
	private final String handleToCheck;
	private final IAppDirectories appDirectories;
	private final IAnalysisReportFactory analysisReportFactory;

	public SearchRunRepliesBuilderJBoto( final IResourceBundleWithFormatting bundle,
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
						final IAppDirectories appDirectories,
						final IAnalysisReportFactory analysisReportFactory,
						final IBrowserScriptFactory browserScriptFactory,
						final IBrowserExtensionFactory browserExtensionFactory,
						final ISearchRunRepliesItinerary itinerary ) throws Exception {
		this.bundle = bundle;
		this.storage = storage;
		this.prefsFactory = prefsFactory;
		this.prefs = prefs;
		this.webDriverFactoryFactory = webDriverFactoryFactory;
		this.searchRunFactory = searchRunFactory;
		this.snapshotFactory = snapshotFactory;
		this.tweetFactory = tweetFactory;
		this.browserProxyFactory = browserProxyFactory;
		this.archiveDirectory = archiveDirectory;
		this.statusMessageReceiver = statusMessageReceiver;
		this.appDirectories = appDirectories;
		this.analysisReportFactory = analysisReportFactory;
		this.browserScriptFactory = browserScriptFactory;
		this.browserExtensionFactory = browserExtensionFactory;
		this.itinerary = itinerary;
		this.handleToCheck = Utils.trimDefault( itinerary.getInitiatingUser().getHandle() ).toLowerCase();

		if ( webDriverFactoryFactory == null ) {
			throw new RuntimeException( bundle.getString( "exc_no_webdriverfactory" ) );
		}

		final ITweetUser user = this.itinerary.getInitiatingUser();
		if ( user == null || Utils.isEmpty( user.getHandle() ) ) {
			throw new RuntimeException( "itinerary does not have a user" );
		}

		final ITweetCollection tweetCollection = this.itinerary.getTimelineTweetCollection();
		if ( tweetCollection == null || tweetCollection.getTweets() == null || tweetCollection.getTweets().isEmpty() ) {
			throw new RuntimeException( "itinerary does not have tweet" );
		}
	}

	public ISearchRunReplies buildSearchRunReplies( final IPageParametersSet pageParametersSet ) throws Exception {
		try {
			RepliesProduct product = new RepliesProduct( prefs,
															handleToCheck,
															itinerary,
															pageParametersSet );

			//	@todo: make this a setting
			final IBrowserExtensionList extensionsToInstall = browserExtensionFactory.makeBrowserExtensionList();
			extensionsToInstall.add( this.browserExtensionFactory.makeBrowserExtension( "har_export_trigger", "/har_export_trigger-0.6.1.xpi" ) );

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
														appDirectories,
														analysisReportFactory,
														browserScriptFactory,
														browserExtensionFactory,
														extensionsToInstall,
														DebugLevel.TERSE );

			String testJSON = IOUtils.toString( getClass().getResource( "/jboto-replies.json" ), StandardCharsets.UTF_8 );

			IFrameworkFactory factory = new FrameworkFactory( createResolver() );

			IFramework framework = factory.makeFrameworkFromJSON( "test", testJSON );

			logger.info( "\n" + framework.toDebugString( "" ) );

			logger.info( "product BEFORE: " + product );

			framework.run( product, env, null, 0 );

			logger.info( "product AFTER: " + product );

			final ISearchRunReplies ret = searchRunFactory.makeSearchRunReplies( 0,
																					product.getUser(),
																					product.getStartTime(),
																					Instant.now(),
																					product.getTimeline(),
																					product.getReplyPages() );

			ret.setAttribute( "handle_to_check", handleToCheck );
			ret.setAttribute( "loggedin", ( product.isUsingLogin() || product.isSkipLogin() ) ? "true" : "false" );

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
		aliases.add( resolverFactory.makePackageAlias( "replies", JBOTO_REPLIES_PACKAGE_NAME ) );

		IFQNResolver resolver = resolverFactory.makeResolver( JBOTO_DEFAULT_PACKAGE_NAME, aliases );

		return resolver;
	}
}
