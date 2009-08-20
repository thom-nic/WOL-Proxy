package com.enernoc.rnd.wolproxy

import groovy.sql.Sqlimport java.net.URL/**
 * @author tnichols
 *
 */
public class Bootstrap {
	Sql db
	URL sql
	
	def init() {
		db.execute sql.text
	}
}
