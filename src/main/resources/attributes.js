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
var ret = {};
var attrs = tweetElem.attributes;

if ( !attrs || !attrs.length ) {
	return ret;
}

for ( var i = 0; i < attrs.length; i++ ) {
	var key = attrs[ i ].name;
	if ( key.indexOf( 'data-' ) === 0 ) {
		key = key.substring( 5 );
	}
	key = key.replace( /-/g, '' );
	if ( key == 'conversationsectionquality' ) {
		key = 'quality';
	}
	ret[ key ] = attrs[ i ].value;
}

return ret;
