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

import java.io.File;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.text.Format;
import java.text.DateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TimeZone;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang.time.FastDateFormat;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.vdurmont.emoji.EmojiParser;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.github.cliftonlabs.json_simple.JsonObject;

import okhttp3.HttpUrl;

public final class Utils {
	private static final Logger logger = LogManager.getLogger( Utils.class );

	private static final Format humanDateFormat_EN_US = FastDateFormat.getInstance( "MM/dd/yy hh:mm:ss" );	//	TODO i18n
	private static final Format directoryDateFormat = FastDateFormat.getInstance( "yyyyMMdd_hhmmss_SSS" );
	private static final int EXCEPTION_MESSAGE_LEN = 2000;
	private static final int EXCEPTION_STACKTRACE_LEN = -5000;

	private static ObjectMapper mapper, plainMapper;

	static {
		mapper = new ObjectMapper();
		mapper.registerModule( new JavaTimeModule() );
		mapper.enableDefaultTyping( ObjectMapper.DefaultTyping.NON_FINAL );

		plainMapper = new ObjectMapper();
		plainMapper.registerModule( new JavaTimeModule() );
	}

	public static Map<String,Object> readJSONMap( String data ) throws Exception {
		return Jsoner.deserialize( data, new JsonObject() );
	}

	public static Map<String,String> makeStringMap( Object input ) throws Exception {
		Map<String,String> ret = new HashMap<String,String>();

		if ( input == null ) {
			return new HashMap<String,String>();
		}

		if ( !( input instanceof Map ) ) {
			throw new IllegalArgumentException( "input is not a map:" + input );
		}

		Map map = (Map) input;

		for ( Object keyObj : map.keySet() ) {
			if ( keyObj == null || !( keyObj instanceof String ) ) {
				throw new IllegalArgumentException( "input key " + keyObj + " is not a String in input " + input );
			}

			String key = (String) keyObj;

			Object valueObj = map.get( keyObj );
			if ( valueObj == null ) {
				ret.put( key, null );
			}
			else if ( !( valueObj instanceof String ) ) {
				ret.put( key, valueObj.toString() );
			}
			else {
				ret.put( key, (String) valueObj );
			}
		}

		return ret;
	}

	public static File findFileGoingUp( File directory, String filename ) {
		if ( !directory.isDirectory() ) {
			directory = directory.getParentFile();
		}

		if ( directory == null ) {
			return null;
		}

		while ( directory.getParent() != null ) {
			File tempFile = new File( directory, filename );
			if ( tempFile.exists() ) {
				return tempFile;
			}

			directory = directory.getParentFile();
		}

		return null;
	}

	public static <V> Map<String,V> copyMapWithMatchingKeys( final Map<String,V> data, final String baseKey ) {
		final Map<String,V> ret = new HashMap<String,V>( data.size() );
		final int baseKeyLen = baseKey.length();

		for ( final String key : data.keySet() ) {
			if ( key != null && key.startsWith( baseKey ) ) {
				final String newKey = key.substring( baseKeyLen );
				if ( !Utils.isEmpty( newKey ) ) {
					ret.put( newKey, data.get( key ) );
				}
			}
		}

		return ret;
	}

	public static boolean parseBoolean( final String input ) {
		if ( input == null ) {
			return false;
		}

		String test = input.trim();
		if ( test.isEmpty() ) {
			return false;
		}

		return "1".equals( input ) || "true".equalsIgnoreCase( input );
	}

	public static int makePercentInt( final int dividend, final int divisor ) {
		if ( divisor == 0 ) {
			return 100;
		}

		return (int) Math.floor( ( 100.0 * dividend ) / divisor );
	}

	public static String removeAllEmojis( final String s ) {
		return EmojiParser.removeAllEmojis( s );
	}

	public static String replaceAllEmojis( final String s ) {
		return EmojiParser.parseToAliases( s );
	}

	public static String removeNewlines( final String s ) {
		if ( s == null ) {
			return "";
		}

		return s.replaceAll( "\\r\\n|\\r|\\n", " " );
	}

	public static Date stringToDate( final String s ) {
		try {
			return new Date( 1000L * Integer.parseInt( s ) );
		}
		catch ( final Exception e ) {
			return new Date();
		}
	}

	public static String prettyPrintMap( final String indent, final Map input ) {
		if ( input == null ) {
			return indent + "no map";
		}

		Map<Object,Object> map = new TreeMap<Object,Object>( input );

		List<String> list = new ArrayList<String>( map.size() );

		for ( Object key : map.keySet() ) {
			list.add( indent + "" + key + "=" + map.get( key ) );
		}

		return StringUtils.join( list, "\n" );
	}

	public static String formatTimestampString( final String s ) throws Exception {
		return humanDateFormat_EN_US.format( new Date( 1000L * Integer.parseInt( s ) ) );
	}

	public static String formatTimestampString( final String s, final String defaultValue ) {
		try {
			return formatTimestampString( s );
		}
		catch ( final Exception e ) {
			return defaultValue;
		}
	}

	public static String formatTimestampString( final DateFormat format, final String s ) throws Exception {
		return format.format( new Date( 1000L * Integer.parseInt( s ) ) );
	}

	public static String formatTimestampString( final DateFormat format, final String s, final String defaultValue ) {
		try {
			return formatTimestampString( format, s );
		}
		catch ( final Exception e ) {
			return defaultValue;
		}
	}

	public static ObjectMapper getDefaultObjectMapper() {
		return mapper;
	}

