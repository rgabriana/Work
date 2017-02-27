package com.ems.ws;

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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.model.Floor;
import com.ems.model.User;
import com.ems.mvc.util.FacilityCookieHandler;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FloorManager;
import com.ems.service.UserManager;
import com.ems.types.DeviceType;
import com.ems.types.FacilityType;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.vo.EMEventList;
import com.ems.vo.EMEvents;

@Controller
@Path("/events")
public class EventsAndFaultService {
	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	@Resource(name = "eventsAndFaultManager")
	private EventsAndFaultManager eventAndFaultManager;

	@Autowired
	private MessageSource messageSource;

	@Resource(name = "userManager")
	private UserManager userManager;

	@Resource(name="floorManager")
	private FloorManager floorManager;
	private static final int DEFAULT_ROWS = 20;

	/**
	 * Get the list of events and faults based on the filter data and the
	 * current node selected.
	 * 
	 * @param userdata
	 *            : (List of objects in order active, search string, group,
	 *            start date, end date, severity, event type, org node type, org
	 *            node id)
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
					Double pageNum = Double.parseDouble(keyval[1]);
					pageNum = Math.floor(pageNum);
					page= pageNum.intValue();
					//page = Integer.parseInt(keyval[1]);
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

		EmsAuthenticationContext emsctxt = new EmsAuthenticationContext();
		RoleType role = emsctxt.getCurrentUserRoleType();

		List<Object> eventsAndFaults = eventAndFaultManager.getEventsAndFaults(
				orderBy, orderWay, filter, role.getName(), (page - 1)
						* DEFAULT_ROWS, DEFAULT_ROWS);
		records = (Long) eventsAndFaults.get(0);
		total = (int) (Math.ceil(records / new Double(DEFAULT_ROWS)));
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

		for (int i = 1; i < eventsAndFaults.size(); i++) {
			Object[] each = (Object[]) eventsAndFaults.get(i);
			if (i > 1) {
				output.append(", ");
			}
			output.append("{ \"id\": \""
					+ (Long) each[0]
					+ "\", \"cell\": [ \""
					+ (String) each[1]
					+ "\", \""
					+ (each[7] != null ? (String) each[8] + " -> " + (String) each[12] : "") + "\", \""
					+ each[3] + "\", \"" + each[2] + "\", \"" + each[4]
					+ "\", \"" + "View" + "\", \"" + each[5] + "\" ]}");
		}
		output.append(" ]");

		output.append("}");

		return output.toString();
	}

	/**
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

		EmsAuthenticationContext emsctxt = new EmsAuthenticationContext();
		RoleType role = emsctxt.getCurrentUserRoleType();

		List<Object> eventsAndFaults = eventAndFaultManager.getEventsAndFaults(
				orderBy, orderWay, filter, role.getName(), 0, -1);

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
				+ messageSource.getMessage("eventsAndFault.location", null,
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
				+ ","
				+ messageSource.getMessage("eventsAndFault.resolved", null,
						null));

		for (int i = 1; i < eventsAndFaults.size(); i++) {
			Object[] each = (Object[]) eventsAndFaults.get(i);
			output.append("\r\n");
			output.append((String) each[1]
					+ ","
					+ (each[7] != null ? (String) each[8] + "->" + (String) each[12] : "")
					+ ","
					+ each[3]
					+ ","
					+ each[2]
					+ ","
					+ each[4]
					+ ","
					+ ((Boolean) each[5] ? messageSource.getMessage("lov.no",
							null, null) : messageSource.getMessage("lov.yes",
							null, null)));
		}

		return Response
				.ok(output.toString(), "text/csv")
				.header("Content-Disposition",
						"attachment;filename=Events_And_Faults.csv")
				.build();
	}

	/**
	 * To mark the list of events as resolved.
	 * 
	 * @param ids
	 *            : comma separated event ids to be marked resolved
	 * @param comment
	 *            : resolve comment
	 * @return response status
	 */
	@Path("resolve/{ids}/{comment}")
	@POST
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String resolve(@PathParam("ids") String ids,
			@PathParam("comment") String comment) {
		try {
			String[] listIds = (URLDecoder.decode(ids, "UTF-8")).split(",");
			Long[] longIds = new Long[listIds.length];
			int count = 0;
			for (String a : listIds) {
				longIds[count++] = Long.parseLong(a);
			}
			comment = (URLDecoder.decode(comment, "UTF-8")).trim();
			EmsAuthenticationContext emsctxt = new EmsAuthenticationContext();
			User user = userManager.loadUserById(emsctxt.getUserId());
			eventAndFaultManager.resolveEventsAndFaults(longIds, user, comment);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "E";
		}
		
		userAuditLoggerUtil.log("Resolved event: " + ids, UserAuditActionType.Events_Update.getName());

		return "S";
	}

