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

import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxy;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.jboto.api.framework.IFrameworkFactory;
import com.tolstoy.jboto.api.framework.IFramework;
import com.tolstoy.jboto.app.framework.FrameworkFactory;
import com.tolstoy.jboto.api.IProduct;
import com.tolstoy.jboto.api.IEnvironment;
import com.tolstoy.jboto.api.IForeachCommand;
import com.tolstoy.jboto.api.IIfCommand;
import com.tolstoy.jboto.api.IBasicCommand;
import com.tolstoy.censorship.twitter.checker.app.jboto.OurEnvironment;

public class CreateBrowserDataRecorder implements IBasicCommand {
	private static final Logger logger = LogManager.getLogger( CreateBrowserDataRecorder.class );

	public CreateBrowserDataRecorder() {
	}

	public void run( IProduct product, IEnvironment env, Object extra, int index ) throws Exception {
		SearchRunBaseData searchRunBaseData = (SearchRunBaseData) product;
		OurEnvironment ourEnv = (OurEnvironment) env;

		try {
			ourEnv.setBrowserProxy( ourEnv.getBrowserProxyFactory().makeBrowserProxy() );

			ourEnv.getBrowserProxy().start();
		}
		catch ( final Exception e ) {
			ourEnv.logWarn( logger, "cannot create ourEnv.getBrowserProxy()", e );
			ourEnv.getStatusMessageReceiver().addMessage( new StatusMessage( "cannot create ourEnv.getBrowserProxy()", StatusMessageSeverity.ERROR ) );
			throw e;
		}
	}
}
