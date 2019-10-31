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

import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.IEntityAttributeDescriptor;
import com.tolstoy.basic.api.tweet.EntityAttributeDescriptorType;

public class EntityAttributeDescriptor implements IEntityAttributeDescriptor {
	private static final Logger logger = LogManager.getLogger( EntityAttributeDescriptor.class );

	private EntityAttributeDescriptorType type;
	private String key, defaultValue, description, keyAlias;

	public EntityAttributeDescriptor( String key, String defaultValue ) {
		this( key, defaultValue, EntityAttributeDescriptorType.SCALAR, "", key );
	}

	public EntityAttributeDescriptor( String key, String defaultValue, EntityAttributeDescriptorType type ) {
		this( key, defaultValue, type, key, "" );
	}

	public EntityAttributeDescriptor( String key, String defaultValue, EntityAttributeDescriptorType type, String keyAlias ) {
		this( key, defaultValue, type, keyAlias, "" );
	}

	public EntityAttributeDescriptor( String key, String defaultValue, EntityAttributeDescriptorType type, String keyAlias, String description ) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.type = type;
		this.description = description;
		this.keyAlias = keyAlias;
	}

	public String getKey() {
		return key;
	}

	public String getKeyAlias() {
		return keyAlias;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getDescription() {
		return description;
	}

	public EntityAttributeDescriptorType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hash( key, defaultValue, type, description );
	}

	@Override
	public boolean equals( final Object obj ) {
		if ( obj == null ) {
			return false;
		}

		if ( obj == this ) {
			return true; 
		}

		if ( obj.getClass() != getClass() ) {
			return false;
		}

		final EntityAttributeDescriptor other = (EntityAttributeDescriptor) obj;

		return new EqualsBuilder()
			.appendSuper( super.equals( obj ) )
			.append( key, other.key )
			.append( type, other.type )
			.append( defaultValue, other.defaultValue )
			.append( description, other.description )
			.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "key", key )
		.append( "type", type )
		.append( "defaultValue", defaultValue )
		.append( "description", description )
		.toString();
	}
}
