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
package com.tolstoy.censorship.twitter.checker.app.browserproxy;

import org.dizitart.jbus.Subscribe;

import com.tolstoy.censorship.twitter.checker.api.browserproxy.IBrowserProxyResponseListener;

public class BrowserProxyResponseListenerAdapter {
	private final IBrowserProxyResponseListener target;

	public BrowserProxyResponseListenerAdapter( final IBrowserProxyResponseListener target ) {
		this.target = target;
	}

	@Subscribe
	public void listen( final BrowserProxyResponseEventBUP event ) {
		target.responseEventFired( event );
	}
}
