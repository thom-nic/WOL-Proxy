/* Simple node builder convenience method for Prototype.js.
 * Author: Tom Nichols (tmnichols@gmail.com) */
var $N = function( name, attrs, contents ) {
	var elem = Element.extend( document.createElement(name) );
	if ( attrs ) {
		$H(attrs).each( function(a) {
			elem.setAttribute( a.key, a.value );
		} );
	}

	if ( contents ) {
		if ( typeof contents == 'function' ) contents = contents();
		if ( contents instanceof Array ) $A(contents).each( function(i) {
			elem.insert( {bottom:i} ); // add all contents items in array
		} );
		else elem.insert( {bottom:contents} ); // works for a string, single node, etc.
	}
	return elem;
};

var $T = function(t) { return document.createTextNode(t); };

var Util = {
		
	/* from http://jehiah.cz/archive/firing-javascript-events-properly */
	fireEvent : function( element, event ) {
		if ( document.createEvent ) { // dispatch for firefox + others
			var evt = document.createEvent( 'HTMLEvents' );
			evt.initEvent( event, true, true ); // event type,bubbling,cancelable
			return ! element.dispatchEvent( evt );
		}
		else { // dispatch for IE
			var evt = document.createEventObject();
			return element.fireEvent('on' + event , evt );
		}
	}
}

if ( typeof( console ) == "undefined" ) {
	console = {
		log : function() {},
		debug : function() {},
		info : function() {},
		warn : function() {},
		error : function() {}
	}
}
if ( ! console.debug ) console.debug = console.log;