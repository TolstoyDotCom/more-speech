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
package com.tolstoy.censorship.twitter.checker.app.browserproxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.jbus.JBus;

import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyFactory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.BrowserDataRecorderType;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;

public class BrowserProxyFactory implements IBrowserProxyFactory {
	private static final Logger logger = LogManager.getLogger( BrowserProxyFactory.class );

	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;
	private final JBus jbus;
	private final IBrowserScriptFactory browserScriptFactory;

	public BrowserProxyFactory( final IPreferences prefs,
								final IResourceBundleWithFormatting bundle,
								final JBus jbus,
								final IBrowserScriptFactory browserScriptFactory ) {
		this.prefs = prefs;
		this.bundle = bundle;
		this.jbus = jbus;
		this.browserScriptFactory = browserScriptFactory;
	}

	@Override
	public IBrowserProxy makeBrowserDataRecorder() throws Exception {
		return new BrowserProxyHAR( prefs, bundle, jbus, browserScriptFactory );
		//return new BrowserProxyBMP( prefs, bundle, jbus );
		//return new BrowserProxyBUP( prefs, bundle, jbus );
		//return new BrowserProxyXHR( prefs, bundle, jbus, browserScriptFactory );
	}

	@Override
	public IBrowserProxy makeBrowserDataRecorder( BrowserDataRecorderType preferredType ) throws Exception {
		switch ( preferredType ) {
			case BMP_PROXY:
				throw new IllegalArgumentException( "BrowserProxyBMP no longer supported" );

			case BUP_PROXY:
				return new BrowserProxyBUP( prefs, bundle, jbus );

			case HAR_EXPORT_EXTENSION:
				return new BrowserProxyHAR( prefs, bundle, jbus, browserScriptFactory );

			default:
				return new BrowserProxyXHR( prefs, bundle, jbus, browserScriptFactory );
		}
	}
}
