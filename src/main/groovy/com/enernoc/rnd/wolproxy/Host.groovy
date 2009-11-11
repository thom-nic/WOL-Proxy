package com.enernoc.rnd.wolproxy

/**
 * @author tnichols
 */
public class Host {

	Integer hostID // PK
	String ownerID // FK
	
	String alias
	String macAddress
	String hostName
	String ip
	String broadcastAddr
	Date createdDate
	Integer wolPort = 9
	String sharedGroup
	
	String getAlias() { alias ?: hostName }
	
	public String toString() { "Host ($hostID) $hostName" }
}
