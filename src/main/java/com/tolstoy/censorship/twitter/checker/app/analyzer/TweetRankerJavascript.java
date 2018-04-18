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
package com.tolstoy.censorship.twitter.checker.app.analyzer;

import java.util.*;
import java.io.File;
import javax.script.*;
import java.nio.charset.Charset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;
import com.tolstoy.basic.api.utils.*;
import com.tolstoy.basic.app.utils.*;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.censorship.twitter.checker.api.preferences.*;
import com.tolstoy.censorship.twitter.checker.api.analyzer.*;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunTimeline;
import com.tolstoy.censorship.twitter.checker.app.helpers.IAppDirectories;

class TweetRankerJavascript implements ITweetRanker {
	private static final Logger logger = LogManager.getLogger( TweetRankerJavascript.class );

	private static final String SCRIPT_FILENAME_STARTSWITH = "tweetranker_";
	private static final String SCRIPT_FILENAME_ENDSWITH = ".js";

	private ScriptEngine engine;
	private CompiledScript compiledScript;
	private ITweetFactory tweetFactory;
	private IAppDirectories appDirectories;
	private IPreferences prefs;
	private IResourceBundleWithFormatting bundle;
	private String functionName;
	private String script;

	public TweetRankerJavascript( ITweetFactory tweetFactory, IAppDirectories appDirectories, IPreferences prefs, IResourceBundleWithFormatting bundle )
	throws Exception {
		this.tweetFactory = tweetFactory;
		this.appDirectories = appDirectories;
		this.prefs = prefs;
		this.bundle = bundle;
		this.compiledScript = null;
		this.functionName = "";
		this.script = "";

		loadScript();

		ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByName( "nashorn" );

		Compilable compilableEngine = (Compilable) engine;
		compiledScript = compilableEngine.compile( script );

		logger.info( "using the " + functionName + " script as the tweet ranker" );
	}

	protected void loadScript() throws Exception {
		File userScriptsDir = appDirectories.getSubdirectory( "userscripts" );
		if ( !userScriptsDir.exists() ) {
			throw new RuntimeException( "does not exist: " + userScriptsDir );
		}

		File foundFile = findFile( userScriptsDir );
		if ( foundFile == null ) {
			throw new RuntimeException( "does not exist: " + userScriptsDir );
		}

		functionName = foundFile.getName().replace( SCRIPT_FILENAME_STARTSWITH, "" ).replace( SCRIPT_FILENAME_ENDSWITH, "" );

		script = FileUtils.readFileToString( foundFile, Charset.defaultCharset() );
	}

	protected File findFile( File userScriptsDir ) throws Exception {
		String[] userScriptsFilenames = userScriptsDir.list();
		for ( String filename : userScriptsFilenames ) {
			if ( filename.startsWith( SCRIPT_FILENAME_STARTSWITH ) &&
					filename.endsWith( SCRIPT_FILENAME_ENDSWITH ) &&
					filename.length() > SCRIPT_FILENAME_STARTSWITH.length() + SCRIPT_FILENAME_ENDSWITH.length() ) {
				File scriptFile = new File( userScriptsDir, filename );
				if ( scriptFile.exists() && scriptFile.isFile() && scriptFile.length() > 20 ) {
					return scriptFile;
				}
			}
		}

		return null;
	}

	@Override
	public String getFunctionName() {
		return functionName;
	}

	@Override
	public void rankTweets( List<IAnalyzedTweet> analyzedTweets, IAnalyzedTweet referenceAnalyzedTweet ) throws Exception {
		int count = analyzedTweets.size();
		for ( IAnalyzedTweet analyzedTweet : analyzedTweets ) {
			rankTweet( analyzedTweet, count, referenceAnalyzedTweet );
		}
	}

	@Override
	public void rankTweet( IAnalyzedTweet analyzedTweet, int count, IAnalyzedTweet referenceAnalyzedTweet ) throws Exception {
		Bindings bindings = engine.createBindings();

		bindings.put( "analyzedTweet", analyzedTweet );
		bindings.put( "count", count );
		bindings.put( "referenceAnalyzedTweet", referenceAnalyzedTweet );

		compiledScript.eval( bindings );
	}
}
