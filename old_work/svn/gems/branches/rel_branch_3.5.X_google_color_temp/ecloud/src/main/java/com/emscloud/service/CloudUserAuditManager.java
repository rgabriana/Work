package com.emscloud.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.CloudUserAuditDao;
import com.emscloud.dao.UserDao;
import com.emscloud.model.CloudUserAudit;
import com.emscloud.model.Users;


@Service("cloudUserAuditManager")
@Transactional(propagation = Propagation.REQUIRED)
public class CloudUserAuditManager {
	
	@Resource
	private CloudUserAuditDao cloudUserAuditDao;
    
	@Resource
    private UserDao userDao;
	
	public CloudUserAudit save(CloudUserAudit cloudUserAudit) {
		Users user = null;
		if(cloudUserAudit.getUser().getId() != null && cloudUserAudit.getUser().getId() >= 0){
		   user = userDao.loadUserByUserId(cloudUserAudit.getUser().getId());
		}else{
			//As this user does not exist so it's quite possible that this has been 
			//invoked by a headless client. So let's set the ip address of the invoking client.
			cloudUserAudit.setUsername(cloudUserAudit.getIpAddress());
		}
		cloudUserAudit.setUser(user);
		return (CloudUserAudit)cloudUserAuditDao.saveObject(cloudUserAudit);
	}
	
	public List<Object> getUserAudits(String order, String orderWay, String filterData, int offset, int limit) {
		
       
		List<Object> filter = new ArrayList<Object>();
		try {
			String[] params = filterData.split("#");
			if (params != null && params.length > 0) {
				if (params[1] != null && !"".equals(params[1])) {
					filter.add(URLDecoder.decode(params[1], "UTF-8"));
				} else {
					filter.add(null);
				}
				
				if (params[2] != null && !"".equals(params[2])) {
					filter.add(Arrays.asList(params[2].split(",")));
				} else {
					filter.add(null);
				}
	
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss a");
				if (params[3] != null && !"".equals(params[3])) {
					filter.add((Date) sdf.parse(params[3]));
				} else {
					filter.add(null);
				}
	
				if (params[4] != null && !"".equals(params[4])) {
					filter.add((Date) sdf.parse(params[4]));
				} else {
					filter.add(null);
				}
				
				if (params[5] != null && !"".equals(params[5])) {
					filter.add(URLDecoder.decode(params[5], "UTF-8"));
				} else {
					filter.add(null);
				}
	
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cloudUserAuditDao.getUserAudits(order, orderWay, filter, offset, limit);
	}
}
