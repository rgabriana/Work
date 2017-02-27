package com.ems.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.ImageUpgradeDeviceStatus;
import com.ems.model.ImageUpgradeDeviceStatusList;
import com.ems.utils.DateUtil;


@Repository("imageUpgradeDeviceStatusDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ImageUpgradeDeviceStatusDao extends BaseDaoHibernate {
    
	@SuppressWarnings("unchecked")
	public List<ImageUpgradeDeviceStatus> loadAllImageUpgradeDeviceStatus() {
		
		Session session = getSession();
		return session.createCriteria(ImageUpgradeDeviceStatus.class).list();		
		
	} 
	
	@SuppressWarnings("unchecked")
	public ImageUpgradeDeviceStatusList loadImageUpgradeDeviceStatusList(String orderby, String orderway, Boolean bSearch, String searchField, String searchString,int offset, int limit) {
		
		ImageUpgradeDeviceStatusList imageUpgradeDeviceStatusList = new ImageUpgradeDeviceStatusList();
		
		String queryString = "select imjb.job_name,d.name,imst.id,imst.job_id,imst.status,imst.new_version,imst.no_of_attempts,imst.start_time,imst.end_time,imst.description from image_upgrade_device_status imst left join device d on d.id = imst.device_id left join image_upgrade_job imjb on imjb.id = imst.job_id";
        
		String query2String = "select COUNT(*) from image_upgrade_device_status imst left join device d on d.id = imst.device_id left join image_upgrade_job imjb on imjb.id = imst.job_id";
		
		
		if (bSearch) {
			if (searchField.equals("jobName")) {
				
				queryString = queryString + " where imjb.job_name like '%"+searchString+"%'";
				
				query2String = query2String + " where imjb.job_name like '%"+searchString+"%'";
				
			}else if (searchField.equals("deviceName")) {
				
				queryString = queryString + " where d.name like '%"+searchString+"%'";
				
				query2String = query2String + " where d.name like '%"+searchString+"%'";
				
			}else if (searchField.equals("status")) {
				
				queryString = queryString + " where imst.status like '%"+searchString+"%'";
				
				query2String = query2String + " where imst.status like '%"+searchString+"%'";
				
			}else if (searchField.equals("new_version")) {
				
				queryString = queryString + " where imst.new_version like '%"+searchString+"%'";
				
				query2String = query2String + " where imst.new_version like '%"+searchString+"%'";
				
			}
		}
		
		if("jobName".equals(orderby)){
			queryString = queryString + " order by imjb.job_name";
		}else if ("deviceName".equals(orderby)){
			queryString = queryString + " order by d.name";
		}else if ("status".equals(orderby)){
			queryString = queryString + " order by imst.status";
		}else if ("new_version".equals(orderby)){
			queryString = queryString + " order by imst.new_version";
		}else if ("noOfAttempts".equals(orderby)){
			queryString = queryString + " order by imst.no_of_attempts";
		}else if ("startTime".equals(orderby)){
			queryString = queryString + " order by imst.start_time";
		}else if ("endTime".equals(orderby)){
			queryString = queryString + " order by imst.end_time";
		}else if ("description".equals(orderby)){
			queryString = queryString + " order by imst.description";
		}else{
			queryString = queryString + " order by imst.start_time";
		}
		
		if ("desc".equals(orderway)){
			queryString = queryString + " DESC";
		}else{
			queryString = queryString + " ASC";
		}
		
		SQLQuery sqlQuery = getSession().createSQLQuery(queryString);
		
		if (limit > 0) {
			sqlQuery.setMaxResults(limit).setFirstResult(offset);
		}
		
		List list = sqlQuery.list();
		
		List<ImageUpgradeDeviceStatus> imageUpgradeDeviceStatuses = new ArrayList<ImageUpgradeDeviceStatus>();
		
		for (int i = 0; i < list.size(); i++) {
		    
			Object[] data = (Object[]) list.get(i);
		    ImageUpgradeDeviceStatus ImageUpgradeDeviceStatus = new ImageUpgradeDeviceStatus();
		    
		    if(data[0] != null){
		    	ImageUpgradeDeviceStatus.setJobName(data[0].toString());
		    }else{
		    	ImageUpgradeDeviceStatus.setJobName("");
		    }
		    
		    if(data[1] != null){
		    	ImageUpgradeDeviceStatus.setDeviceName(data[1].toString());
		    }else{
		    	ImageUpgradeDeviceStatus.setDeviceName("");
		    }
		    
		    ImageUpgradeDeviceStatus.setId(Long.valueOf(data[2].toString()));
		    
		    if(data[3] != null){
		    	ImageUpgradeDeviceStatus.setDeviceId(Long.valueOf(data[3].toString()));
		    }else{
		    	ImageUpgradeDeviceStatus.setDeviceId(null);
		    }
		    
		    if(data[4] != null){
		    	ImageUpgradeDeviceStatus.setStatus(data[4].toString());
		    }else{
		    	ImageUpgradeDeviceStatus.setStatus("");
		    }
		    
		    if(data[5] != null){
		    	ImageUpgradeDeviceStatus.setNew_version(data[5].toString());
		    }else{
		    	ImageUpgradeDeviceStatus.setNew_version("");
		    }
		    
		    if(data[6] != null){
		    	ImageUpgradeDeviceStatus.setNoOfAttempts(Integer.valueOf(data[6].toString()));
		    }else{
		    	ImageUpgradeDeviceStatus.setNoOfAttempts(null);
		    }
		    
		    if(data[7] != null){
		    	ImageUpgradeDeviceStatus.setStartTime(DateUtil.parseString(data[7].toString(), "yyyy-MM-dd HH:mm:ss.S"));
		    }else{
		    	ImageUpgradeDeviceStatus.setStartTime(null);
		    }
		    
		    if(data[8] != null){
		    	ImageUpgradeDeviceStatus.setEndTime(DateUtil.parseString(data[8].toString(), "yyyy-MM-dd HH:mm:ss.S"));
		    }else{
		    	ImageUpgradeDeviceStatus.setEndTime(null);
		    }
		    
		    if(data[9] != null){
		    	ImageUpgradeDeviceStatus.setDescription(data[9].toString());
		    }else{
		    	ImageUpgradeDeviceStatus.setDescription("");
		    }
		    
		    imageUpgradeDeviceStatuses.add(ImageUpgradeDeviceStatus);
		}
		
		imageUpgradeDeviceStatusList.setImageUpgradeDeviceStatuses(imageUpgradeDeviceStatuses);
		
		SQLQuery sqlQuery2 = getSession().createSQLQuery(query2String);
		
		imageUpgradeDeviceStatusList.setTotal(((BigInteger)sqlQuery2.uniqueResult()).longValue());
		
		return imageUpgradeDeviceStatusList;
			
	}

	
} 