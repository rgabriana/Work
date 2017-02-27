package com.ems.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ems.dao.EventsAndFaultDao;
import com.ems.dao.UserDao;

import com.ems.model.Device;
import com.ems.model.EventType;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.User;

import com.ems.server.ServerMain;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.StrictExpectations;

/**
 * @author mark.clark
 *
 */
public class EventsAndFaultManagerTest {

	// The following constants are provided for use where their values do not matter
	private static final String DESC = "someDescription";
	private static final String EVENT_TYPE = "someEventType";
	private static final String SEVERITY = "someSeverity";
	private static final String COMMENT = "someComment";
	private static final String EMAIL = "someEmail";
	private static final Long DEVICE_ID = 0L;
	private static final Long EVENT_ID = 1L;

	@Mocked UserDao userDao;
	@Mocked EventsAndFault eaf;
	@Mocked EventsAndFaultDao eafDao;
	@Mocked EventType et;
	@Mocked ServerMain sm;

	EventsAndFaultManager out;	// Object under test

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.out = new EventsAndFaultManager();
		// Initialize static members which would normally be initialized by Spring Framework
		Deencapsulation.setField(this.out, userDao);
		Deencapsulation.setField(this.out, eafDao);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#save(com.ems.model.EventsAndFault)}.
	 */
	@Test
	public final void testSave(@Mocked final User user) {
		final String EMAIL_ADDRESS="someEmailAddress";
		final String EMAIL_ADDRESS1="someEmailAddress1";
		final User user1 = new User();

		new StrictExpectations() {
			{
				eaf.getId();
				result=0L;
				eaf.setId((Long)withNull());
				eaf.getResolvedBy();
				result = user;
				user.getEmail();
				result=EMAIL_ADDRESS;
				eaf.getResolvedBy();
				result=user1;
				user1.getEmail();
				result=EMAIL_ADDRESS1;
				userDao.loadUserByUserName(EMAIL_ADDRESS1);
				result=user1;
				eaf.setResolvedBy(user1);
				eafDao.saveObject(eaf);
				result=eaf;
			}
		};
		EventsAndFault actualEaf = this.out.save(eaf);
		assertSame("Wrong EventsAndFault object", eaf, actualEaf);
	}

//	/**
//	 * Test method for {@link com.ems.service.EventsAndFaultManager#getEventById(java.lang.Long)}.
//	 */
//	@Test
//	@Ignore	// No logic to test
//	public final void testGetEventById() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link com.ems.service.EventsAndFaultManager#getEventsAndFaultsByFixtureId(java.lang.Long)}.
//	 */
//	@Test
//	@Ignore	// No logic to test
//	public final void testGetEventsAndFaultsByFixtureId() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link com.ems.service.EventsAndFaultManager#resolveEventsAndFaults(java.lang.Long[], com.ems.model.User, java.lang.String)}.
//	 */
//	@Test
//	@Ignore	// No logic to test
//	public final void testResolveEventsAndFaults() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link com.ems.service.EventsAndFaultManager#getEventsAndFaults(java.lang.String, java.lang.String, java.util.List, java.lang.String, int, int)}.
//	 */
//	@Test
//	@Ignore	// No logic to test
//	public final void testGetEventsAndFaults() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link com.ems.service.EventsAndFaultManager#getEventsOnFaultyFixtures(java.lang.Integer, java.util.Date, java.util.Date)}.
//	 */
//	@Test
//	@Ignore	// No logic to test
//	public final void testGetEventsOnFaultyFixtures() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link com.ems.service.EventsAndFaultManager#getFaultyFloorsByCampusAndBuilding(java.lang.Integer, java.lang.String, java.util.Date, java.util.Date)}.
//	 */
//	@Test
//	@Ignore	// No logic to test
//	public final void testGetFaultyFloorsByCampusAndBuilding() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link com.ems.service.EventsAndFaultManager#getFaultyFixtures(java.lang.Integer, java.util.Date, java.util.Date)}.
//	 */
//	@Test
//	@Ignore	// No logic to test
//	public final void testGetFaultyFixtures() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link com.ems.service.EventsAndFaultManager#getFaultyFixturesByNode(com.ems.util.OutageReportVO)}.
//	 */
//	@Test
//	@Ignore	// No logic to test
//	public final void testGetFaultyFixturesByNode() {
//		fail("Not yet implemented");
//	}
//
//	/**
//	 * Test method for {@link com.ems.service.EventsAndFaultManager#getFaultyGWByNode(com.ems.util.OutageReportVO)}.
//	 */
//	@Test
//	@Ignore	// No logic to test
//	public final void testGetFaultyGWByNode() {
//		fail("Not yet implemented");
//	}

