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
package com.tolstoy.basic.app.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StringList {
	private final List<String> items;
	private final String original;

	private static final String DELIMITERS = ", \t\r\n";
	private static final char JOINCHAR = ',';

	@JsonCreator
	public StringList( @JsonProperty("original") final String s ) {
		final String str = s != null ? s : "";

		final List<String> tempList = Arrays.asList( StringUtils.split( StringUtils.strip( str, DELIMITERS ), DELIMITERS ) );

		this.items = new ArrayList<String>( tempList.size() );

		for ( final String temp : tempList ) {
			if ( !Utils.isEmpty( temp ) ) {
				this.items.add( temp );
			}
		}

		this.original = StringUtils.join( this.items, JOINCHAR );
	}

	@JsonIgnore
	public List<String> getItems() {
		return new ArrayList<String>( items );
	}

	public String getOriginal() {
		return original;
	}

	@Override
	public int hashCode() {
		return Objects.hash( original );
	}

	@Override
	public boolean equals( final Object obj ) {
		if ( !( obj instanceof StringList ) ) {
			return false;
		}

		final StringList other = (StringList) obj;

		return Objects.equals( original, other.original );
	}

	@Override
	public String toString() {
		return original;
	}
}
