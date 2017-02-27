package com.emscloud.ws ;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
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
import com.emscloud.model.CustomerList;
import com.emscloud.model.UserCustomers;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.UserCustomersManager;
import com.emscloud.service.UserManager;
import com.emscloud.types.RoleType;


@Controller
@Path("/org/customer")
public class CustomerService {
	
	@Resource
	CustomerManager customerManager;
	
	@Resource
	UserCustomersManager userCustomersManager;
	
	@Resource
	UserManager userManager;
	
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	
	
    
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
    
    @Path("listAllCustomers")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CustomerList listAllCustomers(@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
    	
		List<Long> cList = new ArrayList<Long>();
		CustomerList oCustomerList;
		if (emsAuthContext.getCurrentUserRoleType().getName().equals(
				RoleType.Admin)) {
			oCustomerList = customerManager.loadCustomerList(orderway,
					(page - 1) * CustomerList.DEFAULT_ROWS,
					CustomerList.DEFAULT_ROWS, cList);
		} else {
			// cList = null;
			List<UserCustomers> uCustomers = userCustomersManager
					.loadUserCustomersByUserId(emsAuthContext.getUserId());
			for (Iterator<UserCustomers> iterator = uCustomers.iterator(); iterator
					.hasNext();) {
				UserCustomers userCustomers = (UserCustomers) iterator.next();
				cList.add(userCustomers.getCustomer().getId());
			}
			oCustomerList = customerManager.loadCustomerList(orderway,
					(page - 1) * CustomerList.DEFAULT_ROWS,
					CustomerList.DEFAULT_ROWS, cList);
		}

		oCustomerList.setPage(page);
		return oCustomerList;
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
