/* JScript, not javascript.  Works in IE only.  This is an alternate way for 
 * detecting the MAC address for users who do not have the Java plug-in 
 * installed on their computer.  Unfortunately it has a few drawbacks of its own.
 * 
 * You can modify this script slightly and run it from the Windows Console
 * by using CSCript.exe.  Note that in a web environment, the WBEM AcitveX object
 * is not "marked safe for scripting," so this application's domain should be 
 * added to the "Trusted Sites" list in Internet Explorer, and change the 
 * security permission "Initialize and script ActiveX controls not marked as safe
 * for scripting" to either "Allow" or "Prompt" (I'd suggest "Prompt".)
 * 
 * http://www.qualitycodes.com/tutorial.php?articleid=19
 * http://www.devarticles.com/c/a/JavaScript/Advanced-JavaScript-with-Internet-Explorer-Retrieving-Networking-Configuration-Information/1/
 */ 
if ( typeof( app ) == "undefined" ) app = {};
app.ie = {
		getHostInfo : function() {
		var wbem = new ActiveXObject("WbemScripting.SWbemLocator");
		// http://msdn.microsoft.com/en-us/library/aa394217%28VS.85%29.aspx
		var resultSet = wbem.ConnectServer(".").ExecQuery( 
				"SELECT * FROM Win32_NetworkAdapterConfiguration" );
		var iter = new Enumerator( resultSet );

		var result = {};
		for (; ! iter.atEnd(); iter.moveNext() ) {
			var iface = iter.item();
			if ( ! iface.IPEnabled ) continue;
//			WScript.Echo( iface.Caption );

			// convert from SAFEARRAY:
			var ips = new VBArray(iface.IPAddress).toArray();
			if ( ips.length ) {
//				WScript.Echo( " IPs:" );
//				for( ip in ips ) WScript.Echo( "  " + ip );
				for ( var i=0; i<ips.length; i++ ) {
//					WScript.Echo( "  " + ips[i] );
					if ( ips[i].indexOf('0.') != 0 ) {
						result.ip = ips[i];
						break; // find the first interface w/ a valid IP address assigned
					}
				}
			}
			if ( ! result.ip ) continue;
			
			var snet = new VBArray(iface.IPSubnet).toArray();
			if ( snet.length ) {
//				WScript.Echo( " Subnets: " );
//				for ( var i=0; i<snet.length; i++ ) WScript.Echo( "  " + snet[i] );
				result.subnet = snet[0];
			}
			
			// calculate broadcast addr as described here:
			//http://www.tech-faq.com/broadcast-address.shtml
			result.broadcast = "";
			var ipOctets = result.ip.split('.');
			var sMask = result.subnet.split('.');
			for ( var i=0; i<sMask.length; i++ )
				sMask[i] = ( 255 - sMask[i] ) | ipOctets[i];
			result.broadcastAddr = sMask.join('.');
//			WScript.Echo( result.broadcast );
			
			result.hostName = iface.DNSHostName;
			result.macAddress = iface.MACAddress;
			result.wolPort = 9;
			result.alias = '';
			return result;

/*			WScript.Echo( iface.Caption );
			WScript.Echo( " Host name: " + iface.DNSHostName );
			WScript.Echo( " Domain: " + iface.DNSDomain );
			WScript.Echo( " DHCP Server: " + iface.DHCPServer );
			WScript.Echo( " Gateway: " + iface.DefaultIPGateway );
			if ( iface.MACAddress ) WScript.Echo( " MAC: " + iface.MACAddress );
*/
		}
	},
	
	guessHostInfo : function(evt) {
		app.editHost( app.ie.getHostInfo() );
		if ( evt ) evt.stop();
	}
}