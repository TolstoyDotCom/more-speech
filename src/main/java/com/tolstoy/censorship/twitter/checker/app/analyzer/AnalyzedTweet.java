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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.FuzzyScore;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tolstoy.basic.api.tweet.ITweet;
import com.tolstoy.censorship.twitter.checker.api.analyzer.IAnalyzedTweet;
import com.tolstoy.external.de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures;
import com.twitter.twittertext.Extractor;

import cue.lang.SentenceIterator;
import cue.lang.WordIterator;
import cue.lang.stop.StopWords;

@JsonIgnoreProperties(ignoreUnknown=true)
class AnalyzedTweet implements IAnalyzedTweet {
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger( AnalyzedTweet.class );

	@JsonIgnore
	private static final float FRACTION_TO_BE_MOSTLY_UPPERCASE = 0.75f;

	@JsonIgnore
	private static final Extractor extractor = new Extractor();

	@JsonIgnore
	private static final CosineDistance comparerCosineDistance = new CosineDistance();

	@JsonIgnore
	private static final FuzzyScore comparerFuzzyScore = new FuzzyScore( Locale.ENGLISH );

	@JsonIgnore
	private static final JaccardSimilarity comparerJaccardSimilarity = new JaccardSimilarity();

	@JsonIgnore
	private static final JaroWinklerDistance comparerJaroWinklerDistance = new JaroWinklerDistance();

	@JsonProperty
	private final ITweet tweet;

	@JsonProperty
	private final Map<String,String> tweetMap;

	@JsonProperty
	private Map<String,String> attributes;

	@JsonProperty
	private final List<String> sentences;

	@JsonProperty
	private final List<String> words;

	@JsonProperty
	private final List<String> wordsWithoutStopWordsLowercase;

	@JsonProperty
	private final List<String> urls;

	@JsonProperty
	private final List<String> hashtags;

	@JsonProperty
	private final List<String> mentions;

	@JsonProperty
	private final String textContentRaw;

	@JsonProperty
	private final String textContentPlain;

	@JsonProperty
	private Double toReferenceTweetCosineDistance;

	@JsonProperty
	private Double toReferenceTweetJaccardSimilarity;

	@JsonProperty
	private Double toReferenceTweetJaroWinklerDistance;

	@JsonProperty
	private Integer toReferenceTweetFuzzyScore;

	@JsonProperty
	private Integer toReferenceTweetLevenshteinDistance;

	@JsonProperty
	private String rankingFunction;

	@JsonProperty
	private double ranking;

	@JsonProperty
	private double readabilityFlesch;

	@JsonProperty
	private double readabilityFog;

	@JsonProperty
	private double readabilityKincaid;

	@JsonProperty
	private double readabilityAri;

	@JsonProperty
	private double readabilityColemanLiau;

	@JsonProperty
	private double readabilityLix;

	@JsonProperty
	private double readabilitySmog;

	@JsonProperty
	private final int numSentences;

	@JsonProperty
	private final int numWords;

	@JsonProperty
	private final int originalOrder;

	@JsonProperty
	private int dateOrder;

	@JsonProperty
	private int rankingOrder;

	@JsonProperty
	private boolean hasPic;

	@JsonProperty
	private boolean hasCard;

	@JsonProperty
	private boolean mostlyCaps;

