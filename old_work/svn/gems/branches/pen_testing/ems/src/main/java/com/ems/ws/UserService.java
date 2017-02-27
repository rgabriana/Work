/**
 * 
 */
package com.ems.ws;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;

import com.ems.filter.GlemRequestValidationFilter;
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
import com.ems.service.SystemConfigurationManager;
import com.ems.service.TenantManager;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.util.Constants;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/user")
public class UserService {
	private static final Logger logger = Logger.getLogger("WSLogger");
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
    
    @Resource(name = "systemConfigurationManager")
	private SystemConfigurationManager systemConfigurationManager;
    
    @Resource (name="passwordValidator")
    private ENLValidator passwordValidator;
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
    
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
	@Path("list/{username}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public User getUserList(@PathParam("username") String username) {
		Response resp = new Response();
		resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "username", username);
		if(resp!=null && resp.getStatus()!=200){
			m_Logger.error("Validation error "+resp.getMsg());
			return null;
		}
		return userManager.loadUserByUserName(username);
	}

	/**
	 * returns list of all facility users and tenant users 
	 * @return List of User
	 */
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
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
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
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
        
        
        String[] oldPasswordArray = {"","","","",""};
        final String oldPasswords = user.getOldPasswords();
	   	if(oldPasswords != null && !"".equals(oldPasswords)){
	   		oldPasswordArray = oldPasswords.split(Constants.FORGOT_PASS_TEMP_SPLITTER);
	   	}
	   	if(PasswordUtils.isPasswordUsedBefore(passwordEncoder.encodePassword(newPassword, null), oldPasswordArray)){
	   		oResponse.setStatus(1);
			oResponse.setMsg(messageSource.getMessage("password.change.lastused", null, null));
			return oResponse;
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
	            }
				else {
	                oResponse.setStatus(1);
	                oResponse.setMsg(messageSource.getMessage("password.old.mismatch", null, null));
	                return oResponse;
				}
			} catch (IOException ioe) {
				oResponse.setMsg("Some error occurred");
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
	            }
				else {
	                oResponse.setStatus(1);
	                oResponse.setMsg(messageSource.getMessage("password.old.mismatch", null, null));
	                return oResponse;
				}
			} catch (IOException ioe) {
				
				ioe.printStackTrace();
			}
        }
        else {
        	
        	String oldEncodedPassword = "";
            String newEncodedPassword = "";
        	
            if(user.getPassword() != null && user.getPassword().equals(passwordEncoder.encodePassword(oldPassword, null))){
                newEncodedPassword = passwordEncoder.encodePassword(newPassword, null);
                oResponse.setStatus(0);
                oResponse.setMsg(messageSource.getMessage("password.change.success", null, null));
            } else {
                oResponse.setStatus(1);
                oResponse.setMsg(messageSource.getMessage("password.old.mismatch", null, null));
                return oResponse;
            }
        }

        final String newEncodedPassword = passwordEncoder.encodePassword(newPassword, null);
        user.setPassword(newEncodedPassword);
    	user.setOldPasswords(oldPasswordArray[1]+ Constants.FORGOT_PASS_TEMP_SPLITTER + oldPasswordArray[2] + Constants.FORGOT_PASS_TEMP_SPLITTER
    			+ oldPasswordArray[3] + Constants.FORGOT_PASS_TEMP_SPLITTER + oldPasswordArray[4] + Constants.FORGOT_PASS_TEMP_SPLITTER + newEncodedPassword);
    	user.setPasswordChangedAt(Calendar.getInstance().getTime());
    	userManager.update(user);
    	userAuditLoggerUtil.log("Password changed for user " + user.getEmail() , UserAuditActionType.Change_Password.getName());
    	
        return oResponse;
    }
	
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("genapikey/{email}")
	public javax.ws.rs.core.Response generateAPIKey(@PathParam("email") String email) {
		try {
			if(StringUtils.isEmpty(email)){
				return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity("User does not exists.").build();
			}
			final User user = userManager.loadUserByUserName(email);
			if(user == null){
				return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity("User does not exists.").build();
			}
			final java.util.Date time = new java.util.Date(System.currentTimeMillis());
			final String apiKey = GlemRequestValidationFilter.getSaltBasedDigest(email,String.valueOf(time.getTime()),Constants.ENL_AUTH_KEY, Constants.SHA1_ALGO);
			user.setSecretKey(apiKey);
			userManager.save(user);
			userAuditLoggerUtil.log("User "+email+" has successfuly generated the secret api key "+apiKey+" at "+ time, UserAuditActionType.Api_Access.getName());
			return javax.ws.rs.core.Response.ok(apiKey).build();
		} catch (Exception e) {
			logger.error("Issue in generating API key for "+email, e);
			return javax.ws.rs.core.Response.serverError().status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	
	}
}
