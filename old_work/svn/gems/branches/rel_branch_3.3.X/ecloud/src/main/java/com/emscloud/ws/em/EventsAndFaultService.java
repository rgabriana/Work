package com.emscloud.ws.em;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.communication.CommunicationUtils;
import com.emscloud.communication.vos.Event;
import com.emscloud.service.EventsAndFaultManager;
import com.emscloud.service.FacilityEmMappingManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.util.FacilityCookieHandler;



@Controller
@Path("/events")
public class EventsAndFaultService {
	
	@Resource(name = "eventsAndFaultManager")
	private EventsAndFaultManager eventAndFaultManager;

	@Resource(name="facilityManager")
	FacilityManager facilityManager;
	
	@Resource
	CommunicationUtils communicationUtils;
	@Autowired
    private MessageSource messageSource;
	@Resource
	FacilityEmMappingManager facilityEmMappingManager;
	private static final int DEFAULT_ROWS = 100;

	/**
	 * Get the list of events and faults based on the filter data and the
	 * current node selected.
	 * 
	 * @param userdata
	 *            : (List of objects in order )
	 * @param cookie
	 *            : selected tree node
	 * @return: events data in json format
	 * @throws UnsupportedEncodingException
	 * @throws ParseException
	 */
	@Path("list/getdata")
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	public String getEventsList(
			@RequestParam("data") String userdata,
			@CookieParam(FacilityCookieHandler.selectedFacilityCookie) String cookie)
			throws UnsupportedEncodingException, ParseException {

		String[] input = userdata.split("&");
		StringBuffer output = new StringBuffer("{");
		int page = 0;
		long total, records = 0;
		String orderBy = null;
		String orderWay = null;
		String query = null;

		String[] params = null;

		if (input != null && input.length > 0) {
			for (String each : input) {
				String[] keyval = each.split("=", 2);
				if (keyval[0].equals("page")) {
					page = Integer.parseInt(keyval[1]);
				} else if (keyval[0].equals("userData")) {
					query = URLDecoder.decode(keyval[1], "UTF-8");
					output.append("\"" + keyval[0] + "\": \"" + query + "\"");
					params = query.split("#");
				} else if (keyval[0].equals("sidx")) {
					orderBy = keyval[1];
				} else if (keyval[0].equals("sord")) {
					orderWay = keyval[1];
				}
			}
		}

		List<Object> filter = new ArrayList<Object>();
		FacilityCookieHandler cookieHandler = new FacilityCookieHandler(
                URLDecoder.decode(cookie, "UTF-8"));
        filter.add(cookieHandler.getFaciltiyType());
        filter.add(cookieHandler.getFacilityId());
		List<Object> eventsAndFaults = eventAndFaultManager.getEventsAndFaults(
				orderBy, orderWay, filter, (page - 1) * DEFAULT_ROWS, DEFAULT_ROWS);
		records = (Long) eventsAndFaults.get(0);
		total = (int) (Math.ceil(records / new Double(DEFAULT_ROWS)));
		if (total == 0) {
			total = 1;
		}
		output.append(", \"page\": " + page);
		output.append(", \"total\": " + total);
		output.append(", \"records\": " + records);

		output.append(", \"rows\": [");

		
		for (int i = 1; i < eventsAndFaults.size(); i++) {
			Event each = (Event) eventsAndFaults.get(i);
			if (i > 1) {
				output.append(", ");
			}
			output.append("{ \"id\": \""
					+ (Long) each.getId()
					+ "\", \"cell\": [ \""
					+ each.getEventTime().toString() + "\",\""
					+ each.getEventType().getName() + "\", \"" 
					+ each.getSeverity().getName() + "\", \"" 
					+ each.getDescription() + "\"]}");
		}
		output.append(" ]");

		output.append("}");

		return output.toString();
	}
	
