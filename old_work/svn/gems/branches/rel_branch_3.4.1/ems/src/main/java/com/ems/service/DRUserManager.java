/**
 * 
 */
package com.ems.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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

    public boolean save(DRUsers user) {
    	DRUsers oDRUser = new DRUsers();
        oDRUser.setName(user.getName());
        oDRUser.setPassword(user.getPassword());
        
        File drUserFile = new File(ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/openADRSeverConfig.xml");
    	Writer output = null;
    	try {
			output = new BufferedWriter(new FileWriter(drUserFile));
			
			if("1.0".equals(user.getVersion())){
				output.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><root><users>" +
						"<server>" +user.getServer() + "</server>" +
						"<user>" + user.getName()+"</user>" +
						"<password>" + user.getPassword() + "</password>" +
						"<timeInterval>" + user.getTimeInterval() + "</timeInterval>" +
						"<venId>"+""+"</venId>" +
						"<marketContext1>"+""+"</marketContext1>" +
						"<marketContext2>"+""+"</marketContext2>" +
						"<marketContext3>"+""+"</marketContext3>" +
						"<vtnId1>"+""+"</vtnId1>" +
						"<vtnId2>"+""+"</vtnId2>" +
						"<vtnId3>"+""+"</vtnId3>" +
						"<version>" + user.getVersion() + "</version>" +
						"<keystoreFileName>"+""+"</keystoreFileName>" +
						"<keystorePassword>"+""+"</keystorePassword>" +
						"<truststoreFileName>"+""+"</truststoreFileName>" +
						"<truststorePassword>"+""+"</truststorePassword>" +
						"<prefix>"+""+"</prefix>" +
						"<servicepath>"+""+"</servicepath>" +
						"<connectionTimeout>" + "10000" + "</connectionTimeout>" +
						"<socketTimeout>" + "30000" + "</socketTimeout>" +
					"</users></root>");
			}else{
				output.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><root><users>" +
						"<server>" +user.getServer() + "</server>" +
						"<user>" + user.getName()+"</user>" +
						"<password>" + user.getPassword() + "</password>" +
						"<timeInterval>" + user.getTimeInterval() + "</timeInterval>" +
						"<venId>" + user.getVenId() + "</venId>" +
						"<marketContext1>" + user.getMarketcontext1() + "</marketContext1>" +
						"<marketContext2>" + user.getMarketcontext2() + "</marketContext2>" +
						"<marketContext3>" + user.getMarketcontext3() + "</marketContext3>" +
						"<vtnId1>" + user.getVtnId1() + "</vtnId1>" +
						"<vtnId2>" + user.getVtnId2() + "</vtnId2>" +
						"<vtnId3>" + user.getVtnId3() + "</vtnId3>" +
						"<version>" + user.getVersion() + "</version>" +
						"<keystoreFileName>" + user.getKeystoreFileName() + "</keystoreFileName>" +
						"<keystorePassword>" + user.getKeystorePassword() + "</keystorePassword>" +
						"<truststoreFileName>" + user.getTruststoreFileName() + "</truststoreFileName>" +
						"<truststorePassword>" + user.getTruststorePassword() + "</truststorePassword>" +
						"<prefix>" + user.getPrefix() + "</prefix>" +
						"<servicepath>" + user.getServicepath() + "</servicepath>" +
						"<connectionTimeout>" + "10000" + "</connectionTimeout>" +
						"<socketTimeout>" + "30000" + "</socketTimeout>" +
					"</users></root>");
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
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
        return true;
    }
    
    public void uploadCertificateFile(String path, String fileName, MultipartFile file) throws IOException{
		File certificateStore = new File(path);
		if(!certificateStore.exists()){
			certificateStore.mkdirs();
		}
		
		File certificateFile=new File(path, fileName);
		
		byte[] bytes = file.getBytes();
		FileOutputStream fos = new FileOutputStream(certificateFile);
		fos.write(bytes);
		fos.flush();
		fos.close();
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
    
    
    public DRUsers getDRUser( ){
        
        DRUsers oDRUser ;
        
        File drUserFile = new File(ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/openADRSeverConfig.xml");
        if(!drUserFile.exists()) {
            oDRUser = new DRUsers();
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
                    
                    String server = serverNode.getChildNodes().item(0).getNodeValue();
                    
                    String user = "";
                    
                    if(each.getChildNodes().item(1).hasChildNodes()){
                    	user = userNode.getChildNodes().item(0).getNodeValue();
                    }
                    
                    
                    oDRUser = new DRUsers();
                    oDRUser.setName(user);
                    oDRUser.setServer(server);
                    
            		if (each.getChildNodes().getLength() > 3) {
            			if(each.getChildNodes().item(3).hasChildNodes()) {
            				String timeInterval = each.getChildNodes().item(3).getChildNodes().item(0).getNodeValue();
                        	oDRUser.setTimeInterval((timeInterval == null || "".equals(timeInterval)) ? null : new Long(timeInterval) );	
            			}
            			if(each.getChildNodes().getLength() > 4 && each.getChildNodes().item(4).hasChildNodes()) {
            				oDRUser.setVenId(each.getChildNodes().item(4).getChildNodes().item(0).getNodeValue());
            			}
                    	if(each.getChildNodes().getLength() > 5 && each.getChildNodes().item(5).hasChildNodes()) {
                    		oDRUser.setMarketcontext1(each.getChildNodes().item(5).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 6 && each.getChildNodes().item(6).hasChildNodes()) {
                    		oDRUser.setMarketcontext2(each.getChildNodes().item(6).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 7 && each.getChildNodes().item(7).hasChildNodes()) {
                    		oDRUser.setMarketcontext3(each.getChildNodes().item(7).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 8 && each.getChildNodes().item(8).hasChildNodes()) {
                    		oDRUser.setVtnId1(each.getChildNodes().item(8).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 9 && each.getChildNodes().item(9).hasChildNodes()) {
                    		oDRUser.setVtnId2(each.getChildNodes().item(9).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 10 && each.getChildNodes().item(10).hasChildNodes()) {
                    		oDRUser.setVtnId3(each.getChildNodes().item(10).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 11 && each.getChildNodes().item(11).hasChildNodes()) {
                    		oDRUser.setVersion(each.getChildNodes().item(11).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 12 && each.getChildNodes().item(12).hasChildNodes()) {
                    		oDRUser.setKeystoreFileName(each.getChildNodes().item(12).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 13 && each.getChildNodes().item(13).hasChildNodes()) {
                    		oDRUser.setKeystorePassword(each.getChildNodes().item(13).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 14 && each.getChildNodes().item(14).hasChildNodes()) {
                    		oDRUser.setTruststoreFileName(each.getChildNodes().item(14).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 15 && each.getChildNodes().item(15).hasChildNodes()) {
                    		oDRUser.setTruststorePassword(each.getChildNodes().item(15).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 16 && each.getChildNodes().item(16).hasChildNodes()) {
                    		oDRUser.setPrefix(each.getChildNodes().item(16).getChildNodes().item(0).getNodeValue());
                    	}
                    	if(each.getChildNodes().getLength() > 17 && each.getChildNodes().item(17).hasChildNodes()) {
                    		oDRUser.setServicepath(each.getChildNodes().item(17).getChildNodes().item(0).getNodeValue());
                    	}
                    }
                    
                }
                else {
                    oDRUser = new DRUsers();  
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
               } 
            }
        
            return oDRUser;
        }

}
