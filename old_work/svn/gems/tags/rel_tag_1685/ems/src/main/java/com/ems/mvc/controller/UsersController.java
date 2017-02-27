package com.ems.mvc.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.encoding.PasswordEncoder;
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
import com.ems.security.EmsAuthenticationContext;
import com.ems.service.TenantManager;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserAuditActionType;
import com.ems.types.UserStatus;

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

    @RequestMapping("/list.ems")
    String getListOfUsers(Model model, @RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(value = "facilityAssignmentStatus", required = false) String facilityAssignmentStatus) {

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
        model.addAttribute("tenantId", tenantId);
        model.addAttribute("tenant", (tenantId != null && tenantId != 0L ? tenantManger.get(tenantId) : null));
        model.addAttribute("usersList", usersList);
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
            user.setPassword(passwordEncoder.encodePassword(password, null));
            User savedUser = userManager.save(user);
            Role role = userManager.loadRoleByUserId(savedUser.getId());
            userAuditLoggerUtil.log("Create user: " + user.getEmail() + "(Role - " + role.getRoleType() + ", id - "
                    + user.getId() + ")", UserAuditActionType.User_Create.getName());
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
            User savedUser = userManager.update(userToSave);
            Role role = userManager.loadRoleByUserId(savedUser.getId());
            userAuditLoggerUtil.log("Update user: " + user.getEmail() + "(Role - " + role.getRoleType() + ", id - "
                    + user.getId() + ")", UserAuditActionType.User_Update.getName());
        }

        return "redirect:/users/list.ems?tenantId=" + tenantId;
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
        userAuditLoggerUtil.log("Delete user: " + userName + "( id - " + user.getId() + ")",
                UserAuditActionType.User_Delete.getName());
        if (tenantId == null)
            return "redirect:list.ems";
        else
            return "redirect:list.ems?tenantId=" + tenantId;
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
