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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.browserup.harreader.model.HarEntry;
import com.browserup.harreader.model.HarRequest;
import com.browserup.harreader.model.HarResponse;
import com.browserup.harreader.model.HarContent;
import com.browserup.harreader.model.HarHeader;

import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyLogEntry;

final class BrowserProxyLogEntry implements IBrowserProxyLogEntry {
	private static final Logger logger = LogManager.getLogger( BrowserProxyLogEntry.class );

	private final Map<String,String> map;
	private final List<String> headers;

	public BrowserProxyLogEntry( final HarEntry entry ) {
		final HarRequest request = entry.getRequest();

		final HarResponse response = entry.getResponse();
		final HarContent content = response.getContent();

		this.headers = new ArrayList<String>( 20 );
		for ( HarHeader header : response.getHeaders() ) {
			headers.add( header.getName() + ": " + header.getValue() );
		}

		this.map = new HashMap<String,String>( 10 );

		this.map.put( "url", request.getUrl() );

		this.map.put( "content", content.getText() );
		this.map.put( "content_comment", "" + content.getComment() );
		this.map.put( "content_compression", "" + content.getCompression() );
		this.map.put( "content_encoding", "" + content.getEncoding() );
		this.map.put( "content_mimetype", "" + content.getMimeType() );
		this.map.put( "content_size", "" + content.getSize() );

		this.map.put( "response_additional", "" + response.getAdditional() );
		this.map.put( "response_bodysize", "" + response.getBodySize() );
		this.map.put( "response_comment", response.getComment() );
		this.map.put( "response_redirecturl", response.getRedirectURL() );
		this.map.put( "response_status", "response_status " + response.getStatus() );
		this.map.put( "response_statustext", response.getStatusText() );
	}

	public BrowserProxyLogEntry( final Map<String,Object> entry ) {
		Map<String,Object> request = (Map<String,Object>) entry.get( "request" );
		Map<String,Object> response = (Map<String,Object>) entry.get( "response" );
		Map<String,Object> content = (Map<String,Object>) response.get( "content" );

		this.headers = new ArrayList<String>( 20 );
		for ( Map<String,Object> header : (List<Map<String,Object>>) response.get( "headers" ) ) {
			headers.add( header.get( "name" ) + ": " + header.get( "value" ) );
		}

		this.map = new HashMap<String,String>( 10 );

		this.map.put( "url", "" + request.get( "url" ) );

		this.map.put( "content", "" + content.get( "text" ) );
		this.map.put( "content_comment", content.containsKey( "comment" ) ? "" + content.get( "comment" ) : "" );
		this.map.put( "content_compression", content.containsKey( "compression" ) ? "" + content.get( "compression" ) : "" );
		this.map.put( "content_encoding", content.containsKey( "encoding" ) ? "" + content.get( "encoding" ) : "" );
		this.map.put( "content_mimetype", "" + content.get( "mimeType" ) );
		this.map.put( "content_size", "" + content.get( "size" ) );

		this.map.put( "response_additional", "" );
		this.map.put( "response_bodysize", "" + response.get( "bodySize" ) );
		this.map.put( "response_comment", response.containsKey( "comment" ) ? "" + response.get( "comment" ) : "" );
		this.map.put( "response_redirecturl", "" + response.get( "redirectURL" ) );
		this.map.put( "response_status", "response_status " + response.get( "status" ) );
		this.map.put( "response_statustext", "" + response.get( "statusText" ) );
	}

	@Override
	public String getURL() {
		return MapUtils.getString( map, "url", "" );
	}

	@Override
	public String getContent() {
		return MapUtils.getString( map, "content", "" );
	}

	@Override
	public String getCompression() {
		return MapUtils.getString( map, "content_compression", "" );
	}

	@Override
	public String getEncoding() {
		return MapUtils.getString( map, "content_encoding", "" );
	}

	@Override
	public String getMimeType() {
		return MapUtils.getString( map, "content_mimetype", "" );
	}

	@Override
	public List<String> getHeaders() {
		return headers;
	}

	@Override
	public String toString() {
		return "content=" + StringUtils.substring( getContent(), 0, 30 ) + " of type " + getMimeType() + " from URL=" + getURL();
	}
}
