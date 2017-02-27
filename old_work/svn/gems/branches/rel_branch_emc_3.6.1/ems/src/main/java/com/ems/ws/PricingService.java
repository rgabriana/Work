/**
 * 
 */
package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import com.ems.model.Pricing;
import com.ems.model.SystemConfiguration;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.CompanyManager;
import com.ems.service.PricingManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.UserAuditActionType;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/pricing")
public class PricingService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;

	
	@Resource(name = "pricingManager")
	private PricingManager pricingManager;
	@Autowired
    private MessageSource messageSource;
	@Resource
    private SystemConfigurationManager systemConfigurationManager;
	
	@Resource (name= "companyManager")
	private CompanyManager companyManager;
	
	
	 private static final Logger m_Logger = Logger.getLogger("WSLogger");

	 
	public PricingService() {
	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	/**
	 * Updates the existing pricing
	 * 
	 * @param Pricing
	 *            pricing
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("update")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updatePricing(Pricing pricing) {
		Response oStatus = new Response();
		String msg = pricingManager.updatePricing(pricing);
		if (!"S".equals(msg)) {
			oStatus.setStatus(1);
			oStatus.setMsg(msg);
		}
		userAuditLoggerUtil.log("Update pricing with id: " + pricing.getId(), UserAuditActionType.Pricing_Update.getName());
		return oStatus;
	}

	/**
	 * Adds new pricing
	 * 
	 * @param Pricing
	 *            pricing
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("add")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response addPricing(Pricing pricing) {
		Response oStatus = new Response();
		String msg = pricingManager.addPricing(pricing);
		if (!"S".equals(msg)) {
			oStatus.setStatus(1);
			oStatus.setMsg(msg);
		} else {
			oStatus.setMsg(pricing.getId().toString());
		}
		userAuditLoggerUtil.log("Add pricing with id: " + pricing.getId(), UserAuditActionType.Pricing_Update.getName());
		return oStatus;
	}

	/**
	 * Deletes existing pricing
	 * 
	 * @param Pricing
	 *            pricing
	 * @return Response status
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("delete")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response deletePricing(Pricing pricing) {
		Response oStatus = new Response();
		String msg = pricingManager.deletePricing(pricing);
		if (!"S".equals(msg)) {
			oStatus.setStatus(1);
			oStatus.setMsg(msg);
		}
		userAuditLoggerUtil.log("Delete pricing with id: " + pricing.getId(), UserAuditActionType.Pricing_Update.getName());
		return oStatus;
	}
	
	/**
	 * Updates the existing pricing
	 * 
	 * @param Pricing
	 *            pricing
	 * @return Response status
	 */
	@Path("updatepricingconfiguration/{pricingType}/{price}/{currency}")
	@POST
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response updatePricingConfiguration(@PathParam("pricingType") String pricingType, @PathParam("price") Float price,@PathParam("currency") String currency) {
		Response oStatus = new Response();
		Map<String, Object> nameValMap = new HashMap<String, Object>();
		nameValMap.put("pricingType", pricingType);
		nameValMap.put("floatPrice", price);
		nameValMap.put("currency", currency);
		oStatus = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
	    if(oStatus!= null && oStatus.getStatus()!=200){
	    	m_Logger.error("Validation error"+oStatus.getMsg());
	    	return oStatus;
	    }
		SystemConfiguration pricingTypeConfig = systemConfigurationManager.loadConfigByName("enable.pricing");
		if(pricingTypeConfig!=null)
		{
			pricingTypeConfig.setValue(pricingType.toString());
			systemConfigurationManager.save(pricingTypeConfig);				
		}
		
		companyManager.getCompany().setPrice(price);
		
		SystemConfiguration pricingCurrency = systemConfigurationManager.loadConfigByName("pricing.currency");
		if(pricingCurrency!=null)
		{
			pricingCurrency.setValue(currency.toString());
			systemConfigurationManager.save(pricingCurrency);				
		}
		
		if("1".equalsIgnoreCase(pricingType)){
			userAuditLoggerUtil.log("Update Pricing Type to Flat Pricing,Default Electricity rate to"+price.toString()+" and Currency to "+ currency.toString(), UserAuditActionType.Pricing_Configuration_Update.getName());
		}else{
			userAuditLoggerUtil.log("Update Pricing Type to Time of Use (TOU) Pricing and Currency to "+ currency.toString(), UserAuditActionType.Pricing_Configuration_Update.getName());
		}
		
		oStatus.setStatus(1);
		
		return oStatus;
	}
	
	@Path("getPricingCurrencyType")
    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public String getPricingCurrencyType() {
		SystemConfiguration pricingCurrency = systemConfigurationManager.loadConfigByName("pricing.currency");
		if(pricingCurrency!=null)
		{
			return pricingCurrency.getValue();
		}else{
			return "USD";
		}
    }
	
	
}
