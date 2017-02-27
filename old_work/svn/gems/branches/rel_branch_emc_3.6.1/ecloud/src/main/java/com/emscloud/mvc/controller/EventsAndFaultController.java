package com.emscloud.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.emscloud.types.EventSeverity;



@Controller
@RequestMapping("/events")
public class EventsAndFaultController {
    
    @RequestMapping(value = "/list.ems", method = RequestMethod.GET)
    public String listEvents(Model model) {
        return "events/list";
    }
    @RequestMapping(value = "/filter.ems", method = RequestMethod.GET)
    public String filterEvents(Model model) {
        List<String> eventSeverities = new ArrayList<String>();
        eventSeverities.add(EventSeverity.SEVERE.getName());
        eventSeverities.add(EventSeverity.INFO.getName());
        eventSeverities.add(EventSeverity.WARNING.getName());
        model.addAttribute("severities", eventSeverities);
        return "events/filter";
    }
}