	/**
	 * Get the list of events and faults based on the filter data and the
     * current node selected and export it to CSV.
     * 
     * @param orderBy
     *            : attribute specifying order of events
     * @param orderWay
     *            : ascending or descending order
     * @param userdata
     *            : (List of objects in order active, search string, group,
     *            start date, end date, severity, event type, org node type, org
     *            node id)
     * @param cookie
     *            : selected tree node
     * @return : events and faults in csv format
     * @throws UnsupportedEncodingException
     * @throws ParseException
     */
    @Path("list/getexportdata")
    @POST
    @Produces("application/csv")
    public Response getCompleteEventsList(
            @FormParam("orderBy") String orderBy,
            @FormParam("orderWay") String orderWay,
            @FormParam("exportInput") String userdata,
            @CookieParam(FacilityCookieHandler.selectedFacilityCookie) String cookie)
            throws UnsupportedEncodingException, ParseException {

        StringBuffer output = new StringBuffer("");
        String query = URLDecoder.decode(userdata, "UTF-8");

        String[] params = query.split("#");

        /*
         * (List of objects in order active, search string, group, start date,
         * end date, severity, event type, org node type, org node id)
         */
        List<Object> filter = new ArrayList<Object>();

        if (params != null && params.length > 0) {
            if (params[0] != null && !"".equals(params[0])) {
                filter.add(params[0]);
            } else {
                filter.add(null);
            }

            if (params[1] != null && !"".equals(params[1])) {
                filter.add(URLDecoder.decode(params[1], "UTF-8"));
            } else {
                filter.add(null);
            }

            if (params[2] != null && !"".equals(params[2])) {
                filter.add(params[2]);
            } else {
                filter.add(null);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss a");
            if (params[3] != null && !"".equals(params[3])) {
                filter.add((Date) sdf.parse(params[3]));
            } else {
                filter.add(null);
            }

            if (params[4] != null && !"".equals(params[4])) {
                filter.add((Date) sdf.parse(params[4]));
            } else {
                filter.add(null);
            }

            if (params[5] != null && !"".equals(params[5])) {
                filter.add(Arrays.asList(params[5].split(",")));
            } else {
                filter.add(null);
            }

            if (params[6] != null && !"".equals(params[6])) {
                filter.add(Arrays.asList(params[6].split(",")));
            } else {
                filter.add(null);
            }
        }

        FacilityCookieHandler cookieHandler = new FacilityCookieHandler(
                URLDecoder.decode(cookie, "UTF-8"));
        filter.add(cookieHandler.getFaciltiyType());
        filter.add(cookieHandler.getFacilityId());

        List<Object> eventsAndFaults = eventAndFaultManager.getEventsAndFaults(
                orderBy, orderWay, filter,  0, -1);

        /*
         * EventId - 0, EventTime - 1, Severity - 2, EventType - 3, Description
         * - 4, Active - 5, Resolved On - 6, FixtureId - 7, FixtureLocation - 8,
         * FixtureFloorId - 9, FixtureBuildingId - 10, FixtureCampusId - 11,
         * FixtureName - 12, GatewayId - 13, GatewayLocation - 14,
         * GatewayFloorId - 15, GatewayBuildingId - 16, GatewayCampusId - 17,
         * GatewayName - 18, SeverityLevel - 19, EventTimeSort - 20
         */

        output.append(messageSource.getMessage("eventsAndFault.time", null,
                null)
                + ","
                + messageSource.getMessage("eventsAndFault.eventType", null,
                        null)
                + ","
                + messageSource.getMessage("eventsAndFault.severity", null,
                        null)
                + ","
                + messageSource.getMessage("eventsAndFault.description", null,
                        null)
                        );
        
        for (int i = 1; i < eventsAndFaults.size(); i++) {
            Event each = (Event) eventsAndFaults.get(i);
            output.append("\r\n");
            output.append((String) each.getEventTime().toString()
                    + ","
                    + each.getEventType().getName()
                    + ","
                    + each.getSeverity().getName()
                    + ","
                    + each.getDescription()
           );
        }

        return Response
                .ok(output.toString(), "text/csv")
                .header("Content-Disposition",
                        "attachment;filename=Events_And_Faults.csv")
                .build();
    }
    /**
     * Get the list of events and faults based on the filter data and the
     * current node selected from EM to UEM.
     * 
     * @param userdata
     *            : (List of objects in order active, search string, group,
     *            start date, end date, severity, event type, org node type, org
     *            node id)
     * @param cookie
     *            : selected tree node
     * @return : events and faults list in JSON String format
     * @throws UnsupportedEncodingException
     * @throws ParseException
     */
	@Path("EmEventList")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public String getEMEventsList(@RequestParam("data") String userdata,@CookieParam(FacilityCookieHandler.selectedFacilityCookie) String cookie) throws UnsupportedEncodingException, ParseException
    {
	    // Get the Events of the Energy Manager and then merge them into UEM events before showing it up in UI
	    FacilityCookieHandler cookieHandler = new FacilityCookieHandler(
                URLDecoder.decode(cookie, "UTF-8"));
        return eventAndFaultManager.getEMEventsList(userdata, cookieHandler);
    }
}
