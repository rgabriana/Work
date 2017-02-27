package com.emsdashboard.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emsdashboard.dao.UserDao;
import com.emsdashboard.model.User;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Service("userManager")
@Transactional(propagation = Propagation.REQUIRED)
public class UserManager {

	@Resource
	private UserDao userDao;

	/**
	 * load user details if user email is given.
	 * 
	 * @param userId
	 *            user email or user id.
	 * @return User com.ems.model.User object
	 */
	public User loadUserByUserName(String userId) {
		return userDao.loadUserByUserName(userId);
	}

}
