package com.ems.mvc.controller;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ems.model.User;
import com.ems.service.UserManager;
import com.ems.util.Constants;
import com.ems.utils.DateUtil;

@Controller
@RequestMapping("/loginFailure")
public class LoginFailureController {

	@Autowired
    protected MessageSource messageSource;
	
	@Resource
	UserManager userManager;

	@RequestMapping("/loginAttempts.ems")
    String loginFailure(Model model,HttpServletRequest request, HttpServletResponse response) {
		
		final Object obj = request.getSession().getAttribute("userId");
		final Object defaultDelay = request.getSession().getAttribute(Constants.DEFAULT_DELAY_ADMIN_ATTRIB);
		User user = null;
		boolean isAdmin = false;
		if(obj != null){
			user = userManager.loadUserById((Long)obj);
			if(user != null){
				isAdmin = user.isAdmin(); 
				if(isAdmin){
					//show him the forgot password link
					model.addAttribute("isFPLink", "true");
					//Reset the unlock time if there is the case..
					if(user.getUnlockTime() != null && user.getUnlockTime().getTime() < (new Date()).getTime()){
						user.setUnlockTime(null);
						user.setNoOfLoginAttempts(0l);
					}
				}
				
				updateLoginAttempts(user, user.getNoOfLoginAttempts() + 1);
				
				if(!user.isUserLocked()){
					final String warningMsg = messageSource.getMessage(Constants.ERROR_LOGIN_ATTEMPTS,
							new Object[]{user.getNoOfAttemptsRemaining()}, LocaleContextHolder.getLocale());
					model.addAttribute("loginerror", warningMsg);
				}else if(user.isUserLocked()){
					//DO nothing. dont set login error in model..
				}
				
				
				
			}
		}else{
			final String warningMsg = messageSource.getMessage(Constants.ERROR_LOGIN_ATTEMPTS_GENERAL,
					new Object[]{""}, LocaleContextHolder.getLocale());
			model.addAttribute("loginerror", warningMsg);
		}
		
		if(null != defaultDelay){
			long defaultDelayTimeInMillis = (Long)defaultDelay;
			model.addAttribute(Constants.DEFAULT_DELAY_ADMIN_ATTRIB, defaultDelayTimeInMillis);
		}else{
			model.addAttribute(Constants.DEFAULT_DELAY_ADMIN_ATTRIB, 0l);
		}
		
		if(isAdmin && user != null && user.isUserLocked() ){
			final String warningMsg = messageSource.getMessage(Constants.ADMIN_LOCKED_MESSAGE,
					new Object[]{DateUtil.formatDate(user.getUnlockTime(),Constants.DATE_FORMAT_READABLE)}, LocaleContextHolder.getLocale());
			model.addAttribute("loginerror", warningMsg);
		}
		 request.getSession().removeAttribute("userId");
		 //request.getSession().invalidate();
        return "forward:/login.jsp?loginAttempts=true";
    }
	
	@RequestMapping("/{caseId}/forgotLoginAttempts.ems")
    String forgotPassword(Model model, @PathVariable("caseId") Integer caseId, HttpServletRequest request, HttpServletResponse response) {
		
		final Object obj = request.getSession().getAttribute("userId");
		User user = null;
		if(obj != null){
			user = userManager.loadUserById((Long)obj);
		}
		switch(caseId){
			case 1 :
					//case of expired password from support
					final String expiredMsg = messageSource.getMessage(Constants.FORGOT_PASS_EXPIRED,
						null, LocaleContextHolder.getLocale());
					model.addAttribute("loginerror", expiredMsg);
					break;
			default :
				final String warningMsg = messageSource.getMessage("error.login.incorrect.general",
						new Object[]{""}, LocaleContextHolder.getLocale());
				model.addAttribute("loginerror", warningMsg);
					break;
		}
		
		request.getSession().removeAttribute("userId");
        return "forward:/forgotPassword.jsp?resetFail=true";
    }
	
	private void updateLoginAttempts(final User user, final long noOfLoginAttempts){
		//final User user = userManager.loadUserById(userObj.getId());
		user.setNoOfLoginAttempts(noOfLoginAttempts);
		//userManager.updateUserDetailsInNewTransaction(Constants.NO_LOGIN_ATTEMPT_COLUMN, String.valueOf(noOfLoginAttempts), String.valueOf(user.getId()));
		//@For admin users set the lockout time.
		//if this is his 5th attempt then set the lockout time to a day greater than the current time.
		if(user.isAdmin() &&  noOfLoginAttempts == Constants.MAX_LOGIN_ATTEMPTS_ADMINS){
			final String adminLockPeriodStr = messageSource.getMessage(Constants.ADMIN_LOCK_PERIOD_HOURS,null, LocaleContextHolder.getLocale());
			final long incrementTimeMillis = Long.parseLong(adminLockPeriodStr) * 60 * 60 * 1000;
			user.setUnlockTime(DateUtil.addTimeToDate(new Date(), incrementTimeMillis));
		}
		userManager.save(user);
	}


}
