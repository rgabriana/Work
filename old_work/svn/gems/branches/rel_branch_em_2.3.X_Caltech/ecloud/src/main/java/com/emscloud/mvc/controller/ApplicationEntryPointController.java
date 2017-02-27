package com.emscloud.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class ApplicationEntryPointController {
	
	@RequestMapping(value = "/home.ems")
	public String entryPoint() {
		return "redirect:/createCustomer.ems";
	}
}
