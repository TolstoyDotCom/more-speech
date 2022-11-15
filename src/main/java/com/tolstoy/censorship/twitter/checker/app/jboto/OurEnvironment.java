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
package com.tolstoy.censorship.twitter.checker.app.jboto;

import java.util.List;
import java.util.ArrayList;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.tolstoy.jboto.api.IEnvironment;
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
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyLogEntry;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;
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

public class OurEnvironment implements IEnvironment {
	private static final Logger logger = LogManager.getLogger( OurEnvironment.class );

	private static final Duration TIMEOUT = Duration.ofSeconds( 10000 );
	private static final int WEBDRIVER_CLOSE_DELAY_MILLIS = 5000;

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
	/*private final KeyedLists<IBrowserProxyResponseEvent> proxyEvents;*/
	private IWebDriverUtils webDriverUtils;
	private IWebDriverFactory webDriverFactory;
	private WebDriver webDriver;
	private IBrowserProxy browserProxy;
	private DebugLevel debugLevel;

	public OurEnvironment( final IResourceBundleWithFormatting bundle,
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
							final DebugLevel debugLevel ) throws Exception {
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
		this.debugLevel = debugLevel;

		if ( webDriverFactoryFactory == null ) {
			throw new RuntimeException( bundle.getString( "exc_no_webdriverfactory" ) );
		}
	}

	public Duration getGeneralTimeout() {
		return TIMEOUT;
	}

	public DebugLevel getDebugLevel() {
		return debugLevel;
	}

	public void setDebugLevel( DebugLevel val ) {
		debugLevel = val;
	}

	public int getWebdriverCloseDelay() {
		return WEBDRIVER_CLOSE_DELAY_MILLIS;
	}

	public IWebDriverFactoryFactory getWebDriverFactoryFactory() {
		return webDriverFactoryFactory;
	}

	public IWebDriverFactory getWebDriverFactory() {
		return webDriverFactory;
	}

	public void setWebDriverFactory( IWebDriverFactory webDriverFactory ) {
		this.webDriverFactory = webDriverFactory;
	}

	public WebDriver getWebDriver() {
		return webDriver;
	}

	public void setWebDriver( WebDriver webDriver ) {
		this.webDriver = webDriver;
	} 

	public IWebDriverUtils getWebDriverUtils() {
		return webDriverUtils;
	}

	public void setWebDriverUtils( IWebDriverUtils webDriverUtils ) {
		this.webDriverUtils = webDriverUtils;
	} 

	public IBrowserScriptFactory getBrowserScriptFactory() {
		return browserScriptFactory;
	}

	public IBrowserProxyFactory getBrowserProxyFactory() {
		return browserProxyFactory;
	}

	public IBrowserProxy getBrowserProxy() {
		return browserProxy;
	}

	public void setBrowserProxy( IBrowserProxy browserProxy ) {
		this.browserProxy = browserProxy;
	}

	public IResourceBundleWithFormatting getBundle() {
		return bundle;
	}

	public IStorage getStorage() {
		return storage;
	}

	public IPreferencesFactory getPrefsFactory() {
		return prefsFactory;
	}

	public IPreferences getPrefs() {
		return prefs;
	}

	public ISearchRunFactory getSearchRunFactory() {
		return searchRunFactory;
	}

	public ISnapshotFactory getSnapshotFactory() {
		return snapshotFactory;
	}

	public ITweetFactory getTweetFactory() {
		return tweetFactory;
	}

	public IStatusMessageReceiver getStatusMessageReceiver() {
		return statusMessageReceiver;
	}

	public IArchiveDirectory getArchiveDirectory() {
		return archiveDirectory;
	}

	public List<String> saveJSONStrings( final List<IBrowserProxyLogEntry> responses, final IArchiveDirectory archiveDirectory ) throws Exception {
		final List<String> jsonStrings = new ArrayList<String>( 10 );

		for ( IBrowserProxyLogEntry response : responses ) {
			String responseContent = response.getContent();
			final String responseURL = response.getURL();
			if ( responseURL == null ) {
				continue;
			}

/*
			if ( || responseURL.indexOf( "cursor" ) < 0 ) {
				continue;
			}
*/

			if ( responseContent == null || responseContent.length() < 1 ) {
				logger.info( "WebDriverFactoryNT.saveJSONStrings: NO RESPONSE FOR " + responseURL );
				continue;
			}

			responseContent = responseContent.trim();
			if ( !( responseContent.startsWith( "[" ) || responseContent.startsWith( "{" ) ) ) {
				logger.info( "WebDriverFactoryNT.saveJSONStrings: NOT JSON FOR " + responseURL );
				continue;
			}

			jsonStrings.add( responseContent );

			String archiveFilename = archiveDirectory.put( responseContent );
			logger.info( "WebDriverFactoryNT.saveJSONStrings: ARCHIVE SAVED " + responseURL + " TO " + archiveFilename );
		}

		return jsonStrings;
	}

	public void logInfo( final Logger logger, final String s ) {
		logger.info( s );
		statusMessageReceiver.addMessage( new StatusMessage( s, StatusMessageSeverity.INFO ) );
	}

	public void logWarn( final Logger logger, final String s ) {
		logger.info( s );
		statusMessageReceiver.addMessage( new StatusMessage( s, StatusMessageSeverity.WARN ) );
	}

	public void logWarn( final Logger logger, final String s, final Exception e ) {
		Utils.logException( logger, s, e );
		statusMessageReceiver.addMessage( new StatusMessage( s, StatusMessageSeverity.WARN ) );
	}
}
