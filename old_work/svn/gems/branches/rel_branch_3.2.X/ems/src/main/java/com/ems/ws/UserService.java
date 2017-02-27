/**
 * 
 */
package com.ems.ws;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Set;

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

import org.springframework.context.MessageSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;

import com.ems.model.Scene;
import com.ems.model.Tenant;
import com.ems.model.User;
import com.ems.model.UserPassword;
import com.ems.model.UserSwitches;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.mvc.validator.ENLValidator;
import com.ems.security.EmsAuthenticationContext;
import com.ems.security.util.PasswordUtils;
import com.ems.service.SwitchManager;
import com.ems.service.TenantManager;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/user")
public class UserService {
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "userManager")
	private UserManager userManager;
	@Resource(name = "switchManager")
	private SwitchManager switchManager;
    @Resource(name = "tenantManager")
	private TenantManager tenantManager;
    
    @Resource(name = "passwordEncoder")
    private PasswordEncoder passwordEncoder;
    
    @Resource(name = "shaPasswordEncoder")
    private PasswordEncoder shaPasswordEncoder;
    
    @Resource(name = "emsAuthContext")
    private EmsAuthenticationContext emsAuthContext;
    
    @Resource(name = "messageSource")
    private MessageSource messageSource;
    
    @Resource (name="passwordValidator")
    private ENLValidator passwordValidator;
    
    
	public UserService() {

	}

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	/**
	 * Returns user details
	 * 
	 * @param username
	 * @return user details
	 */
	@Path("list/{username}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public User getUserList(@PathParam("username") String username) {
		return userManager.loadUserByUserName(username);
	}

	/**
	 * returns list of all facility users and tenant users 
	 * @return List of User
	 */
	@Path("userandtenantlist")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<User> getAllUserAndTenantList() {
	    List<User> users = userManager.loadCompanyUsers();
       
	    List<Tenant> tenants = tenantManager.getAllTenants();
	    for(Tenant oTenant : tenants){
	        users.addAll(userManager.loadTenantUsers(oTenant.getId()));
	    }
	    
       return users;
    }
	   
	/**
	 * Returns switch associated with user FIXME: Need to tweak this based on
	 * the usecase.
	 * 
	 * @param userid
	 * @return UserSwitch list for the userid
	 */
	@Path("switch/list/{userid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<UserSwitches> getUserSwitchList(@PathParam("userid") Long userid) {
		return userManager.loadSwitchListbyUserId(userid);
	}

	/**
	 * save UserSwitch mapping
	 * 
	 * @param userSwitchesList
	 *             <userSwitchess><userSwitches><id>1</id><switchid>108</switchid><userid>1</userid></userSwitches></userSwitchess>
	 * @return
	 */
	@Path("switch/save")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response saveUserSwitchList(List<UserSwitches> userSwitchesList) {
		Long switchId = userSwitchesList.get(0).getSwitchId();
		Set<Long> currentAssignedUsers = userManager.loadUserSwitchesBySwitchId(switchId);
	    for(UserSwitches userswitch : userSwitchesList){
	    	currentAssignedUsers.remove(userswitch.getUserId());
	        List<UserSwitches> list = userManager.loadUserSwitchesByUserIdSwitchId(userswitch.getUserId(), userswitch.getSwitchId());
	        if(list == null){
	            userManager.saveSwitchToUser(userswitch);
	        }
	    }
	    for(Long userId: currentAssignedUsers) {
	    	userManager.removeSwitchFromUser(userId,switchId);
	    }
        return new Response();
    }
	
	/**
	 * Returns scene list
	 * 
	 * @param switchid
	 * @return Scene list for the selected switchid
	 */
	@Path("switchscene/list/{switchid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Scene> getUserSwitchSceneList(
			@PathParam("switchid") Long switchid) {
		return switchManager.loadSceneBySwitchId(switchid);
	}
	
	/**
	 * Change password of currently logged in User
	 * 
	 * @param userPassword
	 *        <userPassword><oldPassword>oldPassword</oldPassword><newPassword>newPassword</newPassword></userPassword>
	 * @return
	 */
	
	@Path("changepassword")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response changePassword(UserPassword userPassword) {
		
	    Response oResponse = new Response();
        Long currentUserId = emsAuthContext.getUserId();
        User user = userManager.loadUserById(currentUserId);
        
        String newPassword ="";
        String oldPassword = "";
        
        try {
            newPassword = URLDecoder.decode(userPassword.getNewPassword(),"UTF-8");
            oldPassword = URLDecoder.decode(userPassword.getOldPassword(),"UTF-8");
            if(!passwordValidator.validate(newPassword)){
    			final String err = "Password "+String.valueOf(userPassword)+" does not meet the policycriteria";
    			userAuditLoggerUtil.log(err,UserAuditActionType.Change_Password.getName());
    			oResponse.setStatus(1);
	            oResponse.setMsg(messageSource.getMessage("error.passwords.policy", null, null));
	            return oResponse;
    		}
            
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        
        
        
              
        if("admin".equals(user.getEmail())) {
        	
        	String oldsalt = "";
            String newsalt = "";
            String oldEncodedPassword = "";
            String newEncodedPassword = "";
            try {
    			oldsalt = PasswordUtils.extractPassword()[1];
    			newsalt = PasswordUtils.generateSalt();
    			newEncodedPassword = shaPasswordEncoder.encodePassword(newPassword, newsalt);
            	oldEncodedPassword = PasswordUtils.generateDigest(oldPassword);
    		} catch (FileNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
        	
        	
        	try {
	    		Runtime rt = Runtime.getRuntime();
	    		Process proc;
				proc = rt.exec(new String[]{"authadmin.sh", "change", "admin", oldEncodedPassword, newEncodedPassword, newsalt});
				BufferedReader outputStream = new BufferedReader(
				new InputStreamReader(proc.getInputStream()));
				String output = null;
				String authStatus = null;
				while ((output = outputStream.readLine()) != null) {
					authStatus = output;
					break;
				}
				if ("S".equals(authStatus)) {
	                oResponse.setStatus(0);
	                oResponse.setMsg(messageSource.getMessage("password.change.success", null, null));
	                userAuditLoggerUtil.log("Password changed for user " + user.getEmail() , UserAuditActionType.Change_Password.getName());
				}
				else {
	                oResponse.setStatus(1);
	                oResponse.setMsg(messageSource.getMessage("password.old.mismatch", null, null));
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
        }
        else if(RoleType.Admin == user.getRole().getRoleType()) {
        	
        	String newsalt = "";
            String oldEncodedPassword = "";
            String newEncodedPassword = "";
            try {
    			newsalt = PasswordUtils.generateSalt();
    			newEncodedPassword = shaPasswordEncoder.encodePassword(newPassword, newsalt);
            	oldEncodedPassword = PasswordUtils.generateAdminTypeUserDigest(oldPassword,user.getEmail());
    		} catch (FileNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
        	
        	
        	try {
	    		Runtime rt = Runtime.getRuntime();
	    		Process proc;
				proc = rt.exec(new String[]{"authadmin.sh", "change", user.getEmail(), oldEncodedPassword, newEncodedPassword, newsalt});
				BufferedReader outputStream = new BufferedReader(
				new InputStreamReader(proc.getInputStream()));
				String output = null;
				String authStatus = null;
				while ((output = outputStream.readLine()) != null) {
					authStatus = output;
					break;
				}
				if ("S".equals(authStatus)) {
	                oResponse.setStatus(0);
	                oResponse.setMsg(messageSource.getMessage("password.change.success", null, null));
	                userAuditLoggerUtil.log("Password changed for user " + user.getEmail() , UserAuditActionType.Change_Password.getName());
				}
				else {
	                oResponse.setStatus(1);
	                oResponse.setMsg(messageSource.getMessage("password.old.mismatch", null, null));
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
        }
        else {
            if(user.getPassword() != null && user.getPassword().equals(passwordEncoder.encodePassword(oldPassword, null))){
                user.setPassword(passwordEncoder.encodePassword(newPassword, null));
                userManager.update(user);
                oResponse.setStatus(0);
                oResponse.setMsg(messageSource.getMessage("password.change.success", null, null));
                userAuditLoggerUtil.log("Password changed for user " + user.getEmail() , UserAuditActionType.Change_Password.getName());
            } else {
                oResponse.setStatus(1);
                oResponse.setMsg(messageSource.getMessage("password.old.mismatch", null, null));
            }
        }

        return oResponse;
    }
	    
}
