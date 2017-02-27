package com.ems.mvc.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.model.Role;
import com.ems.model.Tenant;
import com.ems.model.User;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.mvc.validator.ENLValidator;
import com.ems.security.EmsAuthenticationContext;
import com.ems.security.util.PasswordUtils;
import com.ems.service.TenantManager;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.types.UserStatus;
import com.ems.util.Constants;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasAnyRole('Admin','TenantAdmin','FacilitiesAdmin')")
public class UsersController {

    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource
    UserManager userManager;

    @Resource
    TenantManager tenantManger;

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource(name = "emsAuthContext")
    private EmsAuthenticationContext emsAuthContext;
    @Autowired
	private HttpServletRequest request;
    
    @Resource (name="passwordValidator")
    private ENLValidator passwordValidator;

    @RequestMapping("/list.ems")
    String getListOfUsers(Model model, @RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(value = "facilityAssignmentStatus", required = false) String facilityAssignmentStatus,
    		@RequestParam(value = "newUserId", required = false) Long newUserId) {

        if (!checkOperationAllowed(tenantId)) {
            userAuditLoggerUtil.log("Listing of User not permitted for Tenant id: " + tenantId,
                    UserAuditActionType.Unauthorised_Access.getName());
            throw new IllegalArgumentException("Listing User for Tenant id: " + tenantId + " not permitted for "
                    + emsAuthContext.getUserName() + "-" + emsAuthContext.getUserId());
        }

        List<User> usersList = null;
        if (tenantId == null || tenantId == 0) {
            usersList = userManager.loadCompanyUsers();
        } else {
            usersList = userManager.loadTenantUsers(tenantId);
        }
        
        if(emsAuthContext.getCurrentUserRoleType() != RoleType.Admin){
        	List<User> adminUsersList = new ArrayList<User>(usersList);
        	for (User user : usersList){
        		if(user.getRole().getRoleType() == RoleType.Admin){
        			adminUsersList.remove(user);
        		}
        	}
        	model.addAttribute("usersList", adminUsersList);
        }else{
        	model.addAttribute("usersList", usersList);
        }
        
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("tenant", (tenantId != null && tenantId != 0L ? tenantManger.get(tenantId) : null));
        if(newUserId != null){
        	model.addAttribute("newUser", userManager.loadUserById(newUserId));
        }
        if(facilityAssignmentStatus != null)
        {
        	model.addAttribute("facilityAssignmentStatus", facilityAssignmentStatus);
        }
        return "users/list";
    }

    @RequestMapping("/create.ems")
    String createUser(Model model, @RequestParam("tenantId") Long tenantId) {

        if (!checkOperationAllowed(tenantId)) {
            userAuditLoggerUtil.log("Creation of User not permitted for Tenant id: " + tenantId,
                    UserAuditActionType.Unauthorised_Access.getName());
            throw new IllegalArgumentException("Creating User for Tenant id: " + tenantId + " not permitted for "
                    + emsAuthContext.getUserName() + "-" + emsAuthContext.getUserId());
        }

        User user = new User();
        model.addAttribute("user", user);
        model.addAttribute("statusList", UserStatus.values());
        model.addAttribute("mode", "create");

        if (tenantId == null || tenantId == 0) {
        	List<Role> roles = userManager.loadRolesForCompany();
        	if(emsAuthContext.getCurrentUserRoleType() == RoleType.Admin){
        		List<Role> adminRoles = new ArrayList<Role>(roles);
        		adminRoles.add(userManager.getRoleByRoleType(RoleType.Admin));
        		model.addAttribute("roles", adminRoles);
        	}else{
        		model.addAttribute("roles", roles);
        	}
        	model.addAttribute("tenantId", 0);
            model.addAttribute("validDomain", "");
        } else {
            Tenant tenant = tenantManger.get(tenantId);
            user.setTenant(tenant);
            model.addAttribute("roles", userManager.loadRolesForTenant());
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("validDomain", tenant.getValidDomain());
        }

        return "users/details";
    }

