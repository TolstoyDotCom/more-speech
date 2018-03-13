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
package com.tolstoy.censorship.twitter.checker.api.searchrun;

import java.util.Map;
import java.time.Instant;
import com.tolstoy.basic.api.tweet.ITweetUser;

public interface ISearchRun {
	long getID();
	void setID( long id );

	ITweetUser getInitiatingUser();
	void setInitiatingUser( ITweetUser user );

	Instant getStartTime();
	void setStartTime( Instant startTime );

	Instant getEndTime();
	void setEndTime( Instant endTime );

	/**
	 * Optional, free-form metadata such as "loggedin", etc.
	 * Don't rely on any of those being available.
	 */
	Map<String,String> getAttributes();
	void setAttributes( Map<String,String> attributes );

	String getAttribute( String key );
	void setAttribute( String key, String value );
}