	AnalyzedTweet( final ITweet tweet, final int order, final IAnalyzedTweet referenceTweet ) {
		this.attributes = new HashMap<String,String>();

		this.sentences = new ArrayList<String>();
		this.words = new ArrayList<String>();
		this.wordsWithoutStopWordsLowercase = new ArrayList<String>();
		this.urls = new ArrayList<String>();
		this.hashtags = new ArrayList<String>();
		this.mentions = new ArrayList<String>();

		this.toReferenceTweetCosineDistance = null;
		this.toReferenceTweetJaccardSimilarity = null;
		this.toReferenceTweetJaroWinklerDistance = null;
		this.toReferenceTweetFuzzyScore = null;
		this.toReferenceTweetLevenshteinDistance = null;

		this.readabilityFlesch = 0;
		this.readabilityFog = 0;
		this.readabilityKincaid = 0;
		this.readabilityAri = 0;
		this.readabilityColemanLiau = 0;
		this.readabilityLix = 0;
		this.readabilitySmog = 0;

		this.ranking = 0;
		this.rankingFunction = "";

		this.dateOrder = 0;
		this.rankingOrder = 0;

		this.hasPic = false;
		this.mostlyCaps = false;

		this.tweet = tweet;
		this.tweetMap = tweet.getAsMapBasic();
		this.originalOrder = order;
		this.textContentRaw = tweet.getAttribute( "tweettext" );

		this.textContentPlain = extractPlainTextHashtagsURLsMentionsHasPicHasCards( this.textContentRaw );

		for ( final String sentence : new SentenceIterator( this.textContentPlain, Locale.ENGLISH ) ) {
			sentences.add( sentence );
		}

		int numAllUpper = 0;
		for ( final String word : new WordIterator( this.textContentPlain ) ) {
			words.add( word );
			if ( !StopWords.English.isStopWord( word ) ) {
				final String baseWord = getBaseWord( word );
				if ( baseWord != null ) {
					wordsWithoutStopWordsLowercase.add( baseWord );
				}
			}
			if ( StringUtils.isAllUpperCase( word ) ) {
				numAllUpper++;
			}
		}

		//System.out.println( BlockUtil.guessUnicodeBlock( textContentRaw ) );
		//System.out.println( StopWords.guess( textContentRaw ) );

		this.numSentences = sentences.size();
		this.numWords = words.size();

		if ( numWords > 0 && ( (float) numAllUpper ) / ( (float) numWords ) > FRACTION_TO_BE_MOSTLY_UPPERCASE ) {
			mostlyCaps = true;
		}

		if ( this.numWords > 0 ) {
			final ReadabilityMeasures readability = new ReadabilityMeasures( "en" );
			this.readabilityFlesch = readability.getReadabilityScore( ReadabilityMeasures.Measures.flesch, words, numSentences );
			this.readabilityFog = readability.getReadabilityScore( ReadabilityMeasures.Measures.fog, words, numSentences );
			this.readabilityKincaid = readability.getReadabilityScore( ReadabilityMeasures.Measures.kincaid, words, numSentences );
			this.readabilityAri = readability.getReadabilityScore( ReadabilityMeasures.Measures.ari, words, numSentences );
			this.readabilityColemanLiau = readability.getReadabilityScore( ReadabilityMeasures.Measures.coleman_liau, words, numSentences );
			this.readabilityLix = readability.getReadabilityScore( ReadabilityMeasures.Measures.lix, words, numSentences );
			this.readabilitySmog = readability.getReadabilityScore( ReadabilityMeasures.Measures.smog, words, numSentences );
		}

		if ( referenceTweet != null &&
				!wordsWithoutStopWordsLowercase.isEmpty() &&
				!referenceTweet.getWordsWithoutStopWordsLowercase().isEmpty() ) {
			List<String> temp = new ArrayList<String>( wordsWithoutStopWordsLowercase );
			Collections.sort( temp );
			final String thisText = StringUtils.join( temp, " " );

			temp = new ArrayList<String>( referenceTweet.getWordsWithoutStopWordsLowercase() );
			Collections.sort( temp );
			final String referenceText = StringUtils.join( temp, " " );

			try {
				toReferenceTweetCosineDistance = comparerCosineDistance.apply( referenceText, thisText );
			}
			catch ( final Exception e ) {
				logger.error( "bad comparerCosineDistance, referenceText=" + referenceText + ", thisText=" + thisText );
			}

			try {
				toReferenceTweetJaccardSimilarity = comparerJaccardSimilarity.apply( referenceText, thisText );
			}
			catch ( final Exception e ) {
				logger.error( "bad comparerJaccardSimilarity, referenceText=" + referenceText + ", thisText=" + thisText );
			}

			try {
				toReferenceTweetJaroWinklerDistance = comparerJaroWinklerDistance.apply( referenceText, thisText );
			}
			catch ( final Exception e ) {
				logger.error( "bad comparerJaroWinklerDistance, referenceText=" + referenceText + ", thisText=" + thisText );
			}

			try {
				toReferenceTweetFuzzyScore = comparerFuzzyScore.fuzzyScore( referenceText, thisText );
			}
			catch ( final Exception e ) {
				logger.error( "bad comparerFuzzyScore, referenceText=" + referenceText + ", thisText=" + thisText );
			}

			try {
				toReferenceTweetLevenshteinDistance = LevenshteinDistance.getDefaultInstance().apply( referenceText, thisText );
			}
			catch ( final Exception e ) {
				logger.error( "bad LevenshteinDistance, referenceText=" + referenceText + ", thisText=" + thisText );
			}
		}
	}

