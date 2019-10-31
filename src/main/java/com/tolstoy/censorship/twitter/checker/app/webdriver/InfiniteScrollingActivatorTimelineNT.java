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
package com.tolstoy.censorship.twitter.checker.app.webdriver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IInfiniteScrollingActivator;

class InfiniteScrollingActivatorTimelineNT implements IInfiniteScrollingActivator {
	private static final Logger logger = LogManager.getLogger( InfiniteScrollingActivatorTimelineNT.class );

	private final WebDriver driver;
	private final IWebDriverUtils driverutils;

	InfiniteScrollingActivatorTimelineNT( final WebDriver driver, final IWebDriverUtils driverutils ) {
		this.driver = driver;
		this.driverutils = driverutils;
	}

	@Override
	public boolean getComplete() {
		return false;
	}

	@Override
	public void activate( int max ) {
	}
}
