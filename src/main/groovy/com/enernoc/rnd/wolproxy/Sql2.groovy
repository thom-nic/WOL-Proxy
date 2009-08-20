package com.enernoc.rnd.wolproxy

import groovy.sql.Sql
import groovy.lang.GroovyObjectimport java.util.concurrent.ConcurrentHashMapimport java.util.regex.Patternimport javax.sql.DataSourceimport java.sql.Connection
import org.slf4j.LoggerFactoryimport org.slf4j.Logger/**
 * Extension to groovy.sql.Sql which adds named query parameters in the form 
 * ":paramName".  This class also overloads the executeUpdate and executeInsert
 * methods to accept a POJO.  The SQL query is then parsed and the named 
 * parameters are mapped to the POJO properties.  Say you have a POJO like 
 * this:
 * <pre> 
 * class Person {
 * 	long ID
 * 	String firstName
 * 	String lastName
 * 	String address
 * 	int age
 * }</pre>
 * And corresponding SQL table definition
 * <pre>
 * create table person (
 * 	person_id varchar identity,
 * 	first_name varchar(20),
 * 	last_name varchar(20),
 * 	addr varchar(80),
 * 	age bigint
 * }</pre>
 * Your inserts can look like this:
 * <pre>
 * insert into person ( first_name, last_name, addr, age ) values {
 * 	:firstName, :lastName, :address, :age )</pre>
 * And an update:
 * <pre>
 * update person set first_name=:firstName, last_name=:lastName, addr=:address, 
 * 	age=:age where person_id=:ID</pre>
 * Queries are, for the time being, still left as positional-parameter ('?') 
 * only, as they usually only require a few parameters.
 * 
 * <p>'Named parameter' queries are parsed into their equivalent positional-
 * parameter equivalents and cached along with the list of parameter names the
 * first time they encountered.  After that point, the same 'named parameter' 
 * query will be retrieved from the cache and not re-parsed.</p> 
 * 
 * @author tnichols
 * @version 0.1
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
	
	List executeInsert( String sql, GroovyObject model ) {
		def query = parseQuery( sql )
		return super.executeInsert( query.sql, 
				query.params.collect { model.getProperty it } )
	}
	
	protected static NamedQuery parseQuery( String sql ) {
		def query = queryCache[sql]
		if ( query ) return query
		
		def newSql = new StringBuilder()
		def params = []
		
		int txtIndex = 0
		def matcher = PATTERN.matcher( sql )
		while ( matcher.find() ) { // replace named params w/ positional ones
			newSql.append( sql[txtIndex..matcher.start()-1] ).append('?')
			params << matcher.group()[1..-1]
			txtIndex = matcher.end()
		}
		if ( txtIndex < sql.length() ) // append ending SQL after last param.
			newSql.append sql[txtIndex..-1]
		
		query = new NamedQuery( sql : newSql, params: params )
		queryCache[sql] = query
		if ( log.debugEnabled )	
			log.debug "Parsed named query to '{}' - Args: {}", 
					query.sql, query.params
		return query
	}
}

class NamedQuery {
	String sql 
	List<String> params
}