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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import com.tolstoy.basic.api.statusmessage.*;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunRepliesProcessor;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;

public class SearchRunRepliesProcessorUploadDataJson implements ISearchRunRepliesProcessor {
	private static final Logger logger = LogManager.getLogger( SearchRunRepliesProcessorUploadDataJson.class );

	private IResourceBundleWithFormatting bundle;
	private IPreferences prefs;

	public SearchRunRepliesProcessorUploadDataJson( IResourceBundleWithFormatting bundle, IPreferences prefs ) {
		this.bundle = bundle;
		this.prefs = prefs;
	}

	@Override
	public ISearchRunReplies process( ISearchRunReplies searchRun, IStatusMessageReceiver statusMessageReceiver ) throws Exception {
		if ( !Utils.isStringTrue( prefs.getValue( "prefs.upload_results" ) ) ) {
			return searchRun;
		}

		CloseableHttpClient client = null;

		try {
			client = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost( prefs.getValue( "search_run_upload_data.upload_url" ) );

			String json = Utils.getDefaultObjectMapper().writeValueAsString( searchRun );

			List <NameValuePair> nameValuePairs = new ArrayList <NameValuePair>();

			nameValuePairs.add( new BasicNameValuePair( "json", json ) );
			nameValuePairs.add( new BasicNameValuePair( "upload_results", prefs.getValue( "prefs.upload_results" ) ) );
			nameValuePairs.add( new BasicNameValuePair( "make_results_public", prefs.getValue( "prefs.make_results_public" ) ) );
			nameValuePairs.add( new BasicNameValuePair( "user_email", prefs.getValue( "prefs.user_email" ) ) );

			httpPost.setEntity( new UrlEncodedFormEntity( nameValuePairs ) );

			CloseableHttpResponse response = client.execute( httpPost );

			int status = response.getStatusLine().getStatusCode();

			if ( status != 200 ) {
				throw new IOException( bundle.getString( "search_run_upload_data_error" ) );
			}

			statusMessageReceiver.addMessage( new StatusMessage( "Uploaded data", StatusMessageSeverity.INFO ) );
		}
		finally {
			if ( client != null ) {
				client.close();
			}
		}

		return searchRun;
	}

	@Override
	public String getDescription() {
		return bundle.getString( "srp_upload_data" );
	}
}
