package com.emscloud.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.UserDao;
import com.emscloud.model.Users;

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
	public Users loadUserByUserName(String userId) {
		return userDao.loadUserByUserName(userId);
	}
	
	public Users loadUserByUserId(Long userId) {
		return userDao.loadUserByUserId(userId);
	}
	
	public Users save(Users user) {
        //user.setCreatedOn(new Date());
        return (Users) userDao.saveObject(user);
    }
	
	public void saveOrUpdate(Users user)
	{
		userDao.saveOrUpdate(user);		
	}
	
	public List<Users> loadallUsers() {
		
		return userDao.loadAllUsers();
	}

	public Users update(Users userToSave) {
		// TODO Auto-generated method stub
		return (Users) userDao.saveObject(userToSave);		
	}

	public int delete(Long id) {
		// TODO Auto-generated method stub
		int status = 1;
		try {
			userDao.removeObject(Users.class, id);
		} catch (Exception e) {
			status = 0;
			e.printStackTrace();
		}
		return status;
	}
	
	

}
