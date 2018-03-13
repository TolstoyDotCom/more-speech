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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.utils.*;
import com.tolstoy.basic.app.utils.*;
import com.tolstoy.basic.api.tweet.ITweetFactory;
import com.tolstoy.censorship.twitter.checker.api.preferences.*;
import com.tolstoy.censorship.twitter.checker.api.analyzer.*;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;

public class AnalysisReportFactory implements IAnalysisReportFactory {
	private static final Logger logger = LogManager.getLogger( AnalysisReportFactory.class );

	private ITweetFactory tweetFactory;
	private IPreferences prefs;
	private IResourceBundleWithFormatting bundle;

	public AnalysisReportFactory( ITweetFactory tweetFactory, IPreferences prefs, IResourceBundleWithFormatting bundle ) {
		this.tweetFactory = tweetFactory;
		this.prefs = prefs;
		this.bundle = bundle;
	}

	@Override
	public IAnalysisReportRepliesBasic createAnalysisReportRepliesBasic( ISearchRunReplies searchRun ) throws Exception {
		return new AnalysisReportRepliesBasic( searchRun, tweetFactory, prefs, bundle );
	}
}

