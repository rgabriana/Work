/**
 * 
 */
package com.ems.ws;

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

import org.springframework.context.MessageSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;

import com.ems.model.Scene;
import com.ems.model.Tenant;
import com.ems.model.User;
import com.ems.model.UserSwitches;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.SwitchManager;
import com.ems.service.TenantManager;
import com.ems.service.UserManager;
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

    @Resource(name = "emsAuthContext")
    private EmsAuthenticationContext emsAuthContext;
    
    @Resource(name = "messageSource")
    private MessageSource messageSource;
    
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
	    for(UserSwitches userswitch : userSwitchesList){
	        List<UserSwitches> list = userManager.loadUserSwitchesByUserIdSwitchId(userswitch.getUserId(), userswitch.getSwitchId());
	        if(list == null){
	            userManager.saveSwitchToUser(userswitch);
	        }
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
	 * @param oldPassword
	 *         Old password string
	 * @param newPassword
	 *         New password string
	 * @return
	 */
	@Path("changepassword/old/{oldPassword}/new/{newPassword}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response changePassword(@PathParam("oldPassword") String oldPassword, @PathParam("newPassword") String newPassword) {
	    Response oResponse = new Response();
        
        Long currentUserId = emsAuthContext.getUserId();
        User user = userManager.loadUserById(currentUserId);
        
        if(user.getPassword() != null && user.getPassword().equals(passwordEncoder.encodePassword(oldPassword, null))){
            user.setPassword(passwordEncoder.encodePassword(newPassword, null));
            userManager.update(user);
            oResponse.setStatus(0);
            oResponse.setMsg(messageSource.getMessage("password.change.success", null, null));
        } else {
            oResponse.setStatus(1);
            oResponse.setMsg(messageSource.getMessage("password.old.mismatch", null, null));
        }
        userAuditLoggerUtil.log("Changed Password");
        return oResponse;
    }
	    
}
