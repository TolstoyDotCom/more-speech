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
package com.tolstoy.censorship.twitter.checker.app.helpers;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;

public class LoginToSite {
	private static final Logger logger = LogManager.getLogger( LoginToSite.class );

	private final IPreferences prefs;
	private final String username, password;

	public LoginToSite( final String username, final String password, final IPreferences prefs ) {
		this.username = username;
		this.password = password;
		this.prefs = prefs;
	}

	public void perform( final WebDriver webDriver, final IWebDriverUtils webDriverUtils ) {
		WebElement temp;
		Actions actions;

		webDriver.manage().timeouts().implicitlyWait( 20, TimeUnit.SECONDS );
		webDriver.get( prefs.getValue( "targetsite.login_url" ) );

		final WebElement formElem = webDriver.findElement( By.xpath( webDriverUtils.makeByXPathClassString( "signin" ) ) );

		temp = webDriverUtils.safeFindByClass( formElem, "js-username-field" );
		actions = new Actions( webDriver );
		actions.sendKeys( temp, username );
		actions.perform();

		Utils.delay( 1500 );

		temp = webDriverUtils.safeFindByClass( formElem, "js-password-field" );
		actions = new Actions( webDriver );
		actions.sendKeys( temp, password );
		actions.perform();

		Utils.delay( 2000 );

		temp = webDriverUtils.safeFindByClass( formElem, "submit" );
		temp.click();
	}
}

