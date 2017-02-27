package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.model.EventType;
import com.ems.model.EventsAndFault;
import com.ems.model.SystemConfiguration;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.GroupManager;
import com.ems.service.MetaDataManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.util.Constants;

@Controller
@RequestMapping("/events")
public class EventsAndFaultController {
    @Resource(name = "eventsAndFaultManager")
    private EventsAndFaultManager eventAndFaultManager;
    @Resource(name = "groupManager")
    private GroupManager groupManager;
    @Resource(name = "metaDataManager")
    private MetaDataManager metaDataManager;
    @Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
    @RequestMapping(value = "/list.ems", method = RequestMethod.GET)
    public String listEvents(Model model) {
        return "events/list";
    }

    @RequestMapping(value = "/view.ems", method = RequestMethod.POST)
    public String viewEvents(@RequestParam("id") String id, Model model) {
        model.addAttribute("eventsandfaultView", eventAndFaultManager.getEventById(Long.parseLong(id)));
        return "events/view";
    }

    @RequestMapping(value = "/filter.ems", method = RequestMethod.GET)
    public String filterEvents(Model model) {
    	model.addAttribute("groups", groupManager.loadAllGroups());
    	List<EventType> eventList = metaDataManager.getEventTypes();
    	 
    	 SystemConfiguration bulbConfigurationEnableConfig = systemConfigurationManager.loadConfigByName("bulbconfiguration.enable");
         if (bulbConfigurationEnableConfig != null && "false".equalsIgnoreCase(bulbConfigurationEnableConfig.getValue())) {
        	 if (eventList != null && eventList.size() > 0) {
     			for (Iterator<EventType> iterator = eventList.iterator(); iterator.hasNext();) {
     				EventType bulbCOnfigurationObj = (EventType) iterator.next();
     				if(bulbCOnfigurationObj.getType().equalsIgnoreCase(EventsAndFault.FIXTURE_BULB_OUTAGE_EVENT_STR))
     				{
     					eventList.remove(bulbCOnfigurationObj);
     					break;
     				}
     			}
     		}
         }
    	model.addAttribute("events", eventList);
		List<String> eventSeverities = new ArrayList<String>();
		eventSeverities.add(Constants.SEVERITY_CRITICAL);
		eventSeverities.add(Constants.SEVERITY_MAJOR);
		eventSeverities.add(Constants.SEVERITY_MINOR);
		eventSeverities.add(Constants.SEVERITY_WARNING);
		eventSeverities.add(Constants.SEVERITY_INFORMATIONAL);
		model.addAttribute("severities", eventSeverities);
        return "events/filter";
    }
    
	@RequestMapping("/resolve/comment.ems")
    public String getResolveCommentDialog(Model model){
        return "events/resolveDialog";
    }
    
}
