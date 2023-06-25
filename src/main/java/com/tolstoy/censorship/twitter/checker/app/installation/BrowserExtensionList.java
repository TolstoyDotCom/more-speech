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
package com.tolstoy.censorship.twitter.checker.app.installation;

import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtension;
import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserExtensionList;

class BrowserExtensionList implements IBrowserExtensionList {
	private static final Logger logger = LogManager.getLogger( BrowserExtensionList.class );

	private final List<IBrowserExtension> list;

	public BrowserExtensionList() {
		this.list = new ArrayList<IBrowserExtension>();
	}

	public BrowserExtensionList( List<IBrowserExtension> list ) {
		this.list = list;
	}

	public List<IBrowserExtension> getList() {
		return list;
	}

	public void add( IBrowserExtension ext ) {
		list.add( ext );
	}

	public IBrowserExtension getByKey( String key ) {
		for ( IBrowserExtension ext : list ) {
			if ( ext.getKey().equals( key ) ) {
				return ext;
			}
		}

		return null;
	}
}
