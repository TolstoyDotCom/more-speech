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
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.logging.LogType;
import io.github.bonigarcia.wdm.WebDriverManager;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.TargetPageType;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.TweetUserVerifiedStatus;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.api.utils.IArchiveDirectory;
import com.tolstoy.basic.app.utils.StringList;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyLogEntry;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtensionFactory;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IInfiniteScrollingActivator;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.InfiniteScrollingActivatorType;

public class WebDriverFactory implements IWebDriverFactory {
	private static final Logger logger = LogManager.getLogger( WebDriverFactory.class );

	private static final String TWEETUSER_HANDLE_UNKNOWN = "unknownuser";
	private static final int DELAY_PRE_TWEETS = 10000;
	private static final int DELAY_POST_CLICK_LOWQUALITY_BUTTON = 5000;
	private static final int DELAY_POST_CLICK_ABUSIVEQUALITY_BUTTON = 5000;
	private static final int NUMBER_OF_SCROLL_CHECK_FOR_BUTTONS_CYCLES = 2;
	private static final int IMPLICITWAIT_PRE_ERRORPAGE = 5;
	private static final int IMPLICITWAIT_PRE_SCROLLING = 20;
	private static final int IMPLICITWAIT_PRE_TWEETS = 20;
	private static final int IMPLICITWAIT_POST_TWEETS = 0;

	private final ITweetFactory tweetFactory;
	private final ISnapshotFactory snapshotFactory;
	private final IAppDirectories appDirectories;
	private final IBrowserScriptFactory browserScriptFactory;
	private final IBrowserExtensionFactory browserExtensionFactory;
	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;

