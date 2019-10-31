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
import org.openqa.selenium.WebElement;

import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;

class WebDriverUtils implements IWebDriverUtils {
	private static final Logger logger = LogManager.getLogger( WebDriverUtils.class );

	private final WebDriver driver;

	WebDriverUtils( final WebDriver driver ) {
		this.driver = driver;
	}

	@Override
	public String makeByXPathClassString( final String clz ) {
		return ".//*[contains(concat(' ', normalize-space(@class), ' '), ' " + clz + " ')]";
	}

	@Override
	public WebElement safeFindByClass( final WebElement parent, final String clz, final String subcontainer ) {
		try {
			return parent.findElement( By.xpath( makeByXPathClassString( clz ) + "/" + subcontainer ) );
		}
		catch ( final Exception e ) {
			return null;
		}
	}

	@Override
	public WebElement safeFindByClass( final WebElement parent, final String clz ) {
		try {
			return parent.findElement( By.xpath( makeByXPathClassString( clz ) ) );
		}
		catch ( final Exception e ) {
			return null;
		}
	}

	@Override
	public String getWebElementText( final WebElement elem ) {
		if ( elem == null ) {
			return "";
		}

		String s;

		s = Utils.trimDefault( elem.getText() );
		if ( s.length() > 0 ) {
			return s;
		}

		s = Utils.trimDefault( elem.getAttribute( "innerHTML" ) );
		if ( s.length() > 0 ) {
			return s;
		}

		s = Utils.trimDefault( elem.getAttribute( "textContent" ) );
		if ( s.length() > 0 ) {
			return s;
		}

		return "";
	}

	@Override
	public String dumpWebElement( final WebElement elem ) {
		return elem.getAttribute( "outerHTML" );
	}
}
