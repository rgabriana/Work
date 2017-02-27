package com.ems.server.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.verification.PrivateMethodVerification;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ems.action.SpringContext;
import com.ems.cache.DeviceInfo;
import com.ems.cache.FixtureCache;
import com.ems.model.Ballast;
import com.ems.model.EnergyConsumption;
import com.ems.model.Fixture;
import com.ems.server.ServerMain;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.server.util.ServerUtil;
import com.ems.service.BallastManager;
import com.ems.service.EnergyConsumptionManager;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureManager;
import com.ems.service.MetaDataManager;
import com.ems.service.PricingManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PrivateMethodVerification.class)
public class PMStatsTest {

	Fixture fixture = new Fixture();
	Ballast ballast = new Ballast();

	SpringContext spContextGlobal;

	ServerUtil serverUtil;

	@Resource(name = "fixtureManager")
	private FixtureManager fixtureManager;

	@Resource(name = "pricingManager")
	private PricingManager pricingManager;

	@Resource(name = "metaDataManager")
	private MetaDataManager metaDataManager;
	
	@Resource(name = "ballastManager")
	private BallastManager ballastManager;

	/*
	 * @Resource(name = "energyConsumptionManager") private
	 * EnergyConsumptionManager ecManager;
	 */

	// Put the entries for ballast

	Ballast[][] mBallasts = new Ballast[2][2];
	Fixture[][] mFixtures = new Fixture[2][2];
	byte[][] mBytes = new byte[2][2];

	List<Ballast> bList = new ArrayList<Ballast>();
	List<Fixture> fList = new ArrayList<Fixture>();
	byte[][] twodim = new byte[2][];

	// Result set
	final List<EnergyConsumption> ecResult = new ArrayList<EnergyConsumption>();

