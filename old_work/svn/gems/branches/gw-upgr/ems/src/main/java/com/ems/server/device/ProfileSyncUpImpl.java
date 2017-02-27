package com.ems.server.device;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.cache.FixtureCache;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.Groups;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.util.ServerUtil;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;

public class ProfileSyncUpImpl {

	/*
	 * singleton object instance
	 */
	private static ProfileSyncUpImpl instance = null;

	private FixtureManager fixtureMgr = null;
	private EventsAndFaultManager eventMgr = null;
	private GroupManager groupMgr = null;
	private static Logger profileLogger = Logger.getLogger("ProfileLogger");

	private ProfileSyncUpImpl() {
		fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
		groupMgr = (GroupManager) SpringContext.getBean("groupManager");
		eventMgr = (EventsAndFaultManager) SpringContext
				.getBean("eventsAndFaultManager");
	} // end of constructor

	/*
	 * Singleton method to make sure that only one instance of the
	 * DeviceServiceImpl exists
	 */
	public static ProfileSyncUpImpl getInstance() {

		if (instance == null) {
			synchronized (ProfileSyncUpImpl.class) {
				if (instance == null) {
					instance = new ProfileSyncUpImpl();
				}
			}
		}
		return instance;

	} // end of method getInstance

	public void initiateProfileSyncActivity(Fixture oFixture,
			byte gProfChecksum, byte sProfChecksum, byte profileGroupId,
			int msgType) {
	    // Fixture cache may get invalidated between the PMstat event and actually
	    // this function been called, we need to ensure that we get the latest updated fixture object in the cache. 
	    Fixture fixture =  FixtureCache.getInstance().getDeviceFixture(oFixture.getSnapAddress());
	    long fixtureId = fixture.getId();
		
		String sEventType = EventsAndFault.FIXTURE_PROFILE_MISMATCH;
		String sMsgType = Integer.toHexString(msgType);
		if (profileLogger.isDebugEnabled()) {
			profileLogger.debug(fixtureId + ": (" + sMsgType
					+ ") profile group Id from fixture -- " + profileGroupId);
		}
		// byte bFixtureProfileGroupId =
		// DeviceServiceImpl.getInstance().getProfileGroupId(fixture);
		byte fixture_profile_no = (byte) fixtureMgr
				.getProfileNoForFixture(fixture.getId());
		if (profileGroupId != fixture_profile_no) {
			if (profileLogger.isDebugEnabled()) {
				profileLogger.debug(fixtureId + ": (" + sMsgType
						+ ") fetched groupId from fixture -- "
						+ fixture_profile_no);
			}
		}

		if (profileLogger.isDebugEnabled()) {
			profileLogger.debug(fixtureId + ": (" + sMsgType
					+ ") profile checksum from fixture -- " + sProfChecksum);
		}
		byte calcSchedPrChecksum = DeviceServiceImpl.getInstance()
				.calculateScheduledProfileChecksum(fixture);
		if (sProfChecksum != calcSchedPrChecksum) {
			if (profileLogger.isDebugEnabled()) {
				profileLogger.debug(fixtureId + ": (" + sMsgType
						+ ") calc schedvprofile checksum -- "
						+ calcSchedPrChecksum);
			}
		}
		if (profileLogger.isDebugEnabled()) {
			profileLogger.debug(fixtureId + ": (" + sMsgType
					+ ") global profile checksum from fixture -- "
					+ gProfChecksum);
		}
		byte calcGlobalPrChecksum = DeviceServiceImpl.getInstance()
				.calculateGlobalProfileChecksum(fixture);
		if (gProfChecksum != calcGlobalPrChecksum) {
			if (profileLogger.isDebugEnabled()) {
				profileLogger.debug(fixtureId + ": (" + sMsgType
						+ ") calculated global profile checksum -- "
						+ calcGlobalPrChecksum);
			}
		}
		
		Fixture fixt = fixtureMgr.getFixtureById(fixtureId);
		if(fixt.getChangeTriggerType().intValue() != fixt.getCurrentTriggerType().intValue()) {
			DeviceServiceImpl.getInstance().setTriggerType(fixt, ServerConstants.SU_CMD_HB_CONFIG_MSG_TYPE, null);
		}

		// Check for the user action first, if this is still set then we have to
		// push the profile to the SU.
		if (fixture.isPushProfile() || fixture.isPushGlobalProfile()) {
			// an alarm should be raised
			sEventType = EventsAndFault.FIXTURE_PROFILE_PUSH_USERACTION;
			eventMgr.addAlarm(fixture, "Fixture profile needs to be pushed", sEventType);
			if (profileLogger.isDebugEnabled()) {
				profileLogger.debug(fixtureId + ": (" + sMsgType
						+ ") Profile needs to be pushed to SU.");
			}
			DeviceServiceImpl.getInstance().sendCustomProfile(fixture.getId());
			DeviceServiceImpl.getInstance().setGlobalProfile(fixture.getId());
		} else {
			// User push action is NOT set, now the verification begins...
			// 1. Check Profile_no matches for the said fixture id
			Groups grps = groupMgr.getGroupById(fixt.getGroupId());

		
			// profileGroupId != 0: indicates that the SU is currently
			// associated with some group profile.
			if (profileGroupId != 0) {
				boolean bAssociationCheckRequired = true;
				// If profile group id matches with fixture associated group's
				// profile_no then next check for checksums
				if (profileGroupId == grps.getProfileNo()) {
					bAssociationCheckRequired = false;
					if ((sProfChecksum != calcSchedPrChecksum) || (gProfChecksum != calcGlobalPrChecksum)) {
						sEventType = EventsAndFault.FIXTURE_PROFILE_MISMATCH_USERACTION;
						eventMgr.addAlarm(
								fixture,
								"Fixture "
										+ fixture.getFixtureName()
										+ " - ("
										+ fixture.getVersion()
										+ ") profile mismatch. Group Id matches but checksum mismatches, needs user action!",
								sEventType);
						if (profileLogger.isInfoEnabled()) {
							profileLogger
									.info(fixtureId
											+ ": ("
											+ sMsgType
											+ ") Group Id matches but checksum mismatches, needs user action on "
											+ fixture.getFixtureName() + " - ("
											+ fixture.getVersion() + ")");
						}
						if (ServerUtil.compareVersion(fixture.getVersion(),
								ServerMain.getInstance().getGemsVersion()) != 0) {
							fixture.setVersionSynced(1);
							fixtureMgr.updateFixtureVersionSyncedState(fixture);
						}
					}
				}

				if (bAssociationCheckRequired) {
					sEventType = EventsAndFault.FIXTURE_PROFILE_MISMATCH;
					eventMgr.addAlarm(fixture, "Fixture profile mismatch", sEventType);
					if (profileLogger.isDebugEnabled()) {
						profileLogger
								.debug(fixtureId
										+ ": ("
										+ sMsgType
										+ ") Check if Profile matches any of the groups profiles.");
					}

					Long groupId = fixtureMgr.assignSUGroupProfileToFixture(
							fixtureId, profileGroupId);

					if (groupId != 0L) {
						if (profileLogger.isDebugEnabled()) {
							profileLogger.debug(fixtureId + ": (" + sMsgType
									+ ") associated with group. "
									+ groupId.longValue());
						}
					} else {
						profileLogger
								.error(fixtureId
										+ ": Profile should have matched one of the groups, "
										+ "but since it didn't we are going to download it based on matching the checksums with the fixture custom profile.");
					}
				}
			}
		}
		
	} // end of method

}
