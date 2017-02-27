package com.ems.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
                    cloudServerInfo.setServerPort(server.item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeValue());
                    cloudServerInfo.setEmMac(server.item(0).getChildNodes().item(2).getChildNodes().item(0).getNodeValue());
                }
            
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } 
    	}
        return cloudServerInfo;
    }
    
    
    public String saveCloudServerInformation(CloudServerInfo cloudServerInfo){
        
    	String status = "F";        
    	File cloudServerInfoFile = new File(ServerMain.getInstance().getTomcatLocation() + cloudServerInfoFilePath);
    	
        if(!cloudServerInfoFile.exists()) {
        	Writer output = null;
        	try {
				output = new BufferedWriter(new FileWriter(cloudServerInfoFile));
				output.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><server>" + 
								"<serverIp>" + cloudServerInfo.getServerIp() + "</serverIp>" +
								"<serverPort>" + cloudServerInfo.getServerPort() +"</serverPort>" +
								"<emMac>" + cloudServerInfo.getEmMac() + "</emMac></server>");
			} catch (IOException e) {
				e.printStackTrace();
				return status;
			}
        	finally {
        		if(output != null) {
        			try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
						return status;
					}
        		}
        	}
        }
        else {
        	try {
        		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(cloudServerInfoFile.getAbsoluteFile());
				NodeList server = doc.getElementsByTagName("server");
				if(server != null && server.getLength() > 0) {
					
					Node serverIpNode = server.item(0).getChildNodes().item(0);
					Node serverPortNode = server.item(0).getChildNodes().item(1);
					Node emMacNode = server.item(0).getChildNodes().item(2);
					serverIpNode.getChildNodes().item(0).setNodeValue(cloudServerInfo.getServerIp());
					serverPortNode.getChildNodes().item(0).setNodeValue(cloudServerInfo.getServerPort());
					emMacNode.getChildNodes().item(0).setNodeValue(cloudServerInfo.getEmMac());
					
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, "no");
					StreamResult result = new StreamResult(cloudServerInfoFile);
					DOMSource source = new DOMSource(doc);
					transformer.transform(source, result);
					
				}
				else {
					Writer output = null;
		        	try {
						output = new BufferedWriter(new FileWriter(cloudServerInfoFile));
						output.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><server>" + 
								"<serverIp>" + cloudServerInfo.getServerIp() + "</serverIp>" +
								"<serverPort>" + cloudServerInfo.getServerPort() +"</serverPort>" +
								"<emMac>" + cloudServerInfo.getEmMac() + "</emMac></server>");
					} catch (IOException e) {
						e.printStackTrace();
						return status;
					}
		        	finally {
		        		if(output != null) {
		        			try {
								output.close();
							} catch (IOException e) {
								e.printStackTrace();
								return status;
							}
		        		}
		        	}
				}
			
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
        }
        return "S";
    }

}
