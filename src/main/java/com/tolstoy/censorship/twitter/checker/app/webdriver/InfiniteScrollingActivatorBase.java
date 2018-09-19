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
import com.tolstoy.basic.app.utils.*;

abstract class InfiniteScrollingActivatorBase implements IInfiniteScrollingActivator {
	private static final Logger logger = LogManager.getLogger( InfiniteScrollingActivatorBase.class );

	private static final int MAX_LIMIT = 1000;
	private static final int DELAY_PER_SCREEN_MILLIS = 3000;

	private WebDriver driver;
	private IWebDriverUtils driverutils;
	private boolean complete;

	InfiniteScrollingActivatorBase( WebDriver driver, IWebDriverUtils driverutils ) {
		this.driver = driver;
		this.driverutils = driverutils;
		this.complete = false;
	}

	protected WebDriver getDriver() {
		return driver;
	}

	protected IWebDriverUtils getDriverUtils() {
		return driverutils;
	}

	protected abstract String getHeightScript();
	protected abstract By getHitPlateBy();

	@Override
	public boolean getComplete() {
		return complete;
	}

	@Override
	public void activate( int max ) {
		Actions actions;

		JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;

		WebElement element = driver.findElement( getHitPlateBy() );

		int curHeight = getOverlayHeight( driver, getHeightScript() );
		int tempHeight;

		while ( max >= 0 && max < MAX_LIMIT ) {
			try {
				actions = new Actions( driver );
				actions.sendKeys( element, Keys.PAGE_DOWN );
				actions.perform();
			}
			catch ( Exception e ) {
				try {
					actions = new Actions( driver );
					actions.keyDown( Keys.CONTROL ).sendKeys( Keys.END ).keyUp( Keys.CONTROL ).perform();
				}
				catch ( Exception e2 ) {
					javascriptExecutor.executeScript( "window.scrollBy( 0,5000 );" );
					logger.info( "can't send page down2" );
				}

				logger.info( "can't send page down" );
			}

			Utils.delay( DELAY_PER_SCREEN_MILLIS );

			tempHeight = getOverlayHeight( driver, getHeightScript() );
			logger.info( "curHeight=" + curHeight + ", tempHeight=" + tempHeight );
			if ( Math.abs( tempHeight - curHeight ) < 10 ) {
				logger.info( "heights similar, setting complete true and breaking" );
				complete = true;
				break;
			}

			curHeight = tempHeight;
			max--;
		}

		logger.info( "finished scrolling" );
	}

	private int getOverlayHeight( WebDriver driver, String script ) {
		JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;

		return Utils.numberObjectToInteger( javascriptExecutor.executeScript( script ) );
	}
}

