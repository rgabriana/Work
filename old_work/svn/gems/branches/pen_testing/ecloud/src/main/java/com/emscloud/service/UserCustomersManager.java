package com.emscloud.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.BaseDaoHibernate;
import com.emscloud.dao.UserCustomersDao;
import com.emscloud.dao.UserDao;
import com.emscloud.model.Customer;
import com.emscloud.model.UserCustomers;
import com.emscloud.model.Users;


@Service("userCustomersManager")
@Transactional(propagation = Propagation.REQUIRED)
public class UserCustomersManager{
	
	@Resource
	private UserDao userDao;
	@Resource
	UserCustomersDao userCustomersDao;
	
	public UserCustomers save(UserCustomers user) {
        //user.setCreatedOn(new Date());
        return (UserCustomers) userCustomersDao.saveObject(user);
    }
	
	public List<UserCustomers> loadUserCustomersByUserId(Long userId) {
		
		return userCustomersDao.loadUserCustomersByUserId(userId) ;
	}

	public void deleteOldAssignments(Long userid) {
		// TODO Auto-generated method stub
			userCustomersDao.deleteOldAssignments(userid);		
	}
	
	public Map<Long,UserCustomers> loadMapForUser(Long userid)
	{
	// Key customer id 
	Map<Long,UserCustomers> uCustomerMap = new HashMap<Long, UserCustomers>();
	List<UserCustomers> mList = userCustomersDao.loadUserCustomersByUserId(userid);
	
	for (Iterator iterator = mList.iterator(); iterator.hasNext();) {
		UserCustomers userCustomers = (UserCustomers) iterator.next();
		uCustomerMap.put(userCustomers.getCustomer().getId(), userCustomers);		
	}		
	return uCustomerMap;
	}

	public void removeByUserAndCustomerId(Long userid, Long customerKey) {
		// TODO Auto-generated method stub
		userCustomersDao.removeByUserAndCustomerId(userid,customerKey);
	}
	

}
