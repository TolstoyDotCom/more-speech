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

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.TweetSupposedQuality;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.app.utils.StringList;

@JsonIgnoreProperties(ignoreUnknown=true)
class Tweet implements ITweet {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( Tweet.class );

	@JsonProperty
	private Map<String,String> attributes;

	@JsonProperty
	private StringList classes;

	@JsonProperty
	private StringList mentions;

	@JsonProperty
	private ITweetUser user;

	@JsonProperty
	private long id;

	Tweet() {
		this.id = 0;
		this.attributes = new HashMap<String,String>();
		this.classes = new StringList( "" );
		this.mentions = new StringList( "" );
		this.user = null;
	}

	Tweet( long id, Map<String,String> attributes, StringList classes, StringList mentions, ITweetUser user ) {
		this.id = id;
		this.attributes = attributes;
		this.classes = classes;
		this.mentions = mentions;
		this.user = user;
	}

	@Override
	public long getID() {
		return id;
	}

	@Override
	public void setID( long id ) {
		this.id = id;
	}

	@JsonIgnore
	@Override
	public Map<String,String> getAsMapBasic() {
		Map<String,String> ret = new HashMap<String,String>();

		ret.put( "time", Utils.trimDefault( getAttribute( "time" ), "0" ) );
		ret.put( "id", "" + getID() );
		ret.put( "user_from", getUser().getHandle() );
		ret.put( "user_verified", "" + getUser().getVerifiedStatus() );
		ret.put( "quality", "" + getSupposedQuality() );
		ret.put( "retweetcount", Utils.trimDefault( getAttribute( "retweetcount" ), "-1" ) );
		ret.put( "favoritecount", Utils.trimDefault( getAttribute( "favoritecount" ), "-1" ) );
		ret.put( "replycount", Utils.trimDefault( getAttribute( "replycount" ), "-1" ) );
		ret.put( "text", Utils.removeNewlines( Utils.trimDefault( StringEscapeUtils.escapeHtml4( Utils.removeAllEmojis( getAttribute( "tweettext" ) ) ) ) ) );

		return ret;
	}

	@JsonIgnore
	@Override
	public String getSummary() {
		StringBuffer sb = new StringBuffer( 500 );

		String dateStr = Utils.formatTimestampString( getAttribute( "time" ), "date unknown" );

		sb.append( "[tweet: " );
		sb.append( getID() );

		sb.append( ", from: @" );
		sb.append( getUser().getHandle() );

		sb.append( " (" );
		sb.append( getUser().getVerifiedStatus() );
		sb.append( ")" );

		sb.append( " on " );
		sb.append( dateStr );

		if ( getRepliedToTweetID() != 0 ) {
			sb.append( ", reply to " + getRepliedToTweetID() );
			sb.append( " from " );
			sb.append( getRepliedToHandle() );
			sb.append( " " );
		}

		sb.append( "(" );
		sb.append( getSupposedQuality() );

		sb.append( "," );
		sb.append( getAttribute( "retweetcount" ) );

		sb.append( "," );
		sb.append( getAttribute( "favoritecount" ) );

		sb.append( "," );
		sb.append( getAttribute( "replycount" ) );
		sb.append( ")" );

		sb.append( " " );
		sb.append( Utils.removeNewlines( Utils.trimDefault( StringEscapeUtils.escapeHtml4( Utils.removeAllEmojis( getAttribute( "tweettext" ) ) ) ) ) );

		sb.append( "]" );

		return sb.toString();
	}

	@Override
	public ITweetUser getUser() {
		return user;
	}

	@Override
	public void setUser( ITweetUser user ) {
		this.user = user;
	}

	@JsonIgnore
	@Override
	public TweetSupposedQuality getSupposedQuality() {
		return TweetSupposedQuality.getMatching( Utils.trimDefault( getAttribute( "quality" ) ) );
	}

	@Override
	public String getAttribute( String key ) {
		return attributes.get( key );
	}

	@Override
	public void setAttribute( String key, String value ) {
		attributes.put( key, value );
	}

	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes( Map<String,String> attributes ) {
		this.attributes = attributes;
	}

	@Override
	public StringList getClasses() {
		return classes;
	}

	@Override
	public void setClasses( StringList classes ) {
		this.classes = classes;
	}

	@Override
	public StringList getMentions() {
		return mentions;
	}

	@Override
	public void setMentions( StringList mentions ) {
		this.mentions = mentions;
	}

	/**
	 * A tweet with a null permalinkpath is assumed to not be valid.
	 * Individual tweet pages include a tweet with classes
	 * [RetweetDialog-tweet, modal-body, modal-tweet, tweet], and
	 * everything else is null.
	 * Tweets like that will not be valid.
	 */
	@JsonIgnore
	@Override
	public boolean isValid() {
		return !Utils.isEmpty( attributes.get( "permalinkpath" ) );
	}

		//if hasparenttweet="true" && isreplyto="true", ID of the tweet replied to will be conversationid
	@JsonIgnore
	@Override
	public long getRepliedToTweetID() {
		if ( !Utils.isStringTrue( attributes.get( "hasparenttweet" ) ) ||
				!Utils.isStringTrue( attributes.get( "isreplyto" ) ) ) {
			return 0L;
		}

		return Utils.parseLongDefault( attributes.get( "conversationid" ) );
	}

	@JsonIgnore
	@Override
	public String getRepliedToHandle() {
		return Utils.trimDefault( attributes.get( "repliedtohandle" ) );
	}

	@JsonIgnore
	@Override
	public long getRepliedToUserID() {
		return Utils.parseLongDefault( attributes.get( "repliedtouserid" ) );
	}

	@Override
	public int hashCode() {
		return Objects.hash( attributes, classes, mentions, id );
	}

	@Override
	public boolean equals( Object obj ) {
		if ( !( obj instanceof Tweet ) ) {
			return false;
		}

		Tweet other = (Tweet) obj;
		return Objects.equals( attributes, other.attributes ) &&
				Objects.equals( classes, other.classes ) &&
				Objects.equals( mentions, other.mentions ) &&
				Objects.equals( id, other.id );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "id", id )
		.append( "mentions", mentions )
		.append( "classes", classes )
		.append( "attributes", attributes )
		.toString();
	}
}
