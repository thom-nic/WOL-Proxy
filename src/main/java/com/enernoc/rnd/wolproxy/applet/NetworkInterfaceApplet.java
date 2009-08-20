package com.enernoc.rnd.wolproxy.applet;

import java.applet.Applet;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Applet used on the client side to guess interface information.
 * @author tnichols
 */
public class NetworkInterfaceApplet extends Applet {

	private static final long serialVersionUID = 1765586317276697832L;
	private Logger log = Logger.getLogger( getClass().getName() );
	AccessControlContext acsCtx;
	Host host;
	
	// TODO detect java version for 1.5 and show error.
	
	@Override public void init() {
//		super.setBackground( Color.BLACK );
		super.init();
		this.acsCtx = AccessController.getContext();
	}
	
	public Host guessHostInfoPriv() throws Exception {
		try {
			return AccessController.doPrivileged( new PrivilegedExceptionAction<Host>() {
				public Host run() throws Exception {
					log.fine( "About to guess host info..." );
					return guessHostInfo();
				}
			}, acsCtx);
		}
		catch ( Exception ex ) {
//			ex.printStackTrace(out);
			log.log( Level.SEVERE, "WTF", ex );
			throw ex;
		}
	}
	
	public Host guessHostInfo() throws SocketException, UnknownHostException {
		System.getSecurityManager().checkPermission( new AllPermission() );
		log.info( "Applet has full security permissions" );
		
		Host host = new Host();
		for ( NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces()) ) {
			if ( iface.isLoopback() || iface.isPointToPoint() 
					|| ! iface.isUp() || iface.isVirtual() ) continue;

			log.fine( "IFace: (" + iface.getName() + ") " + iface.getDisplayName() );
			
			host.macAddress = macToString( iface );

			for ( InterfaceAddress ifAddr : iface.getInterfaceAddresses() ) {
				InetAddress addr = ifAddr.getAddress(); 
				if ( addr.getAddress().length != 4 ) continue; // skip ipv6
				host.ip = addr.getHostAddress();
				host.hostName = addr.getHostName();
				host.broadcastAddr = ifAddr.getBroadcast().getHostAddress();
				return host;
			}
			log.fine( "Couldn't fine an InterfaceAddress; looking through InetAddresses" );
			Enumeration<InetAddress> addrs = iface.getInetAddresses();
			if ( addrs == null || ! addrs.hasMoreElements() ) continue;
			while ( addrs.hasMoreElements() ) {
				InetAddress addr = addrs.nextElement();
				if ( addr.isMulticastAddress() ) host.broadcastAddr = addr.getHostAddress();
				else {
					host.ip = addr.getHostAddress();
					host.hostName = addr.getHostName();
				}
			}
			
			return host;
		}
		log.warning( "Could not find a suitable NetworkInterface" );
		throw new UnknownHostException( "Could not find a suitable NetworkInterface" );
	}
	
	protected String macToString( NetworkInterface iface ) throws SocketException {
		StringBuffer sb = new StringBuffer();
		for ( byte b : iface.getHardwareAddress() ) 
			sb.append( String.format("%x",b) ).append(':');
		return sb.deleteCharAt( sb.length()-1 ).toString();
	}	
}
