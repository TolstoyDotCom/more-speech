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

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.utils.StringList;
import com.tolstoy.censorship.twitter.checker.api.installation.IAppDirectories;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;

public class OverridePreferencesFromEmbedPathsLinux implements IOverridePreferences {
	private static final Logger logger = LogManager.getLogger( OverridePreferencesFromEmbedPathsLinux.class );

	private static final String PREF_PATH_PROFILE = "prefs.firefox_path_profile";
	private static final String PREF_PATH_APP = "prefs.firefox_path_app";

	private final IAppDirectories appDirectories;

	public OverridePreferencesFromEmbedPathsLinux( final IAppDirectories appDirectories ) {
		this.appDirectories = appDirectories;
	}

	@Override
	public boolean override( final IPreferences prefs, final IResourceBundleWithFormatting bundle ) {
		boolean bChanged = false;

		prefs.setValue( "prefs.firefox_path_geckodriver", "/home/zapp/projects/htdocs/twitter_censorship/geckodriver" );
		bChanged = true;

		final File firefoxDir = appDirectories.getSubdirectory( prefs.getValue( "embed.firefox.containing_dir" ) );
		if ( firefoxDir == null || !firefoxDir.exists() || !firefoxDir.isDirectory() ) {
			return false;
		}

		final File firefoxProfilesDir = new File( firefoxDir, prefs.getValue( "embed.firefox.linux.profile_dir" ) );
		if ( firefoxProfilesDir.exists() && firefoxProfilesDir.isDirectory() ) {
			prefs.setValue( PREF_PATH_PROFILE, firefoxProfilesDir.toString() );
			bChanged = true;
			logger.info( bundle.getString( "preferences_overrideable_by_embed", PREF_PATH_PROFILE ) );
		}

		final File firefoxBinDir = new File( firefoxDir, prefs.getValue( "embed.firefox.linux.binary_dir" ) );
		if ( firefoxBinDir.exists() && firefoxBinDir.isDirectory() ) {
			final StringList stringList = new StringList( prefs.getValue( "embed.firefox.linux.binary_names" ) );
			final List<String> binaryNames = stringList.getItems();
			for ( final String binaryName : binaryNames ) {
				final File binaryFile = new File( firefoxBinDir, binaryName );
				if ( binaryFile.exists() && binaryFile.isFile() ) {
					prefs.setValue( PREF_PATH_APP, binaryFile.toString() );
					bChanged = true;
					logger.info( bundle.getString( "preferences_overrideable_by_embed", PREF_PATH_APP ) );
					break;
				}
			}
		}

		return bChanged;
	}
}
