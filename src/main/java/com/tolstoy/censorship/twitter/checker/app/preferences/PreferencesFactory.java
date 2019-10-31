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

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.storage.IStorable;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.storage.StorageOrdering;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferencesFactory;
import com.tolstoy.censorship.twitter.checker.app.storage.StorageTable;

public class PreferencesFactory implements IPreferencesFactory {
	private static final Logger logger = LogManager.getLogger( PreferencesFactory.class );

	private final IStorage storage;
	private final Map<String,String> defaultAppPrefs;
	private IPreferences appPrefs;

	public PreferencesFactory( final IStorage storage, final Map<String,String> defaultAppPrefs ) {
		this.storage = storage;
		this.defaultAppPrefs = defaultAppPrefs;
		this.appPrefs = null;
	}

	@Override
	public IPreferences createPreferences() {
		return new Preferences();
	}

	@Override
	public IPreferences createPreferences( final Map<String,String> defaults ) {
		return new Preferences( defaults );
	}

	@Override
	public IPreferences getAppPreferences() throws Exception {
		if ( appPrefs != null ) {
			return appPrefs;
		}

		if ( storage != null ) {
			final List<IStorable> list = storage.getRecords( StorageTable.PREFS, StorageOrdering.DESC, 1 );
			if ( list != null && !list.isEmpty() ) {
				final IStorable record = list.get( 0 );
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
