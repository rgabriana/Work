package com.ems.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.EventsAndFaultDao;
import com.ems.dao.UserDao;

import com.ems.model.Device;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.User;

import com.ems.server.ServerMain;

import com.ems.util.OutageReportVO;

import com.ems.utils.ArgumentUtils;
import com.ems.utils.DateUtil;

@Service("eventsAndFaultManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EventsAndFaultManager {

    static final Logger logger = Logger.getLogger(EventsAndFaultManager.class.getName());

    @Resource
    private EventsAndFaultDao eventsAndFaultDao;

    @Resource
    private UserDao userDao;

    public EventsAndFault save(EventsAndFault eventsAndFault) {
        if (eventsAndFault.getId() == 0) {
            eventsAndFault.setId(null);
        }
        User oUser = eventsAndFault.getResolvedBy();
        if (oUser != null) {
            if (!ArgumentUtils.isNullOrEmpty(oUser.getEmail())) {
                User user = userDao.loadUserByUserName(eventsAndFault.getResolvedBy().getEmail());
                eventsAndFault.setResolvedBy(user);
            }
        }
        return (EventsAndFault) eventsAndFaultDao.saveObject(eventsAndFault);
    }

    public EventsAndFault getEventById(Long id) {
        return eventsAndFaultDao.getEventById(id);
    }

    public List<EventsAndFault> getEventsAndFaultsByFixtureId(Long fixture_id) {
        return eventsAndFaultDao.getEventsAndFaultsByFixtureId(fixture_id);
    }

    public void resolveEventsAndFaults(Long[] ids, User user, String description) {
        eventsAndFaultDao.resolveEventsAndFaults(ids, user, description);
    }

    public List<Object> getEventsAndFaults(String order, String orderWay,List<Object> filter, String roleType, int offset, int limit) {
        return eventsAndFaultDao.getEventsAndFaults(order, orderWay, filter, roleType, offset, limit);
    }

    public List<EventsAndFault> getEventsOnFaultyFixtures(Integer floorId, java.util.Date toDate,
            java.util.Date fromDate) {
        return eventsAndFaultDao.getEventsOnFaultyFixtures(floorId, toDate, fromDate);
    }

    @SuppressWarnings("rawtypes")
    public List getFaultyFloorsByCampusAndBuilding(Integer locationId, String locationType, java.util.Date toDate,
            java.util.Date fromDate) {
        return eventsAndFaultDao.getFaultyFloorsByCampusAndBuilding(locationId, locationType, toDate, fromDate);
    }

    public List<Fixture> getFaultyFixtures(Integer floorId, java.util.Date toDate, java.util.Date fromDate) {
        return eventsAndFaultDao.getFaultyFixtures(floorId, toDate, fromDate);
    }

    public List<Fixture> getFaultyFixturesByNode(OutageReportVO outageVO) {
        return eventsAndFaultDao.getFaultyFixturesByNode(outageVO);
    }

    public List<Gateway> getFaultyGWByNode(OutageReportVO outageVO) {
        return eventsAndFaultDao.getFaultyGWByNode(outageVO);
    }

    private boolean isFilteredFixtureEventType(String eventType) {
    	return eventType.equals(EventsAndFault.FIXTURE_PROFILE_GROUP_ASSOCIATION_CHANGED)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH_USERACTION)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_PUSH_USERACTION);
    }

    public void addEvent(String desc, String eventType) {

    	try {
            if (isFilteredFixtureEventType(eventType)) {
            	return;
            }
            addEventCommon(null, desc, eventType);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}

    } // end of method addEvent

    private boolean alarmExistsAndUpdate(List<EventsAndFault> faultsList, String desc, String eventType, String severity) {

        int noOfFaults = faultsList.size();
        EventsAndFault event = null;
        for (int i = 0; i < noOfFaults; i++) {
            event = faultsList.get(i);
            if (event.getEventType().equals(eventType)) {
                // alarm is outstanding. so no need to raise it again
            	// Just update the fields and increment the count
                if (!event.getSeverity().equals(severity)) {
                    // if the severity changed, change the severity in the same event object
                    event.setSeverity(severity);
                }
                event.setEventTime(new Date());
                event.setEventValue(event.getEventValue() + 1);
                event.setDescription(desc);
                eventsAndFaultDao.saveOrUpdateEvent(event);

                return true;
            }
        }
        return false;

    } // end of method alarmExistsAndUpdate

    // Common code for all Alarms
    private void addAlarmCommon(Device device, String desc, String eventType, String severity) {

    	List<EventsAndFault> faultsList;
    	if (device == null) {
    		// Since there is no device with this alarm, get alarm by event type
    		faultsList = eventsAndFaultDao.getEventsAndFaultsByEventTypeNoDevice(eventType);
        } else {
        	faultsList = eventsAndFaultDao.getEventsAndFaultsByDeviceId(device.getId());
        }
    	if (severity == null) {
    		severity = ServerMain.getInstance().getEventType(eventType).getSeverityString();
    	}
    	if (alarmExistsAndUpdate(faultsList, desc, eventType, severity)) {
    		return;
    	}
    	EventsAndFault event = new EventsAndFault();
    	event.setSeverity(severity);
    	addEventAlarmCommon(event, device, desc, eventType);
    } //end of method addAlarmCommon

    public void addAlarm(Device device, String desc, String eventType, String severity) {
        addAlarmCommon(device, desc, eventType, severity);
    }

    public void addAlarm(String desc, String eventType, String severity) {
        addAlarmCommon(null, desc, eventType, severity);
    }

    public void addAlarm(Device device, String desc, String eventType) {

      addAlarmCommon(device, desc, eventType, null);
    } //end of method addAlarm

    public void clearAlarm(Device device, String eventType) {

      eventsAndFaultDao.resolveAlarms(device.getId(), eventType);

    } //end of method clearAlarm

    public void addAlarm(Fixture fixture, String desc, String eventType) {
      if (isFilteredFixtureEventType(eventType)) {
    	  return;
      }
      addAlarmCommon(fixture, desc, eventType, null);
    } // end of method addAlarm

    public void addUpdateAlarm(Fixture fixture, String desc, String eventType) {
        if (isFilteredFixtureEventType(eventType)) {
        	return;
        }
        List<EventsAndFault> faultsList = eventsAndFaultDao.getEventsAndFaultsByFixtureId(fixture.getId(), eventType);
        String severity = ServerMain.getInstance().getEventType(eventType).getSeverityString();
        if (faultsList != null && faultsList.size() > 0) {
            EventsAndFault event = faultsList.get(0);
            event.setSeverity(severity);
            event.setEventTime(new Date());
            event.setDescription(desc);
            eventsAndFaultDao.saveOrUpdateEvent(event);
        }else {
            EventsAndFault event = new EventsAndFault();
            event.setDevice(fixture);
            event.setActive(true);
            event.setDescription(desc);
            event.setEventTime(new Date());
            event.setEventType(eventType);
            event.setSeverity(severity);
            eventsAndFaultDao.saveOrUpdateEvent(event);
        }
      } // end of method addAlarm

    /**
     * TO add update alarm apart from Device e.g. in case of Email is not sent.
     * Method assumes that there will be only one event/alarm associated with this eventType and accordingly its values will be updated.
     * This is to reduce the no of entries in the table and reuse the same record. Here event value will let user know the number of times this is failed from last success.
     *
     * @param desc
     * @param eventType
     * @param severity
     * @param isActive
     */
    public void addUpdateSingleAlarm(final String errIn, String eventType, boolean isActive) {

    	final int maxLen = errIn == null ? 0 : errIn.length() > 499 ? 499 : errIn.length();
    	final String err = errIn == null ? null : errIn.substring(0, maxLen).replaceAll("\n", "").trim(); // THis is because the description is 500 chars only in db
    	final List<EventsAndFault> faultsList = eventsAndFaultDao.getEventsAndFaultsByEventType(eventType);
    	EventsAndFault event = null;
    	final String severity = ServerMain.getInstance().getEventType(eventType).getSeverityString();
    	if(faultsList == null || faultsList.size() < 1){
    		if(isActive){
	    		//New
	    		event = new EventsAndFault();
	            event.setActive(isActive);
	            event.setEventValue(1l);
	            event.setDescription("Failed "+event.getEventValue()+" time. The last error occured is "+err);
	            event.setEventTime(new Date());
	            event.setEventType(eventType);
	            event.setSeverity(severity);
	            eventsAndFaultDao.saveOrUpdateEvent(event);
    		}
    	}else{
    		//Update
    		event = faultsList.get(0);
    		event.setActive(isActive);
    		if(isActive){
    			event.setEventValue(event.getEventValue()+1l);
    			event.setDescription("Failed "+event.getEventValue()+" times. The last error occured is "+err);
    		}else{
    			event.setEventValue(0l);
    		}
    		event.setEventTime(new Date());
			event.setEventType(eventType);
			event.setSeverity(severity);
			eventsAndFaultDao.saveOrUpdateEvent(event);
    	}

    }

    // Common code for Events only
    private void addEventCommon(Device device, String desc, String eventType) {
        EventsAndFault event = new EventsAndFault();
        // For pruning purposes, all events must have a severity of INFO
        event.setSeverity(EventsAndFault.INFO_SEV_STR);
        addEventAlarmCommon(event, device, desc, eventType);
    }

    // Common code for all Alarms and Events
    private void addEventAlarmCommon(EventsAndFault event, Device device, String desc, String eventType) {
    	if (device != null) {
    		event.setDevice(device);
    	}
        event.setActive(true);
        event.setDescription(desc);
        event.setEventTime(new Date());
        event.setEventType(eventType);
        eventsAndFaultDao.saveOrUpdateEvent(event);

    } // end of method addEventAlarmCommon

    // it does not check whether an event of the same type is outstanding
    public void addEvent(Device device, String desc, String eventType) {
        if (isFilteredFixtureEventType(eventType)) {
        	return;
        }
        addEventCommon(device, desc, eventType);
    } // end of method addEvent

    // it does not check whether an event of the same type is outstanding
    public void addEvent(Gateway gateway, String desc, String eventType) {

    	addEventCommon(gateway, desc, eventType);
    } // end of method addEvent

    public String updateEvent(Long eventId, String comment, User user, Boolean active){
    	EventsAndFault eventAndFault = getEventById(eventId);
        if(eventAndFault != null) {
            if(eventAndFault.getActive()){
            	if(!active) {
            		Long[] ids = new Long[]{eventAndFault.getId()};
            		resolveEventsAndFaults(ids,user,comment);
            		return "R";
            	}

            }
            eventAndFault.setResolutionComments(formatComment(eventAndFault.getResolutionComments(), comment, user.getEmail()));
            eventsAndFaultDao.saveObject(eventAndFault);
            return "S";
        }
        return "R";
    }

    private String formatComment(String previousComments, String newComment, String userEmail){
        StringBuffer description = new StringBuffer("");
        if(!ArgumentUtils.isNullOrEmpty(previousComments)){
                description.append(previousComments);
        }
        description.append("[");
        description.append((DateUtil.formatDate(new Date(), DateUtil.INTERNATIONAL_FORMAT)).toString());
        description.append(": @");
        description.append(userEmail);
        description.append(":]");
        description.append(newComment);
        description.append(";\n");
        return description.toString();
    }
    
    public void pruneEventsData(){
        eventsAndFaultDao.pruneEventsData();
    }

}
