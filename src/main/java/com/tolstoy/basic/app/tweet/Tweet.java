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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.time.Instant;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.TweetSupposedQuality;
import com.tolstoy.basic.api.tweet.IEntityAttributeDescriptor;
import com.tolstoy.basic.api.tweet.IEntityAttributeDescriptorSet;
import com.tolstoy.basic.api.tweet.EntityAttributeDescriptorType;
import com.tolstoy.basic.app.utils.StringList;
import com.tolstoy.basic.app.utils.Utils;

@JsonIgnoreProperties(ignoreUnknown=true)
class Tweet implements ITweet {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( Tweet.class );

	@JsonIgnore
	private static final String TWEETTEXT_EMPTY_MARKER = "[empty]";

	@JsonIgnore
	private static final String[] DEBUG_STRING_PREV_NEXT_MESSAGES = {
		"NO_PN",
		"NO_P",
		"NO_N",
		"has_pn"
	};

	@JsonIgnore
	private static final EntityAttributeDescriptorSet entityAttributeDescriptorSet;

	static {
		final EntityAttributeDescriptor[] entityAttributeDescriptors = {
			new EntityAttributeDescriptor( "id", "0", EntityAttributeDescriptorType.PRIMARY_KEY ),
			new EntityAttributeDescriptor( "handle", "placeholder_handle" ),
			new EntityAttributeDescriptor( "displayName", "" ),
			new EntityAttributeDescriptor( "verifiedStatus", "UNKNOWN" ),
			new EntityAttributeDescriptor( "numTotalTweets", "0" ),
			new EntityAttributeDescriptor( "numFollowers", "0" ),
			new EntityAttributeDescriptor( "numFollowing", "0" ),
			new EntityAttributeDescriptor( "errors", "" ),
			new EntityAttributeDescriptor( "avatarURL", "", EntityAttributeDescriptorType.SCALAR, "avatar_url" ),
			new EntityAttributeDescriptor( "componentcontext", "" ),
			new EntityAttributeDescriptor( "conversationid", "0" ),
			new EntityAttributeDescriptor( "datestring", "" ),
			new EntityAttributeDescriptor( "disclosuretype", "" ),
			new EntityAttributeDescriptor( "favoritecount", "0" ),
			new EntityAttributeDescriptor( "followsyou", "" ),
			new EntityAttributeDescriptor( "fullname", "" ),
			new EntityAttributeDescriptor( "hascards", "" ),
			new EntityAttributeDescriptor( "hasparenttweet", "" ),
			new EntityAttributeDescriptor( "innertweetid", "" ),
			new EntityAttributeDescriptor( "innertweetrawhref", "" ),
			new EntityAttributeDescriptor( "is_pinned", "" ),
			new EntityAttributeDescriptor( "is_toptweet", "" ),
			new EntityAttributeDescriptor( "isreplyto", "" ),
			new EntityAttributeDescriptor( "itemid", "" ),
			new EntityAttributeDescriptor( "iterationindex", "0" ),
			new EntityAttributeDescriptor( "iterationnumber", "0" ),
			new EntityAttributeDescriptor( "name", "" ),
			new EntityAttributeDescriptor( "nexttweetid", "0" ),
			new EntityAttributeDescriptor( "permalinkpath", "" ),
			new EntityAttributeDescriptor( "photourl", "" ),
			new EntityAttributeDescriptor( "previoustweetid", "0" ),
			new EntityAttributeDescriptor( "quality", "unknown_quality" ),
			new EntityAttributeDescriptor( "repliedtohandle", "" ),
			new EntityAttributeDescriptor( "repliedtouserid", "0" ),
			new EntityAttributeDescriptor( "replycount", "0" ),
			new EntityAttributeDescriptor( "replytousersjson", "" ),
			new EntityAttributeDescriptor( "retweetcount", "0" ),
			new EntityAttributeDescriptor( "retweetid", "0" ),
			new EntityAttributeDescriptor( "screenname", "" ),
			new EntityAttributeDescriptor( "suggestionjson", "" ),
			new EntityAttributeDescriptor( "time", "0" ),
			new EntityAttributeDescriptor( "tweetclasses", "", EntityAttributeDescriptorType.SCALAR, "class" ),
			new EntityAttributeDescriptor( "tweethtml", "" ),
			new EntityAttributeDescriptor( "tweetid", "0" ),
			new EntityAttributeDescriptor( "tweetlanguage", "" ),
			new EntityAttributeDescriptor( "tweetmentions", "" ),
			new EntityAttributeDescriptor( "tweetnonce", "" ),
			new EntityAttributeDescriptor( "tweetphoto_image", "" ),
			new EntityAttributeDescriptor( "tweetphoto_link", "" ),
			new EntityAttributeDescriptor( "tweetstatinitialized", "" ),
			new EntityAttributeDescriptor( "tweettext", "" ),
			new EntityAttributeDescriptor( "user", "0", EntityAttributeDescriptorType.OBJECT ),
			new EntityAttributeDescriptor( "userid", "0" ),
			new EntityAttributeDescriptor( "username", "" ),
			new EntityAttributeDescriptor( "verifiedText", "" ),
			new EntityAttributeDescriptor( "videothumburl", "" ),
			new EntityAttributeDescriptor( "youblock", "" ),
			new EntityAttributeDescriptor( "youfollow", "" ),
			new EntityAttributeDescriptor( "errors", "" )
		};

		entityAttributeDescriptorSet = new EntityAttributeDescriptorSet( entityAttributeDescriptors );
	}

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

