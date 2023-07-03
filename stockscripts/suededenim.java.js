/*
 * Copyright 2019 Chris Kelly
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

(function ensureOurNamespace() {
	if (typeof window.com === 'undefined') {
		window.com = {};
	}

	if (typeof window.com.tolstoy === 'undefined') {
		window.com.tolstoy = {};
	}

	if (typeof window.com.tolstoy.external === 'undefined') {
		window.com.tolstoy.external = {};
	}

	if (typeof window.com.tolstoy.basic === 'undefined') {
		window.com.tolstoy.basic = {};
	}

	if (typeof window.com.tolstoy.basic.app === 'undefined') {
		window.com.tolstoy.basic.app = {};
	}

	if (typeof window.com.tolstoy.basic.app.main === 'undefined') {
		window.com.tolstoy.basic.app.main = {};
	}

	if (typeof window.com.tolstoy.basic.app.tweet === 'undefined') {
		window.com.tolstoy.basic.app.tweet = {};
	}

	if (typeof window.com.tolstoy.basic.app.utils === 'undefined') {
		window.com.tolstoy.basic.app.utils = {};
	}

	if (typeof window.com.tolstoy.basic.app.retriever === 'undefined') {
		window.com.tolstoy.basic.app.retriever = {};
	}

	if (typeof window.com.tolstoy.basic.app.scroller === 'undefined') {
		window.com.tolstoy.basic.app.scroller = {};
	}

	if (typeof window.com.tolstoy.basic.app.jsonparser === 'undefined') {
		window.com.tolstoy.basic.app.jsonparser = {};
	}

	if (typeof window.com.tolstoy.basic.app.tweetparser === 'undefined') {
		window.com.tolstoy.basic.app.tweetparser = {};
	}

	if (typeof window.com.tolstoy.basic.app.tweetparser.html === 'undefined') {
		window.com.tolstoy.basic.app.tweetparser.html = {};
	}

	if (typeof window.com.tolstoy.basic.app.tweetparser.json === 'undefined') {
		window.com.tolstoy.basic.app.tweetparser.json = {};
	}

	if (typeof window.com.tolstoy.basic.app.tweetparser.html.helper === 'undefined') {
		window.com.tolstoy.basic.app.tweetparser.html.helper = {};
	}

	if (typeof window.com.tolstoy.basic.app.tweetparser.json.helper === 'undefined') {
		window.com.tolstoy.basic.app.tweetparser.json.helper = {};
	}

	if (typeof window.com.tolstoy.basic.app.harretriever === 'undefined') {
		window.com.tolstoy.basic.app.harretriever = {};
	}
})();

com.tolstoy.basic.app.utils.DebugLevel = function( debugLevel ) {
	var levels = {
		'0': 0,
		'1': 1,
		'2': 2,
		NONE: 0,
		TERSE: 1,
		VERBOSE: 2
	};

	var level = levels.hasOwnProperty( debugLevel ) ? levels[ debugLevel ] : 1;

	this.getLevel = function() {
		return level;
	};

	this.isDebug = function() {
		return level > 0;
	};

	this.isVerbose = function() {
		return level > 1;
	};
};

com.tolstoy.basic.app.utils.ConsoleLogger = function( $, debugLevel ) {
	var newlineRegex = /\r?\n/;

	this.getDebugLevel = function() {
		return debugLevel;
	};

	this.init = function() {
	};

	this.info = function( output ) {
		if ( !output || !debugLevel || !debugLevel.isDebug() ) {
			return;
		}

		if ( window.console && window.console.info ) {
			var ary = output.split( newlineRegex );
			ary = ary || [];
			for ( var i = 0; i < ary.length; i++ ) {
				console.info( ary[ i ] );
			}
		}
	};
};

com.tolstoy.basic.app.utils.TextareaLogger = function( $, debugLevel ) {
	this.getDebugLevel = function() {
		return debugLevel;
	};

	this.init = function() {
		var textarea = $( '<textarea/>' )
		.attr({
			rows: 5,
			cols: 120,
			id:'suededenim_output'
		});

		var div = $( '<div/>' )
		.css({
			width: '100%',
			height: '5rem',
			position: 'absolute',
			top: 0,
			left: 0,
			zIndex: 1000
		})
		.attr({
			id: 'suededenim'
		});

		div.append( textarea );

		$( 'body' ).prepend( div );
	};

	this.info = function( output ) {
		if ( !$( '#suededenim_output' ).length ) {
			this.init();
		}

		$( '#suededenim_output' ).text( '' + output );
	};
};

com.tolstoy.basic.app.utils.NumericPhrase = function( text ) {
	this.text = text || '';
	this.components = this.text.split( /\s+/ );
	this.numbers = [];
	this.words = [];

	this.components = this.components || [];

	for ( var i = 0; i < this.components.length; i++ ) {
		var item = this.components[ i ];
		item = item.trim();
		if ( !item ) {
			continue;
		}

		var num = parseInt( item );

		if ( !isNaN( num ) ) {
			this.numbers.push( '' + num );
		}
		else {
			this.words.push( item.toLowerCase() );
		}
	}

	this.countWords = function() {
		return this.words ? this.words.length : 0;
	};

	this.countNumbers = function() {
		return this.numbers ? this.numbers.length : 0;
	};

	this.getWord = function( which ) {
		return this.words ? this.words[ which ] : null;
	};

	this.getNumber = function( which ) {
		return this.numbers ? this.numbers[ which ] : null;
	};

	this.containsWord = function( word ) {
		if ( !word || !this.countWords() ) {
			return false;
		}

		var wordLC = word.toLowerCase();

		if ( this.words ) {
			for ( var i = 0; i < this.words.length; i++ ) {
				if ( this.words[ i ].indexOf( wordLC ) > -1 ) {
					return true;
				}
			}
		}

		return false;
	};
};

com.tolstoy.basic.app.utils.Utils = function( $ ) {
	var numberOnlyRegex = new RegExp( '^\\d+$' );
	var newlinesRegex = new RegExp( '/\r?\n|\r/g' );

	this.extend = function( dest, src ) {
		dest = dest || {};
		src = src || {};

		$.each( src, function( index, val ) {
			if ( !dest[ index ] ) {
				dest[ index ] = val;
			}
		});

		return dest;
	};

	this.getNestedJSON = function( json, keys ) {
		keys = keys || [];
		for ( var i = 0; i < keys.length; i++ ) {
			var key = keys[ i ];
			if ( typeof json[ key ] === 'undefined' ) {
				return null;
			}
			json = json[ key ];
		}

		return json;
	};

	this.swapUserID = function( user ) {
		if ( user.id && user.rest_id && /^\d+$/.test( user.rest_id ) ) {
			var rest_id = user.id;
			user.id = user.rest_id;
			user.rest_id = rest_id;
		}

		return user;
	};

	this.simplifyText = function( text, maxLen, defaultValue ) {
		maxLen = maxLen || 20;
		defaultValue = defaultValue || '[EMPTY]';

		if ( this.isEmpty( text ) ) {
			return defaultValue;
		}

		text = this.removeNewlines( this.removeEmojis( text ) );

		text = text ? text.trim() : defaultValue;

		return text.substring( 0, maxLen );
	};

	this.removeEmojis = function( input ) {
		//	https://stackoverflow.com/a/41543705
		return input.replace( /([\uE000-\uF8FF]|\uD83C[\uDC00-\uDFFF]|\uD83D[\uDC00-\uDFFF]|[\u2011-\u26FF]|\uD83E[\uDD10-\uDDFF])/g, '' );
	};

	this.removeNewlines = function( input ) {
		if ( !input || input.length < 1 ) {
			return input;
		}

		return input.replace( newlinesRegex, '' );
	};

	this.isEmpty = function( input ) {
		return !input || input.length < 1;
	};

	this.isEmptyOrZero = function( input ) {
		return !input || input === '0' || input.length < 1;
	};

	this.prettyPrint = function( input, indent ) {
		var ary = [];

		$.each( input, function( key, value ) {
			ary.push( indent + key + ': ' + value );
		});

		return ary.join( "\n" );
	};

	this.makeNumericPhrase = function( input ) {
		return new com.tolstoy.basic.app.utils.NumericPhrase( input );
	};

	this.ensureMapIsStringString = function( ary ) {
		var ret = {};

		if ( !ary ) {
			return ret;
		}

		$.each( ary, function( key, value ) {
			key = '' + key;
			value = '' + value;

			ret[ key ] = value;
		});

		return ret;
	};

	//	if testid goes away, 'which' won't work for various languages.
	//	parsing "88651 Μου αρέσει", "88651 отметка «Нравится»" probably
	//	isn't an option so add "lang=en" to the URL.
	this.findInteraction = function( which, testid, numericPhrases ) {
		var phrases = [];

		$.each( numericPhrases, function ( index, numericPhrase ) {
			if ( numericPhrase.countNumbers() ) {
				phrases.push( numericPhrase );
			}
		});

		if ( !phrases.length ) {
			return false;
		}

		phrases.sort( function( a, b ) {
			return b.getNumber( 0 ) - a.getNumber( 0 );
		});

		if ( testid && testid.indexOf( which ) > -1 ) {
			return phrases.shift().getNumber( 0 );
		}

		var ret = null;

		$.each( phrases, function ( index, phrase ) {
			if ( phrase.containsWord( which ) ) {
				ret = phrase;
				return false;
			}
		});

		return ret ? ret.getNumber( 0 ) : null;
	};

	this.importDataUsingDescriptors = function( target, source, descriptors ) {
		$.each( descriptors, function() {
			var sourceKey = this.sourceKey;
			var targetKey = this.targetKey;
			var defaultValue = this.defaultValue;
			var importer = this.importer;

			if ( importer ) {
				importer( target, source );
			}
			else if ( source.hasOwnProperty( sourceKey ) ) {
				target[ targetKey ] = source[ sourceKey ];
			}
			else {
				target[ targetKey ] = defaultValue;
			}
		});
	};

	this.exportDataUsingDescriptors = function( target, source, descriptors ) {
		$.each( descriptors, function() {
			var sourceKey = this.targetKey;
			var targetKey = this.sourceKey;
			var defaultValue = this.defaultValue;
			var exporter = this.exporter;

			if ( exporter ) {
				exporter( target, source );
			}
			else if ( source.hasOwnProperty( sourceKey ) ) {
				target[ targetKey ] = source[ sourceKey ];
			}
			else {
				target[ targetKey ] = defaultValue;
			}
		});
	};

	this.exportDataUsingDescriptorsSameKeys = function( target, source, descriptors ) {
		$.each( descriptors, function() {
			var sourceKey = this.targetKey;
			var targetKey = this.targetKey;
			var defaultValue = this.defaultValue;
			var exporter = this.exporter;

			if ( exporter ) {
				exporter( target, source );
			}
			else if ( source.hasOwnProperty( sourceKey ) ) {
				target[ targetKey ] = source[ sourceKey ];
			}
			else {
				target[ targetKey ] = defaultValue;
			}
		});
	};

	this.getTextContent = function( $container, raw ) {
		var first = $container.get( 0 );
		if ( !first ) {
			return '';
		}

		var text = first.textContent;
		if ( !text ) {
			return '';
		}

		if ( !raw ) {
			text = text.trim();
		}

		return text;
	};

	//	deprecated
	this.extractTweetIDFromURL = function( urlString ) {
		if ( !urlString || urlString.indexOf( 'status' ) < 0 ) {
			return null;
		}

		var ary = urlString.split( '/' );
		if ( !ary || ary.length < 1 ) {
			return null;
		}

		for ( var i = 0; i < ary.length; i++ ) {
			if ( ary[ i ] == 'status' && ( i + 1 ) < ary.length && ary[ i + 1 ].match( this.numberOnlyRegex ) ) {
				return ary[ i + 1 ];
			}
		}

		return null;
	};

	//	deprecated
	this.extractTwitterHandle = function( text ) {
		if ( !text ) {
			return null;
		}

		var ary = text.split( '/' );
		if ( !ary || ary.length < 1 ) {
			return null;
		}

		for ( var i = ary.length - 1; i >= 0; i-- ) {
			if ( ary[ i ] && ary[ i ].trim() ) {
				return ary[ i ].trim();
			}
		}

		return null;
	};

	//	deprecated
	this.isPhotoURL = function( url ) {
		return url && url.indexOf( 'photo' ) > -1;
	};
};

com.tolstoy.basic.app.tweet.TweetSupposedQuality = {
	HIGH: 'high_quality',
	LOW: 'low_quality',
	ABUSIVE: 'abusive_quality',
	UNKNOWN: 'unknown_quality'
};

com.tolstoy.basic.app.tweet.TweetLink = function( $, input, utils, logger ) {
	var valid = false;
	var bBareLink = false;
	var bHashtagLink = false;
	var bStatusLink = false;
	var bPhotoLink = false;
	var bRetweetLink = false;
	var bLikeLink = false;
	var bReplyLink = false;
	var bHelpLink = false;
	var bShortenedLink = false;
	var source = '';
	var hashtag = '';
	var handle = '';
	var tweetid = '';
	var photoid = '';
	var shortcode = '';
	var extra = '';
	var error_message = '';

	var helpLinkText = 'help.twitter.com/using-twitter';
	var shortenedRegex = /https?:\/\/t.co\/(\w+)\??(\w+)?/;						//	https://t.co/123
	var hashtagRegex = /\/hashtag\/([a-zA-Z0-9_-]+)\?/;							//	/hashtag/somehashtag
	var photoRegex = /\/?([a-zA-Z0-9_]+)\/status\/([\d]+)\/photo\/([\d]+)/;		//	handle/status/12345/photo/1
	var retweetRegex = /\/?([a-zA-Z0-9_]+)\/status\/([\d]+)\/retweet/;			//	handle/status/12345/retweet
	var likeRegex = /\/?([a-zA-Z0-9_]+)\/status\/([\d]+)\/like/;				//	handle/status/12345/like
	var replyRegex = /\/?([a-zA-Z0-9_]+)\/status\/([\d]+)\/repl/;				//	handle/status/12345/repl
	var bareRegex = /^\/([a-zA-z0-9_]+)$/;										//	/handle
	var statusRegex = /\/?([a-zA-Z0-9_]+)\/status\/([\d]+)(?:\/)?(.*)?/;		//	handle/status/12345

	this.isValid = function() {
		return valid;
	};

	this.isBareLink = function() {
		return bBareLink;
	};

	this.isShortenedLink = function() {
		return bShortenedLink;
	};

	this.isHelpLink = function() {
		return bHelpLink;
	};

	this.isHashtagLink = function() {
		return bHashtagLink;
	};

	this.isStatusLink = function() {
		return bStatusLink;
	};

	this.isPhotoLink = function() {
		return bPhotoLink;
	};

	this.isRetweetLink = function() {
		return bRetweetLink;
	};

	this.isLikeLink = function() {
		return bLikeLink;
	};

	this.isReplyLink = function() {
		return bReplyLink;
	};

	this.isInteractionLink = function() {
		return bRetweetLink || bLikeLink || bReplyLink;
	};

	this.getSource = function() {
		return source;
	};

	this.getHashtag = function() {
		return hashtag;
	};

	this.getHandle = function() {
		return handle;
	};

	this.getTweetID = function() {
		return tweetid;
	};

	this.getPhotoID = function() {
		return photoid;
	};

	this.getShortcode = function() {
		return shortcode;
	};

	this.getStatusLink = function() {
		if ( !this.getHandle() || !this.getTweetID() ) {
			return '';
		}

		return '/' + this.getHandle() + '/status/' + this.getTweetID();
	};

	this.getExtra = function() {
		return extra;
	};

	this.getError = function() {
		return error_message;
	};

	if ( !input.source || !$.trim( input.source ) ) {
		return;
	}

	source = $.trim( input.source );

	var ary;

	if ( source.indexOf( helpLinkText ) > -1 ) {
		bHelpLink = true;
		valid = true;

		return;
	}

	ary = shortenedRegex.exec( source );
	if ( ary && ary.length > 1 ) {
		shortcode = ary[ 1 ];
		extra = ary.length > 2 ? ary[ 2 ] : '';

		bShortenedLink = true;
		valid = true;

		return;
	}

	ary = hashtagRegex.exec( source );
	if ( ary && ary.length > 1 ) {
		hashtag = ary[ 1 ];

		bHashtagLink = true;
		valid = true;

		return;
	}

	ary = bareRegex.exec( source );
	if ( ary && ary.length > 1 ) {
		handle = ary[ 1 ];

		bBareLink = true;
		valid = true;

		return;
	}

	ary = photoRegex.exec( source );
	if ( ary && ary.length > 1 ) {
		handle = ary[ 1 ];
		tweetid = ary[ 2 ];
		photoid = ary[ 3 ];

		bStatusLink = true;
		bPhotoLink = true;
		valid = true;

		return;
	}

	ary = retweetRegex.exec( source );
	if ( ary && ary.length > 1 ) {
		handle = ary[ 1 ];
		tweetid = ary[ 2 ];

		bStatusLink = true;
		bRetweetLink = true;
		valid = true;

		return;
	}

	ary = likeRegex.exec( source );
	if ( ary && ary.length > 1 ) {
		handle = ary[ 1 ];
		tweetid = ary[ 2 ];

		bStatusLink = true;
		bLikeLink = true;
		valid = true;

		return;
	}

	ary = replyRegex.exec( source );
	if ( ary && ary.length > 1 ) {
		handle = ary[ 1 ];
		tweetid = ary[ 2 ];

		bStatusLink = true;
		bReplyLink = true;
		valid = true;

		return;
	}

	ary = statusRegex.exec( source );
	if ( ary && ary.length > 1 ) {
		handle = ary[ 1 ];
		tweetid = ary[ 2 ];
		extra = ary.length > 3 ? ary[ 3 ] : '';

		bStatusLink = true;
		valid = true;

		return;
	}

	error_message = 'cannot parse ' + source;
};

com.tolstoy.basic.app.tweet.TweetUser = function( $, input, utils, logger ) {
	input = input || {};

	var descriptors = [
		{ targetKey: 'id', sourceKey: 'id', defaultValue: '0' },
		{ targetKey: 'handle', sourceKey: 'handle', defaultValue: 'placeholder_handle' },
		{ targetKey: 'displayName', sourceKey: 'displayName', defaultValue: '' },
		{ targetKey: 'verifiedStatus', sourceKey: 'verifiedStatus', defaultValue: 'UNKNOWN' },
		{ targetKey: 'avatarURL', sourceKey: 'avatarURL', defaultValue: '' },
		{ targetKey: 'numTotalTweets', sourceKey: 'numTotalTweets', defaultValue: '0' },
		{ targetKey: 'numFollowers', sourceKey: 'numFollowers', defaultValue: '0' },
		{ targetKey: 'numFollowing', sourceKey: 'numFollowing', defaultValue: '0' },
		{ targetKey: 'canDM', sourceKey: 'canDM', defaultValue: '' },
		{ targetKey: 'canMediaTag', sourceKey: 'canMediaTag', defaultValue: '' },
		{ targetKey: 'advertiserAccountType', sourceKey: 'advertiserAccountType', defaultValue: '' },
		{ targetKey: 'withheldInCountries', sourceKey: 'withheldInCountries', defaultValue: '' },
		{ targetKey: 'blueSubscriber', sourceKey: 'blueSubscriber', defaultValue: '' },
		{ targetKey: 'requireSomeConsent', sourceKey: 'requireSomeConsent', defaultValue: '' },
		{ targetKey: 'hasGraduatedAccess', sourceKey: 'hasGraduatedAccess', defaultValue: '' },
		{ targetKey: 'superFollowEligible', sourceKey: 'superFollowEligible', defaultValue: '' },
		{
			targetKey: 'errors',
			defaultValue: '',
			importer: function( target, source ) {
				if ( !source.errors ) {
					target.errors = [];
				}
				else if ( typeof source.errors === 'string' ) {
					target.errors = source.errors.split( ' ;;; ' );
				}
				else if ( typeof source.getErrors === 'function' ) {
					target.errors = source.getErrors();
				}
				else if ( Array.isArray( source.errors ) ) {
					target.errors = source.errors;
				}
				else {
					target.errors = [];
				}
			},
			exporter: function( target, source ) {
				target.errors = source.getErrors && source.getErrors() ? source.getErrors().join( ' ;;; ' ) : '';
			}
		}
	];

	this.setAttribute = function( which, value ) {
		this[ which ] = value;
	};

	this.getAttribute = function( which ) {
		return this[ which ];
	};

	this.getErrors = function() {
		return this.errors;
	};

	this.addError = function( message ) {
		this.errors.push( message );
	};

	this.export = function() {
		var ret = {};

		utils.exportDataUsingDescriptorsSameKeys( ret, this, descriptors );

		return ret;
	};

	this.toDebugString = function( indent ) {
		var ary = [];

		ary.push( this.getAttribute( 'id' ) ? 'id=' + this.getAttribute( 'id' ) : 'NO_ID' );

		if ( !this.getAttribute( 'handle' ) ) {
			ary.push( 'NO_H' );
		}
		else if ( this.getAttribute( 'handle' ) == 'placeholder_handle' ) {
			ary.push( 'DEF_H' );
		}
		else {
			ary.push( 'h=' + this.getAttribute( 'handle' ) );
		}

		ary.push( 'disp=' + utils.simplifyText( this.getAttribute( "displayName" ) ) );

		ary.push( this.getAttribute( 'verifiedStatus' ) ? 'vs=' + this.getAttribute( 'verifiedStatus' ) : 'NO_VS' );

		return indent + ary.join( ', ' );
	};

	utils.importDataUsingDescriptors( this, input, descriptors );
};

com.tolstoy.basic.app.tweet.Tweet = function( $, input, utils, logger ) {
	input = input || {};

	var moi = this;

	var TWEETTEXT_EMPTY_MARKER = '[empty]';

	var descriptors = [
		{ targetKey: 'avatarURL', sourceKey: 'avatarURL', defaultValue: '' },
		{ targetKey: 'componentcontext', sourceKey: 'componentcontext', defaultValue: '' },
		{ targetKey: 'conversationid', sourceKey: 'conversationid', defaultValue: '0' },
		{ targetKey: 'datestring', sourceKey: 'datestring', defaultValue: '' },
		{ targetKey: 'disclosuretype', sourceKey: 'disclosuretype', defaultValue: '' },
		{ targetKey: 'favoritecount', sourceKey: 'favoritecount', defaultValue: '0' },
		{ targetKey: 'followsyou', sourceKey: 'followsyou', defaultValue: '' },
		{ targetKey: 'fullname', sourceKey: 'fullname', defaultValue: '' },
		{ targetKey: 'hascards', sourceKey: 'hascards', defaultValue: '' },
		{ targetKey: 'hasparenttweet', sourceKey: 'hasparenttweet', defaultValue: '' },
		{ targetKey: 'innertweetid', sourceKey: 'innertweetid', defaultValue: '' },
		{ targetKey: 'innertweetrawhref', sourceKey: 'innertweetrawhref', defaultValue: '' },
		{ targetKey: 'is_pinned', sourceKey: 'is_pinned', defaultValue: '' },
		{ targetKey: 'is_toptweet', sourceKey: 'is_toptweet', defaultValue: '' },
		{ targetKey: 'isreplyto', sourceKey: 'isreplyto', defaultValue: '' },
		{ targetKey: 'itemid', sourceKey: 'itemid', defaultValue: '' },
		{ targetKey: 'iterationindex', sourceKey: 'iterationindex', defaultValue: '0' },
		{ targetKey: 'iterationnumber', sourceKey: 'iterationnumber', defaultValue: '0' },
		{ targetKey: 'name', sourceKey: 'name', defaultValue: '' },
		{ targetKey: 'nexttweetid', sourceKey: 'nexttweetid', defaultValue: '0' },
		{ targetKey: 'permalinkpath', sourceKey: 'permalinkpath', defaultValue: '' },
		{ targetKey: 'photourl', sourceKey: 'photourl', defaultValue: '' },
		{ targetKey: 'previoustweetid', sourceKey: 'previoustweetid', defaultValue: '0' },
		{ targetKey: 'quality', sourceKey: 'quality', defaultValue: 'unknown_quality' },
		{ targetKey: 'repliedtohandle', sourceKey: 'repliedtohandle', defaultValue: '' },
		{ targetKey: 'repliedtouserid', sourceKey: 'repliedtouserid', defaultValue: '0' },
		{ targetKey: 'replycount', sourceKey: 'replycount', defaultValue: '0' },
		{ targetKey: 'replytousersjson', sourceKey: 'replytousersjson', defaultValue: '' },
		{ targetKey: 'retweetcount', sourceKey: 'retweetcount', defaultValue: '0' },
		{ targetKey: 'retweetid', sourceKey: 'retweetid', defaultValue: '0' },
		{ targetKey: 'screenname', sourceKey: 'screenname', defaultValue: '' },
		{ targetKey: 'suggestionjson', sourceKey: 'suggestionjson', defaultValue: '' },
		{ targetKey: 'time', sourceKey: 'time', defaultValue: '0' },
		{ targetKey: 'tweetclasses', sourceKey: 'tweetclasses', defaultValue: '' },
		{ targetKey: 'tweethtml', sourceKey: 'tweethtml', defaultValue: '' },
		{ targetKey: 'tweetid', sourceKey: 'tweetid', defaultValue: '0' },
		{ targetKey: 'tweetlanguage', sourceKey: 'tweetlanguage', defaultValue: '' },
		{ targetKey: 'tweetmentions', sourceKey: 'tweetmentions', defaultValue: '' },
		{ targetKey: 'tweetnonce', sourceKey: 'tweetnonce', defaultValue: '' },
		{ targetKey: 'tweetphoto_image', sourceKey: 'tweetphoto_image', defaultValue: '' },
		{ targetKey: 'tweetphoto_link', sourceKey: 'tweetphoto_link', defaultValue: '' },
		{ targetKey: 'tweetstatinitialized', sourceKey: 'tweetstatinitialized', defaultValue: '' },
		{ targetKey: 'tweettext', sourceKey: 'tweettext', defaultValue: '' },
		{ targetKey: 'userid', sourceKey: 'userid', defaultValue: '0' },
		{ targetKey: 'username', sourceKey: 'username', defaultValue: '' },
		{ targetKey: 'verifiedText', sourceKey: 'verifiedText', defaultValue: '' },
		{ targetKey: 'videothumburl', sourceKey: 'videothumburl', defaultValue: '' },
		{ targetKey: 'viewscount', sourceKey: 'viewscount', defaultValue: '0' },
		{ targetKey: 'youblock', sourceKey: 'youblock', defaultValue: '' },
		{ targetKey: 'youfollow', sourceKey: 'youfollow', defaultValue: '' },
		{
			targetKey: 'errors',
			defaultValue: '',
			importer: function( target, source ) {
				if ( !source.errors ) {
					target.errors = [];
				}
				else if ( typeof source.errors === 'string' ) {
					target.errors = source.errors.split( ' ;;; ' );
				}
				else if ( typeof source.getErrors === 'function' ) {
					target.errors = source.getErrors();
				}
				else if ( Array.isArray( source.errors ) ) {
					target.errors = source.errors;
				}
				else {
					target.errors = [];
				}
			},
			exporter: function( target, source ) {
				target.errors = source.getErrors && source.getErrors() ? source.getErrors().join( ' ;;; ' ) : '';
			}
		}
	];

	var debugStringPrevNextMessages = [
		'NO_PN',
		'NO_P',
		'NO_N',
		'has_pn'
	];

	this.setAttribute = function( which, value ) {
		this[ which ] = value;
	};

	this.getAttribute = function( which ) {
		return this[ which ];
	};

	this.getAttributeErrors = function() {
		return this.errors;
	};

	this.addError = function( message ) {
		this.errors.push( message );
	};

	this.mergeFrom = function( other ) {
		if ( !other ) {
			return false;
		}
		
		$.each( descriptors, function( index, descriptor ) {
			var thisValue = moi.getAttribute( descriptor.targetKey );
			var otherValue = other.getAttribute( descriptor.targetKey );
			if ( utils.isEmptyOrZero( thisValue ) && !utils.isEmptyOrZero( otherValue ) ) {
				moi.setAttribute( descriptor.targetKey, otherValue );
			}
		});
	};

	this.export = function() {
		var ret = {};

		utils.exportDataUsingDescriptorsSameKeys( ret, this, descriptors );

		var exportedUser = this.getAttribute( 'user' ) ? this.getAttribute( 'user' ).export() : {};
		$.each( exportedUser, function( key, value ) {
			ret[ 'user__' + key ] = value;
		});

		return ret;
	};

	this.toDebugString = function( indent ) {
		var ary = [];

		ary.push( this.getAttribute( 'tweetid' ) ? 'id=' + this.getAttribute( 'tweetid' ) : 'NO_ID' );
		ary.push( 'txt=' + utils.simplifyText( this.getAttribute( "tweettext" ) ) );
		ary.push( this.getAttribute( 'userid' ) ? 'uid=' + this.getAttribute( 'userid' ) : 'NO_UID' );
		ary.push( this.getAttribute( 'username' ) ? 'unm=' + this.getAttribute( 'username' ) : 'NO_UNM' );
		ary.push( this.getAttribute( 'verifiedText' ) ? 'ver=' + this.getAttribute( 'verifiedText' ) : 'NO_VER' );
		ary.push( this.getAttribute( 'quality' ) ? 'q=' + this.getAttribute( 'quality' ) : 'NO_QUAL' );

		ary.push( 'favs=' + this.getAttribute( 'favoritecount' ) );
		ary.push( 'repls=' + this.getAttribute( 'replycount' ) );
		ary.push( 'rts=' + this.getAttribute( 'retweetcount' ) );
		ary.push( 'views=' + this.getAttribute( 'viewscount' ) );

		if ( this.getAttribute( 'permalinkpath' ) ) {
			if ( this.getAttribute( 'permalinkpath' ).indexOf( '/' ) > -1 ) {
				ary.push( 'has_prmlk' );
			}
			else {
				ary.push( 'BAD_PRMLK=' + this.getAttribute( 'permalinkpath' ) );
			}
		}
		else {
			ary.push( 'NO_PRMLK' );
		}

		ary.push( debugStringPrevNextMessages[ ( 2 * ( this.getAttribute( 'previoustweetid' ) ? 1 : 0 ) ) + ( this.getAttribute( 'nexttweetid' ) ? 1 : 0 ) ] );

		ary.push( this.user ? 'user=[' + this.user.toDebugString( '' ) + ']' : 'NO_USER' );

		return indent + ary.join( ', ' );
	};

	utils.importDataUsingDescriptors( this, input, descriptors );

	if ( input.user ) {
		for ( var key in input ) {
			if ( key.indexOf( 'user__' ) === 0 && input.hasOwnProperty( key ) ) {
				input.user.setAttribute( key.replace( 'user__', '' ), input.key );
			}
		}
	}

	this.setAttribute( 'user', input.user );
};

com.tolstoy.basic.app.tweet.TweetCollection = function( $, input, utils, logger ) {
	this.tweets = [];

	this.addTweets = function( ary ) {
		var count = 0;

		for ( var i = 0; i < ary.length; i++ ) {
			if ( this.addTweet( ary[ i ] ) ) {
				count++;
			}
		}

		return count;
	};

	this.exportAll = function() {
		var ary = [];

		for ( var i = 0; i < this.tweets.length; i++ ) {
			ary.push( this.tweets[ i ].export() );
		}

		return ary;
	};

	/**
	 * @return 0 if not added, 1 if appended, 2 if replaced.
	 */
	this.addTweet = function( newTweet ) {
		if ( !newTweet || !newTweet.getAttribute( 'tweetid' ) ) {
			return 0;
		}

		var existing = this.findTweetByID( newTweet.getAttribute( 'tweetid' ) );
		if ( !existing ) {
			this.tweets.push( newTweet );

			return 1;
		}

		existing.mergeFrom( newTweet );

		return 2;
	};

	this.findTweetByID = function( id ) {
		if ( !id ) {
			return null;
		}

		for ( var i = 0; i < this.tweets.length; i++ ) {
			if ( this.tweets[ i ].getAttribute( 'tweetid' ) == id ) {
				return this.tweets[ i ];
			}
		}

		return null;
	};

	this.toDebugString = function( indent ) {
		if ( !this.tweets || !this.tweets.length ) {
			return 'no items';
		}

		var ary = [];

		for ( var i = 0; i < this.tweets.length; i++ ) {
			ary.push( indent + '  ' + this.tweets[ i ].toDebugString( '' ) );
		}

		return indent + ary.join( "\n" );
	};
};

