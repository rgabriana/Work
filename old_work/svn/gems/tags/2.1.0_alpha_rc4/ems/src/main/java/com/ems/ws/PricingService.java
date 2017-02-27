/**
 * 
 */
package com.ems.ws;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Controller;

import com.ems.model.Pricing;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.PricingManager;
import com.ems.types.UserAuditActionType;
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
}
