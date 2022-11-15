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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.jboto.api.framework.IFrameworkFactory;
import com.tolstoy.jboto.api.framework.IFramework;
import com.tolstoy.jboto.app.framework.FrameworkFactory;
import com.tolstoy.jboto.api.IProduct;
import com.tolstoy.jboto.api.IEnvironment;
import com.tolstoy.jboto.api.IForeachCommand;
import com.tolstoy.jboto.api.IIfCommand;
import com.tolstoy.jboto.api.IBasicCommand;
import com.tolstoy.censorship.twitter.checker.app.jboto.OurEnvironment;

public class CloseEverything implements IBasicCommand {
	private static final Logger logger = LogManager.getLogger( CloseEverything.class );
	private static final int WEBDRIVER_CLOSE_DELAY_MILLIS = 5000;

	public CloseEverything() {
	}

	public void run( IProduct product, IEnvironment env, Object extra, int index ) throws Exception {
		SearchRunBaseData searchRunBaseData = (SearchRunBaseData) product;
		OurEnvironment ourEnv = (OurEnvironment) env;

		IBrowserProxy browserProxy = ourEnv.getBrowserProxy();
		if ( browserProxy != null ) {
			try {
				logger.info( "about to stop browserProxy" );
				ourEnv.setBrowserProxy( null );
				browserProxy.stop();
				logger.info( "stopped browserProxy" );
			}
			catch ( final Exception e ) {
				ourEnv.logWarn( logger, "cannot stop browserProxy", e );
			}
		}

		WebDriver webDriver = ourEnv.getWebDriver();
		if ( webDriver != null ) {
			try {
				ourEnv.setWebDriver( null );
				Utils.delay( WEBDRIVER_CLOSE_DELAY_MILLIS );
				webDriver.close();
			}
			catch ( final Exception e ) {
				ourEnv.logWarn( logger, "cannot close webDriver", e );
			}
		}
	}
}
