package com.ems.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ems.model.CloudServerInfo;
import com.ems.server.ServerMain;

@Service("cloudServerManager")
@Transactional(propagation=Propagation.REQUIRED)
public class CloudServerManager {
	
	private String cloudServerInfoFilePath = "../../Enlighted/cloudServerInfo.xml";
	
    public CloudServerInfo getCloudServerInformation(){
        
    	CloudServerInfo cloudServerInfo = new CloudServerInfo();
        
        File cloudServerInfoFile = new File(ServerMain.getInstance().getTomcatLocation() + cloudServerInfoFilePath);
        if(cloudServerInfoFile.exists()) {
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(cloudServerInfoFile.getAbsoluteFile());
                NodeList server = doc.getElementsByTagName("server");
                if(server != null && server.getLength() > 0) {
                    cloudServerInfo.setServerIp(server.item(0).getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
                    cloudServerInfo.setEmMac(server.item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeValue());
                }
            
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (Exception e) {
            	e.printStackTrace();
            }
    	}
        return cloudServerInfo;
    }
    
    
    public String saveCloudServerInformation(CloudServerInfo cloudServerInfo){
        
    	String status = "F";        
    	File cloudServerInfoFile = new File(ServerMain.getInstance().getTomcatLocation() + cloudServerInfoFilePath);
    	
 
    	try {
    		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(cloudServerInfoFile.getAbsoluteFile());
			NodeList server = doc.getElementsByTagName("server");

				
			Node serverIpNode = server.item(0).getChildNodes().item(0);
			serverIpNode.getChildNodes().item(0).setNodeValue(cloudServerInfo.getServerIp());
			
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			StreamResult result = new StreamResult(cloudServerInfoFile);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

		} catch (SAXException e) {
			e.printStackTrace();
			return status;
		} catch (IOException e) {
			e.printStackTrace();
			return status;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return status;
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return status;
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
			return status;
		} catch (TransformerException e) {
			e.printStackTrace();
			return status;
		}
        return "S";
    }
    
    public void populateEmMac() {
    	
    	CloudServerInfo cloudInfo = getCloudServerInformation();
    	if(cloudInfo.getEmMac() == null || "".equals(cloudInfo.getEmMac().trim()) || "443".equals(cloudInfo.getEmMac().trim())) {
        
        	File cloudServerInfoFile = new File(ServerMain.getInstance().getTomcatLocation() + cloudServerInfoFilePath);
        	     
        	try {
        		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    			Document doc = docBuilder.parse(cloudServerInfoFile.getAbsoluteFile());
    			NodeList server = doc.getElementsByTagName("server");
    			String macId = "443";
    			
    			Runtime rt = Runtime.getRuntime();
    			Process proc = rt.exec(new String[]{"/bin/bash", "/var/lib/tomcat6/webapps/ems/adminscripts/getMac.sh"});
    			proc.waitFor();
				BufferedReader outputStream = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				String output = null;

				while ((output = outputStream.readLine()) != null) {
					macId = output.trim();
					break;
				}

    			Node macNode = server.item(0).getChildNodes().item(1);
    			macNode.getChildNodes().item(0).setNodeValue(macId);
    			
    			
    			Transformer transformer = TransformerFactory.newInstance().newTransformer();
    			transformer.setOutputProperty(OutputKeys.INDENT, "no");
    			StreamResult result = new StreamResult(cloudServerInfoFile);
    			DOMSource source = new DOMSource(doc);
    			transformer.transform(source, result);

    		} catch (SAXException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} catch (ParserConfigurationException e) {
    			e.printStackTrace();
    		} catch (TransformerConfigurationException e) {
    			e.printStackTrace();
    		} catch (TransformerFactoryConfigurationError e) {
    			e.printStackTrace();
    		} catch (TransformerException e) {
    			e.printStackTrace();
    		} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }

}
