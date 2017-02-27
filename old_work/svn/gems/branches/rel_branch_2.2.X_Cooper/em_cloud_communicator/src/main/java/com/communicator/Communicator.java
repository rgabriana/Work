package com.communicator;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.communication.types.CloudParamType;
import com.communication.utils.CloudHttpResponse;
import com.communication.utils.CloudRequest;
import com.communication.utils.CloudResponse;
import com.communication.utils.JsonUtil;
import com.communication.utils.NameValue;
import com.communicator.communication.EasySSLProtocolSocketFactory;
import com.communicator.communication.TrustEverythingSSLSocketFactory;



public class Communicator {

	private static String appVersion = null;
	private static String macAddress = null;
	private static String host = null;
	private static String syncStatus = null;
	private static long sleepTime = 300 * 1000;

	static final Logger logger = Logger.getLogger(Communicator.class.getName());
	static {
		try {
			logger.setLevel(Level.INFO);
			LogManager lm = LogManager.getLogManager();
			lm.addLogger(logger);
		} catch (Throwable e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	
	public static void main(String[] args) {

		String status = checkCloudConnectivity(); 
		if (status != null && !"".equals(status) && !"0".equals(status)) {
			
			getCloudServerInfo();

			while (status != null && !"".equals(status) && !"0".equals(status)) {
				try {
					if ("1".equals(syncStatus)) {
						if("1".equals(status)) {
							sendDataCallHome();
						}
						else if ("2".equals(status)) {
							sendDataSPPA();
						}
					} else {
						getCloudServerInfo();
					}
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, e.toString(), e);
				}
				status = checkCloudConnectivity();
			}
		}
	}

	private static String checkCloudConnectivity() {

		Connection conn = null;
		Statement stmt = null;
		String status = "0";
		try {
			conn = DriverManager
					.getConnection(
							"jdbc:postgresql://localhost:5433/ems?characterEncoding=utf-8",
							"postgres", "postgres");
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("select value from system_configuration where name = 'cloud.communicate.type'");
			if (rs.next()) {
                  status = rs.getString(1);               
			}
		} catch (SQLException se) {
			 logger.log(Level.SEVERE,se.getMessage());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException se) {
				logger.log(Level.SEVERE,se.getMessage());
			}
		}
		return status;
	}

	
	private static String checkEMAcess() {
		
		Runtime rt = Runtime.getRuntime();
		Process proc;
		try {
			proc = rt.exec(new String[]{"/bin/bash", "/opt/enLighted/communicator/check_em_access.sh"});
			BufferedReader outputStream = new BufferedReader(
					new InputStreamReader(proc.getInputStream()));
			String output = null;

			while ((output = outputStream.readLine()) != null) {
				if (output.contains("200 OK")) {
					return "TRUE";
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return "FALSE";
	}
	
	
	private static long getLastWalSyncId() {
		long lastWalSyncId = -100; 
		Protocol easyhttps = new Protocol("https",
				new EasySSLProtocolSocketFactory(), 443);
		GetMethod get = new GetMethod(
				"/ecloud/services/org/communicate/em/lastWalSynced/" + macAddress);

		HostConfiguration hc = new HostConfiguration();
		hc.setHost(host, 443, easyhttps);
		HttpClient client = new HttpClient();
		
		try {
			client.executeMethod(hc, get);
			byte[] responseBody = get.getResponseBody();
			lastWalSyncId = Long.parseLong(new String(responseBody));
			logger.info("Last Wal Sync ID is " + lastWalSyncId);
		} catch (HttpException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		return lastWalSyncId;
	}
	
	public static void sendDataSPPA() {
		
		logger.info("Starting new sync event at "  + new Date());
		Long lastWalLogDataId = null;
		Long newWalLogDataId = null;
		
		try {
			lastWalLogDataId = getLastWalSyncId();
			if(lastWalLogDataId != -100) {
				
				Connection connection = null;
				PreparedStatement preparedStatement = null;
				Statement statement = null;
				Statement dltWalStmt = null;
				ByteArrayOutputStream baos_other = new ByteArrayOutputStream();
				ZipOutputStream zipOtherOut = new ZipOutputStream(baos_other);
				
				try {
					
					Part[] parts = new Part[1];
					 
					connection = DriverManager.getConnection("jdbc:postgresql://localhost:5433/ems?characterEncoding=utf-8",
									"postgres", "postgres");
					statement = connection.createStatement();
		            ResultSet rs = statement.executeQuery("select (select max(id) from wal_logs) as max_wal_id");
		             
					rs.next();
					newWalLogDataId = rs.getLong("max_wal_id");
				
					/*		
						zipEnergyOut.putNextEntry(new ZipEntry("energy_data"));
					CopyManager cm = new CopyManager((BaseConnection) connection);
					cm.copyOut("COPY (SELECT id, min_temperature, max_temperature, avg_temperature, light_on_seconds, "
						       + "light_min_level, light_max_level, light_avg_level, light_on," 
						       + "light_off, power_used, occ_in, occ_out, occ_count, dim_percentage," 
						       + "dim_offset, bright_percentage, bright_offset, capture_at, fixture_id," 
						       + "price, cost, base_power_used, base_cost, saved_power_used," 
						       + "saved_cost, occ_saving, tuneup_saving, ambient_saving, manual_saving," 
						       + "zero_bucket, avg_volts, curr_state, motion_bits, power_calc," 
						       + "energy_cum, energy_calib, min_volts, max_volts, energy_ticks," 
						       + "last_volts, saving_type, cu_status, last_temperature "
						       + " from energy_consumption where id > " + lastEnergyDataId + " and id <= " + newEnergyDataId + " order by id) "
						       + "TO STDOUT with delimiter '~'" , zipEnergyOut);
					
					zipEnergyOut.closeEntry();
					logger.info("Compressed energy data size = " + baos_energy.toByteArray().length);
					ByteArrayPartSource baps_energy = new ByteArrayPartSource("energy", baos_energy.toByteArray());*/
					
			        dltWalStmt = connection.createStatement();
			        dltWalStmt.executeUpdate("delete from wal_logs where id < " + (lastWalLogDataId - 100));
			        
					preparedStatement = connection.prepareStatement("SELECT sql_statement FROM wal_logs WHERE id > ? and id <= ? order by id");
					preparedStatement.setLong(1, lastWalLogDataId);
					preparedStatement.setLong(2, newWalLogDataId);
					rs = preparedStatement.executeQuery();
					List<String> statements = new ArrayList<String>();
					while (rs.next()) {
						statements.add(rs.getString("sql_statement"));
						//logger.info("statement added " + rs.getString("sql_statement"));
					}
		
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("data", statements);
					map.put("macId", macAddress);
					map.put("version", appVersion);
					map.put("maxWalLogDataId", newWalLogDataId);
					
					zipOtherOut.putNextEntry(new ZipEntry("sql_statements"));
					ObjectOutputStream outObj = new ObjectOutputStream(zipOtherOut);
					
					try {
						outObj.writeObject(map);
						zipOtherOut.closeEntry();
					}
					catch (Exception e) {
						logger.log(Level.SEVERE, e.toString(), e);
					}
					finally {
						if(outObj != null) {
							try {
								outObj.close();
							} catch (Exception e) {
								logger.log(Level.SEVERE, e.toString(), e);
							}
						}
					}
					logger.info("Compressed  data size = " + baos_other.toByteArray().length);
					ByteArrayPartSource baps_other = new ByteArrayPartSource("other", baos_other.toByteArray());
			        parts[0] = new FilePart("other", baps_other);
		
					PostMethod post = new PostMethod(
							"/ecloud/services/org/communicate/em/data");
					
					MultipartRequestEntity requestEntity = new MultipartRequestEntity(parts, post.getParams());
					post.setRequestEntity(requestEntity);
					
					HostConfiguration hc = new HostConfiguration();
					Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
					hc.setHost(host, 443, easyhttps);
					
					HttpClient client = new HttpClient();
					int status = client.executeMethod(hc, post);
					logger.info("status " + status);
					if(status == 200) {
						logger.info("sync successful.");
					}
					else {
						logger.info("SYNC FAILED!!!!!");
					}
				} catch (SQLException e) {
					logger.log(Level.SEVERE, e.toString(), e);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.toString(), e);
				}
				finally {
					if(statement != null) {
						try {
							statement.close();
						} catch (Exception e) {
							logger.log(Level.SEVERE, e.toString(), e);
						}
					}
					if(dltWalStmt != null) {
						try {
							dltWalStmt.close();
						} catch (Exception e) {
							logger.log(Level.SEVERE, e.toString(), e);
						}
					}
					if(preparedStatement != null) {
						try {
							preparedStatement.close();
						} catch (SQLException e) {
							logger.log(Level.SEVERE, e.toString(), e);
						}
					}
					if(connection != null) {
						try {
							connection.close();
						} catch (SQLException e) {
							logger.log(Level.SEVERE, e.toString(), e);
						}
					}
					if(zipOtherOut != null) {
						try {
							zipOtherOut.close();
						} catch (Exception e) {
							logger.log(Level.SEVERE, e.toString(), e);
						}
					}
					if(baos_other != null) {
						try {
							baos_other.close();
						} catch (Exception e) {
							logger.log(Level.SEVERE, e.toString(), e);
						}
					}
				}
			}
			else {
				logger.info("Could not connect to cloud server.");
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	
	}

	
	public static void sendDataCallHome(){
		
		logger.info("Starting new sync event at " + new Date());
		Connection connection = null;
		Statement statement = null;
		CloudRequest cloudrequest = new CloudRequest(macAddress, appVersion);
		ArrayList<NameValue> list = new ArrayList<NameValue>();
		cloudrequest.setNameval(list);
		
		try {
			TimeZone tz = Calendar.getInstance().getTimeZone();
			list.add(new NameValue(CloudParamType.EmTimezone, tz.getID()));
			list.add(new NameValue(CloudParamType.StatsEmAccessible, checkEMAcess()));
			
			connection = DriverManager
			.getConnection(
					"jdbc:postgresql://localhost:5433/ems?characterEncoding=utf-8",
					"postgres", "postgres");
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select id, capture_at, "
			+ "active_thread_count, gc_count, gc_time, "
			+ "heap_used, non_heap_used, sys_load, cpu_percentage "
			+ "from em_stats order by capture_at desc limit 1");
			
			if (rs.next()) {
				
				list.add(new NameValue(CloudParamType.StatsId, new Long(rs.getLong("id")).toString()));
				list.add(new NameValue(CloudParamType.StatsCaptureAt, new Long(rs.getTimestamp("capture_at").getTime()).toString()));
				list.add(new NameValue(CloudParamType.StatsActiveThreadCount, new Integer(rs.getInt("active_thread_count")).toString()));
				list.add(new NameValue(CloudParamType.StatsGcCount, new Long(rs.getLong("gc_count")).toString()));
				list.add(new NameValue(CloudParamType.StatsGcTime, new Long(rs.getLong("gc_time")).toString()));
				list.add(new NameValue(CloudParamType.StatsHeadUsed, rs.getBigDecimal("heap_used").toString()));
				list.add(new NameValue(CloudParamType.StatsNonHeapUsed, rs.getBigDecimal("non_heap_used").toString()));
				list.add(new NameValue(CloudParamType.StatsSysLoad, rs.getBigDecimal("sys_load").toString()));
				list.add(new NameValue(CloudParamType.StatsCpuPercentage, rs.getBigDecimal("cpu_percentage").toString()));
				
			}
			
			CloudHttpResponse response = executePost("/ecloud/services/org/communicate/em/callhome", JsonUtil.getJSONString(cloudrequest), host);
			JsonUtil<CloudResponse> jsonUtil = new JsonUtil<CloudResponse>();
			CloudResponse cloudresponse = jsonUtil.getObject(response.getResponse(), CloudResponse.class);

			
			if(cloudresponse.getStatus() != 0) {
				logger.info("SYNC MIGHT HAVE FAILED!!");
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		
		
		
	}
	
	
	public static void getCloudServerInfo() {
		try {
			File manifestFile = new File(
					"/var/lib/tomcat6/webapps/ems/META-INF/MANIFEST.MF");
			Manifest mf = new Manifest();
			mf.read(new FileInputStream(manifestFile));
			Attributes atts = mf.getAttributes("ems");
			if (atts != null) {
				appVersion = atts.getValue("Implementation-Version") + "."
						+ atts.getValue("Build-Version");
			}

			File drUserFile = new File(
					"/var/lib/tomcat6/Enlighted/cloudServerInfo.xml");
			if (drUserFile.exists()) {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(drUserFile.getAbsoluteFile());
				NodeList server = doc.getElementsByTagName("server");
				if (server != null && server.getLength() > 0) {
					NodeList each = server.item(0).getChildNodes();
					host = each.item(0).getFirstChild().getNodeValue();
					macAddress = each.item(1).getFirstChild().getNodeValue();
				}
				logger.info("Cloud Server Info & EM Mac Id = " + host + " " + macAddress);
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		} catch (SAXException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		} catch (ParserConfigurationException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
		
		
		CloudRequest cloudrequest = new CloudRequest(macAddress, appVersion);
		CloudHttpResponse response = executePost("/ecloud/services/org/communicate/em/cloudsyncstatus", JsonUtil.getJSONString(cloudrequest), host);
		JsonUtil<CloudResponse> jsonUtil = new JsonUtil<CloudResponse>();
		CloudResponse cloudresponse = jsonUtil.getObject(response.getResponse(), CloudResponse.class);
		HashMap<CloudParamType, String> respMap = cloudresponse.getNameValueMap();
		
		syncStatus = respMap.get(CloudParamType.EmCloudSyncStatus);
		
	}
	
	
	
	
	public static String getApiKey(){
		
		ResourceBundle resourcebundle = ResourceBundle.getBundle("key");
		String apiKey = resourcebundle.getString("ApiKey");
		
		return apiKey;
		
	}
	
	private static DefaultHttpClient getHttpClient() {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		FileInputStream instream = null;
		try {
			
			SSLSocketFactory socketFactory = TrustEverythingSSLSocketFactory.getSocketFactory();
			Scheme sch = new Scheme("https", 443, socketFactory);
					
			httpclient.getConnectionManager().getSchemeRegistry().register(sch);
			
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (instream != null) {
					instream.close();
				}
			} catch (Exception ignore) {
			}
		}
		return httpclient;
	}
	
	private static CloudHttpResponse executePost(String service, String stringPost ,String ip) {
		 CloudHttpResponse cloudResponse = null;
				DefaultHttpClient httpclient = getHttpClient();
				try {
					String requestLink = "https://" + ip+ service;
					HttpPost httpPost = new HttpPost(requestLink);
					StringEntity requestBodies = new StringEntity(stringPost);
					requestBodies.setContentType(MediaType.TEXT_PLAIN );
					httpPost.setEntity(requestBodies);
					httpPost.addHeader("apiKey", getApiKey());
					HttpResponse response = httpclient.execute(httpPost);
					
					cloudResponse = getCloudResponse(response);

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					// When HttpClient instance is no longer needed,
					// shut down the connection manager to ensure
					// immediate deallocation of all system resources
					httpclient.getConnectionManager().shutdown();
					return cloudResponse;
				}
			}
	
	private static CloudHttpResponse getCloudResponse(HttpResponse httpResponse) {
		CloudHttpResponse cloudResponse = new CloudHttpResponse();
		
		cloudResponse.setStatus(httpResponse.getStatusLine().getStatusCode());
		String responseString = null;
		
		try {
			//Read the content
			HttpEntity entity = httpResponse.getEntity();
			responseString = EntityUtils.toString(entity);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		cloudResponse.setResponse(responseString);
		return cloudResponse;
	}
	


}