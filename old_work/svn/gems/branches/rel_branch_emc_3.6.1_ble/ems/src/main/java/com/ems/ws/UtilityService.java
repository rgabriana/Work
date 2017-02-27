package com.ems.ws;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import com.ems.action.SpringContext;
import com.ems.model.Company;
import com.ems.model.Gateway;
import com.ems.model.NetworkInterfaceMapping;
import com.ems.model.SystemConfiguration;
import com.ems.model.Title24;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.server.ServerMain;
import com.ems.service.CompanyManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.GroupManager;
import com.ems.service.MetaDataManager;
import com.ems.service.NetworkSettingsManager;
import com.ems.service.ProfileManager;
import com.ems.service.ProfileTemplateManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.Title24Manager;
import com.ems.service.UserManager;
import com.ems.tags.Title24PageNumber;
import com.ems.tags.Title24PageNumberStartCounter;
import com.ems.types.NetworkType;
import com.ems.util.Constants;
import com.ems.utils.AdminUtil;
import com.lowagie.text.Document;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;

@Controller
@Path("/org/utility")
public class UtilityService {
	private static final Logger logger = Logger.getLogger("SysLog");

	@Context
	private HttpServletRequest httpRequest;
	@Context
	private HttpServletResponse httpResponse;
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	@Autowired
	protected MessageSource messageSource;
	@Resource
	UserManager userManager;
	@Resource
	SystemConfigurationManager systemConfigurationManager;

	@Resource(name = "groupManager")
	private GroupManager groupManager;
	@Resource(name = "profileManager")
	private ProfileManager profileManager;
	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;
	@Resource(name = "companyManager")
	private CompanyManager companyManager;
	@Resource(name = "metaDataManager")
	private MetaDataManager metaDataManager;
	@Resource(name = "profileTemplateManager")
	private ProfileTemplateManager profileTemplateManager;

	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	@Resource 
    private Title24Manager title24Manager;
	@Resource
    private NetworkSettingsManager networkSettingsManager;
	
	 @Resource(name = "gatewayManager")
	    private GatewayManager gatewayManager;

