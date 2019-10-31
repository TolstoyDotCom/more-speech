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
package com.tolstoy.basic.api.tweet;

import java.util.List;
import java.util.Map;

public interface ITweetUser {
	IEntityAttributeDescriptorSet getAttributeDescriptorSet();

	long getID();
	void setID( long id );
	String getHandle();
	void setHandle( String handle );
	String getDisplayName();
	String getAvatarURL();
	TweetUserVerifiedStatus getVerifiedStatus();
	int getNumTotalTweets();
	int getNumFollowers();
	int getNumFollowing();

	Map<String,String> getAttributes();
	void setAttributes( final Map<String,String> attributes );
	String getAttribute( final String key );
	void setAttribute( final String key, final String value );
	void loadFromMap( Map<String,String> map );

	List<String> supplementFrom( ITweetUser other );
	String toDebugString( String indent );
}
