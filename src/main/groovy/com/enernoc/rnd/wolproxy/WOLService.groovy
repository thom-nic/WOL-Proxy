package com.enernoc.rnd.wolproxy

import java.net.*
import org.slf4j.Loggerimport org.slf4j.LoggerFactory
import java.sql.SQLException
import org.springframework.security.BadCredentialsException/**
 * @author tnichols
 */ 
public class WOLService {

	final Logger log = LoggerFactory.getLogger( getClass() )
	groovy.sql.Sql db
	def authService
	
	static packetHeader = [0xff]*6
	
	String hostListQuery
	String hostDetailQuery
	String hostUpdate
	String hostInsert
	String hostDelete
	String seqQuery
	String hostOwnerCheck
	
	protected String getUserID() {
		authService.currentUser 
	}
	
	public List<Host> getHosts() {
		if ( ! userID ) throw new BadCredentialsException("Please log in")
		def list = []
		db.eachRow( hostListQuery, [userID] ) {
			list << new Host( it.toRowResult() )
		}
		list
	}
	
	public Host getHost( int hostID ) {
		if ( ! userID ) throw new BadCredentialsException("Please log in")
		def row = db.firstRow( hostDetailQuery, [hostID,userID] )
		if ( ! row ) throw new RemoteException( "Could not find host with ID $hostID" )
		new Host( row )
	}
	
	public Host updateHost( Host host ) {
		def sql = this.hostUpdate
		host.ownerID = this.userID
		if ( ! host.hostID ) { 
			sql = this.hostInsert
			host.hostID  = db.executeScalar( seqQuery )
			host.createdDate = new Date()
		}
		// Verify owner of this hostID
//		else if ( db.executeScalar( hostOwnerCheck, host ) < 1 ) 
//				throw new RemoteException( "You are not the owner of this host" )

		try {
			def count = db.executeUpdate( sql, host )
			if ( count < 1 ) throw new RemoteException( 
					"Could not update host ID ${host.hostID}" )
		} catch ( SQLException ex ) {
			def msg
			switch ( ex.message ) {
			case ~/IDX_HOST/ :
				msg = "This host name is already registered"
				break
			case ~/IDX_MAC/ :
				msg = "This MAC address is already registered with another host"
				break
			default : msg = "Database error"
			}
			throw new RemoteException( msg, ex )
		}
		host
	}
	
	public void deleteHost( int hostID ) {
		def count = db.executeUpdate( hostDelete, [hostID, this.userID] )
		if ( count < 1 ) throw new RemoteException( "Could not find host ID $hostID for your user" )
	}
	
	public Map wakeHost( int hostID ) {
		def host = getHost( hostID )
		
		log.info "Waking {}", host
		sendWOLPacket host
		
		// TODO sleep for a little while before attempting to ping host.
		def addr = ping( host )
		if ( ! addr ) throw new RemoteException( 'Could not access host' + host.hostName )
		/*if ( ! addr ) {
			sendWOLPacket host, false
			addr = ping( host )
		}*/
		
		return [ hostName: host.alias, ip: addr.hostAddress ] 
	}
	
	public testHost( Host host ) {
		
	}
	
	protected sendWOLPacket( Host host, useHost=true ) {
		def inet = getSubnet( host )
//			useHost ? InetAddress.getByName( host.hostName ) :
//			InetAddress.getByName( host.ip )
			
		if ( ! inet ) throw new RemoteException( "Could not resolve host by name or IP" )

		def packet = host.macAddress.split(':').collect {
			Short.parseShort( it, 16 )
		}
		packet = packetHeader + packet * 16

		def sock = new DatagramSocket( broadcast:true )
		try {
			sock.send new DatagramPacket( packet as byte[], packet.size(), 
			    inet, host.wolPort )
		} finally {	sock.close() }
		log.debug "Sent WOL packet to {}", inet
	}

	protected InetAddress ping( host, attempts=2 ) {
		def suffixes = ['','.enernoc.local','.enernoc.net']
		def addr
		suffixes.find {
			try {
				addr = InetAddress.getByName( host.hostName + it )
				return addr.isReachable( 12000 ) 
			} catch (ex) { false }
		}
		if ( ! addr && host.ip ) try {
			addr = InetAddress.getByName( host.ip )
		}
		catch ( ex ) {}
		
		if ( ! addr && attempts ) {
			log.debug "Attempt failed to ping host ${host.hostName}"
			Thread.sleep 5000
			addr = ping( host, attempts-1 )
		}
		addr
	}
	
	protected InetAddress getSubnet( host ) {
		def addr = host.broadcastAddr
		if ( ! addr ) {
			addr = host.ip ?: host.hostName
			addr = InetAddress.getByName( addr ).address // should be a byte[]
			// increment third octet by one and 0xff the last octet
			addr[-2] ++  
			addr[-1] = 0xff
		}
		else addr = InetAddress.getByName( addr ).address		
		InetAddress.getByAddress( addr )
	}
}