	// @Resource(name = "viewResolver")
	@Autowired
	private WebApplicationContext context;
	private ViewResolver viewResolver;

	
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	@Path("generatetitle24report")
	@RequestMapping(value="/generatetitle24report")
	public Response generatepdfitextYAHP() throws Exception{
		try {
			final Authentication authedUser = SecurityContextHolder
					.getContext().getAuthentication();
			if (authedUser == null) {
				logger.error("Serious Issue: someone without logged in accessing the webservice");
				return Response
						.status(Response.Status.UNAUTHORIZED)
						.entity("someone without logged in accessing the webservice")
						.build();
			}
			Map<String, Object> model = new HashMap<String, Object>();

			// WebApplicationContext webApplicationContext =
			// WebApplicationContextUtils.getRequiredWebApplicationContext(httpRequest.getServletContext());
			viewResolver = (ViewResolver) context.getBean("viewResolver");

			if (viewResolver == null) {
				return Response.status(Response.Status.NO_CONTENT)
						.entity("view resolver itself is null").build();
			}
			String viewToRender = title24ViewName(model, 1l, 2l);
			
			final View view = viewResolver.resolveViewName(viewToRender,
					LocaleContextHolder.getLocale());
			if (view == null) {
				return Response.status(Response.Status.NO_CONTENT)
						.entity("view is null").build();
			}

			final HttpServletResponseWrapper responseWrapper = getResponseWrapper(httpResponse);
			view.render(model, httpRequest, responseWrapper);
			
			String html = responseWrapper.toString();
			
			//For header
			String headerViewToRender = title24HeaderViewName(model);
			final View headerview = viewResolver.resolveViewName(headerViewToRender,
					LocaleContextHolder.getLocale());
			if (headerview == null) {
				return Response.status(Response.Status.NO_CONTENT)
						.entity("Header view is null").build();
			}
			final HttpServletResponseWrapper headerresponseWrapper = getResponseWrapper(httpResponse);
			headerview.render(model, httpRequest, headerresponseWrapper);
			String htmlHeader = headerresponseWrapper.toString();
			htmlHeader = AdminUtil.replaceAPatternInString(";jsessionid.*?\"","\"", htmlHeader);
			htmlHeader = AdminUtil.replaceAPatternInString("/ems/",Constants.ENL_APP_HOME+"/webapps/ems/", htmlHeader);
			final String tempFheader = "/tmp/file-"+authedUser.getName()+"-header.html";
			FileUtils.writeStringToFile(new File(tempFheader), htmlHeader);
			//Header completed
			
			//For footer
			String footerViewToRender = title24footerViewName(model);
			final View footerview = viewResolver.resolveViewName(footerViewToRender,
					LocaleContextHolder.getLocale());
			if (footerview == null) {
				return Response.status(Response.Status.NO_CONTENT)
						.entity("footer view is null").build();
			}
			final HttpServletResponseWrapper footerresponseWrapper = getResponseWrapper(httpResponse);
			footerview.render(model, httpRequest, footerresponseWrapper);
			String htmlfooter = footerresponseWrapper.toString();
			htmlfooter = AdminUtil.replaceAPatternInString(";jsessionid.*?\"","\"", htmlfooter);
			htmlfooter = AdminUtil.replaceAPatternInString("/ems/",Constants.ENL_APP_HOME+"/webapps/ems/", htmlfooter);
			final String tempFfooter = "/tmp/file-"+authedUser.getName()+"-footer.html";
			FileUtils.writeStringToFile(new File(tempFfooter), htmlfooter);
			//Header completed
			
			
			//File f = new File("/tmp/file-"+authedUser.getName()+".html");
			//html = FileUtils.readFileToString(f);
			html = AdminUtil.replaceAPatternInString(";jsessionid.*?\"","\"", html);
			html = AdminUtil.replaceAPatternInString("/ems/",Constants.ENL_APP_HOME+"/webapps/ems/", html);
			final long ctime = System.currentTimeMillis();
			final String tempF = "/tmp/file-"+authedUser.getName()+".html";
			FileUtils.writeStringToFile(new File(tempF), html);
			final String pdfFile = "/tmp/file-"+authedUser.getName()+".pdf";
			
            
            // Call shell script and give it these 2 file paths'
			AdminUtil.generatePdfFromHtmlFilePath(tempF, pdfFile,tempFheader, tempFfooter);
            
            final ByteArrayOutputStream bos = getByteArrayOutputStream(pdfFile);
			httpResponse.setHeader("Expires", "0");
		    httpResponse.setHeader("Cache-Control",
					"must-revalidate, post-check=0, pre-check=0");
		    httpResponse.setHeader("Pragma", "public");
			// setting the content type
		    httpResponse.setContentType("application/pdf");
			// the contentlength
		    httpResponse.setContentLength(bos.size());
			// write ByteArrayOutputStream to the ServletOutputStream
			OutputStream os = httpResponse.getOutputStream();
			bos.writeTo(os);
			os.flush();
			os.close();
		    
		    return Response.ok().build();
		} catch (Exception e) {
		    logger.error("***EXCEPTION OCCURED****", e);
			return Response.serverError()
					.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}
	
	protected HttpServletResponseWrapper getResponseWrapper(
			HttpServletResponse response) {
		final HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(
				response) {
			private final StringWriter sw = new StringWriter();

			@Override
			public PrintWriter getWriter() throws IOException {
				return new PrintWriter(sw);
			}

			@Override
			public String toString() {
				return sw.toString();
			}
		};
		return responseWrapper;
	}

	private final String title24ViewName(Map<String, Object> model, Long id,
			Long groupId) {
		
		Integer pricingType = null;
        SystemConfigurationManager sysConfigManager = (SystemConfigurationManager)SpringContext.getBean("systemConfigurationManager");
    	SystemConfiguration pricingTypeConfig = sysConfigManager
        .loadConfigByName("enable.pricing");
    	if(pricingTypeConfig != null)
    	{    		
    		//1 for Fixed Pricing , 2 for Time Of Day Pricing    		
    		if ("1".equalsIgnoreCase(pricingTypeConfig
					.getValue())) {    			
    			pricingType = 1;
			}       
    		else if("2".equalsIgnoreCase(pricingTypeConfig
					.getValue()))
    		{
    			pricingType = 2;
    		}
    	}
        //ENL - 4179 End
    	Company company = companyManager.getAllCompanies().get(0);
    	if(pricingType!=null) company.setPricingType(pricingType);
		model.put("company", company);
		model.put("mode", "admin");
		ArrayList<String> list = new ArrayList<String>();
		NetworkInterfaceMapping nimCorporate = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Corporate.getName());
		String corporateMapping="eth0",buildingMapping="eth1";
		if(nimCorporate != null && nimCorporate.getNetworkSettings()!= null && nimCorporate.getNetworkSettings().getName() != null){
			corporateMapping = nimCorporate.getNetworkSettings().getName();
		}
		NetworkInterfaceMapping nimBuilding = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Building.getName());
		if(nimBuilding != null && nimBuilding.getNetworkSettings()!= null && nimBuilding.getNetworkSettings().getName()!= null){
			buildingMapping = nimBuilding.getNetworkSettings().getName();			
		}else{
			buildingMapping = corporateMapping;
		}
		list.add(ServerMain.getInstance().getIpAddress(corporateMapping));
		list.add(ServerMain.getInstance().getIpAddress(buildingMapping));
		list.add(ServerMain.getInstance().getSubnetMask(corporateMapping));
		list.add(ServerMain.getInstance().getDefaultGateway());
		
