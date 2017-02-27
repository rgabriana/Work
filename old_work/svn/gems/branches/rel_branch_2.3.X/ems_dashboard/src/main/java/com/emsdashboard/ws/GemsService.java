package com.emsdashboard.ws;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.emsdashboard.model.GemsServer;
import com.emsdashboard.service.GemsManager;

@Controller
@Path("org")
public class GemsService {
	
	@Resource(name = "gemsManager")
	private GemsManager gemsManager;
	static final Logger logger = Logger.getLogger("EMS_DASHBOARD");
	
	@Context
	ServletContext context;
	
	/**
	 * Method will return XML String of the Serverdata.xml
	 * @Return XML String to UI - Which will be displayed in the form of Tree.
	 * 
	 */
	@Path("load/gemdata")
    @GET
    @Produces({ MediaType.APPLICATION_XML})
	public List<GemsServer> loadServerDataXML()
	{
		logger.info("LoadServerDataXML webService called -->" +  gemsManager.loadGEMSData());
		List<GemsServer> gemList = null;
		try
		{
		     gemList=  gemsManager.loadGEMSData();
		}catch (Exception e) {
		    logger.debug("Failed to retrive data from database ");
        }
		return gemList;
	}
	
	/**
	 * Method will save xml string coming from UI to serverdata.xml
	 * Return Serverdata.xml to UI - Which will be displayed in the form of Tree.
	 * @param XML String
	 * @return String value: 0- Success, 1- Fail
	 */
	@POST  
    @Path("save/gemdata")   
    @Consumes("application/xml")   
	@Produces("text/plain")
    public String saveServerDataXML(String incomingXML) {  
		logger.info("incomingXML :" + incomingXML); 
		String response="0";
        try
		{
            gemsManager.saveGEMSData(incomingXML);
		}catch (Exception e) {
		    response="1";
		    logger.debug("saveServerDataXML Service failed to save data into database ");
        }
		return response;
    } 
	
	/**
	 * Method will remove given node from GEMS table
	 * Return 0(Success)/1(Fail)
	 * @param XML String
	 * @return String value: 1- Success, 0- Fail
	 */
	
	@POST  
    @Path("remove/gemdata")   
    @Consumes("application/xml")   
    @Produces("text/plain")   
    public String removeItemInXML(String nodeValue) {  
		logger.info("nodeValue :" + nodeValue); 
		String response="0";
		try
		{
		    gemsManager.removeGEMSData(Long.parseLong(nodeValue));
		}catch (Exception e) {
		    response = "1";
		    logger.debug("removeItemInXML Service failed to delete data from database ");
        }
		return response;
    }
	
	/**
     * Method will return XML String of the Serverdata.xml
     * @Return XML String to UI - Which will be displayed in the form of Tree.
     * 
     */
    @Path("get/GEMSVersion/{gemId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML})
    public String getGEMSVersion(@PathParam("gemId") long gemId)
    {
        //logger.info("getGEMSVersion webService called -->" + gemsManager.getGEMSVersion(gemId));
        String gemVersion = null;
        try
        {
            gemVersion=  gemsManager.getGEMSVersion(gemId);
        }catch (Exception e) {
            logger.debug("Failed to retrive data from database ");
        }
        return "<version>"+gemVersion+ "</version>";
    }
    
    @Path("getServerTimeOffsetFromGMT")
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String getServerTimeOffsetFromGMT() {
    	DateFormat dateFormat = DateFormat.getDateTimeInstance( 
    			 DateFormat.LONG, DateFormat.LONG );
    	
    	TimeZone zone = dateFormat.getTimeZone();
    	Date d = new Date();
    	Integer offset = zone.getOffset(d.getTime())/60000;
		return offset.toString(); 
    }
    
    //Service to get the bread crumb for given node
    
    @Path("facilities/nodepath/{nodeType}/{nodeId}")
    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public String getNodePath(@PathParam("nodeType") String nodeType, @PathParam("nodeId") Long nodeID) {
        String path=gemsManager.getNodePath(nodeType, nodeID);        
        return path;
    }
	
}
