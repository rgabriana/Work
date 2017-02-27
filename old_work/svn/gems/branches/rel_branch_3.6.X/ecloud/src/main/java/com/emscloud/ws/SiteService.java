package com.emscloud.ws;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.communication.utils.ArgumentUtils;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.adaptor.CloudAdapter;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.model.EmSite;
import com.emscloud.model.Site;
import com.emscloud.model.SiteAnomalyList;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.InventoryReportManager;
import com.emscloud.service.SiteManager;
import com.emscloud.service.SppaManager;
import com.emscloud.service.SystemConfigurationManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.util.CloudAuditLoggerUtil;
import com.emscloud.util.CommonUtils;
import com.emscloud.vo.SiteList;
import com.sun.jersey.api.client.GenericType;


@Controller
@Path("/org/site/v1")
public class SiteService {
	private static final Logger logger = Logger.getLogger("WSAPI");

	@Resource
	SiteManager siteManager;

	@Resource
	InventoryReportManager inventoryReportManager;
	
	@Resource
	CustomerManager customerManager;
	
	@Resource
	EmInstanceManager emInstanceManager;
	@Resource
	SppaManager sppaManager;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
	@Resource
	CloudAdapter cloudAdapter;
	
	@Resource
	SystemConfigurationManager systemConfigurationManager;
	
    @Path("getsitelistbycustomerid/{custId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SiteList getSitelListByCustomerId(@PathParam("custId") Long custId,
            @RequestParam("data") String userdata) throws UnsupportedEncodingException {
        SiteList siteList = siteManager.getSitesByCustomerWithSpecificAttibute(custId,userdata);
        return siteList;
    }
    
    @Path("getSitesOfCustomer/{custId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<com.emscloud.vo.Site> getSitesOfCustomer(@PathParam("custId") Long custId) throws UnsupportedEncodingException {
    	
    	List<com.emscloud.vo.Site> siteList = new ArrayList<com.emscloud.vo.Site>();
      String metaDataServerIp = CommonUtils.getMetaDataServer(systemConfigurationManager);
    	if(metaDataServerIp.equalsIgnoreCase("localhost")) {
    		siteList = siteManager.loadSiteFacilitiesByCustomer(custId);
    		return siteList;
    	}
    	try{  			
  			ResponseWrapper<List<com.emscloud.vo.Site>> response = cloudAdapter.executeGetList(metaDataServerIp, 
  					"/ecloud/services/internal/v1/getSitesOfCustomer/" + custId, MediaType.APPLICATION_JSON,
  					new GenericType<List<com.emscloud.vo.Site>>() {});  					        
  			if (response.getStatus()== javax.ws.rs.core.Response.Status.OK.getStatusCode()) {
  				siteList = response.getItems();
  			}
    	}catch(Exception e) {
  			e.printStackTrace();
  		}
    	return siteList;
      
    } //end of method getSitesOfCustomer
    
    @Path("checkassociatedsite/{siteId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response checkassociatedsite(@PathParam("siteId") Long siteId,
            @RequestParam("data") String userdata) {
        Response reponse = new Response();
        List<Long> siteList = siteManager.getSiteEms(siteId);
        reponse.setStatus(siteList.size());
        return reponse;
    }
    @Path("delete/{siteId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response deletSite(@PathParam("siteId") Long siteId) {
        Response reponse = new Response();
        Site site = siteManager.loadSiteById(siteId);
        int status = siteManager.deleteSite(siteId);
        cloudAuditLoggerUtil.log("Deleted Site: "+site.getName()+" of Customer "+site.getCustomer().getName(), CloudAuditActionType.Customer_Site_Delete.getName());
        reponse.setStatus(status);
        return reponse;
    }
    
    @Path("loademinstbysiteid/{siteId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmInstanceList loadEmInstBySiteId(@PathParam("siteId") Long siteId,
            @RequestParam("data") String userdata) throws UnsupportedEncodingException, ParseException {
        EmInstanceList oEmInstList = siteManager.loadEmInstBySiteId(siteId,userdata);
        return oEmInstList;
    }
    /**
     * This service will map EM to site
     * 
     * @param siteId
     *              Site id to which the em need to be mapped
     * @param emInstances
     *            List of emInstances
     *            "<eminstances><eminstance><id>1</id></eminstance></eminstances>"
     * @return Response status
     */
    @Path("assignemtosite/{siteId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response assignEmToSite(@PathParam("siteId") Long siteId,List<EmInstance> emInstances) {
        Response reponse = new Response();
        Iterator<EmInstance> itr = emInstances.iterator();
        EmInstance emInstance;
        int count=0;
        while (itr.hasNext()) {
            EmSite emSite = new EmSite();
            emInstance = (EmInstance) itr.next();
            emSite.setEmId(emInstance.getId());
            emSite.setSiteId(siteId);
            siteManager.assignEmToSite(emSite);
            count++;
        }
        if(count>0 && count == emInstances.size())
        {
            reponse.setStatus(count);
        }
        return reponse;
    }
    
