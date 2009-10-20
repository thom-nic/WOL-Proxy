/* JScript, not javascript.
 * http://www.qualitycodes.com/tutorial.php?articleid=19
 * http://www.devarticles.com/c/a/JavaScript/Advanced-JavaScript-with-Internet-Explorer-Retrieving-Networking-Configuration-Information/1/
 */ 
application.ie = {
		getMACAddress : function() {
			var wbem = new ActiveXObject("WbemScripting.SWbemLocator");
			var resultSet = wbem.ConnectServer(".").ExecQuery( 
					"SELECT * FROM Win32_NetworkAdapterConfiguration" );
			var iter = new Enumerator( resultSet );

			for (; ! iter.atEnd(); iter.moveNext() ) {
				iter.MACAddress;
				// TODO get IP, broadcast, host name 
			}
		}
}