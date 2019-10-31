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
package com.tolstoy.basic.api.tweet;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.app.utils.Utils;

public class TweetDateComparator implements Comparator<ITweet>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3843540037282075212L;

	private static final Logger logger = LogManager.getLogger( TweetDateComparator.class );

	private final TweetComparatorDirection direction;

	public TweetDateComparator( final TweetComparatorDirection direction ) {
		this.direction = direction;
	}

	@Override
	public int compare( final ITweet a, final ITweet b ) {
		final int dateA = Utils.parseIntDefault( a.getAttribute( "time" ) );
		final int dateB = Utils.parseIntDefault( b.getAttribute( "time" ) );

		return direction == TweetComparatorDirection.DESC ? dateB - dateA : dateA - dateB;
	}
}
