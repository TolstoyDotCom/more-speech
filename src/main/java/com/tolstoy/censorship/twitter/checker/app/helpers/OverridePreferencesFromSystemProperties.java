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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;

public class OverridePreferencesFromSystemProperties implements IOverridePreferences {
	private static final Logger logger = LogManager.getLogger( OverridePreferencesFromSystemProperties.class );

	private String[] overrideable;

	public OverridePreferencesFromSystemProperties( String[] overrideable ) {
		this.overrideable = overrideable;
	}

	public boolean override( IPreferences prefs, IResourceBundleWithFormatting bundle ) {
		boolean bChanged = false;

		if ( overrideable.length > 0 ) {
			for ( int i = 0; i < overrideable.length; i++ ) {
				String key = overrideable[ i ];

				if ( !Utils.isEmpty( System.getProperty( key ) ) ) {
					logger.info( bundle.getString( "preferences_overrideable_by_system_properties", key ) );
					prefs.setValue( key, System.getProperty( key ) );
					bChanged = true;
				}
			}
		}

		return bChanged;
	}
}