	public WebDriverFactory( final ISnapshotFactory snapshotFactory,
								final ITweetFactory tweetFactory,
								final IAppDirectories appDirectories,
								final IBrowserScriptFactory browserScriptFactory,
								final IBrowserExtensionFactory browserExtensionFactory,
								final IPreferences prefs,
								final IResourceBundleWithFormatting bundle ) throws Exception {
		this.tweetFactory = tweetFactory;
		this.snapshotFactory = snapshotFactory;
		this.appDirectories = appDirectories;
		this.browserScriptFactory = browserScriptFactory;
		this.browserExtensionFactory = browserExtensionFactory;
		this.prefs = prefs;
		this.bundle = bundle;
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
		final ISnapshotUserPageTimeline ret = snapshotFactory.makeSnapshotUserPageTimeline( url, Instant.now() );

		ret.setTweetCollection( makeTweetCollectionFromURL( driver, driverutils, infiniteScroller, archiveDirectory,
															url, TargetPageType.TIMELINE, numberOfPagesToCheck, maxTweets ) );
		ret.setComplete( infiniteScroller.getComplete() );

		WebElement tempElem;

		final Map<String,String> profileMap = new HashMap<String,String>();
		final WebElement profileElem = driver.findElement( By.xpath( driverutils.makeByXPathClassString( "ProfileNav" ) ) );
		profileMap.put( "userid", profileElem.getAttribute( "data-user-id" ) );

		final List<WebElement> profileNavLinks = profileElem.findElements( By.tagName( "a" ) );
		for ( final WebElement profileNavLink : profileNavLinks ) {
			tempElem = driverutils.safeFindByClass( profileNavLink, "ProfileNav-value" );
			if ( tempElem != null ) {
				profileMap.put( profileNavLink.getAttribute( "data-nav" ), tempElem.getAttribute( "data-count" ) );
			}
		}

		final WebElement profileCardMiniElem = driver.findElement( By.xpath( driverutils.makeByXPathClassString( "ProfileCardMini" ) ) );

		tempElem = driverutils.safeFindByClass( profileCardMiniElem, "fullname" );
		profileMap.put( "fullname", driverutils.getWebElementText( tempElem ) );

		tempElem = driverutils.safeFindByClass( profileCardMiniElem, "UserBadges" );
		profileMap.put( "verifiedText", driverutils.getWebElementText( tempElem ) );

		tempElem = driverutils.safeFindByClass( profileCardMiniElem, "username", "b" );
		profileMap.put( "handle", driverutils.getWebElementText( tempElem ) );

		tempElem = driverutils.safeFindByClass( profileCardMiniElem, "profile-picture" );
		if ( tempElem != null ) {
			profileMap.put( "avatarURL", Utils.trimDefault( tempElem.getAttribute( "data-url" ) ) );
		}

		ret.setUser( makeTweetUser( profileMap.get( "handle" ),
									profileMap.get( "fullname" ),
									profileMap.get( "userid" ),
									profileMap.get( "verifiedText" ),
									profileMap.get( "avatarURL" ) ) );

		ret.setTitle( driver.getTitle() );

		ret.setNumTotalTweets( Utils.parseIntDefault( profileMap.get( "tweets" ) ) );
		ret.setNumFollowers( Utils.parseIntDefault( profileMap.get( "followers" ) ) );
		ret.setNumFollowing( Utils.parseIntDefault( profileMap.get( "following" ) ) );

		return ret;
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

		final ISnapshotUserPageIndividualTweet ret = snapshotFactory.makeSnapshotUserPageIndividualTweet( url, Instant.now() );

		final ITweetCollection tweetCollection = makeTweetCollectionFromURL( driver, driverutils, infiniteScroller, archiveDirectory,
																				url, TargetPageType.REPLYPAGE, numberOfPagesToCheck, maxTweets );
		ret.setComplete( infiniteScroller.getComplete() );

		final List<ITweet> tweets = tweetCollection.getTweets();
		if ( tweets == null || tweets.isEmpty() ) {
			throw new RuntimeException( "individual page must have at least one tweet" );
		}

		final ITweet individualTweet = tweets.remove( 0 );
		ret.setIndividualTweet( individualTweet );
		ret.setUser( individualTweet.getUser() );
		ret.setTweetID( individualTweet.getID() );

		tweetCollection.setTweets( tweets );
		ret.setTweetCollection( tweetCollection );

		ret.setTitle( driver.getTitle() );

		ret.setNumRetweets( Utils.parseIntDefault( individualTweet.getAttribute( "retweetcount" ) ) );
		ret.setNumLikes( Utils.parseIntDefault( individualTweet.getAttribute( "favoritecount" ) ) );
		ret.setNumReplies( Utils.parseIntDefault( individualTweet.getAttribute( "replycount" ) ) );

		return ret;
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
		Utils.delay( DELAY_PRE_TWEETS );

		driver.manage().timeouts().implicitlyWait( IMPLICITWAIT_PRE_ERRORPAGE, TimeUnit.SECONDS );

		final List<WebElement> errorPageElems = driver.findElements( By.xpath( driverutils.makeByXPathClassString( "errorpage-body-content" ) ) );
		if ( !errorPageElems.isEmpty() ) {
			throw new RuntimeException( "page not found: " + url );
		}

		final ITweetCollection collection = tweetFactory.makeTweetCollection();
		collection.setAttribute( "url", url );
		collection.setAttribute( "numberOfPagesToCheck", "" + numberOfPagesToCheck );
		collection.setAttribute( "maxTweets", "" + maxTweets );

		driver.manage().timeouts().implicitlyWait( IMPLICITWAIT_PRE_SCROLLING, TimeUnit.SECONDS );

		for ( int i = 0; i < NUMBER_OF_SCROLL_CHECK_FOR_BUTTONS_CYCLES; i++ ) {
			logger.info( "about to scroll" );
			infiniteScroller.activate( numberOfPagesToCheck );
			logger.info( "done scrolling phase #" + i );

			boolean bNoMoreButtons = true;

			driver.manage().timeouts().implicitlyWait( IMPLICITWAIT_PRE_TWEETS, TimeUnit.SECONDS );

			final List<WebElement> lowQualityButtons = driver.findElements( By.xpath( driverutils.makeByXPathClassString( "ThreadedConversation-showMoreThreadsButton" ) ) );
			if ( !lowQualityButtons.isEmpty() ) {
				logger.info( "found 'low quality' button" );
				lowQualityButtons.get( 0 ).click();
				Utils.delay( DELAY_POST_CLICK_LOWQUALITY_BUTTON );
				bNoMoreButtons = false;
			}

			final List<WebElement> abusiveQualityButtons = driver.findElements( By.xpath( driverutils.makeByXPathClassString( "ThreadedConversation-showMoreThreadsPrompt" ) ) );
			if ( !abusiveQualityButtons.isEmpty() ) {
				logger.info( "found 'abusive quality' button" );
				abusiveQualityButtons.get( 0 ).click();
				Utils.delay( DELAY_POST_CLICK_ABUSIVEQUALITY_BUTTON );
				bNoMoreButtons = false;
			}

			if ( bNoMoreButtons ) {
				logger.info( "found no lowquality/abusivequality buttons in phase #" + i );
				break;
			}
		}

		logger.info( "looking for tweets..." );
		final List<WebElement> tweetElems = driver.findElements( By.xpath( driverutils.makeByXPathClassString( "tweet" ) ) );
		logger.info( "found " + tweetElems.size() + " tweets" );

		driver.manage().timeouts().implicitlyWait( IMPLICITWAIT_POST_TWEETS, TimeUnit.SECONDS );

		int tweetCount = 0;
		for ( final WebElement tweetElem : tweetElems ) {
			if ( Utils.isEmpty( tweetElem.getAttribute( "data-tweet-id" ) ) ||
					Utils.isEmpty( tweetElem.getAttribute( "data-name" ) ) ) {
				logger.info( "skipping empty tweet, classes:" + tweetElem.getAttribute( "class" ) );
				continue;
			}

			final ITweet tweet = tweetFactory.makeTweet();

			loadTweetAttributes( driver, driverutils, tweet, tweetElem );

			tweet.setClasses( new StringList( tweetElem.getAttribute( "class" ) ) );
			tweet.setMentions( new StringList( tweetElem.getAttribute( "data-mentions" ) ) );

			try {
				tweet.setID( Long.parseLong( tweet.getAttribute( "tweetid" ) ) );
			}
			catch ( final Exception e ) {
				logger.info( "can't parse tweetid attribute, tweet is " + tweet );
				throw e;
			}

			tweet.setUser( makeTweetUser( tweet.getAttribute( "screenname" ),
											tweet.getAttribute( "name" ),
											tweet.getAttribute( "userid" ),
											tweet.getAttribute( "verifiedText" ),
											tweet.getAttribute( "avatarURL" ) ) );

			collection.addTweet( tweet );

			tweetCount++;

			if ( tweetCount % 10 == 0 ) {
				logger.info( "retrieving tweet #" + tweetCount );
			}

			if ( maxTweets != 0 && tweetCount >= maxTweets ) {
				break;
			}
		}

		return collection;
	}

