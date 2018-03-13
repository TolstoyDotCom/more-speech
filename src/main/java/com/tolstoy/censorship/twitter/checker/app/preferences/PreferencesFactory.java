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
package com.tolstoy.censorship.twitter.checker.app.preferences;

import java.util.*;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.storage.*;
import com.tolstoy.censorship.twitter.checker.api.preferences.*;
import com.tolstoy.censorship.twitter.checker.app.storage.StorageTable;

public class PreferencesFactory implements IPreferencesFactory {
	private static final Logger logger = LogManager.getLogger( PreferencesFactory.class );

	private IStorage storage;
	private Map<String,String> defaultAppPrefs;
	private IPreferences appPrefs;

	public PreferencesFactory( IStorage storage, Map<String,String> defaultAppPrefs ) {
		this.storage = storage;
		this.defaultAppPrefs = defaultAppPrefs;
		this.appPrefs = null;
	}

	public IPreferences createPreferences() {
		return new Preferences();
	}

	public IPreferences createPreferences( Map<String,String> defaults ) {
		return new Preferences( defaults );
	}

	public IPreferences getAppPreferences() throws Exception {
		if ( appPrefs != null ) {
			return appPrefs;
		}

		if ( storage != null ) {
			List<IStorable> list = storage.getRecords( StorageTable.PREFS, StorageOrdering.DESC, 1 );
			if ( list != null && list.size() > 0 ) {
				IStorable record = list.get( 0 );
				if ( record != null ) {
					appPrefs = new Preferences( storage, record.getID(), record.getCreateTime(), record.getModifyTime(),
												( (IPreferences) record ).getValues(), defaultAppPrefs );
					return appPrefs;
				}
			}
		}

		appPrefs = new Preferences( storage, defaultAppPrefs );

		if ( storage != null ) {
			appPrefs.save();
		}

		return appPrefs;
	}
}
