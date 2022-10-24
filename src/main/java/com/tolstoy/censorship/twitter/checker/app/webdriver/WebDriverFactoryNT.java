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
package com.tolstoy.censorship.twitter.checker.app.webdriver;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.concurrent.TimeUnit;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.logging.LogEntry;
import io.github.bonigarcia.wdm.WebDriverManager;
//import net.jsourcerer.webdriver.jserrorcollector.JavaScriptError;
import org.openqa.selenium.remote.http.ClientConfig;

import com.tolstoy.basic.api.installation.DebugLevel;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.TargetPageType;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetUserCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.TweetUserVerifiedStatus;
import com.tolstoy.basic.api.tweet.TweetSupposedQuality;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.api.utils.IArchiveDirectory;
import com.tolstoy.basic.app.utils.StringList;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyLogEntry;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPage;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IInfiniteScrollingActivator;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.InfiniteScrollingActivatorType;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeContainer;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeMetadata;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeSupposedQualities;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptParams;

class WebDriverFactoryNT implements IWebDriverFactory {
	private static final Logger logger = LogManager.getLogger( WebDriverFactoryNT.class );

	private static final String TWEETUSER_HANDLE_UNKNOWN = "unknownuser";
	private static final Duration TIMEOUT = Duration.ofSeconds( 10000 );

	private final ISnapshotFactory snapshotFactory;
	private final ITweetFactory tweetFactory;
	private final IAppDirectories appDirectories;
	private final IBrowserScriptFactory browserScriptFactory;
	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;
	private final DebugLevel debugLevel;

	private static class TweetCollectionExtended {
		ITweetCollection tweetCollection;
		JavascriptInterchangeContainer interchangeContainer;

		TweetCollectionExtended( ITweetCollection tweetCollection, JavascriptInterchangeContainer interchangeContainer ) {
			this.tweetCollection = tweetCollection;
			this.interchangeContainer = interchangeContainer;
		}
	}

	WebDriverFactoryNT( final ISnapshotFactory snapshotFactory,
								final ITweetFactory tweetFactory,
								final IAppDirectories appDirectories,
								final IBrowserScriptFactory browserScriptFactory,
								final IPreferences prefs,
								final IResourceBundleWithFormatting bundle,
								final DebugLevel debugLevel ) throws Exception {
		this.snapshotFactory = snapshotFactory;
		this.tweetFactory = tweetFactory;
		this.appDirectories = appDirectories;
		this.browserScriptFactory = browserScriptFactory;
		this.prefs = prefs;
		this.bundle = bundle;
		this.debugLevel = debugLevel;
	}

	@Override
	public ISnapshotUserPageTimeline makeSnapshotUserPageTimelineFromURL( final WebDriver driver,
																			final IWebDriverUtils driverutils,
																			final IInfiniteScrollingActivator infiniteScroller,
																			final IBrowserProxy browserProxy,
																			final IArchiveDirectory archiveDirectory,
																			final String url,
																			final int numberOfPagesToCheck,
																			final int maxTweets ) throws Exception {
		browserProxy.beginRecording( driver, url );

		final TweetCollectionExtended tweetCollectionExtended = makeTweetCollectionFromURLInternal( driver,
																									driverutils,
																									infiniteScroller,
																									archiveDirectory,
																									url,
																									TargetPageType.TIMELINE,
																									numberOfPagesToCheck,
																									maxTweets );

		final ITweetCollection tweetCollection = tweetCollectionExtended.tweetCollection;
		final JavascriptInterchangeMetadata meta = tweetCollectionExtended.interchangeContainer.getMetadata();

		final List<IBrowserProxyLogEntry> responses = browserProxy.endRecording( driver );

		final List<String> jsonStrings = saveJSONStrings( responses, archiveDirectory );

		final List<String> tweetSupplementMessages = supplementTweetCollection( driver, tweetCollection, jsonStrings, TargetPageType.TIMELINE, url );

		final ISnapshotUserPageTimeline snapshot = snapshotFactory.makeSnapshotUserPageTimeline( url, Instant.now() );

		snapshot.setTweetCollection( tweetCollection );

		snapshot.setTitle( driver.getTitle() );

		snapshot.setComplete( meta != null ? meta.isCompleted() : false );

		if ( tweetCollection.getTweets() != null && !tweetCollection.getTweets().isEmpty() ) {
			ITweetUser tweetUser = tweetCollection.getTweets().get( 0 ).getUser();
			if ( tweetUser != null ) {
				snapshot.setUser( tweetUser );
				snapshot.setNumTotalTweets( tweetUser.getNumTotalTweets() );
				snapshot.setNumFollowers( tweetUser.getNumFollowers() );
				snapshot.setNumFollowing( tweetUser.getNumFollowing() );
			}
		}

		logger.info( "\n\n\nmakeSnapshotUserPageTimelineFromURL made timeline snapshot, tweets after supplementation=\n" + tweetCollection.toDebugString( "  " ) );
		logger.info( "\n\n\nmakeSnapshotUserPageTimelineFromURL made replypage snapshot, tweetSupplementMessages=\n" + tweetSupplementMessages );

		return snapshot;
	}

