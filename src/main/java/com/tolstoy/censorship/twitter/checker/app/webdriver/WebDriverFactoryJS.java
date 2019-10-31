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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;

public class WebDriverFactoryJS extends WebDriverFactory implements IWebDriverFactory {
	private static final Logger logger = LogManager.getLogger( WebDriverFactoryJS.class );

	private final String attributesScript, tweetScript;

	public WebDriverFactoryJS( final ISnapshotFactory snapshotFactory,
								final ITweetFactory tweetFactory,
								final IAppDirectories appDirectories,
								final IBrowserScriptFactory browserScriptFactory,
								final IPreferences prefs,
								final IResourceBundleWithFormatting bundle ) throws Exception {
		super( snapshotFactory, tweetFactory, appDirectories, browserScriptFactory, prefs, bundle );

		attributesScript = IOUtils.toString( getClass().getResource( "/attributes.js" ), StandardCharsets.UTF_8 );
		tweetScript = IOUtils.toString( getClass().getResource( "/tweet.js" ), StandardCharsets.UTF_8 );
	}

	@Override
	protected void loadTweetAttributes( final WebDriver driver, final IWebDriverUtils driverutils, final ITweet tweet, final WebElement tweetElem ) {
		final WebElement tempElem;

		final JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;

		final Map<String,String> attributesMap = makeStringMap( javascriptExecutor.executeScript( attributesScript, tweetElem ) );

		final Map<String,String> tweetMap = makeStringMap( javascriptExecutor.executeScript( tweetScript, tweetElem ) );

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

	private Map<String,String> makeStringMap( final Object x ) {
		if ( !( x instanceof Map ) ) {
			throw new RuntimeException( "webdriver JS returned something other than a Map: " + x );
		}

		@SuppressWarnings("unchecked")
		final
		Map<String,Object> temp = (Map<String,Object>) x;

		final Map<String,String> ret = new HashMap<String,String>();

		for ( final String key : temp.keySet() ) {
			final Object obj = temp.get( key );
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

