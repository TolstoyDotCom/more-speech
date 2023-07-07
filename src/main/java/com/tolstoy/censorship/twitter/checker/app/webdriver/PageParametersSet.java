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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class PageParametersSet implements IPageParametersSet {
	private static final Logger logger = LogManager.getLogger( PageParametersSet.class );

	private final IPageParameters timeline, individualPage;

	PageParametersSet( final IPageParameters timeline, final IPageParameters individualPage ) throws Exception {
		this.timeline = timeline;
		this.individualPage = individualPage;

		if ( this.timeline == null || this.individualPage == null ) {
			throw new IllegalArgumentException( "timeline " + this.timeline + " and/or individualPage " + this.individualPage + " is null" );
		}
	}

	public IPageParameters getTimeline() {
		return timeline;
	}

	public IPageParameters getIndividualPage() {
		return individualPage;
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "timeline", timeline )
		.append( "individualPage", individualPage )
		.toString();
	}
}
