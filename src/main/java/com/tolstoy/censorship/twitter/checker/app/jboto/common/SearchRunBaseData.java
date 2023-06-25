/*
 * Copyright 2022 Chris Kelly
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
package com.tolstoy.censorship.twitter.checker.app.jboto.common;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeContainer;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.jboto.api.IProduct;

public class SearchRunBaseData implements IProduct {
	private static final Logger logger = LogManager.getLogger( SearchRunBaseData.class );

	private final Instant startTime;
	private final String handleToCheck, loginName, loginPassword;
	private final int numberOfTimesToScrollOnTimeline, numberOfTimesToScrollOnIndividualPages, numberOfReplyPagesToCheck, numberOfTimelineTweetsToSkip;
	private final boolean bUsingLogin, bSkipLogin;

	public SearchRunBaseData( final IPreferences prefs,
								final String handleToCheck,
								final int numberOfTimesToScrollOnTimeline,
								final int numberOfTimesToScrollOnIndividualPages,
								final int numberOfReplyPagesToCheck,
								final int numberOfTimelineTweetsToSkip ) {
		this.handleToCheck = Utils.trimDefault( handleToCheck ).toLowerCase();
		this.numberOfTimesToScrollOnTimeline = numberOfTimesToScrollOnTimeline;
		this.numberOfTimesToScrollOnIndividualPages = numberOfTimesToScrollOnIndividualPages;
		this.numberOfReplyPagesToCheck = numberOfReplyPagesToCheck;
		this.numberOfTimelineTweetsToSkip = numberOfTimelineTweetsToSkip;

		this.startTime = Instant.now();
		this.loginName = prefs.getValue( "prefs.testing_account_name_private" );
		this.loginPassword = prefs.getValue( "prefs.testing_account_password_private" );

		this.bSkipLogin = Utils.isStringTrue( prefs.getValue( "prefs.skip_login" ) );

		if ( !Utils.isEmpty( loginName ) && !Utils.isEmpty( loginPassword ) ) {
			this.bUsingLogin = true;
		}
		else {
			this.bUsingLogin = false;
		}
	}

	public Instant getStartTime() {
		return startTime;
	}

	public String getHandleToCheck() {
		return handleToCheck;
	}

	public String getLoginName() {
		return loginName;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public int getNumberOfTimesToScrollOnTimeline() {
		return numberOfTimesToScrollOnTimeline;
	}

	public int getNumberOfReplyPagesToCheck() {
		return numberOfReplyPagesToCheck;
	}

	public int getNumberOfTimesToScrollOnIndividualPages() {
		return numberOfTimesToScrollOnIndividualPages;
	}

	public int getNumberOfTimelineTweetsToSkip() {
		return numberOfTimelineTweetsToSkip;
	}

	public boolean isUsingLogin() {
		return bUsingLogin;
	}

	public boolean isSkipLogin() {
		return bSkipLogin;
	}

	public String toString() {
		return new ToStringBuilder( this )
		.appendSuper( super.toString() )
		.append( "startTime", startTime )
		.append( "handleToCheck", handleToCheck )
		.append( "numberOfTimesToScrollOnTimeline", numberOfTimesToScrollOnTimeline )
		.append( "numberOfTimesToScrollOnIndividualPages", numberOfTimesToScrollOnIndividualPages )
		.append( "numberOfReplyPagesToCheck", numberOfReplyPagesToCheck )
		.append( "numberOfTimelineTweetsToSkip", numberOfTimelineTweetsToSkip )
		.toString();
	}
}