com.tolstoy.basic.app.tweet.TweetFactory = function( $, utils, logger ) {
	this.makeUser = function( input ) {
		input = input || {};

		return new com.tolstoy.basic.app.tweet.TweetUser( $, input, utils, logger );
	};

	this.makeTweet = function( input ) {
		input = input || {};

		if ( !input.user ) {
			input.user = this.makeUser();
		}

		return new com.tolstoy.basic.app.tweet.Tweet( $, input, utils, logger );
	};

	this.makeTweetCollection = function( input ) {
		return new com.tolstoy.basic.app.tweet.TweetCollection( $, input, utils, logger );
	};

	this.makeTweetLink = function( input ) {
		var link = new com.tolstoy.basic.app.tweet.TweetLink( $, input, utils, logger );
		if ( !link.isValid() && logger.getDebugLevel() && logger.getDebugLevel().isDebug() ) {
			logger.info( 'TweetFactory::makeTweetLink error: ' + link.getError() );
		}

		return link;
	};
};

com.tolstoy.basic.app.scroller.ScrollerStatus = {
	READY: 'ready',
	RUNNING: 'running',
	STOPPED: 'stopped',
	EXCEEDEDLIMIT: 'exceededlimit',
	FINISHED: 'finished'
};

com.tolstoy.basic.app.scroller.IntervalScroller = function( $, pageType, url, heightMultiplier, numTimesToScroll, delay, scrollCallback, finishedCallback, utils, logger ) {
	var scrollerStasuses = com.tolstoy.basic.app.scroller.ScrollerStatus;

	heightMultiplier = heightMultiplier || 1;
	numTimesToScroll = numTimesToScroll || 20;
	delay = delay || 1000;

	var scrollDistance = heightMultiplier * document.documentElement.clientHeight;
	var count = 0;
	var status = scrollerStasuses.READY;

	this.validate = function() {
		if ( !numTimesToScroll || numTimesToScroll < 1 ) {
			throw new Error( 'bad numTimesToScroll' );
		}

		if ( !heightMultiplier || heightMultiplier < 0.01 ) {
			throw new Error( 'bad heightMultiplier' );
		}

		if ( !delay || delay < 1 ) {
			throw new Error( 'bad delay' );
		}

		if ( typeof scrollCallback !== 'function' ) {
			throw new Error( 'bad scrollCallback' );
		}

		if ( typeof finishedCallback !== 'function' ) {
			throw new Error( 'bad finishedCallback' );
		}
	};

	this.validate();

	this.getStatus = function() {
		return status;
	};

	this.start = function() {
		var scroller = window.setInterval( function() {
			if ( status == scrollerStasuses.FINISHED ) {
				return;
			}

			status = scrollerStasuses.RUNNING;

			window.scrollBy( { top: scrollDistance, left: 0, behavior: 'smooth' } );
			scrollCallback();
			count++;

			var total = document.documentElement.clientHeight + document.documentElement.scrollTop;

			if ( count >= numTimesToScroll || total >= document.body.offsetHeight ) {
				status = scrollerStasuses.FINISHED;
				window.clearInterval( scroller );
				finishedCallback();
			}
		}, delay );
	};
};

