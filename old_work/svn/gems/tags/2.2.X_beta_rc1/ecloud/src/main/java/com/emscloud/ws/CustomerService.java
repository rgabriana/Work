package com.emscloud.ws ;


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
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.emscloud.model.Customer;
import com.emscloud.service.CustomerManager;


@Controller
@Path("/org/customer")
public class CustomerService {
	
	@Resource
	CustomerManager customerManager;
	
	
    
    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public CustomerService() {
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
    @Path("list")
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
    @Path("list/{name}")
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
    @Path("save")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void saveCustomer(Customer customer) {
        customerManager.saveOrUpdate(customer);
    }
    
  


   
}