	protected String getBaseWord( final String input ) {
		if ( input == null || input.length() < 1 ) {
			return null;
		}
		final int len = input.length();
		final StringBuffer sb = new StringBuffer( len );
		for ( int i = 0; i < len; i++ ) {
			if ( Character.isLetterOrDigit( input.charAt( i ) ) ) {
				sb.append( input.charAt( i ) );
			}
		}

		final String output = sb.toString().trim().toLowerCase();

		return output.length() > 0 ? output : null;
	}

	protected boolean isPictureURL( final String url ) {
		return url.toLowerCase().indexOf( "pic.twitter" ) > -1;
	}

	protected boolean isCardURL( final String url ) {
		return url.toLowerCase().indexOf( "cards.twitter" ) > -1;
	}

	private String extractPlainTextHashtagsURLsMentionsHasPicHasCards( String input ) {
		input = input.replace( "…", " " ).trim();

		if ( input.toLowerCase().indexOf( "pic.twitter" ) > -1 ) {
			hasPic = true;
		}

		final List<Extractor.Entity> entities = extractor.extractEntitiesWithIndices( input );
		if ( entities == null || entities.isEmpty() ) {
			return input.replace( "#", "" );
		}

		for ( final Extractor.Entity entity : entities ) {
			final Extractor.Entity.Type type = entity.getType();
			final String value = entity.getValue();
			if ( type == Extractor.Entity.Type.CASHTAG ) {
				input = input.replace( "$" + value, "" );
			}
			else if ( type == Extractor.Entity.Type.HASHTAG ) {
				hashtags.add( value );
				//	#GoodMorning -> Good Morning
				input = input.replace( "#" + value, StringUtils.join( StringUtils.splitByCharacterTypeCamelCase( value ), " " ) );
			}
			else if ( type == Extractor.Entity.Type.MENTION ) {
				mentions.add( value );
				input = input.replace( "@" + value, "" );
			}
			else if ( type == Extractor.Entity.Type.URL ) {
				urls.add( value );
				input = input.replace( value, "" );

				if ( isPictureURL( value ) ) {
					hasPic = true;
				}

				if ( isCardURL( value ) ) {
					hasCard = true;
				}
			}
		}

		return input.replace( "#", "" ).trim();
	}

	@Override
	public ITweet getTweet() {
		return tweet;
	}

	@Override
	public Map<String, String> getTweetMap() {
		return tweetMap;
	}

	@Override
	public List<String> getSentences() {
		return sentences;
	}

	@Override
	public List<String> getWords() {
		return words;
	}

	@Override
	public List<String> getWordsWithoutStopWordsLowercase() {
		return wordsWithoutStopWordsLowercase;
	}

	@Override
	public List<String> getUrls() {
		return urls;
	}

	@Override
	public List<String> getHashtags() {
		return hashtags;
	}

	@Override
	public List<String> getMentions() {
		return mentions;
	}

	@Override
	public String getTextContentRaw() {
		return textContentRaw;
	}

	@Override
	public String getTextContentPlain() {
		return textContentPlain;
	}

	@Override
	public String getRankingFunction() {
		return rankingFunction;
	}

	@Override
	public void setRankingFunction( final String rankingFunction ) {
		this.rankingFunction = rankingFunction;
	}

	@Override
	public double getToReferenceTweetCosineDistance() {
		return toReferenceTweetCosineDistance != null ? toReferenceTweetCosineDistance.doubleValue() : 0;
	}

	@Override
	public double getToReferenceTweetJaccardSimilarity() {
		return toReferenceTweetJaccardSimilarity != null ? toReferenceTweetJaccardSimilarity.doubleValue() : 0;
	}

	@Override
	public double getToReferenceTweetJaroWinklerDistance() {
		return toReferenceTweetJaroWinklerDistance != null ? toReferenceTweetJaroWinklerDistance.doubleValue() : 0;
	}

	@Override
	public int getToReferenceTweetFuzzyScore() {
		return toReferenceTweetFuzzyScore != null ? toReferenceTweetFuzzyScore.intValue() : 0;
	}

	@Override
	public int getToReferenceTweetLevenshteinDistance() {
		return toReferenceTweetLevenshteinDistance != null ? toReferenceTweetLevenshteinDistance.intValue() : 0;
	}

	@Override
	public double getRanking() {
		return ranking;
	}

	@Override
	public void setRanking( final double ranking ) {
		this.ranking = ranking;
	}

	@Override
	public double getReadabilityFlesch() {
		return readabilityFlesch;
	}

	@Override
	public double getReadabilityFog() {
		return readabilityFog;
	}

	@Override
	public double getReadabilityKincaid() {
		return readabilityKincaid;
	}

