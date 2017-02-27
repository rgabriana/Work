package com.emscloud.mvc.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.Customer;
import com.emscloud.model.UserCustomers;
import com.emscloud.model.Users;
import com.emscloud.mvc.controller.validator.ENLValidator;
import com.emscloud.security.exception.EcloudValidationException;
import com.emscloud.service.CustomerManager;
import com.emscloud.service.SystemConfigurationManager;
import com.emscloud.service.UserCustomersManager;
import com.emscloud.service.UserManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.types.RoleType;
import com.emscloud.types.Status;
import com.emscloud.util.CloudAuditLoggerUtil;
import com.emscloud.util.CommonUtils;
import com.emscloud.vo.UserCustomersVO;

@Controller
@RequestMapping("/users")
public class UsersController {

	@Resource
	UserManager userManager;
	@Resource
	CustomerManager customerManager;
	@Resource
	UserCustomersManager userCustomersManager;
	@Resource
	PasswordEncoder shaPasswordEncoder;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
	@Resource
	SystemConfigurationManager systemConfigurationManager;
	
	@Autowired
	private MessageSource messageSource;

	@Resource (name="passwordValidator")
	private ENLValidator passwordValidator;
	   
	@RequestMapping(value = "/list.ems")
	public String customerList(Model model) {
		return "users/list";
	}
	
	@RequestMapping("/changepassworddialog.ems")
    public String changePasswordDialog(Model model) {
        return "users/changepassword/dialog";
    }

	@RequestMapping("/save.ems")
	String saveUser(Users user, @RequestParam("password") String password,HttpServletRequest request) throws EcloudValidationException {
		
		Map<String,Object> nameValMap = new HashMap<String, Object>();
        nameValMap.put("userFirstName", user.getFirstName());
        nameValMap.put("userLastName", user.getLastName());
        nameValMap.put("userEmail", user.getEmail());
        nameValMap.put("userRoleType", user.getRoleType());
        nameValMap.put("userStatus", user.getStatus().getName());
		CommonUtils.isParamValueAllowedAndThrowException(messageSource, systemConfigurationManager, nameValMap);
		
		String type = null;
		if (user.getId() == null || user.getId()==0)
			type = "new";
		else
			type = "edit";
		if (type.equalsIgnoreCase("new")) {
			 if(!passwordValidator.validate(password)){
	    			final String err = "Password "+password+" does not meet the policy criteria";
	    			cloudAuditLoggerUtil.log(err,CloudAuditActionType.User_Create.getName());
	    			throw new IllegalArgumentException(err);
	    		}

			// For new user
			user.setSalt(user.getEmail());
			user.setCreatedOn(new Date());
			user.setPassword(shaPasswordEncoder.encodePassword(password, user
					.getSalt()));
			Users savedUser = userManager.save(user);
			savedUser.setSalt(savedUser.getId().toString());
			savedUser.setPassword(shaPasswordEncoder.encodePassword(password, user
					.getSalt()));
			savedUser = userManager.save(savedUser);
			cloudAuditLoggerUtil.log("Created user: " + savedUser.getEmail() + " (Role - " + savedUser.getRoleType() + ", id - "
                    + savedUser.getId() + ")", CloudAuditActionType.User_Create.getName());
		} else {
			// For existing user
			Users userToSave = userManager.loadUserByUserId(user.getId());			
			userToSave.setFirstName(user.getFirstName());
			userToSave.setLastName(user.getLastName());
			userToSave.setEmail(user.getEmail());
			//userToSave.setSalt(user.getEmail());			
			userToSave.setStatus(user.getStatus());
			if(user.getCreatedOn() != null)
			userToSave.setCreatedOn(user.getCreatedOn());
			userToSave.setRoleType(user.getRoleType());
			// See if passwords are changed
			if (password != null && password.length() > 0) {
				if(!passwordValidator.validate(password)){
        			final String err = "Password "+password+" does not meet the policy criteria";
        			cloudAuditLoggerUtil.log(err,CloudAuditActionType.User_Update.getName());
        			throw new IllegalArgumentException(err);
        		}
				userToSave.setPassword(shaPasswordEncoder.encodePassword(
						password, userToSave.getSalt()));
			}
			Users savedUser = userManager.update(userToSave);
			cloudAuditLoggerUtil.log("Updated user: " + savedUser.getEmail() + " (Role - " + savedUser.getRoleType() + ", id - "
                    + savedUser.getId() + ")", CloudAuditActionType.User_Update.getName());
		}
		return "redirect:/users/list.ems?" + "status=" + type;
	}

