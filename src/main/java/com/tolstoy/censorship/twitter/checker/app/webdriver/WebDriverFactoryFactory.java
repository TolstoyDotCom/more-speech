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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.installation.DebugLevel;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverFactoryFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.WebDriverFactoryType;

public class WebDriverFactoryFactory implements IWebDriverFactoryFactory {
	private static final Logger logger = LogManager.getLogger( WebDriverFactoryFactory.class );

	private final ISnapshotFactory snapshotFactory;
	private final ITweetFactory tweetFactory;
	private final IAppDirectories appDirectories;
	private final IBrowserScriptFactory browserScriptFactory;
	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;
	private final DebugLevel debugLevel;

	public WebDriverFactoryFactory( final ISnapshotFactory snapshotFactory,
								final ITweetFactory tweetFactory,
								final IAppDirectories appDirectories,
								final IBrowserScriptFactory browserScriptFactory,
								final IPreferences prefs,
								final IResourceBundleWithFormatting bundle,
								final DebugLevel debugLevel ) {
		this.snapshotFactory = snapshotFactory;
		this.tweetFactory = tweetFactory;
		this.appDirectories = appDirectories;
		this.browserScriptFactory = browserScriptFactory;
		this.prefs = prefs;
		this.bundle = bundle;
		this.debugLevel = debugLevel;
	}

	@Override
	public IWebDriverFactory makeWebDriverFactory( final WebDriverFactoryType type ) throws Exception {
		if ( WebDriverFactoryType.ORIGINAL.equals( type ) ) {
			return new WebDriverFactory( snapshotFactory, tweetFactory, appDirectories, browserScriptFactory, prefs, bundle );
		}
		else if ( WebDriverFactoryType.ORIGINAL_WITH_JAVASCRIPT.equals( type ) ) {
			return new WebDriverFactoryJS( snapshotFactory, tweetFactory, appDirectories, browserScriptFactory, prefs, bundle );
		}
		else if ( WebDriverFactoryType.NEWTWITTER_WITH_JAVASCRIPT.equals( type ) ) {
			return new WebDriverFactoryNT( snapshotFactory, tweetFactory, appDirectories, browserScriptFactory, prefs, bundle, debugLevel );
		}
		else {
			throw new IllegalArgumentException( "unknown WebDriverFactoryType: " + type );
		}
	}
}
