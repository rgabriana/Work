package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.service.EventsAndFaultManager;
import com.ems.service.GroupManager;
import com.ems.service.MetaDataManager;
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
    	model.addAttribute("events", metaDataManager.getEventTypes());
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
