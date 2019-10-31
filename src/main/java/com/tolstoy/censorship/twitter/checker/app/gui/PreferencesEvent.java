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
package com.tolstoy.censorship.twitter.checker.app.gui;

import java.util.EventObject;
import java.util.Map;

public class PreferencesEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8910263205145961962L;
	private final Map<String,String> userdata;

	public PreferencesEvent( final Object source, final Map<String,String> userdata ) {
		super( source );

		this.userdata = userdata;
	}

	public Map<String,String> getUserdata() {
		return userdata;
	}
}
