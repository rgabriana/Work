package com.emscloud.util;

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

} //end of class DatabaseUtil