	private void addEventAlarmCommonExpectations(final EventsAndFault event, final Device device, final String desc, final String eventType) {
		if (device != null) {
			new StrictExpectations() {
				{
					event.setDevice(device);
				}
			};
		}
		new StrictExpectations() {
			{
				event.setActive(true);
				event.setDescription(desc);
				event.setEventTime((Date)any);
				event.setEventType(eventType);
				eafDao.saveOrUpdateEvent(event);
			}
		};
	}

	private void addEventCommonExpectations(final Device device, final String desc, final String eventType) {
		new StrictExpectations() {
			@Mocked EventsAndFault eafLocal;
			{
				eafLocal = new EventsAndFault();
				eafLocal.setSeverity(EventsAndFault.INFO_SEV_STR);
				addEventAlarmCommonExpectations(eafLocal, device, desc, eventType);
			}
		};
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addEvent(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddEventStringString() {
		addEventCommonExpectations(null, DESC, EVENT_TYPE);
		this.out.addEvent(DESC, EVENT_TYPE);
	}

	private boolean alarmExistsAndUpdateExpectations(final List<EventsAndFault> faultList, final String desc, final String eventType, final String severity) {
		final int TIMES = faultList.size();
		if (TIMES > 0) {
			new StrictExpectations() {
				{
					eaf.getEventType(); times=TIMES;
					result=EVENT_TYPE;
					eaf.getSeverity(); times=TIMES;
					result=SEVERITY;
				}
			};
			if (!SEVERITY.equals(severity)) {
				new StrictExpectations() {
					{
						eaf.setSeverity(severity);
					}
				};
			}
			new StrictExpectations() {
				{
					eaf.setEventTime((Date)any); times=TIMES;
					eaf.getEventValue(); times=TIMES;
					result = 1l;
					eaf.setEventValue(2L); times=TIMES;
					eaf.setDescription(desc);
					eafDao.saveOrUpdateEvent(eaf); times=TIMES;
				}
			};
			return true;
		}
		return false;
	}

	private void addAlarmCommonExpectations(final Device device, final String desc, final String eventType, final String severity) {
		final List<EventsAndFault> FAULT_LIST = new ArrayList<EventsAndFault>();
		FAULT_LIST.add(eaf);

		if (device==null) {
			new StrictExpectations() {
				{
					eafDao.getEventsAndFaultsByEventTypeNoDevice(eventType);
					result =  FAULT_LIST;
				}
			};
		} else {
			new StrictExpectations() {
				{
					device.getId();
					result=DEVICE_ID;
					eafDao.getEventsAndFaultsByDeviceId(DEVICE_ID);
					result = FAULT_LIST;
				}
			};
		}
		String localSeverity = severity;
		if (severity == null) {
			new StrictExpectations() {
				{
					ServerMain.getInstance();
					result=sm;
					sm.getEventType(eventType);
					result=et;
					et.getSeverityString();
					result=SEVERITY;
				}
			};
			localSeverity = SEVERITY;
		}

		if (alarmExistsAndUpdateExpectations(FAULT_LIST, desc, eventType, localSeverity)) {
			return;
		}

		new StrictExpectations() {
			@Mocked EventsAndFault eafNew;
			{
				eafNew = new EventsAndFault();
				result = eafNew;
				eafNew.setSeverity(severity);
				addEventAlarmCommonExpectations(eafNew, device, desc, eventType);
			}
		};
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addAlarm(com.ems.model.Device, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddAlarmDeviceStringStringString(@Mocked final Device device) {
		addAlarmCommonExpectations(device, DESC, EVENT_TYPE, SEVERITY);
		this.out.addAlarm(device, DESC, EVENT_TYPE, SEVERITY);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addAlarm(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddAlarmStringStringString() {
		addAlarmCommonExpectations(null, DESC, EVENT_TYPE, SEVERITY);
		this.out.addAlarm(DESC, EVENT_TYPE, SEVERITY);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addAlarm(com.ems.model.Device, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddAlarmDeviceStringString(@Mocked final Device device) {
		addAlarmCommonExpectations(device, DESC, EVENT_TYPE, null);
		this.out.addAlarm(device, DESC, EVENT_TYPE);
	}

//	/**
//	 * Test method for {@link com.ems.service.EventsAndFaultManager#clearAlarm(com.ems.model.Device, java.lang.String)}.
//	 */
//	@Test
//	@Ignore	// No logic to test
//	public final void testClearAlarm() {
//		fail("Not yet implemented");
//	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addAlarm(com.ems.model.Fixture, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddAlarmFixtureStringString(@Mocked final Fixture fixture) {
		addAlarmCommonExpectations(fixture, DESC, EVENT_TYPE, null);
		this.out.addAlarm(fixture, DESC, EVENT_TYPE);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addAlarm(com.ems.model.Fixture, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddAlarmFixtureStringStringFiltered(@Mocked final Fixture fixture) {
		new StrictExpectations() {
			{
				// ensure that it does not save or update event
				eafDao.saveOrUpdateEvent(eaf); times=0;
			}
		};
		this.out.addAlarm(fixture, DESC, EventsAndFault.FIXTURE_PROFILE_MISMATCH);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addUpdateAlarm(com.ems.model.Fixture, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddUpdateAlarmUpdate(@Mocked final Fixture fixture) {
		final List<EventsAndFault> FAULT_LIST = new ArrayList<EventsAndFault>();
		FAULT_LIST.add(eaf);

		new StrictExpectations() {
			{
				fixture.getId();
				result=DEVICE_ID;
				eafDao.getEventsAndFaultsByFixtureId(DEVICE_ID, EVENT_TYPE);
				result=FAULT_LIST;
				ServerMain.getInstance();
				result=sm;
				sm.getEventType(EVENT_TYPE);
				result=et;
				et.getSeverityString();
				result=SEVERITY;
				eaf.setSeverity(SEVERITY);
				eaf.setEventTime((Date)any);
				eaf.setDescription(DESC);
				eafDao.saveOrUpdateEvent(eaf);
			}
		};
		this.out.addUpdateAlarm(fixture, DESC, EVENT_TYPE);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addUpdateAlarm(com.ems.model.Fixture, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddUpdateAlarmAdd(@Mocked final Fixture fixture) {
		final List<EventsAndFault> FAULT_LIST = new ArrayList<EventsAndFault>();

		new StrictExpectations() {
			{
				fixture.getId();
				result=DEVICE_ID;
				eafDao.getEventsAndFaultsByFixtureId(DEVICE_ID, EVENT_TYPE);
				result=FAULT_LIST;
				ServerMain.getInstance();
				result=sm;
				sm.getEventType(EVENT_TYPE);
				result=et;
				et.getSeverityString();
				result=SEVERITY;
				new EventsAndFault();
				result=eaf;
				eaf.setDevice(fixture);
				eaf.setActive(true);
				eaf.setDescription(DESC);
				eaf.setEventTime((Date)any);
				eaf.setEventType(EVENT_TYPE);
				eaf.setSeverity(SEVERITY);
				eafDao.saveOrUpdateEvent(eaf);
			}
		};
		this.out.addUpdateAlarm(fixture, DESC, EVENT_TYPE);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addUpdateSingleAlarm(java.lang.String, java.lang.String, boolean)}.
	 */
	@Test
	public final void testAddUpdateSingleAlarmUpdate() {
		final List<EventsAndFault> FAULT_LIST = new ArrayList<EventsAndFault>();
		FAULT_LIST.add(eaf);
		final String ERR_IN = "someErrorString";

		new StrictExpectations() {
			{
				eafDao.getEventsAndFaultsByEventType(EVENT_TYPE);
				result=FAULT_LIST;
				ServerMain.getInstance();
				result=sm;
				sm.getEventType(EVENT_TYPE);
				result=et;
				et.getSeverityString();
				result=SEVERITY;
				eaf.setActive(true);
				eaf.getEventValue();
				result=1L;
				eaf.setEventValue(2L);
				eaf.getEventValue();
				result=2;
				eaf.setDescription(anyString);
				eaf.setEventTime((Date)any);
				eaf.setEventType(EVENT_TYPE);
				eaf.setSeverity(SEVERITY);
				eafDao.saveOrUpdateEvent(eaf);
			}
		};
		this.out.addUpdateSingleAlarm(ERR_IN, EVENT_TYPE, true);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addUpdateSingleAlarm(java.lang.String, java.lang.String, boolean)}.
	 */
	@Test
	public final void testAddUpdateSingleAlarmAdd() {
		final List<EventsAndFault> FAULT_LIST = new ArrayList<EventsAndFault>();
		final String ERR_IN = "someErrorString";

		new StrictExpectations() {
			{
				eafDao.getEventsAndFaultsByEventType(EVENT_TYPE);
				result=FAULT_LIST;
				ServerMain.getInstance();
				result=sm;
				sm.getEventType(EVENT_TYPE);
				result=et;
				et.getSeverityString();
				result=SEVERITY;
				new EventsAndFault();
				result=eaf;
				eaf.setActive(true);
				eaf.setEventValue(1L);
				eaf.getEventValue();
				result=1;
				eaf.setDescription(anyString);
				eaf.setEventTime((Date)any);
				eaf.setEventType(EVENT_TYPE);
				eaf.setSeverity(SEVERITY);
				eafDao.saveOrUpdateEvent(eaf);
			}
		};
		this.out.addUpdateSingleAlarm(ERR_IN, EVENT_TYPE, true);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addEvent(com.ems.model.Device, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddEventDeviceStringString(@Mocked final Device device) {
		addEventCommonExpectations(device, DESC, EVENT_TYPE);
		this.out.addEvent(device, DESC, EVENT_TYPE);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#addEvent(com.ems.model.Gateway, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddEventGatewayStringString(@Mocked final Gateway gateway) {
		addEventCommonExpectations(gateway, DESC, EVENT_TYPE);
		this.out.addEvent(gateway, DESC, EVENT_TYPE);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#updateEvent(java.lang.Long, java.lang.String, com.ems.model.User, java.lang.Boolean)}.
	 */
	@Test
	public final void testUpdateEvent(@Mocked final User user) {
		final String PREV_COMMENTS = "someOtherComments";
		new StrictExpectations() {
			{
				eafDao.getEventById(EVENT_ID);
				result=eaf;
				eaf.getActive();
				result=true;
				eaf.getResolutionComments();
				result= PREV_COMMENTS;
				user.getEmail();
				result=EMAIL;
				eaf.setResolutionComments(anyString);
				eafDao.saveObject(eaf);
			}
		};
		String actualResult = this.out.updateEvent(EVENT_ID, COMMENT, user, true);
		assertEquals("Wrong result", "S", actualResult);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#updateEvent(java.lang.Long, java.lang.String, com.ems.model.User, java.lang.Boolean)}.
	 */
	@Test
	public final void testUpdateEventNoEvent(@Mocked final User user) {
		new StrictExpectations() {
			{
				eafDao.getEventById(EVENT_ID);
				result=null;
			}
		};
		String actualResult = this.out.updateEvent(EVENT_ID, COMMENT, user, true);
		assertEquals("Wrong result", "R", actualResult);
	}

	/**
	 * Test method for {@link com.ems.service.EventsAndFaultManager#updateEvent(java.lang.Long, java.lang.String, com.ems.model.User, java.lang.Boolean)}.
	 */
	@Test
	public final void testUpdateEventNotActive(@Mocked final User user) {
		new StrictExpectations() {
			{
				eafDao.getEventById(EVENT_ID);
				result=eaf;
				eaf.getActive();
				result=true;
				eaf.getId();
				result=EVENT_ID;
				eafDao.resolveEventsAndFaults((Long[])any, user, COMMENT);
			}
		};
		String actualResult = this.out.updateEvent(EVENT_ID, COMMENT, user, false);
		assertEquals("Wrong result", "R", actualResult);
	}
}
