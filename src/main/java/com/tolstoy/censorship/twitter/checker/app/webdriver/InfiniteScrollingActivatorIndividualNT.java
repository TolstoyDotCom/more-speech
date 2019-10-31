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

class InfiniteScrollingActivatorIndividualNT extends InfiniteScrollingActivatorBase {
	private static final Logger logger = LogManager.getLogger( InfiniteScrollingActivatorIndividualNT.class );

	InfiniteScrollingActivatorIndividualNT( final WebDriver driver, final IWebDriverUtils utils ) {
		super( driver, utils );
	}

	@Override
	protected String getHeightScript() {
		return "var x = document.getElementsByClassName( 'PermalinkOverlay-content' ); if ( !x || !x[ 0 ] ) { return 0; } return x[ 0 ].scrollHeight;";
	}

	@Override
	protected By getHitPlateBy() {
		return By.xpath( getDriverUtils().makeByXPathClassString( "stream-footer" ) );
	}
}