	@Override
	public double getReadabilityAri() {
		return readabilityAri;
	}

	@Override
	public double getReadabilityColemanLiau() {
		return readabilityColemanLiau;
	}

	@Override
	public double getReadabilityLix() {
		return readabilityLix;
	}

	@Override
	public double getReadabilitySmog() {
		return readabilitySmog;
	}

	@Override
	public int getNumSentences() {
		return numSentences;
	}

	@Override
	public int getNumWords() {
		return numWords;
	}

	@Override
	public int getOriginalOrder() {
		return originalOrder;
	}

	@Override
	public int getDateOrder() {
		return dateOrder;
	}

	@Override
	public void setDateOrder( final int dateOrder ) {
		this.dateOrder = dateOrder;
	}

	@Override
	public int getRankingOrder() {
		return rankingOrder;
	}

	@Override
	public void setRankingOrder( final int rankingOrder ) {
		this.rankingOrder = rankingOrder;
	}

	@Override
	public boolean getHasPic() {
		return hasPic;
	}

	@Override
	public boolean getHasCard() {
		return hasCard;
	}

	@Override
	public boolean getMostlyCaps() {
		return mostlyCaps;
	}

	@Override
	public String getAttribute( final String key ) {
		return attributes.get( key );
	}

	@Override
	public void setAttribute( final String key, final String value ) {
		attributes.put( key, value );
	}

	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes( final Map<String,String> attributes ) {
		this.attributes = attributes;
	}

	@Override
	public String getSummary() {
		final Map<String,String> rankAttributes = new HashMap<String,String>();
		for ( final String key : attributes.keySet() ) {
			if ( key.indexOf( "rank_" ) == 0 ) {
				rankAttributes.put( key.replace( "rank_", "" ), attributes.get( key ) );
			}
		}
		final String rankString = "ranking=" + ranking + " from " + rankAttributes;
		final String orderString = "orders: orig=" + originalOrder + ", rank=" + rankingOrder + ", date=" + dateOrder;

		return textContentRaw + "\n " + rankString + "\n " + orderString;
	}

	@Override
	public String toString() {
		final List<String> list = new ArrayList<String>();
		list.add( "textContentRaw=" + textContentRaw );
		list.add( "textContentPlain=" + textContentPlain );
		list.add( "ranking=" + ranking );
		list.add( "originalOrder=" + originalOrder );
		list.add( "dateOrder=" + dateOrder );
		list.add( "rankingOrder=" + rankingOrder );
		list.add( "rankingFunction=" + rankingFunction );
		list.add( "readabilityFlesch=" + readabilityFlesch );
		list.add( "readabilityFog=" + readabilityFog );
		list.add( "readabilityKincaid=" + readabilityKincaid );
		list.add( "readabilityAri=" + readabilityAri );
		list.add( "readabilityColemanLiau=" + readabilityColemanLiau );
		list.add( "readabilityLix=" + readabilityLix );
		list.add( "readabilitySmog=" + readabilitySmog );
		list.add( "toReferenceTweetCosineDistance=" + toReferenceTweetCosineDistance );
		list.add( "toReferenceTweetJaccardSimilarity=" + toReferenceTweetJaccardSimilarity );
		list.add( "toReferenceTweetJaroWinklerDistance=" + toReferenceTweetJaroWinklerDistance );
		list.add( "toReferenceTweetFuzzyScore=" + toReferenceTweetFuzzyScore );
		list.add( "toReferenceTweetLevenshteinDistance=" + toReferenceTweetLevenshteinDistance );
		list.add( "hasPic=" + hasPic );
		list.add( "hasCard=" + hasCard );
		list.add( "mostlyCaps=" + mostlyCaps );
		list.add( "numSentences=" + numSentences );
		list.add( "sentences=" + StringUtils.join( sentences, " /// " ) );
		list.add( "numWords=" + numWords );
		list.add( "words=" + StringUtils.join( words, " ^ " ) );
		list.add( "wordsWithoutStopWordsLowercase=" + StringUtils.join( wordsWithoutStopWordsLowercase, " ^ " ) );
		list.add( "urls=" + StringUtils.join( urls, " ^ " ) );
		list.add( "hashtags=" + StringUtils.join( hashtags, " ^ " ) );
		list.add( "mentions=" + StringUtils.join( mentions, " ^ " ) );
		list.add( "attributes=" + attributes );
		list.add( "tweet=" + tweetMap );

		return StringUtils.join( list, "\n" );
	}
}

