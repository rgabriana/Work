package com.emscloud.mvc.controller;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.Customer;
import com.emscloud.model.Facility;
import com.emscloud.model.Site;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.FacilityManager;
import com.emscloud.service.SiteManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.types.FacilityType;
import com.emscloud.util.CloudAuditLoggerUtil;

@Controller
@RequestMapping("/sites")
public class SiteController {
   
   @Resource
   private CustomerManager customerManager; 
   @Resource
   private SiteManager siteManager;
   @Resource
 	FacilityManager facilityManager;

   @Resource
   CloudAuditLoggerUtil cloudAuditLoggerUtil;
  
   @RequestMapping(value = "/list.ems", method = { RequestMethod.GET, RequestMethod.POST })
   public String listEmInstance(Model model,  @RequestParam("customerId") long customerId)
   {
       model.addAttribute("customerId", customerId);
       Customer customer = customerManager.loadCustomerById(customerId);
       model.addAttribute("customerName", customer.getName());
       return "sites/list";
   }
   @RequestMapping("/create.ems")
   @PreAuthorize("hasAnyRole('Admin')")
   String createSite(Model model, @RequestParam("customerId") Long customerId) {
       Site site = new Site();
       site.setTaxRate((double) 0);
       model.addAttribute("site", site);
       model.addAttribute("customerId", customerId);
       model.addAttribute("mode", "create");
       return "sites/siteform";
   }
   
   @RequestMapping("/edit.ems")
   @PreAuthorize("hasAnyRole('Admin')")
   String editEmInstance(Model model, @RequestParam("siteId") Long siteId) {
       Site site = siteManager.loadSiteById(siteId);
       model.addAttribute("site", site);
       model.addAttribute("customerId", site.getCustomer().getId());
       model.addAttribute("mode", "edit");
       return "sites/siteform";
   }
   @RequestMapping("/save.ems")
   String saveSite(Site site, @RequestParam("customerId") Long customerId) {
       Customer customer = customerManager.loadCustomerById(customerId);
       if(site.getId() != 0 )
       {
           Site dbsite = siteManager.loadSiteById(site.getId());
           dbsite.setName(site.getName());
           dbsite.setGeoLocation(site.getGeoLocation());
           dbsite.setRegion(site.getRegion());
           dbsite.setSppaPrice(site.getSppaPrice());
           dbsite.setSquareFoot(site.getSquareFoot());
           dbsite.setBillStartDate(site.getBillStartDate());
           dbsite.setBlockPurchaseEnergy(site.getBlockPurchaseEnergy());
           dbsite.setPoNumber(site.getPoNumber());
           dbsite.setTaxRate(site.getTaxRate());
           dbsite.setEstimatedBurnHours(site.getEstimatedBurnHours());
           siteManager.saveOrUpdate(dbsite);
           cloudAuditLoggerUtil.log("Updated Site: "+dbsite.getName()+" for Customer "+customer.getName(), CloudAuditActionType.Customer_Site_Update.getName());
       }else
       {
           site.setCustomer(customer);
           siteManager.saveOrUpdate(site);
           cloudAuditLoggerUtil.log("Created Site: "+site.getName()+" for Customer "+customer.getName(), CloudAuditActionType.Customer_Site_Create.getName());
           
           Facility childFacility = facilityManager.getFacility(site.getName());
           if(childFacility==null)
           {
	           childFacility = new Facility();
	           childFacility.setName(site.getName());
	           childFacility.setParentId(facilityManager.getOrganizationIdOfCustomer(customerId));
	           childFacility.setType(FacilityType.getFacilityType(FacilityType.CAMPUS));
	           childFacility.setCustomerId(customerId);
	           facilityManager.addFacility(childFacility);
           }
       }
       return "redirect:/sites/list.ems?customerId=" + customerId ;
   }
   
