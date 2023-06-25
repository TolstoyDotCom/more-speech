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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseListener;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyLogEntry;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.DecoderResult;

public class BrowserProxyHAR implements IBrowserProxy {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( BrowserProxyHAR.class );

	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;
	private final IBrowserScriptFactory browserScriptFactory;
	private final JBus jbus;

	public BrowserProxyHAR( final IPreferences prefs, final IResourceBundleWithFormatting bundle, final JBus jbus, IBrowserScriptFactory browserScriptFactory ) {
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
		logger.info( "beginning recording..." );
	}

	@Override
	public List<IBrowserProxyLogEntry> endRecording( WebDriver driver ) throws Exception {
		logger.info( "end recording" );

		final String suedeDenimHARRetrieverScript = browserScriptFactory.getScript( "har_retriever" ).getScript();
		logger.info( "got script, calling it" );

		JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;

		Map<String,Object> resultsData = (Map<String,Object>) javascriptExecutor.executeAsyncScript( suedeDenimHARRetrieverScript, new HashMap<String,Object>() );
		logger.info( "called script" );

		List<Map<String,Object>> entries = (List<Map<String,Object>>) resultsData.get( "entries" );

		List<IBrowserProxyLogEntry> list = new ArrayList<IBrowserProxyLogEntry>( entries != null ? entries.size() : 1 );
		if ( entries == null ) {
			logger.info( "WARNING: EMPTY LIST" );
			return list;
		}

		for ( Map<String,Object> entry : entries ) {
			IBrowserProxyLogEntry logEntry = new BrowserProxyLogEntry( entry );
			logger.info( "  created log entry=" + logEntry );
			list.add( logEntry );
		}

		logger.info( "returning recording" );

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
