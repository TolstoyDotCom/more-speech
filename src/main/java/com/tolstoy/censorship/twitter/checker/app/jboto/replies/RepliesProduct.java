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
package com.tolstoy.censorship.twitter.checker.app.jboto.replies;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunReplies;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunRepliesItinerary;
import com.tolstoy.censorship.twitter.checker.api.searchrun.ISearchRunTimeline;
import com.tolstoy.censorship.twitter.checker.api.snapshot.IReplyThread;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageTimeline;
import com.tolstoy.censorship.twitter.checker.api.preferences.IPreferences;
import com.tolstoy.censorship.twitter.checker.api.snapshot.ISnapshotUserPageIndividualTweet;
import com.tolstoy.censorship.twitter.checker.app.webdriver.ntjs.JavascriptInterchangeContainer;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.jboto.api.IProduct;
import com.tolstoy.censorship.twitter.checker.app.jboto.common.SearchRunBaseData;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersSet;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParameters;

public class RepliesProduct extends SearchRunBaseData {
	private static final Logger logger = LogManager.getLogger( RepliesProduct.class );

	private ISearchRunReplies searchRun;
	private final String timelineURL;
	private final ISearchRunRepliesItinerary itinerary;

	private ITweetUser user = null;
	private ISnapshotUserPageTimeline timeline = null;
	private Map<Long,ISnapshotUserPageIndividualTweet> individualPages;
	private Map<Long,JavascriptInterchangeContainer> individualPageJICs;
	private Map<Long,List<String>> individualPageJSONStringList;
	private Map<Long,String> individualPageURLs;
	private final Map<Long,IReplyThread> replyThreads;

	public RepliesProduct( final IPreferences prefs,
								final String handleToCheck,
								final ISearchRunRepliesItinerary itinerary,
								final IPageParametersSet pageParametersSet ) {
		super( prefs, handleToCheck, pageParametersSet );

		this.replyThreads = new HashMap<Long,IReplyThread>();
		this.itinerary = itinerary;
		this.timelineURL = String.format( prefs.getValue( "targetsite.pattern.timeline" ), getHandleToCheck() );
		this.individualPages = new HashMap<Long,ISnapshotUserPageIndividualTweet>();
		this.individualPageJICs = new HashMap<Long,JavascriptInterchangeContainer>();
		this.individualPageJSONStringList = new HashMap<Long,List<String>>();
		this.individualPageURLs = new HashMap<Long,String>();
	}

	public ISearchRunRepliesItinerary getItinerary() {
		return itinerary;
	}

	public String getTimelineURL() {
		return timelineURL;
	}

	public ISearchRunReplies getSearchRun() {
		return searchRun;
	}

	public void setSearchRun( ISearchRunReplies val ) {
		searchRun = val;
	}

	public ISnapshotUserPageTimeline getTimeline() {
		return timeline;
	}

	public void setTimeline( ISnapshotUserPageTimeline val ) {
		timeline = val;
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

	public Map<Long,IReplyThread> getReplyPages() {
		return replyThreads;
	}

	public void setReplyPage( Long id, IReplyThread replyThread ) {
		replyThreads.put( id, replyThread );
	}

	public ITweetUser getUser() {
		return user;
	}

	public void setUser( ITweetUser val ) {
		user = val;
	}

	public String toString() {
		return new ToStringBuilder( this )
		.appendSuper( super.toString() )
		.append( "timelineURL", timelineURL )
		.append( "user", user )
		.append( "individualPages", individualPages != null ? individualPages.keySet().size() : "[null]" )
		.append( "individualPageJICs", individualPageJICs != null ? individualPageJICs.keySet().size() : "[null]" )
		.append( "individualPageJSONStringList", individualPageJSONStringList != null ? individualPageJSONStringList.keySet().size() : "[null]" )
		.append( "individualPageURLs", individualPageURLs != null ? individualPageURLs.keySet().size() : "[null]" )
		.append( "replyThreads", replyThreads != null ? replyThreads.keySet().size() : "[null]" )
		//.append( "timeline", timeline )
		//.append( "itinerary", itinerary )
		.toString();
	}
}
