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
var tweetElem = arguments[ 0 ];
var tempElem;

var ret = {
	avatarURL: '',
	fullname: '',
	verifiedText: '',
	username: '',
	time: '',
	tweettext: '',
	tweethtml: '',
	tweetlanguage: '',
	repliedtohandle: '',
	repliedtouserid: '',
	photourl: '',
	videothumburl: '',
	replycount: '',
	retweetcount: '',
	favoritecount: '',
	innertweetid: '',
	innertweetrawhref: '',
	suggestionjson: ''
};

tempElem = tweetElem.querySelector( '.avatar' );
if ( tempElem != null ) {
	ret.avatarURL = tempElem.getAttribute( 'src' );
}

tempElem = tweetElem.querySelector( '.fullname' );
if ( tempElem != null ) {
	ret.fullname = tempElem.textContent;
}

tempElem = tweetElem.querySelector( '.UserBadges' );
if ( tempElem != null ) {
	ret.verifiedText = tempElem.textContent;
}

tempElem = tweetElem.querySelector( '.username' );
if ( tempElem != null ) {
	ret.username = tempElem.textContent;
}

tempElem = tweetElem.querySelector( '.js-relative-timestamp' );
if ( tempElem != null ) {
	ret.time = tempElem.getAttribute( 'data-time' );
}
else {
	tempElem = tweetElem.querySelector( '.js-short-timestamp' );
	if ( tempElem != null ) {
		ret.time = tempElem.getAttribute( 'data-time' );
	}
	else {
		tempElem = tweetElem.querySelector( '_timestamp' );
		if ( tempElem != null ) {
			ret.time = tempElem.getAttribute( 'data-time' );
		}
	}
}

tempElem = tweetElem.querySelector( '.tweet-text' );
if ( tempElem != null ) {
	ret.tweettext = tempElem.textContent;
	ret.tweethtml = tempElem.innerHTML;
	ret.tweetlanguage = tempElem.getAttribute( 'lang' );
}

tempElem = tweetElem.querySelector( ':scope .ReplyingToContextBelowAuthor a' );
if ( tempElem != null ) {
	ret.repliedtohandle = tempElem.getAttribute( 'href' );
	ret.repliedtouserid = tempElem.getAttribute( 'data-user-id' );
}

	//	inside AdaptiveMedia-container
tempElem = tweetElem.querySelector( ':scope .AdaptiveMedia-photoContainer img' );
if ( tempElem != null ) {
	ret.photourl = tempElem.getAttribute( 'src' );
}

	//	inside AdaptiveMedia-container
tempElem = tweetElem.querySelector( '.PlayableMedia-player' );
if ( tempElem != null ) {
	ret.videothumburl = tempElem.getAttribute( 'style' );
}

tempElem = tweetElem.querySelector( ':scope .ProfileTweet-action--reply span' );
if ( tempElem != null ) {
	ret.replycount = tempElem.getAttribute( 'data-tweet-stat-count' );
}

tempElem = tweetElem.querySelector( ':scope .ProfileTweet-action--retweet span' );
if ( tempElem != null ) {
	ret.retweetcount = tempElem.getAttribute( 'data-tweet-stat-count' );
}
else {
	tempElem = tweetElem.querySelector( '.request-retweeted-popup' );
	if ( tempElem != null ) {
		ret.retweetcount = tempElem.getAttribute( 'data-tweet-stat-count' );
	}
}

tempElem = tweetElem.querySelector( ':scope .ProfileTweet-action--favorite span' );
if ( tempElem != null ) {
	ret.favoritecount = tempElem.getAttribute( 'data-tweet-stat-count' );
}
else {
	tempElem = tweetElem.querySelector( '.request-favorited-popup' );
	if ( tempElem != null ) {
		ret.favoritecount = tempElem.getAttribute( 'data-tweet-stat-count' );
	}
}

tempElem = tweetElem.querySelector( '.QuoteTweet-link' );
if ( tempElem != null ) {
	ret.innertweetid = tempElem.getAttribute( 'data-conversation-id' );
	ret.innertweetrawhref = tempElem.getAttribute( 'href' );
}

tempElem = tweetElem.querySelector( ':scope .js-stream-item span' );
if ( tempElem != null ) {
	ret.suggestionjson = tempElem.getAttribute( 'data-suggestion-json' );
}

return ret;
