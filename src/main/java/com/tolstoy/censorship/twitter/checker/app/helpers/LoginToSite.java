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
		Actions actions;

		webDriver.manage().timeouts().implicitlyWait( 20, TimeUnit.SECONDS );
		webDriver.get( prefs.getValue( "targetsite.login_url" ) );

		LoginPlan plan;

		plan = new LoginPlanA( webDriver, webDriverUtils );
		plan.perform();
		if ( !plan.isValid() ) {
			plan = new LoginPlanB( webDriver, webDriverUtils );
			plan.perform();
		}
		// plan C = javascript

		if ( !plan.isValid() ) {
			throw new RuntimeException( "cannot find login page elements" );
		}

		actions = new Actions( webDriver );
		actions.sendKeys( plan.getUsername(), username );
		actions.perform();

		Utils.delay( 1500 );

		actions = new Actions( webDriver );
		actions.sendKeys( plan.getPassword(), password );
		actions.perform();

		Utils.delay( 2000 );

		plan.getSubmit().click();

		logger.info( "logged in" );
	}

	private static final class LoginPlanA extends LoginPlan {
		LoginPlanA( final WebDriver webDriver, final IWebDriverUtils webDriverUtils ) {
			super( webDriver, webDriverUtils );
		}

		void perform() {
			String name, role, testid;

			try {
				for ( final WebElement textInput : getWebDriver().findElements( By.xpath( "//input[@type='text']" ) ) ) {
					name = textInput.getAttribute( "name" );

					if ( StringUtils.containsIgnoreCase( name, "username" ) ) {
						setUsername( textInput );
						break;
					}
				}

				for ( final WebElement passwordInput : getWebDriver().findElements( By.xpath( "//input[@type='password']" ) ) ) {
					name = passwordInput.getAttribute( "name" );

					if ( StringUtils.containsIgnoreCase( name, "password" ) ) {
						setPassword( passwordInput );
						break;
					}
				}

				for ( final WebElement div : getWebDriver().findElements( By.tagName( "div" ) ) ) {
					role = div.getAttribute( "role" );
					testid = div.getAttribute( "data-testid" );

					if ( StringUtils.containsIgnoreCase( role, "button" ) || StringUtils.containsIgnoreCase( testid, "button" ) ) {
						setSubmit( div );
						break;
					}
				}
			}
			catch ( Exception e ) {
				logger.error( "finding login textfields, LoginPlanA failed", e );
			}
		}
	}

	private static final class LoginPlanB extends LoginPlan {
		LoginPlanB( final WebDriver webDriver, final IWebDriverUtils webDriverUtils ) {
			super( webDriver, webDriverUtils );
		}

		void perform() {
			try {
				final WebElement formElem = getWebDriver().findElement( By.xpath( getWebDriverUtils().makeByXPathClassString( "signin" ) ) );

				setUsername( getWebDriverUtils().safeFindByClass( formElem, "js-username-field" ) );

				setPassword( getWebDriverUtils().safeFindByClass( formElem, "js-password-field" ) );

				setSubmit( getWebDriverUtils().safeFindByClass( formElem, "submit" ) );
			}
			catch ( Exception e ) {
				logger.error( "finding login textfields, LoginPlanB failed", e );
			}
		}
	}

	private static abstract class LoginPlan {
		private final WebDriver webDriver;
		private final IWebDriverUtils webDriverUtils;
		private WebElement username, password, submit;

		LoginPlan( final WebDriver webDriver, final IWebDriverUtils webDriverUtils ) {
			this.webDriver = webDriver;
			this.webDriverUtils = webDriverUtils;
			this.username = null;
			this.password = null;
			this.submit = null;
		}

		abstract void perform();

		boolean isValid() {
			return username != null && password != null && submit != null;
		}

		WebDriver getWebDriver() {
			return webDriver;
		}

		IWebDriverUtils getWebDriverUtils() {
			return webDriverUtils;
		}

		WebElement getUsername() {
			return username;
		}

		void setUsername( WebElement elem ) {
			this.username = elem;
		}

		WebElement getPassword() {
			return password;
		}

		void setPassword( WebElement elem ) {
			this.password = elem;
		}

		WebElement getSubmit() {
			return submit;
		}

		void setSubmit( WebElement elem ) {
			this.submit = elem;
		}
	}
}
