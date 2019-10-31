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
package com.tolstoy.censorship.twitter.checker.app.analyzer;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalyzedTweet;
import com.tolstoy.censorship.twitter.checker.api.analyzer.ITweetRanker;

class TweetRankerBasic implements ITweetRanker {
	private static final Logger logger = LogManager.getLogger( TweetRankerBasic.class );

	private static final String RANKING_FUNCTION_NAME = "formula1";
	private static final int FEW_WORDS_LIMIT = 5;
	private static final double FEW_WORDS_PENALTY = -5.0d;
	private static final double FEW_WORDS_AND_PIC_PENALTY = -10.0d;
	private static final double MOSTLY_CAPS_PENALTY = -3.0d;
	private static final double FUZZY_DIVISOR = 10.0d;
	private static final double FUZZY_LIMIT = 2.8d;
	private static final double BOOST_FUZZY_OVER_LIMIT = 0.5d;
	private static final double FLESCH_DIVISOR = 100.0d;
	private static final double FOG_DIVISOR = 20.0d;
	private static final double KINCAID_DIVISOR = 20.0d;
	private static final double ARI_DIVISOR = 20.0d;
	private static final double COLEMAN_LIAU_DIVISOR = 20.0d;
	private static final double LIX_DIVISOR = 100.0d;
	private static final double SMOG_DIVISOR = 10.0d;
	private static final double COSINE_MIN_DISTANCE = 0.5d;
	private static final double COSINE_MULTIPLIER = 4.0d;
	private static final double JACCARD_MIN_DISTANCE = 0.75d;
	private static final double JACCARD_DIVISOR = 2.0d;
	private static final double JARO_WINKLER_MIN_DISTANCE = 0.75d;
	private static final double JARO_WINKLER_DIVISOR = 2.0d;
	private static final double NUM_WORDS_DIVISOR = 10.0d;
	private static final double BOOST_REPLIES = 5.0d;
	private static final double BOOST_RETWEETS = 3.0d;
	private static final double BOOST_FAVORITES = 2.0d;
	private static final double BOOST_DATE_RATIO = 2.0d;

	private static final DecimalFormat decimalFormat;

	static {
		decimalFormat = new DecimalFormat( "#.##" );
		decimalFormat.setRoundingMode( RoundingMode.CEILING );
	}

	public TweetRankerBasic() {
	}

	@Override
	public String getFunctionName() {
		return RANKING_FUNCTION_NAME;
	}

	@Override
	public void rankTweets( final List<IAnalyzedTweet> analyzedTweets, final IAnalyzedTweet referenceAnalyzedTweet ) {
		final int count = analyzedTweets.size();
		for ( final IAnalyzedTweet analyzedTweet : analyzedTweets ) {
			rankTweet( analyzedTweet, count, referenceAnalyzedTweet );
		}
	}

