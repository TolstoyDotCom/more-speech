/*
 * Copyright 2022 Chris Kelly
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
(function(XHR) {
	"use strict";

	class SessionArrayStorage {
		static append( key, item ) {
			var ary = this.get( key );

			ary.push( item );

			this.set( key, ary );
		}

		static get( key ) {
			var s = sessionStorage.getItem( key );
			if ( s ) {
				return JSON.parse( s );
			}

			var data = [];

			this.set( key, data );

			return data;
		}

		static getJSON( key ) {
			var s = sessionStorage.getItem( key );
			if ( s ) {
				return s;
			}

			return JSON.stringify( [] );
		}

		static set( key, data ) {
			if ( !data || !Array.isArray( data ) ) {
				throw new Error( 'bad data' );
			}

			var json = JSON.stringify( data );

			sessionStorage.setItem( key, json );
		}
	}

	function ajaxSpy() {
		var existingSend = XMLHttpRequest.prototype.send;
		XMLHttpRequest.prototype.send = function() {
			this.addEventListener( 'readystatechange', function() {
				if ( this.readyState === 4 &&
						( this.responseType == '' || this.responseType == 'text' || this.responseType == 'json' ) &&
						this.response ) {
					SessionArrayStorage.append( 'JSONStrings', btoa( this.response ) );
				}
			}, false);

			existingSend.apply( this, arguments );
		};
	};

	//ajaxSpy();

	var element = document.createElement('div');
	element.id = "interceptedResponse";
	element.appendChild(document.createTextNode(""));
	document.body.appendChild(element);

	var open = XHR.prototype.open;
	var send = XHR.prototype.send;

	XHR.prototype.open = function( method, url, asyncFlag ) {
		if ( arguments.length < 3 ) {
			asyncFlag = true;
		}

		open.call( this, method, url, asyncFlag );
	};

	XHR.prototype.send = function(data) {
		var self = this;
		var oldOnReadyStateChange;

		function onReadyStateChange() {
			if ( self.status == 200 && self.readyState == 4 /* complete */) {		//
				if ( ( self.responseType === 'text' || self.responseType === '' ) && self.responseText ) {
					SessionArrayStorage.append( 'JSONStrings', btoa( self.responseText ) );
				}
			}

			if ( oldOnReadyStateChange ) {
				oldOnReadyStateChange();
			}
		}

		if ( this.addEventListener ) {
			this.addEventListener( "readystatechange", onReadyStateChange, false );
		}
		else {
			oldOnReadyStateChange = this.onreadystatechange;
			this.onreadystatechange = onReadyStateChange;
		}

		send.call(this, data);
	}
})(XMLHttpRequest);
