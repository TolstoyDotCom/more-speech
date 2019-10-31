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

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalyzedTweet;
import com.tolstoy.censorship.twitter.checker.api.analyzer.ITweetRanker;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;

class TweetRankerJavascript implements ITweetRanker {
	private static final Logger logger = LogManager.getLogger( TweetRankerJavascript.class );

	private static final String SCRIPT_FILENAME_STARTSWITH = "tweetranker_";
	private static final String SCRIPT_FILENAME_ENDSWITH = ".js";

	private final ScriptEngine engine;
	private CompiledScript compiledScript;
	private final ITweetFactory tweetFactory;
	private final IAppDirectories appDirectories;
	private final IPreferences prefs;
	private final IResourceBundleWithFormatting bundle;
	private String functionName;
	private String script;

	public TweetRankerJavascript( final ITweetFactory tweetFactory, final IAppDirectories appDirectories, final IPreferences prefs, final IResourceBundleWithFormatting bundle )
	throws Exception {
		this.tweetFactory = tweetFactory;
		this.appDirectories = appDirectories;
		this.prefs = prefs;
		this.bundle = bundle;
		this.compiledScript = null;
		this.functionName = "";
		this.script = "";

		loadScript();

		final ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByName( "nashorn" );

		final Compilable compilableEngine = (Compilable) engine;
		compiledScript = compilableEngine.compile( script );

		logger.info( "using the " + functionName + " script as the tweet ranker" );
	}

	protected void loadScript() throws Exception {
		final File userScriptsDir = appDirectories.getSubdirectory( "userscripts" );
		if ( !userScriptsDir.exists() ) {
			throw new RuntimeException( "does not exist: " + userScriptsDir );
		}

		final File foundFile = findFile( userScriptsDir );
		if ( foundFile == null ) {
			throw new RuntimeException( "does not exist: " + userScriptsDir );
		}

		functionName = foundFile.getName().replace( SCRIPT_FILENAME_STARTSWITH, "" ).replace( SCRIPT_FILENAME_ENDSWITH, "" );

		script = FileUtils.readFileToString( foundFile, Charset.defaultCharset() );
	}

	protected File findFile( final File userScriptsDir ) throws Exception {
		final String[] userScriptsFilenames = userScriptsDir.list();
		for ( final String filename : userScriptsFilenames ) {
			if ( filename.startsWith( SCRIPT_FILENAME_STARTSWITH ) &&
					filename.endsWith( SCRIPT_FILENAME_ENDSWITH ) &&
					filename.length() > SCRIPT_FILENAME_STARTSWITH.length() + SCRIPT_FILENAME_ENDSWITH.length() ) {
				final File scriptFile = new File( userScriptsDir, filename );
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
	public void rankTweets( final List<IAnalyzedTweet> analyzedTweets, final IAnalyzedTweet referenceAnalyzedTweet ) throws Exception {
		final int count = analyzedTweets.size();
		for ( final IAnalyzedTweet analyzedTweet : analyzedTweets ) {
			rankTweet( analyzedTweet, count, referenceAnalyzedTweet );
		}
	}

	@Override
	public void rankTweet( final IAnalyzedTweet analyzedTweet, final int count, final IAnalyzedTweet referenceAnalyzedTweet ) throws Exception {
		final Bindings bindings = engine.createBindings();

		bindings.put( "analyzedTweet", analyzedTweet );
		bindings.put( "count", count );
		bindings.put( "referenceAnalyzedTweet", referenceAnalyzedTweet );

		compiledScript.eval( bindings );
	}
}