	@Override
	public void rankTweet( final IAnalyzedTweet analyzedTweet, final int count, final IAnalyzedTweet referenceAnalyzedTweet ) {
		double ranking = 0.0d, temp = 0.0d;

		temp = analyzedTweet.getToReferenceTweetFuzzyScore();
		if ( temp != 0 ) {
			temp = temp / FUZZY_DIVISOR;
			if ( temp > FUZZY_LIMIT ) {
				temp = temp * BOOST_FUZZY_OVER_LIMIT;
			}

			analyzedTweet.setAttribute( "rank_fuzzy", decimalFormat.format( temp ) );
			ranking += temp;
		}

		if ( analyzedTweet.getNumWords() <= FEW_WORDS_LIMIT ) {
			analyzedTweet.setAttribute( "rank_fww", "" + FEW_WORDS_PENALTY );
			ranking += FEW_WORDS_PENALTY;
			if ( analyzedTweet.getHasPic() || analyzedTweet.getHasCard() ) {
				analyzedTweet.setAttribute( "rank_fwwp", "" + FEW_WORDS_AND_PIC_PENALTY );
				ranking += FEW_WORDS_AND_PIC_PENALTY;
			}
		}

		if ( analyzedTweet.getMostlyCaps() ) {
			analyzedTweet.setAttribute( "rank_caps", "" + MOSTLY_CAPS_PENALTY );
			ranking += MOSTLY_CAPS_PENALTY;
		}

		temp = 200.0d - Math.min( 200.0d, analyzedTweet.getReadabilityFlesch() );
		temp = temp / FLESCH_DIVISOR;
		analyzedTweet.setAttribute( "rank_flesch", decimalFormat.format( temp ) );
		ranking += temp;

		temp = analyzedTweet.getReadabilityFog() / FOG_DIVISOR;
		analyzedTweet.setAttribute( "rank_fog", decimalFormat.format( temp ) );
		ranking += temp;

		temp = analyzedTweet.getReadabilityKincaid() / KINCAID_DIVISOR;
		analyzedTweet.setAttribute( "rank_kincaid", decimalFormat.format( temp ) );
		ranking += temp;

		temp = analyzedTweet.getReadabilityAri() / ARI_DIVISOR;
		analyzedTweet.setAttribute( "rank_ari", decimalFormat.format( temp ) );
		ranking += temp;

		temp = analyzedTweet.getReadabilityColemanLiau() / COLEMAN_LIAU_DIVISOR;
		analyzedTweet.setAttribute( "rank_coleman", decimalFormat.format( temp ) );
		ranking += temp;

		temp = analyzedTweet.getReadabilityLix() / LIX_DIVISOR;
		analyzedTweet.setAttribute( "rank_lix", decimalFormat.format( temp ) );
		ranking += temp;

		temp = analyzedTweet.getReadabilitySmog() / SMOG_DIVISOR;
		analyzedTweet.setAttribute( "rank_smog", decimalFormat.format( temp ) );
		ranking += temp;

		if ( analyzedTweet.getToReferenceTweetCosineDistance() > COSINE_MIN_DISTANCE ) {
			temp = 1.0d - analyzedTweet.getToReferenceTweetCosineDistance();
			temp = COSINE_MULTIPLIER * temp;
			analyzedTweet.setAttribute( "rank_cos", decimalFormat.format( temp ) );
			ranking += temp;
		}

		if ( analyzedTweet.getToReferenceTweetJaccardSimilarity() > JACCARD_MIN_DISTANCE ) {
			temp = analyzedTweet.getToReferenceTweetJaccardSimilarity() / JACCARD_DIVISOR;
			analyzedTweet.setAttribute( "rank_jac", decimalFormat.format( temp ) );
			ranking += temp;
		}

		if ( analyzedTweet.getToReferenceTweetJaroWinklerDistance() > JARO_WINKLER_MIN_DISTANCE ) {
			temp = analyzedTweet.getToReferenceTweetJaroWinklerDistance() / JARO_WINKLER_DIVISOR;
			analyzedTweet.setAttribute( "rank_jrw", decimalFormat.format( temp ) );
			ranking += temp;
		}

		temp = analyzedTweet.getNumSentences();
		analyzedTweet.setAttribute( "rank_numsent", decimalFormat.format( temp ) );
		ranking += temp;

		temp = analyzedTweet.getNumWords();
		temp = temp / NUM_WORDS_DIVISOR;
		temp = Math.floor( temp );
		analyzedTweet.setAttribute( "rank_numword", decimalFormat.format( temp ) );
		ranking += temp;

		final double numReplies = Utils.parseIntDefault( analyzedTweet.getTweet().getAttribute( "replycount" ) );
		final double numRetweets = Utils.parseIntDefault( analyzedTweet.getTweet().getAttribute( "retweetcount" ) );
		final double numFavorites = Utils.parseIntDefault( analyzedTweet.getTweet().getAttribute( "favoritecount" ) );

		temp = ( BOOST_REPLIES * numReplies ) + ( BOOST_RETWEETS * numRetweets ) + ( BOOST_FAVORITES * numFavorites );

		if ( temp > 0d ) {
			temp = Math.log( temp );
			analyzedTweet.setAttribute( "rank_pop", decimalFormat.format( temp ) );
			ranking += temp;
		}

		temp = analyzedTweet.getDateOrder();
		temp = ( ( count - temp + 1.0d ) / count );
		temp = BOOST_DATE_RATIO * temp;

		analyzedTweet.setAttribute( "rank_time", decimalFormat.format( temp ) );
		ranking += temp;

		analyzedTweet.setRanking( ranking );
		analyzedTweet.setRankingFunction( getFunctionName() );
	}
}
