package com.ems.server.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.mockito.Matchers.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.ems.action.SpringContext;
import com.ems.model.Ballast;
import com.ems.model.Building;
import com.ems.model.Bulb;
import com.ems.model.Campus;
import com.ems.model.EnergyConsumption;
import com.ems.model.Fixture;
import com.ems.model.Floor;
import com.ems.model.PlacedFixture;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.server.ServerMain;
import com.ems.service.BallastManager;
import com.ems.service.BuildingManager;
import com.ems.service.BulbManager;
import com.ems.service.CampusManager;
import com.ems.service.FixtureManager;
import com.ems.service.FloorManager;
import com.ems.service.PlacedFixtureManager;
import com.ems.types.UserAuditActionType;
import com.ems.vo.model.PlacementInfoVO;
import com.ems.vo.model.SensorConfig;
import com.ems.ws.FixtureService;
import com.ems.ws.util.Response;

@RunWith(PowerMockRunner.class)
public class PlacedSensorInfoTest {

	@Resource (name="campusManager")
	CampusManager campusManager;
	
	static PlacementInfoVO plInfo;
	static List cfgInfoList;
	SpringContext spContextGlobal;
	
	@BeforeClass
	public static void createPlacementInfoObject()
	{
		plInfo = new PlacementInfoVO();
		cfgInfoList = new ArrayList<SensorConfig>();
		
		plInfo.setCampusName("TestCampus");
		plInfo.setBldgName("TestBuilding");
		plInfo.setFloorName("TestFloor");
		plInfo.setSensorConfigList(cfgInfoList);
		
		SensorConfig cfg1 = new SensorConfig();
		cfg1.setBallastName("QTP 1x32T8/UNV DIM-TC");
		cfg1.setBulbName("F17T8/TL830/ALTO");
		cfg1.setMac("986456123456");
		cfg1.setNoOfBallasts((long) 2);
		cfg1.setVoltage((long) 277);
		cfg1.setX((long) 123);
		cfg1.setY((long) 345);
		
		plInfo.getSensorConfigList().add(cfg1);

		SensorConfig cfg2 = new SensorConfig();
		cfg2.setBallastName("QTP 1x32T8/UNV DIM-TC");
		cfg2.setBulbName("F17T8/TL830/ALTO");
		cfg2.setMac("986456654321");
		cfg2.setNoOfBallasts((long) 2);
		cfg2.setVoltage((long) 277);
		cfg2.setX((long) 123);
		cfg2.setY((long) 345);
		
		plInfo.getSensorConfigList().add(cfg2);
	}

   @PrepareForTest({ SpringContext.class })
   public void testSetPlacementInfo() {
	   
		try {
		   Campus campus = new Campus();
		   campus.setId((long) 1);
		   campus.setName("TestCampus");
		   
		   Building bldg = new Building((long) 1, "TestBuilding");
		   
		   Floor floor = new Floor((long) 1, "TestFloor");
		   
		   Ballast ballast = new Ballast();
		   Bulb bulb = new Bulb();
		   
			PowerMockito.mockStatic(SpringContext.class);

			CampusManager campusManager = PowerMockito.spy(new CampusManager());
			BuildingManager buildingManager = PowerMockito.spy(new BuildingManager());
			FloorManager floorManager = PowerMockito.spy(new FloorManager());
			FixtureManager fixtureManager = PowerMockito.spy(new FixtureManager());
//			PlacedFixtureManager placedFixtureManager = PowerMockito.spy(new PlacedFixtureManager());
			PlacedFixtureManager placedFixtureManager = PowerMockito.mock(PlacedFixtureManager.class);
			BulbManager bulbManager = PowerMockito.spy(new BulbManager());
			BallastManager ballastManager = PowerMockito.spy(new BallastManager());
			UserAuditLoggerUtil userAuditLoggerUtil = PowerMockito.mock(UserAuditLoggerUtil.class);
			
			PowerMockito.doReturn(campus).when(campusManager, "getCampusByName", "TestCampus");
			PowerMockito.doReturn(bldg).when(buildingManager, "getBuildingByNameAndCampusId", "TestBuilding", 1L);
			PowerMockito.doReturn(floor).when(floorManager, "getFloorByNameAndBuildingId", "TestFloor", 1L);
			PowerMockito.doReturn(null).when(fixtureManager, "getFixtureByMacAddr", Mockito.anyString());
			PowerMockito.doReturn(null).when(placedFixtureManager, "getPlacedFixtureByMacAddr", Mockito.anyString());
			PowerMockito.doReturn(ballast).when(ballastManager, "getBallastByName", Mockito.anyString());
			PowerMockito.doReturn(bulb).when(bulbManager, "getBulbByName", Mockito.anyString());
			PowerMockito.when(placedFixtureManager, "save", Mockito.any(PlacedFixture.class)).thenAnswer(new Answer() {
				public Object answer(InvocationOnMock invocation)
						throws Throwable {
					// TODO Auto-generated method stub

					Object args[] = invocation.getArguments();
					PlacedFixture fixture = (PlacedFixture) args[0];
					SensorConfig result = null;

					for (Iterator iterator = cfgInfoList.iterator(); iterator
							.hasNext();) {
						SensorConfig type = (SensorConfig) iterator.next();
						if (type.getMac().equals(fixture.getMacAddress())) {
							result = type;
							break;
						}
					}

					if (result != null) {
						assertEquals(new Long(fixture.getVoltage()), result.getVoltage());
						assertEquals(new Long(fixture.getXaxis()), result.getX());
						assertEquals(new Long(fixture.getYaxis()), result.getY());
					}
					return fixture;
				}
			});
	
			FixtureService fxService = PowerMockito.spy(new FixtureService());
			Whitebox.setInternalState(fxService, "campusManager", campusManager);
			Whitebox.setInternalState(fxService, "buildingManager", buildingManager);
			Whitebox.setInternalState(fxService, "floorManager", floorManager);
			Whitebox.setInternalState(fxService, "fixtureManager", fixtureManager);
			Whitebox.setInternalState(fxService, "placedFixtureManager", placedFixtureManager);
			Whitebox.setInternalState(fxService, "ballastManager", ballastManager);
			Whitebox.setInternalState(fxService, "bulbManager", bulbManager);
			Whitebox.setInternalState(fxService, "userAuditLoggerUtil", userAuditLoggerUtil);
	
			Response resp = fxService.setPlacementInfo(plInfo);
		   
			assertEquals(resp.getStatus(), 200);
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
}