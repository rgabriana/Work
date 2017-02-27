package com.ems.mvc.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.ems.model.Wds;
import com.ems.service.SystemConfigurationManager;
import com.ems.service.WdsManager;
import com.ems.utils.ArgumentUtils;



@Controller
@RequestMapping("/wdsreport")
public class WdsReportController extends AbstractController {
	
	@Resource(name = "wdsManager")
    private WdsManager wdsManager;
	
	@Resource(name = "systemConfigurationManager")
   	SystemConfigurationManager systemConfigurationManager ;
		
	@RequestMapping(value = "/downloadWdsPdfReport.ems")
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		Map<String,Object> wdsReportData = new HashMap<String,Object>();
		
		List<Wds> wdsList = wdsManager.loadAllWds();
		
		if(!ArgumentUtils.isNullOrEmpty(wdsList)){
        	int normalMin = 0,lowMin = 0;
			String val = systemConfigurationManager.loadConfigByName("wds.normal.level.min").getValue();
		    if(val!=null)
		    {
		    	normalMin = Integer.parseInt(val);
		    }
		    val = systemConfigurationManager.loadConfigByName("wds.low.level.min").getValue();
		    if(val!=null)
		    {
		    	lowMin = Integer.parseInt(val);
		    }
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        Date now = new Date();
	        
        	for (int i = 0; i < wdsList.size(); i++) {
                Wds wds = wdsList.get(i);
                wdsManager.setWDSBatteryLevel(wds,normalMin,lowMin,now,sdf);
            }
        	wdsReportData.put("wdsList", wdsList);
        	
        }
		
		return new ModelAndView("WdsReportPdf","wdsReportData",wdsReportData);
	}
}
