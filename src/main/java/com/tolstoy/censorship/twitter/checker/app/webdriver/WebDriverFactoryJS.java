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
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
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

public class WebDriverFactoryJS extends WebDriverFactory implements IWebDriverFactory {
	private static final Logger logger = LogManager.getLogger( WebDriverFactoryJS.class );

	private String attributesScript, tweetScript;

	public WebDriverFactoryJS( ISnapshotFactory snapshotFactory, ITweetFactory tweetFactory,
									IPreferences prefs, IResourceBundleWithFormatting bundle ) throws Exception {
		super( snapshotFactory, tweetFactory, prefs, bundle );

		attributesScript = IOUtils.toString( getClass().getResource( "/attributes.js" ), StandardCharsets.UTF_8 );
		tweetScript = IOUtils.toString( getClass().getResource( "/tweet.js" ), StandardCharsets.UTF_8 );
	}

	@Override
	protected void loadTweetAttributes( WebDriver driver, IWebDriverUtils driverutils, ITweet tweet, WebElement tweetElem ) {
		WebElement tempElem;

		JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;

		Map<String,String> attributesMap = makeStringMap( javascriptExecutor.executeScript( attributesScript, tweetElem ) );

		Map<String,String> tweetMap = makeStringMap( javascriptExecutor.executeScript( tweetScript, tweetElem ) );

		tweetMap.putAll( attributesMap );

		tweet.setAttributes( tweetMap );

		if ( Utils.isEmpty( tweet.getAttribute( "tweethtml" ) ) ) {
			tweet.setAttribute( "tweethtml", "" );
		}
		if ( Utils.isEmpty( tweet.getAttribute( "tweetlanguage" ) ) ) {
			tweet.setAttribute( "tweetlanguage", "en" );
		}

		if ( Utils.isEmpty( tweet.getAttribute( "repliedtohandle" ) ) ) {
			tweet.setAttribute( "repliedtohandle", "" );
		}
		else {
			tweet.setAttribute( "repliedtohandle", Utils.extractHandle( tweet.getAttribute( "repliedtohandle" ) ) );
		}

		if ( Utils.isEmpty( tweet.getAttribute( "videothumburl" ) ) ) {
			tweet.setAttribute( "videothumburl", "" );
		}
		else {
			tweet.setAttribute( "videothumburl", Utils.extractFirstLink( tweet.getAttribute( "videothumburl" ) ) );
		}
	}

	private Map<String,String> makeStringMap( Object x ) {
		if ( !( x instanceof Map ) ) {
			throw new RuntimeException( "webdriver JS returned something other than a Map: " + x );
		}

		@SuppressWarnings("unchecked")
		Map<String,Object> temp = (Map<String,Object>) x;

		Map<String,String> ret = new HashMap<String,String>();

		for ( String key : temp.keySet() ) {
			Object obj = temp.get( key );
			if ( obj == null ) {
				ret.put( key, "" );
			}
			else {
				ret.put( key, obj.toString() );
			}
		}

		return ret;
	}
}

