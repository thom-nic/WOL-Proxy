package com.enernoc.rnd.wolproxy

import org.codehaus.groovy.runtime.TimeCategory

/**
 * @author tnichols
 */
public class StatsService {

	groovy.sql.Sql db
	String hostCountQuery
	Date sinceDate = new Date()

	float kwPerHost = 0.12 // 120W difference
	int avgHoursAsleepPerDay = 21 // ( 13 hours * 5 days + 44 hours * 2 days ) / 7
	
	def lastUpdate = new Date(0)
	Map stats
	
	public Map showStats() {
		def now = new Date()
		use ( TimeCategory ) { // only run update query once per day.
			if ( stats && lastUpdate + 5.minutes > now ) return this.stats

			def kwhSaved = 0.0
			def hostCount = 0
			db.eachRow( hostCountQuery ) {
				hostCount += it.count
				kwhSaved += ( now - it.created_dttm ).days * avgHoursAsleepPerDay * kwPerHost
			}
			this.stats = [ hostCount: hostCount, kwhTotal: kwhSaved, sinceDt : sinceDate ]
			this.lastUpdate = now
		}
		stats
	}
}