	@Override
	public ISnapshotUserPageIndividualTweet makeSnapshotUserPageIndividualTweetFromURL( final WebDriver driver,
																						final IWebDriverUtils driverutils,
																						final IInfiniteScrollingActivator infiniteScroller,
																						final IBrowserProxy browserProxy,
																						final IArchiveDirectory archiveDirectory,
																						final String url,
																						final int numberOfPagesToCheck,
																						final int maxTweets ) throws Exception {
		browserProxy.beginRecording( driver, url );

		final TweetCollectionExtended tweetCollectionExtended = makeTweetCollectionFromURLInternal( driver,
																									driverutils,
																									infiniteScroller,
																									archiveDirectory,
																									url,
																									TargetPageType.REPLYPAGE,
																									numberOfPagesToCheck,
																									maxTweets );

		final ITweetCollection tweetCollection = tweetCollectionExtended.tweetCollection;
		final JavascriptInterchangeMetadata meta = tweetCollectionExtended.interchangeContainer.getMetadata();

		final List<ITweet> tweets = tweetCollection.getTweets();
		if ( tweets == null || tweets.isEmpty() ) {
			throw new RuntimeException( "makeSnapshotUserPageIndividualTweetFromURL: individual page must have at least one tweet" );
		}

		List<IBrowserProxyLogEntry> responses = browserProxy.endRecording( driver );

		List<String> jsonStrings = saveJSONStrings( responses, archiveDirectory );

		final List<String> tweetSupplementMessages = supplementTweetCollection( driver, tweetCollection, jsonStrings, TargetPageType.REPLYPAGE, url );

		final ISnapshotUserPageIndividualTweet snapshot = snapshotFactory.makeSnapshotUserPageIndividualTweet( url, Instant.now() );

		snapshot.setComplete( infiniteScroller.getComplete() );

		final ITweet individualTweet = tweets.remove( 0 );
		snapshot.setIndividualTweet( individualTweet );
		snapshot.setUser( individualTweet.getUser() );
		snapshot.setTweetID( individualTweet.getID() );

		tweetCollection.setTweets( tweets );
		snapshot.setTweetCollection( tweetCollection );

		snapshot.setTitle( driver.getTitle() );

		snapshot.setComplete( meta != null ? meta.isCompleted() : false );

		snapshot.setNumRetweets( Utils.parseIntDefault( individualTweet.getAttribute( "retweetcount" ) ) );
		snapshot.setNumLikes( Utils.parseIntDefault( individualTweet.getAttribute( "favoritecount" ) ) );
		snapshot.setNumReplies( Utils.parseIntDefault( individualTweet.getAttribute( "replycount" ) ) );

		logger.info( "makeSnapshotUserPageIndividualTweetFromURL made replypage snapshot, tweets after supplementation=\n" + tweetCollection.toDebugString( "  " ) );
		logger.info( "makeSnapshotUserPageIndividualTweetFromURL made replypage snapshot, tweetSupplementMessages=\n" + tweetSupplementMessages );

		return snapshot;
	}

	@Override
	public ITweetCollection makeTweetCollectionFromURL( final WebDriver driver,
														final IWebDriverUtils driverutils,
														final IInfiniteScrollingActivator infiniteScroller,
														final IArchiveDirectory archiveDirectory,
														final String url,
														final TargetPageType pageType,
														final int numberOfPagesToCheck,
														final int maxTweets ) throws Exception {
		return makeTweetCollectionFromURLInternal( driver,
													driverutils,
													infiniteScroller,
													archiveDirectory,
													url,
													pageType,
													numberOfPagesToCheck,
													maxTweets ).tweetCollection;
	}

