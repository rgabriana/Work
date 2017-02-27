package com.emscloud.util;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;

public class DatabaseUtil {
	
	public static int port = 5432;
		
	public static void closeResultSet(ResultSet rs) {
		
		if(rs != null) {
			try {
				rs.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
		}
		
	} //end of method closeResultSet
	
	public static void closeConnection(Connection con) {
		
		if(con != null) {
			try {
				con.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
		}
		
	} //end of method closeResultSet

	public static void closeStatement(Statement stmt) {
	
		if(stmt != null) {
			try {
				stmt.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	
	} //end of method closeStatement
	
	
	public static Connection getDbConnection(String dbName, String replicaServer) {
		
		String dbUser = "postgres";
		String dbPassword = "postgres";
		
		Connection connection = null;		
		if(dbName == null) {
			return null;
		}
		try {
			String conString = "jdbc:postgresql://" + replicaServer + ":" + DatabaseUtil.port + "/" + dbName +  "?characterEncoding=utf-8";
			connection = DriverManager.getConnection(conString, dbUser, dbPassword);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return connection;
		
	} //end of method getDbConnection

} //end of class DatabaseUtil