	Tweet( final long id, final Map<String,String> attributes, final StringList classes, final StringList mentions, final ITweetUser user ) {
		this.id = id;
		this.attributes = attributes;
		this.classes = classes;
		this.mentions = mentions;
		this.user = user;
	}

	@JsonIgnore
	@Override
	public IEntityAttributeDescriptorSet getAttributeDescriptorSet() {
		return entityAttributeDescriptorSet;
	}

	@Override
	public long getID() {
		return id;
	}

	@Override
	public void setID( final long id ) {
		this.id = id;
	}

	@JsonIgnore
	@Override
	public String getSummary() {
		final StringBuffer sb = new StringBuffer( 500 );

		final String dateStr = Utils.formatTimestampString( getAttribute( "time" ), "date unknown" );

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
		sb.append( getTweettextPlain( 0 ) );

		sb.append( "]" );

		return sb.toString();
	}

	@Override
	public ITweetUser getUser() {
		return user;
	}

	@Override
	public void setUser( final ITweetUser user ) {
		this.user = user;
	}

	@JsonIgnore
	@Override
	public TweetSupposedQuality getSupposedQuality() {
		return TweetSupposedQuality.getMatching( Utils.trimDefault( getAttribute( "quality" ) ) );
	}

	@JsonIgnore
	@Override
	public void setSupposedQuality( final TweetSupposedQuality supposedQuality ) {
		setAttribute( "quality", "" + supposedQuality );
	}

	@Override
	public String getAttribute( final String key ) {
		return attributes.get( key );
	}

	@Override
	public void setAttribute( final String key, final String value ) {
		attributes.put( key, value );
	}

	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes( final Map<String,String> attributes ) {
		this.attributes = attributes;
	}

	@JsonIgnore
	@Override
	public void loadFromMap( final Map<String,String> sourceMap ) {
		for ( IEntityAttributeDescriptor descriptor : getAttributeDescriptorSet().getDescriptors() ) {
			if ( descriptor.getType() == EntityAttributeDescriptorType.PRIMARY_KEY ) {
				if ( getID() == 0 && sourceMap.containsKey( descriptor.getKey() ) ) {
					long tempID = Utils.parseLongDefault( sourceMap.get( descriptor.getKey() ) );
					if ( tempID != 0 ) {
						setID( tempID );
					}
				}
			}
			else if ( descriptor.getType() == EntityAttributeDescriptorType.SCALAR ) {
				String value = ObjectUtils.firstNonNull( sourceMap.get( descriptor.getKey() ),
															sourceMap.get( descriptor.getKeyAlias() ),
															descriptor.getDefaultValue() );

				attributes.put( descriptor.getKey(), value );
			}
		}

		String tempTime = attributes.get( "time" );
		String tempDateString = sourceMap.get( "datestring" );

		if ( !Utils.isEmpty( tempDateString ) && ( Utils.isEmpty( tempTime ) || "0".equals( tempTime ) ) ) {
			try {
				long seconds = Instant.parse( tempDateString ).getEpochSecond();
				attributes.put( "time", "" + seconds );
			}
			catch ( Exception e ) {
				logger.info( "cannot parse date " + tempDateString );
			}
		}
	}