	public void buildEnergyData() {

		EnergyConsumption ec1 = new EnergyConsumption();
		ec1.setAvgVolts((short) 75);
		ec1.setAmbientSaving(new BigDecimal(0));
		ec1.setAvgTemperature((short) 78);
		ec1.setBaseCost(0.0f);
		ec1.setBasePowerUsed(new BigDecimal(
				28.160000000000000142108547152020037174224853515625));
		ec1.setBrightOffset((short) 0);
		ec1.setBrightPercentage((short) 72);
		ec1.setCaptureAt(new Date()); // Note : Don't compare captureAt
		ec1.setCost(0.0f);
		ec1.setCurrState((short) 10);
		ec1.setCuStatus(0);
		ec1.setDimOffset((short) 0);
		ec1.setDimPercentage(null);
		ec1.setEnergyCalib(39427);
		ec1.setEnergyCum(null);
		ec1.setEnergyTicks(14);
		ec1.setLastTemperature((short) 30);
		ec1.setLastVolts((short) 75);
		ec1.setLightAvgLevel((short) 701);
		ec1.setLightMaxLevel((short) 728);
		ec1.setLightMinLevel((short) 634);
		ec1.setLightOff((short) 0);
		ec1.setLightOn((short) 0);
		ec1.setLightOnSeconds((short) 300);
		ec1.setMaxTemperature((short) 31);
		ec1.setMaxVolts((short) 75);
		ec1.setMinTemperature((short) 30);
		ec1.setMinVolts((short) 75);
		ec1.setMotionBits(new Long(0L));
		ec1.setOccCount(null);
		ec1.setOccIn(null);
		ec1.setOccOut(null);
		ec1.setPowerCalc((short) 1);
		ec1.setPrice(0.0f);
		ec1.setSavedCost(0.0f);
		ec1.setSavingType((short) 3);
		ec1.setSysUptime(new Long(1817255L));
		ec1.setZeroBucket((short) 0);
		ec1.setAmbientSaving(new BigDecimal(0));
		ec1.setBasePowerUsed(new BigDecimal(
				28.160000000000000142108547152020037174224853515625));
		ec1.setManualSaving(new BigDecimal(0));
		ec1.setOccSaving(new BigDecimal(0));
		ec1.setPowerUsed(new BigDecimal(
				20.4800000000000039790393202565610408782958984375));
		ec1.setSavedPowerUsed(new BigDecimal(
				7.679999999999996163069226895458996295928955078125));
		ec1.setTuneupSaving(new BigDecimal(
				7.679999999999996163069226895458996295928955078125));

		ec1.setFixture(fList.get(0));
		ecResult.add(ec1);

		EnergyConsumption ec2 = new EnergyConsumption();
		ec2.setAvgVolts((short) 75);
		ec2.setAmbientSaving(new BigDecimal(0));
		ec2.setAvgTemperature((short) 83);
		ec2.setBaseCost(0.0f);
		ec2.setBasePowerUsed(new BigDecimal(
				28.160000000000000142108547152020037174224853515625));
		ec2.setBrightOffset((short) 0);
		ec2.setBrightPercentage((short) 72);
		ec2.setCaptureAt(new Date()); // Note : Don't compare captureAt
		ec2.setCost(0.0f);
		ec2.setCurrState((short) 10);
		ec2.setCuStatus(0);
		ec2.setDimOffset((short) 0);
		ec2.setDimPercentage(null);
		ec2.setEnergyCalib(0);
		ec2.setEnergyCum(null);
		ec2.setEnergyTicks(3239);
		ec2.setLastTemperature((short) 33);
		ec2.setLastVolts((short) 75);
		ec2.setLightAvgLevel((short) 782);
		ec2.setLightMaxLevel((short) 802);
		ec2.setLightMinLevel((short) 742);
		ec2.setLightOff((short) 0);
		ec2.setLightOn((short) 0);
		ec2.setLightOnSeconds((short) 190);
		ec2.setMaxTemperature((short) 34);
		ec2.setMaxVolts((short) 75);
		ec2.setMinTemperature((short) 33);
		ec2.setMinVolts((short) 75);
		ec2.setMotionBits(new Long(206158430208L));
		ec2.setOccCount(null);
		ec2.setOccIn(null);
		ec2.setOccOut(null);
		ec2.setPowerCalc((short) 1);
		ec2.setPrice(0.0f);
		ec2.setSavedCost(0.0f);
		ec2.setSavingType((short) 3);
		ec2.setSysUptime(new Long(30670L));
		ec2.setZeroBucket((short) 0);
		ec2.setAmbientSaving(new BigDecimal(0));
		ec2.setBasePowerUsed(new BigDecimal(
				28.160000000000000142108547152020037174224853515625));
		ec2.setManualSaving(new BigDecimal(0));
		ec2.setOccSaving(new BigDecimal(0));
		ec2.setPowerUsed(new BigDecimal(
				20.4800000000000039790393202565610408782958984375));
		ec2.setSavedPowerUsed(new BigDecimal(
				7.679999999999996163069226895458996295928955078125));
		ec2.setTuneupSaving(new BigDecimal(
				7.679999999999996163069226895458996295928955078125));
		ec2.setFixture(fList.get(1));
		ecResult.add(ec2);

	}

	public void buildBallast() {

		Ballast ballast1 = new Ballast();
		ballast1.setBallastFactor(0.88);
		ballast1.setBallastManufacturer("OSRAM");
		ballast1.setBallastName("QTP 1x32T8/UNV DIM-TC");
		ballast1.setFixtureWattage(9);
		ballast1.setInputVoltage("120-277");
		ballast1.setItemNum(50705L);
		ballast1.setLampNum(1);
		ballast1.setLampType("T8");
		ballast1.setVoltPowerMapId(1L);
		ballast1.setWattage(32);
		ballast1.setId(1L);
		bList.add(ballast1);

		Ballast ballast2 = new Ballast();
		ballast2.setBallastFactor(0.88);
		ballast2.setBallastManufacturer("OSRAM");
		ballast2.setBallastName("QTP 1x32T8/UNV DIM-TC");
		ballast2.setFixtureWattage(9);
		ballast2.setInputVoltage("120-277");
		ballast2.setItemNum(50707L);
		ballast2.setLampNum(1);
		ballast2.setLampType("T8");
		ballast2.setVoltPowerMapId(1L);
		ballast2.setWattage(32);
		ballast2.setId(1L);
		bList.add(ballast2);

	}

