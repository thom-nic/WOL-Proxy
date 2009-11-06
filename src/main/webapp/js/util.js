/**
 * Utility methods not found in Prototype.js
 * Author: Tom Nichols (tmnichols@gmail.com)
 */

var Util = {
	/** Node builder convenience method.  Handy for markup creation from JSON */
	createNode : function( name, attrs, contents ) {
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
	},
	/** Fire an event on a DOM element.
	 *  from http://jehiah.cz/archive/firing-javascript-events-properly
	 */
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
};

// Shortcuts to common methods:
var $N = Util.createNode;
var $T = document.createTextNode;

// Prototype.js mechanism to add a couple extra methods to the DOM Element class
Element.addMethods({   
	lastChildElement : function( elem ) {
		var d = $(elem).childElements();
		return d[d.length-1];
	},
	getText : function( elem ) { /* textContent alternative for IE compat. */
		return elem.textContent || elem.innerText || elem.innerHTML;
	}
});

// Dummy object & methods for platforms w/o a debug console.
if ( typeof( console ) == "undefined" ) {
	console = {
		log : function() {},
		debug : function() {},
		info : function() {},
		warn : function() {},
		error : function() {}
	}
}
// For IE8 and earlier Webkit builds:
if ( ! console.debug ) console.debug = console.log;