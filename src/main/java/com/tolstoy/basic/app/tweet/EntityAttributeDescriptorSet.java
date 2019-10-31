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

import java.util.Arrays;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.api.tweet.IEntityAttributeDescriptor;
import com.tolstoy.basic.api.tweet.IEntityAttributeDescriptorSet;

class EntityAttributeDescriptorSet implements IEntityAttributeDescriptorSet {
	private static final Logger logger = LogManager.getLogger( EntityAttributeDescriptorSet.class );

	private IEntityAttributeDescriptor[] descriptors;

	EntityAttributeDescriptorSet( IEntityAttributeDescriptor[] descriptors ) {
		this.descriptors = descriptors;
	}

	@Override
	public IEntityAttributeDescriptor[] getDescriptors() {
		return descriptors;
	}

	@Override
	public IEntityAttributeDescriptor getDescriptorByKey( String key ) {
		if ( key == null ) {
			return null;
		}

		for ( IEntityAttributeDescriptor descriptor: descriptors ) {
			if ( key.equals( descriptor.getKey() ) ) {
				return descriptor;
			}
		}

		return null;
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode( descriptors );
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

		final EntityAttributeDescriptorSet other = (EntityAttributeDescriptorSet) obj;

		return Arrays.deepEquals( this.descriptors, other.descriptors );
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "descriptors", descriptors )
		.toString();
	}
}
