package com.enlightedportal.ws ;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.enlightedportal.model.Customer;
import com.enlightedportal.model.LicenseDetails;
import com.enlightedportal.model.LicensePanel;
import com.enlightedportal.service.CustomerManager;
import com.enlightedportal.service.LicenseDetailManager;



/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org")
public class InternalService {
	
	@Resource
	CustomerManager customerManager;
	@Resource
	LicenseDetailManager  licenseDetailManager;
	
    
    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public InternalService() {
    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;

   
    /**
     * Returns list of all customers
     * 
     * @return customer list
     */
    @Path("customer/list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Customer> getCustomerList() {
    	List<Customer> customers = customerManager.loadallCustomer();
    	if(customers!=null || !customers.isEmpty())
    	{
    		return customers ;
    	}
    	return null ;
    }

    /**
     * Return Customer
     * 
     * @param Customer Name
     * @return Customer for given name
     */
    @Path("customer/list/{name}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Customer getCustomer(@PathParam("name") String name) {
        return customerManager.loadCustomerByName(name);
    }
    /**
     * Return Customer
     * 
     * @param Customer Name
     * @return Customer for given name
     */
    @Path("customer/save")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void saveCustomer(Customer customer) {
        customerManager.saveOrUpdate(customer);
    }
    
    /**
     * Return License Key List
     * 
     * @param Customer Name
     * @return Customer for given name
     */
    @Path("customer/license/list/{name}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public ArrayList<LicensePanel> licenseList(@PathParam("name") String name) {
    	ArrayList<LicensePanel> lp = new ArrayList<LicensePanel>() ;
    	ArrayList<LicenseDetails> ld = new ArrayList<LicenseDetails>() ;
    	if(name==null||name.isEmpty())
    	{
    		name = "ALL";
    	}
    	
        if(name.equalsIgnoreCase("ALL"))
        {
        	ld = (ArrayList<LicenseDetails>) licenseDetailManager.loadAllLicenseDetails() ;
        	
        }
        else
        {
        	ld = (ArrayList<LicenseDetails>) licenseDetailManager.loadLicenseDetailsByCustomerId(customerManager.loadCustomerByName(name).getId()) ;
        
        }
        if(ld!=null && !ld.isEmpty())
        { 
        	for (LicenseDetails l : ld )
        	{
        	LicensePanel license = new LicensePanel() ;
        	license.setMacId(l.getMacId()) ;
        	license.setCustomer(customerManager.loadCustomerById(l.getCustomerId()).getName());
        	license.setStartDate(l.getStartDate());
        	license.setEndDate(l.getEndDate());
        	license.setDownloadRestPath("customer/license/file/" + l.getMacId() ) ;
        	lp.add(license);
        	}
        	return lp ;
        }
        return null ;
    }

    /**
     * Return License file
     * 
     * @param Mac ID
     * @return License file
     */
    @Path("customer/license/file/{mac}")
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM })
    public Response licenseFile(@PathParam("mac") String mac) {
    	
    	final byte[]  key = licenseDetailManager.loadApiKeyWRTMac(mac) ;
    	
    	return Response.ok(key).header("content-disposition","attachment; filename = restApi.enlighted").build(); 
    }


   
}
