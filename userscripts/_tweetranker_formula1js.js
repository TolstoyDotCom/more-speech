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
/**
 * This is a sample implementation of a tweet ranking function in Javascript.
 *
 * This can be called from the application by putting it in the "userscripts"
 * directory and giving it a name starting with "tweetranker_", followed
 * by the name of your algorithm, followed by ".js".
 *
 * For instance: tweetranker_formula1js.js
 *
 * Only the first script matching that pattern will be used, so if you
 * have multiple scripts add a "_" to the start of their filenames to
 * make sure the correct one is being used.
 * 
 * For testing, this script can be copied into a script block in an HTML
 * page. In that case, the testRankTweet function will be called to run
 * the algorithm using testing data.
 *
 * The analyzedTweet.setAttribute(... calls below are optional; those are
 * just used for debugging. Each key should start with "rank_".
 *
 * After doing your calculations, call analyzedTweet.setRanking with the
 * ranking and call analyzedTweet.setRankingFunction with the name of
 * your algorithm. That name should match the last part of the filename,
 * e.g. "formula1js".
 */
if ( typeof console === "undefined" && typeof print === "function" ) {
	console = {
		log: print
	}
}

function rankTweet( analyzedTweet, count, referenceAnalyzedTweet ) {
	var RANKING_FUNCTION_NAME = "formula1js";
	var FEW_WORDS_LIMIT = 5;
	var FEW_WORDS_PENALTY = -5.0;
	var FEW_WORDS_AND_PIC_PENALTY = -10.0;
	var MOSTLY_CAPS_PENALTY = -3.0;
	var FUZZY_DIVISOR = 10.0;
	var FUZZY_LIMIT = 2.8;
	var BOOST_FUZZY_OVER_LIMIT = 0.5;
	var FLESCH_DIVISOR = 100.0;
	var FOG_DIVISOR = 20.0;
	var KINCAID_DIVISOR = 20.0;
	var ARI_DIVISOR = 20.0;
	var COLEMAN_LIAU_DIVISOR = 20.0;
	var LIX_DIVISOR = 100.0;
	var SMOG_DIVISOR = 10.0;
	var COSINE_MIN_DISTANCE = 0.5;
	var COSINE_MULTIPLIER = 4.0;
	var JACCARD_MIN_DISTANCE = 0.75;
	var JACCARD_DIVISOR = 2.0;
	var JARO_WINKLER_MIN_DISTANCE = 0.75;
	var JARO_WINKLER_DIVISOR = 2.0;
	var NUM_WORDS_DIVISOR = 10.0;
	var BOOST_REPLIES = 5.0;
	var BOOST_RETWEETS = 3.0;
	var BOOST_FAVORITES = 2.0;
	var BOOST_DATE_RATIO = 2.0;

	var decimalFormat = {
		format: function( num ) {
			return num ? num.toFixed( 2 ) : "0";
		}
	};

	var ranking = 0.0;
	var temp = 0.0;

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
		analyzedTweet.setAttribute( "rank_mostlycaps", "" + MOSTLY_CAPS_PENALTY );
		ranking += MOSTLY_CAPS_PENALTY;
	}

	temp = 200.0 - Math.min( 200.0, analyzedTweet.getReadabilityFlesch() );
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
		temp = 1.0 - analyzedTweet.getToReferenceTweetCosineDistance();
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

	var numReplies = 1 * analyzedTweet.getTweet().getAttribute( "replycount" );
	var numRetweets = 1 * analyzedTweet.getTweet().getAttribute( "retweetcount" );
	var numFavorites = 1 * analyzedTweet.getTweet().getAttribute( "favoritecount" );

	temp = ( BOOST_REPLIES * numReplies ) + ( BOOST_RETWEETS * numRetweets ) + ( BOOST_FAVORITES * numFavorites );

	if ( temp > 0 ) {
		temp = Math.log( temp );
		analyzedTweet.setAttribute( "rank_pop", decimalFormat.format( temp ) );
		ranking += temp;
	}

	temp = analyzedTweet.getDateOrder();
	temp = ( ( count - temp + 1.0 ) / count );
	temp = BOOST_DATE_RATIO * temp;

	analyzedTweet.setAttribute( "rank_time", decimalFormat.format( temp ) );
	ranking += temp;

	analyzedTweet.setRanking( ranking );
	analyzedTweet.setRankingFunction( RANKING_FUNCTION_NAME );

	//console.log( "set ranking to " + ranking );
}

function testRankTweet() {
	var analyzedTweet = {
		setRanking: function( ranking ) {
			console.log( "set ranking to " + ranking );
		},
		setRankingFunction: function( rankingFunction ) {
			console.log( "set rankingFunction to " + rankingFunction );
		},
		setAttribute: function( key, value ) {
			console.log( "set " + key + " to " + value );
		},
		getTweet: function() {
			return {
				getAttribute: function( key ) {
					return 2;
				}
			}
		},
		getNumWords: function() { return 2; },
		getNumSentences: function() { return 2; },
		getHasPic: function() { return 2; },
		getHasCard: function() { return 2; },
		getMostlyCaps: function() { return 2; },
		getDateOrder: function() { return 2; },
		getReadabilityFlesch: function() { return 2; },
		getReadabilityFog: function() { return 2; },
		getReadabilityKincaid: function() { return 2; },
		getReadabilityAri: function() { return 2; },
		getReadabilityColemanLiau: function() { return 2; },
		getReadabilityLix: function() { return 2; },
		getReadabilitySmog: function() { return 2; },
		getToReferenceTweetFuzzyScore: function() { return 2; },
		getToReferenceTweetCosineDistance: function() { return 2; },
		getToReferenceTweetJaccardSimilarity: function() { return 2; },
		getToReferenceTweetJaroWinklerDistance: function() { return 2; }
	}

	rankTweet( analyzedTweet, 1, null );
}

if ( typeof analyzedTweet !== "undefined" && typeof count !== "undefined" && typeof referenceAnalyzedTweet !== "undefined" ) {
	rankTweet( analyzedTweet, count, referenceAnalyzedTweet );
}
else {
	testRankTweet();
}

