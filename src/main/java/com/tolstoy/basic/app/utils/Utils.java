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
package com.tolstoy.basic.app.utils;

import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import okhttp3.HttpUrl;
import org.nibor.autolink.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class Utils {
	private static final Logger logger = LogManager.getLogger( Utils.class );

	private static final DateFormat dateFormat = new SimpleDateFormat( "MM/dd/yy hh:mm:ss" );	//	TODO i18n

	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.registerModule( new JavaTimeModule() );
		mapper.enableDefaultTyping( ObjectMapper.DefaultTyping.NON_FINAL );
	}

	public static String removeAllEmojis( String s ) {
		return emoji4j.EmojiUtils.removeAllEmojis( s );
	}

	public static String removeNewlines( String s ) {
		if ( s == null ) {
			return "";
		}

		return s.replaceAll( "\\r\\n|\\r|\\n", " " );
	}

	public static String formatTimestampString( String s ) throws Exception {
		return dateFormat.format( new Date( 1000L * Integer.parseInt( s ) ) );
	}

	public static String formatTimestampString( String s, String defaultValue ) {
		try {
			return formatTimestampString( s );
		}
		catch ( Exception e ) {
			return defaultValue;
		}
	}

	public static ObjectMapper getDefaultObjectMapper() {
		return mapper;
	}

	public static Map<String,String> sanitizeMap( Map<String,String> map ) {
		Map<String,String> newMap = new HashMap<String,String>( map );

		for ( String key : newMap.keySet() ) {
			if ( key != null && key.indexOf( "_private" ) > -1 ) {
				newMap.put( key, "***" );
			}
		}

		return newMap;
	}

	public static String extractHandle( String s ) {
		if ( s == null ) {
			return "";
		}

		s = StringUtils.strip( s, "/" );
		if ( s.indexOf( "/" ) < 0 ) {
			return s;
		}

		return getURLPathComponentFirst( s );
	}

	public static String getURLPathComponentFirst( String url ) {
		if ( url == null ) {
			return "";
		}

		HttpUrl httpURL = HttpUrl.parse( url );
		if ( httpURL == null ) {
			return "";
		}

			//	 from the docs: "This list is never empty though it may contain a single empty string."
		List<String> list = httpURL.pathSegments();

		return list.get( 0 );
	}

	public static boolean isStringTrue( String s ) {
		if ( s == null ) {
			return false;
		}

		s = s.toLowerCase().trim();

		return ( "1".equals( s ) || "true".equals( s ) );
	}

	public static int parseIntDefault( String s ) {
		if ( s == null ) {
			return 0;
		}

		try {
			return Integer.parseInt( s );
		}
		catch ( Exception e ) {
			return 0;
		}
	}

	public static int parseIntDefault( String s, int defaultValue ) {
		if ( s == null ) {
			return defaultValue;
		}

		try {
			return Integer.parseInt( s );
		}
		catch ( Exception e ) {
			return defaultValue;
		}
	}

	public static long parseLongDefault( String s ) {
		if ( s == null ) {
			return 0L;
		}

		try {
			return Long.parseLong( s );
		}
		catch ( Exception e ) {
			return 0L;
		}
	}

	public static long parseLongDefault( String s, long defaultValue ) {
		if ( s == null ) {
			return defaultValue;
		}

		try {
			return Long.parseLong( s );
		}
		catch ( Exception e ) {
			return defaultValue;
		}
	}

	public static String trimDefault( String s ) {
		return s == null ? "" : s.trim();
	}

	public static Instant currentInstant() {
		return Instant.now().truncatedTo( ChronoUnit.MICROS );
	}

	public static String extractFirstLink( String s ) {
		LinkExtractor linkExtractor = LinkExtractor.builder()
			.linkTypes( EnumSet.of( LinkType.URL, LinkType.WWW ) )
			.build();
		Iterable<LinkSpan> links = linkExtractor.extractLinks( s );
		LinkSpan link = links.iterator().next();
		if ( link != null ) {
			return s.substring( link.getBeginIndex(), link.getEndIndex() );
		}

		return "";
	}

	public static boolean isEmpty( String s ) {
		return ( s == null || s.trim().isEmpty() );
	}

	public static int numberObjectToInteger( Object x ) {
		if ( x == null ) {
			return 0;
		}

		try {
			if ( x instanceof Number ) {
				return ( (Number) x ).intValue();
			}
			else if ( x instanceof String ) {
				double dbl = Double.parseDouble( (String) x );
				return (int) Math.round( dbl );
			}
			else {
				return 0;
			}
		}
		catch ( Exception e ) {
			return 0;
		}
	}

	public static void delay( int millis ) {
		try {
			Thread.sleep( millis );
		}
		catch ( Exception e ) {
		}
	}

	private Utils() {
	}
}
