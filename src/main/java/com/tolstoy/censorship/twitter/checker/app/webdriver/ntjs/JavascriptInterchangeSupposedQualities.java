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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.collections4.MapUtils;

import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.api.tweet.TweetSupposedQuality;

public final class JavascriptInterchangeSupposedQualities {
	private static final Logger logger = LogManager.getLogger( JavascriptInterchangeSupposedQualities.class );

	private final Map<Long,TweetSupposedQuality> map;

	JavascriptInterchangeSupposedQualities( final Map<String,String> input ) throws Exception {
		if ( input == null ) {
			throw new IllegalArgumentException( "input is null" );
		}

		this.map = new HashMap<Long,TweetSupposedQuality>( input.size() );

		for ( String key : input.keySet() ) {
			try {
				long tweetID = Long.parseLong( key );
				map.put( tweetID, TweetSupposedQuality.getMatching( input.get( key ) ) );
			}
			catch ( final Exception e ) {
				logger.error( "cannot parse tweetID " + key );
			}
		}
	}

	public TweetSupposedQuality getSupposedQuality( long tweetid ) {
		return map.get( tweetid );
	}

	public String toDebugString( String indent ) {
		List<String> list = new ArrayList<String>( map.size() );

		for ( Long key : map.keySet() ) {
			list.add( indent + key + "=" + map.get( key ) );
		}

		return StringUtils.join( list, "\n" );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "map", map )
		.toString();
	}
}
