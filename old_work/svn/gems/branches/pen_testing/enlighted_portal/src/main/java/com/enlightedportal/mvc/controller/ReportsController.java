package com.enlightedportal.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.enlightedportal.types.UserAuditActionType;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    
    @RequestMapping("/auditlog.ems")
    public String getAuditLog(Model model) {
        return "reports/auditlog";
    }
    
    
	@RequestMapping(value = "/auditfilter.ems", method = RequestMethod.GET)
	public String filterAudits(Model model) {
		List<String> actions = new ArrayList<String>();
		for (UserAuditActionType type: UserAuditActionType.values()) {
			actions.add(type.getName());
		}
		model.addAttribute("actions", actions);
		return "reports/auditfilter";
	}
}