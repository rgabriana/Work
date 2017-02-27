package com.ems.mvc.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import sun.misc.BASE64Encoder;

import com.ems.model.DRUsers;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.DRTargetManager;
import com.ems.service.DRUserManager;

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
	
	@RequestMapping("/listDR.ems")
    public String listDR(Model model) {
        model.addAttribute("drlist", drTargetManager.getAllDRTargets());
        return "dr/listDR";
    }
	
	@RequestMapping("/addUser.ems")
    public String addDRUser(Model model) {
		model.addAttribute("druser", drUserManager.getDRUser());
        return "dr/addDRUser";
    }
	
	@RequestMapping(value = "/registerUser.ems",  method = RequestMethod.POST)
	public String registerUser(@RequestParam("newPassword") String password, @RequestParam("server") String server, @ModelAttribute("druser") DRUsers druser) {
		druser.setPassword(password);
		//VALIDATE DR USERNAME/PASSWORD
		boolean isDRCredentailValid = isValidDRUser(druser);
		if(isDRCredentailValid)
		{
			if(drUserManager.save(druser, server) != null)
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
