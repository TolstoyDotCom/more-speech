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

import com.tolstoy.basic.api.statusmessage.IStatusMessageReceiver;
import com.tolstoy.basic.api.statusmessage.StatusMessage;
import com.tolstoy.basic.api.statusmessage.StatusMessageSeverity;
import com.tolstoy.basic.api.storage.IStorable;
import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRun;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunProcessor;
import com.tolstoy.censorship.twitter.checker.app.storage.StorageTable;

public class SearchRunProcessorInsertNewToStorage implements ISearchRunProcessor {
	private static final Logger logger = LogManager.getLogger( SearchRunProcessorInsertNewToStorage.class );

	private final IResourceBundleWithFormatting bundle;
	private final IPreferences prefs;
	private final IStorage storage;

	public SearchRunProcessorInsertNewToStorage( final IResourceBundleWithFormatting bundle, final IPreferences prefs, final IStorage storage ) {
		this.bundle = bundle;
		this.prefs = prefs;
		this.storage = storage;
	}

	@Override
	public ISearchRun process( final ISearchRun searchRun, final IStatusMessageReceiver statusMessageReceiver ) throws Exception {
		storage.saveRecord( StorageTable.SEARCHRUN, (IStorable) searchRun );

		statusMessageReceiver.addMessage( new StatusMessage( "Wrote search run to storage", StatusMessageSeverity.INFO ) );

		logger.info( "Wrote search run to storage" );

		return searchRun;
	}

	@Override
	public String getDescription() {
		return bundle.getString( "srp_insert_new_to_storage" );
	}
}