	@Override
	public IInfiniteScrollingActivator makeInfiniteScrollingActivator( final WebDriver driver,
																		final IWebDriverUtils driverutils,
																		final InfiniteScrollingActivatorType type ) {
		if ( type == InfiniteScrollingActivatorType.TIMELINE ) {
			return new InfiniteScrollingActivatorTimeline( driver, driverutils );
		}
		else if ( type == InfiniteScrollingActivatorType.INDIVIDUAL ) {
			return new InfiniteScrollingActivatorIndividual( driver, driverutils );
		}
		else {
			throw new IllegalArgumentException( bundle.getString( "exc_unknown_scroller_type", "" + type ) );
		}
	}

	@Override
	public IWebDriverUtils makeWebDriverUtils( final WebDriver driver ) {
		return new WebDriverUtils( driver );
	}

	@Override
	public WebDriver makeWebDriver() throws Exception {
		return makeWebDriver( null );
	}

	@Override
	public WebDriver makeWebDriver( final IBrowserProxy proxy ) throws Exception {
		try {
			//logger.info( "original=" + prefs.getValue( "prefs.firefox_path_geckodriver" ) );
			//System.setProperty( "webdriver.gecko.driver", prefs.getValue( "prefs.firefox_path_geckodriver" ) );

			final DesiredCapabilities capabilities = new DesiredCapabilities();
			capabilities.setAcceptInsecureCerts( true );

			final LoggingPreferences loggingPreferences = new LoggingPreferences();
			loggingPreferences.enable( LogType.BROWSER, Level.INFO );
			//	LOGGING_PREFS has been removed, try plain text even though it might not work with Firefox
			//capabilities.setCapability( CapabilityType.LOGGING_PREFS, loggingPreferences );
			capabilities.setCapability( "goog:loggingPrefs", loggingPreferences );

			if ( proxy != null ) {
				Proxy seleniumProxy = proxy.getSeleniumProxy();
				if ( seleniumProxy != null ) {
					logger.info( "WebDriverFactory.makeWebDriver: using browser proxy" );
					capabilities.setCapability( CapabilityType.PROXY, seleniumProxy );
				}
			}

			logger.info( "WebDriverFactory.makeWebDriver: webdriver capabilities=" + capabilities );

			final FirefoxOptions firefoxOptions = new FirefoxOptions( capabilities );

			if ( !prefs.isEmpty( "prefs.firefox_path_app" ) ) {
				String firefoxBinaryPath = prefs.getValue( "prefs.firefox_path_app" );

				logger.info( "WebDriverFactory.makeWebDriver: making WebDriver binary from " + firefoxBinaryPath );

				//WebDriverManager.firefoxdriver().browserPath( firefoxBinaryPath ).setup();
				WebDriverManager.firefoxdriver().setup();
				firefoxOptions.setBinary( new FirefoxBinary( new File( firefoxBinaryPath ) ) );

				return new FirefoxDriver( firefoxOptions );
			}
			else {
				WebDriverManager.firefoxdriver().setup();
			}

			final FirefoxProfile firefoxProfile;

			if ( !prefs.isEmpty( "prefs.firefox_path_profile" ) ) {
				String firefoxProfilePath = prefs.getValue( "prefs.firefox_path_profile" );

				logger.info( "WebDriverFactory.makeWebDriver: making WebDriver profile from " + firefoxProfilePath );

				firefoxProfile = new FirefoxProfile( new File( firefoxProfilePath ) );
			}
			else {
				logger.info( "WebDriverFactory.makeWebDriver: making empty WebDriver profile" );
				firefoxProfile = new FirefoxProfile();
			}

			setFirefoxProfilePreferences( firefoxProfile );
			addFirefoxExtensions( firefoxProfile );

			logger.info( "WebDriverFactory.makeWebDriver: making WebDriver from profile" );

			firefoxOptions.setProfile( firefoxProfile );

			return new FirefoxDriver( firefoxOptions );
		}
		catch ( Exception e ) {
			logger.error( "FAILED TO CREATE WEBDRIVER", e );
			throw e;
		}
	}