	/**
	 * update an existing event
	 * 
	 * @param event
	 *            id
	 * @param resolution
	 *            comment
	 * @param resolve
	 *            or not
	 * @return response status
	 */
	@Path("update/{id}/{comment}/{resolve}")
	@POST
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public String updateEvent(@PathParam("id") String id,
			@PathParam("comment") String comment,
			@PathParam("resolve") String resolve) {
		try {
			comment = (URLDecoder.decode(comment, "UTF-8")).trim();
			Long eventId = Long.parseLong(id);
			EmsAuthenticationContext emsctxt = new EmsAuthenticationContext();
			User user = userManager.loadUserById(emsctxt.getUserId());
			Boolean active = !"true".equals(resolve);
			userAuditLoggerUtil.log("Update event: " + id, UserAuditActionType.Events_Update.getName());
			return eventAndFaultManager.updateEvent(eventId, comment, user,
					active);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "E";
		}
	}
	
	
	/**
     * Get the list of events and faults based on the filter data and facility ID provided.
     * 
     * @param userdata
     *            : (List of objects in order active, search string, group,
     *            start date, end date, severity, event type, org node type, org
     *            node id)
     * @param facilityId
     *            : facility id (Floor id)
     * @return: EMEventList VO
     * @throws UnsupportedEncodingException
     * @throws ParseException
     */
    @Path("list/emeventlist/{facilityId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EMEventList getEMEventsList(@RequestParam("data") String userdata,@PathParam("facilityId") Long facilityId)
            throws UnsupportedEncodingException, ParseException {
        if(userdata.isEmpty())
        {
            m_Logger.error("Get EM Events List command failed from EM because of empty query string");
        }
        EMEventList eMEventList = new EMEventList();
        List<Object> eventsAndFaults=null;
        int DEFAULT_ROWS =100;
        String[] input = userdata.split("&");
        int page = 0;
        long total=0;
        long records = 0;
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
                   // output.append("\"" + keyval[0] + "\": \"" + query + "\"");
                    params = query.split("#");
                } else if (keyval[0].equals("sidx")) {
                    orderBy = keyval[1];
                } else if (keyval[0].equals("sord")) {
                    orderWay = keyval[1];
                }
            }
        }

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

        if(params!=null && params.length>0)
        {
            Floor floor;
            try {
                floor = floorManager.getFloorById(facilityId);
                filter.add(FacilityType.FLOOR);
                filter.add(floor.getId());
            } catch (Exception e) {
                //Load Company Level Events List
                filter.add(FacilityType.COMPANY);
                filter.add(1l);
            }
            
            EmsAuthenticationContext emsctxt = new EmsAuthenticationContext();
            RoleType role = emsctxt.getCurrentUserRoleType();

            eventsAndFaults = eventAndFaultManager.getEventsAndFaults(
                    orderBy, orderWay, filter, role.getName(), (page - 1)
                            * DEFAULT_ROWS, DEFAULT_ROWS);
            records = (Long) eventsAndFaults.get(0);
            total = (int) (Math.ceil(records / new Double(DEFAULT_ROWS)));
            if (total == 0) {
                total = 1;
            }
        }

        /*
         * EventId - 0, EventTime - 1, Severity - 2, EventType - 3, Description
         * - 4, Active - 5, Resolved On - 6, FixtureId - 7, FixtureLocation - 8,
         * FixtureFloorId - 9, FixtureBuildingId - 10, FixtureCampusId - 11,
         * FixtureName - 12, GatewayId - 13, GatewayLocation - 14,
         * GatewayFloorId - 15, GatewayBuildingId - 16, GatewayCampusId - 17,
         * GatewayName - 18, SeverityLevel - 19, EventTimeSort - 20
         */
       
        eMEventList.setRecords(records);
        eMEventList.setTotal(total);
        eMEventList.setPage(page);
        List<EMEvents> emEventsArr = new ArrayList<EMEvents>();
        if(eventsAndFaults!=null && eventsAndFaults.size()>0)
        {
            for (int i = 1; i < eventsAndFaults.size(); i++) {
                Object[] each = (Object[]) eventsAndFaults.get(i);
                
                EMEvents emEvents = new EMEvents();
                emEvents.setId((Long) each[0]);
                emEvents.setEventTime((String) each[1]);
                emEvents.setSeverity((String) each[2]);
                emEvents.setEventType((String) each[3]);
                emEvents.setDescription((String) each[4]);
                emEvents.setActive((Boolean) each[5]);
                emEvents.setResolvedOn(each[6]==null?"": (String)each[6]);
                emEvents.setDeviceId((Long) each[7]);
                emEvents.setLocation((String) each[8]);
                String devName = (String) each[12];
                if(devName!=null)
                {
                    emEvents.setDeviceName(devName);
                    if(devName.contains("GW")){
                        emEvents.setDeviceType(DeviceType.Gateway.getName());
                    }else if(devName.contains("Sensor")){
                        emEvents.setDeviceType(DeviceType.Fixture.getName());
                    }
                }
                emEventsArr.add(emEvents);
            }
        }
        eMEventList.setEmEventsList(emEventsArr);
        return eMEventList;
    }
}
