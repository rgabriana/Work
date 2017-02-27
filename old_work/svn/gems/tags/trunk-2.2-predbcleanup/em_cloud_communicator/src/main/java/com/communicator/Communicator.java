package com.communicator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.protocol.Protocol;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.communicator.communication.EasySSLProtocolSocketFactory;
import com.communicator.diff.FacilityDiffFinder;
import com.communicator.json.JsonMapper;
import com.communicator.model.vo.AreaVO;
import com.communicator.model.vo.BuildingVO;
import com.communicator.model.vo.CampusVO;
import com.communicator.model.vo.ClientToServerVO;
import com.communicator.model.vo.CompanyVO;
import com.communicator.model.vo.FixtureVO;
import com.communicator.model.vo.FloorVO;
import com.communicator.model.vo.GatewayVO;
import com.communicator.spring.SpringApplicationContext;


public class Communicator {
	
	private static String appVersion = "2.2.0";
	private static String macAddress = "23:34:45";
	private static String host = "localhost";
	private static Integer port = 8443;
	private static long sleepTime = 60 * 1000;
	
	public static void main(String[] args) {
		
        getCloudServerInfo();
        
        while(true) {
        	try {
        		if(host != null && port != null && macAddress != null) {
        			pollServer();
        		}
        		else {
        			getCloudServerInfo();
        		}
        		Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }

	}
	
	public static void getCloudServerInfo() {
		try {
            File manifestFile = new File("/var/lib/tomcat6/webapps/ems/META-INF/MANIFEST.MF");
            Manifest mf = new Manifest();
            mf.read(new FileInputStream(manifestFile));
            Attributes atts = mf.getAttributes("ems");
            if (atts != null) {
                appVersion = atts.getValue("Implementation-Version") + "." + atts.getValue("Build-Version");
            }
            
            File drUserFile = new File("/var/lib/tomcat6/Enlighted/cloudServerInfo.xml");
            if(drUserFile.exists()) {
            		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    				Document doc = docBuilder.parse(drUserFile.getAbsoluteFile());
    				NodeList server = doc.getElementsByTagName("server");
    				if(server != null && server.getLength() > 0) {
    					NodeList each = server.item(0).getChildNodes();
    					host = each.item(0).getFirstChild().getNodeValue();
    					port = Integer.parseInt(each.item(1).getFirstChild().getNodeValue());
    					macAddress = each.item(2).getFirstChild().getNodeValue();
					}
    				System.out.println(host + " " + port + " " + macAddress);
            }
		} catch (FileNotFoundException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        } catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

    public static void pollServer() {
    	
    	System.out.println(new Date());
    	
    	String filename = null;
		try {			
			File manifestFile = new File("/var/lib/tomcat6/Enlighted/CloudDataManifest.MF");
            Manifest mf = new Manifest();
            mf.read(new FileInputStream(manifestFile));
            Attributes atts = mf.getAttributes("CloudData");
            System.out.println(atts.getValue("CurrentTransactionStatus")) ;
            if (true) {
            	
            	System.out.println("Test");
        		List<CompanyVO> company = new ArrayList<CompanyVO>();
        		List<CampusVO> campus = new ArrayList<CampusVO>();
        		List<BuildingVO> building = new ArrayList<BuildingVO>();
        		List<FloorVO> floor = new ArrayList<FloorVO>();
        		List<AreaVO> area = new ArrayList<AreaVO>();
        		List<FixtureVO> fixture = new ArrayList<FixtureVO>();
        		List<GatewayVO> gateway= new ArrayList<GatewayVO>();

        		FacilityDiffFinder facilityDiffFinder = (FacilityDiffFinder)SpringApplicationContext.getBean("facilityDiffFinder");
        		facilityDiffFinder.getChangedData(company, campus, building, floor, area, fixture, gateway);

        		JsonMapper mapper = (JsonMapper) SpringApplicationContext.getBean("jsonMapper");

        		File file = new File("/tmp/json.file");
        		
        		ClientToServerVO dataObj = new ClientToServerVO(company, campus, building, floor, area, fixture, gateway, macAddress.trim(), appVersion);
        		mapper.mapObjectsToJson(file, dataObj);
        		
    			BufferedReader br = null;
    			Runtime rt = Runtime.getRuntime();
    			Process pr = null;
    			pr = rt.exec( new String[] { "txn_and_zip.sh", "new" });
    			pr.waitFor();
    			br = new BufferedReader(new InputStreamReader(
    					pr.getInputStream()));
    			filename = "/tmp/" + br.readLine();
    			
    			file = new File(filename);
				
    			
    			//TODO: Use AuthSSLProtocolSocketFactory instead of EasySSLProtocolSocketFactory
    			/*Protocol authhttps = new Protocol("https",  new AuthSSLProtocolSocketFactory(
   					 new URL("file:my.keystore"), "mypassword",
   					 new URL("file:my.truststore"), "mypassword"), port); 
				PostMethod post = new PostMethod("/ecloud/services/org/communicate/em/data");
				//post.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
				
				FilePart filePart = new FilePart("file", file);
				filePart.setContentType("application/gzip");
				Part[] parts = {filePart};
				MultipartRequestEntity request = new MultipartRequestEntity(parts, post.getParams());
				post.setRequestEntity(request);
				
				HttpClient client = new HttpClient();
				client.getHostConfiguration().setHost(host, port, authhttps);
				
				System.out.println(client.executeMethod(post));*/
				
    			
				//TODO: Temporary Code
				Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), port);
				PostMethod post = new PostMethod("/ecloud/services/org/communicate/em/data");
				//post.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
				
				FilePart filePart = new FilePart("file", file);
				filePart.setContentType("application/gzip");
				Part[] parts = {filePart};
				MultipartRequestEntity request = new MultipartRequestEntity(parts, post.getParams());
				post.setRequestEntity(request);
				
				HostConfiguration hc = new HostConfiguration();
				hc.setHost(host, port, easyhttps);
				HttpClient client = new HttpClient();
				System.out.println(client.executeMethod(hc, post));
				
				
            }

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	
}
