package com.emscloud.ws ;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.emscloud.model.Customer;
import com.emscloud.model.CustomerDetailedBill;
import com.emscloud.model.CustomerSppaBill;
import com.emscloud.model.Site;
import com.emscloud.model.SppaBill;
import com.emscloud.mvc.controller.util.ControllerUtils;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.SiteManager;
import com.emscloud.service.SppaManager;


@Controller
@Path("/sppa")
public class SppaService {
	
	@Resource
	SppaManager sppaManager;
	
	@Resource
	EmInstanceManager emInstanceManger;
	@Resource
	SiteManager siteManger;
	@Resource
	CustomerManager customerManager;
	
	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	public SppaService() {
	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	/**
	 * Returns list of bills per customer
	 * 
	 * @return bill list
	 */
	@Path("generateLastMonthCustomerBill/{custId}/{startDate}/{endDate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public CustomerSppaBill generateLastMonthBillPerCustomer(@PathParam("custId") Long custId,@PathParam("startDate") String startDateStr,@PathParam("endDate") String endDateStr) {
		
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
		
		Date startDate = new Date();
		try {
			startDate = df.parse(startDateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Date endDate = new Date();
		try {
			endDate = df.parse(endDateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Customer customer = customerManager.loadCustomerById(custId);
		CustomerDetailedBill customerBill = sppaManager.getBillReportCustomer(customer,startDate,endDate, false, null);
		return customerBill.getBillInvoice();
    	
	} //end of method generateLastMonthBillPerCustomer
	
	/**
	 * Returns list of bills per customer for Each Em
	 * 
	 * @return bill list
	 */
	@Path("generateLastMonthCustomerBillList/{custId}/{startDate}/{endDate}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<SppaBill> generateLastMonthBillListPerCustomer(@PathParam("custId") Long custId,@PathParam("startDate") String startDateStr,@PathParam("endDate") String endDateStr) {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
		
		Date startDate = new Date();
		try {
			startDate = df.parse(startDateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Date endDate = new Date();
		try {
			endDate = df.parse(endDateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//get all the em instances
		List<Site> sites = siteManger.loadSitesByCustomer(custId);
		Iterator<Site> siteIter = sites.iterator();
		SppaBill sppaBill = null;
		List<SppaBill> sppaBillList = new ArrayList<SppaBill>();
		while(siteIter.hasNext()) {
			Site site = siteIter.next();
			sppaBill = sppaManager.generateLastMonthBillPerSite(site, startDate, endDate);
			if (sppaBill != null) {
				sppaBillList.add(sppaBill);
			}
		}
		return sppaBillList;
		
	} //end of method generateLastMonthBillListPerCustomer
	
	/**
	 * Returns list of bills per customer
	 * 
	 * @return bill list
	 */
	@Path("getLastMonthCustomerBill/{custId}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<SppaBill> getLastMonthBillPerCustomer(@PathParam("custId") Long custId) {
		List<SppaBill> billList = sppaManager.getLastMonthBillPerCustomer(custId);
		if(billList != null || !billList.isEmpty()) {
			return billList;
		}
		return null ;
    	
	} //end of method generateLastMonthBillPerCustomer
	
	
	/**
     * Checks for previously executing billing task and returns message to UI to restrict the user to generate further bill till previous bill is successfully generated. 
     * 
     * @return bill list
     */
    @Path("getCurrentBillRunningStatus")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getCurrentBillingTaskStatus() {
        Response res = new Response();
        if (ControllerUtils.getInstance().getBillProcessRunningStatus()==1) {
            res.setStatus(-1);
        }
        return res;
        
    }
    
//	/**
//	 * Returns bill per em site
//	 * 
//	 * @return bill
//	 */
//	@Path("generateLastMonthSiteBill/{emId}")
//	@GET
//	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
//	public SppaBill generateLastMonthBillPerEmSite(@PathParam("emId") Long emId) {
//		SppaBill bill = sppaManager.generateLastMonthBillPerSite(emId);
//		return bill;
//    	
//	} //end of method generateLastMonthBillPerEmSite
      
} //end of class SppaService
