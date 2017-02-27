package com.communicator.diff;

import java.util.List;

import com.communicator.model.vo.AreaVO;
import com.communicator.model.vo.BuildingVO;
import com.communicator.model.vo.CampusVO;
import com.communicator.model.vo.CompanyVO;
import com.communicator.model.vo.FixtureVO;
import com.communicator.model.vo.FloorVO;
import com.communicator.model.vo.GatewayVO;


public interface IFinder {
	
	public void getChangedData(List<CompanyVO> company, List<CampusVO> campus,
			List<BuildingVO> building, List<FloorVO> floor, List<AreaVO> area,
			List<FixtureVO> fixture, List<GatewayVO> gateway);

}