	@JsonIgnore
	@Override
	public Map<String,String> getAsMapBasic() {
		final Map<String,String> ret = new HashMap<String,String>();

		ret.put( "time", Utils.trimDefault( getAttribute( "time" ), "0" ) );
		ret.put( "id", "" + getID() );
		ret.put( "username", getUser().getHandle() );
		ret.put( "verifiedText", "" + getUser().getVerifiedStatus() );
		ret.put( "quality", "" + getSupposedQuality() );
		ret.put( "retweetcount", Utils.trimDefault( getAttribute( "retweetcount" ), "-1" ) );
		ret.put( "favoritecount", Utils.trimDefault( getAttribute( "favoritecount" ), "-1" ) );
		ret.put( "replycount", Utils.trimDefault( getAttribute( "replycount" ), "-1" ) );
		ret.put( "tweettext", getTweettextPlain( 0 ) );

		return ret;
	}

	@Override
	public StringList getClasses() {
		return classes;
	}

	@Override
	public void setClasses( final StringList classes ) {
		this.classes = classes;
	}

	@Override
	public StringList getMentions() {
		return mentions;
	}

	@Override
	public void setMentions( final StringList mentions ) {
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
		return !Utils.isEmpty( getAttribute( "permalinkpath" ) );
	}

		//if hasparenttweet="true" && isreplyto="true", ID of the tweet replied to will be conversationid
	@JsonIgnore
	@Override
	public long getRepliedToTweetID() {
		if ( !Utils.isStringTrue( getAttribute( "hasparenttweet" ) ) ||
				!Utils.isStringTrue( getAttribute( "isreplyto" ) ) ) {
			return 0L;
		}

		return Utils.parseLongDefault( getAttribute( "conversationid" ) );
	}

	@JsonIgnore
	@Override
	public String getRepliedToHandle() {
		return Utils.trimDefault( getAttribute( "repliedtohandle" ) );
	}

	@JsonIgnore
	@Override
	public long getRepliedToUserID() {
		return Utils.parseLongDefault( getAttribute( "repliedtouserid" ) );
	}

	@JsonIgnore
	@Override
	public List<String> supplementFrom( final ITweet other ) {
		final List<String> warnings = new ArrayList<String>( 10 );

		if ( this.getID() == 0L ) {
			this.setID( other.getID() );
			this.setAttribute( "tweetid", "" + other.getID() );
			warnings.add( "Tweet " + this.getID() + " replaced ID" );
		}
		else if ( other.getID() != 0L && other.getID() != this.getID() ) {
			warnings.add( "Tweet " + this.getID() + " MISMATCH, other id is " + other.getID() );
		}

		if ( this.getClasses() == null || Utils.isEmpty( this.getClasses().getOriginal() ) ) {
			this.setClasses( new StringList( other.getClasses() != null ? other.getClasses().getOriginal() : "" ) );
			warnings.add( "Tweet " + this.getID() + " replaced classes" );
		}
		else if ( other.getClasses() != null && !other.getClasses().equals( this.getClasses() ) ) {
			warnings.add( "Tweet " + this.getID() + " MISMATCH, other classes is " + other.getClasses() );
		}

		if ( this.getMentions() == null || Utils.isEmpty( this.getMentions().getOriginal() ) ) {
			this.setMentions( new StringList( other.getMentions() != null ? other.getMentions().getOriginal() : "" ) );
			warnings.add( "Tweet " + this.getID() + " replaced mentions" );
		}
		else if ( other.getMentions() != null && !other.getMentions().equals( this.getMentions() ) ) {
			warnings.add( "Tweet " + this.getID() + " MISMATCH, other mentions is " + other.getMentions() );
		}

		for ( IEntityAttributeDescriptor descriptor : getAttributeDescriptorSet().getDescriptors() ) {
			if ( descriptor.getType() == EntityAttributeDescriptorType.SCALAR ) {
				this.setAttribute( descriptor.getKey(), Utils.chooseLeastEmpty( this.getAttribute( descriptor.getKey() ),
																				other.getAttribute( descriptor.getKey() ),
																				descriptor.getDefaultValue() ) );
			}
		}

		return warnings;
	}