	@Override
	public List<String> getBrowserLogs( WebDriver driver ) {
		final List<String> list = new ArrayList<String>( 1000 );

		for ( LogEntry entry : driver.manage().logs().get( LogType.BROWSER ) ) {
			list.add( "" + entry );
		}

		return list;
	}

	protected void setFirefoxProfilePreferences( final FirefoxProfile ffProfile ) {
		ffProfile.setPreference( "app.update.auto", false );
		ffProfile.setPreference( "app.update.enabled", false );
		ffProfile.setPreference( "browser.shell.checkDefaultBrowser", false );
	}

	protected void loadTweetAttributes( final WebDriver driver, final IWebDriverUtils driverutils, final ITweet tweet, final WebElement tweetElem ) {
		WebElement tempElem;

		tweet.setAttribute( "tweetid", tweetElem.getAttribute( "data-tweet-id" ) );
		tweet.setAttribute( "youblock", tweetElem.getAttribute( "data-you-block" ) );
		tweet.setAttribute( "followsyou", tweetElem.getAttribute( "data-follows-you" ) );
		tweet.setAttribute( "youfollow", tweetElem.getAttribute( "data-you-follow" ) );
		tweet.setAttribute( "tweetnonce", tweetElem.getAttribute( "data-tweet-nonce" ) );
		tweet.setAttribute( "conversationid", tweetElem.getAttribute( "data-conversation-id" ) );
		tweet.setAttribute( "permalinkpath", tweetElem.getAttribute( "data-permalink-path" ) );
		tweet.setAttribute( "itemid", tweetElem.getAttribute( "data-item-id" ) );
		tweet.setAttribute( "tweetstatinitialized", tweetElem.getAttribute( "data-tweet-stat-initialized" ) );
		tweet.setAttribute( "disclosuretype", tweetElem.getAttribute( "data-disclosure-type" ) );
		tweet.setAttribute( "hascards", tweetElem.getAttribute( "data-has-cards" ) );
		tweet.setAttribute( "replytousersjson", tweetElem.getAttribute( "data-reply-to-users-json" ) );
		tweet.setAttribute( "hasparenttweet", tweetElem.getAttribute( "data-has-parent-tweet" ) );
		tweet.setAttribute( "isreplyto", tweetElem.getAttribute( "data-is-reply-to" ) );
		tweet.setAttribute( "retweetid", tweetElem.getAttribute( "data-retweet-id" ) );
		tweet.setAttribute( "quality", tweetElem.getAttribute( "data-conversation-section-quality" ) );
		tweet.setAttribute( "componentcontext", tweetElem.getAttribute( "data-component-context" ) );

		tweet.setAttribute( "userid", tweetElem.getAttribute( "data-user-id" ) );
		tweet.setAttribute( "name", tweetElem.getAttribute( "data-name" ) );
		tweet.setAttribute( "screenname", tweetElem.getAttribute( "data-screen-name" ) );

		tempElem = driverutils.safeFindByClass( tweetElem, "avatar" );
		if ( tempElem != null ) {
			tweet.setAttribute( "avatarURL", tempElem.getAttribute( "src" ) );
		}

		tempElem = driverutils.safeFindByClass( tweetElem, "fullname" );
		tweet.setAttribute( "fullname", driverutils.getWebElementText( tempElem ) );

		tempElem = driverutils.safeFindByClass( tweetElem, "UserBadges" );
		tweet.setAttribute( "verifiedText", driverutils.getWebElementText( tempElem ) );

		tempElem = driverutils.safeFindByClass( tweetElem, "username" );
		tweet.setAttribute( "username", driverutils.getWebElementText( tempElem ) );

		tempElem = driverutils.safeFindByClass( tweetElem, "js-relative-timestamp" );
		if ( tempElem != null ) {
			tweet.setAttribute( "time", tempElem.getAttribute( "data-time" ) );
		}
		else {
			tempElem = driverutils.safeFindByClass( tweetElem, "js-short-timestamp" );
			if ( tempElem != null ) {
				tweet.setAttribute( "time", tempElem.getAttribute( "data-time" ) );
			}
			else {
				tempElem = driverutils.safeFindByClass( tweetElem, "_timestamp" );
				if ( tempElem != null ) {
					tweet.setAttribute( "time", tempElem.getAttribute( "data-time" ) );
				}
			}
		}

		tempElem = driverutils.safeFindByClass( tweetElem, "tweet-text" );
		tweet.setAttribute( "tweettext", driverutils.getWebElementText( tempElem ) );
		if ( tempElem != null ) {
			tweet.setAttribute( "tweethtml", tempElem.getAttribute( "innerHTML" ) );
			tweet.setAttribute( "tweetlanguage", tempElem.getAttribute( "lang" ) );
		}
		if ( Utils.isEmpty( tweet.getAttribute( "tweethtml" ) ) ) {
			tweet.setAttribute( "tweethtml", "" );
		}
		if ( Utils.isEmpty( tweet.getAttribute( "tweetlanguage" ) ) ) {
			tweet.setAttribute( "tweetlanguage", "en" );
		}

		tempElem = driverutils.safeFindByClass( tweetElem, "ReplyingToContextBelowAuthor", "a" );
		if ( tempElem != null ) {
			tweet.setAttribute( "repliedtohandle", Utils.extractHandle( tempElem.getAttribute( "href" ) ) );
			tweet.setAttribute( "repliedtouserid", tempElem.getAttribute( "data-user-id" ) );
		}

			//	inside AdaptiveMedia-container
		tempElem = driverutils.safeFindByClass( tweetElem, "AdaptiveMedia-photoContainer", "img" );
		if ( tempElem != null ) {
			tweet.setAttribute( "photourl", tempElem.getAttribute( "src" ) );
		}

			//	inside AdaptiveMedia-container
		tempElem = driverutils.safeFindByClass( tweetElem, "PlayableMedia-player" );
		if ( tempElem != null ) {
			tweet.setAttribute( "videothumburl", Utils.extractFirstLink( tempElem.getAttribute( "style" ) ) );
		}

		tempElem = driverutils.safeFindByClass( tweetElem, "ProfileTweet-action--reply", "span" );
		if ( tempElem != null ) {
			tweet.setAttribute( "replycount", tempElem.getAttribute( "data-tweet-stat-count" ) );
		}

		tempElem = driverutils.safeFindByClass( tweetElem, "ProfileTweet-action--retweet", "span" );
		if ( tempElem != null ) {
			tweet.setAttribute( "retweetcount", tempElem.getAttribute( "data-tweet-stat-count" ) );
		}
		else {
			tempElem = driverutils.safeFindByClass( tweetElem, "request-retweeted-popup" );
			if ( tempElem != null ) {
				tweet.setAttribute( "retweetcount", tempElem.getAttribute( "data-tweet-stat-count" ) );
			}
		}

		tempElem = driverutils.safeFindByClass( tweetElem, "ProfileTweet-action--favorite", "span" );
		if ( tempElem != null ) {
			tweet.setAttribute( "favoritecount", tempElem.getAttribute( "data-tweet-stat-count" ) );
		}
		else {
			tempElem = driverutils.safeFindByClass( tweetElem, "request-favorited-popup" );
			if ( tempElem != null ) {
				tweet.setAttribute( "favoritecount", tempElem.getAttribute( "data-tweet-stat-count" ) );
			}
		}

		tempElem = driverutils.safeFindByClass( tweetElem, "QuoteTweet-link" );
		if ( tempElem != null ) {
			tweet.setAttribute( "innertweetid", tempElem.getAttribute( "data-conversation-id" ) );
			tweet.setAttribute( "innertweetrawhref", tempElem.getAttribute( "href" ) );
		}

		tempElem = driverutils.safeFindByClass( tweetElem, "js-stream-item", "span" );
		if ( tempElem != null ) {
			tweet.setAttribute( "suggestionjson", tempElem.getAttribute( "data-suggestion-json" ) );
		}
	}

	protected void addFirefoxExtensions( final FirefoxProfile firefoxProfile ) {
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

	protected IAppDirectories getAppDirectories() {
		return appDirectories;
	}

	protected IBrowserScriptFactory getBrowserScriptFactory() {
		return browserScriptFactory;
	}

	protected IPreferences getPreferences() {
		return prefs;
	}

	protected IResourceBundleWithFormatting getResourceBundleWithFormatting() {
		return bundle;
	}
}
