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
package com.tolstoy.censorship.twitter.checker.api.analyzer;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.app.utils.Utils;

public class AnalyzedTweetDateComparator implements Comparator<IAnalyzedTweet>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7302056076095931759L;

	private static final Logger logger = LogManager.getLogger( AnalyzedTweetDateComparator.class );

	private final AnalyzedTweetComparatorDirection direction;

	public AnalyzedTweetDateComparator( final AnalyzedTweetComparatorDirection direction ) {
		this.direction = direction;
	}

	@Override
	public int compare( final IAnalyzedTweet a, final IAnalyzedTweet b ) {
		final int dateA = Utils.parseIntDefault( a.getTweet().getAttribute( "time" ) );
		final int dateB = Utils.parseIntDefault( b.getTweet().getAttribute( "time" ) );

		return direction == AnalyzedTweetComparatorDirection.DESC ? dateB - dateA : dateA - dateB;
	}
}
