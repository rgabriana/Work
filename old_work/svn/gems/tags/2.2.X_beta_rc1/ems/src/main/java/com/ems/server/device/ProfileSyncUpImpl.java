package com.ems.server.device;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ems.action.SpringContext;
import com.ems.cache.DeviceInfo;
import com.ems.cache.FixtureCache;
import com.ems.model.Company;
import com.ems.model.EventsAndFault;
import com.ems.model.Fixture;
import com.ems.model.FixtureCustomGroupsProfile;
import com.ems.model.Groups;
import com.ems.model.ProfileHandler;
import com.ems.server.ServerConstants;
import com.ems.server.ServerMain;
import com.ems.server.util.EmsThreadPool;
import com.ems.server.util.ServerUtil;
import com.ems.service.CompanyManager;
import com.ems.service.EmsAuditService;
import com.ems.service.EventsAndFaultManager;
import com.ems.service.FixtureManager;
import com.ems.service.GatewayManager;
import com.ems.service.GroupManager;
import com.ems.service.ProfileManager;

public class ProfileSyncUpImpl {

	/*
	 * singleton object instance
	 */
	private static ProfileSyncUpImpl instance = null;

	private FixtureManager fixtureMgr = null;
	private EventsAndFaultManager eventMgr = null;
	private ProfileManager profileMgr = null;
	private GroupManager groupMgr = null;
	private GatewayManager gwMgr = null;
	private CompanyManager companyMgr = null;
	private static Logger profileLogger = Logger.getLogger("ProfileLogger");

