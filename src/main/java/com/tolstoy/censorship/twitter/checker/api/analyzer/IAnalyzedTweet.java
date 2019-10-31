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

import java.util.List;
import java.util.Map;

import com.tolstoy.basic.api.tweet.ITweet;

public interface IAnalyzedTweet {
	ITweet getTweet();
	Map<String, String> getTweetMap();
	List<String> getSentences();
	List<String> getWords();
	List<String> getWordsWithoutStopWordsLowercase();
	List<String> getUrls();
	List<String> getHashtags();
	List<String> getMentions();
	String getTextContentRaw();
	String getTextContentPlain();
	String getRankingFunction();
	String getSummary();
	void setRankingFunction( final String rankingFunction );
	double getToReferenceTweetCosineDistance();
	double getToReferenceTweetJaccardSimilarity();
	double getToReferenceTweetJaroWinklerDistance();
	int getToReferenceTweetFuzzyScore();
	int getToReferenceTweetLevenshteinDistance();
	double getRanking();
	void setRanking( final double ranking );
	double getReadabilityFlesch();
	double getReadabilityFog();
	double getReadabilityKincaid();
	double getReadabilityAri();
	double getReadabilityColemanLiau();
	double getReadabilityLix();
	double getReadabilitySmog();
	int getNumSentences();
	int getNumWords();
	int getOriginalOrder();
	int getDateOrder();
	void setDateOrder( final int dateOrder );
	int getRankingOrder();
	void setRankingOrder( final int rankingOrder );
	boolean getHasPic();
	boolean getHasCard();
	boolean getMostlyCaps();

	/** Get all the attributes.
	 * @return a map of attributes
	*/
	Map<String,String> getAttributes();

	/** Set all the attributes.
	 * @param attributes a map of attributes
	*/
	void setAttributes( final Map<String,String> attributes );

	/** Get a single attribute.
	 * @param key the name of the attribute
	 * @return the attribute value or null
	*/
	String getAttribute( final String key );

	/** Set a single attribute.
	 * @param key the name of the attribute
	 * @param value the value of the attribute
	*/
	void setAttribute( final String key, final String value );
}