		list.add(ServerMain.getInstance().getSubnetMask(buildingMapping));
		model.put("system", list);
		model.put("dhcpPresent", ServerMain.getInstance().determineDHCP(corporateMapping));
        SystemConfiguration dhcpConfig = systemConfigurationManager.loadConfigByName("dhcp.enable");
        if (dhcpConfig != null) {
    		model.put("dhcpEnable", dhcpConfig.getValue());
        }
        List<Gateway> gwList = gatewayManager.loadAllGateways();
        if(gwList.isEmpty())
    		model.put("gatewaysPresent", "false");
        else
    		model.put("gatewaysPresent", "true");
        
       	final Title24 title24  = title24Manager.loadTitle24Details();
       	//TODO For 3.6 first drop as per discussion in the call no data from db is shown to UI.. REMOVE FOLLOWING CODE IF BACKEND TO ENABLE
       	final ObjectMapper mapper = new ObjectMapper();
       	try {
			//Title24 t24tmp = mapper.readValue("{\"compliance\":{\"flag\":\"true\"}}", Title24.class);
			//t24tmp.getCompliance().setFlag(title24.getCompliance().getFlag());
			model.put("title24", title24);
		} catch (Exception e) {
			logger.info("Test json not able to retrieve...",e);
		} 
       	
