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

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.api.statusmessage.*;
import com.tolstoy.basic.api.storage.*;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRun;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunProcessor;
import com.tolstoy.censorship.twitter.checker.app.storage.StorageTable;

public class SearchRunProcessorInsertNewToStorage implements ISearchRunProcessor {
	private static final Logger logger = LogManager.getLogger( SearchRunProcessorInsertNewToStorage.class );

	private IResourceBundleWithFormatting bundle;
	private IPreferences prefs;
	private IStorage storage;

	public SearchRunProcessorInsertNewToStorage( IResourceBundleWithFormatting bundle, IPreferences prefs, IStorage storage ) {
		this.bundle = bundle;
		this.prefs = prefs;
		this.storage = storage;
	}

	@Override
	public ISearchRun process( ISearchRun searchRun, IStatusMessageReceiver statusMessageReceiver ) throws Exception {
		storage.saveRecord( StorageTable.SEARCHRUN, (IStorable) searchRun );

		statusMessageReceiver.addMessage( new StatusMessage( "Wrote search run to storage", StatusMessageSeverity.INFO ) );

		return searchRun;
	}

	@Override
	public String getDescription() {
		return bundle.getString( "srp_insert_new_to_storage" );
	}
}
