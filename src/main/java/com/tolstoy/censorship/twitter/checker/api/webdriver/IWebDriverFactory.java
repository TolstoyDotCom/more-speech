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

import org.openqa.selenium.WebDriver;
import com.tolstoy.basic.api.tweet.ITweetCollection;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;

public interface IWebDriverFactory {
	WebDriver makeWebDriver() throws Exception;

	IWebDriverUtils makeWebDriverUtils( WebDriver driver );

	IInfiniteScrollingActivator makeInfiniteScrollingActivator( WebDriver driver,
																IWebDriverUtils driverutils,
																InfiniteScrollingActivatorType type );

	ITweetCollection makeTweetCollectionFromURL( WebDriver driver,
													IWebDriverUtils driverutils,
													IInfiniteScrollingActivator infiniteScroller,
													String url,
													int numberOfPagesToCheck,
													int maxTweets ) throws Exception;

	ISnapshotUserPageTimeline makeSnapshotUserPageTimelineFromURL( WebDriver driver,
																	IWebDriverUtils driverutils,
																	IInfiniteScrollingActivator infiniteScroller,
																	String url,
																	int numberOfPagesToCheck,
																	int maxTweets ) throws Exception;

	ISnapshotUserPageIndividualTweet makeSnapshotUserPageIndividualTweetFromURL( WebDriver driver,
																					IWebDriverUtils driverutils,
																					IInfiniteScrollingActivator infiniteScroller,
																					String url,
																					int numberOfPagesToCheck,
																					int maxTweets ) throws Exception;
}
