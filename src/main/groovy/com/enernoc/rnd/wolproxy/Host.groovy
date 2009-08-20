package com.enernoc.rnd.wolproxy

/**
 * @author tnichols
 */
public class Host {

	int hostID // PK
	String ownerID // FK
	
	String alias
	String macAddress
	String hostName
	String ip
	String broadcastAddr
	Date createdDate
	int wolPort = 9
	
	String getAlias() { alias ?: hostName }
	
	public String toString() { "Host ($hostID) $hostName" }
}