com.tolstoy.basic.app.scroller.StepScroller = function( $, pageType, url, heightMultiplier, numTimesToScroll, utils, logger ) {
	var scrollerStasuses = com.tolstoy.basic.app.scroller.ScrollerStatus;

	heightMultiplier = heightMultiplier || 1;
	numTimesToScroll = numTimesToScroll || 20;

	var count = 0, lastScrollTop = 0;
	var status = scrollerStasuses.READY;

	this.validate = function() {
		if ( !numTimesToScroll || numTimesToScroll < 1 ) {
			throw new Error( 'bad numTimesToScroll' );
		}

		if ( !heightMultiplier || heightMultiplier < 0.01 ) {
			throw new Error( 'bad heightMultiplier' );
		}
	};

	this.validate();

	/***
	 * FINISHED = reached the bottom of the page
	 * EXCEEDEDLIMIT = didn't reach the bottom of the page but did meet or exceed numTimesToScroll
	 */
	this.getStatus = function() {
		return status;
	};

	this.setStatus = function( newStatus ) {
		status = newStatus;
	};

	this.reset = function() {
		count = 0;
		lastScrollTop = 0;
		status = scrollerStasuses.READY;
	};

	this.step = function() {
		logger.info( 'StepScroller status=' + status );

		if ( status == scrollerStasuses.FINISHED || status == scrollerStasuses.EXCEEDEDLIMIT || status == scrollerStasuses.STOPPED ) {
			logger.info( 'StepScroller returning due to status=' + status + ', count=' + count );
			return;
		}

		status = scrollerStasuses.RUNNING;
		count++;

		var scrollDistance = heightMultiplier * document.documentElement.clientHeight;

		window.scrollBy( { top: scrollDistance, left: 0, behavior: 'smooth' } );

		//var total2 = document.documentElement.clientHeight + document.documentElement.scrollTop;
		//var height2 = document.body.offsetHeight;

		var total = window.innerHeight + window.pageYOffset;
		var height = document.body.scrollHeight;

		var amountMoved = Math.abs( document.documentElement.scrollTop - lastScrollTop );

		/*
		logger.info( 'StepScroller: '
						+ ' count=' + count
						+ ' amountMoved=' + amountMoved
						+ ' total=' + total
						+ ', height=' + height
						+ ' total2=' + total2
						+ ', height2=' + height2
						+ ', document.documentElement.clientHeight=' + document.documentElement.clientHeight
						+ ', document.documentElement.scrollTop=' + document.documentElement.scrollTop
						+ ', window.pageYOffset=' + window.pageYOffset
						+ ', window.innerHeight=' + window.innerHeight );
		*/

		lastScrollTop = document.documentElement.scrollTop;

		if ( count > 3 && amountMoved < 10 ) {
			logger.info( 'StepScroller returning because it is not moving. lastScrollTop=' + lastScrollTop + ', current scrollTop=' + document.documentElement.scrollTop );
			status = scrollerStasuses.FINISHED;
			return;
		}

/*
		if ( total >= height ) {
			logger.info( 'StepScroller returning because offsetHeight ' + total + ' >= ' + height );
			status = scrollerStasuses.FINISHED;
			return;
		}
*/

		if ( count >= numTimesToScroll ) {
			logger.info( 'StepScroller returning because count ' + count + ' >= ' + numTimesToScroll );
			status = scrollerStasuses.EXCEEDEDLIMIT;
			return;
		}
	};
};

com.tolstoy.basic.app.scroller.ScrollerFactory = function( $, utils, logger ) {
	this.makeIntervalScroller = function( pageType, url, heightMultiplier, numTimesToScroll, delay, scrollCallback, finishedCallback ) {
		return new com.tolstoy.basic.app.scroller.IntervalScroller( $, pageType, url, heightMultiplier, numTimesToScroll, delay, scrollCallback, finishedCallback, utils, logger );
	};

	this.makeStepScroller = function( pageType, url, heightMultiplier, numTimesToScroll ) {
		return new com.tolstoy.basic.app.scroller.StepScroller( $, pageType, url, heightMultiplier, numTimesToScroll, utils, logger );
	};
};

com.tolstoy.basic.app.tweetparser.html.helper.Debug = function( $, $elem, tweetID, tweetFactory, utils, logger ) {
	var valid = false;

	this.isValid = function() {
		return valid;
	};

	$( 'a', $elem ).each( function() {
		tweetFactory.makeTweetLink( { source: $(this).attr( 'href' ) } );
	});
};

com.tolstoy.basic.app.tweetparser.html.helper.AuthorAvatar = function( $, $elem, tweetID, tweetFactory, utils, logger ) {//DONE
	var valid = false, src = '';

	this.isValid = function() {
		return valid;
	};

	this.getImage = function() {
		return src;
	};

	var $img = $( 'div > div > div > div > div > div > div > div > div > div > div > a > div > div > div > div > img', $elem );

	if ( $img.length < 1 ) {
		valid = false;
		return;
	}

	src = $img.attr( 'src' );
	if ( !src ) {
		valid = false;
		return;
	}

	valid = true;
};

com.tolstoy.basic.app.tweetparser.html.helper.AuthorNames = function( $, $elem, tweetID, tweetFactory, utils, logger ) {//DONE
	var valid = false, displayName = '', handle = '';

	this.isValid = function() {
		return valid;
	};

	this.getDisplayName = function() {
		return displayName;
	};

	this.getHandle = function() {
		return handle;
	};


	displayName = $( 'div > div > div > div > div > div > div > div > div > div > div > a > div > div > span > span', $elem ).text();
	handle = $( 'div > div > div > div > div > div > div > div > div > div > div > div > a > div > span', $elem ).text();

	displayName = displayName ? displayName.trim() : '';
	handle = handle ? handle.trim() : '';

	if ( handle ) {
		valid = true;
	}
	if ( !displayName ) {
		displayName = handle;
	}
};

com.tolstoy.basic.app.tweetparser.html.helper.AuthorVerified = function( $, $elem, tweetID, tweetFactory, utils, logger ) {//DONE
	var valid = false, src = '';

	this.isValid = function() {
		return valid;
	};

	this.getValue = function() {
		return valid ? 'VERIFIED' : null;
	};

	var $svg = $( 'div > div > div > div > div > div > div > div > div > div > div > a > div > div > span > svg', $elem );

	if ( $svg.length < 1 ) {
		valid = false;
		return;
	}

	src = ' ' + $svg.attr( 'aria-label' ) + ' ' + $svg.data( 'testid' );

	valid = src.toLowerCase().indexOf( 'verified' ) > -1 ? true : false;
};

com.tolstoy.basic.app.tweetparser.html.helper.Date1 = function( $, $elem, tweetID, tweetFactory, utils, logger ) {//DONE
	var valid = false, dateString = '';

	this.isValid = function() {
		return valid;
	};

	this.getDateString = function() {
		return dateString;
	};

	dateString = $( 'time', $elem ).attr( 'datetime' );

	valid = !!dateString;
};

com.tolstoy.basic.app.tweetparser.html.helper.Date2 = function( $, $elem, tweetID, tweetFactory, utils, logger ) {//NOTWORKING
	var valid = false, dateString = '';

	this.isValid = function() {
		return valid;
	};

	this.getDateString = function() {
		return dateString;
	};

	$('div > div > a', $elem).each( function() {
		var $t = $(this);
		var link = tweetFactory.makeTweetLink( { source: $t.attr( 'href' ) } );

		$t.parent().find( '> span' ).each( function() {
			var ariaHidden = $(this).attr( 'aria-hidden' );
			dateString = $(this).text();
			if ( dateString && link.isHelpLink() && ariaHidden != 'true' ) {
				valid = true;
				var dateAry = dateString.split( '·' );
				if ( dateAry && dateAry.length == 2 ) {
					dateString = dateAry[ 1 ] + ' ' + dateAry[ 0 ];
					return false;
				}
				else {
					dateString = '';
				}
			}
		});
	});
};

com.tolstoy.basic.app.tweetparser.html.helper.Tweetid1 = function( $, $elem, tweetID, tweetFactory, utils, logger ) {//DONE
	var valid = false, id = '';

	this.isValid = function() {
		return valid;
	};

	this.getID = function() {
		return id;
	};

	$( 'time', $elem ).each( function() {
		var $t = $(this);
		var $par = $t.parent();
		var datetime = $t.attr( 'datetime' );
		var link = tweetFactory.makeTweetLink( { source: $par.attr( 'href' ) } );

		if ( datetime && $par.is( 'a' ) && link.isValid() && link.isStatusLink() ) {
			id = link.getTweetID();
			return false;
		}
	});

	valid = !!id;
};

com.tolstoy.basic.app.tweetparser.html.helper.Tweetid2 = function( $, $elem, tweetID, tweetFactory, utils, logger ) {//NOTWORKING
	var valid = false, id = '';

	this.isValid = function() {
		return valid;
	};

	this.getID = function() {
		return id;
	};

	$('div > div > a', $elem).each( function() {
		var $t = $(this);
		var link = tweetFactory.makeTweetLink( { source: $t.attr( 'href' ) } );

		if ( link.isInteractionLink() ) {
			id = link.getTweetID();
			valid = true;

			return false;
		}
	});
};

com.tolstoy.basic.app.tweetparser.html.helper.Tweettext1 = function( $, $elem, tweetID, tweetFactory, utils, logger ) {//DONE
	var valid = false, text = '', lang = '', html = '';

	this.isValid = function() {
		return valid;
	};

	this.getTweettext = function() {
		return text;
	};

	this.getTweetHTML = function() {
		return html;
	};

	this.getLang = function() {
		return lang;
	};

	$( 'div > div > span', $elem ).each( function() {
		var $t = $(this);
		var $par = $t.parent();
		lang = $par.attr( 'lang' );
		text = $par.text();
		html = $par.html();
		if ( lang && text ) {
			valid = true;
			return false;
		}
		else {
			lang = text = html = '';
		}
	});
};


