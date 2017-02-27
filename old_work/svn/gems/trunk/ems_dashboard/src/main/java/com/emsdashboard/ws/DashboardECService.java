package com.emsdashboard.ws;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.hibernate.classic.Session;
import org.springframework.stereotype.Controller;


import com.emsdashboard.model.Avgrecord;
import com.emsdashboard.model.DashboardRecord;
import com.emsdashboard.model.GemsServer;
import com.emsdashboard.model.MeterRecord;
import com.emsdashboard.service.DashboardDataManager;

@Controller
@Path("org/ecDashboard")
public class DashboardECService{
    static final Logger logger = Logger.getLogger(DashboardECService.class.getName());
    
    @Resource(name = "dashboardDataManager")
    private DashboardDataManager dashboardDataManager;
    
    @Context
    ServletContext context;
    
   
   
   
}
