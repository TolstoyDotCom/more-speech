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

import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParameters;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersSet;
import com.tolstoy.censorship.twitter.checker.api.webdriver.IPageParametersSetBuilder;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class PageParametersSetBuilder implements IPageParametersSetBuilder {
	private static final Logger logger = LogManager.getLogger( PageParametersSetBuilder.class );

	private IPageParameters timeline, individualPage;

	PageParametersSetBuilder() {
		this.timeline = null;
		this.individualPage = null;
	}

	public IPageParametersSetBuilder setTimeline( IPageParameters pageParameters ) {
		timeline = pageParameters;

		return this;
	}

	public IPageParametersSetBuilder setIndividualPage( IPageParameters pageParameters ) {
		individualPage = pageParameters;

		return this;
	}

	public IPageParametersSet build() throws Exception {
		if ( timeline == null || individualPage == null ) {
			throw new RuntimeException( "timeline or individualPage is null" );
		}

		return new PageParametersSet( timeline, individualPage );
	}
}
