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

import java.util.Comparator;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tolstoy.basic.app.utils.Utils;

public class AnalyzedTweetRankingComparator implements Comparator<IAnalyzedTweet>, Serializable {
	private static final Logger logger = LogManager.getLogger( AnalyzedTweetRankingComparator.class );

	private AnalyzedTweetComparatorDirection direction;

	public AnalyzedTweetRankingComparator( AnalyzedTweetComparatorDirection direction ) {
		this.direction = direction;
	}

	@Override
	public int compare( IAnalyzedTweet a, IAnalyzedTweet b ) {
		double rankA = a.getRanking();
		double rankB = b.getRanking();

		return direction == AnalyzedTweetComparatorDirection.DESC ? Double.compare( rankB, rankA ) : Double.compare( rankA, rankB );
	}
}
