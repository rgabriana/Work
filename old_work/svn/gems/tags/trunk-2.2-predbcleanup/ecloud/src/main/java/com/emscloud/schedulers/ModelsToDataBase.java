package com.emscloud.schedulers;
import java.util.ArrayList;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communicator.model.vo.*;
import com.emscloud.jdbcDao.dao.JdbcBuildingDao;
import com.emscloud.service.EmInstanceManager;
@Service("modelsToDataBase")
@Transactional(propagation = Propagation.REQUIRED)
public class ModelsToDataBase {
	
	@Resource
	JdbcBuildingDao jdbcBuildingDao;
	
	private String version ;
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getEmInstanceID() {
		return EmInstanceID;
	}
	public void setEmInstanceID(String emInstanceID) {
		EmInstanceID = emInstanceID;
	}
	private String EmInstanceID ;
	
	public void saveFloorVO(ArrayList<FloorVO> modelList)
	{
		
	}
	public void saveCompanyVO(ArrayList<CompanyVO> modelList)
	{
		
	}
	public void saveCampusVO(ArrayList<CampusVO> modelList)
	{
		
	}
	public void saveFixtureVO(ArrayList<FixtureVO> modelList)
	{
		
	}
	public void saveGatewayVO(ArrayList<GatewayVO> modelList)
	{
		
	}
	public void saveAreaVO(ArrayList<AreaVO> modelList)
	{
		
	}
	public void saveBuildingVO(ArrayList<BuildingVO> modelList)
	{
		for(BuildingVO building : modelList )
			jdbcBuildingDao.add(building.getId(), building.getName() ) ;
	}
	
	

}
