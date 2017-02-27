package com.emscloud.ws;

import java.text.ParseException;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Controller;

import com.emscloud.model.CustomerBillPayment;
import com.emscloud.service.BillPaymentManager;

@Controller
@Path("/org/bills")
public class BillPaymentService {
	@Resource
	BillPaymentManager		billPaymentManager;
	
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    BillPaymentService()
	{
		
	}

	@Path("paymentlistbycutomerid/{id}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public CustomerBillPayment getBillPaymentListByCustomerId(@PathParam("id") Long id,
    		@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
	
		CustomerBillPayment customerBillPayment = 	billPaymentManager.getAllBillsPerCustomer(id,orderway, (page - 1) * CustomerBillPayment.DEFAULT_ROWS,CustomerBillPayment.DEFAULT_ROWS);
		customerBillPayment.setPage(page);
    	if(customerBillPayment.getBillPayments() != null && !customerBillPayment.getBillPayments().isEmpty())
    	{
    		return customerBillPayment;
    	}
    	return null ;
    }
}
