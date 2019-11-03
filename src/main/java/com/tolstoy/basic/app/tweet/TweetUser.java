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
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.api.tweet.ITweetUser;
import com.tolstoy.basic.api.tweet.TweetUserVerifiedStatus;
import com.tolstoy.basic.api.tweet.IEntityAttributeDescriptor;
import com.tolstoy.basic.api.tweet.IEntityAttributeDescriptorSet;
import com.tolstoy.basic.api.tweet.EntityAttributeDescriptorType;
import com.tolstoy.basic.app.utils.Utils;

@JsonIgnoreProperties(ignoreUnknown=true)
class TweetUser implements ITweetUser {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( TweetUser.class );

	private static final String PLACEHOLDER_HANDLE = "placeholder_handle";

	@JsonIgnore
	private static final EntityAttributeDescriptorSet entityAttributeDescriptorSet;

	static {
		final EntityAttributeDescriptor[] entityAttributeDescriptors = {
			new EntityAttributeDescriptor( "id", "0", EntityAttributeDescriptorType.PRIMARY_KEY ),
			new EntityAttributeDescriptor( "handle", "placeholder_handle" ),
			new EntityAttributeDescriptor( "displayName", "", EntityAttributeDescriptorType.SCALAR, "display_name" ),
			new EntityAttributeDescriptor( "verifiedStatus", "UNKNOWN", EntityAttributeDescriptorType.SCALAR, "verified_status" ),
			new EntityAttributeDescriptor( "avatarURL", "", EntityAttributeDescriptorType.SCALAR, "avatar_url" ),
			new EntityAttributeDescriptor( "numTotalTweets", "0", EntityAttributeDescriptorType.SCALAR, "num_total_tweets" ),
			new EntityAttributeDescriptor( "numFollowers", "0", EntityAttributeDescriptorType.SCALAR, "num_followers" ),
			new EntityAttributeDescriptor( "numFollowing", "0", EntityAttributeDescriptorType.SCALAR, "num_following" ),
			new EntityAttributeDescriptor( "errors", "" )
		};

		entityAttributeDescriptorSet = new EntityAttributeDescriptorSet( entityAttributeDescriptors );
	}

	@JsonProperty
	private Map<String,String> attributes;

	@JsonProperty
	private long id;

	TweetUser( final String handle,
				final long id,
				final String displayName,
				final TweetUserVerifiedStatus verifiedStatus,
				final String avatarURL ) {
		this( handle, id, displayName, verifiedStatus, avatarURL, 0, 0, 0 );
		this.normalizeHandle();
	}