	/**
	 * The API was based on 'old Twitter' and it's been mostly resilient despite 'New Twitter', but there are
	 * things that weren't planned for. This is just a temporary fix.
	 * @todo v3: something like a SnapshotBuilder API that hides the differences between new & old and has
	 * a pipeline of processors to deal with parsing JSON, supplementing tweets, etc.
	 * The builder methods in this class will be moved into that API and only the API implementation will
	 * know about webdriver etc.
	 */
	protected TweetCollectionExtended makeTweetCollectionFromURLInternal( final WebDriver driver,
																			final IWebDriverUtils driverutils,
																			final IInfiniteScrollingActivator infiniteScroller,
																			final IArchiveDirectory archiveDirectory,
																			final String url,
																			final TargetPageType pageType,
																			final int numberOfPagesToCheck,
																			final int maxTweets ) throws Exception {
		final JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;

		driver.manage().timeouts().scriptTimeout( TIMEOUT );
		driver.manage().window().maximize();

		logger.info( "makeTweetCollectionFromURL: calling SuedeDenim tweet_retriever script" );

		final JavascriptParams jsParams = new JavascriptParams( url, pageType, debugLevel );
		jsParams.setValue( "scrollerNumTimesToScroll", "" + ( 5 * numberOfPagesToCheck ) );
		jsParams.setValue( "scrollerHeightMultiplier", "0.25" );

		final String suedeDenimRetrieverScript = browserScriptFactory.getScript( "tweet_retriever" ).getScript();

		final List rawInterchangeData = (List) javascriptExecutor.executeAsyncScript( suedeDenimRetrieverScript, jsParams.getMap() );

		final JavascriptInterchangeContainer interchangeContainer = new JavascriptInterchangeContainer( rawInterchangeData, tweetFactory, bundle );

/*
		logger.info( "SuedeDenim tweet_retriever script called, messages=" );
		for ( String message : getBrowserLogs( driver ) ) {
			logger.info( "  " + message );
		}
*/

		logger.info( "makeTweetCollectionFromURL: SuedeDenim tweet_retriever script called, javascript interchange=\n" + interchangeContainer.toDebugString( "  " ) );

		final JavascriptInterchangeMetadata meta = interchangeContainer.getMetadata();

		final ITweetCollection tweetCollection = interchangeContainer.getTweetCollection();
		tweetCollection.setAttribute( "url", url );
		tweetCollection.setAttribute( "numberOfPagesToCheck", "" + numberOfPagesToCheck );
		tweetCollection.setAttribute( "maxTweets", "" + maxTweets );

		return new TweetCollectionExtended( tweetCollection, interchangeContainer );
	}

	protected List<String> supplementTweetCollection( final WebDriver driver,
														final ITweetCollection tweetCollection,
														final List<String> jsonStrings,
														final TargetPageType pageType,
														final String url ) throws Exception {
		final JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;

		driver.manage().timeouts().scriptTimeout( TIMEOUT );

		logger.info( "supplementTweetCollection: calling SuedeDenim json_parser script" );

		final JavascriptParams jsParams = new JavascriptParams( url, pageType, debugLevel );

		final String suedeDenimJSONParserScript = browserScriptFactory.getScript( "json_parser" ).getScript();

		final List rawInterchangeData = (List) javascriptExecutor.executeAsyncScript( suedeDenimJSONParserScript, jsParams.getMap(), jsonStrings );

		final JavascriptInterchangeContainer interchangeContainer = new JavascriptInterchangeContainer( rawInterchangeData, tweetFactory, bundle );

/*
		logger.info( "SuedeDenim json_parser script called, messages=" );
		for ( String message : getBrowserLogs( driver ) ) {
			logger.info( "  " + message );
		}
*/

		logger.info( "\n\n\nsupplementTweetCollection: SuedeDenim json_parser script called, javascript interchange=\n" + interchangeContainer.toDebugString( "  " ) );

		final List<String> tweetSupplementMessages = tweetCollection.supplementFrom( interchangeContainer.getTweetCollection() );

		final ITweetUserCollection users = tweetFactory.makeTweetUserCollection( tweetCollection.getTweetUsers(), Instant.now(), new HashMap<String,String>( 1 ) );

		users.supplementFrom( interchangeContainer.getTweetUserCollection() );

		if ( interchangeContainer.getSupposedQualities() != null ) {
			supplementSupposedQualities( tweetCollection.getTweets(), interchangeContainer.getSupposedQualities() );
		}

		supplementUserIDs( tweetCollection.getTweets() );

		supplementUserHandles( tweetCollection.getTweets() );

		return tweetSupplementMessages;
	}

