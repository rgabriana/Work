package com.communicator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.protocol.Protocol;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.communicator.communication.EasySSLProtocolSocketFactory;

public class Communicator {

	private static String appVersion = null;
	private static String macAddress = null;
	private static String host = null;
	private static Integer port = null;
	private static Integer emInstance = -1;
	private static long sleepTime = 150 * 1000;
	
	static final Logger logger = Logger.getLogger(Communicator.class.getName());
    static {
        try {
            logger.setLevel(Level.INFO); 
            LogManager lm = LogManager.getLogManager();
            lm.addLogger(logger);
        }
        catch (Throwable e) {
        	logger.log(Level.SEVERE, e.toString(), e);
        }
    }

	public static void main(String[] args) {

		getCloudServerInfo();

		while (true) {
			try {
				if (host != null && port != null && macAddress != null && emInstance != -1) {
					sendData();
				} else {
					getCloudServerInfo();
				}
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, e.toString(), e);
			}
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
					port = Integer.parseInt(each.item(1).getFirstChild()
							.getNodeValue());
					macAddress = each.item(2).getFirstChild().getNodeValue();
				}
				logger.info("Cloud Server Info & EM Mac Id = " + host + " " + port + " " + macAddress);
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
		
		// TODO: Use AuthSSLProtocolSocketFactory instead of
		// EasySSLProtocolSocketFactory
		/*
		 * Protocol authhttps = new Protocol("https", new
		 * AuthSSLProtocolSocketFactory( new URL("file:my.keystore"),
		 * "mypassword", new URL("file:my.truststore"), "mypassword"),
		 * port); PostMethod post = new
		 * PostMethod("/ecloud/services/org/communicate/em/data");
		 * //post.getParams().setBooleanParameter(HttpMethodParams.
		 * USE_EXPECT_CONTINUE, true);
		 * 
		 * FilePart filePart = new FilePart("file", file);
		 * filePart.setContentType("application/gzip"); Part[] parts =
		 * {filePart}; MultipartRequestEntity request = new
		 * MultipartRequestEntity(parts, post.getParams());
		 * post.setRequestEntity(request);
		 * 
		 * HttpClient client = new HttpClient();
		 * client.getHostConfiguration().setHost(host, port, authhttps);
		 * 
		 * System.out.println(client.executeMethod(post));
		 */

		// TODO: Temporary Code
		// Get EM ID from the cloud for this EM
		Protocol easyhttps = new Protocol("https",
				new EasySSLProtocolSocketFactory(), port);
		GetMethod get = new GetMethod(
				"/ecloud/services/org/communicate/em/info/" + macAddress);

		HostConfiguration hc = new HostConfiguration();
		hc.setHost(host, port, easyhttps);
		HttpClient client = new HttpClient();
		try {
			client.executeMethod(hc, get);
			byte[] responseBody = get.getResponseBody();
			emInstance = Integer.parseInt(new String(responseBody));
			logger.info("EM ID is " + emInstance);
			if (emInstance == -1) {
				logger.severe("EM not registered with Cloud server. Please do so.");
			}
		} catch (HttpException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}

	}
	
	private static long getLastWalSyncId() {
				long lastWalSyncId = -100; 
				Protocol easyhttps = new Protocol("https",
						new EasySSLProtocolSocketFactory(), port);
				GetMethod get = new GetMethod(
						"/ecloud/services/org/communicate/em/lastWalSynced/" + macAddress);

				HostConfiguration hc = new HostConfiguration();
				hc.setHost(host, port, easyhttps);
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


	public static void sendData() {
		
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
					Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), port);
					hc.setHost(host, port, easyhttps);
					
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
}