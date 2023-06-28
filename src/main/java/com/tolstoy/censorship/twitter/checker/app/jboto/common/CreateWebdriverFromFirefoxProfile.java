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
import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import io.github.bonigarcia.wdm.WebDriverManager;

import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IWebDriverUtils;
import com.tolstoy.censorship.twitter.checker.app.webdriver.CustomProfilesIni;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtension;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtensionList;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtensionFactory;
import com.tolstoy.jboto.api.framework.IFrameworkFactory;
import com.tolstoy.jboto.api.framework.IFramework;
import com.tolstoy.jboto.app.framework.FrameworkFactory;
import com.tolstoy.jboto.api.IProduct;
import com.tolstoy.jboto.api.IEnvironment;
import com.tolstoy.jboto.api.IForeachCommand;
import com.tolstoy.jboto.api.IIfCommand;
import com.tolstoy.jboto.api.IBasicCommand;
import com.tolstoy.censorship.twitter.checker.app.jboto.OurEnvironment;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.firefox.*;

public class CreateWebdriverFromFirefoxProfile implements IBasicCommand {
	private static final Logger logger = LogManager.getLogger( CreateWebdriverFromFirefoxProfile.class );

	public CreateWebdriverFromFirefoxProfile() {
	}

	public void run( IProduct prod, IEnvironment env, Object extra, int index ) throws Exception {
		SearchRunBaseData product = (SearchRunBaseData) prod;
		OurEnvironment ourEnv = (OurEnvironment) env;

		try {
			WebDriverManager.firefoxdriver().setup();

			final CustomProfilesIni profilesIni = new CustomProfilesIni();
			final String firefoxProfileName = Utils.trimDefault( ourEnv.getPrefs().getValue( "prefs.firefox_name_profile" ), null, true );
			logger.info( "profile name=" + firefoxProfileName );

			FirefoxOptions firefoxOptions = createOptionsWithExtensions( ourEnv, profilesIni.getProfile( firefoxProfileName ) );

			if ( firefoxOptions == null ) {
				logger.info( "cannot create with extensions, trying without" );
				firefoxOptions = createOptionsWithoutExtensions( ourEnv, profilesIni.getProfile( firefoxProfileName ) );
			}

			final Duration timeout = ourEnv.getGeneralTimeout();

			final ClientConfig clientConfig = ClientConfig.defaultConfig().readTimeout( timeout ).connectionTimeout( timeout );

			final WebDriver driver = new FirefoxDriver( GeckoDriverService.createDefaultService(), firefoxOptions, clientConfig );

			driver.manage().timeouts().scriptTimeout( timeout );

			ourEnv.setWebDriver( driver );

			final int positionX = Utils.parseIntDefault( ourEnv.getPrefs().getValue( "prefs.firefox_screen_position_x" ) );
			final int positionY = Utils.parseIntDefault( ourEnv.getPrefs().getValue( "prefs.firefox_screen_position_y" ) );
			driver.manage().window().setPosition( new Point( positionX, positionY ) );
		}
		catch ( final Exception e ) {
			ourEnv.logWarn( logger, "cannot create ourEnv.getWebDriver()", e );
			ourEnv.getStatusMessageReceiver().addMessage( new StatusMessage( "cannot create ourEnv.getWebDriver()", StatusMessageSeverity.ERROR ) );
			throw e;
		}
	}

	protected FirefoxOptions createOptionsWithExtensions( final OurEnvironment ourEnv, final FirefoxProfile firefoxProfile ) {
		IBrowserExtensionList installedExtensions = ourEnv.getBrowserExtensionFactory().makeBrowserExtensionList();

		try {
			for ( IBrowserExtension ext : ourEnv.getExtensionsToInstall().getList() ) {
				firefoxProfile.addExtension( CreateWebdriverFromFirefoxProfile.class, ext.getFilename() );

				installedExtensions.add( ext );

				logger.info( "installed extension " + ext );
			}

			setFirefoxProfilePreferences( ourEnv, firefoxProfile );

			FirefoxOptions firefoxOptions = new FirefoxOptions();

			firefoxOptions.setLogLevel( FirefoxDriverLogLevel.WARN );
			firefoxOptions.addPreference( "toolkit.asyncshutdown.log", true );
			firefoxOptions.addArguments( "--devtools" );

			firefoxOptions.setProfile( firefoxProfile );

			ourEnv.setInstalledExtensions( installedExtensions );

			return firefoxOptions;
		}
		catch ( Exception e ) {
			logger.info( "FAILED TO INSTALL EXTENSIONS " + ourEnv.getExtensionsToInstall().getList() + " DUE TO " + e.getMessage() );

			return null;
		}
	}

	protected FirefoxOptions createOptionsWithoutExtensions( final OurEnvironment ourEnv, final FirefoxProfile firefoxProfile ) {
		try {
			setFirefoxProfilePreferences( ourEnv, firefoxProfile );

			FirefoxOptions firefoxOptions = new FirefoxOptions();

			firefoxOptions.setLogLevel( FirefoxDriverLogLevel.WARN );

			firefoxOptions.setProfile( firefoxProfile );

			//	empty list
			ourEnv.setInstalledExtensions( ourEnv.getBrowserExtensionFactory().makeBrowserExtensionList() );

			return firefoxOptions;
		}
		catch ( Exception e ) {
			logger.info( "FAILED TO createOptionsWithoutExtensions " + e.getMessage() );

			return null;
		}
	}

	protected void setFirefoxProfilePreferences( final OurEnvironment ourEnv, final FirefoxProfile firefoxProfile ) {
		firefoxProfile.setPreference( "app.update.auto", false );
		firefoxProfile.setPreference( "app.update.enabled", false );
		firefoxProfile.setPreference( "browser.shell.checkDefaultBrowser", false );
		firefoxProfile.setPreference( "devtools.console.stdout.content", true );
		firefoxProfile.setPreference( "devtools.toolbox.selectedTool", "netmonitor" );
		firefoxProfile.setPreference( "devtools.netmonitor.persistlog", true );
		firefoxProfile.setPreference( "devtools.toolbox.footer.height", 120 );

		firefoxProfile.setPreference( "extensions.pocket.enabled", false );
		firefoxProfile.setPreference( "identity.fxaccounts.enabled", false );

		//	fission = different processes for different sites, not needed in this case.
		firefoxProfile.setPreference( "fission.autostart", false );
		firefoxProfile.setPreference( "fission.bfcacheInParent", 0 );
		firefoxProfile.setPreference( "fission.webContentIsolationStrategy", 0 );


		/* for using an external proxy:
		profile.setPreference( "network.proxy.type", 1 );
		profile.setPreference( "network.proxy.http", "127.0.0.1" );
		profile.setPreference( "network.proxy.http_port", 8080 );
		profile.setPreference( "network.proxy.ssl", "127.0.0.1" );
		profile.setPreference( "network.proxy.ssl_port", 8080 );
		*/
	}
}
