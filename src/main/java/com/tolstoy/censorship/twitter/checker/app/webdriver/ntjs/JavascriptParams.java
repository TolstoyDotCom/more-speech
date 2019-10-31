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
package com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs;

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.collections4.MapUtils;

import com.tolstoy.basic.api.tweet.TargetPageType;
import com.tolstoy.basic.api.installation.DebugLevel;
import com.tolstoy.basic.app.utils.Utils;

public final class JavascriptParams {
	private static final Logger logger = LogManager.getLogger( JavascriptParams.class );

	private final Map<String, String> map;

	public JavascriptParams( final String url, final TargetPageType pageType, final DebugLevel debugLevel ) {
		this.map = new HashMap<String, String>( 20 );

		this.map.put( "url", url );
		this.map.put( "pageType", pageType.getKey() );
		this.map.put( "debugLevel", "" + debugLevel.getAsInt() );

		this.map.put( "mainClockDelay", "1500" );

		this.map.put( "tweetSelector", "main article" );

		this.map.put( "scrollerNumTimesToScroll", "20" );
		this.map.put( "scrollerHeightMultiplier", "0.25" );

		this.map.put( "checkLoggedInDelay", "5" );

		this.map.put( "maxWaitForTweetSelector", "30" );

		this.map.put( "hiddenRepliesAfterClickIterations", "2" );
		this.map.put( "hiddenRepliesAttemptIterations", "10" );
	}

	public String getURL() {
		return getValue( "url" );
	}

	public void setURL( String url ) {
		setValue( "url", url );
	}

	public TargetPageType getTargetPageType() {
		return TargetPageType.getMatching( getValue( "pageType" ) );
	}

	public void setTargetPageType( TargetPageType pageType ) {
		setValue( "pageType", pageType != null ? pageType.getKey() : "" + TargetPageType.UNKNOWN );
	}

	public DebugLevel getDebugLevel() {
		return DebugLevel.getMatching( getValue( "debugLevel" ) );
	}

	public void setDebugLevel( DebugLevel debugLevel ) {
		setValue( "debugLevel", debugLevel != null ? "" + debugLevel.getAsInt() : "" + DebugLevel.NONE );
	}

	public String getValue( String key ) {
		return MapUtils.getString( map, key, "" );
	}

	public void setValue( String key, String value ) {
		map.put( key, value );
	}

	public Map<String,String> getMap() {
		return new HashMap<String, String>( map );
	}

	public String toDebugString( String indent ) {
		return indent +
				"map:\n" +
				Utils.prettyPrintMap( indent + "  ", map );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "map", map )
		.toString();
	}
}
