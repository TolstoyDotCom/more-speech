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

import java.util.*;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceBundleWithFormatting implements IResourceBundleWithFormatting {
	private static final Logger logger = LogManager.getLogger( ResourceBundleWithFormatting.class );

	private ResourceBundle bundle;
	private String name;

	public ResourceBundleWithFormatting( String name ) {
		this.name = name;
		this.bundle = ResourceBundle.getBundle( name );
	}

	public String getString( String key, Object... replacements ) {
		String pattern;

		try {
			pattern = bundle.getString( key );
		}
		catch ( Exception e ) {
			return "unknown";
		}

		if ( replacements.length < 1 ) {
			return pattern;
		}

		try {
			return String.format( pattern, replacements );
		}
		catch ( Exception e ) {
			return pattern;
		}
	}
}
