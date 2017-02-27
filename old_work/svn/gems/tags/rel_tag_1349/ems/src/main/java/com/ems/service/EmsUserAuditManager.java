package com.ems.service;

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

import com.ems.dao.EmsUserAuditDao;
import com.ems.dao.UserDao;
import com.ems.model.EmsUserAudit;
import com.ems.model.User;

@Service("emsUserAuditManager")
@Transactional(propagation = Propagation.REQUIRED)
public class EmsUserAuditManager {
	
	@Resource
	private EmsUserAuditDao emsUserAuditDao;
    @Resource
    private UserDao userDao;
	
	public EmsUserAudit save(EmsUserAudit emsUserAudit) {
		User user = userDao.loadUserById(emsUserAudit.getUser().getId());
		emsUserAudit.setUser(user);
		return (EmsUserAudit)emsUserAuditDao.saveObject(emsUserAudit);
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
	
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return emsUserAuditDao.getUserAudits(order, orderWay, filter, offset, limit);
	}
}