		return "title24/report";
	}
	
	
	private final String title24HeaderViewName(Map<String, Object> model) {
		Title24PageNumber.pageNum = 0;
		return "title24/header";
	}
	
	private final String title24footerViewName(Map<String, Object> model) {
		return "title24/footer";
	}
	
	
	
	/**
	 * Install wkhtmltopsf on linux using following commands (tested on 63 bit)
	 *  sudo apt-get -f install wkhtmltopdf
	 *  sudo apt-get -f install
	 *  sudo apt-get update
	 *  sudo apt-get install wkhtmltopdf
	 *  sudo apt-get install xvfb
	 *  
	 *  create a wkhtmltopdf.sh file in the /opt/tomcat/webapps/ems/adminscripts/. dir having following contents in it
		 	#!/bin/bash
			wkhtmltopdfpath=`which wkhtmltopdf`
			xvfb-run -a -s "-screen 0 640x480x16" $wkhtmltopdfpath "$@"
	 *  sudo chmod a+x /opt/tomcat/webapps/ems/adminscripts/wkhtmltopdf.sh
	 *  
	 *  Test a pdf using following command
	 *   sh /opt/tomcat/webapps/ems/adminscripts/wkhtmltopdf.sh http://www.google.com test.pdf
	 *  
	 *   refer flink for installation
	 *   http://stackoverflow.com/questions/9604625/wkhtmltopdf-cannot-connect-to-x-server
	 * @return
	 * @throws Exception
	 */
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	@Path("generatetitle24reportwkhtmltopdf")
	@RequestMapping(value="/generatetitle24reportwkhtmltopdf")
	public Response generatepdf() throws Exception {
		FileWriter fw  = null;
		BufferedWriter bw = null;
		try {
			Map<String, Object> model = new HashMap<String, Object>();

			// WebApplicationContext webApplicationContext =
			// WebApplicationContextUtils.getRequiredWebApplicationContext(httpRequest.getServletContext());
			viewResolver = (ViewResolver) context.getBean("viewResolver");

			if (viewResolver == null) {
				return Response.status(Response.Status.NO_CONTENT)
						.entity("view resolver itself is null").build();
			}
			String tempUrl = "https://127.0.0.1/ems/profile/fixturesetting.ems?fixtureId=1&groupId=2&ts=1455253770147";
			//tempUrl = title24ViewName(model, 1l, 2l);//testmethod(model, 1l, 2l);
			tempUrl = testmethod(model, 1l, 2l);

			final View view = viewResolver.resolveViewName(tempUrl,
					LocaleContextHolder.getLocale());
			if (view == null) {
				return Response.status(Response.Status.NO_CONTENT)
						.entity("view is null").build();
			}

			final HttpServletResponseWrapper responseWrapper = getResponseWrapper(httpResponse);
			view.render(model, httpRequest, responseWrapper);
			// httpRequest.getRequestDispatcher(tempUrl).include(httpRequest,responseWrapper);
			final Authentication authedUser = SecurityContextHolder
					.getContext().getAuthentication();
			if (authedUser == null) {
				logger.error("Serious Issue: someone without logged in accessing the webservice");
				return Response
						.status(Response.Status.UNAUTHORIZED)
						.entity("someone without logged in accessing the webservice")
						.build();
			}

			// Get the response string and save in the html
			// Write to a file from java
			final String fName = "/tmp/file-"+authedUser.getName()+".html";
			File file = new File(fName);
			logger.info("Pdf FilePath::"+file.getCanonicalPath());
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			bw.write(responseWrapper.toString());
			bw.close();
			bw = null;
			logger.info("ResponseWrapper:"+responseWrapper.toString());
			
			if(file == null || !file.exists()){
				return Response.serverError().status(Response.Status.NO_CONTENT).entity("Html file "+fName+" is not getting generated").build();
			}
			
			//Generate the pdf now using wkhtmltopdf utity 
			final String pdfFile = "/tmp/file-"+authedUser.getName()+".pdf";
			AdminUtil.generatePdfFromHtmlFilePath(fName, pdfFile, null, null);
			File pdf = new File(pdfFile);
			if (pdf == null || !pdf.exists()){
				return Response.serverError().status(Response.Status.NO_CONTENT).entity("Pdf file "+pdfFile+" is not getting generated").build();
			}
			//Return generate pdf in the Response
			
			final ByteArrayOutputStream bos = getByteArrayOutputStream(pdfFile);
			httpResponse.setHeader("Expires", "0");
		    httpResponse.setHeader("Cache-Control",
					"must-revalidate, post-check=0, pre-check=0");
		    httpResponse.setHeader("Pragma", "public");
			// setting the content type
		    httpResponse.setContentType("application/pdf");
			// the contentlength
		    httpResponse.setContentLength(bos.size());
			// write ByteArrayOutputStream to the ServletOutputStream
			OutputStream os = httpResponse.getOutputStream();
			bos.writeTo(os);
			os.flush();
			os.close();
			
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("***EXCEPTION OCCURED****", e);
			return Response.serverError()
					.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}finally{
			try {
				//fw.close();
				if(bw != null){
					bw.close();
				}
				bw =null;
			} catch (Exception e) {
				logger.error("***EXCEPTION OCCURED closing the files while generating html****", e);
			}
		}
	}
	
	private ByteArrayOutputStream getByteArrayOutputStream(final String path)
			throws Exception {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[256];
		try {
			File file = new File(path);
			FileInputStream fis = new FileInputStream(file);
			for (int readNum; (readNum = fis.read(buf)) != -1;) {
				bos.write(buf, 0, readNum); // no doubt here is 0
				// Writes len bytes from the specified byte array starting at
				// offset off to this byte array output stream.
			}
		} catch (Exception ex) {
			logger.error("***EXCEPTION OCCURED getByteArrayOutputStream****",
					ex);
		}

		return bos;
	}
	
	private final String testmethod(Map<String, Object> model, Long id,
			Long groupId) {
		Integer pricingType = null;
        SystemConfigurationManager sysConfigManager = (SystemConfigurationManager)SpringContext.getBean("systemConfigurationManager");
    	SystemConfiguration pricingTypeConfig = sysConfigManager
        .loadConfigByName("enable.pricing");
    	if(pricingTypeConfig != null)
    	{    		
    		//1 for Fixed Pricing , 2 for Time Of Day Pricing    		
    		if ("1".equalsIgnoreCase(pricingTypeConfig
					.getValue())) {    			
    			pricingType = 1;
			}       
    		else if("2".equalsIgnoreCase(pricingTypeConfig
					.getValue()))
    		{
    			pricingType = 2;
    		}
    	}
        //ENL - 4179 End
    	Company company = companyManager.getAllCompanies().get(0);
    	if(pricingType!=null) company.setPricingType(pricingType);
		model.put("company", company);
		model.put("mode", "admin");
		ArrayList<String> list = new ArrayList<String>();
		NetworkInterfaceMapping nimCorporate = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Corporate.getName());
		String corporateMapping="eth0",buildingMapping="eth1";
		if(nimCorporate != null && nimCorporate.getNetworkSettings()!= null && nimCorporate.getNetworkSettings().getName() != null){
			corporateMapping = nimCorporate.getNetworkSettings().getName();
		}
		NetworkInterfaceMapping nimBuilding = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Building.getName());
		if(nimBuilding != null && nimBuilding.getNetworkSettings()!= null && nimBuilding.getNetworkSettings().getName()!= null){
			buildingMapping = nimBuilding.getNetworkSettings().getName();			
		}else{
			buildingMapping = corporateMapping;
		}
		list.add(ServerMain.getInstance().getIpAddress(corporateMapping));
		list.add(ServerMain.getInstance().getIpAddress(buildingMapping));
		list.add(ServerMain.getInstance().getSubnetMask(corporateMapping));
		list.add(ServerMain.getInstance().getDefaultGateway());
		
		list.add(ServerMain.getInstance().getSubnetMask(buildingMapping));
		model.put("system", list);
		model.put("dhcpPresent", ServerMain.getInstance().determineDHCP(corporateMapping));
        SystemConfiguration dhcpConfig = systemConfigurationManager.loadConfigByName("dhcp.enable");
        if (dhcpConfig != null) {
    		model.put("dhcpEnable", dhcpConfig.getValue());
        }
        List<Gateway> gwList = gatewayManager.loadAllGateways();
        if(gwList.isEmpty())
    		model.put("gatewaysPresent", "false");
        else
    		model.put("gatewaysPresent", "true");
        
		return "title24/compliance";
	}
}
