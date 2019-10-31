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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.censorship.twitter.checker.api.installation.IBrowserScript;

public class BrowserScript implements IBrowserScript {
	private static final Logger logger = LogManager.getLogger( BrowserScript.class );

	private final String name, script;

	public BrowserScript( final String name, final String script ) {
		this.name = name;
		this.script = script;
	}

	public String getName() {
		return name;
	}

	public String getScript() {
		return script;
	}
}
