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
package com.tolstoy.basic.app.tweet;

import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.TweetUserVerifiedStatus;
import com.tolstoy.basic.app.utils.Utils;

@JsonIgnoreProperties(ignoreUnknown=true)
class TweetUser implements ITweetUser {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( TweetUser.class );

	@JsonProperty
	private String handle;

	@JsonProperty
	private long id;

	@JsonProperty
	private String displayName;

	@JsonProperty
	private String avatarURL;

	@JsonProperty
	private TweetUserVerifiedStatus verifiedStatus;

	@JsonProperty
	private int numTotalTweets;

	@JsonProperty
	private int numFollowers;

	@JsonProperty
	private int numFollowing;

	TweetUser( String handle,
						long id,
						String displayName,
						TweetUserVerifiedStatus verifiedStatus,
						String avatarURL ) {
		this( handle, id, displayName, verifiedStatus, avatarURL, 0, 0, 0 );
	}

	TweetUser( @JsonProperty("handle") String handle,
						@JsonProperty("id") long id,
						@JsonProperty("displayName") String displayName,
						@JsonProperty("verifiedStatus") TweetUserVerifiedStatus verifiedStatus,
						@JsonProperty("avatarURL") String avatarURL,
						@JsonProperty("numTotalTweets") int numTotalTweets,
						@JsonProperty("numFollowers") int numFollowers,
						@JsonProperty("numFollowing") int numFollowing ) {
		if ( Utils.isEmpty( handle ) ) {
			throw new IllegalArgumentException( "handle cannot be empty" );
		}

		this.handle = StringUtils.strip( handle, " @\t\n\r" ).toLowerCase();
		if ( Utils.isEmpty( this.handle ) ) {
			throw new IllegalArgumentException( "handle cannot be empty" );
		}

		this.id = id;
		this.displayName = ( !Utils.isEmpty( displayName ) ? displayName : handle );
		this.verifiedStatus = verifiedStatus;
		this.avatarURL = avatarURL;

		this.numTotalTweets = numTotalTweets;
		this.numFollowers = numFollowers;
		this.numFollowing = numFollowing;
	}

	@Override
	public String getHandle() {
		return handle;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getAvatarURL() {
		return avatarURL;
	}

	@Override
	public long getID() {
		return id;
	}

	@Override
	public TweetUserVerifiedStatus getVerifiedStatus() {
		return verifiedStatus;
	}

	@Override
	public int getNumTotalTweets() {
		return numTotalTweets;
	}

	@Override
	public int getNumFollowers() {
		return numFollowers;
	}

	@Override
	public int getNumFollowing() {
		return numFollowing;
	}

	@Override
	public int hashCode() {
		return Objects.hash( handle, displayName, id, verifiedStatus, avatarURL );
	}

	@Override
	public boolean equals( Object obj ) {
		if ( !( obj instanceof TweetUser ) ) {
			return false;
		}

		TweetUser other = (TweetUser) obj;

		return handle.equals( other.handle ) &&
				displayName.equals( other.displayName ) &&
				avatarURL.equals( other.avatarURL ) &&
				verifiedStatus == verifiedStatus &&
				id == other.id;
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "handle", handle )
		.append( "id", id )
		.append( "displayName", displayName )
		.append( "verifiedStatus", verifiedStatus )
		.append( "avatarURL", avatarURL )
		.toString();
	}
}
