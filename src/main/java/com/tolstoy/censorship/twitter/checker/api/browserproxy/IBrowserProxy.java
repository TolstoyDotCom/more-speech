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
package com.tolstoy.censorship.twitter.checker.api.browserproxy;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Map;

public interface IBrowserProxy {
	boolean start() throws Exception;
	void stop() throws Exception;

	Proxy getSeleniumProxy();

	void beginRecording( WebDriver driver, String name ) throws Exception;
	List<IBrowserProxyLogEntry> endRecording( WebDriver driver ) throws Exception;

	void addBrowserProxyResponseListener( final IBrowserProxyResponseListener l );
	void removeBrowserProxyResponseListener( final IBrowserProxyResponseListener l );
}
