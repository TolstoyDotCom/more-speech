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
package com.tolstoy.censorship.twitter.checker.app.jboto.timeline;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeContainer;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.jboto.api.IProduct;
import com.tolstoy.censorship.twitter.checker.app.jboto.common.SearchRunBaseData;

public class SearchRunTimelineData extends SearchRunBaseData {
	private static final Logger logger = LogManager.getLogger( SearchRunTimelineData.class );

	private ISnapshotUserPageTimeline timeline;
	private JavascriptInterchangeContainer timelineJIC;
	private List<String> timelineJSONStringList;

	private Map<Long,ISnapshotUserPageIndividualTweet> individualPages;
	private Map<Long,JavascriptInterchangeContainer> individualPageJICs;
	private Map<Long,List<String>> individualPageJSONStringList;
	private Map<Long,String> individualPageURLs;

	private ITweetUser user;
	private final String timelineURL;

	public SearchRunTimelineData( final IPreferences prefs,
								final String handleToCheck,
								final int numberOfTimelinePagesToScroll,
								final int numberOfIndividualPagesToScroll,
								final int numberOfReplyPagesToCheck,
								final int numberOfTimelineTweetsToSkip ) {
		super( prefs, handleToCheck, numberOfTimelinePagesToScroll, numberOfIndividualPagesToScroll, numberOfReplyPagesToCheck, numberOfTimelineTweetsToSkip );

		this.timelineURL = String.format( prefs.getValue( "targetsite.pattern.timeline" ), getHandleToCheck() );

		this.individualPages = new HashMap<Long,ISnapshotUserPageIndividualTweet>();
		this.individualPageJICs = new HashMap<Long,JavascriptInterchangeContainer>();
		this.individualPageJSONStringList = new HashMap<Long,List<String>>();
		this.individualPageURLs = new HashMap<Long,String>();
	}

	public String getTimelineURL() {
		return timelineURL;
	}

	public ISnapshotUserPageTimeline getTimeline() {
		return timeline;
	}

	public void setTimeline( ISnapshotUserPageTimeline val ) {
		this.timeline = val;
	}

	public List<String> getTimelineJSONStringList() {
		return timelineJSONStringList;
	}

	public void setTimelineJSONStringList( List<String> val ) {
		timelineJSONStringList = val;
	}

	public Map<Long,ISnapshotUserPageIndividualTweet> getIndividualPages() {
		return individualPages;
	}

	public ISnapshotUserPageIndividualTweet getIndividualPage( Long id ) {
		return individualPages.get( id );
	}

	public void setIndividualPage( Long id, ISnapshotUserPageIndividualTweet val ) {
		individualPages.put( id, val );
	}

	public Map<Long,JavascriptInterchangeContainer> getIndividualPageJICs() {
		return individualPageJICs;
	}

	public JavascriptInterchangeContainer getIndividualPageJIC( Long id ) {
		return individualPageJICs.get( id );
	}

	public void setIndividualPageJIC( Long id, JavascriptInterchangeContainer val ) {
		individualPageJICs.put( id, val );
	}

	public Map<Long,List<String>> getIndividualPageJSONStringLists() {
		return individualPageJSONStringList;
	}

	public List<String> getIndividualPageJSONStringList( Long id ) {
		return individualPageJSONStringList.get( id );
	}

	public void setIndividualPageJSONStringList( Long id, List<String> val ) {
		individualPageJSONStringList.put( id, val );
	}

	public Map<Long,String> getIndividualPageURLs() {
		return individualPageURLs;
	}

	public String getIndividualPageURL( Long id ) {
		return individualPageURLs.get( id );
	}

	public void setIndividualPageURL( Long id, String val ) {
		individualPageURLs.put( id, val );
	}

	public JavascriptInterchangeContainer getTimelineJIC() {
		return timelineJIC;
	}

	public void setTimelineJIC( JavascriptInterchangeContainer val ) {
		timelineJIC = val;
	}

	public ITweetUser getUser() {
		return user;
	}

	public void setUser( ITweetUser val ) {
		this.user = val;
	}

	public String toString() {
		return "car with license plate number ";
	}
}
