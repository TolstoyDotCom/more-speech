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
package com.tolstoy.censorship.twitter.checker.app.installation;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;

import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScriptFactory;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScript;

public class BrowserScriptFactory implements IBrowserScriptFactory {
	private static final Logger logger = LogManager.getLogger( BrowserScriptFactory.class );

	private final Map<String,IBrowserScript> map;
	private final String[] scriptNames = { "tweet_retriever", "json_parser" };

	public BrowserScriptFactory( final File stockscriptsDir ) throws Exception {
		this.map = new HashMap<String,IBrowserScript>( 2 );

		final File jqueryFile = new File( stockscriptsDir, "jquery.js" );
		if ( !jqueryFile.exists() ) {
			throw new RuntimeException( "jquery.js does not exist in " + jqueryFile );
		}

		final File suededenimFile = new File( stockscriptsDir, "suededenim.java.js" );
		if ( !suededenimFile.exists() ) {
			throw new RuntimeException( "suededenim.java.js does not exist in " + suededenimFile );
		}

		final File tweetRetrieverFooterFile = new File( stockscriptsDir, "tweet_retriever.footer.js" );
		if ( !suededenimFile.exists() ) {
			throw new RuntimeException( "tweet_retriever.footer.js does not exist in " + suededenimFile );
		}

		final File jsonParserFooterFile = new File( stockscriptsDir, "json_parser.footer.js" );
		if ( !suededenimFile.exists() ) {
			throw new RuntimeException( "json_parser.footer.js does not exist in " + suededenimFile );
		}

		final String jQueryContents = FileUtils.readFileToString( jqueryFile, Charset.defaultCharset() );
		final String suedeDenimContents = FileUtils.readFileToString( suededenimFile, Charset.defaultCharset() );
		final String tweetRetrieverFooterContents = FileUtils.readFileToString( tweetRetrieverFooterFile, Charset.defaultCharset() );
		final String jsonParserFooterContents = FileUtils.readFileToString( jsonParserFooterFile, Charset.defaultCharset() );

		map.put( "tweet_retriever", new BrowserScript( "tweet_retriever", jQueryContents + "\n\n" + suedeDenimContents + "\n\n" + tweetRetrieverFooterContents ) );
		map.put( "json_parser", new BrowserScript( "json_parser", jQueryContents + "\n\n" + suedeDenimContents + "\n\n" + jsonParserFooterContents ) );
	}

	public IBrowserScript getScript( String name ) throws Exception {
		if ( !map.containsKey( name ) ) {
			throw new RuntimeException( "No script found for name " + name );
		}

		return map.get( name );
	}

	public String[] getAvailableScriptNames() {
		return scriptNames;
	}
}
