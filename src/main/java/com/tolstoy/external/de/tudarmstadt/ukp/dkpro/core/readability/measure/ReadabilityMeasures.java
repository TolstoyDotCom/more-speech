/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * NOTE: This file is from https://github.com/dkpro/dkpro-core but was put into
 * this package instead of bringing in large dependencies.
 */

package com.tolstoy.external.de.tudarmstadt.ukp.dkpro.core.readability.measure;

import java.util.ArrayList;
import java.util.List;

/**
 * Java port of readability measures from the Linux 'style' command ('diction'
 * package).
 * 
 */
// FIXME add unit test
public class ReadabilityMeasures
{

    public enum Measures {
        ari,
        coleman_liau,
        flesch,
        fog,
        kincaid,
        lix,
        smog
    }
    
    private final WordSyllableCounter syllableCounter;
    private String language;

    public ReadabilityMeasures()
    {
        // initialize with default (English)
        this("en");
    }

    public ReadabilityMeasures(final String language)
    {
        this.language = language;
        this.syllableCounter = new WordSyllableCounter(language);
    }

    public double getReadabilityScore(final Measures measure, final List<String> words, final int nrofSentences) {
        if (measure.equals(Measures.ari)) {
            return ari(words, nrofSentences);
        }
        else if (measure.equals(Measures.coleman_liau)) {
            return coleman_liau(words, nrofSentences);
        }
        else if (measure.equals(Measures.flesch)) {
            return flesch(words, nrofSentences);
        }
        else if (measure.equals(Measures.fog)) {
            return fog(words, nrofSentences);
        }
        else if (measure.equals(Measures.kincaid)) {
            return kincaid(words, nrofSentences);
        }
        else if (measure.equals(Measures.lix)) {
            return lix(words, nrofSentences);
        }
        else if (measure.equals(Measures.smog)) {
            return smog(words, nrofSentences);
        }
        else {
            throw new IllegalArgumentException("Unknown measure: " + measure.name());
        }
    }
    
    /*
     * only the strings consist of numbers or letters
     * are considered as words.
     */
    private boolean isWord(final String strWord)
    {
        for (int i = 0; i < strWord.length(); ++i) {
            final char ch = strWord.charAt(i);
            if (!Character.isLetterOrDigit(ch)) {
                return false;
            }
        }
        return true;
    }
    
    private List<String> filterWords(final List<String> words)
    {
        final List<String> newWords = new ArrayList<String>();
        for (final String word : words) {
            if (isWord(word)) {
                newWords.add(word);
            }
        }
        return newWords;
    }
    
    /**
     * Calculate Kincaid Formula (reading grade).
     * 
     * @param words words
     * @param nrofSentences number of sentences.
     * @return score.
     * 
     */
    public double kincaid(List<String> words, final int nrofSentences)
    {
        words = filterWords(words);
        final int nrofSyllables = this.syllableCounter.countSyllables(words);
        return kincaid(words.size(), nrofSyllables, nrofSentences);
    }
    
    private double kincaid(final Integer nrofWords, final Integer nrofSyllables, final Integer nrofSentences)
    {
        return 11.8 * (((double) nrofSyllables) / nrofWords)
                + 0.39 * (((double) nrofWords) / nrofSentences) - 15.59;
    }
    
    /**
     * Calculate Automated Readability Index (reading grade).
     * 
     * @param words words.
     * @param nrofSentences number of sentences.
     * @return score.
     */
    public double ari(List<String> words, final int nrofSentences)
    {
        words = filterWords(words);
        final int nrofLetters = this.getNrofLetters(words);
        return ari(nrofLetters, words.size(), nrofSentences);
    }
    
    private double ari(final Integer nrofLetters, final Integer nrofWords, final Integer nrofSentences)
    {
        return 4.71 * (((double) nrofLetters) / nrofWords)
                + 0.5 * (((double) nrofWords) / nrofSentences) - 21.43;
    }
    
    /**
     * Calculate Coleman-Liau formula.
     * 
     * @param words words.
     * @param nrofSentences number of sentences.
     * @return score.
     * 
     */
    public double coleman_liau(List<String> words, final int nrofSentences)
    {
        words = filterWords(words);
        final int nrofLetters = this.getNrofLetters(words);
        return coleman_liau(nrofLetters, words.size(), nrofSentences);
    }
    private double coleman_liau(final Integer nrofLetters, final Integer nrofWords, final Integer nrofSentences)
    {
        return 5.89 * (((double) nrofLetters) / nrofWords)
                - 0.3 * (((double) nrofSentences) / (100 * nrofWords)) - 15.8;
    }