com.tolstoy.basic.app.tweetparser.html.helper.Interaction1 = function( $, $elem, tweetID, tweetFactory, utils, logger ) {
	var valid = false, replyCount = '', retweetCount = '', likesCount = '', regex = /(\d+) .*, (\d+) .*, (\d+) /g;

	this.isValid = function() {
		return valid;
	};

	this.getReplyCount = function() {
		return replyCount;
	};

	this.getRetweetCount = function() {
		return retweetCount;
	};

	this.getLikesCount = function() {
		return likesCount;
	};

	$( 'div[aria-label]', $elem ).each( function() {
		var ary = regex.exec( $(this).attr( 'aria-label' ) );
		if ( ary && ary.length > 2 ) {
			replyCount = ary[ 1 ].trim();
			retweetCount = ary[ 2 ].trim();
			likesCount = ary[ 3 ].trim();

			valid = true;

			return false;
		}
		else {
			replyCount = retweetCount = likesCount = '';
		}
	});

	if ( this.isValid ) {
		//logger.info( 'Interaction1 for ' + tweetID + ', replies=' + this.getReplyCount() + ', RT=' + this.getRetweetCount() + ', like=' + this.getLikesCount() );
	}
	else {
		logger.info( 'Interaction1 for ' + tweetID + ': NOT VALID' );
	}
};

com.tolstoy.basic.app.tweetparser.html.helper.Interaction2 = function( $, $elem, tweetID, tweetFactory, utils, logger ) {
	var valid = false, replyCount = '', retweetCount = '', likesCount = '';
	var repliesRegex = /(\d+) reply/gi;
	var retweetsRegex = /(\d+) retweet/gi;
	var likesRegex = /(\d+) like/gi;

	this.isValid = function() {
		return valid;
	};

	this.getReplyCount = function() {
		return replyCount;
	};

	this.getRetweetCount = function() {
		return retweetCount;
	};

	this.getLikesCount = function() {
		return likesCount;
	};

	$( 'div[aria-label]', $elem ).each( function() {
		var label = $(this).attr( 'aria-label' );
		var ary;

		ary = repliesRegex.exec( label );
		if ( ary && ary.length > 1 ) {
			replyCount = ary[ 1 ].trim();
			valid = true;
		}

		ary = retweetsRegex.exec( label );
		if ( ary && ary.length > 1 ) {
			retweetCount = ary[ 1 ].trim();
			valid = true;
		}

		ary = likesRegex.exec( label );
		if ( ary && ary.length > 1 ) {
			likesCount = ary[ 1 ].trim();
			valid = true;
		}

		if ( valid ) {
			return false;
		}
		else {
			replyCount = retweetCount = likesCount = '';
		}
	});

	if ( this.isValid ) {
		//logger.info( 'Interaction2 for ' + tweetID + ', replies=' + this.getReplyCount() + ', RT=' + this.getRetweetCount() + ', like=' + this.getLikesCount() );
	}
	else {
		logger.info( 'Interaction2 for ' + tweetID + ': NOT VALID' );
	}
};

com.tolstoy.basic.app.tweetparser.html.helper.Interaction3 = function( $, $elem, tweetID, tweetFactory, utils, logger ) {
	var valid = false, replyCount = '', retweetCount = '', likesCount = '', quoteTweetCount = '';

	this.isValid = function() {
		return valid;
	};

	this.getReplyCount = function() {
		return replyCount;
	};

	this.getRetweetCount = function() {
		return retweetCount;
	};

	this.getQuoteTweetCount = function() {
		return quoteTweetCount;
	};

	this.getLikesCount = function() {
		return likesCount;
	};

	$( 'article > div > div > div > div > div > div > div > div > a[role="link"]', $elem ).each( function() {
		var $t = $(this);
		var href = $t.attr( 'href' );
		var $valContainer = $t.find( 'div > span > span > span' );
		var val = $valContainer.text();

		if ( !href || $valContainer.length < 1 || !val ) {
			return;
		}

		val = val.trim( '/' );

		if ( val.endsWith( 'retweets' ) ) {
			retweetCount = val;
			valid = true;
		}
		else if ( val.endsWith( 'retweets/with_comments' ) ) {
			quoteTweetCount = val;
			valid = true;
		}
		else if ( val.endsWith( 'likes' ) ) {
			likesCount = val;
			valid = true;
		}
	});

	if ( this.isValid ) {
		//logger.info( 'Interaction3 for ' + tweetID + ', replies=' + this.getReplyCount() + ', RT=' + this.getRetweetCount() + ', like=' + this.getLikesCount() );
	}
	else {
		logger.info( 'Interaction3 for ' + tweetID + ': NOT VALID' );
	}
};

com.tolstoy.basic.app.tweetparser.html.helper.ViewsCount = function( $, $elem, tweetID, tweetFactory, utils, logger ) {
	var valid = false, viewsCount = '';

	this.isValid = function() {
		return valid;
	};

	this.getViewsCount = function() {
		return viewsCount;
	};

	$( 'a', $elem ).each( function() {
		var $t = $(this);
		var numString = $t.attr( 'aria-label' );
		var href = $t.attr( 'href' );
		if ( !numString || !href || href.indexOf( 'analytics' ) < 0 ) {
			return;
		}

		var phrase = utils.makeNumericPhrase( numString );
		viewsCount = phrase.getNumber( 0 );
		if ( viewsCount === null ) {
			return;
		}

		valid = true;
		return false;
	});

	if ( this.isValid ) {
		//logger.info( 'ViewsCount for ' + tweetID + ', count=' + this.getViewsCount() );
	}
	else {
		logger.info( 'ViewsCount for ' + tweetID + ': NOT VALID' );
	}
};

com.tolstoy.basic.app.tweetparser.html.helper.Permalink1 = function( $, $elem, tweetID, tweetFactory, utils, logger ) {//DONE
	var valid = false, permalink = '';

	this.isValid = function() {
		return valid;
	};

	this.getPermalink = function() {
		return permalink;
	};

	$( 'time', $elem ).each( function() {
		var $t = $(this);
		var link = tweetFactory.makeTweetLink( { source: $t.parent().attr( 'href' ) } );

		if ( $t.attr( 'datetime' ) && link.isStatusLink() ) {
			permalink = link.getSource();
			return false;
		}
	});

	valid = !!permalink;
};

com.tolstoy.basic.app.tweetparser.html.helper.Permalink2 = function( $, $elem, tweetID, tweetFactory, utils, logger ) {//DONE
	var valid = false, permalink = '';

	this.isValid = function() {
		return valid;
	};

	this.getPermalink = function() {
		return permalink;
	};

	$( 'div > div > a[role="link"]', $elem ).each( function() {
		var $t = $(this);
		var link = tweetFactory.makeTweetLink( { source: $t.attr( 'href' ) } );

		if ( link.isStatusLink() || link.isRetweetLink() || link.isLikeLink() || link.isReplyLink() ) {
			permalink = link.getStatusLink();
			return false;
		}
	});

	valid = !!permalink;
};

com.tolstoy.basic.app.tweetparser.html.helper.Photo1 = function( $, $elem, tweetID, tweetFactory, utils, logger ) {//DONE
	var valid = false, photoLink = '', photoImage = '';

	this.isValid = function() {
		return valid;
	};

	this.getPhotoLink = function() {
		return photoLink;
	};

	this.getPhotoImage = function() {
		return photoImage;
	};

	$( 'div > div > a', $elem ).each( function() {
		var $t = $(this);
		var $par = $t.parent();
		var link = tweetFactory.makeTweetLink( { source: $t.attr( 'href' ) } );

		if ( link && link.isPhotoLink() ) {
			$( 'div > div > img', $par ).each( function () {
				var style = $(this).prev().attr( 'style' );
				var src = $(this).attr( 'src' );
				if ( src && style && style.indexOf( 'background-image' ) > -1 ) {
					photoLink = link.getSource();
					photoImage = src;
					valid = true;
					return false;
				}
			});
		}

		if ( valid ) {
			return false;
		}
	});
};

com.tolstoy.basic.app.tweetparser.html.ParsedTweetFactory = function( $, tweetFactory, utils, logger ) {
	var helpers = com.tolstoy.basic.app.tweetparser.html.helper;

	var debugHelpers = [
		helpers.Debug,
	];
	var dateHelpers = [
		helpers.Date1,
		helpers.Date2
	];
	var tweetidHelpers = [
		helpers.Tweetid1,
		helpers.Tweetid2
	];
	var permalinkHelpers = [
		helpers.Permalink1,
		helpers.Permalink2
	];
	var tweettextHelpers = [
		helpers.Tweettext1
	];
	var interactionHelpers = [
		helpers.Interaction1,
		helpers.Interaction3,
		helpers.Interaction2
	];
	var viewsCountHelpers = [
		helpers.ViewsCount
	];
	var photoHelpers = [
		helpers.Photo1
	];
	var authorAvatarHelpers = [
		helpers.AuthorAvatar
	];
	var authorNameHelpers = [
		helpers.AuthorNames
	];
	var authorVerifiedHelpers = [
		helpers.AuthorVerified
	];

	this.makeTweet = function( $article ) {
		var tweet = tweetFactory.makeTweet();

		var user = tweet.getAttribute( 'user' );

		this.runImplementations( debugHelpers, [ $, $article, 0, tweetFactory, utils, logger ], function( impl ) {
		},
		function() {
		});

		this.runImplementations( tweetidHelpers, [ $, $article, 0, tweetFactory, utils, logger ], function( impl ) {
			tweet.setAttribute( 'tweetid', impl.getID() );
		},
		function() {
			tweet.addError( 'cannot find tweetid' );
		});

		var tweetID = tweet.getAttribute( 'tweetid' );

		this.runImplementations( interactionHelpers, [ $, $article, tweetID, tweetFactory, utils, logger ], function( impl ) {
			tweet.setAttribute( 'replycount', impl.getReplyCount() );
			tweet.setAttribute( 'retweetcount', impl.getRetweetCount() );
			tweet.setAttribute( 'favoritecount', impl.getLikesCount() );
		},
		function() {
			tweet.addError( 'cannot find interactions' );
		});

		this.runImplementations( viewsCountHelpers, [ $, $article, tweetID, tweetFactory, utils, logger ], function( impl ) {
			tweet.setAttribute( 'viewscount', impl.getViewsCount() );
		},
		function() {
			tweet.addError( 'cannot find viewscount' );
		});

		this.runImplementations( tweettextHelpers, [ $, $article, tweetID, tweetFactory, utils, logger ], function( impl ) {
			tweet.setAttribute( 'tweettext', impl.getTweettext() );
			tweet.setAttribute( 'tweethtml', impl.getTweetHTML() );
			tweet.setAttribute( 'tweetlanguage', impl.getLang() );
		},
		function() {
			tweet.addError( 'cannot find tweettext' );
		});

		this.runImplementations( photoHelpers, [ $, $article, tweetID, tweetFactory, utils, logger ], function( impl ) {
			tweet.setAttribute( 'tweetphoto_link', impl.getPhotoLink() );
			tweet.setAttribute( 'tweetphoto_image', impl.getPhotoImage() );
		},
		function() {
			tweet.addError( 'cannot find photos' );
		});

		this.runImplementations( permalinkHelpers, [ $, $article, tweetID, tweetFactory, utils, logger ], function( impl ) {
			tweet.setAttribute( 'permalinkpath', impl.getPermalink() );
		},
		function() {
			tweet.addError( 'cannot find permalinkpath' );
		});

		this.runImplementations( dateHelpers, [ $, $article, tweetID, tweetFactory, utils, logger ], function( impl ) {
			tweet.setAttribute( 'datestring', impl.getDateString() );
		},
		function() {
			tweet.addError( 'cannot find datestring' );
		});

		this.runImplementations( authorAvatarHelpers, [ $, $article, tweetID, tweetFactory, utils, logger ], function( impl ) {
			tweet.setAttribute( 'avatarURL', impl.getImage() );
			user.setAttribute( 'avatarURL', impl.getImage() );
		},
		function() {
			user.addError( 'cannot find avatarURL' );
		});

		this.runImplementations( authorNameHelpers, [ $, $article, tweetID, tweetFactory, utils, logger ], function( impl ) {
			user.setAttribute( 'handle', impl.getHandle() );
			user.setAttribute( 'displayName', impl.getDisplayName() );
		},
		function() {
			user.addError( 'cannot find screenname' );
		});

		this.runImplementations( authorVerifiedHelpers, [ $, $article, tweetID, tweetFactory, utils, logger ], function( impl ) {
			user.setAttribute( 'verifiedText', impl.getValue() );
		},
		function() {
		});

		if ( !user.getAttribute( 'handle' ) ) {
			logger.info( 'BAD HTML, no handle:' + $article.html() );
		}

		if ( !tweet.getAttribute( 'tweetid' ) ) {
			logger.info( 'BAD HTML, no tweetid:' + $article.html() );
		}

		if ( !tweet.getAttribute( 'permalinkpath' ) ) {
			//logger.info( 'BAD HTML, no permalinkpath:' + $article.html() );
		}

		return tweet;
	};

	this.runImplementations = function( implementations, args, successCallback, failureCallback ) {
		var implementation;

		implementations = implementations || [];
		for ( var i = 0; i < implementations.length; i++ ) {
			implementation = this.constructObject( implementations[ i ], args );

			if ( implementation.isValid() ) {
				successCallback( implementation );
				return;
			}
		}

		failureCallback();
	};

	//	from https://stackoverflow.com/a/1608546
	this.constructObject = function( constructor, args ) {
		function F() {
			return constructor.apply( this, args );
		}

		F.prototype = constructor.prototype;

		return new F();
	};
};

