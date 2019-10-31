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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyedLists<T> {
	private final Map<String,List<T>> lists;

	public KeyedLists() {
		this.lists = new HashMap<String,List<T>>( 10 );
	}

	public void add( final String key, final T item ) {
		if ( key == null || key.length() < 1 ) {
			throw new IllegalArgumentException( "key is null or empty" );
		}

		if ( item == null ) {
			throw new IllegalArgumentException( "item is null" );
		}

		List<T> list = lists.get( key );

		if ( list == null ) {
			list = new ArrayList<T>( 5 );
		}

		list.add( item );
	}

	public List<T> get( final String key ) {
		return lists.get( key );
	}
}
