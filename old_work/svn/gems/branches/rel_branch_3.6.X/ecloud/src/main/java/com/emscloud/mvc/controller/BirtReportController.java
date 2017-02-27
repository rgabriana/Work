package com.emscloud.mvc.controller;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.emscloud.reports.ReportFormat;
import com.emscloud.service.ReportGenerationService;


@Controller
@RequestMapping("/reports")
public class BirtReportController {
	
	@Resource
	ReportGenerationService reportGenerationService;

	@RequestMapping(value = "/generate.ems", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public FileSystemResource  generateReport(HttpServletRequest request, HttpServletResponse response){
		ReportFormat format = ReportFormat.PDF;
		Map<String, Object> context = new HashMap<String, Object>();
		
		final Enumeration<String> attributeNames = request.getParameterNames();
		
		while( attributeNames.hasMoreElements()){
			String key = attributeNames.nextElement();
			context.put(key, request.getParameter(key));
		}
		
		File file = reportGenerationService.generateReport(context, "BillingReport.rptdesign", format);
		response.setContentType(format.getContentType());      
		response.setHeader("Content-Disposition", "attachment; filename=" +file.getName()); 
		return new FileSystemResource(file);
	}

}
