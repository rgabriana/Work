package com.emscloud.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Controller;

import com.emscloud.model.EmInstance;
import com.emscloud.model.EmStats;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmStatsManager;
import com.sun.jersey.multipart.FormDataParam;

@Controller
@Path("/org/communicate")
public class communicatorWebservices {

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	@Resource
	EmInstanceManager emInstanceManger ;
	@Resource
	EmStatsManager emStatsManager;
	
	private String dbport = "5432";
	

	@SuppressWarnings("unchecked")
	@Path("em/stats")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response call_home_communicate( @FormDataParam("data") InputStream otherDataStream) {
		
		boolean fail = false;

		ZipInputStream zipOtherIn = new ZipInputStream(otherDataStream);
		ObjectInputStream objectIn = null;
		
		try {
			zipOtherIn.getNextEntry();
			objectIn = new ObjectInputStream(zipOtherIn);
			Map<String, Object> map = (HashMap<String, Object>) objectIn.readObject();
			System.out.println("Mac Id = " + map.get("macId") + "   App Version = " + map.get("version"));
			EmInstance emInstance = emInstanceManger.loadEmInstanceByMac(map.get("macId").toString());
			
			if(emInstance != null && map.containsKey("id") && map.get("id") != null && !"".equals(map.get("id").toString())) {
				emInstance.setVersion(map.get("version").toString());
				emInstance.setLastConnectivityAt(new Date());
				emInstanceManger.saveOrUpdate(emInstance);
				
				EmStats emStats = new EmStats();
				emStats.setEmInstanceId(emInstance.getId());
				emStats.setCaptureAt((Date)map.get("capture_at"));
				emStats.setActiveThreadCount((Integer) map.get("active_thread_count"));
				emStats.setGcCount((Long)map.get("gc_count"));
				emStats.setGcTime((Long)map.get("gc_time"));
				emStats.setHeapUsed(((BigDecimal)map.get("heap_used")).doubleValue());
				emStats.setNonHeapUsed(((BigDecimal)map.get("non_heap_used")).doubleValue());
				emStats.setSysLoad(((BigDecimal)map.get("sys_load")).doubleValue());
				emStats.setCpuPercentage(((BigDecimal)map.get("cpu_percentage")).floatValue());
				emStats.setIsEmAccessible(((Boolean) (map.get("em_accessible"))).booleanValue());
				emStatsManager.saveObject(emStats);
			}
			else {
				fail = true;
			}
		} catch (IOException e) {
			fail = true;
			e.printStackTrace();
		} catch (Exception e) {
			fail = true;
			e.printStackTrace();
		} finally {
			if(zipOtherIn != null) {
				try {
					zipOtherIn.close();
				} catch (IOException e) {
					fail = true;
					e.printStackTrace();
				}
			}
			if(objectIn != null) {
				try {
					objectIn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		if(fail) {
			return Response.status(500).entity("").build();
		}
		else {
			return Response.status(200).entity("").build();
		}
		

	}

	@Path("em/info")
	@POST
	@Consumes({ MediaType.APPLICATION_OCTET_STREAM })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String sendEmInfo(String data) {

		System.out.println(data);
		
		String[] arr = data.split("#");
		String emInstanceId = "-1";
		EmInstance em = emInstanceManger.loadEmInstanceByMac(arr[0]) ;
		if(em != null) {
			if(em.getActive()) {
				emInstanceId = String.valueOf(em.getId()) ;
			}
			else {
				em.setLastConnectivityAt(new Date());
				emInstanceManger.saveOrUpdate(em);
			}
		}
		else {
			EmInstance emInstance = new EmInstance();
			emInstance.setActive(false);
			emInstance.setVersion(arr[1]);
			emInstance.setMacId(arr[0]);
			emInstance.setLastConnectivityAt(new Date());
			emInstanceManger.saveOrUpdate(emInstance);
		}
		return emInstanceId ;

	}
	
	@Path("em/lastWalSynced/{emMacId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String lastWalSynced(@PathParam("emMacId") String macId) {

		String lastWalSyncId = "-100";
		EmInstance em = emInstanceManger.loadEmInstanceByMac(macId) ;
		Connection connection = null;
		Statement stmt = null;
		if(em!=null) {
			try {
				String conString = "jdbc:postgresql://localhost:" + dbport + "/" + em.getDatabaseName() +  "?characterEncoding=utf-8";
				connection = DriverManager.getConnection(conString,
						"postgres", "postgres");
				stmt = connection.createStatement();
	            ResultSet rs = stmt.executeQuery("select val from cloud_config where name = 'lastWalSyncId'");
	             
				rs.next();
				lastWalSyncId = rs.getString("val");
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if(stmt != null) {
					try {
						stmt.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(connection != null) {
					try {
						connection.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return lastWalSyncId ;

	}
	
	/**
	 * @param otherDataStream
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Path("em/data")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response sppa_communicate( @FormDataParam("other") InputStream otherDataStream) {
		
		System.out.println("Testing");
		
		boolean fail = false;

		///ZipInputStream zipEnergyIn = new ZipInputStream(energyDataStream);
		ZipInputStream zipOtherIn = new ZipInputStream(otherDataStream);
		Connection connection = null;
		Statement stmt = null;
		
		try {
			zipOtherIn.getNextEntry();
			ObjectInputStream objectIn = new ObjectInputStream(zipOtherIn);
			Map<String, Object> map = (HashMap<String, Object>) objectIn.readObject();
			System.out.println(map.get("macId") + "   " + map.get("version") + "   " + map.get("maxWalLogDataId"));
			EmInstance emInstance = emInstanceManger.loadEmInstanceByMac(map.get("macId").toString());
			
			if(emInstance != null && !emInstance.getDatabaseName().isEmpty()) {
				String conString = "jdbc:postgresql://localhost:" + dbport + "/" + emInstance.getDatabaseName() +  "?characterEncoding=utf-8";
				System.out.println(conString);
				connection = DriverManager.getConnection(conString,
						"postgres", "postgres");
				connection.setAutoCommit(false);
				
				try {
					List<String> myObj1 = (ArrayList<String>) map.get("data");
					int count = 2;
					stmt = connection.createStatement();
					stmt.addBatch("delete from wal_logs where id < (select max(id) - 10000 from wal_logs)");	
					for(String each: myObj1) {
						if(count < 1000) {
							count++;
							stmt.addBatch(each);
						}
						else {
							stmt.executeBatch();
							count = 1;
							stmt.close();
							stmt = connection.createStatement();
							stmt.addBatch(each);
						}
					}
					stmt.executeUpdate("update cloud_config set val = " + map.get("maxWalLogDataId") + " where name = 'lastWalSyncId'");
					stmt.executeBatch();
					
					/*zipEnergyIn.getNextEntry();
					CopyManager cm1 = new CopyManager((BaseConnection) connection);
					cm1.copyIn("COPY energy_consumption FROM STDIN WITH DELIMITER '~'", zipEnergyIn);
					zipEnergyIn.closeEntry();*/
					
					connection.commit();
				} catch(Exception e) {
					fail = true;
					e.printStackTrace();
				} finally {
					if(objectIn != null) {
						objectIn.close();
					}
				}
				
				

			}
			else {
				fail = true;
			}
		} catch (IOException e) {
			fail = true;
			e.printStackTrace();
		} catch (SQLException e) {
			fail = true;
			e.printStackTrace();
		} catch (Exception e) {
			fail = true;
			e.printStackTrace();
		} finally {
			/*if(zipEnergyIn != null) {
				try {
					zipEnergyIn.close();
				} catch (IOException e) {
					fail = true;
					e.printStackTrace();
				}
			}*/
			if(zipOtherIn != null) {
				try {
					zipOtherIn.close();
				} catch (IOException e) {
					fail = true;
					e.printStackTrace();
				}
			}
			if(stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					fail = true;
					e.printStackTrace();
				}
			}
			if(connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					fail = true;
					e.printStackTrace();
				}
			}
		}
		
		if(fail) {
			return Response.status(500).entity("").build();
		}
		else {
			//TODO: return transaction id
			return Response.status(200).entity("").build();
		}
		

	}

}