	// Put the entries for the byte array
	public void buildByteArray() {
		byte[] byte1 = new byte[] { 88, 2, 0, 81, 0, 0, 23, -86, 0, -64, 95,
				-39, 75, 75, 0, 75, 75, 2, 122, 2, -40, 0, 0, 2, -67, 30, 31,
				0, 30, 30, -102, 3, 0, 14, 0, 0, 0, 0, 1, 44, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 0, 27, -70, -89, 13, 0, 0,
				0, -20, -96, 2, 121, -71, 9, 0, 0, 81, 65, -75, -88, 1, 44, 94 };

		byte[] byte2 = new byte[] { 88, 2, 0, 75, 0, 0, 0, 101, 0, -54, -125,
				-39, 75, 75, 0, 75, 75, 2, -26, 3, 34, 0, 0, 3, 14, 33, 34, 0,
				33, 33, 0, 0, 12, -89, 0, 0, 1, -29, 0, -66, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 3, 2, 0, 0, 119, -50, 10, 0, 0,
				0, 0, 0, 2, 121, -71, 9, 0, 0, 94 };

		twodim[0] = byte1;
		twodim[1] = byte2;
	}

	// Build the fixture object using ballast .
	public void buildFixture() {
		Fixture fixture1 = new Fixture();
		fixture1.setBallast(bList.get(0));
		fixture1.setBootLoaderVersion("2.3");
		fixture1.setBuildingId(1L);
		fixture1.setSecGwId(4L);
		fixture1.setSensorId("Sensor00c05f");
		fixture1.setSnapAddress("0:c0:5f");
		fixture1.setState("COMMISSIONED");
		fixture1.setType("Fixture");
		fixture1.setVersion("2.2.0 b1804");
		fixture1.setVersionSynced(0);
		fixture1.setVoltage((short) 277);

		fixture1.setWattage(0);
		fixture1.setXaxis(536);
		fixture1.setYaxis(301);
		fixture1.setId(1L);
		fixture1.setLastOccupancySeen(440);
		fixture1.setVersion("2.2.0 b1816");
		fixture1.setGroupsSyncPending(true);
		fixture1.setName("First");

		fixture1.setCommType(1);
		fixture1.setNoOfFixtures(1);
		fList.add(fixture1);

		Fixture fixture2 = new Fixture();
		fixture2.setName("Second");
		fixture2.setBallast(bList.get(1));
		fixture2.setBootLoaderVersion("2.3");
		fixture2.setBuildingId(1L);
		fixture2.setSecGwId(147L); // Done
		fixture2.setSensorId("Sensor00ca83"); // Done
		fixture2.setSnapAddress("0:ca:83"); // Done
		fixture2.setState("COMMISSIONED"); // Done
		fixture2.setType("Fixture"); // Done
		fixture2.setVersion("2.2.0 b1804");
		fixture2.setVersionSynced(0); // Done
		fixture2.setVoltage((short) 277); // Done

		fixture2.setWattage(61);
		fixture2.setXaxis(536); // Dummy not true
		fixture2.setYaxis(301); // Dummy not true
		fixture2.setId(2L);
		fixture2.setLastOccupancySeen(5);
		fixture2.setVersion("2.2.0 b1816");
		fixture2.setGroupsSyncPending(true); // Note : set it to true for
												// running test case

		fixture2.setCommType(1); // Done
		fixture2.setNoOfFixtures(1); // Done

		fList.add(fixture2);
	}