   @RequestMapping(value = "/viewsitedetails.ems", method = RequestMethod.POST)
   String viewSiteMainDetails(Model model, @RequestParam("siteId") Long siteId) {
       Site site = siteManager.loadSiteById(siteId);
       model.addAttribute("siteId", siteId);
       model.addAttribute("siteName", site.getName());
       model.addAttribute("customerId", site.getCustomer().getId());
       return "sites/mainviewdetails";
       
   }
   @RequestMapping(value = "/details.ems")
   public String viewSiteDetail(Model model, @RequestParam("siteId") Long siteId)
   {
       Site site;
       site = siteManager.loadSiteById(siteId);
       model.addAttribute("siteDetailsView", site);
       model.addAttribute("customerId", site.getCustomer().getId());
       return "sites/sitedetails";
   }
   @RequestMapping(value = "/loadsitesemlist.ems")
   public String viewSitesEmList(Model model, @RequestParam("siteId") Long siteId)
   {
       Site site;
       site = siteManager.loadSiteById(siteId);
       model.addAttribute("siteId", site.getId());
       model.addAttribute("siteName", site.getName());
       model.addAttribute("customerId", site.getCustomer().getId());
       return "sites/sitesemlist";
   }
   @RequestMapping(value = "/assignemtositemapping.ems")
   public String assignEmToSiteMapping(Model model, @RequestParam("siteId") Long siteId)
   {
       Site site;
       site = siteManager.loadSiteById(siteId);
       List<Object[]> emList =  siteManager.getUnmappedEmInstList(site.getCustomer().getId());
       model.addAttribute("siteId", site.getId());
       model.addAttribute("siteName", site.getName());
       model.addAttribute("customerId", site.getCustomer().getId());
       model.addAttribute("emList", emList);
       return "sites/siteemmapping";
   }
   @RequestMapping(value="/viewallanomaliesdetails.ems")
   public String viewSiteAnomalyDetails(Model model, @RequestParam("customerId") Long customerId)
   {
	   Customer customer = customerManager.loadCustomerById(customerId);
       model.addAttribute("customerName", customer.getName());
	   model.addAttribute("customerId", customerId);
	   
	   Date startDate = null;
	   Date endDate = null;
	   Calendar calendar = Calendar.getInstance();
	   calendar.set(Calendar.HOUR_OF_DAY, 0);
	   calendar.set(Calendar.MINUTE, 0);
	   calendar.set(Calendar.SECOND, 0);
	   calendar.set(Calendar.MILLISECOND, 0);
	   //calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
	   endDate = calendar.getTime();
	   calendar.add(Calendar.DATE, -30);
	   //calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMinimum(Calendar.DAY_OF_MONTH));	   
	   startDate = calendar.getTime();
	   SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss a");
	   model.addAttribute("startDate", sdf.format(startDate));
	   model.addAttribute("endDate", sdf.format(endDate));
	   
	   return "sites/viewallanomaliesdetails";
   }
   @RequestMapping(value = "/viewsiteanoaliesdetails.ems")
   public String viewSitesAnomalyList(Model model, @RequestParam("siteId") Long siteId)
   {
       Site site;
       site = siteManager.loadSiteById(siteId);
       model.addAttribute("siteId", site.getId());
       model.addAttribute("siteName", site.getName());
       model.addAttribute("customerId", site.getCustomer().getId());
      
       Date startDate = null;
	   Date endDate = null;
	   Calendar calendar = Calendar.getInstance();
	   calendar.set(Calendar.HOUR_OF_DAY, 0);
	   calendar.set(Calendar.MINUTE, 0);
	   calendar.set(Calendar.SECOND, 0);
	   calendar.set(Calendar.MILLISECOND, 0);
	   //calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
	   endDate = calendar.getTime();
	   //calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
	   calendar.add(Calendar.DATE, -30);
	   startDate = calendar.getTime();
	   SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss a");
	   model.addAttribute("startDate", sdf.format(startDate));
	   model.addAttribute("endDate", sdf.format(endDate));
       return "sites/sitesanomalieslist";
   }
  
