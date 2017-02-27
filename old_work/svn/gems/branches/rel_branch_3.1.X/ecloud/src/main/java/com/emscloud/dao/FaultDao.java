/**
 * 
 */
package com.emscloud.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.util.DateUtil;
import com.emscloud.util.DatabaseUtil;

/**
 * @author sreedhar.kamishetti
 *
 */
@Service("faultDao")
@Transactional(propagation = Propagation.REQUIRED)
public class FaultDao {

	/**
	 * 
	 */
	public FaultDao() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList<String> getEmAlarms(String dbName, Date fromDate, Date toDate) {
		
		String dbUser = "postgres";
		String dbPassword = "postgres";
		
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		if(dbName == null) {
			return null;
		}
		try {
			String conString = "jdbc:postgresql://localhost:" + DatabaseUtil.port + "/" + dbName +  "?characterEncoding=utf-8";
			connection = DriverManager.getConnection(conString, dbUser, dbPassword);
			stmt = connection.createStatement();
			String query = "select event_time, severity, event_type, description, device_id, name, location " 
					+ "from events_and_fault as ef, device as d where event_time > '" + 
					DateUtil.formatDate(fromDate, "yyyy-MM-dd HH:mm:ss") + "' and event_time <= '" +
					DateUtil.formatDate(toDate, "yyyy-MM-dd HH:mm:ss") + "' and (event_type = " +
					"'Gateway unreachable' or event_type = 'Fixture Out') and d.id = ef.id";
			rs = stmt.executeQuery(query);
			ArrayList<String> alarmList = new ArrayList<String>();
			while(rs.next()) {
				String eventStr = rs.getString("name") + ", " + rs.getDate("event_time") + ", " + rs.getString("severity") +
						", " + rs.getString("event_type") + ", " + rs.getString("location");
				alarmList.add(eventStr);
			}
			return alarmList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DatabaseUtil.closeResultSet(rs);
			DatabaseUtil.closeStatement(stmt);
			DatabaseUtil.closeConnection(connection);
		}
		return null;
		
	} //end of method getEmAlarms

} //end of class FaultDao