	public static ObjectMapper getPlainObjectMapper() {
		return plainMapper;
	}

	public static Map<String,String> sanitizeMap( final Map<String,String> map ) {
		final Map<String,String> newMap = new HashMap<String,String>( map );

		for ( final String key : newMap.keySet() ) {
			if ( key != null && key.indexOf( "_private" ) > -1 ) {
				newMap.put( key, "***" );
			}
		}

		return newMap;
	}

	public static String extractHandle( final String s ) {
		String str = s != null ? s : "";

		str = StringUtils.strip( str, "/" );
		if ( str.indexOf( "/" ) < 0 ) {
			return s;
		}

		return getURLPathComponentFirst( str );
	}

	public static String normalizeHandle( String handle ) {
		if ( Utils.isEmpty( handle ) ) {
			return "";
		}

		return StringUtils.strip( handle, " @\t\n\r" ).toLowerCase();
	}

	public static String getURLPathComponentFirst( final String url ) {
		if ( url == null ) {
			return "";
		}

		final HttpUrl httpURL = HttpUrl.parse( url );
		if ( httpURL == null ) {
			return "";
		}

			//	 from the docs: "This list is never empty though it may contain a single empty string."
		final List<String> list = httpURL.pathSegments();

		return list.get( 0 );
	}

	public static boolean isStringTrue( final String s ) {
		String str = s;

		if ( str == null ) {
			return false;
		}

		str = str.toLowerCase().trim();

		return ( "1".equals( str ) || "true".equals( str ) );
	}

	public static int parseIntDefault( final String s ) {
		if ( s == null ) {
			return 0;
		}

		try {
			return Integer.parseInt( s );
		}
		catch ( final Exception e ) {
			return 0;
		}
	}

	public static int parseIntDefault( final String s, final int defaultValue ) {
		if ( s == null ) {
			return defaultValue;
		}

		try {
			return Integer.parseInt( s );
		}
		catch ( final Exception e ) {
			return defaultValue;
		}
	}

	public static long parseLongDefault( final String s ) {
		if ( s == null ) {
			return 0L;
		}

		try {
			return Long.parseLong( s );
		}
		catch ( final Exception e ) {
			return 0L;
		}
	}

	public static long parseLongDefault( final String s, final long defaultValue ) {
		if ( s == null ) {
			return defaultValue;
		}

		try {
			return Long.parseLong( s );
		}
		catch ( final Exception e ) {
			return defaultValue;
		}
	}

	public static String trimDefault( final String s ) {
		return s == null ? "" : s.trim();
	}

	public static String trimDefault( final String s, final String defaultValue ) {
		return s == null ? defaultValue : s.trim();
	}

	public static String trimDefault( final String s, final String defaultValue, final boolean bReturnDefaultIfEmpty ) {
		if ( s == null ) {
			return defaultValue;
		}

		String temp = s.trim();

		if ( bReturnDefaultIfEmpty && Utils.isEmpty( temp ) ) {
			return defaultValue;
		}

		return temp;
	}

	public static Instant currentInstant() {
		return Instant.now().truncatedTo( ChronoUnit.MICROS );
	}

	public static String extractFirstLink( final String s ) {
		if ( s == null ) {
			return "";
		}

		final LinkExtractor linkExtractor = LinkExtractor.builder()
			.linkTypes( EnumSet.of( LinkType.URL, LinkType.WWW ) )
			.build();
		final Iterable<LinkSpan> links = linkExtractor.extractLinks( s );
		if ( !links.iterator().hasNext() ) {
			return "";
		}
		final LinkSpan link = links.iterator().next();
		if ( link != null ) {
			return s.substring( link.getBeginIndex(), link.getEndIndex() );
		}

		return "";
	}

	public static boolean isEmpty( final String s ) {
		return ( s == null || s.trim().isEmpty() );
	}

	public static boolean isEmptyOrZero( final String s ) {
		return ( s == null || s.trim().isEmpty() || "0".equals( s.trim() ) );
	}

	public static String chooseLeastEmpty( final String a, final String b, final String defaultValue ) {
		if ( Utils.isEmpty( a ) ) {
			return !Utils.isEmpty( b ) ? b : defaultValue;
		}

		if ( Utils.isEmpty( b ) ) {
			return !Utils.isEmpty( a ) ? a : defaultValue;
		}

		if ( "0".equals( a ) ) {
			return b;
		}

		if ( "0".equals( b ) ) {
			return a;
		}

		return a.length() > b.length() ? a : b;
	}

	public static int numberObjectToInteger( final Object x ) {
		if ( x == null ) {
			return 0;
		}

		try {
			if ( x instanceof Number ) {
				return ( (Number) x ).intValue();
			}
			else if ( x instanceof String ) {
				final double dbl = Double.parseDouble( (String) x );
				return (int) Math.round( dbl );
			}
			else {
				return 0;
			}
		}
		catch ( final Exception e ) {
			return 0;
		}
	}

	public static void delay( final int millis ) {
		try {
			Thread.sleep( millis );
		}
		catch ( final Exception e ) {
		}
	}

	public static void logException( Logger logger, String message, Exception e ) {
		logger.error( message +
						",\nmessage=" + StringUtils.substring( e.getMessage(), 0, EXCEPTION_MESSAGE_LEN ) +
						",\ntrace=" + StringUtils.substring( ExceptionUtils.getStackTrace( e ), EXCEPTION_STACKTRACE_LEN, -1 ) );
	}

	private Utils() {
	}
}
