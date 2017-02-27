package com.ems.task;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.filter.GlemRequestValidationFilter;
import com.ems.model.Role;
import com.ems.model.User;
import com.ems.service.UserManager;
import com.ems.types.RoleType;
import com.ems.types.UserStatus;
import com.ems.util.Constants;

/**
 * One time activity after system start up that will search all users having secret key is not generated.
 * For each such user a new secret key will be generated.
 * @author admin
 *
 */
public class GenerateSecretKeyTask implements Runnable{

	private static final Logger log = Logger.getLogger(GenerateSecretKeyTask.class);
	@Override
	public void run() {
		//Generate secret key for all users who do not have that.
		try {
			final UserManager userManager = (UserManager) SpringContext.getBean("userManager");
			
			//Create backNet user if not exists in the db
			final User user = userManager.loadBacnetUser();
			if(user == null){
				final User u = new User();
				u.setEmail("bacnet");
				final Role role = userManager.getRoleByRoleType(RoleType.Bacnet);
				if(role == null){
					//
					log.error("ERROR OCCURED: bacnet role does not exists in the db. Please check upgrade/install script");
				}else{
					u.setRole(role);
					u.setPassword("21232f297a57a5a743894a0e4a801fc3");
					u.setCreatedOn(new Date());
					u.setNoOfLoginAttempts(0l);
					u.setStatus(UserStatus.ACTIVE);
					u.setTermConditionAccepted(false);
					userManager.save(u);
				}
			}
			
			final List<User> list = userManager.loadAllUsersHavingNoSecretKey();
			final java.util.Date time = new java.util.Date(System.currentTimeMillis());
			if(list != null){
				for (final User u: list){
					final String key = GlemRequestValidationFilter.getSaltBasedDigest(u.getEmail(),String.valueOf(time.getTime()),Constants.ENL_AUTH_KEY, Constants.SHA1_ALGO);
					u.setSecretKey(key);
					userManager.save(u);
				}
			}
		} catch (Exception e) {
			log.error("ERROR Occured in generating secret key for old users:",e);
		}
		
	}
	
	

}
