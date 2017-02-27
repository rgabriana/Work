package com.emscloud.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.stereotype.Controller;

import com.emscloud.model.EmInstance;
import com.emscloud.service.EmInstanceManager;
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
	

	/**
	 * @param otherDataStream
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Path("em/data")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response communicate( @FormDataParam("other") InputStream otherDataStream) {
		
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
				String conString = "jdbc:postgresql://localhost:5432/" + emInstance.getDatabaseName() +  "?characterEncoding=utf-8";
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

	@Path("em/info/{emMacId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String sendEmInfo(@PathParam("emMacId") String macId) {

		String emInstanceId =null;
		EmInstance em = emInstanceManger.loadEmInstanceByMac(macId) ;
		if(em!=null)
		{
			emInstanceId = String.valueOf(em.getId()) ;
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
				String conString = "jdbc:postgresql://localhost:5432/" + em.getDatabaseName() +  "?characterEncoding=utf-8";
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

}