    @RequestMapping(value = "/{userId}/edit.ems")
    public String editUser(Model model, @PathVariable("userId") Long userId) {

        User user = userManager.loadUserById(userId);

        if (user.getTenant() != null) {
            if (!checkOperationAllowed(user.getTenant().getId())) {
                userAuditLoggerUtil.log("Listing of User not permitted for Tenant id: " + user.getTenant().getId(),
                        UserAuditActionType.Unauthorised_Access.getName());
                throw new IllegalArgumentException("Listing User for Tenant id: " + user.getTenant().getId()
                        + " not permitted for " + emsAuthContext.getUserName() + "-" + emsAuthContext.getUserId());
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("statusList", UserStatus.values());
        model.addAttribute("mode", "edit");

        if (user.getTenant() != null) {
            model.addAttribute("roles", userManager.loadRolesForTenant());
            model.addAttribute("tenantId", user.getTenant().getId());
            model.addAttribute("validDomain", user.getTenant().getValidDomain());
        } else {
        	List<Role> roles = userManager.loadRolesForCompany();
        	if(emsAuthContext.getCurrentUserRoleType() == RoleType.Admin){
        		List<Role> adminRoles = new ArrayList<Role>(roles);
        		adminRoles.add(userManager.getRoleByRoleType(RoleType.Admin));
        		model.addAttribute("roles", adminRoles);
        	}else{
        		model.addAttribute("roles", roles);
        	}
            model.addAttribute("tenantId", 0);
            model.addAttribute("validDomain", "");
        }
        return "users/details";
    }
    
    @RequestMapping(value = "/{userId}/unlockUser.ems")
    public String unlockUserDialog(Model model, @PathVariable("userId") Long userId) {

        User user = userManager.loadUserById(userId);

        model.addAttribute("user", user);
        model.addAttribute("statusList", UserStatus.values());

        return "users/unlockUser";
        //return "redirect:/users/list.ems?tenantId=" + tenantId + "&status="+type;
    }
    
    @RequestMapping(value = "/{userId}/forgotPassword.ems")
    @PreAuthorize("permitAll()")
    public String resetForgotPassword(Model model, @PathVariable("userId") Long userId) {
    	
        User user = userManager.loadUserById(userId);

        model.addAttribute("user", user);
        model.addAttribute("statusList", UserStatus.values());
        return "forgot/unlockUser";
        //return "redirect:/users/list.ems?tenantId=" + tenantId + "&status="+type;
    }
    
    
    @RequestMapping(value = "/unlockUser.ems")
    public String unlockUser(User user,  @RequestParam("password") String password) {

    	resetPassword(user, password, true);
        return "redirect:/users/list.ems?status=unlocked";
    }
    
    @RequestMapping(value = "/resetUserPassword.ems")
    @PreAuthorize("permitAll()")
    public String resetUserPassword(User user,  @RequestParam("password") String password) {

    	//request.getSession().invalidate();
    	
    	if(resetPassword(user, password, false)){
    		
    		//return "redirect:/pages/users/reset_password.jsp?resetSuccess=true";
    		return "redirect:/users/"+user.getId()+"/true/loginPage.ems";
    	}else{
    		//return "redirect:/pages/users/reset_password.jsp?resetFail=true";
    		return "redirect:/users/"+user.getId()+"/false/loginPage.ems";
    	}
        
    }
    
    @RequestMapping(value = "{userid}/{result}/loginPage.ems")
    @PreAuthorize("permitAll()")
    public String toLoginPage(Model model,@PathVariable("userid") Long userid, @PathVariable("result") String result) {
    	final boolean bresult = result != null && "TRUE".equalsIgnoreCase(result);
    	final String modelResult = bresult ? "true" : "false";
    	model.addAttribute("resultSuccess",modelResult);
    	User user = userManager.loadUserById(userid);
        model.addAttribute("user", user);
        model.addAttribute("statusList", UserStatus.values());
        SecurityContextHolder.getContext().setAuthentication(null);
    	return "forgot/unlockUser";
    }

	private boolean resetPassword(User user, String password, boolean isUnlock) {
		boolean bReturn = true;
		//DO NOTHING AS OF NOW...VALIDATE FIRST THE PASSWORD AND THEN CHANGE IT AND RESET THE LOGIN ATTEMPTS TO 0
    	if(!passwordValidator.validate(password)){
			final String err = "Password "+password+" does not meet the policy criteria";
			userAuditLoggerUtil.log(err,UserAuditActionType.Change_Password.getName());
			throw new IllegalArgumentException(err);
		}
    	final User dbUser = userManager.loadUserById(user.getId());
    	 user.setPassword(passwordEncoder.encodePassword(password, null));
    	 if("admin".equalsIgnoreCase(dbUser.getEmail())){
    		 try {
          		if (password != null && password.length() > 0) {
          				PasswordUtils.updatePassword(password);
          		}
          	}catch(Exception e){
          		bReturn = false;
          		e.printStackTrace();
          	}
    	 }else if(dbUser.getRole().getRoleType() == RoleType.Admin){
         	try {
         		if (password != null && password.length() > 0) {
         				PasswordUtils.updateAdminTypeUserPassword(dbUser.getEmail(), password);
         		}
         	}catch(Exception e){
         		bReturn = false;
         		e.printStackTrace();
         	}
    	 }
         //User savedUser = userManager.save(user);
//    	 userManager.updateUserDetails(Constants.PASSWORD_COLUMN, user.getPassword(), String.valueOf(user.getId()));
//    	 userManager.updateUserDetails(Constants.NO_LOGIN_ATTEMPT_COLUMN, String.valueOf(0), String.valueOf(user.getId()));
//    	 userManager.updateUserDetails(Constants.IDENTIFIER_FORGOT_PASS, null, String.valueOf(user.getId()));
    	 dbUser.setPassword(user.getPassword());
    	 dbUser.setNoOfLoginAttempts(0l);
    	 dbUser.setUnlockTime(null);
    	 if(isUnlock){
    		 dbUser.setForgotPasswordIdentifier(Constants.PASSWORD_CHANGED_IDENTIFIER);
    	 }else{
    		 dbUser.setForgotPasswordIdentifier(null);
    	 }
    	 User savedUser = userManager.save(dbUser);
         userAuditLoggerUtil.log("Admin "+emsAuthContext.getUserName()+" has successfully unlocked the account for user:"+ user.getId(),UserAuditActionType.Change_Password.getName());
         return bReturn;
	}
    

    @RequestMapping("/save.ems")
    String saveUser(User user, @RequestParam("tenantId") Long tenantId, @RequestParam("password") String password) {

    	 String type=null;
    	 Long newUserId = null;
    	 if(user.getId()!=null)
         	type = "edit";
         else
         	type = "new";
    	 
        if (!checkOperationAllowed(tenantId)) {
            userAuditLoggerUtil.log("Listing of User not permitted for Tenant id: " + tenantId,
                    UserAuditActionType.Unauthorised_Access.getName());
            throw new IllegalArgumentException("Listing User for Tenant id: " + tenantId + " not permitted for "
                    + emsAuthContext.getUserName() + "-" + emsAuthContext.getUserId());
        }

        if (user.getId() == null || user.getId() == 0) {
            if (tenantId == null || tenantId == 0) {
                user.setTenant(null);
            } else {
                Tenant tenant = tenantManger.get(tenantId);
                user.setTenant(tenant);
            }
            if(!passwordValidator.validate(password)){
    			final String err = "Password "+password+" does not meet the policy criteria";
    			userAuditLoggerUtil.log(err,UserAuditActionType.User_Update.getName());
    			throw new IllegalArgumentException(err);
    		}
            user.setPassword(passwordEncoder.encodePassword(password, null));
            User savedUser = userManager.save(user);
            newUserId = savedUser.getId();
            Role role = userManager.loadRoleByUserId(savedUser.getId());
            userAuditLoggerUtil.log("Create user: " + user.getEmail() + "(Role - " + role.getRoleType() + ", id - "
                    + user.getId() + ")", UserAuditActionType.User_Create.getName());
            
            if(role.getRoleType() == RoleType.Admin){
            	try {
					PasswordUtils.addAdminTypeUser(user.getEmail(), password);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
        } else {
            User userToSave = userManager.loadUserById(user.getId());
            userToSave.setFirstName(user.getFirstName());
            userToSave.setLastName(user.getLastName());
            userToSave.setEmail(user.getEmail());
            userToSave.setContact(user.getContact());
            userToSave.setRole(user.getRole());
            userToSave.setStatus(user.getStatus());

            // See if passwords are changed
            if (password != null && password.length() > 0) {
            	if(!passwordValidator.validate(password)){
        			final String err = "Password "+password+" does not meet the policy criteria";
        			userAuditLoggerUtil.log(err,UserAuditActionType.User_Update.getName());
        			throw new IllegalArgumentException(err);
        		}
                userToSave.setPassword(passwordEncoder.encodePassword(password, null));
            }
            User savedUser = userManager.update(userToSave);
            Role role = userManager.loadRoleByUserId(savedUser.getId());
            
            if(role.getRoleType() == RoleType.Admin){
            	try {
            		if (password != null && password.length() > 0) {
            			PasswordUtils.updateAdminTypeUserPassword(user.getEmail(), password);
            		}
            	} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
            userAuditLoggerUtil.log("Update user: " + user.getEmail() + "(Role - " + role.getRoleType() + ", id - "
                    + user.getId() + ")", UserAuditActionType.User_Update.getName());
            
            
        }
        
        if("new".equalsIgnoreCase(type)){
        	return "redirect:/users/list.ems?tenantId=" + tenantId + "&status="+type + "&newUserId="+newUserId;
        }else{
        	return "redirect:/users/list.ems?tenantId=" + tenantId + "&status="+type;
        }
   }

    @RequestMapping("/assignuserstoswitches.ems")
    public String assignUsersToSwitches(Model model, @RequestParam("switchId") Long switchId) {
        List<User> users = userManager.loadCompanyUsersExceptAuditor();

        // If there are no company user than make an empty list of users
        // so that we can hold tenant users
        if (users == null) {
            users = new ArrayList<User>();
        }

        List<Tenant> tenants = tenantManger.getAllTenants();
        for (Tenant oTenant : tenants) {
            users.addAll(userManager.loadTenantUsers(oTenant.getId()));
        }
        
        Set<Long> assignedUsers = userManager.loadUserSwitchesBySwitchId(switchId);
        
        for(User u: users) {
        	if(assignedUsers.contains(u.getId())){
        		u.setSelected(true);
        	}
        	else {
        		u.setSelected(false);
        	}
        }

        model.addAttribute("users", users);
        return "switches/assignusers";
    }

    @PreAuthorize("permitAll()")
    @RequestMapping("/changepassworddialog.ems")
    public String changePasswordDialog(Model model) {
        return "users/changepassword/dialog";
    }

    /* Added by Nitin to delete the user from DB */

    @RequestMapping(value = "/delete.ems", method = RequestMethod.POST)
    public String deleteUser(@RequestParam("userId") Long userId,
            @RequestParam(value = "tenantId", required = false) Long tenantId) {

        if (!checkOperationAllowed(tenantId)) {
            userAuditLoggerUtil.log("Listing of User not permitted for Tenant id: " + tenantId,
                    UserAuditActionType.Unauthorised_Access.getName());
            throw new IllegalArgumentException("Listing User for Tenant id: " + tenantId + " not permitted for "
                    + emsAuthContext.getUserName() + "-" + emsAuthContext.getUserId());
        }

        // Let's fetch the userName before we delete
        User user = userManager.loadUserById(userId);
        String userName = "";
        if (user != null) {
            userName = user.getEmail();
        }

        userManager.deleteUser(userId);
        
        if(user.getRole().getRoleType() == RoleType.Admin){
        	try {
				PasswordUtils.deleteAdminTypeUser(user.getEmail());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        userAuditLoggerUtil.log("Delete user: " + userName + "( id - " + user.getId() + ")",
                UserAuditActionType.User_Delete.getName());
        if (tenantId == null)
            return "redirect:list.ems?status=delete";
        else
            return "redirect:list.ems?tenantId=" + tenantId+"&status=delete";
    }

    private boolean checkOperationAllowed(Long tenantId) {
        boolean allowed = true;
        if (tenantId != null) {
            if (emsAuthContext.getCurrentUserRoleType() == RoleType.TenantAdmin) {
                if (emsAuthContext.getCurrentTenant().getId().longValue() != tenantId) {
                    return false;
                }
            }
        }

        return allowed;
    }

}
