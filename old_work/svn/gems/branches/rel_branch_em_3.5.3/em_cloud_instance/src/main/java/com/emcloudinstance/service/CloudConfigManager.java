package com.emcloudinstance.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.emcloudinstance.dao.CloudConfigDao;
import com.emcloudinstance.util.DatabaseUtil;

@Service("cloudConfigManager")
public class CloudConfigManager {
	
	@Resource
	CloudConfigDao cloudConfigDao;
	

	public String getLastWALSynched(String mac){
	   return cloudConfigDao.getCloudConfig(mac, "lastWalSyncId");	
	}
	
	public String getLastWALSynched(String mac, JdbcTemplate jdbcTemplate){
		   return cloudConfigDao.getCloudConfig(mac, "lastWalSyncId", jdbcTemplate);	
		}
	
	public int updateCloudConfig(String mac, String configName, String value){
		return cloudConfigDao.updateCloudConfig(mac, configName,value);	
	}
	
	public int updateCloudConfig(String mac, String configName, String value, JdbcTemplate jdbcTemplate){
		return cloudConfigDao.updateCloudConfig(mac, configName,value, jdbcTemplate);	
	}
	
	
	@Resource 
	DatabaseUtil databaseUtil;

	public void addOrUpdateConfig(String dbName, String configName, String configValue) {
		String conString = "jdbc:postgresql://localhost:" + databaseUtil.port + "/" + dbName +  "?characterEncoding=utf-8";
		Connection connection = null;
		Statement stmt = null;
		
		try {
			connection = DriverManager.getConnection(conString, "postgres", "postgres");
			stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select '1' from cloud_config where name = '" + configName + "'");
            if(rs != null && rs.next()) {
            	stmt.close();
            	stmt = connection.createStatement();
            	stmt.executeUpdate("update cloud_config set val = '" + configValue + "' where name = '" + configName + "'");
            }
            else {
            	stmt.close();
            	stmt = connection.createStatement();
            	stmt.executeUpdate("insert into cloud_config (id, val, name) values ((select coalesce(max(id),0)+1 from cloud_config), '" + configValue + "' , '" + configName + "' )");
            }

			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public String getCloudConfig(String dbName, String configName) {
		String conString = "jdbc:postgresql://localhost:" + databaseUtil.port + "/" + dbName +  "?characterEncoding=utf-8";
		Connection connection = null;
		Statement stmt = null;
		
		String output = null;
		
		try {
			connection = DriverManager.getConnection(conString, "postgres", "postgres");
			stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select val from cloud_config where name = '" + configName + "'");
            if(rs != null && rs.next()) {
            	output = rs.getString("val");
            }
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return output;
	}

}