	@Override
	public IInfiniteScrollingActivator makeInfiniteScrollingActivator( final WebDriver driver,
																		final IWebDriverUtils driverutils,
																		final InfiniteScrollingActivatorType type ) {
		return new InfiniteScrollingActivatorTimelineNT( driver, driverutils );
	}

	@Override
	public IWebDriverUtils makeWebDriverUtils( final WebDriver driver ) {
		return new WebDriverUtils( driver );
	}

	@Override
	public WebDriver makeWebDriver() throws Exception {
		return makeWebDriver( null );
	}

	public WebDriver makeWebDriver( final IBrowserProxy proxy ) throws Exception {
		try {
			final String firefoxBinaryPath = Utils.trimDefault( prefs.getValue( "prefs.firefox_path_app" ), null, true );
			final String firefoxProfilePath = Utils.trimDefault( prefs.getValue( "prefs.firefox_path_profile" ), null, true );
			final String firefoxProfileName = Utils.trimDefault( prefs.getValue( "prefs.firefox_name_profile" ), null, true );

			logger.info( "makeWebDriver: binary=" + firefoxBinaryPath +
							", profile path=" + firefoxProfilePath +
							", profile name=" + firefoxProfileName +
							", proxy=" + proxy );

			WebDriverManager.firefoxdriver().setup();

			final FirefoxOptions firefoxOptions = new FirefoxOptions();

			firefoxOptions.setAcceptInsecureCerts( true );
			firefoxOptions.setPageLoadTimeout( TIMEOUT );
			firefoxOptions.setScriptTimeout( TIMEOUT );
			firefoxOptions.setImplicitWaitTimeout​( TIMEOUT );
			firefoxOptions.setUnhandledPromptBehaviour​​( UnexpectedAlertBehaviour.DISMISS );

			if ( proxy != null ) {
				Proxy seleniumProxy = proxy.getSeleniumProxy();
				if ( seleniumProxy != null ) {
					firefoxOptions.setProxy( seleniumProxy );
				}
			}

			if ( firefoxBinaryPath != null ) {
				firefoxOptions.setBinary( new FirefoxBinary( new File( firefoxBinaryPath ) ) );

				return new FirefoxDriver( firefoxOptions );
			}

			final FirefoxProfile firefoxProfile;

			if ( firefoxProfileName != null ) {
				ProfilesIni profilesIni = new ProfilesIni();
				firefoxProfile = profilesIni.getProfile( firefoxProfileName );
			}
			else if ( firefoxProfilePath != null ) {
				firefoxProfile = new FirefoxProfile( new File( firefoxProfilePath ) );
			}
			else {
				firefoxProfile = new FirefoxProfile();
			}

			setFirefoxProfilePreferences( firefoxProfile );
			addFirefoxExtensions( firefoxProfile );

			firefoxOptions.setLogLevel( FirefoxDriverLogLevel.WARN );
			firefoxOptions.addPreference( "toolkit.asyncshutdown.log", true );
			firefoxOptions.addArguments( "--devtools" );

			firefoxOptions.setProfile( firefoxProfile );

			/*	doesn't work for local drivers
			ClientConfig config = ClientConfig.defaultConfig()
									.readTimeout( TIMEOUT )
									.connectionTimeout( TIMEOUT );

			WebDriver driver = FirefoxDriver.builder()
									.config( config )
									.oneOf( firefoxOptions )
									.build();
			*/

			WebDriver driver = new FirefoxDriver( firefoxOptions );

			driver.manage().timeouts().scriptTimeout( TIMEOUT );
			logger.info( "WEBDRIVER SCRIPT TIMEOUT=" + driver.manage().timeouts().getScriptTimeout() );

			return driver;
		}
		catch ( Exception e ) {
			logger.error( "FAILED TO CREATE WEBDRIVER", e );
			throw e;
		}
	}