	TweetUser( @JsonProperty("handle") final String handle,
				@JsonProperty("id") final long id,
				@JsonProperty("displayName") final String displayName,
				@JsonProperty("verifiedStatus") final TweetUserVerifiedStatus verifiedStatus,
				@JsonProperty("avatarURL") final String avatarURL,
				@JsonProperty("numTotalTweets") final int numTotalTweets,
				@JsonProperty("numFollowers") final int numFollowers,
				@JsonProperty("numFollowing") final int numFollowing ) {
		if ( Utils.isEmpty( handle ) ) {
			throw new IllegalArgumentException( "handle cannot be empty" );
		}

		this.id = id;

		this.attributes = new HashMap<String,String>();

		this.attributes.put( "handle", handle );
		if ( Utils.isEmpty( this.attributes.get( "handle" ) ) ) {
			throw new IllegalArgumentException( "handle cannot be empty" );
		}

		this.normalizeHandle();

		this.attributes.put( "displayName", ( !Utils.isEmpty( displayName ) ? displayName : this.attributes.get( "handle" ) ) );
		this.attributes.put( "verifiedStatus", "" + verifiedStatus );
		this.attributes.put( "avatarURL", avatarURL );

		this.attributes.put( "numTotalTweets", "" + numTotalTweets );
		this.attributes.put( "numFollowers", "" + numFollowers );
		this.attributes.put( "numFollowing", "" + numFollowing );
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
	public void setID( long id ) {
		this.id = id;
	}

	@Override
	public String getHandle() {
		return getAttribute( "handle" );
	}

	@Override
	public void setHandle( String handle ) {
		setAttribute( "handle", handle );
	}

	@Override
	public String getDisplayName() {
		return getAttribute( "displayName" );
	}

	@Override
	public String getAvatarURL() {
		return getAttribute( "avatarURL" );
	}

	@Override
	public TweetUserVerifiedStatus getVerifiedStatus() {
		return TweetUserVerifiedStatus.getMatching( Utils.trimDefault( getAttribute( "verifiedStatus" ) ) );
	}

	@Override
	public int getNumTotalTweets() {
		return Utils.parseIntDefault( getAttribute( "numTotalTweets" ) );
	}

	@Override
	public int getNumFollowers() {
		return Utils.parseIntDefault( getAttribute( "numFollowers" ) );
	}

	@Override
	public int getNumFollowing() {
		return Utils.parseIntDefault( getAttribute( "numFollowing" ) );
	}

	@Override
	public String getAttribute( final String key ) {
		return attributes.get( key );
	}

	@Override
	public void setAttribute( final String key, final String value ) {
		attributes.put( key, value );

		if ( "handle".equals( key ) ) {
			this.normalizeHandle();
		}
	}

	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes( final Map<String,String> attributes ) {
		this.attributes = attributes;

		this.normalizeHandle();
	}

	@JsonIgnore
	@Override
	public void loadFromMap( Map<String,String> sourceMap ) {
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

		this.normalizeHandle();
	}

	@JsonIgnore
	@Override
	public List<String> supplementFrom( ITweetUser other ) {
		final List<String> warnings = new ArrayList<String>( 10 );

		if ( this.getID() == 0L ) {
			this.id = other.getID();
			warnings.add( "User " + this.getID() + " replaced ID" );
		}
		else if ( other.getID() != 0L && other.getID() != this.getID() ) {
			warnings.add( "User " + this.getID() + " MISMATCH, other id is " + other.getID() );
		}

		if ( Utils.isEmpty( this.getHandle() ) ) {
			this.setAttribute( "handle", !Utils.isEmpty( other.getHandle() ) ? other.getHandle() : this.PLACEHOLDER_HANDLE );
			normalizeHandle();
			warnings.add( "User " + this.getID() + " replaced handle with " + this.getHandle() );
		}
		else if ( !Utils.isEmpty( other.getHandle() ) && !other.getHandle().equals( this.getHandle() ) ) {
			warnings.add( "User " + this.getID() + " MISMATCH, other handle is " + other.getHandle() + " but this handle is " + this.getHandle() );
		}

		if ( this.getVerifiedStatus() == null || this.getVerifiedStatus() == TweetUserVerifiedStatus.UNKNOWN ) {
			this.setAttribute( "verifiedStatus", other.getVerifiedStatus() != null ? "" + other.getVerifiedStatus() : "" + TweetUserVerifiedStatus.UNKNOWN );
			warnings.add( "User " + this.getID() + " replaced verifiedStatus with " + this.getVerifiedStatus() );
		}
		else if ( !( other.getVerifiedStatus() == null || other.getVerifiedStatus() == TweetUserVerifiedStatus.UNKNOWN ) &&
				other.getVerifiedStatus() != this.getVerifiedStatus() ) {
			warnings.add( "User " + this.getID() + " MISMATCH, other verifiedStatus is " + other.getVerifiedStatus() + " but this verifiedStatus is " + this.getVerifiedStatus() );
		}

		if ( Utils.isEmpty( this.getDisplayName() ) ) {
			this.setAttribute( "displayName", other.getDisplayName() != null ? other.getDisplayName() : "" );
		}

		if ( Utils.isEmpty( this.getAvatarURL() ) ) {
			this.setAttribute( "avatarURL", other.getAvatarURL() != null ? other.getAvatarURL() : "" );
		}

		if ( this.getNumTotalTweets() == 0 ) {
			this.setAttribute( "numTotalTweets", "" + other.getNumTotalTweets() );
		}

		if ( this.getNumFollowers() == 0 ) {
			this.setAttribute( "numFollowers", "" + other.getNumFollowers() );
		}

		if ( this.getNumFollowing() == 0 ) {
			this.setAttribute( "numFollowing", "" + other.getNumFollowing() );
		}

		return warnings;
	}

	@JsonIgnore
	@Override
	public String toDebugString( String indent ) {
		List<String> list = new ArrayList<String>( 10 );

		list.add( getID() != 0 ? "id=" + getID() : "NO_ID" );

		if ( Utils.isEmpty( getHandle() ) ) {
			list.add( "NO_H" );
		}
		else if ( getHandle() == PLACEHOLDER_HANDLE ) {
			list.add( "DEF_H" );
		}
		else {
			list.add( "h=" + getHandle() );
		}

		list.add( !Utils.isEmpty( getDisplayName() ) ? "disp=" + getDisplayName() : "NO_DISP" );

		list.add( getVerifiedStatus() != null ? "vs=" + getVerifiedStatus() : "NO_VS" );

		list.add( "ttls=" + getNumTotalTweets() );
		list.add( "flwrs=" + getNumFollowers() );
		list.add( "flwng=" + getNumFollowing() );

		return indent + StringUtils.join( list, ", " );
	}

	@JsonIgnore
	protected void normalizeHandle() {
		attributes.put( "handle", Utils.normalizeHandle( getHandle() ) );
	};

	@Override
	public int hashCode() {
		return Objects.hash( getHandle(), getDisplayName(), getID(), getVerifiedStatus(), getAvatarURL() );
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

		final TweetUser other = (TweetUser) obj;

		return new EqualsBuilder()
			.appendSuper( super.equals( obj ) )
			.append( getID(), other.getID() )
			.append( getHandle(), other.getHandle() )
			.append( getDisplayName(), other.getDisplayName() )
			.append( getAvatarURL(), other.getAvatarURL() )
			.append( getVerifiedStatus(), other.getVerifiedStatus() )
			.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "handle", getHandle() )
		.append( "id", getID() )
		.append( "displayName", getDisplayName() )
		.append( "verifiedStatus", getVerifiedStatus() )
		.append( "avatarURL", getAvatarURL() )
		.toString();
	}
}
