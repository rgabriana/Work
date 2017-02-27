package com.ems.ws;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.model.LocatorDevice;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.LocatorDeviceManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;


@Controller
@Path("/org/locatordevice")
public class LocatorDeviceService {
    
    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource(name = "locatorDeviceManager")
    private LocatorDeviceManager locatorDeviceManager;
    
    @Autowired
    private MessageSource messageSource;
	@Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
    public LocatorDeviceService() {

    }

   
    /**
     * Adds a Locator Device
     * 
     *      
     * @param floorId
     *            floorId at which to add a Locator Device
     * @return Response status
     */
    @Path("add/name/{name}/locatorDeviceType/{locatorDeviceType}/estimatedBurnHours/{estimatedBurnHours}/floorId/{floorId}/xaxis/{xaxis}/yaxis/{yaxis}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addLocatorDevice(@PathParam("name") String name,
            @PathParam("locatorDeviceType") String locatorDeviceType, @PathParam("estimatedBurnHours") Long estimatedBurnHours,@PathParam("floorId") String floorId , @PathParam("xaxis") String xaxis,@PathParam("yaxis") String yaxis) {
        Response resp = new Response();
       
        Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("locatorDeviceType", locatorDeviceType);
        nameValMap.put("estimatedBurnHours", estimatedBurnHours);
        nameValMap.put("floorId", floorId);
        nameValMap.put("xaxis", xaxis);
        nameValMap.put("yaxis", yaxis);
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return resp;
    	}
        locatorDeviceManager.addLocatorDevice(name, locatorDeviceType, estimatedBurnHours,floorId,xaxis,yaxis); 
        userAuditLoggerUtil.log("Added LocatorDevice: " + name +" with type "+locatorDeviceType, UserAuditActionType.Locator_Device_Create.getName());
        return resp;
    }
    
    /**
     * Updates a Locator Device
     * 
     *      
     * @param floorId
     *            floorId at which to add a Locator Device
     * @return Response status
     */
    @Path("update/name/{name}/id/{id}/estimatedBurnHours/{estimatedBurnHours}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateLocatorDevice(@PathParam("name") String name,
            @PathParam("id") String id,@PathParam("estimatedBurnHours") Long estimatedBurnHours) {
        Response resp = new Response();       
        Map<String,Object> nameValMap = new HashMap<String,Object>();
        nameValMap.put("locatorDeviceName", name);
        //nameValMap.put("id", id);
        //nameValMap.put("estimatedBurnHours", estimatedBurnHours);       
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return resp;
    	}
        locatorDeviceManager.updateLocatorDevice(id,name,estimatedBurnHours); 
        userAuditLoggerUtil.log("Updated LocatorDevice: " + name, UserAuditActionType.Locator_Device_Update.getName());
        return resp;
    }
    
    
    /**
     * Delete Switch
     * 
     * @param id
     *            Switch unique identifier
     * @return Response status
     */
    @Path("delete/{id}")
    @GET
    public Response deleteLocatorDevice(@PathParam("id") long id) {
    	 Response response = new Response();  
       
      
        String deletedLocatorDeviceName = locatorDeviceManager.getLocatorDeviceById(id).getName();
        int status = locatorDeviceManager.deleteLocatorDevice(id);
        
        response.setStatus(status);
        if(status == 1){
            
            userAuditLoggerUtil.log("Deleted LocatorDevice: " + deletedLocatorDeviceName, UserAuditActionType.Locator_Device_Delete.getName());
        }
        return response;
    }
    
    /**
     * Return Locator Device list
     * @param floorId
     *            property unique identifier
     * @param name
     *            Locator Device name
     * @return LocatorDevice for the selected name and org level
     */
    @Path("details/{floorId}/{name}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public LocatorDevice loadLocatorDeviceByNameandFloorId(@PathParam("floorId") Long floorId,
            @PathParam("name") String name) {
    	Response response = new Response();  
    	Map<String,Object> nameValMap = new HashMap<String,Object>();       
         nameValMap.put("id", floorId);
         nameValMap.put("locatorDeviceName", name);
         response = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
         if(response!= null && response.getStatus()!=200){
        		m_Logger.error("Validation error"+response.getMsg());
     		return new LocatorDevice();
     	}
        return locatorDeviceManager.getLocatorDeviceByNameandFloorId(name, floorId);
        
    }
    
    
    /**
     * Returns locator device list
     * 
     * @param property
     *            (company|campus|building|floor|area)
     * @param pid
     *            property unique identifier
     * @return locator device  list for the property level
     */
    @Path("list/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<LocatorDevice> getLocatorDeviceList(@PathParam("property") String property, @PathParam("pid") Long pid) {
    	Response resp = new Response();
        Map<String,Object> nameValMap = new HashMap<String,Object>();
    	nameValMap.put("property", property);    	
        resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
        if(resp!= null && resp.getStatus()!=200){
        	m_Logger.error("Validation error"+resp.getMsg());
    		return null;
    	}
        if (property.equalsIgnoreCase("company")) {
            return locatorDeviceManager.loadAllLocatorDevices();
        } else if (property.equalsIgnoreCase("campus")) {
            return locatorDeviceManager.loadLocatorDevicesByCampusId(pid);
        } else if (property.equalsIgnoreCase("building")) {
            return locatorDeviceManager.loadLocatorDevicesByBuldingId(pid);
        } else if (property.equalsIgnoreCase("floor")) {
            return locatorDeviceManager.loadLocatorDevicesByFloorId(pid);
        } else if (property.equalsIgnoreCase("area")) {
            return null;
        }
        return null;
    }
    
    /**
     * Updates the position of the selected Locator Devices on the floorplan
     * 
     * @param locatordevices
     *            List of selected locator devices with their respective x & y co-ordinates
     *            "<locatordevices><locatordevice><id>1</id><xaxis>100</xaxis><yaxis>100</yaxis></locatordevice></locatordevices>"
     * @return Response status
     */
    @Path("du/updateposition")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateLocatorDevicePosition(List<LocatorDevice> locatorDevices) {
        Iterator<LocatorDevice> itr = locatorDevices.iterator();
        StringBuffer locatorDeviceNames = new StringBuffer("");
        boolean log = false;
        while (itr.hasNext()) {
            LocatorDevice oLocatorDevice = (LocatorDevice) itr.next();
            if (oLocatorDevice.getXaxis() != null && oLocatorDevice.getYaxis() != null) {
                locatorDeviceManager.updatePositionById(oLocatorDevice);
                locatorDeviceNames.append(locatorDeviceManager.getLocatorDeviceById(oLocatorDevice.getId()).getName() + "(X:" + oLocatorDevice.getXaxis()
                        + " Y:" + oLocatorDevice.getYaxis() + ") ");
                log = true;
            }
        }
        if (log) {
            userAuditLoggerUtil.log("Updated Locator Device positions for " + locatorDeviceNames,
                    UserAuditActionType.Locator_Device_Update_Position.getName());
        }
        return new Response();
    }
    

}