    @RequestMapping(value = "/filtersiteanomalyprompt.ems")
	public String billingPaymentPromt(Model model, @RequestParam("id") long id,@RequestParam("mode") String modeType)
	{
    	if(modeType.equalsIgnoreCase("customer"))
    	{
    		model.addAttribute("customerId", id);
    		Customer customer = customerManager.loadCustomerById(id);
    		model.addAttribute("customerName", customer.getName());
    	}else if(modeType.equalsIgnoreCase("site") || modeType.equalsIgnoreCase("analysis"))
    	{
    		Site site = siteManager.loadSiteById(id);
    		model.addAttribute("siteId", id);
    		model.addAttribute("siteName", site.getName());
    	    model.addAttribute("customerId", site.getCustomer().getId());
    	}
    	else
    	{
    		model.addAttribute("customerId", id);
    		Customer customer = customerManager.loadCustomerById(id);
    		model.addAttribute("customerName", customer.getName());
    	}
    	model.addAttribute("modeType", modeType);
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Calendar cal = Calendar.getInstance();
	    Date currentServerDate = new Date();
	    cal.setTime(currentServerDate);
	    cal.add(Calendar.DAY_OF_MONTH, -1);
	    currentServerDate = cal.getTime();
		String currentServerDateStr = df.format(currentServerDate);
		model.addAttribute("currentServerDateStr", currentServerDateStr);
       return "sites/filterSiteAnomalyPrompt";
	}
   
