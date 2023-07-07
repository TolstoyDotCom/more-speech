/*
 * Copyright 2023 Chris Kelly
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
package com.tolstoy.censorship.twitter.checker.app.webdriver;

import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersFactory;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersBuilder;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersSetBuilder;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PageParametersFactory implements IPageParametersFactory {
	private static final Logger logger = LogManager.getLogger( PageParametersFactory.class );

	public PageParametersFactory() {
	}

	public IPageParametersBuilder makePageParametersBuilder() {
		return new PageParametersBuilder();
	}

	public IPageParametersSetBuilder makePageParametersSetBuilder() {
		return new PageParametersSetBuilder();
	}
}
