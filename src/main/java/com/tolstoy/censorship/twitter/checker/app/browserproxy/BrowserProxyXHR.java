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
package com.tolstoy.censorship.twitter.checker.app.browserproxy;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.jbus.JBus;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import com.browserup.harreader.HarReader;
import com.browserup.harreader.HarReaderMode;
import com.browserup.harreader.model.Har;
import com.browserup.harreader.model.HarEntry;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseListener;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyLogEntry;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;
import com.tolstoy.censorship.twitter.checker.app.browserproxy.BrowserProxyLogEntry;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.DecoderResult;

public class BrowserProxyXHR implements IBrowserProxy {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( BrowserProxyXHR.class );

	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;
	private final IBrowserScriptFactory browserScriptFactory;
	private final JBus jbus;

	public BrowserProxyXHR( final IPreferences prefs, final IResourceBundleWithFormatting bundle, final JBus jbus, IBrowserScriptFactory browserScriptFactory ) {
		this.prefs = prefs;
		this.bundle = bundle;
		this.jbus = jbus;
		this.browserScriptFactory = browserScriptFactory;
	}

	@Override
	public boolean start() throws Exception {
		return true;
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public void beginRecording( WebDriver driver, String name ) throws Exception {
		final String ajaxCollectorScript = browserScriptFactory.getScript( "ajax_collector" ).getScript();

		( (JavascriptExecutor) driver ).executeScript( ajaxCollectorScript );

		logger.info( "beginning BrowserProxyXHR recording..." );
	}

	@Override
	public List<IBrowserProxyLogEntry> endRecording( WebDriver driver ) throws Exception {
		logger.info( "end BrowserProxyXHR recording" );

		List<IBrowserProxyLogEntry> list = new ArrayList<IBrowserProxyLogEntry>( 1000 );

		String json = (String) ( (JavascriptExecutor) driver ).executeScript( "return sessionStorage.getItem( 'JSONStrings' );" );
		JSONArray ary = new JSONArray( json );
		for ( Object raw : ary ) {
			try {
				String decodedPayload = new String( Base64.getDecoder().decode( (String) raw ) );
				if ( Utils.isJSON( decodedPayload ) ) {
					list.add( new BrowserProxyLogEntry( decodedPayload, "none", "none", "none" ) );
				}
				else {
					logger.info( "NOT JSON " + decodedPayload );
				}
			}
			catch ( Exception e ) {
			}
		}

		return list;
	}

	@Override
	public Proxy getSeleniumProxy() {
		return null;
	}

	@Override
	public void addBrowserProxyResponseListener( final IBrowserProxyResponseListener l ) {
		jbus.register( new BrowserProxyResponseListenerAdapter( l ) );
	}

	@Override
	public void removeBrowserProxyResponseListener( final IBrowserProxyResponseListener l ) {
	}

	@Override
	public String toString() {
		return "proxy is waiting";
	}
}
