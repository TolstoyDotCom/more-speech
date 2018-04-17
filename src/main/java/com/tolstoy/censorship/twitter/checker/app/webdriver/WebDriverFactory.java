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
import java.util.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.interactions.*;
import com.tolstoy.basic.api.tweet.*;
import com.tolstoy.basic.api.utils.*;
import com.tolstoy.basic.app.utils.*;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.webdriver.*;
import com.tolstoy.censorship.twitter.checker.api.snapshot.*;

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

	private ITweetFactory tweetFactory;
	private ISnapshotFactory snapshotFactory;
	private IPreferences prefs;
	private IResourceBundleWithFormatting bundle;

	public WebDriverFactory( ISnapshotFactory snapshotFactory, ITweetFactory tweetFactory,
									IPreferences prefs, IResourceBundleWithFormatting bundle ) throws Exception {
		this.tweetFactory = tweetFactory;
		this.snapshotFactory = snapshotFactory;
		this.prefs = prefs;
		this.bundle = bundle;
	}

	@Override
	public ISnapshotUserPageTimeline makeSnapshotUserPageTimelineFromURL( WebDriver driver,
																			IWebDriverUtils driverutils,
																			IInfiniteScrollingActivator infiniteScroller,
																			String url,
																			int numberOfPagesToCheck,
																			int maxTweets ) throws Exception {
		ISnapshotUserPageTimeline ret = snapshotFactory.makeSnapshotUserPageTimeline( url, Instant.now() );

		ret.setTweetCollection( makeTweetCollectionFromURL( driver, driverutils, infiniteScroller,
															url, numberOfPagesToCheck, maxTweets ) );
		ret.setComplete( infiniteScroller.getComplete() );

		WebElement tempElem;

		Map<String,String> profileMap = new HashMap<String,String>();
		WebElement profileElem = driver.findElement( By.xpath( driverutils.makeByXPathClassString( "ProfileNav" ) ) );
		profileMap.put( "userid", profileElem.getAttribute( "data-user-id" ) );

		List<WebElement> profileNavLinks = profileElem.findElements( By.tagName( "a" ) );
		for ( WebElement profileNavLink : profileNavLinks ) {
			tempElem = driverutils.safeFindByClass( profileNavLink, "ProfileNav-value" );
			if ( tempElem != null ) {
				profileMap.put( profileNavLink.getAttribute( "data-nav" ), tempElem.getAttribute( "data-count" ) );
			}
		}

		WebElement profileCardMiniElem = driver.findElement( By.xpath( driverutils.makeByXPathClassString( "ProfileCardMini" ) ) );

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
	public ISnapshotUserPageIndividualTweet makeSnapshotUserPageIndividualTweetFromURL( WebDriver driver,
																						IWebDriverUtils driverutils,
																						IInfiniteScrollingActivator infiniteScroller,
																						String url,
																						int numberOfPagesToCheck,
																						int maxTweets ) throws Exception {
		ISnapshotUserPageIndividualTweet ret = snapshotFactory.makeSnapshotUserPageIndividualTweet( url, Instant.now() );

		ITweetCollection tweetCollection = makeTweetCollectionFromURL( driver, driverutils, infiniteScroller,
																		url, numberOfPagesToCheck, maxTweets );
		ret.setComplete( infiniteScroller.getComplete() );

		List<ITweet> tweets = tweetCollection.getTweets();
		if ( tweets == null || tweets.size() < 1 ) {
			throw new RuntimeException( "individual page must have at least one tweet" );
		}

		ITweet individualTweet = tweets.remove( 0 );
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
	public ITweetCollection makeTweetCollectionFromURL( WebDriver driver,
														IWebDriverUtils driverutils,
														IInfiniteScrollingActivator infiniteScroller,
														String url,
														int numberOfPagesToCheck,
														int maxTweets ) throws Exception {
		Utils.delay( DELAY_PRE_TWEETS );

		driver.manage().timeouts().implicitlyWait( IMPLICITWAIT_PRE_ERRORPAGE, TimeUnit.SECONDS );

		List<WebElement> errorPageElems = driver.findElements( By.xpath( driverutils.makeByXPathClassString( "errorpage-body-content" ) ) );
		if ( errorPageElems.size() > 0 ) {
			throw new RuntimeException( "page not found: " + url );
		}

		ITweetCollection collection = tweetFactory.makeTweetCollection();
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

			List<WebElement> lowQualityButtons = driver.findElements( By.xpath( driverutils.makeByXPathClassString( "ThreadedConversation-showMoreThreadsButton" ) ) );
			if ( lowQualityButtons.size() > 0 ) {
				logger.info( "found 'low quality' button" );
				lowQualityButtons.get( 0 ).click();
				Utils.delay( DELAY_POST_CLICK_LOWQUALITY_BUTTON );
				bNoMoreButtons = false;
			}

			List<WebElement> abusiveQualityButtons = driver.findElements( By.xpath( driverutils.makeByXPathClassString( "ThreadedConversation-showMoreThreadsPrompt" ) ) );
			if ( abusiveQualityButtons.size() > 0 ) {
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
		List<WebElement> tweetElems = driver.findElements( By.xpath( driverutils.makeByXPathClassString( "tweet" ) ) );
		logger.info( "found " + tweetElems.size() + " tweets" );

		driver.manage().timeouts().implicitlyWait( IMPLICITWAIT_POST_TWEETS, TimeUnit.SECONDS );

		int tweetCount = 0;
		for ( WebElement tweetElem : tweetElems ) {
			if ( Utils.isEmpty( tweetElem.getAttribute( "data-tweet-id" ) ) ||
					Utils.isEmpty( tweetElem.getAttribute( "data-name" ) ) ) {
				logger.info( "skipping empty tweet, classes:" + tweetElem.getAttribute( "class" ) );
				continue;
			}

			ITweet tweet = tweetFactory.makeTweet();

			loadTweetAttributes( driver, driverutils, tweet, tweetElem );

			tweet.setClasses( new StringList( tweetElem.getAttribute( "class" ) ) );
			tweet.setMentions( new StringList( tweetElem.getAttribute( "data-mentions" ) ) );

			try {
				tweet.setID( Long.parseLong( tweet.getAttribute( "tweetid" ) ) );
			}
			catch ( Exception e ) {
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
	public IInfiniteScrollingActivator makeInfiniteScrollingActivator( WebDriver driver,
																		IWebDriverUtils driverutils,
																		InfiniteScrollingActivatorType type ) {
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
	public IWebDriverUtils makeWebDriverUtils( WebDriver driver ) {
		return new WebDriverUtils( driver );
	}

	@Override
	public WebDriver makeWebDriver() throws Exception {
		FirefoxBinary ffBin;
		FirefoxProfile ffProfile;

		if ( !prefs.isEmpty( "prefs.firefox_path_app" ) && !prefs.isEmpty( "prefs.firefox_path_profile" ) ) {
			ffBin = new FirefoxBinary( new File( prefs.getValue( "prefs.firefox_path_app" ) ) );
			ffProfile = new FirefoxProfile( new File( prefs.getValue( "prefs.firefox_path_profile" ) ) );
			setFirefoxProfilePreferences( ffProfile );

			logger.info( "making WebDriver from bin and profile" );

			return new FirefoxDriver( ffBin, ffProfile );
		}
		else if ( !prefs.isEmpty( "prefs.firefox_path_profile" ) ) {
			ffProfile = new FirefoxProfile( new File( prefs.getValue( "prefs.firefox_path_profile" ) ) );
			setFirefoxProfilePreferences( ffProfile );

			logger.info( "making WebDriver from profile" );

			return new FirefoxDriver( ffProfile );
		}
		else {
			logger.info( "making WebDriver without bin or profile" );

			return new FirefoxDriver();
		}
	}

	protected void setFirefoxProfilePreferences( FirefoxProfile ffProfile ) {
		ffProfile.setPreference( "app.update.auto", false );
		ffProfile.setPreference( "app.update.enabled", false );
		ffProfile.setPreference( "browser.shell.checkDefaultBrowser", false );
	}

	protected void loadTweetAttributes( WebDriver driver, IWebDriverUtils driverutils, ITweet tweet, WebElement tweetElem ) {
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

	protected ITweetUser makeTweetUser( String handle, String displayName, String userIDString, String verifiedText, String avatarURL ) {
		if ( Utils.isEmpty( handle ) ) {
			return tweetFactory.makeTweetUser( TWEETUSER_HANDLE_UNKNOWN );
		}

		long userID = 0;
		try {
			userID = Long.parseLong( userIDString );
		}
		catch ( Exception e ) {
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