	@RequestMapping("/create.ems")
	String createUser(Model model) {
		Users user = new Users();
		model.addAttribute("user", user);

		// Don't add the Admin role -- Kepp in sequence for further modification		
		RoleType[] finalRoleArr = new RoleType[4];
		finalRoleArr[0] = RoleType.SystemAdmin;
		finalRoleArr[1] = RoleType.SupportAdmin;
		finalRoleArr[2] = RoleType.ThirdPartySupportAdmin;
		finalRoleArr[3] = RoleType.SPPA;
		
		model.addAttribute("roles", finalRoleArr);
		model.addAttribute("statuslist", Status.values());

		return "users/details";
	}
	
	@RequestMapping("/edit.ems")
	String editUser(Model model, @RequestParam("userId") Long userId) {
		Users user = userManager.loadUserByUserId(userId);
		model.addAttribute("user", user);

		// mRoles.add(RoleType.Admin.toString()); // Don't add the Admin role
		RoleType[] finalRoleArr = new RoleType[4];
		finalRoleArr[0] = RoleType.SystemAdmin;
		finalRoleArr[1] = RoleType.SupportAdmin;
		finalRoleArr[2] = RoleType.ThirdPartySupportAdmin;
		finalRoleArr[3] = RoleType.SPPA;
		model.addAttribute("roles", finalRoleArr);
		model.addAttribute("statuslist", Status.values());

		return "users/details";
	}
	
	

	@RequestMapping("/assigncustomers.ems")
	String assignCustomers(Model model, @RequestParam("userId") Long userId) {
		
		Users user = userManager.loadUserByUserId(userId);
		List<UserCustomersVO> mList = new ArrayList<UserCustomersVO>();
		List<UserCustomers> userCustomers = userCustomersManager
				.loadUserCustomersByUserId(userId);

		UserCustomersVO uCustomerVo = null;
		Customer customer = null;

		// Add the assigned customers
		List<Long> customerIds = new ArrayList<Long>();
		for (Iterator<UserCustomers> iterator = userCustomers.iterator(); iterator
				.hasNext();) {
			customer = new Customer();
			UserCustomers userCustomers2 = (UserCustomers) iterator.next();
			uCustomerVo = new UserCustomersVO();
			uCustomerVo.setSelected(true);
			customer = customerManager.loadCustomerById(userCustomers2
					.getCustomer().getId());
			uCustomerVo.setCustomerId(customer.getId());
			uCustomerVo.setCustomerName(customer.getName());
			customerIds.add(customer.getId());
			mList.add(uCustomerVo);
		}
		
		// Add not assigned customers and set the flag to false
		List<Customer> customerList = new ArrayList<Customer>();
		customerList = customerManager.loadUnMappedCustomers(customerIds);
		for (Iterator<Customer> iterator = customerList.iterator(); iterator
				.hasNext();) {
			customer = (Customer) iterator.next();
			uCustomerVo = new UserCustomersVO();
			uCustomerVo.setSelected(false);
			uCustomerVo.setCustomerId(customer.getId());
			uCustomerVo.setCustomerName(customer.getName());
			mList.add(uCustomerVo);
		}
		
		String userName = user.getEmail();
		model.addAttribute("customerlist", mList);
		model.addAttribute("userid", userId);
		model.addAttribute("userName", userName);
		return "users/customerlistforuser";
	}

}