com.tolstoy.basic.app.tweetparser.json.helper.InstructionAddEntriesHelper = function( $, tweetFactory, utils, logger ) {
	var moi = this;

	this.makeInstructionTimelineCursor = function( entry, errorCallback ) {
	};

	this.makeInstructionTimelineItem = function( entry, ret, errorCallback ) {
		var result, result2;
		result = utils.getNestedJSON( entry, [ 'content', 'itemContent', 'tweet_results', 'result' ] );
		if ( result && result.__typename && result.__typename == 'Tweet' ) {
			if ( utils.getNestedJSON( result, [ 'core', 'user_results', 'result', '__typename' ] == 'User' ) ) {
				utils.swapUserID( result.core.user_results.result );
				ret.users.push( result.core.user_results.result );
				delete result.core.user_results.result;
			}
			ret.tweets.push( result );
			delete entry.content.itemContent.tweet_results.result;
		}

		result = utils.getNestedJSON( entry, [ 'core', 'user_results', 'result' ] );
		if ( result && result.__typename && result.__typename == 'User' ) {
			ret.users.push( result );
			delete entry.core.user_results.result;
		}

		result = utils.getNestedJSON( entry, [ 'content', 'items' ] );
		if ( result && Array.isArray( result ) ) {
			$.each( result, function( inner_index, inner_val ) {
				if ( inner_val.entryId && inner_val.entryId.indexOf( 'whoToFollow' ) > -1 ) {
					delete entry.content.items[ inner_index ];
				}
				else {
					result2 = utils.getNestedJSON( inner_val, [ 'item', 'itemContent', 'tweet_results', 'result' ] );
					if ( result2 && utils.getNestedJSON( result2, [ 'core', 'user_results', 'result', '__typename' ] ) == 'User' ) {
						ret.users.push( entry.entries[ index ].content.items[ inner_index ].item.itemContent.tweet_results.result.core.user_results.result );
						delete entry.content.items[ inner_index ].item.itemContent.tweet_results.result.core.user_results.result;
					}

					if ( result2 && result2.__typename && result2.__typename == 'Tweet' ) {
						ret.tweets.push( result2 );
						delete entry.content.items[ inner_index ].item.itemContent.tweet_results.result;
					}
				}
			});
		}
	};

	this.makeInstructionTimelineModule = function( entry, ret, errorCallback ) {
		var items = utils.getNestedJSON( entry, [ 'content', 'items' ] );
		if ( !items ) {
			return;
		}

		var displayType = utils.getNestedJSON( entry, [ 'content', 'displayType' ] );
		$.each( items, function( index, item ) {
			var tweetID = utils.getNestedJSON( item, [ 'item', 'itemContent', 'tweet_results', 'result', 'rest_id' ] );
			var supposedQuality = utils.getNestedJSON( item, [ 'item', 'clientEventInfo', 'details', 'conversationDetails', 'conversationSection' ] );

			if ( tweetID && supposedQuality ) {
				ret.timelineItems.push({
					tweetID: tweetID,
					displayType: displayType ? displayType : '',
					conversationSection: supposedQuality
				});

				delete entry.content.items[ index ].item.clientEventInfo.details.conversationDetails.conversationSection;
			}

			var rawUser = utils.getNestedJSON( item, [ 'item', 'itemContent', 'tweet_results', 'result', 'core', 'user_results', 'result' ] );
			if ( rawUser && rawUser.__typename && rawUser.__typename == 'User' ) {
				ret[ 'users' ].push( rawUser );

				delete entry.content.items[ index ].item.itemContent.tweet_results.result.core.user_results.result;
			}

			var rawTweet = utils.getNestedJSON( item, [ 'item', 'itemContent', 'tweet_results', 'result' ] );
			if ( rawTweet && rawTweet.__typename && rawTweet.__typename == 'Tweet' ) {
				ret[ 'tweets' ].push( rawTweet );

				delete entry.content.items[ index ].item.itemContent.tweet_results.result;
			}
		});

		console.log( "timelineItems=", ret.timelineItems );
	};

	this.makeInstructionAddEntries = function( json, ret, errorCallback ) {
		var ret = {
			type: 'AddEntries',
			timelineItems: [],
			tweets: [],
			users: [],
			otherItems: []
		};

		if ( !json.entries ) {
			errorCallback( 'InstructionAddEntries: no json.entries' );
			return ret;
		}

		$.each( json.entries, function( index, entry ) {
			if ( entry.content && entry.content.entryType ) {
				if ( entry.content.entryType == 'TimelineTimelineCursor' ) {
					moi.makeInstructionTimelineCursor( entry, ret, errorCallback );
				}
				else if ( entry.content.entryType == 'TimelineTimelineItem' ) {
					moi.makeInstructionTimelineItem( entry, ret, errorCallback );
				}
				else if ( entry.content.entryType == 'TimelineTimelineModule' ) {
					moi.makeInstructionTimelineModule( entry, ret, errorCallback );
				}
			}
		});

		return ret;
	};
};

com.tolstoy.basic.app.tweetparser.json.helper.InstructionTerminateTimelineHelper = function( $, tweetFactory, utils, logger ) {
	this.makeInstructionTerminateTimeline = function( json, errorCallback ) {
		var ret = {
			type: 'TerminateTimeline',
			direction: json.direction
		};

		return ret;
	};
};

com.tolstoy.basic.app.tweetparser.json.helper.TweetHelper = function( $, tweetFactory, utils, logger ) {
	var descriptors = [
		{ targetKey: 'tweetid', sourceKey: 'id_str', defaultValue: '0' },
		{ targetKey: 'userid', sourceKey: 'user_id_str', defaultValue: '0' },

		{ targetKey: 'datestring', sourceKey: 'created_at', defaultValue: '' },
		{ targetKey: 'tweettext', sourceKey: 'full_text', defaultValue: '' },
		{ targetKey: 'tweetlanguage', sourceKey: 'lang', defaultValue: 'en' },

		{ targetKey: 'isreplyto', sourceKey: 'in_reply_to_status_id_str', defaultValue: '' },
		{ targetKey: 'repliedtohandle', sourceKey: 'in_reply_to_screen_name', defaultValue: '' },
		{ targetKey: 'repliedtouserid', sourceKey: 'in_reply_to_user_id_str', defaultValue: '' },

		{ targetKey: 'favoritecount', sourceKey: 'favoritecount', defaultValue: '0' },
		{ targetKey: 'replycount', sourceKey: 'replycount', defaultValue: '0' },
		{ targetKey: 'retweetcount', sourceKey: 'retweetcount', defaultValue: '0' },
		{ targetKey: 'viewscount', sourceKey: 'viewscount', defaultValue: '0' },

		{ targetKey: 'conversationid', sourceKey: 'conversation_id_str', defaultValue: '' },

		{
			targetKey: 'tweetmentions',
			sourceKey: 'tweetmentions',
			defaultValue: '',
			importer: function( target, source ) {
				var mentions = [];
				if ( source.entities && source.entities.user_mentions ) {
					$.each( source.entities && source.entities.user_mentions, function() {
						if ( this.screen_name ) {
							mentions.push( this.screen_name );
						}
					});
				}
				target[ 'tweetmentions' ] = mentions.join( ',' );
			}
		},
		{
			targetKey: 'tweetphoto_link',
			sourceKey: 'tweetphoto_link',
			defaultValue: '',
			importer: function( target, source ) {
				if ( source.entities && source.entities.media && source.entities.media.expanded_url ) {
					target[ 'tweetphoto_link' ] = source.entities.media.expanded_url;
				}
				else {
					target[ 'tweetphoto_link' ] = '';
				}
			}
		},
		{
			targetKey: 'tweetphoto_image',
			sourceKey: 'tweetphoto_image',
			defaultValue: '',
			importer: function( target, source ) {
				if ( source.entities && source.entities.media && ( source.entities.media.media_url_https || source.entities.media.media_url ) ) {
					target[ 'tweetphoto_image' ] = source.entities.media.media_url_https || source.entities.media.media_url;
				}
				else {
					target[ 'tweetphoto_image' ] = '';
				}
			}
		},
		{
			targetKey: 'videothumburl',
			sourceKey: 'videothumburl',
			defaultValue: '',
			importer: function( target, source ) {
				if ( source.extended_entities &&
						source.extended_entities.media &&
						( source.extended_entities.media.media_url_https || source.extended_entities.media.media_url ) &&
						source.extended_entities.media.video_info  ) {
					target[ 'videothumburl' ] = source.extended_entities.media.media_url_https || source.extended_entities.media.media_url;
				}
				else {
					target[ 'videothumburl' ] = '';
				}
			}
		},
		{
			targetKey: 'time',
			sourceKey: 'time',
			defaultValue: '0',
			importer: function( target, source ) {
				target[ 'time' ] = source.created_at ? Date.parse( source.created_at ) : 0;
				if ( isNaN( target[ 'time' ] ) ) {
					target[ 'time' ] = 0;
				}

				target[ 'time' ] /= 1000;	//	'time' is in seconds since Unix epoch
			}
		},
		{
			targetKey: 'hascards',
			sourceKey: 'hascards',
			defaultValue: '0',
			importer: function( target, source ) {
				target[ 'hascards' ] = source.card ? '1' : '0';
			}
		}
	];

/*	NOT AVAILABLE AT THIS STAGE:
		{ targetKey: 'avatarURL', sourceKey: 'avatarURL', defaultValue: '' },
		{ targetKey: 'componentcontext', sourceKey: 'componentcontext', defaultValue: '' },
		{ targetKey: 'disclosuretype', sourceKey: 'disclosuretype', defaultValue: '' },
		{ targetKey: 'followsyou', sourceKey: 'followsyou', defaultValue: '' },
		{ targetKey: 'fullname', sourceKey: 'fullname', defaultValue: '' },
		{ targetKey: 'hasparenttweet', sourceKey: 'hasparenttweet', defaultValue: '' },
		{ targetKey: 'innertweetid', sourceKey: 'innertweetid', defaultValue: '' },
		{ targetKey: 'innertweetrawhref', sourceKey: 'innertweetrawhref', defaultValue: '' },
		{ targetKey: 'is_pinned', sourceKey: 'is_pinned', defaultValue: '' },
		{ targetKey: 'is_toptweet', sourceKey: 'is_toptweet', defaultValue: '' },
		{ targetKey: 'itemid', sourceKey: 'itemid', defaultValue: '' },
		{ targetKey: 'name', sourceKey: 'name', defaultValue: '' },
		{ targetKey: 'permalinkpath', sourceKey: 'permalinkpath', defaultValue: '' },
		{ targetKey: 'photourl', sourceKey: 'photourl', defaultValue: '' },
		{ targetKey: 'quality', sourceKey: 'quality', defaultValue: 'unknown_quality' },
		{ targetKey: 'replytousersjson', sourceKey: 'replytousersjson', defaultValue: '' },
		{ targetKey: 'retweetid', sourceKey: 'retweetid', defaultValue: '0' },
		{ targetKey: 'screenname', sourceKey: 'screenname', defaultValue: '' },
		{ targetKey: 'suggestionjson', sourceKey: 'suggestionjson', defaultValue: '' },
		{ targetKey: 'tweetclasses', sourceKey: 'tweetclasses', defaultValue: '' },
		{ targetKey: 'tweethtml', sourceKey: 'tweethtml', defaultValue: '' },
		{ targetKey: 'tweetnonce', sourceKey: 'tweetnonce', defaultValue: '' },
		{ targetKey: 'tweetstatinitialized', sourceKey: 'tweetstatinitialized', defaultValue: '' },
		{ targetKey: 'username', sourceKey: 'username', defaultValue: '' },
		{ targetKey: 'verifiedText', sourceKey: 'verifiedText', defaultValue: '' },
		{ targetKey: 'youblock', sourceKey: 'youblock', defaultValue: '' },
		{ targetKey: 'youfollow', sourceKey: 'youfollow', defaultValue: '' },
*/

	this.makeTweet = function( json, errorCallback ) {
		var input = {}, legacyInput = {};

		utils.importDataUsingDescriptors( input, json, descriptors );

		if ( json.legacy ) {
			utils.importDataUsingDescriptors( legacyInput, json.legacy, descriptors );
		}

		input = utils.extend( input, legacyInput );

		if ( !input.id ) {
			if ( json.id && /^\d+$/.test( json.id ) ) {
				input.id = json.id;
			}
			else if ( json.rest_id && /^\d+$/.test( json.rest_id ) ) {
				input.id = json.rest_id;
			}
		}

		input.innertweetid = utils.getNestedJSON( json, [ 'quoted_status_result', 'result', 'rest_id' ] );
		input.innertweetrawhref = utils.getNestedJSON( json, [ 'legacy', 'quoted_status_permalink', 'expanded' ] );

		return tweetFactory.makeTweet( input );
	};
};

com.tolstoy.basic.app.tweetparser.json.helper.UserHelper = function( $, tweetFactory, utils, logger ) {
	var descriptors = [
		{ targetKey: 'id', sourceKey: 'id_str', defaultValue: '' },
		{ targetKey: 'handle', sourceKey: 'screen_name', defaultValue: '' },
		{ targetKey: 'displayName', sourceKey: 'name', defaultValue: '' },
		{ targetKey: 'avatarURL', sourceKey: 'profile_image_url_https', defaultValue: '' },
		{ targetKey: 'verifiedStatus', sourceKey: 'verified', defaultValue: '' },
		{ targetKey: 'numTotalTweets', sourceKey: 'statuses_count', defaultValue: '' },
		{ targetKey: 'numFollowers', sourceKey: 'followers_count', defaultValue: '' },
		{ targetKey: 'numFollowing', sourceKey: 'friends_count', defaultValue: '' },
		{ targetKey: 'canDM', sourceKey: 'can_dm', defaultValue: '' },
		{ targetKey: 'canMediaTag', sourceKey: 'can_media_tag', defaultValue: '' },
		{ targetKey: 'advertiserAccountType', sourceKey: 'advertiser_account_type', defaultValue: '' },
		{ targetKey: 'blueSubscriber', sourceKey: 'ext_is_blue_verified', defaultValue: '' },
		{ targetKey: 'blueSubscriber', sourceKey: 'is_blue_verified', defaultValue: '' },
		{ targetKey: 'requireSomeConsent', sourceKey: 'require_some_consent', defaultValue: '' },
		{ targetKey: 'hasGraduatedAccess', sourceKey: 'has_graduated_access', defaultValue: '' },
		{ targetKey: 'superFollowEligible', sourceKey: 'super_follow_eligible', defaultValue: '' },
		{
			targetKey: 'withheldInCountries',
			defaultValue: '',
			importer: function( target, source ) {
				target.withheldInCountries = source.withheld_in_countries ? source.withheld_in_countries.join( ' ;;; ' ) : '';
			}
		}
	];

	this.makeUser = function( json, errorCallback ) {
		var input = {}, legacyInput = {};

		utils.importDataUsingDescriptors( input, json, descriptors );

		if ( json.legacy ) {
			utils.importDataUsingDescriptors( legacyInput, json.legacy, descriptors );
		}

		input = utils.extend( input, legacyInput );

		if ( !input.id ) {
			if ( json.id && /^\d+$/.test( json.id ) ) {
				input.id = json.id;
			}
			else if ( json.rest_id && /^\d+$/.test( json.rest_id ) ) {
				input.id = json.rest_id;
			}
		}

		input.verifiedStatus = input.verifiedStatus ? 'VERIFIED' : 'UNKNOWN';

		var user = tweetFactory.makeUser( input );

		return user;
	};
};

