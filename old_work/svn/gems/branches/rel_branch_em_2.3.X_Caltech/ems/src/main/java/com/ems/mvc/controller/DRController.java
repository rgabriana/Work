package com.ems.mvc.controller;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import sun.misc.BASE64Encoder;

import com.ems.model.DRUsers;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerMain;
import com.ems.service.DRTargetManager;
import com.ems.service.DRUserManager;
import com.ems.service.SystemConfigurationManager;

@Controller
@RequestMapping("/dr")
@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
public class DRController {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource(name = "drTargetManager")
    private DRTargetManager drTargetManager;
    @Resource(name = "drUserManager")
    private DRUserManager drUserManager;
    
    @Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	
	@RequestMapping("/listDR.ems")
    public String listDR(Model model) {
        model.addAttribute("drlist", drTargetManager.getAllManualDRTargets());
        return "dr/listDR";
    }
	
	@RequestMapping("/addUser.ems")
    public String addDRUser(Model model) {
		List<String> versionList = new ArrayList<String>();
		versionList.add("1.0");
		versionList.add("2.0");
		DRUsers drUser = drUserManager.getDRUser();
		model.addAttribute("druser", drUser);
		if(drUser.getKeystoreFileName() != null && !"".equals(drUser.getKeystoreFileName())){
			model.addAttribute("druserKeystoreAvailable", true);
		}else{
			model.addAttribute("druserKeystoreAvailable", false);
		}
		if(drUser.getTruststoreFileName() != null && !"".equals(drUser.getTruststoreFileName())){
			model.addAttribute("druserTruststoreAvailable", true);
		}else{
			model.addAttribute("druserTruststoreAvailable", false);
		}
		model.addAttribute("druserKeystoreFileName", drUser.getKeystoreFileName());
		model.addAttribute("druserTruststoreFileName", drUser.getTruststoreFileName());
		model.addAttribute("versionList", versionList);
		model.addAttribute("drPollTimeInterval", systemConfigurationManager
				.loadConfigByName("dr.minimum.polltimeinterval").getValue());
        return "dr/addDRUser";
    }
	
	@RequestMapping(value = "/registerUserWithoutCertificate.ems",  method = RequestMethod.POST)
	public String registerUserWithoutCertificate(@ModelAttribute("druser") DRUsers druser) {
		
		boolean isDRCredentailValid = true;
		//VALIDATE DR USERNAME/PASSWORD
		//isDRCredentailValid = isValidDRUser(druser);
		if(isDRCredentailValid)
		{
			if(drUserManager.save(druser) != false)
			{
				return "redirect:/dr/addUser.ems?status=S";
			}
		}
		else
		{
			return "redirect:/dr/addUser.ems?status=E";
		}
		return null;
	}
	
	@RequestMapping(value = "/registerUserWithCertificate.ems",  method = RequestMethod.POST)
	public String registerUserWithCertificate(@RequestParam("keystoreCertificate") MultipartFile keystoreCertificateFile,
			@RequestParam("truststoreCertificate") MultipartFile truststoreCertificateFile,
			@ModelAttribute("druser") DRUsers druser) {
		
		boolean isDRCredentailValid = true;
		//VALIDATE DR USERNAME/PASSWORD
		//isDRCredentailValid = isValidDRUser(druser);
		if(isDRCredentailValid)
		{
			String enLightedADRcertsFolder = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/adr/certs/";
	        File oENLADRFolder = new File(enLightedADRcertsFolder);
	        if (!oENLADRFolder.exists()) {
	        	oENLADRFolder.mkdirs();
	        }
	   		
	   		if (!keystoreCertificateFile.isEmpty()) {
	   			try {
					drUserManager.uploadCertificateFile(enLightedADRcertsFolder,druser.getKeystoreFileName(),keystoreCertificateFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	   		}
	   		
	   		if (!truststoreCertificateFile.isEmpty()) {
	   			try {
					drUserManager.uploadCertificateFile(enLightedADRcertsFolder,druser.getTruststoreFileName(),truststoreCertificateFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	   		}
	   		
			
			if(drUserManager.save(druser) != false)
			{
				return "redirect:/dr/addUser.ems?status=S";
			}
		}
		else
		{
			return "redirect:/dr/addUser.ems?status=E";
		}
		return null;
	}
	
	public boolean isValidDRUser(DRUsers druser)
	{
		  try {
	            //String confirmEndPoint = "http://cdp.openadr.com/RestClientWS/nossl/restConfirm";
	            URLConnection connection = new URL(druser.getServer()).openConnection();
	            // String plain = "enlighted.1:Test_1234";
	            String plain = druser.getName() + ":" + druser.getPassword();
	            String enocoded = new BASE64Encoder().encode(plain.getBytes());
	            connection.setRequestProperty("Authorization", "Basic " + enocoded);
	            // create the dom
	            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	            Document document = builder.parse(connection.getInputStream());
	            if(document!=null)
	            	return true;

	        } catch (NullPointerException npe) {
	            npe.printStackTrace();
	            return false;
	        } catch (Exception e) {
	        	e.printStackTrace();
	            return false;
	        }
	        return true;
	}

}
