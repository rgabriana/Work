package com.emscloud.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.communication.CommunicationUtils;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.adaptor.UemAdapter;
import com.emscloud.communication.enlightedUrls.EmFacilitiesUrls;
import com.emscloud.communication.enlightedUrls.EmFixtureUrls;
import com.emscloud.communication.vos.EMEvents;
import com.emscloud.model.EmInstance;
import com.emscloud.model.Facility;
import com.emscloud.model.FacilityEmMapping;
import com.emscloud.types.FacilityType;
import com.emscloud.util.FacilityCookieHandler;
import com.emscloud.vo.EMEventList;


@Service("eventsAndFaultManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EventsAndFaultManager {

    static final Logger logger = Logger.getLogger(EventsAndFaultManager.class.getName());
    @Resource
    UemAdapter uemAdapter;
    //@Resource
    //private EventsAndFaultDao eventsAndFaultDao;
    @Resource
    CommunicationUtils communicationUtils;
    @Resource
    FacilityEmMappingManager facilityEmMappingManager;
    @Resource
    EmInstanceManager emInstanceManager;
    @Resource
    FacilityManager facilityManager;
    @Resource
	private GlemManager glemManager;
    
    @Resource
    private EventsAndFaultManager eventsAndFaultManager;
    public List<Object> getEventsAndFaults(String order, String orderWay,List<Object> filter, int offset, int limit) {
    	//TODO: Need to implement to display all local events of GLEM
        return null;//eventsAndFaultDao.getEventsAndFaults(order, orderWay, filter, offset, limit);
    }
    
    public EMEventList getEMEventsAndFaults(EmInstance emInstance, String userdata, Long orgId)
    {
        EMEventList result =null;
        ResponseWrapper<EMEventList> response = uemAdapter
                .executePost(emInstance, glemManager.getAdapter().getContextUrl()+EmFixtureUrls.getEMEvents + orgId, MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, EMEventList.class, userdata);
        if(response!=null && response.getStatus()!=null){
            if(response.getStatus()==Response.Status.OK.getStatusCode() && response.getItems()!=null){
                    result = response.getItems();
            } else {
                logger.error("Get EM Events List command failed from UEM:- "
                        + response.getEm().getIpAddress()
                        + " reason :- " + response.getStatus());
            }
        }else{
                logger.error("Failed to load EM Events from UEM. Please check EM is connected to UEM");
        }
       return result;
    }
    
    /**
     * @param userdata
     * @param cookieHandler
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getEMEventsList(String userdata, FacilityCookieHandler cookieHandler)
            throws UnsupportedEncodingException {
        
        //Global EM Events List
        List<EMEvents> emEvents= new ArrayList<EMEvents>();
        
        Long orgId = cookieHandler.getFacilityId();
        Facility facility = facilityManager.getFacility(orgId);
        ArrayList<EmInstance> emList = communicationUtils.getEmMap(facilityManager.getFacility(orgId));
        int DEFAULT_ROWS =100;
        Long EmFloorId=(long) -1;
        int page = 0;
        long total = 0, records = 0;
        EMEventList eventsAndFaults = null;
        String[] input = userdata.split("&");
        StringBuffer output = new StringBuffer("{");
        String query = null;

        if (input != null && input.length > 0) {
            for (String each : input) {
                String[] keyval = each.split("=", 2);
                if (keyval[0].equals("page")) {
                    page = Integer.parseInt(keyval[1]);
                } else
                if (keyval[0].equals("userData")) {
                    query = URLDecoder.decode(keyval[1], "UTF-8");
                    System.out.println("Query " + query);
                    output.append("\"" + keyval[0] + "\": \"" + query + "\"");
                }
            }
        }
        if (FacilityType.getFacilityType(FacilityType.FLOOR) == facility.getType()) 
        //if(facility.getType().toString().equalsIgnoreCase(FacilityType.FLOOR.getName()))
        {
            FacilityEmMapping facilityEMmapping = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(orgId);
            EmFloorId = facilityEMmapping.getEmFacilityId();
            eventsAndFaults = getEMEventsAndFaults(emList.get(0),userdata,EmFloorId);
        }else
        {
            ArrayList<Facility> floorFacilitys = (ArrayList<Facility>) communicationUtils.getFloor(facility);
            Iterator<Facility> facilityItr = floorFacilitys.iterator();
            while (facilityItr.hasNext()) {
                Facility facilityObj = facilityItr.next();
                FacilityEmMapping facilityEMmapping = facilityEmMappingManager.getFacilityEmMappingOnFacilityId(facilityObj.getId());
                EmFloorId = facilityEMmapping.getEmFacilityId();
                EmInstance emInstance = emInstanceManager.getEmInstance(facilityEMmapping.getEmId());
                EMEventList tempEventsAndFaults = getEMEventsAndFaults(emInstance,userdata,EmFloorId);
                if(tempEventsAndFaults!=null && tempEventsAndFaults.getEmEventsList().size()>0)
                {
                    emEvents.addAll(tempEventsAndFaults.getEmEventsList());
                    records+=tempEventsAndFaults.getRecords();
                    //page=tempEventsAndFaults.getPage();
                }
            }
            EMEventList eMEventList = new EMEventList();
            eMEventList.setEmEventsList(emEvents);
            eMEventList.setRecords(records);
            
            total = (int) (Math.ceil(records / new Double(DEFAULT_ROWS)));
            if (total == 0) {
                total = 1;
            }
            eMEventList.setTotal(total);
            eMEventList.setPage(page);
            eventsAndFaults = eMEventList;
        }
       
       
        if(eventsAndFaults!=null && eventsAndFaults.getEmEventsList()!=null)
        {
            emEvents = eventsAndFaults.getEmEventsList();
            records = eventsAndFaults.getRecords();
            page = eventsAndFaults.getPage();
            total = eventsAndFaults.getTotal();
        }
        if (total == 0) {
            total = 1;
        }
        output.append(", \"page\": " + page);
        output.append(", \"total\": " + total);
        output.append(", \"records\": " + records);

        output.append(", \"rows\": [");

        /*
         * EventId - 0, EventTime - 1, Severity - 2, EventType - 3, Description
         * - 4, Active - 5, Resolved On - 6, FixtureId - 7, FixtureLocation - 8,
         * FixtureFloorId - 9, FixtureBuildingId - 10, FixtureCampusId - 11,
         * FixtureName - 12, GatewayId - 13, GatewayLocation - 14,
         * GatewayFloorId - 15, GatewayBuildingId - 16, GatewayCampusId - 17,
         * GatewayName - 18, SeverityLevel - 19, EventTimeSort - 20
         */

        if(emEvents!=null)
        {
            Iterator<EMEvents> rwitr = emEvents.iterator();
            int i=1;
            while (rwitr.hasNext()) {
                if (i > 1) {
                    output.append(", ");
                }
                EMEvents nextRw = rwitr.next();
                output.append("{ \"id\": \""
                        + nextRw.getId()
                        + "\", \"cell\": [ \""
                        + nextRw.getEventTime()
                        + "\", \""
                        + nextRw.getEventType()
                        + "\", \""
                        + (nextRw.getDeviceName() != null ? nextRw.getDeviceName() : "" )+ "\", \"" + nextRw.getSeverity() + "\", \"" + nextRw.getDescription()
                        + "\" ]}");
                i++;
            }
        }
        output.append(" ]");
        output.append("}");
        return output.toString();
    }
}