com.tolstoy.basic.app.tweetparser.json.ParsedJSONFactory = function( $, tweetFactory, utils, logger ) {
	var helpers = com.tolstoy.basic.app.tweetparser.json.helper;

	var tweetHelper = new helpers.TweetHelper( $, tweetFactory, utils, logger );
	var userHelper = new helpers.UserHelper( $, tweetFactory, utils, logger );
	var addEntriesHelper = new helpers.InstructionAddEntriesHelper( $, tweetFactory, utils, logger );
	var terminateTimelineHelper = new helpers.InstructionTerminateTimelineHelper( $, tweetFactory, utils, logger );

	this.parseJSON = function( json ) {
		var ret = {
			tweets: [],
			users: [],
			instructions: [],
			errors: []
		};

		function addError( msg ) {
			ret.errors.push( msg );
		}

		function addInstructions( instructionsResult ) {
			$.each( instructionsResult[ 'tweets' ], function( index, val ) {
				ret.tweets.push( tweetHelper.makeTweet( val, addError ) );
			});

			$.each( instructionsResult[ 'users' ], function( index, val ) {
				ret.users.push( userHelper.makeUser( val, addError ) );
			});

			instructionsResult[ 'tweets' ] = [];
			instructionsResult[ 'users' ] = [];

			ret.instructions.push( instructionsResult );
		}

		var parser = this.createParser( json );
		if ( !parser || !parser.isValid() ) {
			return ret;
		}

		console.log( 'parser=' + parser.getName() );

		$.each( parser.getRawTweets(), function( index, val ) {
			ret.tweets.push( tweetHelper.makeTweet( val, addError ) );
		});

		$.each( parser.getRawUsers(), function( index, val ) {
			ret.users.push( userHelper.makeUser( val, addError ) );
		});

		$.each( parser.getRawInstructions(), function( index, val ) {
			if ( val.type ) {
				if ( val.type == 'TimelineAddEntries' ) {
					addInstructions( addEntriesHelper.makeInstructionAddEntries( val, addError ) );
					
				}
				else if ( val.type == 'TimelineTerminateTimeline' ) {
					addInstructions( terminateTimelineHelper.makeInstructionTerminateTimeline( val, addError ) );
				}
			}
			else if ( val.terminateTimeline ) {
				addInstructions( terminateTimelineHelper.makeInstructionTerminateTimeline( val.terminateTimeline, addError ) );
			}
			else if ( val.addEntries ) {
				//	ignore
			}
			else if ( val.clearCache ) {
				//	ignore
			}
			else if ( val.clearEntriesUnreadState ) {
				//	ignore
			}
			else if ( val.markEntriesUnreadGreaterThanSortIndex ) {
				//	ignore
			}
			else {
				addError( 'ParsedJSONFactory: unknown instruction' );
			}
		});

		return ret;
	};

	this.createParser = function( json ) {
		if ( !json || json.length < 4 ) {
			return null;
		}

		if ( json.JSON ) {
			json = json.JSON;
		}

		//	??
		if ( json.data && json.data.user_result_by_screen_name ) {
			return new com.tolstoy.basic.app.tweetparser.json.JSONParserIgnored( json, $, tweetFactory, utils, logger );
		}

		//	something to do with media; one file had a nm of 'DownVote_02_C'
		if ( json.w  && json.h && ( json.nm || json.assets ) ) {
			return new com.tolstoy.basic.app.tweetparser.json.JSONParserIgnored( json, $, tweetFactory, utils, logger );
		}

		//	user settings
		if ( typeof json[ 'discoverable_by_email' ] !== 'undefined' ) {
			return new com.tolstoy.basic.app.tweetparser.json.JSONParserIgnored( json, $, tweetFactory, utils, logger );
		}

		//	list of domain names to promote?
		if ( utils.getNestedJSON( json, [ 'data', 'viewer', 'article_nudge_domains' ] ) ) {
			return new com.tolstoy.basic.app.tweetparser.json.JSONParserIgnored( json, $, tweetFactory, utils, logger );
		}

		//	hashflags
		if ( Array.isArray( json ) && json.length > 0 && json[ 0 ].starting_timestamp_ms ) {
			return new com.tolstoy.basic.app.tweetparser.json.JSONParserIgnored( json, $, tweetFactory, utils, logger );
		}

		if ( json.data && json.data.users && Array.isArray( json.data.users ) && json.data.users.length > 0 && json.data.users[ 0 ].result ) {
			return new com.tolstoy.basic.app.tweetparser.json.JSONParserIncompleteUserList( json, $, tweetFactory, utils, logger );
		}

		if ( json.data && json.data.user && json.data.user.result && !json.data.user.result.timeline_v2 ) {
			return new com.tolstoy.basic.app.tweetparser.json.JSONParserIncompleteUser( json, $, tweetFactory, utils, logger );
		}

		if ( json.globalObjects || json.timeline ) {
			return new com.tolstoy.basic.app.tweetparser.json.JSONParserGlobalTimeline( json, $, tweetFactory, utils, logger );
		}

		if ( Array.isArray( json ) && json.length > 0 && json[ 0 ].token ) {
			return new com.tolstoy.basic.app.tweetparser.json.JSONParserUserList( json, $, tweetFactory, utils, logger );
		}

		if ( json.data && json.data.threaded_conversation_with_injections_v2 ) {
			return new com.tolstoy.basic.app.tweetparser.json.JSONParserThreadedConversation( json, $, tweetFactory, utils, logger );
		}

		if ( utils.getNestedJSON( json, [ 'data', 'user', 'result', 'timeline_v2', 'timeline' ] ) ) {
			return new com.tolstoy.basic.app.tweetparser.json.JSONParserTimelineV2( json, $, tweetFactory, utils, logger );
		}

		logger.info( 'ParsedJSONFactory: unrecognized JSON ' + JSON.stringify( json ) );

		return null;
	};
};

com.tolstoy.basic.app.tweetparser.json.JSONParserGlobalTimeline = function( input, $, tweetFactory, utils, logger ) {
	var valid = false, tweets = [], users = [], instructions = [];

	this.getName = function() {
		return 'GlobalTimeline';
	};

	this.isValid = function() {
		return valid;
	};

	this.getRemaining = function() {
		return input;
	};

	this.getRawTweets = function() {
		return tweets;
	};

	this.getRawUsers = function() {
		return users;
	};

	this.getRawInstructions = function() {
		return instructions;
	};

	var k;

	if ( input.globalObjects.tweets ) {
		for ( k in input.globalObjects.tweets ) {
			tweets.push( input.globalObjects.tweets[ k ] );
		}

		delete input.globalObjects.tweets;
	}

	if ( input.globalObjects.users ) {
		for ( k in input.globalObjects.users ) {
			users.push( input.globalObjects.users[ k ] );
		}

		delete input.globalObjects.users;
	}

	if ( input.timeline.tweets ) {
		for ( k in input.timeline.tweets ) {
			tweets.push( input.timeline.tweets[ k ] );
		}

		delete input.timeline.tweets;
	}

	if ( input.timeline.users ) {
		for ( k in input.timeline.users ) {
			users.push( input.timeline.users[ k ] );
		}

		delete input.timeline.users;
	}

	if ( input.timeline.instructions ) {
		for ( k in input.timeline.instructions ) {
			instructions.push( input.timeline.instructions[ k ] );
		}

		delete input.timeline.instructions;
	}

	if ( input.globalObjects.media ) {
		delete input.globalObjects.media;
	}

	if ( input.globalObjects.moments ) {
		delete input.globalObjects.moments;
	}

	if ( input.globalObjects.notifications ) {
		delete input.globalObjects.notifications;
	}

	if ( input.timeline.responseObjects ) {
		delete input.timeline.responseObjects;
	}

	valid = true;
};

com.tolstoy.basic.app.tweetparser.json.JSONParserIncompleteUser = function( input, $, tweetFactory, utils, logger ) {
	var valid = false, tweets = [], users = [], instructions = [];

	this.getName = function() {
		return 'IncompleteUser';
	};

	this.isValid = function() {
		return valid;
	};

	this.getRemaining = function() {
		return input;
	};

	this.getRawTweets = function() {
		return tweets;
	};

	this.getRawUsers = function() {
		return users;
	};

	this.getRawInstructions = function() {
		return instructions;
	};

	var user = input.data.user.result;
	utils.swapUserID( user );

	users.push( user );

	delete input.data.user.result;

	valid = true;
};

com.tolstoy.basic.app.tweetparser.json.JSONParserIncompleteUserList = function( input, $, tweetFactory, utils, logger ) {
	var valid = false, tweets = [], users = [], instructions = [];

	this.getName = function() {
		return 'IncompleteUserList';
	};

	this.isValid = function() {
		return valid;
	};

	this.getRemaining = function() {
		return input;
	};

	this.getRawTweets = function() {
		return tweets;
	};

	this.getRawUsers = function() {
		return users;
	};

	this.getRawInstructions = function() {
		return instructions;
	};

	for ( var k in input.data.users ) {
		var user = input.data.users[ k ].result ? input.data.users[ k ].result : null;
		if ( !user ) {
			continue;
		}

		utils.swapUserID( user );

		users.push( user );

		delete input.data.users[ k ].result;
	}

	valid = true;
};

com.tolstoy.basic.app.tweetparser.json.JSONParserUserList = function( input, $, tweetFactory, utils, logger ) {
	var valid = false, tweets = [], users = [], instructions = [];

	this.getName = function() {
		return 'UserList';
	};

	this.isValid = function() {
		return valid;
	};

	this.getRemaining = function() {
		return input;
	};

	this.getRawTweets = function() {
		return tweets;
	};

	this.getRawUsers = function() {
		return users;
	};

	this.getRawInstructions = function() {
		return instructions;
	};

	for ( var k in input ) {
		if ( input[ k ].user ) {
			users.push( input[ k ].user );
		}

		delete input[ k ].user;
	}

	valid = true;
};

com.tolstoy.basic.app.tweetparser.json.JSONParserThreadedConversation = function( input, $, tweetFactory, utils, logger ) {
	var valid = false, tweets = [], users = [], instructions = [];

	this.getName = function() {
		return 'ThreadedConversation';
	};

	this.isValid = function() {
		return valid;
	};

	this.getRemaining = function() {
		return input;
	};

	this.getRawTweets = function() {
		return tweets;
	};

	this.getRawUsers = function() {
		return users;
	};

	this.getRawInstructions = function() {
		return instructions;
	};

	var ary = utils.getNestedJSON( input, [ 'data', 'threaded_conversation_with_injections_v2', 'instructions' ] );
	if ( ary ) {
		for ( var k in ary ) {
			instructions.push( ary[ k ] );

			delete input.data.threaded_conversation_with_injections_v2.instructions[ k ];
		}
	}

	valid = true;
};

com.tolstoy.basic.app.tweetparser.json.JSONParserTimelineV2 = function( input, $, tweetFactory, utils, logger ) {
	var valid = false, tweets = [], users = [], instructions = [];

	this.getName = function() {
		return 'TimelineV2';
	};

	this.isValid = function() {
		return valid;
	};

	this.getRemaining = function() {
		return input;
	};

	this.getRawTweets = function() {
		return tweets;
	};

	this.getRawUsers = function() {
		return users;
	};

	this.getRawInstructions = function() {
		return instructions;
	};

	var base = input.data.user.result.timeline_v2.timeline;
	if ( base.instructions ) {
		for ( var k in base.instructions ) {
			instructions.push( base.instructions[ k ] );

			delete base.instructions[ k ];
		}
	}

	valid = true;
};

com.tolstoy.basic.app.tweetparser.json.JSONParserIgnored = function( input, $, tweetFactory, utils, logger ) {
	var valid = false, tweets = [], users = [], instructions = [];

	this.getName = function() {
		return 'Ignored';
	};

	this.isValid = function() {
		return false;
	};

	this.getRemaining = function() {
		return input;
	};

	this.getRawTweets = function() {
		return tweets;
	};

	this.getRawUsers = function() {
		return users;
	};

	this.getRawInstructions = function() {
		return instructions;
	};
};

com.tolstoy.basic.app.retriever.StateStatus = {
	READY: 'ready',
	RUNNING: 'running',
	STOPPED: 'stopped',
	FAILURE: 'failure',
	FINISHED: 'finished',
	CLICKEDBUTTON: 'clickedbutton',
	NOTFOUND: 'notfound'
};

com.tolstoy.basic.app.retriever.StateCheckLoggedIn = function( $, scroller, checkLoggedInDelay, utils, logger ) {
	checkLoggedInDelay = checkLoggedInDelay || 5;

	var stateStasuses = com.tolstoy.basic.app.retriever.StateStatus;
	var status = stateStasuses.READY;
	var error_code = '';
	var error_message = '';

	scroller.reset();

	this.getName = function() {
		return 'StateCheckLoggedIn';
	};

	this.getStatus = function() {
		return status;
	};

	this.getFailureInformation = function() {
		var ret = {};

		if ( status == stateStasuses.FAILURE ) {
			ret[ 'error_code' ] = error_code ? error_code : this.getName() + '_unknown';
			ret[ 'error_message' ] = error_message ? error_message : 'Unknown error in ' + this.getName();
		}

		return ret;
	};

	var attemptNumber = 0;

	this.run = function( iterationnumber ) {
		if ( status == stateStasuses.FINISHED || status == stateStasuses.FAILURE ) {
			return;
		}

		status = stateStasuses.RUNNING;

		if ( ++attemptNumber > checkLoggedInDelay ) {
			if ( $( '#signin-link' ).length ) {
				error_code = 'StateCheckLoggedIn_found_login_link';
				error_message = 'StateCheckLoggedIn: found login link';
				status = stateStasuses.FAILURE;
			}
			else {
				status = stateStasuses.FINISHED;
			}
		}
	};
};

com.tolstoy.basic.app.retriever.StateWaitForTweetSelector = function( $, tweetSelector, maxWaitIterations, utils, logger ) {
	var stateStasuses = com.tolstoy.basic.app.retriever.StateStatus;
	var status = stateStasuses.READY;
	var iterations = 0;
	var error_code = '';
	var error_message = '';

	this.getName = function() {
		return 'StateWaitForTweetSelector';
	};

	this.getStatus = function() {
		return status;
	};

	this.getFailureInformation = function() {
		var ret = {};

		if ( status == stateStasuses.FAILURE ) {
			ret[ 'error_code' ] = error_code ? error_code : this.getName() + '_unknown';
			ret[ 'error_message' ] = error_message ? error_message : 'Unknown error in ' + this.getName();
		}

		return ret;
	};

	this.run = function( iterationnumber ) {
		if ( status == stateStasuses.FINISHED || status == stateStasuses.NOTFOUND || status == stateStasuses.FAILURE ) {
			return;
		}

		status = stateStasuses.RUNNING;

		iterations++;

		if ( $( tweetSelector ).length ) {
			status = stateStasuses.FINISHED;
			return;
		}

		logger.info( 'StateWaitForTweetSelector iteration number=' + iterations );
		if ( iterations > maxWaitIterations ) {
			status = stateStasuses.NOTFOUND;
			return;
		}
	};
};

// @todo: convert this to an ES6 "class"
com.tolstoy.basic.app.retriever.StateFindUncensoredTweets = function( $, tweetSelector, parsedTweetFactory, tweetCollection, scroller, utils, logger ) {
	var moi = this;

	var stateStasuses = com.tolstoy.basic.app.retriever.StateStatus;
	var scrollerStasuses = com.tolstoy.basic.app.scroller.ScrollerStatus;
	var status = stateStasuses.READY;
	var error_code = '';
	var error_message = '';

	scroller.reset();

	this.getName = function() {
		return 'StateFindUncensoredTweets';
	};

	this.getSupposedQuality = function() {
		return com.tolstoy.basic.app.tweet.TweetSupposedQuality.HIGH;
	};

	this.getStatus = function() {
		return status;
	};

	this.getFailureInformation = function() {
		var ret = {};

		if ( status == stateStasuses.FAILURE ) {
			ret[ 'error_code' ] = error_code ? error_code : this.getName() + '_unknown';
			ret[ 'error_message' ] = error_message ? error_message : 'Unknown error in ' + this.getName();
		}

		return ret;
	};

	this.assignPreviousNext = function( tweets ) {
		var len = tweets.length;
		if ( !len ) {
			return tweets;
		}

		for ( var i = 0; i < len; i++ ) {
			var tweet = tweets[ i ];

			tweet.setAttribute( 'previoustweetid', 0 );
			tweet.setAttribute( 'nexttweetid', 0 );

			if ( i > 0 ) {
				tweet.setAttribute( 'previoustweetid', tweets[ i - 1 ].getAttribute( 'tweetid' ) );
			}

			if ( i + 1 < len ) {
				tweet.setAttribute( 'nexttweetid', tweets[ i + 1 ].getAttribute( 'tweetid' ) );
			}
		}

		return tweets;
	};

	this.run = function( iterationnumber ) {
		if ( status == stateStasuses.FINISHED || status == stateStasuses.FAILURE ) {
			return;
		}

		var tweets = [];

		status = stateStasuses.RUNNING;

		var count = 0;
		$( tweetSelector ).each( function( iterationindex ) {
			var tweet = parsedTweetFactory.makeTweet( $(this) );

			tweet.setAttribute( 'iterationnumber', iterationnumber );
			tweet.setAttribute( 'iterationindex', iterationindex );
			tweet.setAttribute( 'quality', moi.getSupposedQuality() );

			tweets.push( tweet );

			count++;
		});

		tweets = this.assignPreviousNext( tweets );

		tweetCollection.addTweets( tweets );

		//logger.info( 'StateFindUncensoredTweets: ' + count + ' tweet elements, afterwards collection=' + tweetCollection.toDebugString( '  ' ) );

		scroller.step();

		if ( scroller.getStatus() == scrollerStasuses.FINISHED || scroller.getStatus() == scrollerStasuses.EXCEEDEDLIMIT ) {
			status = stateStasuses.FINISHED;
			logger.info( 'StateFindUncensoredTweets set status to FINISHED because scroller status is ' + scroller.getStatus() );
		}
	};
};

