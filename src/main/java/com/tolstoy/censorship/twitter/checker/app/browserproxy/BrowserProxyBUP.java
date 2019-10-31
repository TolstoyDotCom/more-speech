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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.jbus.JBus;
import org.openqa.selenium.Proxy;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.client.ClientUtil;
import com.browserup.bup.filters.ResponseFilter;
import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;
import com.browserup.bup.proxy.CaptureType;
import com.browserup.harreader.model.Har;
import com.browserup.harreader.model.HarLog;
import com.browserup.harreader.model.HarEntry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseListener;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyLogEntry;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.DecoderResult;

public class BrowserProxyBUP implements IBrowserProxy, ResponseFilter {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( BrowserProxyBUP.class );

	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;
	private BrowserUpProxy proxy = null;
	private Proxy seleniumProxy = null;
	private JBus jbus = null;

	public BrowserProxyBUP( final IPreferences prefs, final IResourceBundleWithFormatting bundle, final JBus jbus ) {
		this.prefs = prefs;
		this.bundle = bundle;
		this.jbus = jbus;
	}

	@Override
	public boolean start() throws Exception {
		if ( proxy != null && seleniumProxy != null ) {
			return false;
		}

		try {
			proxy = new BrowserUpProxyServer();

			proxy.addResponseFilter( this );

			proxy.start();

			seleniumProxy = ClientUtil.createSeleniumProxy( proxy );

			return true;
		}
		catch ( final Exception e ) {
			proxy = null;
			seleniumProxy = null;
			logger.error( "PROXY ERROR", e );
			throw e;
		}
	}

	@Override
	public void stop() throws Exception {
		proxy.stop();
	}

	@Override
	public void beginRecording( String name ) {
		logger.info( "beginning recording..." );

		Har unusedHAR = proxy.endHar();

		proxy.enableHarCaptureTypes( CaptureType.REQUEST_HEADERS,
										CaptureType.REQUEST_CONTENT,
										CaptureType.RESPONSE_HEADERS,
										CaptureType.RESPONSE_CONTENT,
										CaptureType.RESPONSE_BINARY_CONTENT );

		proxy.newHar( name );
	}

	@Override
	public List<IBrowserProxyLogEntry> endRecording() {
		logger.info( "end recording" );

		Har har = proxy.endHar();
		HarLog log = har.getLog();

		List<IBrowserProxyLogEntry> list = new ArrayList<IBrowserProxyLogEntry>( 100 );

		for ( HarEntry entry : log.getEntries() ) {
			list.add( new BrowserProxyLogEntry( entry ) );
		}

		return list;
	}

	@Override
	public Proxy getSeleniumProxy() {
		return seleniumProxy;
	}

	@Override
	public void filterResponse( final HttpResponse response, final HttpMessageContents contents, final HttpMessageInfo messageInfo ) {
		DecoderResult res = response.getDecoderResult();

		try {
			final BrowserProxyResponseEventBUP event = new BrowserProxyResponseEventBUP( response, contents, messageInfo );

			jbus.post( event );
		}
		catch ( final Exception e ) {
			logger.error( e );
		}
	}

	@Override
	public void addBrowserProxyResponseListener( final IBrowserProxyResponseListener l ) {
		jbus.register( new BrowserProxyResponseListenerAdapter( l ) );
	}

	@Override
	public void removeBrowserProxyResponseListener( final IBrowserProxyResponseListener l ) {
	}
}
