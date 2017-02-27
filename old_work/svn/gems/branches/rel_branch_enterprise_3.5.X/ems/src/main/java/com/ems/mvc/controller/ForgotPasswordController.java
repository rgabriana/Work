package com.ems.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for forgot password
 * @author Admin
 *
 */

@Controller
@RequestMapping("/forgotpassword")
public class ForgotPasswordController {
	
	 	@RequestMapping(value = "home.ems")
		public String forgotPassword(Model model) {
				return "/forgotpass/home";
		}
}
