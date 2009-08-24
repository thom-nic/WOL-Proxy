package com.enernoc.rnd.wolproxy

import org.springframework.security.SpringSecurityException
import org.springframework.security.AuthenticationManager
import org.springframework.security.context.SecurityContextHolder
import org.springframework.security.context.SecurityContextImpl
import org.springframework.security.providers.UsernamePasswordAuthenticationToken

/**
 * @see org.springframework.security.context.SecurityContextHolder
 * @author tnichols
 */
public class AuthService {
	AuthenticationManager authManager
	
	private final log = org.slf4j.LoggerFactory.getLogger(getClass())
	String user
	
	public String authenticate( String user, String pass ) throws SpringSecurityException {
		log.debug "user ${user} is loggin in..."
		this.user = user; return user;
		def auth = new UsernamePasswordAuthenticationToken(user,pass)
		auth = authManager.authenticate( auth )
		log.debug "user ${auth.principal} has authenticated"
		def secCtx = new SecurityContextImpl()
		secCtx.authentication = auth
		SecurityContextHolder.context = secCtx
		return auth.principal.username // LdapUserDetailsImpl
	}
	
	public String getCurrentUser() throws SpringSecurityException {
		return this.user
		SecurityContextHolder.context?.authentication?.principal?.username
	}
	
	public void logout() {
		this.user = null; return
		SecurityContextHolder.context = new SecurityContextImpl()
	}
}
