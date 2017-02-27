package com.enlightedportal.service;

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

import com.enlightedportal.dao.UserAuditDao;
import com.enlightedportal.model.UserAudit;

@Service("userAuditManager")
@Transactional(propagation = Propagation.REQUIRED)
public class UserAuditManager {
	
	@Resource
	private UserAuditDao userAuditDao;
 	
	public UserAudit save(UserAudit userAudit) {
		return (UserAudit)userAuditDao.saveObject(userAudit);
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
		return userAuditDao.getUserAudits(order, orderWay, filter, offset, limit);
	}
}
