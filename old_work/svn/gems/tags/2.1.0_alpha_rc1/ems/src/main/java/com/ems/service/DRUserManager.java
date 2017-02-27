/**
 * 
 */
package com.ems.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.annotation.Resource;
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

import com.ems.dao.DRUserDao;
import com.ems.model.DRUsers;
import com.ems.server.ServerMain;

/**
 * @author yogesh
 * 
 */
@Service("drUserManager")
@Transactional(propagation=Propagation.REQUIRED)
public class DRUserManager {
	
	@Resource
    private DRUserDao drUserDao;

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.DRUserManager#save(com.ems.model.DRUser)
     */

    public DRUsers save(DRUsers user, String server ) {
    	List<DRUsers> oDRUsers = loadAllDRUsers();
        DRUsers oDRUser = null;
        if (oDRUsers != null && oDRUsers.size() > 0) {
                oDRUser = oDRUsers.get(0);
        } else {
                oDRUser = new DRUsers();
        }
        oDRUser.setName(user.getName());
        oDRUser.setPassword(user.getPassword());
        
        File drUserFile = new File(ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/openADRSeverConfig.xml");
        if(!drUserFile.exists()) {
        	Writer output = null;
        	try {
				output = new BufferedWriter(new FileWriter(drUserFile));
				output.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><root><users><server>" +server + "</server><user>" + user.getName()+"</user></users></root>");
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
        	finally {
        		if(output != null) {
        			try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
        		}
        	}
        }
        else {
        	try {
        		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(drUserFile.getAbsoluteFile());
				NodeList users = doc.getElementsByTagName("users");
				if(users != null && users.getLength() > 0) {
					Node each = users.item(0);
					Node serverNode = each.getChildNodes().item(0);
					Node userNode = each.getChildNodes().item(1);
					serverNode.getChildNodes().item(0).setNodeValue(server);
					userNode.getChildNodes().item(0).setNodeValue(user.getName());
					
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, "no");
					StreamResult result = new StreamResult(drUserFile);
					DOMSource source = new DOMSource(doc);
					transformer.transform(source, result);
					
				}
				else {
					Writer output = null;
		        	try {
						output = new BufferedWriter(new FileWriter(drUserFile));
						output.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><root><users><server>" +server + "</server><user>" + user.getName()+"</user></users></root>");
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
		        	finally {
		        		if(output != null) {
		        			try {
								output.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
		        		}
		        	}
				}
			
			} catch (SAXException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				return null;
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
				return null;
			} catch (TransformerFactoryConfigurationError e) {
				e.printStackTrace();
				return null;
			} catch (TransformerException e) {
				e.printStackTrace();
				return null;
			}
        }
        return (DRUsers) drUserDao.saveObject(oDRUser);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.DRUserManager#update(com.ems.model.DRUser)
     */

    public DRUsers update(DRUsers user) {
        return (DRUsers) drUserDao.saveObject(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.DRUserManager#loadAllDRUsers()
     */

    public List<DRUsers> loadAllDRUsers() {
        return drUserDao.loadAllDRUsers();
    }

}
