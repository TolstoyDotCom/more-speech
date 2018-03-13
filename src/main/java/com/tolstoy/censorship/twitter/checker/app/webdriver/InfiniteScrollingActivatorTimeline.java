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

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.support.ui.*;
import com.tolstoy.censorship.twitter.checker.api.webdriver.*;

class InfiniteScrollingActivatorTimeline extends InfiniteScrollingActivatorBase {
	private static final Logger logger = LogManager.getLogger( InfiniteScrollingActivatorTimeline.class );

	InfiniteScrollingActivatorTimeline( WebDriver driver, IWebDriverUtils utils ) {
		super( driver, utils );
	}

	@Override
	protected String getHeightScript() {
		return "return (document.getElementById( 'timeline' )).scrollHeight;";
	}

	@Override
	protected By getHitPlateBy() {
		return By.xpath( getDriverUtils().makeByXPathClassString( "stream-footer" ) );
	}
}