	public void initSetup() {
		buildBallast();
		buildByteArray();
		buildFixture();
		buildEnergyData();
	}

	
	@PrepareForTest({ SpringContext.class, ServerMain.class })
	public void testPMStats() {
		

		initSetup();

		// Note : set it to 10 to run the test case
		// hack for profile sync.
		twodim[0][57] = 10;
		twodim[1][57] = 10;
		// end hack

		fixtureManager = mock(FixtureManager.class);
		pricingManager = mock(PricingManager.class);
		metaDataManager = mock(MetaDataManager.class);
		EnergyConsumptionManager ecManager = mock(EnergyConsumptionManager.class);

		PowerMockito.mockStatic(SpringContext.class);
		PowerMockito.mockStatic(ServerMain.class);
		Mockito.when(spContextGlobal.getBean("fixtureManager")).thenReturn(
				fixtureManager);
		Mockito.when(spContextGlobal.getBean("metaDataManager")).thenReturn(
				metaDataManager);

		EnergyConsumption energyConsumption = mock(EnergyConsumption.class);
		EventsAndFaultManager efManager = mock(EventsAndFaultManager.class);
		DeviceServiceImpl dsManager = mock(DeviceServiceImpl.class);
		PMStatsProcessorService pmService = PowerMockito
				.spy(new PMStatsProcessorService());
		PMStatsProcessorService pmServiceMock = mock(PMStatsProcessorService.class);
		FixtureCache fixtureCache = PowerMockito.spy(new FixtureCache());
		/*ServerMain serverMain = mock(ServerMain.class);
		serverMain.setMetaDataMgr(metaDataManager);*/

		for (int counter = 0; counter == 0; counter++) {
			DeviceInfo device = new DeviceInfo(fList.get(counter));
			int x = 100;
			try {

				PowerMockito.doReturn(x).when(fixtureCache, "addDevice",
						fList.get(counter).getId(), device);

				PowerMockito.verifyPrivate(fixtureCache, times(2)).invoke(
						"addDevice", fList.get(counter).getId(), device);

			} catch (Exception e) {
				// TODO: handle exception
			}
			// Mock end FixtureCache
			// PowerMockito.spy(ServerUtil.class);

			// use PowerMockito to set up your expectation
			int y = 200;

			try {

				pmService.setPricingManager(pricingManager);
				pmService.setEventsAndFaultManager(efManager);
				pmService.setEnergyConsumptionManager(ecManager);

				Mockito.when(
						ecManager.save(Mockito.any(EnergyConsumption.class)))
						.thenAnswer(new Answer() {

							@Override
							public Object answer(InvocationOnMock invocation)
									throws Throwable {
								// TODO Auto-generated method stub

								Object args[] = invocation.getArguments();
								EnergyConsumption eConsumption = (EnergyConsumption) args[0];
								EnergyConsumption energyResult = null;

								Long id = eConsumption.getFixture().getId();

								for (Iterator iterator = ecResult.iterator(); iterator
										.hasNext();) {
									EnergyConsumption type = (EnergyConsumption) iterator
											.next();
									Long compareId = type.getFixture().getId();
									if (compareId == id) {
										System.out
												.println("----------------------Found the match--------------------------");
										energyResult = new EnergyConsumption();
										energyResult = ecResult.get(Integer
												.parseInt(id.toString()) - 1);
										//System.out.println("Printing");
										break;
									}
								}

								if (energyResult != null) {
									
									assertEquals(eConsumption.getAvgVolts(),
											energyResult.getAvgVolts());
									
									assertEquals(
											eConsumption.getAmbientSaving(),
											energyResult.getAmbientSaving());
									
									assertEquals(
											eConsumption.getAvgTemperature(),
											energyResult.getAvgTemperature());
									
									assertEquals(eConsumption.getBaseCost(),
											energyResult.getBaseCost());
									
									assertEquals(
											eConsumption.getBasePowerUsed(),
											energyResult.getBasePowerUsed());
									
									assertEquals(
											eConsumption.getBrightOffset(),
											energyResult.getBrightOffset());
									
									assertEquals(
											eConsumption.getBrightPercentage(),
											energyResult.getBrightPercentage());
									 // Note
																			// :
																			// Dont
																			// compare
																			// captureAt
									
									assertEquals(eConsumption.getCost(),
											energyResult.getCost());
									
									assertEquals(eConsumption.getCurrState(),
											energyResult.getCurrState());
									
									assertEquals(eConsumption.getCuStatus(),
											energyResult.getCuStatus());
									
									assertEquals(eConsumption.getDimOffset(),
											energyResult.getDimOffset());
									
									assertEquals(
											eConsumption.getDimPercentage(),
											energyResult.getDimPercentage());
									
									assertEquals(eConsumption.getEnergyCalib(),
											energyResult.getEnergyCalib());
									
									assertEquals(eConsumption.getEnergyCum(),
											energyResult.getEnergyCum());
									
									assertEquals(eConsumption.getEnergyTicks(),
											energyResult.getEnergyTicks());
									 // Note
																			// :
																			// Don't
																			// compare
																			// this
																			// field
									
									assertEquals(
											eConsumption.getLastTemperature(),
											energyResult.getLastTemperature());
									
									assertEquals(eConsumption.getLastVolts(),
											energyResult.getLastVolts());
									
									assertEquals(
											eConsumption.getLightAvgLevel(),
											energyResult.getLightAvgLevel());
									
									assertEquals(
											eConsumption.getLightMaxLevel(),
											energyResult.getLightMaxLevel());
									
									assertEquals(
											eConsumption.getLightMinLevel(),
											energyResult.getLightMinLevel());
									
									assertEquals(eConsumption.getLightOff(),
											energyResult.getLightOff());
									
									assertEquals(eConsumption.getLightOn(),
											energyResult.getLightOn());
									
									assertEquals(
											eConsumption.getLightOnSeconds(),
											energyResult.getLightOnSeconds());
									
									assertEquals(
											eConsumption.getMaxTemperature(),
											energyResult.getMaxTemperature());
									
									assertEquals(eConsumption.getMaxVolts(),
											energyResult.getMaxVolts());
									
									assertEquals(
											eConsumption.getMinTemperature(),
											energyResult.getMinTemperature());
									
									assertEquals(eConsumption.getMinVolts(),
											energyResult.getMinVolts());
									
									assertEquals(eConsumption.getMotionBits(),
											energyResult.getMotionBits());
									
									assertEquals(eConsumption.getOccCount(),
											energyResult.getOccCount());
									
									assertEquals(eConsumption.getOccIn(),
											energyResult.getOccIn());
									
									assertEquals(eConsumption.getOccOut(),
											energyResult.getOccOut());
									
									assertEquals(eConsumption.getPowerCalc(),
											energyResult.getPowerCalc());
									
									assertEquals(eConsumption.getPrice(),
											energyResult.getPrice());
									
									assertEquals(eConsumption.getSavedCost(),
											energyResult.getSavedCost());
									
									assertEquals(eConsumption.getSavingType(),
											energyResult.getSavingType());
									
									assertEquals(eConsumption.getSysUptime(),
											energyResult.getSysUptime());
									
									assertEquals(eConsumption.getZeroBucket(),
											energyResult.getZeroBucket());
									
									assertEquals(
											eConsumption.getAmbientSaving(),
											energyResult.getAmbientSaving());
									
									assertEquals(
											eConsumption.getBasePowerUsed(),
											energyResult.getBasePowerUsed());
									 // Note
																			// :
																			// Don't
																			// compare
																			// this
																			// field.

									
									assertEquals(
											eConsumption.getManualSaving(),
											energyResult.getManualSaving());
									
									assertEquals(eConsumption.getOccSaving(),
											energyResult.getOccSaving());
									
									assertEquals(eConsumption.getPowerUsed(),
											energyResult.getPowerUsed());
									
									assertEquals(
											eConsumption.getSavedPowerUsed(),
											energyResult.getSavedPowerUsed());
									
									assertEquals(
											eConsumption.getTuneupSaving(),
											energyResult.getTuneupSaving());
								}
								return null;
							}
						});

				Mockito.when(ballastManager.getBallastById(1L)).thenReturn(
						bList.get(counter));

				Date mDate = new Date();
				mDate.setDate(21);
				fList.get(counter).setLastStatsRcvdTime(mDate); // Note : set it
																// to the date
																// as shown to
																// run the test
																// case , it has
																// to be older
																// date

				try {
					PowerMockito.doReturn(y).when(pmService, "updateStats",
							fList.get(counter), twodim[counter], 0);
				} catch (Exception e) {

				}

				// execute your test
				System.out.println("Done");
				// Use PowerMockito.verify() to verify result
				// PowerMockito.verifyPrivate(pmService, times(2)).invoke(
				// "updateStats", fixture, array, 0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}