	@Override
	public List<String> getBrowserLogs( WebDriver driver ) {
		final List<String> list = new ArrayList<String>( 1000 );

		return list;

/*
		final List<String> list = new ArrayList<String>( 1000 );

		for ( JavaScriptError entry : JavaScriptError.readErrors( driver ) ) {
			list.add( "" + entry );
		}

		return list;
*/

/*
		final List<String> list = new ArrayList<String>( 1000 );

		for ( LogEntry entry : driver.manage().logs().get( LogType.BROWSER ) ) {
			list.add( "" + entry );
		}

		return list;
*/
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

	protected List<String> saveJSONStrings( final List<IBrowserProxyLogEntry> responses, final IArchiveDirectory archiveDirectory ) throws Exception {
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

	protected void setFirefoxProfilePreferences( final FirefoxProfile firefoxProfile ) {
		firefoxProfile.setPreference( "app.update.auto", false );
		firefoxProfile.setPreference( "app.update.enabled", false );
		firefoxProfile.setPreference( "browser.shell.checkDefaultBrowser", false );
		firefoxProfile.setPreference( "devtools.console.stdout.content", true );
		firefoxProfile.setPreference( "devtools.toolbox.selectedTool", "netmonitor" );
		firefoxProfile.setPreference( "devtools.netmonitor.persistlog", true );
		firefoxProfile.setPreference( "devtools.toolbox.footer.height", 120 );

		firefoxProfile.setPreference( "extensions.pocket.enabled", false );
		firefoxProfile.setPreference( "identity.fxaccounts.enabled", false );

		//	fission = different processes for different sites, not needed in this case.
		firefoxProfile.setPreference( "fission.autostart", false );
		firefoxProfile.setPreference( "fission.bfcacheInParent", 0 );
		firefoxProfile.setPreference( "fission.webContentIsolationStrategy", 0 );


		/* for using an external proxy:
		profile.setPreference( "network.proxy.type", 1 );
		profile.setPreference( "network.proxy.http", "127.0.0.1" );
		profile.setPreference( "network.proxy.http_port", 8080 );
		profile.setPreference( "network.proxy.ssl", "127.0.0.1" );
		profile.setPreference( "network.proxy.ssl_port", 8080 );
		*/
	}

	protected void addFirefoxExtensions( final FirefoxProfile firefoxProfile ) {
/* not working with latest FF, need to use something like https://stackoverflow.com/a/45045036
		final File jsErrorCollectorXPIFile = new File( appDirectories.getSubdirectory( "xpi" ), "JSErrorCollector.xpi" );

		if ( jsErrorCollectorXPIFile.exists() ) {
			firefoxProfile.addExtension( JavaScriptError.class, jsErrorCollectorXPIFile );
			logger.info( "Installed Firefox extension from " + jsErrorCollectorXPIFile );
		}
*/
	}

	protected ITweetUser makeTweetUser( final String handle, final String displayName, final String userIDString, final String verifiedText, String avatarURL ) {
		if ( Utils.isEmpty( handle ) ) {
			return tweetFactory.makeTweetUser( TWEETUSER_HANDLE_UNKNOWN );
		}

		long userID = 0;
		try {
			userID = Long.parseLong( userIDString );
		}
		catch ( final Exception e ) {
			logger.info( "can't parse userIDString: " + userIDString + ", handle=" + handle + ", displayName=" + displayName );
			throw e;
		}

		TweetUserVerifiedStatus verifiedStatus = TweetUserVerifiedStatus.UNKNOWN;
		if ( Utils.isEmpty( verifiedText ) ) {
			verifiedStatus = TweetUserVerifiedStatus.NOTVERIFIED;
		}
		else if ( verifiedText.toLowerCase().indexOf( "verified" ) > -1 ) {
			verifiedStatus = TweetUserVerifiedStatus.VERIFIED;
		}

		avatarURL = Utils.trimDefault( avatarURL );

		return tweetFactory.makeTweetUser( handle, userID, displayName, verifiedStatus, avatarURL );
	}
}
