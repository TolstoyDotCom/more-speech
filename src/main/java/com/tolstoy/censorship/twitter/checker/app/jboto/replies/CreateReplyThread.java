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
package com.tolstoy.censorship.twitter.checker.app.jboto.replies;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotFactory;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ReplyThreadType;
import com.tolstoy.jboto.api.framework.IFrameworkFactory;
import com.tolstoy.jboto.api.framework.IFramework;
import com.tolstoy.jboto.app.framework.FrameworkFactory;
import com.tolstoy.jboto.api.IProduct;
import com.tolstoy.jboto.api.IEnvironment;
import com.tolstoy.jboto.api.IForeachCommand;
import com.tolstoy.jboto.api.IIfCommand;
import com.tolstoy.jboto.api.IBasicCommand;
import com.tolstoy.censorship.twitter.checker.app.jboto.OurEnvironment;

public class CreateReplyThread implements IBasicCommand {
	private static final Logger logger = LogManager.getLogger( CreateReplyThread.class );

	public CreateReplyThread() {
	}

	public void run( IProduct prod, IEnvironment env, Object extra, int index ) throws Exception {
		RepliesProduct product = (RepliesProduct) prod;
		OurEnvironment ourEnv = (OurEnvironment) env;
		ITweet tweet = (ITweet) extra;

		ISnapshotUserPageIndividualTweet snapshot = product.getIndividualPage( tweet.getID() );

		final IReplyThread defaultReplyThread = ourEnv.getSnapshotFactory().makeReplyThread( ReplyThreadType.DIRECT,
																								tweet,
																								snapshot.getIndividualTweet(),
																								snapshot,
																								null );
		product.setReplyPage( tweet.getID(), defaultReplyThread );
	}
}