	private ProfileSyncUpImpl() {
		// TODO Auto-generated constructor stub

		fixtureMgr = (FixtureManager) SpringContext.getBean("fixtureManager");
		groupMgr = (GroupManager) SpringContext.getBean("groupManager");
		eventMgr = (EventsAndFaultManager) SpringContext
				.getBean("eventsAndFaultManager");
		profileMgr = (ProfileManager) SpringContext.getBean("profileManager");
		gwMgr = (GatewayManager) SpringContext.getBean("gatewayManager");
		companyMgr = (CompanyManager) SpringContext.getBean("companyManager");

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
		String sResolutionComments = "";
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

		// Check for the user action first, if this is still set then we have to
		// push the profile to the SU.
		if (fixture.isPushProfile() || fixture.isPushGlobalProfile()) {
			// an alarm should be raised
			sEventType = EventsAndFault.FIXTURE_PROFILE_PUSH_USERACTION;
			eventMgr.addAlarm(fixture, "Fixture profile needs to be pushed",
					sEventType, EventsAndFault.MAJOR_SEV_STR);
			sResolutionComments = "Profile pushed to SU";
			if (profileLogger.isDebugEnabled()) {
				profileLogger.debug(fixtureId + ": (" + sMsgType
						+ ") Profile needs to be pushed to SU.");
			}
			DeviceServiceImpl.getInstance().sendCustomProfile(fixture.getId());
			DeviceServiceImpl.getInstance().setGlobalProfile(fixture.getId());
		} else {
			// User push action is NOT set, now the verification begins...
			// 1. Check Profile_no matches for the said fixture id
			Fixture fixt = fixtureMgr.getFixtureById(fixtureId);
			Groups grps = groupMgr.getGroupById(fixt.getGroupId());

			boolean bCheckProfileChecksum = true;
			// profileGroupId != 0: indicates that the SU is currently
			// associated with some group profile.
			if (profileGroupId != 0) {
				boolean bAssociationCheckRequired = true;
				// If profile group id matches with fixture associated group's
				// profile_no then next check for checksums
				if (profileGroupId == grps.getProfileNo()) {
					bAssociationCheckRequired = false;
					bCheckProfileChecksum = false;
					if ((sProfChecksum != calcSchedPrChecksum)
							&& (gProfChecksum != calcGlobalPrChecksum)) {
						sEventType = EventsAndFault.FIXTURE_PROFILE_MISMATCH_USERACTION;
						eventMgr.addAlarm(
								fixture,
								"Fixture "
										+ fixture.getFixtureName()
										+ " - ("
										+ fixture.getVersion()
										+ ") profile mismatch. Group Id matches but checksum mismatches, needs user action!",
								sEventType, EventsAndFault.MAJOR_SEV_STR);
						if (profileLogger.isInfoEnabled()) {
							profileLogger
									.info(fixtureId
											+ ": ("
											+ sMsgType
											+ ") Group Id matches but checksum mismatches, needs user action on "
											+ fixture.getFixtureName() + " - ("
											+ fixture.getVersion() + ")");
						}
						sResolutionComments += "*** Fixture profile checksum mismatched, need user action! *** ";
						if (ServerUtil.compareVersion(fixture.getVersion(),
								ServerMain.getInstance().getGemsVersion()) != 0) {
							fixture.setVersionSynced(1);
							fixtureMgr.updateFixtureVersionSyncedState(fixture);
						}
					}
				}

				if (bAssociationCheckRequired) {
					sEventType = EventsAndFault.FIXTURE_PROFILE_MISMATCH;
					eventMgr.addAlarm(fixture, "Fixture profile mismatch",
							sEventType, EventsAndFault.MAJOR_SEV_STR);
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
						sResolutionComments = "Profile now associated with group "
								+ groupId.longValue();
						if (profileLogger.isDebugEnabled()) {
							profileLogger.debug(fixtureId + ": (" + sMsgType
									+ ") associated with group. "
									+ groupId.longValue());
						}
						bCheckProfileChecksum = false;
					} else {
						profileLogger
								.error(fixtureId
										+ ": Profile should have matched one of the groups, "
										+ "but since it didn't we are going to download it based on matching the checksums with the fixture custom profile.");
					}
				}
			}

			// If profileGroupId==0 then it is custom profile. Now check whether
			// the given profileGroupId is present in the
			// custom_fixture_profile_group table
			if (bCheckProfileChecksum) {
				if ((sProfChecksum != calcSchedPrChecksum) || (gProfChecksum != calcGlobalPrChecksum)) {
					if (profileLogger.isDebugEnabled()) {
						profileLogger
								.debug(fixtureId
										+ "*** Fixture profile checksum mismatched, needs download! *** ");
					}
					DeviceInfo device = FixtureCache.getInstance().getDevice(
							fixture);
					sResolutionComments += "*** Fixture profile checksum mismatched, needs download! *** ";
					// Just take the current fixture profile handler, this will
					// be used to create a new profile, which will
					// be overwritten by the values coming in from SU.
					ProfileHandler oFixturePFH = fixtureMgr
							.getProfileHandlerByGroupId(fixture.getGroupId());
					ProfileHandler oNewPFH = new ProfileHandler();
					oNewPFH.create();
					oNewPFH.copyFrom(oFixturePFH);

					if (profileLogger.isDebugEnabled()) {
						profileLogger.debug(fixtureId + ": (" + sMsgType
								+ ") Profile needs to be downloaded from SU.");
					}
					DeviceServiceImpl.getInstance().downloadScheduleProfile(
							fixtureId, oNewPFH, device);
					byte[] profileCSArr = oNewPFH
							.getScheduledProfileByteArray();
					if (profileCSArr != null) {
						oNewPFH.setProfileChecksum((short) ServerUtil
								.computeChecksum(profileCSArr));
					}
					sResolutionComments += "Profile downloaded from SU.";
					if (profileLogger.isDebugEnabled()) {
						profileLogger
								.debug(fixtureId
										+ ": Global Profile needs to be downloaded from SU.");
					}
					DeviceServiceImpl.getInstance().downloadGlobalProfile(
							fixtureId, oNewPFH, device);
					byte[] profileGCSArr = oNewPFH.getGlobalProfileByteArray();
					if (profileGCSArr != null) {
						oNewPFH.setGlobalProfileChecksum((short) ServerUtil
								.computeChecksum(profileGCSArr));
					}
					sResolutionComments += ", Global Profile downloaded from SU.";
					// Update the profile handler and fixture group mapping
					Groups group;
					if (profileGroupId == fixture_profile_no) {
						group = groupMgr.getGroupById(fixture.getGroupId());
						if (profileLogger.isDebugEnabled()) {
							profileLogger.debug(fixtureId
									+ ": Fixture already in custom, group => "
									+ fixture.getGroupId() + " needs update.");
						}
					} else {
						FixtureCustomGroupsProfile customFixturePFH = fixtureMgr
								.loadCustomGroupByFixureId(fixtureId);
						if (customFixturePFH != null) {
							group = groupMgr.getGroupById(customFixturePFH
									.getGroupId());
							if (profileLogger.isDebugEnabled()) {
								profileLogger.debug(fixtureId
										+ ": Fixture custom group => "
										+ customFixturePFH.getGroupId()
										+ " needs update.");
							}
						} else {
							// Create a new group as this fixture never had
							// custom profile, but the remote fixture data
							// indicates otherwise
							group = new Groups();
							group.setName(fixture.getFixtureName() + "_Custom");
							Company company = companyMgr.getCompany();
							group.setCompany(company);
							group.setProfileNo((short) profileGroupId);
							profileMgr.saveProfileHandler(oNewPFH);
							group.setProfileHandler(oNewPFH);
							groupMgr.save(group);

							if (profileLogger.isDebugEnabled()) {
								profileLogger.debug(fixtureId
										+ ": New group => " + group.getId()
										+ " selected for copy.");
							}
							if (profileLogger.isDebugEnabled()) {
								profileLogger
										.debug(fixtureId
												+ ": Saving downloaded Fixture Profile to profile Id: "
												+ oNewPFH.getId());
							}
						}
					}
					fixtureMgr.syncFixtureCustomProfile(oNewPFH, fixtureId,
							group.getId());
					oNewPFH = null;
					if (!sResolutionComments.equals("")) {
						resetProfileMisMatchEvent(fixture, sResolutionComments,
								sEventType);
					}
				}
			}
		}
	} // end of method

	private void resetProfileMisMatchEvent(Fixture fixture,
			String sResolutionComments, String sType) {
		List<EventsAndFault> oEventsAndFaultsList = eventMgr
				.getEventsAndFaultsByFixtureId(fixture.getId());
		Iterator<EventsAndFault> itr = oEventsAndFaultsList.iterator();
		while (itr.hasNext()) {
			EventsAndFault oEventsAndFault = (EventsAndFault) itr.next();
			if (oEventsAndFault != null
					&& oEventsAndFault.getEventType().equals(sType)) {
				oEventsAndFault.setActive(false);
				oEventsAndFault.setResolutionComments(sResolutionComments);
				oEventsAndFault.setResolvedOn(new Date(System
						.currentTimeMillis()));
				eventMgr.save(oEventsAndFault);
			}
		}
	}
}
