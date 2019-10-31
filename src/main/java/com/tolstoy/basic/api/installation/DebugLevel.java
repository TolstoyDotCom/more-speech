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
package com.tolstoy.basic.api.installation;

public enum DebugLevel {
	NONE( 0 ),
	TERSE( 1 ),
	VERBOSE( 2 );

	private final int intLevel;

	DebugLevel( final int intLevel ) {
		this.intLevel = intLevel;
	}

	public static DebugLevel getMatching( final String debugLevel ) {
		if ( debugLevel == null || debugLevel.length() < 1 ) {
			return NONE;
		}

		final String debugLevelLowercase = debugLevel.toLowerCase();

		for ( final DebugLevel targetDebugLevel : values() ) {
			if ( debugLevelLowercase.indexOf( "" + targetDebugLevel ) > -1 || debugLevelLowercase.equals( "" + targetDebugLevel.getAsInt() ) ) {
				return targetDebugLevel;
			}
		}

		return NONE;
	}

	public int getAsInt() {
		return intLevel;
	}
}
