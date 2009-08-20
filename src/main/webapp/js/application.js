
app = {
	userID : 'tnichols',//null,
	
	getHostList : function() {
		WOL.getHosts( {
			callback: function( hosts ) {
			console.log( "Hosts", hosts );
				if ( app.status.visible() ) app.status.hide();
				
				var hostList = $('hostList').update(); 
				$A(hosts).each( function( host ) {
					var li = $N('li', {}, host.alias );
					li.observe( 'click', app.getHostDetail.curry(host.hostID) );
					hostList.insert( {bottom:li} );
				});
				if ( hosts.length < 1 ) app.editHost(null);
			}
		} );
	}, 
	
	getHostDetail : function( hostID, evt ) {
		console.log( "Retrieving host ID ", hostID );
		WOL.getHost( hostID, { callback: app.displayHost });
		evt.stop();
	},
	
	displayHost : function( host ) {
		console.log( "Show Host", host );
		
		$('hostLabel').update( host.alias );
		
		var table = $('hostInfoTable');
		table.down('.alias .label').update( host.alias );
		table.down('.hostName .label').update( host.hostName );
		table.down('.ip .label').update( host.ip );
		table.down('.macAddress .label').update( host.macAddress );
		table.down('.broadcastAddr .label').update( host.broadcastAddr );
		table.down('.port .label').update( host.wolPort );

		table.select('.label').invoke('show');
		$('hostForm').select('input').invoke('hide');
		$('saveCancel').hide();
		$('editLink').stopObserving('click')
			.observe( 'click', app.editHost.curry(host) ).show();
		$('wakeBtn').stopObserving('click')
			.observe( 'click', app.wakeHost.curry(host) ).show();
	},
	
	editHost : function( host, evt ) {
		var form = $('hostForm');
		console.log( 'Edit Host', host );
		if ( ! host ) {
			$('hostLabel').update( "Add new host..." );
			form.reset();
			$('saveBtn').stopObserving().observe('click', app.saveHost.curry(null) );
			$('delete').hide();
		}
		else { //display given host info:
			var table = $('hostInfoTable');
			table.down('.alias input').value = host.alias;
			table.down('.hostName input').value = host.hostName;
			table.down('.ip input').value = host.ip;
			table.down('.macAddress input').value = host.macAddress;
			table.down('.broadcastAddr input').value = host.broadcastAddr;
			table.down('.port input').value = host.wolPort;
			$('saveBtn').stopObserving().observe('click', app.saveHost.curry(host.hostID) );
			if ( host.id ) $('delete').stopObserving().observe('click', 
					app.deleteHost.curry(host.hostID) ).show();
		}
		
		$('hostInfoTable').select('.label').invoke('hide');
		form.select('input').invoke('show');
		$('cancel').stopObserving().observe( 'click', 
					app.displayHost.curry(host) );
		$('saveCancel').show();
		$('editLink').hide();
		$('wakeBtn').hide();
		if ( evt ) evt.stop();
	},
	
	saveHost : function( hostID, evt ) {
		var host = $('hostForm').serialize(true);
		host.hostID = hostID;
		evt.stop();
		console.log('Saving host', host);
		if ( ! app.validate( host, ['hostName','macAddress'] ) ) return;
		WOL.updateHost( host, {
			callback : function( host ) {
				console.log( "Got host ID: ", host.hostID );
				app.getHostList();
				app.displayHost( host );
				$('saveCancel').hide();
			}
		});
	},
	
	deleteHost : function( host, evt ) {
		WOL.deleteHost( host.hostID, {
			callback : function() {
				app.status.removeClassName('error').update('Host <strong>' 
						+ host.hostName + "</strong> was deleted." ).show();
			}
		});
		evt.stop();
	},
	
	wakeHost : function( host, evt ) {
		WOL.wakeHost( host.hostID, {
			callback : function( hostInfo ) {
				console.info( "Done!", hostInfo );
				app.status.removeClassName( 'error' )
			  	.update( 'Host <strong>' + hostInfo.hostName 
			  			+ '</strong> is now available at <strong>' 
			  			+ hostInfo.ip + '</strong>' ).show();
			}
		});
	},
	
	guessHostInfo : function(evt) {
		evt.stop();
		try {
			def applet = $('applet'); 
			if ( ! applet.guessHostInfoPriv ) throw new Exception( 
					"The 'guess' feature requires <a " +
					"href='http://www.java.com/en/download/index.jsp'>the latest Java plugin</a>" )
			var host = applet.guessHostInfoPriv();
			console.log( "Got host from applet: ", host );
			host.alias = '';
			app.editHost( host );
		}
		catch ( ex ) { app.errHandler( "Oops!  Couldn't guess host info.", ex ); }
	},
	
	validate : function( obj, fields ) {
		var missing = []
		$A(fields).each( function(prop) {
			if ( ! obj[prop] ) missing.push( prop ); 
		});
		if ( missing.size() < 1 ) return true;		
		app.errHandler( 'The following fields are required:', missing );
		return false;
	},
	
	showStats : function() {
		Stats.showStats( { 
			callback : function( stats ) {
				var statPage = $('stats');
				statPage.down('.hostCount').update( stats.hostCount );
				statPage.down('.kwhTotal').update( stats.kwhTotal );
				statPage.down('.sinceDt').update( stats.sinceDt.toDateString() );
			}
		});
	},
	
	errHandler : function( errStr, ex ) {
		console.error( "Handled error", errStr, ex );
		if ( ex && ex.javaClassName && ex.javaClassName.match( /\.security\./ ) ) {
			app.showDialog(app.loginDialog).down('.msg').update( ex.message ).show();
			app.loginDialog.down('input').focus();
			return;
		}
		else if ( ex && ex.javaClassName && ex.javaClassName.endsWith( 'UndeclaredThrowableException' ) 
				&& ex.cause ) return app.errHandler( errStr, ex.cause );
		
		var detail;
		if ( ex && ex.javaClassName ) errStr =  ex.javaClassName+": " + ex.message;
		else  detail = ex;
		if ( ! detail ) detail = '';
	  app.status.addClassName( 'error' );
	  app.status.update( '<strong>Error</strong>: ' + errStr + '<br/>' + detail );
	  app.status.show();
	},
	
	showDialog : function( dialogName ) {
		$('dialogBox').show();
		return $(dialogName).show();
	},
	
	hideDialog : function() {
		var visDialog;
		var box = $('dialogBox');
		box.select( '.dialog' ).each( function( d ) {
			if ( ! d.visible() ) return;
			visDialog = d.hide();
		});
		box.hide();
		return visDialog;
	},
	
	showCurrentUser : function() {
		// if user is not logged in but page was reloaded:
		if ( app.footer.visible() ) return;
		Auth.getCurrentUser( { callback : function(resp) {
			$('userStatus').update(resp);
			app.footer.show();			
		}});
	},
	
	loginAction : function() {
		Auth.authenticate( $('uid').value, $('pass').value, {
			callback: app.loginCallback,
			exceptionHandler : app.errHandler.bind( app )
		});
	},
	
	loginCallback : function(resp) {
		app.hideDialog(app.loginDialog).down('.msg').hide();
		app.loginDialog.select('input').invoke('clear');
		$('userStatus').update(resp);
		app.footer.show();
		app.getHostList();
		app.showCurrentUser();
	},
	
	logout : function(evt) {
		evt.stop();
		Auth.logout();
		app.footer.hide();
		app.showDialog(app.loginDialog).down('input').focus();
	},
	
	tabSelect : function( evt ) {
		var pageID = evt.findElement('a').hash;
		pageID = pageID.substring(1,pageID.length);
		var loadScript;
		$('content').select('.page').each(function(page) {
			if ( page.id == pageID ) {
				page.show();
				loadScript = page.down('.script');
			}
			else page.hide();
		});
		if ( loadScript ) eval( loadScript.textContent );
		
		$('tabMenu').select('li').invoke('removeClassName','selected');
		var tab = evt.findElement('li').addClassName('selected');
		
    // Remember which tab was open last via cookie.
    app.cookies.put( 'tab', pageID );
	},

  loadInitialTab : function() {
    var tabName = 'stats';
    var page = document.location.hash;
    if ( page.length > 1 ) // check page URL for document fragment
      tabName = page.substring(1,page.length);
    else { // check cookie
        var cookie = app.cookies.get('tab');
        if ( cookie ) tabName = cookie;
    }
    Util.fireEvent( $('tabMenu').down('.'+tabName), 'click' );
  },
	
	cookies : new CookieJar( { expires: Date.now().add( 30 ).days() } )
};


// Attach event handlers to markup
Event.observe( window, 'load', function() {
	app.status = $('status');
	app.spinner = $('spinner');
	app.loginDialog = $('loginDialog');
	app.footer = $('footer');
	$('login').observe('click', app.loginAction );
	$('pass').observe('keypress', function(evt) {
		if ( evt.keyCode == Event.KEY_RETURN ) app.loginAction();
	});
	$('logoutLink').observe( 'click', app.logout );
	app.status.observe( 'click', function( evt ) { app.status.hide(); } );
	
	$('addLnk').observe('click', app.editHost.curry(null) );
	$('guessBtn').observe('click', app.guessHostInfo );
//	$('editLink').observe('click', app.editHost );
//	$('wakeBtn').observe('click', app.wakeHost );
	$('hostInfoTable').select('td input').invoke('hide');
	
	$('tabMenu').observe('click', app.tabSelect );
	$('hostsLink').observe('click', app.tabSelect );
	
	app.loadInitialTab();
} );


dwr.engine.setErrorHandler( app.errHandler );
dwr.engine.setPreHook( function() {
	app.spinner.show();
} );

dwr.engine.setPostHook( function() {
	app.spinner.hide();
} );