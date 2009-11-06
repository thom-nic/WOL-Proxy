/**
 * 
 */
package com.enernoc.rnd.wolproxy;

import javax.naming.NamingException;

import org.springframework.dao.DataAccessException;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * @author tnichols
 */
public class LDAPUserDetailsService implements UserDetailsService {
	
	LdapContextSource ldap;

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	@Override
	public UserDetails loadUserByUsername( String uname )
			throws UsernameNotFoundException, DataAccessException {
		
		try {
			Object ctx = ldap.getReadOnlyContext().lookup( uname );
		} catch ( NamingException e ) {
			throw new UsernameNotFoundException( uname );
		}
		return null;
	}

}
