package com.tolstoy.censorship.twitter.checker.app.webdriver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.io.TemporaryFilesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom version of ProfilesIni that deals with Snap.
 * See https://github.com/SeleniumHQ/selenium/issues/11614
 */
public class CustomProfilesIni extends ProfilesIni {
	private static final Logger logger = LogManager.getLogger( CustomProfilesIni.class );

	private Map<String, File> profiles;

	public CustomProfilesIni() {
		File appData = locateAppDataDirectory( Platform.getCurrent() );
		profiles = readProfiles( appData );
	}

	protected File locateAppDataDirectory( Platform os ) {
		File appData;

		if ( os.is( Platform.WINDOWS ) ) {
			appData = new File(MessageFormat.format("{0}\\Mozilla\\Firefox", System.getenv("APPDATA")));

		}
		else if ( os.is( Platform.MAC ) ) {
			appData = new File(MessageFormat.format("{0}/Library/Application Support/Firefox", System.getenv("HOME")));
		}
		else {
			appData = new File(MessageFormat.format("{0}/.mozilla/firefox", System.getenv("HOME")));
		}

		if (!appData.exists()) {
			appData = new File(MessageFormat.format( "{0}/snap/firefox/common/.mozilla/firefox", System.getenv("HOME") ) );
		}

		if ( !appData.exists() ) {
			// It's possible we're being run as part of an automated build.
			// Assume the user knows what they're doing
			return null;
		}

		if (!appData.isDirectory()) {
			throw new WebDriverException("The discovered user firefox data directory " +
					"(which normally contains the profiles) isn't a directory: " + appData.getAbsolutePath());
		}

		return appData;
	}

	protected Map<String, File> readProfiles(File appData) {
		Map<String, File> toReturn = new HashMap<>();

		File profilesIni = new File( appData, "profiles.ini" );
		if ( !profilesIni.exists() ) {
			logger.info( "NO PROFILE.INI FOUND IN " + appData );
			// Fine. No profiles.ini file
			return toReturn;
		}

		boolean isRelative = true;
		String name = null;
		String path = null;

		BufferedReader reader = null;
		try {
			reader = Files.newBufferedReader(profilesIni.toPath(), Charset.defaultCharset());

			String line = reader.readLine();

			while (line != null) {
				if (line.startsWith("[Profile")) {
					File profile = newProfile(name, appData, path, isRelative);
					if (profile != null)
						toReturn.put(name, profile);

					name = null;
					path = null;
				} else if (line.startsWith("Name=")) {
					name = line.substring("Name=".length());
				} else if (line.startsWith("IsRelative=")) {
					isRelative = line.endsWith("1");
				} else if (line.startsWith("Path=")) {
					path = line.substring("Path=".length());
				}

				line = reader.readLine();
			}
		} catch (IOException e) {
			throw new WebDriverException(e);
		} finally {
			try {
				if (reader != null) {
					File profile = newProfile(name, appData, path, isRelative);
					if (profile != null)
						toReturn.put(name, profile);

					reader.close();
				}
			} catch (IOException e) {
				// Nothing that can be done sensibly. Swallowing.
			}
		}

		return toReturn;
	}

	protected File newProfile(String name, File appData, String path, boolean isRelative) {
		if (name != null && path != null) {
			return isRelative ? new File(appData, path) : new File(path);
		}
		return null;
	}

	public FirefoxProfile getProfile(String profileName) {
		File profileDir = profiles.get(profileName);
		if (profileDir == null) {
			logger.info( "NO PROFILE FOUND FOR " + profileName );
			return null;
		}

		// Make a copy of the profile to use
		File tempDir = TemporaryFilesystem.getDefaultTmpFS().createTempDir("userprofile", "copy");
		try {
			FileHandler.copy(profileDir, tempDir);

			// Delete the old compreg.dat file so that our new extension is registered
			File compreg = new File(tempDir, "compreg.dat");
			if (compreg.exists()) {
				if (!compreg.delete()) {
					throw new WebDriverException("Cannot delete file from copy of profile " + profileName);
				}
			}
		} catch (IOException e) {
			throw new WebDriverException(e);
		}

		logger.info( "RETURNING PROFILE BASED ON " + profileDir + " (COPIED TO " + tempDir + ")" );

		return new FirefoxProfile(tempDir);
	}
}