    /**
     * This service will ummap EM to site
     * 
     * @param siteId
     *              Site id to which the em need to be mapped
     * @param emInstances
     *            List of emInstances
     *            "<eminstances><eminstance><id>1</id></eminstance></eminstances>"
     * @return Response status
     */
    @Path("unassignemtosite/{siteId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response unAssignEmToSite(@PathParam("siteId") Long siteId,List<EmInstance> emInstances) {
        Response reponse = new Response();
        Iterator<EmInstance> itr = emInstances.iterator();
        EmInstance emInstance;
        int count=0;
        while (itr.hasNext()) {
            emInstance = (EmInstance) itr.next();
            EmSite emSite = siteManager.loadEMSiteByEmIdAndSiteId(emInstance.getId(),siteId);
            siteManager.unAssignEmToSite(emSite);
            count++;
        }
        if(count>0 && count == emInstances.size())
        {
            reponse.setStatus(count);
        }
        return reponse;
    }
    /**
     * Return Site
     * 
     * @param Site Name
     * @return Site for given name
     */
    @Path("checkforduplicatesite/{name}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Site getSite(@PathParam("name") String name) {
        return siteManager.getSiteByName(name);
    }
    
    @Path("validateSite/{siteId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response validateSite(@PathParam("siteId") Long siteId) {
    	
    	long startTime = System.currentTimeMillis();
    	Site site = siteManager.loadSiteById(siteId);
    	Response resp = new Response();
    	if(site == null) { 
    		resp.setMsg("No Site found - " + siteId);
    		resp.setStatus(1);
    	}    	
    	sppaManager.validateSiteBilling(site);
    	logger.error("time taken to validate site( " + siteId + ") -- " + (System.currentTimeMillis() - startTime));
    	return resp;
    	    	
    } //end of method validateSite
    
    @Path("validateMonthlyBill/{billId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response validateMonthlyBll(@PathParam("billId") Long billId) {
    	
    	long startTime = System.currentTimeMillis();    	
    	Response resp = new Response();    	  	
    	sppaManager.validateCustomerBill(billId);
    	logger.error("time taken to validate monthly bill ( " + billId + ") -- " + (System.currentTimeMillis() - startTime));
    	return resp;
    	    	
    } //end of method validateMonthlyBll
    
    @Path("validateAllSites/{custId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response validateAllSites(@PathParam("custId") Long custId) {
    	
    	long startTime = System.currentTimeMillis();
    	List<Site> sitesList = siteManager.loadSitesByCustomer(custId);
    	Response resp = new Response();
    	if(sitesList == null || sitesList.size() == 0) { 
    		resp.setMsg("No Sites found - " + custId);
    		resp.setStatus(1);
    	}    	
    	Iterator<Site> siteIter = sitesList.iterator();
    	while(siteIter.hasNext()) {
    		sppaManager.validateSiteBilling(siteIter.next());
    	}    	    	
    	logger.error("time taken to all validate all sites -- " + (System.currentTimeMillis() - startTime));
    	return resp;
    	    	
    } //end of method validateAllSites
    
    @Path("getallanamolieslistbysiteid/{siteId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SiteAnomalyList getAllAnamoliesListBySiteId(@PathParam("siteId") Long siteId,@RequestParam("data") String userdata) throws UnsupportedEncodingException, ParseException {
    	List<String> geoLocList = new ArrayList<String>();
    	Site site = siteManager.loadSiteById(siteId);
    	if(site!=null)
    	{
    		geoLocList.add(site.getGeoLocation());
    	}
    	SiteAnomalyList siteAnomaliesList = siteManager.getAllAnamoliesListBySiteId(geoLocList,userdata);
        return siteAnomaliesList;
    }
    @Path("getallanamolieslistbycustomerId/{customerId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SiteAnomalyList getAllAnomaliesListByCustomerId(@PathParam("customerId") Long customerId,@RequestParam("data") String userdata) throws UnsupportedEncodingException, ParseException {
    	List<String> geoLocList = new ArrayList<String>();
    	List<Site> SiteList = siteManager.loadSitesByCustomer(customerId);
    	if(SiteList!=null && SiteList.size()>0)
    	{
	    	Iterator<Site> itr = SiteList.iterator();
	    	while(itr.hasNext())
	    	{
	    		Site siteObj = itr.next();
	    		geoLocList.add(siteObj.getGeoLocation());
	    	}
    	}
    	SiteAnomalyList siteAnomaliesList = siteManager.getAllAnamoliesListBySiteId(geoLocList,userdata);
        return siteAnomaliesList;
    }
    @Path("getbillanamolieslistbycustomerId/{customerId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SiteAnomalyList getBillAnamoliesListByCustomerId(@PathParam("customerId") Long customerId,@RequestParam("data") String userdata) throws UnsupportedEncodingException, ParseException {
    	List<String> geoLocList = new ArrayList<String>();
    	List<Site> SiteList = siteManager.loadSitesByCustomer(customerId);
    	if(SiteList!=null && SiteList.size()>0)
    	{
	    	Iterator<Site> itr = SiteList.iterator();
	    	while(itr.hasNext())
	    	{
	    		Site siteObj = itr.next();
	    		geoLocList.add(siteObj.getGeoLocation());
	    	}
    	}
    	SiteAnomalyList siteAnomaliesList = siteManager.getAllAnamoliesListBySiteId(geoLocList,userdata);
        return siteAnomaliesList;
    }
    
    
    @Path("checksiteemlisttbycustomerid/{customerId}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response checkSiteEmListOfCustomer(@PathParam("customerId") long customerId) throws UnsupportedEncodingException {
      
      Response reponse = new Response();
      List<Site> siteList = siteManager.loadSitesByCustomer(customerId);
      if(!ArgumentUtils.isNullOrEmpty(siteList)){
    	  for(Site site : siteList){
    		  List<Long> emList = siteManager.getSiteEms(site.getId());
    		  if(!ArgumentUtils.isNullOrEmpty(emList)){
    			  reponse.setStatus(1);
    			  break;
    		  }
    	  }
      }
      return reponse;
      
    }
 }
