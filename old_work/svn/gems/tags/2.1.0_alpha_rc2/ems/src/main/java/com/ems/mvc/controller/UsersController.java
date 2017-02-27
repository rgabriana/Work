package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.model.Tenant;
import com.ems.model.User;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.TenantManager;
import com.ems.service.UserManager;
import com.ems.types.UserStatus;

@Controller
@RequestMapping("/users")
public class UsersController {

	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
    @Resource
    UserManager userManager;

    @Resource
    TenantManager tenantManger;

    @Resource
    PasswordEncoder passwordEncoder;

    @RequestMapping("/list.ems")
    String getListOfUsers(Model model, @RequestParam(value = "tenantId", required = false) Long tenantId) {
        List<User> usersList = null;
        if (tenantId == null || tenantId == 0) {
            usersList = userManager.loadCompanyUsers();
        } else {
            usersList = userManager.loadTenantUsers(tenantId);
        }
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("tenant", (tenantId!=null  && tenantId != 0L ? tenantManger.get(tenantId) : null));
        model.addAttribute("usersList", usersList);
        return "users/list";
    }

    @RequestMapping("/create.ems")
    String createUser(Model model, @RequestParam("tenantId") Long tenantId) {
        User user = new User();
        model.addAttribute("user", user);
        model.addAttribute("statusList", UserStatus.values());

        if (tenantId == null || tenantId == 0) {
            model.addAttribute("roles", userManager.loadRolesForCompany());
            model.addAttribute("tenantId", 0);
            model.addAttribute("validDomain", "");
        } else {
            Tenant tenant = tenantManger.get(tenantId);
            user.setTenant(tenant);
            model.addAttribute("roles", userManager.loadRolesForTenant());
            model.addAttribute("tenantId", tenantId);
            model.addAttribute("validDomain", tenant.getValidDomain());
        }

        userAuditLoggerUtil.log("Create User: " + user.getEmail() +"(" + user.getRole() +")");
        
        return "users/details";
    }

    @RequestMapping(value = "/{userId}/edit.ems")
    public String editUser(Model model, @PathVariable("userId") Long userId) {
        User user = userManager.loadUserById(userId);
        model.addAttribute("user", user);
        model.addAttribute("statusList", UserStatus.values());

        if (user.getTenant() != null) {
            model.addAttribute("roles", userManager.loadRolesForTenant());
            model.addAttribute("tenantId", user.getTenant().getId());
            model.addAttribute("validDomain", user.getTenant().getValidDomain());
        } else {
            model.addAttribute("roles", userManager.loadRolesForCompany());
            model.addAttribute("tenantId", 0);
            model.addAttribute("validDomain", "");
        }
        return "users/details";
    }

    @RequestMapping("/save.ems")
    String saveUser(User user, @RequestParam("tenantId") Long tenantId, @RequestParam("password") String password) {
        if (user.getId() == null || user.getId() == 0) {
            if (tenantId == null || tenantId == 0) {
                user.setTenant(null);
            } else {
                Tenant tenant = tenantManger.get(tenantId);
                user.setTenant(tenant);
            }
            user.setPassword(passwordEncoder.encodePassword(password, null));
            userManager.save(user);
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
                userToSave.setPassword(passwordEncoder.encodePassword(password, null));
            }
            userManager.update(userToSave);
        }
        userAuditLoggerUtil.log("Update User: " + user.getEmail() +"(" + user.getRole() +":"+user.getId() +")");
        return "redirect:/users/list.ems?tenantId=" + tenantId;
    }

    @RequestMapping("/assignuserstoswitches.ems")
    public String assignUsersToSwitches(Model model) {
        List<User> users = userManager.loadCompanyUsers();
        
        //If there are no company user than make an empty list of users
        //so that we can hold tenant users
        if (users == null){
        	users = new ArrayList<User>();
        }
        
        List<Tenant> tenants = tenantManger.getAllTenants();
        for(Tenant oTenant : tenants){
            users.addAll(userManager.loadTenantUsers(oTenant.getId()));
        }
        
        model.addAttribute("users", users);
       return "switches/assignusers";
    }
    
    @RequestMapping("/changepassworddialog.ems")
    public String changePasswordDialog(Model model) {
        return "users/changepassword/dialog";
    }
}
