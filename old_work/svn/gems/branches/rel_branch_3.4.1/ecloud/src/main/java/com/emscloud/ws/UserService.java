package com.emscloud.ws;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.context.MessageSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;

import com.emscloud.model.Customer;
import com.emscloud.model.UserCustomers;
import com.emscloud.model.UserPassword;
import com.emscloud.model.Users;
import com.emscloud.mvc.controller.validator.ENLValidator;
import com.emscloud.security.EmsAuthenticationContext;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.UserCustomersManager;
import com.emscloud.service.UserManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.util.CloudAuditLoggerUtil;
import com.emscloud.vo.UsersList;

@Controller
@Path("/org/users")
public class UserService {

	@Resource
	UserManager userManager;
	@Resource
	CustomerManager customerManager;
	@Resource
	UserCustomersManager userCustomersManager;
	@Resource
	PasswordEncoder shaPasswordEncoder;
	@Resource(name = "emsAuthContext")
	private EmsAuthenticationContext emsAuthContext;
	@Resource(name = "messageSource")
    private MessageSource messageSource;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	@Resource (name="passwordValidator")
    private ENLValidator passwordValidator;
	@Path("list/users")
	@POST
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public UsersList loadUsersList(@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
		UsersList mUsersList = userManager.loadallUsers(orderway, orderby,(page - 1) * UsersList.DEFAULT_ROWS, UsersList.DEFAULT_ROWS);
		mUsersList.setPage(page);
		return mUsersList;
	}
	
	@Path("delete/{id}")
    @POST
    public Response deleteUser(@PathParam("id") Long id) {
		//delete the customer assignments for this user
		Response response = new Response();
		userCustomersManager.deleteOldAssignments(id);
		//now delete the user.
		Users user = userManager.loadUserByUserId(id);
		int status = userManager.delete(id);
		cloudAuditLoggerUtil.log("Deleted user: " + user.getEmail() + "(Role - " + user.getRoleType() + ", id - "
                + user.getId() + ")", CloudAuditActionType.User_Delete.getName());
		response.setStatus(status);		
		return response;		
	}
	
	@Path("list/{username}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Users getUserList(@PathParam("username") String username) {
		return userManager.loadUserByUserName(username);
	}