    /**
     * Calculate Flesch reading ease score.
     * 
     * @param words words.
     * @param nrofSentences number of sentences.
     * @return score.
     */
    public double flesch(List<String> words, final int nrofSentences)
    {
        words = filterWords(words);
        final int nrofSyllables = this.syllableCounter.countSyllables(words);
        return flesch(nrofSyllables, words.size(), nrofSentences);
    }
    private double flesch(final Integer nrofSyllables, final Integer nrofWords, final Integer nrofSentences)
    {
        return 206.835 - 84.6 * (((double) nrofSyllables) / nrofWords) - 1.015
                * (((double) nrofWords) / nrofSentences);
    }

   // 206.835-84.6*(((double)syllables)/words)-1.015*(((double)words)/sentences);
    
    
    /**
     * Calculate FOG index.
     * 
     * @param words words.
     * @param nrofSentences number of sentences.
     * @return score.
     */
    public double fog(List<String> words, final int nrofSentences)
    {
        words = filterWords(words);
        final int nrofBigwords = getNrofBigwords(words);
        return fog(words.size(), nrofBigwords, nrofSentences);
    }
    private double fog(final Integer nrofWords, final Integer nrofBigwords, final Integer nrofSentences)
    {
        return ((((double) nrofWords) / nrofSentences + (100.0 * nrofBigwords) / nrofWords) * 0.4);
    }

    /**
     * Calculate Björnsson's Lix formula.
     * 
     * @param words words.
     * @param nrofSentences number of sentences.
     * @return the wheeler smith index as result and the grade level in grade.
     *          If grade is 0, the index is lower than any grade, if the index
     *          is 99, it is higher than any grade.
     */
    public double lix(List<String> words, final int nrofSentences)
    {
        words = filterWords(words);
        final int nrofLongWords = this.getNrofLongwords(words);
        return lix(words.size(), nrofLongWords, nrofSentences);
    }
    private double lix(final Integer nrofWords, final Integer nrofLongWords, final Integer nrofSentences)
    {
        final double idx = ((double) nrofWords) / nrofSentences + 100.0 * (nrofLongWords) / nrofWords;
        if (idx < 34) {
            return 0;
        }
        else if (idx < 38) {
            return 5;
        }
        else if (idx < 41) {
            return 6;
        }
        else if (idx < 44) {
            return 7;
        }
        else if (idx < 48) {
            return 8;
        }
        else if (idx < 51) {
            return 9;
        }
        else if (idx < 54) {
            return 10;
        }
        else if (idx < 57) {
            return 11;
        }
        else {
            return 99;
        }
    }

    /**
     * Calculate SMOG-Grading.
     * 
     * @param words words.
     * @param nrofSentences number of sentences.
     * @return score.
     */
    public double smog(List<String> words, final int nrofSentences)
    {
        words = filterWords(words);
        final int nrofBigwords = this.getNrofBigwords(words);
        return smog(nrofBigwords, nrofSentences);
    } 
    private double smog(final Integer nrofBigWords, final Integer nrofSentences)
    {
        return Math.sqrt((((double) nrofBigWords) / ((double) nrofSentences)) * 30.0) + 3.0;
    }
    

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(final String language)
    {
        this.language = language;
    }

    private int getNrofLetters(final Iterable<String> words)
    {
        int count = 0;
        for (final String word : words) {
            count = count + word.length();
        }
        return count;
    }

    /**
     * @param words
     *            An iterable over words.
     * @return The number of words with more than 3 syllables.
     */
    private int getNrofBigwords(final Iterable<String> words)
    {
        int count = 0;
        for (final String word : words) {
            if (this.syllableCounter.countSyllables(word) >= 3) {
                count++;
            }
        }
        return count;
    }

    /**
     * @param words
     *            An iterable over words.
     * @return The number of words with more than 6 letters.
     */
    private int getNrofLongwords(final Iterable<String> words)
    {
        int count = 0;
        for (final String word : words) {
            if (word.length() > 6) {
                count++;
            }
        }
        return count;
    }
}
