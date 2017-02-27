package com.emscloud.ws.em;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.emscloud.service.WdsManager;
import com.emscloud.communication.vos.Wds;

@Controller
@Path("/org/wds")
public class WdsService {
    private static final Logger m_Logger = Logger.getLogger("WSLogger");
  
    @Resource(name = "wdsManager")
    private WdsManager wdsManager;
         
    /**
     * Return wds list
     * 
     * @param property
     *            (company|campus|building|floor|area|secondarygateway|switch)
     * @param pid
     *            property unique identifier
     * @return Wds list for the selected org level
     */
    @Path("list/{property}/{pid}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Wds> getSortedWdsList(@PathParam("property") String property, @PathParam("pid") Long pid, @FormParam("sidx") String orderBy,
    		@FormParam("sord") String orderWay) {    	
        List<Wds> wdsList = wdsManager.getWdsList(property, pid);  
        
        if("name".equals(orderBy)){
        	if("asc".equals(orderWay)){        		 
        		Collections.sort(wdsList, new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				return wds1.getName().compareToIgnoreCase(wds2.getName());				
        			}
        	    });
        	}
        	else{
        		Collections.sort(wdsList, Collections.reverseOrder(new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				return wds1.getName().compareToIgnoreCase(wds2.getName());				
        			}
        	    }));        		
        	}        	
        }else if("macaddress".equals(orderBy)){
        	if("asc".equals(orderWay)){
        		Collections.sort(wdsList, new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				return wds1.getMacAddress().compareToIgnoreCase(wds2.getMacAddress());				
        			}
        	    });     		
        	}
        	else{        		
        		Collections.sort(wdsList, Collections.reverseOrder(new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				return wds1.getMacAddress().compareToIgnoreCase(wds2.getMacAddress());				
        			}
        	    }));  
        	}        	
        }else if("switchName".equals(orderBy)){
        	if("asc".equals(orderWay)){
        		Collections.sort(wdsList, new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				if (wds1.getSwitchName() == null) {
        			        return (wds2.getSwitchName() == null) ? 0 : -1;
        			    }
        			    if (wds2.getSwitchName() == null) {
        			        return 1;
        			    }
        			    
        				return wds1.getSwitchName().compareToIgnoreCase(wds2.getSwitchName());				
        			}
        	    });       		
        	}
        	else{        		
        		Collections.sort(wdsList, Collections.reverseOrder(new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				if (wds1.getSwitchName() == null) {
        			        return (wds2.getSwitchName() == null) ? 0 : -1;
        			    }
        			    if (wds2.getSwitchName() == null) {
        			        return 1;
        			    }
        			    
        				return wds1.getSwitchName().compareToIgnoreCase(wds2.getSwitchName());				
        			}
        	    })); 
        	}        	
        }else if("batteryLevel".equals(orderBy)){
        	if("asc".equals(orderWay)){
        		Collections.sort(wdsList, new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				return wds1.getBatteryLevel().compareToIgnoreCase(wds2.getBatteryLevel());				
        			}
        	    });       		
        	}
        	else{        		
        		Collections.sort(wdsList, Collections.reverseOrder(new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				return wds1.getBatteryLevel().compareToIgnoreCase(wds2.getBatteryLevel());				
        			}
        	    }));
        	}        	
        }else if("gatewayName".equals(orderBy)){
        	if("asc".equals(orderWay)){
        		Collections.sort(wdsList, new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				if (wds1.getGatewayName() == null) {
        			        return (wds2.getGatewayName() == null) ? 0 : -1;
        			    }
        			    if (wds2.getGatewayName() == null) {
        			        return 1;
        			    }
        				return wds1.getGatewayName().compareToIgnoreCase(wds2.getGatewayName());				
        			}
        	    });       		
        	}
        	else{        		
        		Collections.sort(wdsList, Collections.reverseOrder(new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				if (wds1.getGatewayName() == null) {
        			        return (wds2.getGatewayName() == null) ? 0 : -1;
        			    }
        			    if (wds2.getGatewayName() == null) {
        			        return 1;
        			    }
        				return wds1.getGatewayName().compareToIgnoreCase(wds2.getGatewayName());				
        			}
        	    }));
        	}        	
        }else if("version".equals(orderBy)){
        	if("asc".equals(orderWay)){
        		Collections.sort(wdsList, new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				if (wds1.getVersion() == null) {
        			        return (wds2.getVersion() == null) ? 0 : -1;
        			    }
        			    if (wds2.getVersion() == null) {
        			        return 1;
        			    }
        				return wds1.getVersion().compareToIgnoreCase(wds2.getVersion());				
        			}
        	    });       		
        	}
        	else{        		
        		Collections.sort(wdsList, Collections.reverseOrder(new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				if (wds1.getVersion() == null) {
        			        return (wds2.getVersion() == null) ? 0 : -1;
        			    }
        			    if (wds2.getVersion() == null) {
        			        return 1;
        			    }
        				return wds1.getVersion().compareToIgnoreCase(wds2.getVersion());				
        			}
        	    }));
        	}        	
        }else if("captureAtStr".equals(orderBy)){
        	if("asc".equals(orderWay)){
        		Collections.sort(wdsList, new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				if (wds1.getCaptureAtStr() == null) {
        			        return (wds2.getCaptureAtStr() == null) ? 0 : -1;
        			    }
        			    if (wds2.getCaptureAtStr() == null) {
        			        return 1;
        			    }
        				return wds1.getCaptureAtStr().compareToIgnoreCase(wds2.getCaptureAtStr());				
        			}
        	    });       		
        	}
        	else{        		
        		Collections.sort(wdsList, Collections.reverseOrder(new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				if (wds1.getCaptureAtStr() == null) {
        			        return (wds2.getCaptureAtStr() == null) ? 0 : -1;
        			    }
        			    if (wds2.getCaptureAtStr() == null) {
        			        return 1;
        			    }
        				return wds1.getCaptureAtStr().compareToIgnoreCase(wds2.getCaptureAtStr());			
        			}
        	    }));
        	}        	
        }else if("upgradestatus".equals(orderBy)){
        	if("asc".equals(orderWay)){
        		Collections.sort(wdsList, new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				if (wds1.getUpgradeStatus() == null) {
        			        return (wds2.getUpgradeStatus() == null) ? 0 : -1;
        			    }
        			    if (wds2.getUpgradeStatus() == null) {
        			        return 1;
        			    }
        				return wds1.getUpgradeStatus().compareToIgnoreCase(wds2.getUpgradeStatus());				
        			}
        	    });       		
        	}
        	else{        		
        		Collections.sort(wdsList, Collections.reverseOrder(new Comparator<Wds>() {
        			@Override
        			public int compare(Wds wds1, Wds wds2) {
        				if (wds1.getUpgradeStatus() == null) {
        			        return (wds2.getUpgradeStatus() == null) ? 0 : -1;
        			    }
        			    if (wds2.getUpgradeStatus() == null) {
        			        return 1;
        			    }
        				return wds1.getUpgradeStatus().compareToIgnoreCase(wds2.getUpgradeStatus());				
        			}
        	    }));
        	}        	
        }
              
        return wdsList;
    }
    
    
    /**
     * Return wds list
     * 
     * @param property
     *            (company|campus|building|floor|area|secondarygateway|switch)
     * @param pid
     *            property unique identifier
     * @return Wds list for the selected org level
     */
    @Path("list/{property}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Wds> getWdsList(@PathParam("property") String property, @PathParam("pid") Long pid) {    	
        List<Wds> wdsList = wdsManager.getWdsList(property, pid);  
        return wdsList;
    }
    
    /**
     * Returns WDS Details
     * 
     * @param wdsId
     *            wds unique identifier
     * @param pid
     *            property unique identifier
     * @return wds details
     */
    @Path("details/{wdsId}/{pid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Wds getFixtureDetails(@PathParam("wdsId") Long wdsId, @PathParam("pid") Long pid) {
        return wdsManager.getWdsDetails(wdsId, pid);
    }    
}
