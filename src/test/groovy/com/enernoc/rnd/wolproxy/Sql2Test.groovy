package com.enernoc.rnd.wolproxy

import org.junit.Test/**
 * @author tnichols
 */
public class Sql2Test {

	@Test public void testNamedParamParsing() {
		
		def query = Sql2.parseQuery( '''select one, two from blah where
			name=:name and something like :somethingElse''' )
			
		assert query.params[0] == 'name'
		assert query.params[1] == 'somethingElse'
		assert query.sql == '''select one, two from blah where
			name=? and something like ?'''
					
		query = Sql2.parseQuery( '''insert into host ( owner_id, mac_address, host_name, ip, port ) \
				values ( :ownerID, :macAddress, :hostName, :ip, :wolPort )''' )
				
		assert query.params.size() == 5
		assert query.params[0] == "ownerID"
		assert query.params[1] == "macAddress"
		assert query.params[2] == "hostName"
		assert query.params[3] == "ip"
		assert query.params[4] == "wolPort"
		assert query.sql == '''insert into host ( owner_id, mac_address, host_name, ip, port ) 
				values ( ?, ?, ?, ?, ? )'''
	}
}
