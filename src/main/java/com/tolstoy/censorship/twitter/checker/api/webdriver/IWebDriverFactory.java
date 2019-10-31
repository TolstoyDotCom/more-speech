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
package com.tolstoy.censorship.twitter.checker.api.webdriver;

import java.util.List;

import org.openqa.selenium.WebDriver;

import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.basic.api.tweet.TargetPageType;
import com.tolstoy.basic.api.utils.IArchiveDirectory;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;

public interface IWebDriverFactory {
	WebDriver makeWebDriver() throws Exception;

	WebDriver makeWebDriver( final IBrowserProxy proxy ) throws Exception;

	IWebDriverUtils makeWebDriverUtils( final WebDriver driver );

	IInfiniteScrollingActivator makeInfiniteScrollingActivator( final WebDriver driver,
																final IWebDriverUtils driverutils,
																final InfiniteScrollingActivatorType type );

	ITweetCollection makeTweetCollectionFromURL( final WebDriver driver,
													final IWebDriverUtils driverutils,
													final IInfiniteScrollingActivator infiniteScroller,
													final IArchiveDirectory archiveDirectory,
													final String url,
													final TargetPageType pageType,
													final int numberOfPagesToCheck,
													final int maxTweets ) throws Exception;

	ISnapshotUserPageTimeline makeSnapshotUserPageTimelineFromURL( final WebDriver driver,
																	final IWebDriverUtils driverutils,
																	final IInfiniteScrollingActivator infiniteScroller,
																	final IBrowserProxy browserProxy,
																	final IArchiveDirectory archiveDirectory,
																	final String url,
																	final int numberOfPagesToCheck,
																	final int maxTweets ) throws Exception;

	ISnapshotUserPageIndividualTweet makeSnapshotUserPageIndividualTweetFromURL( final WebDriver driver,
																					final IWebDriverUtils driverutils,
																					final IInfiniteScrollingActivator infiniteScroller,
																					final IBrowserProxy browserProxy,
																					final IArchiveDirectory archiveDirectory,
																					final String url,
																					final int numberOfPagesToCheck,
																					final int maxTweets ) throws Exception;

	List<String> getBrowserLogs( WebDriver driver );
}
