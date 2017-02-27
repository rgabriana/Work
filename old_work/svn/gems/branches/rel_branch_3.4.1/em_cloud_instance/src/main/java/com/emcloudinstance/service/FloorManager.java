package com.emcloudinstance.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emcloudinstance.dao.FloorDao;


@Service("floorManager")
@Transactional(propagation = Propagation.REQUIRED)
public class FloorManager {
	
	@Resource
	FloorDao floorDao ;
	
	 public List getAllFloorsOfCompany(String mac) throws SQLException, IOException {
	      List floors = floorDao.getAllFloorsOfCompany(mac);
	      return floors;
	  }
	
	

}
