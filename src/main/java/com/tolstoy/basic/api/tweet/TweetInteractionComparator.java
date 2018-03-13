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

import java.util.Comparator;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.app.utils.Utils;

public class TweetInteractionComparator implements Comparator<ITweet>, Serializable {
	private static final Logger logger = LogManager.getLogger( TweetInteractionComparator.class );

	private TweetComparatorDirection direction;
	private int boostReplies, boostRTs, boostFavorites;

	public TweetInteractionComparator( int boostReplies, int boostRTs, int boostFavorites, TweetComparatorDirection direction ) {
		this.boostReplies = boostReplies;
		this.boostRTs = boostRTs;
		this.boostFavorites = boostFavorites;
		this.direction = direction;
	}

	@Override
	public int compare( ITweet a, ITweet b ) {
		int scoreA = makeScore( Utils.parseIntDefault( a.getAttribute( "replycount" ) ),
							Utils.parseIntDefault( a.getAttribute( "retweetcount" ) ),
							Utils.parseIntDefault( a.getAttribute( "favoritecount" ) ) );

		int scoreB = makeScore( Utils.parseIntDefault( b.getAttribute( "replycount" ) ),
							Utils.parseIntDefault( b.getAttribute( "retweetcount" ) ),
							Utils.parseIntDefault( b.getAttribute( "favoritecount" ) ) );

		return direction == TweetComparatorDirection.DESC ? scoreB - scoreA : scoreA - scoreB;
	}

	protected int makeScore( int countReplies, int countRTs, int countFavorites ) {
		return ( boostReplies * ( countReplies + 1 ) )
				* ( boostRTs * ( countRTs + 1 ) )
				* ( boostFavorites * ( countFavorites + 1 ) );
	}
}