	@Path("assigncustomers/{userid}/{customerlistcsv}")
	@POST
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response assignCustomersToUser(@PathParam("userid") Long userid,
			@PathParam("customerlistcsv") String customerlistcsv) {
		Response oStatus = new Response();

		StringTokenizer st = new StringTokenizer(customerlistcsv, ",");
		UserCustomers uCustomer = null;
		Users user = null;
		Customer customer = null;
		HashMap<Long, UserCustomers> mUserMap = new HashMap<Long, UserCustomers>();
		while (st.hasMoreElements()) {
			String customerS = (String) st.nextElement();
			Long customerKey = Long.valueOf(customerS);
			uCustomer = new UserCustomers();
			user = new Users();
			customer = new Customer();
			user.setId(userid);
			customer.setId(customerKey);
			uCustomer.setUser(user);
			uCustomer.setCustomer(customer);
			if (customerKey != -1) {
				mUserMap.put(customerKey, uCustomer);
			} else { // Remove all entries from database
				userCustomersManager.deleteOldAssignments(userid);   //delete as customer has selected none on UI
			}
		}

		HashMap<Long, UserCustomers> mAllMap = (HashMap<Long, UserCustomers>) userCustomersManager
				.loadMapForUser(userid);

		// Loop over all entries present in the map
		if (mAllMap != null && mAllMap.size() > 0 && mUserMap != null
				&& mUserMap.size() > 0)
			for (Map.Entry<Long, UserCustomers> entry : mAllMap.entrySet()) {
				Long customerKey = entry.getKey();
				if (mAllMap.containsKey(customerKey)
						&& !mUserMap.containsKey(customerKey)) {
					// delete the record
					userCustomersManager.removeByUserAndCustomerId(userid,
							customerKey);
				}
			}
		
		if (mUserMap != null && mUserMap.size() > 0 && mAllMap != null
				&& mAllMap.size() > 0)
			for (Map.Entry<Long, UserCustomers> entry : mUserMap.entrySet()) {
				Long customerKey = entry.getKey();
				if (!mAllMap.containsKey(customerKey)
						&& mUserMap.containsKey(customerKey)) {
					// Insert the record
					uCustomer = new UserCustomers();
					user = new Users();
					customer = new Customer();
					user.setId(userid);
					customer.setId(customerKey);
					uCustomer.setUser(user);
					uCustomer.setCustomer(customer);
					if (customerKey != -1)
						userCustomersManager.save(uCustomer);
						cloudAuditLoggerUtil.log("Customers updated for User: " + userManager.loadUserByUserId(userid).getEmail() + " (id - "
			                + userid + ")", CloudAuditActionType.User_Customers_Update.getName());
				}
			}

		if ((mUserMap.size() > 0 && mUserMap != null && mAllMap.size() == 0)) {
			// Insert these entries as new records of assignment and no previous
			// assignment were present
			for (Map.Entry<Long, UserCustomers> entry : mUserMap.entrySet()) {
				Long customerKey = entry.getKey();
				uCustomer = new UserCustomers();
				user = new Users();
				customer = new Customer();
				user.setId(userid);
				customer.setId(customerKey);
				uCustomer.setUser(user);
				uCustomer.setCustomer(customer);
				if (customerKey != -1)
					userCustomersManager.save(uCustomer);
					cloudAuditLoggerUtil.log("Customers updated for User: " + userManager.loadUserByUserId(userid).getEmail() + " (id - "
		                + userid + ")", CloudAuditActionType.User_Customers_Update.getName());
			}
		}
		return oStatus;
	}
	
	@Path("changepassword")
	@POST
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response changePassword(UserPassword userPassword) {

		Response oResponse = new Response();
		Long currentUserId = emsAuthContext.getUserId();
		Users user = userManager.loadUserByUserId(currentUserId);

		String newEncodedPassword = "";
		String oldEncodedPassword = "";

		String oldPassword = "";
		String newPassword = "";

		try {
			newPassword = URLDecoder.decode(userPassword.getNewPassword(),
					"UTF-8");
			oldPassword = URLDecoder.decode(userPassword.getOldPassword(),
					"UTF-8");

			newEncodedPassword = shaPasswordEncoder.encodePassword(newPassword,
					user.getSalt());
			oldEncodedPassword = shaPasswordEncoder.encodePassword(oldPassword,
					user.getSalt());
			
			if(!passwordValidator.validate(newPassword)){
    			final String err = "Password "+String.valueOf(userPassword)+" does not meet the policycriteria";
    			cloudAuditLoggerUtil.log(err,CloudAuditActionType.Change_Password.getName());
    			oResponse.setStatus(1);
	            oResponse.setMsg(messageSource.getMessage("error.passwords.policy", null, null));
	            return oResponse;
    		}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String oldDbEncodedPassword = user.getPassword();
		if (!oldEncodedPassword.equalsIgnoreCase(oldDbEncodedPassword)) {
			// User entered old password does not match
			oResponse.setStatus(1);
			oResponse.setMsg(messageSource.getMessage("password.old.mismatch",
					null, null));
		} else {
			// Change the password and return the Success status
			user.setPassword(newEncodedPassword);
			Users savedUser = userManager.save(user);
			cloudAuditLoggerUtil.log("Password changed for User: " + savedUser.getEmail() + "(Role - " + savedUser.getRoleType() + ", id - "
	                + savedUser.getId() + ")", CloudAuditActionType.Change_Password.getName());
			oResponse.setStatus(0);
			oResponse.setMsg(messageSource.getMessage(
					"password.change.success", null, null));
		}
		return oResponse;
	}
	
}