    @RequestMapping(value = "/filtersiteanomalybydate.ems")
   	public String filterSiteAnomalyByDate(Model model, @RequestParam("fsaId") long Id,@RequestParam("fsastartDateId") String startDateStr,@RequestParam("fsaendDateId") String endDateStr,@RequestParam("fsamode") String modeType)
   	{
    	String url =""; 
       if(modeType.equalsIgnoreCase("customer"))
       {
    	   // MODE - customer - Called from All Anomalies button from sites listing page
    	   Customer customer = customerManager.loadCustomerById(Id);
           model.addAttribute("customerName", customer.getName());
      	   model.addAttribute("customerId", Id);
      	   url = "sites/viewallanomaliesdetails";
       }else if(modeType.equalsIgnoreCase("site"))
       {
    	   // MODE - site : called from site details's site Anomaly page
    	   Site site;
           site = siteManager.loadSiteById(Id);
           model.addAttribute("siteId", site.getId());
           model.addAttribute("siteName", site.getName());
           model.addAttribute("customerId", site.getCustomer().getId());
           url = "sites/sitesanomalieslist";
       }else if(modeType.equalsIgnoreCase("analysis"))
       {
    	   // MODE - Analysis : Called from Bill Anomaly Report Page
    	   Site site;
           site = siteManager.loadSiteById(Id);
           model.addAttribute("siteId", site.getId());
           model.addAttribute("siteName", site.getName());
           model.addAttribute("customerId", site.getCustomer().getId());
           url = "sites/analysesitesanomalieslist";
       }else
       {
    	   // MODE - bill : called from Bill Anomaly Report button on bill listing page 
    	   Customer customer = customerManager.loadCustomerById(Id);
           model.addAttribute("customerName", customer.getName());
      	   model.addAttribute("customerId", Id);
      	   url = "bill/billanomalieslist";
       }
  	   Date startDate = null;
  	   Date endDate = null;
  	   Calendar calendar = Calendar.getInstance();
  	   calendar.set(Calendar.HOUR_OF_DAY, 0);
	   calendar.set(Calendar.MINUTE, 0);
	   calendar.set(Calendar.SECOND, 0);
	   calendar.set(Calendar.MILLISECOND, 0);
  	   SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss a");
  	   DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	   if (startDateStr != null && !"".equals(startDateStr)) {
			try {
			   startDate = (Date) df.parse(startDateStr);
			} catch (ParseException e) {
		  	   calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		  	   startDate = calendar.getTime();
			}
	   }
	   if (endDateStr != null && !"".equals(endDateStr)) {
		   try {
			   endDate = (Date) df.parse(endDateStr);
			}catch(ParseException e)
			{
				 calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			  	 endDate = calendar.getTime();
			}
	   }
  	   model.addAttribute("startDate", sdf.format(startDate));
  	   model.addAttribute("endDate", sdf.format(endDate));
  	   return url;
   	}
    @RequestMapping(value = "/billanomalies.ems")
	public String billAnomaliesDetails(@RequestParam("crfCustomerId") Long customerId,@RequestParam("crfCustomerBillId") Long customerBillId,@RequestParam("crfstartDateId") String startDateStr,
			@RequestParam("crfendDateId") String endDateStr,Model model, HttpServletResponse httpResponse)
	{
		model.addAttribute("customerId", customerId);
		Customer customer = customerManager.loadCustomerById(customerId);
		model.addAttribute("customerName", customer.getName());
		Date startDate = null;
		Date endDate = null;
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss a");
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		if (startDateStr != null && !"".equals(startDateStr)) {
			try {
				startDate = (Date) df.parse(startDateStr);
			} catch (ParseException e) {
				calendar.set(Calendar.DAY_OF_MONTH,
						calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
				startDate = calendar.getTime();
			}
		}
		if (endDateStr != null && !"".equals(endDateStr)) {
			try {
				endDate = (Date) df.parse(endDateStr);
			} catch (ParseException e) {
				calendar.set(Calendar.DAY_OF_MONTH,
						calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				endDate = calendar.getTime();
			}
		}

		String startDateVal = sdf.format(startDate);
		String endDateVal = sdf.format(endDate);
		model.addAttribute("startDate", startDateVal );
		model.addAttribute("endDate", endDateVal);
		Cookie billstartdate;
		try {
			billstartdate = new Cookie("billstartdate", URLEncoder.encode(startDateVal, "UTF-8"));
			billstartdate.setPath("/");
			httpResponse.addCookie(billstartdate);
			Cookie billenddate = new Cookie("billenddate", URLEncoder.encode(endDateVal, "UTF-8"));
			billenddate.setPath("/");
			httpResponse.addCookie(billenddate);
		} catch (UnsupportedEncodingException e) {
		}
		return "bill/billanomalieslist";
	}
    @RequestMapping(value = "/analysesiteanomaliesbygeoloc.ems")
    public String AnalyseSitesAnomalyList(Model model, @RequestParam("bageoLocation") String geoLocation,@RequestParam("bastartDateId") String startDateStr,@RequestParam("baendDateId") String endDateStr)
    {
    	Site site;
        site = siteManager.getSiteByGeoLocation(geoLocation);
        model.addAttribute("siteId", site.getId());
        model.addAttribute("siteName", site.getName());
        model.addAttribute("customerId", site.getCustomer().getId());
   	   Date startDate = null;
   	   Date endDate = null;
   	   Calendar calendar = Calendar.getInstance();
   	   calendar.set(Calendar.HOUR_OF_DAY, 0);
 	   calendar.set(Calendar.MINUTE, 0);
 	   calendar.set(Calendar.SECOND, 0);
 	   calendar.set(Calendar.MILLISECOND, 0);
   	   SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss a");
   	   DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
 	   if (startDateStr != null && !"".equals(startDateStr)) {
 			try {
 			   startDate = (Date) df.parse(startDateStr);
 			} catch (ParseException e) {
 		  	   calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
 		  	   startDate = calendar.getTime();
 			}
 	   }
 	   if (endDateStr != null && !"".equals(endDateStr)) {
 		   try {
 			   endDate = (Date) df.parse(endDateStr);
 			}catch(ParseException e)
 			{
 				 calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
 			  	 endDate = calendar.getTime();
 			}
 	   }
   	   model.addAttribute("startDate", sdf.format(startDate));
   	   model.addAttribute("endDate", sdf.format(endDate));
   	   return "sites/analysesitesanomalieslist";
    }
}