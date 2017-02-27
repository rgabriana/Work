package com.ems.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.EventsAndFaultDao;
import com.ems.dao.UserDao;
import com.ems.model.Device;
import com.ems.model.EventsAndFault;
import com.ems.model.EventsAndFaultFilter;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.User;
import com.ems.model.Wds;
import com.ems.server.ServerMain;
import com.ems.util.Constants;
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

    @SuppressWarnings("unchecked")
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

    public void addEvent(String desc, String eventType, String severity) {
    	
    	try {
            if (eventType.equals(EventsAndFault.FIXTURE_PROFILE_GROUP_ASSOCIATION_CHANGED)
                    || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH)
                    || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH_USERACTION)
                    || eventType.equals(EventsAndFault.FIXTURE_PROFILE_PUSH_USERACTION)) {
                return;
            }
            EventsAndFault event = new EventsAndFault();
            event.setActive(true);
            event.setDescription(desc);
            event.setEventTime(new Date());
            event.setEventType(eventType);
            event.setSeverity(severity);
            eventsAndFaultDao.saveOrUpdateEvent(event);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}

    } // end of method addEvent
    
    public void addEvent(Wds wds,String desc,String eventType,String severity)
    {
    	EventsAndFault event = new EventsAndFault();
        event.setDevice(wds);
        event.setActive(true);
        event.setDescription(desc);
        event.setEventTime(new Date());
        event.setEventType(eventType);
        event.setSeverity(severity);
        eventsAndFaultDao.saveOrUpdateEvent(event);
    }
    
    public void addEvent(Gateway gw,String desc,String eventType,String severity)
    {
    	EventsAndFault event = new EventsAndFault();
        event.setDevice(gw);
        event.setActive(true);
        event.setDescription(desc);
        event.setEventTime(new Date());
        event.setEventType(eventType);
        event.setSeverity(severity);
        eventsAndFaultDao.saveOrUpdateEvent(event);
    }
    
    private boolean alarmExistsAndUpdate(List faultsList, String desc, String eventType, String severity) {

        int noOfFaults = faultsList.size();
        EventsAndFault event = null;
        for (int i = 0; i < noOfFaults; i++) {
            event = (EventsAndFault) faultsList.get(i);
            if (event.getEventType().equals(eventType)) {
                // if the severity changed, change the severity in the same event object
                if (!event.getSeverity().equals(severity)) {
                    event.setSeverity(severity);
                    event.setEventTime(new Date());
                    event.setDescription(desc);
                    eventsAndFaultDao.saveOrUpdateEvent(event);
                }
                // alarm is outstanding. so no need to raise it again
                return true;
            }
        }
        return false;

    } // end of method alarmExistsAndUpdate
    
    public void addAlarm(Gateway gw, String desc, String eventType) {

      List faultsList = eventsAndFaultDao.getEventsAndFaultsByGatewayId(gw.getId());
      String severity = ServerMain.getInstance().getEventType(eventType).getSeverityString();      
      if (alarmExistsAndUpdate(faultsList, desc, eventType, severity)) {
          return;
      }
      EventsAndFault event = new EventsAndFault();
      event.setDevice(gw);
      event.setActive(true);
      event.setDescription(desc);
      event.setEventTime(new Date());
      event.setEventType(eventType);
      event.setSeverity(severity);
      eventsAndFaultDao.saveOrUpdateEvent(event);

    }

    public void addAlarm1(Gateway gw, String desc, String eventType, String severity) {

        List faultsList = eventsAndFaultDao.getEventsAndFaultsByGatewayId(gw.getId());
        if (alarmExistsAndUpdate(faultsList, desc, eventType, severity)) {
            return;
        }
        EventsAndFault event = new EventsAndFault();
        event.setDevice(gw);
        event.setActive(true);
        event.setDescription(desc);
        event.setEventTime(new Date());
        event.setEventType(eventType);
        event.setSeverity(severity);
        eventsAndFaultDao.saveOrUpdateEvent(event);

    }
    
    public void clearAlarm(Device device, String eventType) {
            
      eventsAndFaultDao.resolveAlarms(device.getId(), eventType);
            
    } //end of method clearAlarm

    public void addAlarm1(Fixture fixture, String desc, String eventType, String severity) {
        if (eventType.equals(EventsAndFault.FIXTURE_PROFILE_GROUP_ASSOCIATION_CHANGED)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH_USERACTION)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_PUSH_USERACTION)) {
            return;
        }
        List faultsList = getEventsAndFaultsByFixtureId(fixture.getId());
        if (alarmExistsAndUpdate(faultsList, desc, eventType, severity)) {
            return;
        }
        EventsAndFault event = new EventsAndFault();
        event.setDevice(fixture);
        event.setActive(true);
        event.setDescription(desc);
        event.setEventTime(new Date());
        event.setEventType(eventType);
        event.setSeverity(severity);
        eventsAndFaultDao.saveOrUpdateEvent(event);

    } // end of method addAlarm
    
    public void addAlarm(Fixture fixture, String desc, String eventType) {
      if (eventType.equals(EventsAndFault.FIXTURE_PROFILE_GROUP_ASSOCIATION_CHANGED)
              || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH)
              || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH_USERACTION)
              || eventType.equals(EventsAndFault.FIXTURE_PROFILE_PUSH_USERACTION)) {
          return;
      }
      List faultsList = getEventsAndFaultsByFixtureId(fixture.getId());
      String severity = ServerMain.getInstance().getEventType(eventType).getSeverityString();
      if (alarmExistsAndUpdate(faultsList, desc, eventType, severity)) {
          return;
      }
      EventsAndFault event = new EventsAndFault();
      event.setDevice(fixture);
      event.setActive(true);
      event.setDescription(desc);
      event.setEventTime(new Date());
      event.setEventType(eventType);
      event.setSeverity(severity);
      eventsAndFaultDao.saveOrUpdateEvent(event);

    } // end of method addAlarm
    
    public void addUpdateAlarm(Fixture fixture, String desc, String eventType) {
        if (eventType.equals(EventsAndFault.FIXTURE_PROFILE_GROUP_ASSOCIATION_CHANGED)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH_USERACTION)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_PUSH_USERACTION)) {
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

    
    // it does not check whether an event of the same type is outstanding
    public void addEvent(Fixture fixture, String desc, String eventType) {
        if (eventType.equals(EventsAndFault.FIXTURE_PROFILE_GROUP_ASSOCIATION_CHANGED)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH_USERACTION)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_PUSH_USERACTION)) {
            return;
        }

        EventsAndFault event = new EventsAndFault();
        event.setDevice(fixture);
        event.setActive(true);
        event.setDescription(desc);
        event.setEventTime(new Date());
        event.setEventType(eventType);
        event.setSeverity(ServerMain.getInstance().getEventType(eventType).getSeverityString());
        eventsAndFaultDao.saveOrUpdateEvent(event);

    } // end of method addEvent

    // it does not check whether an event of the same type is outstanding
    public void addEvent1(Fixture fixture, String desc, String eventType, String severity) {
        if (eventType.equals(EventsAndFault.FIXTURE_PROFILE_GROUP_ASSOCIATION_CHANGED)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_MISMATCH_USERACTION)
                || eventType.equals(EventsAndFault.FIXTURE_PROFILE_PUSH_USERACTION)) {
            return;
        }

        EventsAndFault event = new EventsAndFault();
        event.setDevice(fixture);
        event.setActive(true);
        event.setDescription(desc);
        event.setEventTime(new Date());
        event.setEventType(eventType);
        event.setSeverity(severity);
        eventsAndFaultDao.saveOrUpdateEvent(event);

    } // end of method addEvent

 // it does not check whether an event of the same type is outstanding
    public void addEvent(Gateway gateway, String desc, String eventType) {

        EventsAndFault event = new EventsAndFault();
        event.setDevice(gateway);
        event.setActive(true);
        event.setDescription(desc);
        event.setEventTime(new Date());
        event.setEventType(eventType);
        event.setSeverity(ServerMain.getInstance().getEventType(eventType).getSeverityString());
        eventsAndFaultDao.saveOrUpdateEvent(event);
        
    } // end of method addEvent
    
    // it does not check whether an event of the same type is outstanding
    public void addEvent1(Gateway gateway, String desc, String eventType, String severity) {

        EventsAndFault event = new EventsAndFault();
        event.setDevice(gateway);
        event.setActive(true);
        event.setDescription(desc);
        event.setEventTime(new Date());
        event.setEventType(eventType);
        event.setSeverity(severity);
        eventsAndFaultDao.saveOrUpdateEvent(event);
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

}