com.tolstoy.basic.app.retriever.StateClickShowHiddenReplies = function( $, afterClickIterations, attemptIterations, scroller, utils, logger ) {
	var stateStasuses = com.tolstoy.basic.app.retriever.StateStatus;
	var status = stateStasuses.READY;
	var error_code = '';
	var error_message = '';

	scroller.reset();

	this.getName = function() {
		return 'StateClickShowHiddenReplies';
	};

	this.getStatus = function() {
		return status;
	};

	this.getFailureInformation = function() {
		var ret = {};

		if ( status == stateStasuses.FAILURE ) {
			ret[ 'error_code' ] = error_code ? error_code : this.getName() + '_unknown';
			ret[ 'error_message' ] = error_message ? error_message : 'Unknown error in ' + this.getName();
		}

		return ret;
	};

	var attemptNumber = 0;

	this.run = function( iterationnumber ) {
		if ( status == stateStasuses.FINISHED || status == stateStasuses.NOTFOUND || status == stateStasuses.FAILURE ) {
			return;
		}

		attemptNumber++;

		if ( status == stateStasuses.CLICKEDBUTTON ) {
			if ( attemptNumber > afterClickIterations ) {
				status = stateStasuses.FINISHED;
			}

			return;
		}

		var $button = null;

		$( 'section > div > div > div > div > div > div[role="button"] > div > div > span' ).each( function() {
			var $t = $(this);
			if ( $.trim( $t.text() ) ) {
				var $levelsUp = $t.parent().parent().parent();
				if ( $levelsUp.length ) {
					$button = $levelsUp;
				}
			}
		});

		if ( $button ) {
			logger.info( 'StateClickShowHiddenReplies: clicking ' + $button.html() );

			$button.click();

			status = stateStasuses.CLICKEDBUTTON;
			attemptNumber = 0;

			return;
		}

		if ( attemptNumber > attemptIterations ) {
			status = stateStasuses.NOTFOUND;
			return;
		}

		scroller.step();
	};
};

com.tolstoy.basic.app.retriever.StateFindCensoredTweets = function( $, tweetSelector, parsedTweetFactory, tweetCollection, scroller, utils, logger ) {
	var moi = this;

	var stateStasuses = com.tolstoy.basic.app.retriever.StateStatus;
	var scrollerStasuses = com.tolstoy.basic.app.scroller.ScrollerStatus;
	var status = stateStasuses.READY;
	var error_code = '';
	var error_message = '';

	scroller.reset();

	this.getName = function() {
		return 'StateFindCensoredTweets';
	};

	this.getSupposedQuality = function() {
		return com.tolstoy.basic.app.tweet.TweetSupposedQuality.LOW;
	};

	this.getStatus = function() {
		return status;
	};

	this.getFailureInformation = function() {
		var ret = {};

		if ( status == stateStasuses.FAILURE ) {
			ret[ 'error_code' ] = error_code ? error_code : this.getName() + '_unknown';
			ret[ 'error_message' ] = error_message ? error_message : 'Unknown error in ' + this.getName();
		}

		return ret;
	};

	this.assignPreviousNext = function( tweets ) {
		tweets = tweets || [];
		var len = tweets.length;
		if ( !len ) {
			return tweets;
		}

		for ( var i = 0; i < len; i++ ) {
			var tweet = tweets[ i ];

			tweet.setAttribute( 'previoustweetid', 0 );
			tweet.setAttribute( 'nexttweetid', 0 );

			if ( i > 0 ) {
				tweet.setAttribute( 'previoustweetid', tweets[ i - 1 ].getAttribute( 'tweetid' ) );
			}

			if ( i + 1 < len ) {
				tweet.setAttribute( 'nexttweetid', tweets[ i + 1 ].getAttribute( 'tweetid' ) );
			}
		}

		return tweets;
	};

	this.run = function( iterationnumber ) {
		if ( status == stateStasuses.FINISHED || status == stateStasuses.FAILURE ) {
			return;
		}

		var tweets = [];

		status = stateStasuses.RUNNING;

		logger.info( 'StateFindCensoredTweets: number of censored tweets=' + $( tweetSelector ).length );
		$( tweetSelector ).each( function( iterationindex ) {
			var tweet = parsedTweetFactory.makeTweet( $(this) );

			tweet.setAttribute( 'iterationnumber', iterationnumber );
			tweet.setAttribute( 'iterationindex', iterationindex );
			tweet.setAttribute( 'quality', moi.getSupposedQuality() );

			tweets.push( tweet );
		});

		tweets = this.assignPreviousNext( tweets );

		tweetCollection.addTweets( tweets );

		scroller.step();

		if ( scroller.getStatus() == scrollerStasuses.FINISHED || scroller.getStatus() == scrollerStasuses.EXCEEDEDLIMIT ) {
			status = stateStasuses.FINISHED;
		}
	};
};

com.tolstoy.basic.app.retriever.StateClickShowHiddenReplies2 = function( $, afterClickIterations, attemptIterations, scroller, utils, logger ) {
	var stateStasuses = com.tolstoy.basic.app.retriever.StateStatus;
	var status = stateStasuses.READY;
	var error_code = '';
	var error_message = '';

	scroller.reset();

	this.getName = function() {
		return 'StateClickShowHiddenReplies2';
	};

	this.getStatus = function() {
		return status;
	};

	this.getFailureInformation = function() {
		var ret = {};

		if ( status == stateStasuses.FAILURE ) {
			ret[ 'error_code' ] = error_code ? error_code : this.getName() + '_unknown';
			ret[ 'error_message' ] = error_message ? error_message : 'Unknown error in ' + this.getName();
		}

		return ret;
	};

	var attemptNumber = 0;

	this.run = function( iterationnumber ) {
		if ( status == stateStasuses.FINISHED || status == stateStasuses.NOTFOUND || status == stateStasuses.FAILURE ) {
			return;
		}

		attemptNumber++;

		if ( status == stateStasuses.CLICKEDBUTTON ) {
			if ( attemptNumber > afterClickIterations ) {
				status = stateStasuses.FINISHED;
			}

			return;
		}

		var $button = null;

		var selectors = [
			'article > div > div > div > div > div > div > div > div > div[role="button"]',
			'article > div > div > div > div > div > div > div > div[role="button"]',
			'article > div > div > div > div > div > div > div[role="button"]',
		];

		for ( k in selectors ) {
			var selector = selectors[ k ];

			$( selector ).each( function() {
			var $t = $(this);
			var $gpar = $t.parent().parent();

			var $message = $gpar.find( 'div:not([role]) > div > span' );
			var $btn = $gpar.find( 'div[role="button"] > div > span' );

			var message_num_kids = $message.find('*').length;
			var message_text = $message.text().trim();

			var btn_num_kids = $btn.find('span').length;
			var btn_text = $btn.text().trim();

			if ( !message_num_kids && btn_num_kids == 1 && message_text && btn_text && message_text.length > btn_text.length ) {
				$button = $t;
					return false;
			}
		});

			if ( $button ) {
				break;
			}
		}

		if ( $button ) {
			logger.info( 'StateClickShowHiddenReplies2: clicking ' + $button.html() );

			$button.click();

			status = stateStasuses.CLICKEDBUTTON;
			attemptNumber = 0;

			return;
		}

		if ( attemptNumber > attemptIterations ) {
			status = stateStasuses.NOTFOUND;
			return;
		}

		scroller.step();
	};
};

com.tolstoy.basic.app.retriever.StateFindCensoredTweets2 = function( $, tweetSelector, parsedTweetFactory, tweetCollection, scroller, utils, logger ) {
	var moi = this;

	var stateStasuses = com.tolstoy.basic.app.retriever.StateStatus;
	var scrollerStasuses = com.tolstoy.basic.app.scroller.ScrollerStatus;
	var status = stateStasuses.READY;
	var error_code = '';
	var error_message = '';

	scroller.reset();

	this.getName = function() {
		return 'StateFindCensoredTweets2';
	};

	this.getSupposedQuality = function() {
		return com.tolstoy.basic.app.tweet.TweetSupposedQuality.ABUSIVE;
	};

	this.getStatus = function() {
		return status;
	};

	this.getFailureInformation = function() {
		var ret = {};

		if ( status == stateStasuses.FAILURE ) {
			ret[ 'error_code' ] = error_code ? error_code : this.getName() + '_unknown';
			ret[ 'error_message' ] = error_message ? error_message : 'Unknown error in ' + this.getName();
		}

		return ret;
	};

	this.assignPreviousNext = function( tweets ) {
		tweets = tweets || [];
		var len = tweets.length;
		if ( !len ) {
			return tweets;
		}

		for ( var i = 0; i < len; i++ ) {
			var tweet = tweets[ i ];

			tweet.setAttribute( 'previoustweetid', 0 );
			tweet.setAttribute( 'nexttweetid', 0 );

			if ( i > 0 ) {
				tweet.setAttribute( 'previoustweetid', tweets[ i - 1 ].getAttribute( 'tweetid' ) );
			}

			if ( i + 1 < len ) {
				tweet.setAttribute( 'nexttweetid', tweets[ i + 1 ].getAttribute( 'tweetid' ) );
			}
		}

		return tweets;
	};

	this.run = function( iterationnumber ) {
		if ( status == stateStasuses.FINISHED || status == stateStasuses.FAILURE ) {
			return;
		}

		var tweets = [];

		status = stateStasuses.RUNNING;

		logger.info( 'StateFindCensoredTweets2: number of censored2 tweets=' + $( tweetSelector ).length );
		$( tweetSelector ).each( function( iterationindex ) {
			var tweet = parsedTweetFactory.makeTweet( $(this) );

			tweet.setAttribute( 'iterationnumber', iterationnumber );
			tweet.setAttribute( 'iterationindex', iterationindex );
			tweet.setAttribute( 'quality', moi.getSupposedQuality() );

			tweets.push( tweet );
		});

		tweets = this.assignPreviousNext( tweets );

		tweetCollection.addTweets( tweets );

		scroller.step();

		if ( scroller.getStatus() == scrollerStasuses.FINISHED || scroller.getStatus() == scrollerStasuses.EXCEEDEDLIMIT ) {
			status = stateStasuses.FINISHED;
		}
	};
};

com.tolstoy.basic.app.retriever.TimelineRunner = function( $, jsParams, tweetCollection, parsedTweetFactory, scroller, finishedCallback, utils, logger ) {
	var moi = this;

	var stateObj = new com.tolstoy.basic.app.retriever.StateCheckLoggedIn( $, scroller, jsParams.checkLoggedInDelay, utils, logger );
	var scrollerStasuses = com.tolstoy.basic.app.scroller.ScrollerStatus;
	var iterationnumber = 0;
	var timer;

	var metadata = {
		url: jsParams.url,
		request_date: ( new Date() ).toUTCString(),
		last_compound: '',
		tweet_selector: '',
		show_hidden_replies: '',
		show_hidden_replies2: '',
		completed: false,
		error_code: '',
		error_message: ''
	};

	this.finished = function() {
		window.clearInterval( timer );
		timer = null;

		metadata.last_compound = stateObj ? stateObj.getName() + '.' + stateObj.getStatus() : 'stateObj was null';

		stateObj = null;

		finishedCallback( metadata );
	};

	this.iteration = function() {
		if ( !stateObj ) {
			metadata.error_code = 'TimelineRunner_called_after_being_finished';
			metadata.error_message = 'TimelineRunner: called after being finished';
			moi.finished();
			return;
		}

		if ( ++iterationnumber > 1000 ) {
			metadata.error_code = 'TimelineRunner_too_many_iterations';
			metadata.error_message = 'TimelineRunner: too many iterations';
			metadata.completed = false;
			moi.finished();
			return;
		}

		var compound = stateObj.getName() + '.' + stateObj.getStatus();

		logger.info( 'TimelineRunner: compound=' + compound );

		switch ( compound ) {
			case 'StateCheckLoggedIn.ready':
			case 'StateWaitForTweetSelector.ready':
			case 'StateFindUncensoredTweets.ready':
				stateObj.run( iterationnumber );
				break;

			case 'StateCheckLoggedIn.running':
			case 'StateWaitForTweetSelector.running':
			case 'StateFindUncensoredTweets.running':
				stateObj.run( iterationnumber );
				break;

			case 'StateCheckLoggedIn.failure':
			case 'StateWaitForTweetSelector.failure':
			case 'StateFindUncensoredTweets.failure':
				metadata.completed = false;
				$.extend( metadata, stateObj.getFailureInformation() );
				moi.finished();
				break;

			case 'StateCheckLoggedIn.finished':
				stateObj = new com.tolstoy.basic.app.retriever.StateWaitForTweetSelector( $, jsParams.tweetSelector, jsParams.maxWaitForTweetSelector, utils, logger );
				break;

			case 'StateWaitForTweetSelector.finished':
				stateObj = new com.tolstoy.basic.app.retriever.StateFindUncensoredTweets( $, jsParams.tweetSelector, parsedTweetFactory, tweetCollection, scroller, utils, logger );
				break;

			case 'StateFindUncensoredTweets.finished':
				metadata.completed = scroller.getStatus() == scrollerStasuses.FINISHED;
				moi.finished();
				break;

			case 'StateWaitForTweetSelector.notfound':
				metadata.completed = false;
				metadata.tweet_selector = 'not found';
				moi.finished();
				break;

			default:
				metadata.error_code = 'Runner_bad_compound';
				metadata.error_message = 'Runner: bad compound: ' + compound;
				moi.finished();
		}
	};

	this.start = function() {
		metadata.completed = true;
		timer = window.setInterval( this.iteration, jsParams.mainClockDelay );
	};
};