	@JsonIgnore
	@Override
	public String toDebugString( final String indent ) {
		List<String> list = new ArrayList<String>( 10 );

		String text = StringUtils.rightPad( getTweettextPlain( 20 ), 20 );

		list.add( getID() != 0 ? "id=" + getID() : "NO_ID" );
		list.add( !TWEETTEXT_EMPTY_MARKER.equals( text ) ? "txt=" + text : "NO_TXT" );
		list.add( !Utils.isEmpty( getAttribute( "userid" ) ) ? "uid=" + getAttribute( "userid" ) : "NO_UID" );
		list.add( !Utils.isEmpty( getAttribute( "username" ) ) ? "unm=" + getAttribute( "username" ) : "NO_UNM" );
		list.add( !Utils.isEmptyOrZero( getAttribute( "time" ) ) ? "time=" + getAttribute( "time" ) : "NO_TIME" );
		list.add( !Utils.isEmpty( getAttribute( "verifiedText" ) ) ? "ver=" + getAttribute( "verifiedText" ) : "NO_VER" );
		list.add( getSupposedQuality() != null ? "q=" + getSupposedQuality() : "NO_QUAL" );

		list.add( "favs=" + ( Utils.isEmpty( getAttribute( "favoritecount" ) ) ? "0" : getAttribute( "favoritecount" ) ) );
		list.add( "repls=" + ( Utils.isEmpty( getAttribute( "replycount" ) ) ? "0" : getAttribute( "replycount" ) ) );
		list.add( "rts=" + ( Utils.isEmpty( getAttribute( "retweetcount" ) ) ? "0" : getAttribute( "retweetcount" ) ) );

		if ( !Utils.isEmpty( getAttribute( "permalinkpath" ) ) ) {
			if ( getAttribute( "permalinkpath" ).indexOf( "/" ) > -1 ) {
				list.add( "has_prmlk" );
			}
			else {
				list.add( "BAD_PRMLK=" + getAttribute( "permalinkpath" ) );
			}
		}
		else {
			list.add( "NO_PRMLK" );
		}

		list.add( DEBUG_STRING_PREV_NEXT_MESSAGES[ ( 2 * ( !Utils.isEmpty( getAttribute( "previoustweetid" ) ) ? 1 : 0 ) ) + ( !Utils.isEmpty( getAttribute( "nexttweetid" ) ) ? 1 : 0 ) ] );

		list.add( getUser() != null ? "user=[" + getUser().toDebugString( "" ) + "]" : "NO_USER" );

		return indent + StringUtils.join( list, ", " );
	}

	@JsonIgnore
	private String getTweettextPlain( final int maxLen ) {
		String text = getAttribute( "tweettext" );

		if ( Utils.isEmpty( text ) ) {
			return TWEETTEXT_EMPTY_MARKER;
		}

		text = Utils.removeNewlines( Utils.trimDefault( StringEscapeUtils.escapeHtml4( Utils.replaceAllEmojis( text ) ) ) );

		return maxLen > 0 ? StringUtils.substring( text, 0, 20 ) : text;
	}

	@Override
	public int hashCode() {
		return Objects.hash( attributes, classes, mentions, id );
	}

	@Override
	public boolean equals( final Object obj ) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true; 
		}

		if ( obj.getClass() != getClass() ) {
			return false;
		}

		final Tweet other = (Tweet) obj;

		return new EqualsBuilder()
			.appendSuper( super.equals( obj ) )
			.append( attributes, other.attributes )
			.append( classes, other.classes )
			.append( mentions, other.mentions )
			.append( id, other.id )
			.isEquals();
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
