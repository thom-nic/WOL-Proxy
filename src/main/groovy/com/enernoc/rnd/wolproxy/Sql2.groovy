package com.enernoc.rnd.wolproxy

import groovy.sql.Sql
import groovy.lang.GroovyObjectimport java.util.concurrent.ConcurrentHashMapimport java.util.regex.Patternimport javax.sql.DataSourceimport java.sql.Connection
import org.slf4j.LoggerFactoryimport org.slf4j.Logger/**
 * @author tnichols
 */
public class Sql2 extends Sql {

	static final Logger log = LoggerFactory.getLogger( Sql2 )
	private static final PATTERN = Pattern.compile( /:\w+/ )
	
	static queryCache = new ConcurrentHashMap<String,NamedQuery>()
	
	public Sql2( DataSource ds ) { super(ds) }
	public Sql2( Connection c ) { super(c) }
	public Sql2( Sql parent ) { super(parent) }
	
	int executeUpdate( String sql, GroovyObject model ) {
		def query = parseQuery( sql )
		super.executeUpdate( query.sql, 
				query.params.collect { model.getProperty it } )
	}
	
	Object executeScalar( String sql, List args=[] ) {
		super.firstRow( sql, args )?.getAt(0) // return the first result
	}

	Object executeScalar( String sql, GroovyObject model ) {
		def query = parseQuery( sql )
		super.firstRow( query.sql, 
				query.params.collect { model.getProperty it } 
			)?.getAt(0) // return the first result
	}

	List executeInsert( String sql, GroovyObject model ) {
		def query = parseQuery( sql )
		return super.executeInsert( query.sql, query.params.collect { model.getProperty it } )
	}
	
	protected static NamedQuery parseQuery( String sql ) {
		def query = queryCache[sql]
		if ( query ) return query
		
		def newSql = new StringBuilder()
		def params = []
		
		int txtIndex = 0
		def matcher = PATTERN.matcher( sql )
		while ( matcher.find() ) {
			newSql.append( sql[txtIndex..matcher.start()-1] ).append('?')
			params << matcher.group()[1..-1]
			txtIndex = matcher.end()
		}
		if ( txtIndex < sql.length() ) // append ending SQL after last param.
			newSql.append sql[txtIndex..-1]
		
		query = new NamedQuery( sql : newSql, params: params )
		queryCache[sql] = query
		if ( log.debugEnabled )	log.debug "Parsed named query to '{}' - Args: {}", query.sql, query.params
		return query
	}
}

class NamedQuery {
	String sql 
	List<String> params
}