com.tolstoy.basic.app.retriever.ReplyPageRunner = function( $, jsParams, tweetCollection, parsedTweetFactory, scroller, finishedCallback, utils, logger ) {
	var moi = this;

	var stateObj = new com.tolstoy.basic.app.retriever.StateCheckLoggedIn( $, scroller, jsParams.checkLoggedInDelay, utils, logger );
	var scrollerStasuses = com.tolstoy.basic.app.scroller.ScrollerStatus;
	var iterationnumber = 0;
	var timer;

	var metadata = {
		url: jsParams.url,
		request_date: ( new Date() ).toUTCString(),
		last_compound: '',
		tweet_selector: '',
		show_hidden_replies: '',
		show_hidden_replies2: '',
		completed: false,
		error_code: '',
		error_message: ''
	};

	this.finished = function() {
		window.clearInterval( timer );
		timer = null;

		metadata.last_compound = stateObj ? stateObj.getName() + '.' + stateObj.getStatus() : 'stateObj was null';

		stateObj = null;

		finishedCallback( metadata );
	};

	this.iteration = function() {
		if ( !stateObj ) {
			metadata.error_code = 'ReplyPageRunner_called_after_being_finished';
			metadata.error_message = 'ReplyPageRunner: called after being finished';
			moi.finished();
			return;
		}

		if ( ++iterationnumber > 1000 ) {
			metadata.error_code = 'ReplyPageRunner_too_many_iterations';
			metadata.error_message = 'ReplyPageRunner: too many iterations';
			metadata.completed = false;
			moi.finished();
			return;
		}

		var compound = stateObj.getName() + '.' + stateObj.getStatus();

		logger.info( 'ReplyPageRunner: compound=' + compound );

		switch ( compound ) {
			case 'StateCheckLoggedIn.ready':
			case 'StateWaitForTweetSelector.ready':
			case 'StateFindUncensoredTweets.ready':
			case 'StateClickShowHiddenReplies.ready':
			case 'StateFindCensoredTweets.ready':
			case 'StateClickShowHiddenReplies2.ready':
			case 'StateFindCensoredTweets2.ready':
				stateObj.run( iterationnumber );
				break;

			case 'StateCheckLoggedIn.running':
			case 'StateWaitForTweetSelector.running':
			case 'StateFindUncensoredTweets.running':
			case 'StateClickShowHiddenReplies.running':
			case 'StateFindCensoredTweets.running':
			case 'StateClickShowHiddenReplies2.running':
			case 'StateFindCensoredTweets2.running':
			case 'StateClickShowHiddenReplies.clickedbutton':
			case 'StateClickShowHiddenReplies2.clickedbutton':
				stateObj.run( iterationnumber );
				break;

			case 'StateCheckLoggedIn.failure':
			case 'StateWaitForTweetSelector.failure':
			case 'StateFindUncensoredTweets.failure':
			case 'StateClickShowHiddenReplies.failure':
			case 'StateFindCensoredTweets.failure':
			case 'StateClickShowHiddenReplies2.failure':
			case 'StateFindCensoredTweets2.failure':
				metadata.completed = false;
				$.extend( metadata, stateObj.getFailureInformation() );
				moi.finished();
				break;

			case 'StateCheckLoggedIn.finished':
				stateObj = new com.tolstoy.basic.app.retriever.StateWaitForTweetSelector( $, jsParams.tweetSelector, jsParams.maxWaitForTweetSelector, utils, logger );
				break;

			case 'StateWaitForTweetSelector.finished':
				stateObj = new com.tolstoy.basic.app.retriever.StateFindUncensoredTweets( $, jsParams.tweetSelector, parsedTweetFactory, tweetCollection, scroller, utils, logger );
				break;

			case 'StateFindUncensoredTweets.finished':
				metadata.completed = scroller.getStatus() == scrollerStasuses.FINISHED;
				stateObj = new com.tolstoy.basic.app.retriever.StateClickShowHiddenReplies( $, jsParams.hiddenRepliesAfterClickIterations, jsParams.hiddenRepliesAttemptIterations, scroller, utils, logger );
				break;

			case 'StateClickShowHiddenReplies.finished':
				metadata.completed = true;
				stateObj = new com.tolstoy.basic.app.retriever.StateFindCensoredTweets( $, jsParams.tweetSelector, parsedTweetFactory, tweetCollection, scroller, utils, logger );
				break;

			case 'StateClickShowHiddenReplies.notfound':
				stateObj = new com.tolstoy.basic.app.retriever.StateClickShowHiddenReplies2( $, jsParams.hiddenRepliesAfterClickIterations, jsParams.hiddenRepliesAttemptIterations, scroller, utils, logger );
				break;

			case 'StateFindCensoredTweets.finished':
				metadata.completed = scroller.getStatus() == scrollerStasuses.FINISHED;
				stateObj = new com.tolstoy.basic.app.retriever.StateClickShowHiddenReplies2( $, jsParams.hiddenRepliesAfterClickIterations, jsParams.hiddenRepliesAttemptIterations, scroller, utils, logger );
				break;

			case 'StateClickShowHiddenReplies2.finished':
				metadata.completed = true;
				stateObj = new com.tolstoy.basic.app.retriever.StateFindCensoredTweets2( $, jsParams.tweetSelector, parsedTweetFactory, tweetCollection, scroller, utils, logger );
				break;

			case 'StateFindCensoredTweets2.finished':
				metadata.completed = scroller.getStatus() == scrollerStasuses.FINISHED;
				moi.finished();
				break;

			case 'StateWaitForTweetSelector.notfound':
				metadata.tweet_selector = 'not found';
				moi.finished();
				break;

			case 'StateClickShowHiddenReplies2.notfound':
				metadata.show_hidden_replies2 = 'not found';
				moi.finished();
				break;

			default:
				metadata.error_code = 'Runner_bad_compound';
				metadata.error_message = 'Runner: bad compound: ' + compound;
				moi.finished();
		}
	};

	this.start = function() {
		metadata.completed = true;
		timer = window.setInterval( this.iteration, jsParams.mainClockDelay );
	};
};

com.tolstoy.basic.app.retriever.Starter = function( jsParams, dataCallback ) {
	if ( !window.jQuery ) {
		var elem = document.createElement( 'script' );
		document.head.append( elem );
		elem.type = 'text/javascript';
		elem.src = 'https://code.jquery.com/jquery-1.12.0.js';
	}

	var waitForJQueryCount = 0;

	var waitForJQueryTimer = window.setInterval( function() {
		if ( ++waitForJQueryCount > 500 ) {
			window.clearInterval( waitForJQueryTimer );

			var jqueryNotLoading = {
				map_type: 'metadata',
				error_code: 'Starter_cannot_load_jquery',
				error_message: 'Starter: cannot load jquery'
			};

			dataCallback( [ jqueryNotLoading ] );
		}

		if ( window.jQuery ) {
			window.clearInterval( waitForJQueryTimer );

			var $ = jQuery.noConflict( true );

			var debugLevel = new com.tolstoy.basic.app.utils.DebugLevel( jsParams.debugLevel );
			var logger = new com.tolstoy.basic.app.utils.ConsoleLogger( $, debugLevel );
			var utils = new com.tolstoy.basic.app.utils.Utils( $ );
			var tweetFactory = new com.tolstoy.basic.app.tweet.TweetFactory( $, utils, logger );
			var tweetCollection = tweetFactory.makeTweetCollection( [] );
			var scrollerFactory = new com.tolstoy.basic.app.scroller.ScrollerFactory( $, utils, logger );
			var parsedTweetFactory = new com.tolstoy.basic.app.tweetparser.html.ParsedTweetFactory( $, tweetFactory, utils, logger );

			function finishedCallback( data ) {
				var ary = tweetCollection.exportAll();
				$.each( ary, function( index, map ) {
					ary[ index ] = utils.ensureMapIsStringString( map );
					ary[ index ][ 'map_type' ] = 'tweet';
				});

				data = utils.ensureMapIsStringString( data );
				data[ 'map_type' ] = 'metadata';
				ary.push( data );

				dataCallback( ary );
			}

			var scroller = scrollerFactory.makeStepScroller( jsParams.pageType, jsParams.url, jsParams.scrollerHeightMultiplier, jsParams.scrollerNumTimesToScroll );

			$(document).ready( function() {
				var runner;

				if ( jsParams.pageType == 'replypage' ) {
					runner = new com.tolstoy.basic.app.retriever.ReplyPageRunner( $, jsParams, tweetCollection, parsedTweetFactory, scroller, finishedCallback, utils, logger );
				}
				else {
					runner = new com.tolstoy.basic.app.retriever.TimelineRunner( $, jsParams, tweetCollection, parsedTweetFactory, scroller, finishedCallback, utils, logger );
				}

				runner.start();
			});
		}
	}, 100 );
};

com.tolstoy.basic.app.jsonparser.InterchangeHelper = function( input, utils, logger ) {
	var results = [];

	function addSupposedQualities() {
		var tweetidToSupposedQualities = {
			map_type: 'tweetid_to_supposed_qualities'
		};

		if ( input.instructions ) {
			for ( var i = 0; i < input.instructions.length; i++ ) {
				var instruction = input.instructions[ i ];

				if ( instruction.type == 'AddEntries' && instruction.timelineItems ) {
					for ( var j = 0; j < instruction.timelineItems.length; j++ ) {
						var timelineItem = instruction.timelineItems[ j ];

						if ( timelineItem.tweetID && timelineItem.conversationSection ) {
							tweetidToSupposedQualities[ '' + timelineItem.tweetID ] = '' + timelineItem.conversationSection;
						}
					}
				}
			}
		}

		results.push( tweetidToSupposedQualities );
	}

	function addTweets() {
		if ( input.tweets ) {
			for ( var i = 0; i < input.tweets.length; i++ ) {
				var map = input.tweets[ i ].export();
				logger.info( 'InterchangeHelper: exporting tweet ' + input.tweets[ i ].toDebugString( '' ) );
				map = utils.ensureMapIsStringString( map );
				map[ 'map_type' ] = 'tweet';
				results.push( map );
			}
		}
	}

	function addUsers() {
		if ( input.users ) {
			for ( var i = 0; i < input.users.length; i++ ) {
				var map = input.users[ i ].export();
				logger.info( 'InterchangeHelper: exporting user ' + input.users[ i ].toDebugString( '' ) );
				map = utils.ensureMapIsStringString( map );
				map[ 'map_type' ] = 'user';
				results.push( map );
			}
		}
	}

	addSupposedQualities();
	addTweets();
	addUsers();

	this.getResults = function() {
		return results;
	};
};

com.tolstoy.basic.app.jsonparser.Starter = function( jsParams, jsonStrings, dataCallback ) {
	if ( !window.jQuery ) {
		var elem = document.createElement( 'script' );
		document.head.append( elem );
		elem.type = 'text/javascript';
		elem.src = 'https://code.jquery.com/jquery-1.12.0.js';
	}

	var waitForJQueryCount = 0;
	var i = 0;

	var waitForJQueryTimer = window.setInterval( function() {
		if ( ++waitForJQueryCount > 500 ) {
			window.clearInterval( waitForJQueryTimer );

			var jqueryNotLoading = {
				map_type: 'metadata',
				error_code: 'Starter_cannot_load_jquery',
				error_message: 'Starter: cannot load jquery'
			};

			dataCallback( [ jqueryNotLoading ] );
		}

		if ( window.jQuery ) {
			window.clearInterval( waitForJQueryTimer );

			var $ = jQuery.noConflict( true );

			var debugLevel = new com.tolstoy.basic.app.utils.DebugLevel( jsParams.debugLevel );
			var logger = new com.tolstoy.basic.app.utils.ConsoleLogger( $, debugLevel );
			var utils = new com.tolstoy.basic.app.utils.Utils( $ );
			var tweetFactory = new com.tolstoy.basic.app.tweet.TweetFactory( $, utils, logger );
			var parsedJSONFactory = new com.tolstoy.basic.app.tweetparser.json.ParsedJSONFactory( $, tweetFactory, utils, logger );

			var combined = {
				tweets: [],
				users: [],
				instructions: [],
				errors: []
			};

			jsonStrings = jsonStrings || [];
			for ( i = 0; i < jsonStrings.length; i++ ) {
				try {
					var json = JSON.parse( jsonStrings[ i ] );

					if ( !json ) {
						combined.errors.push( 'Cannot parse string at position ' + i );
						continue;
					}

					var parsed = parsedJSONFactory.parseJSON( json );

					combined.tweets = combined.tweets.concat( parsed.tweets );
					combined.users = combined.users.concat( parsed.users );
					combined.instructions = combined.instructions.concat( parsed.instructions );
					combined.errors = combined.errors.concat( parsed.errors );
				}
				catch ( error ) {
					combined.errors.push( 'Cannot parse string at position ' + i + ( error && error.message ? error.message : 'unknown exception' ) );
				}
			}

			if ( debugLevel.isVerbose() ) {
				if ( combined.tweets ) {
					logger.info( 'jsonparser.Starter tweets:' );
					for ( i = 0; i < combined.tweets.length; i++ ) {
						logger.info( '  ' + combined.tweets[ i ].toDebugString( '  ' ) );
					}
				}
				else {
					logger.info( 'jsonparser.Starter NO TWEETS' );
				}

				if ( combined.users ) {
					logger.info( 'jsonparser.Starter users:' );
					for ( i = 0; i < combined.users.length; i++ ) {
						logger.info( '  ' + combined.users[ i ].toDebugString( '  ' ) );
					}
				}
				else {
					logger.info( 'jsonparser.Starter NO USERS' );
				}

				if ( combined.instructions ) {
					logger.info( 'jsonparser.Starter instructions:' );
					for ( i = 0; i < combined.instructions.length; i++ ) {
						logger.info( '  ', combined.instructions[ i ] );
					}
				}
				else {
					logger.info( 'jsonparser.Starter NO INSTRUCTIONS' );
				}
			}

			if ( combined.errors ) {
				logger.info( 'jsonparser.Starter errors:' );
				for ( i = 0; i < combined.errors.length; i++ ) {
					logger.info( '  ', combined.errors[ i ] );
				}
			}
			else {
				logger.info( 'jsonparser.Starter NO ERRORS' );
			}

			var interchangeHelper = new com.tolstoy.basic.app.jsonparser.InterchangeHelper( combined, utils, logger );
			var results = interchangeHelper.getResults();

			if ( debugLevel.isVerbose() ) {
				logger.info( 'jsonparser.Starter BEGIN RESULTS' );
				results = results || [];
				for ( i = 0; i < results.length; i++ ) {
					logger.info( '  record:\n' + utils.prettyPrint( results[ i ], '    ' ) );
				}
				logger.info( 'jsonparser.Starter END RESULTS' );
			}

			dataCallback( results );
		}
	}, 100 );
};

com.tolstoy.basic.app.harretriever.Starter = function( jsParams, dataCallback ) {
	if ( typeof HAR === 'undefined' ) {
		dataCallback({ '_error': 'HAR IS UNDEFINED' });
		return;
	}

	if ( typeof HAR.triggerExport !== 'function' ) {
		dataCallback({ '_error': 'HAR METHOD IS UNDEFINED' });
		return;
	}

	HAR.triggerExport().then( function( results ) {
		if ( !results || !results.version ) {
			dataCallback({ '_error': 'BAD RESULTS' });
			return;
		}

		var ret = {
			'version': results.version,
			'creator': {},
			'browser': {},
			'pages': results.pages,
			'entries': [],
			'comment': results.comment,
			'_error': ''
		};

		for ( var k in results.entries ) {
			var entry = results.entries[ k ];
			if ( !entry.request.url || !entry.response.content.text || entry.response.content.text.length < 1 ) {
				continue;
			}

			var content = entry.response.content.text.trim();

			if ( content.startsWith( '{' ) || content.startsWith( '[' ) || entry.response.content.mimeType == 'application/json' ) {
				ret.entries.push( entry );
			}
		}

		dataCallback( ret );

		return;
	});
};

