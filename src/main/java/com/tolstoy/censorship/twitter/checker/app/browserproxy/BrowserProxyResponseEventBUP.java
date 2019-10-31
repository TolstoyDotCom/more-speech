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

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseEvent;

import io.netty.handler.codec.http.HttpResponse;

public class BrowserProxyResponseEventBUP implements IBrowserProxyResponseEvent {
	private final Charset charset;
	private final String text, contentType, requestURL;
	private final int responseCode;
	private final byte[] binaryData;

	public BrowserProxyResponseEventBUP( final HttpResponse response, final HttpMessageContents contents, final HttpMessageInfo messageInfo )
			throws Exception {
		this.responseCode = response.getStatus().code();
		this.text = contents.getTextContents();
		this.binaryData = contents.getBinaryContents();
		this.contentType = contents.getContentType();
		this.charset = contents.getCharset();
		this.requestURL = messageInfo.getOriginalUrl();
	}

	@Override
	public boolean isJSON() {
		String test = getText();
		if ( test == null || test.length() < 3 ) {
			return false;
		}

		test = test.trim();
		if ( test.length() < 3 || !( test.charAt( 0 ) == '[' || test.charAt( 0 ) == '{' ) ) {
			return false;
		}

		if ( contentType == null || !( contentType.indexOf( "javascript" ) > -1 || contentType.indexOf( "json" ) > -1 ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int getResponseCode() {
		return responseCode;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public long getTextLength() {
		return StringUtils.length( getText() );
	}

	@Override
	public byte[] getBinary() {
		return binaryData;
	}

	@Override
	public long getBinaryLength() {
		return binaryData != null ? binaryData.length : 0;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public Charset getCharset() {
		return charset;
	}

	@Override
	public String getRequestURL() {
		return requestURL;
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.appendSuper( super.toString() )
		.append( "url", getRequestURL() )
		.append( "content type", getContentType() )
		.append( "charset", getCharset() )
		.append( "code", getResponseCode() )
		.append( "text len", getTextLength() )
		.append( "binary len", getBinaryLength() )
		.toString();
	}
}
