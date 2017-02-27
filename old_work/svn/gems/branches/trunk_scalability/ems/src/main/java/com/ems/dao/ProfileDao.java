package com.ems.dao;

import java.math.BigInteger;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Fixture;
import com.ems.model.Groups;
import com.ems.model.Profile;
import com.ems.model.ProfileConfiguration;
import com.ems.model.ProfileHandler;
import com.ems.model.WeekDay;
import com.ems.server.util.ServerUtil;
import com.ems.utils.ArgumentUtils;

@Repository("profileDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger("ProfileLogger");
    static final String className = ProfileDao.class.getName();

    public void saveProfile(Profile profile) {
        try {
            getSession().saveOrUpdate(profile);
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    public ProfileConfiguration saveProfileConfiguration(ProfileConfiguration profileConfiguration) {
        try {
            getSession().saveOrUpdate(profileConfiguration);
            return profileConfiguration;
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    public ProfileHandler saveProfileHandler(ProfileHandler profileHandler) {
        try {
            byte[] globalProfileByteArray = profileHandler.getGlobalProfileByteArray();
            short globalProfileChecksum = ServerUtil.computeChecksum(globalProfileByteArray);
            profileHandler.setGlobalProfileChecksum(globalProfileChecksum);
            logger.debug("Global profile checksum: " + profileHandler.getGlobalProfileChecksum());
        } catch (Exception e) {
            logger.warn("Global profile checksum could not be calculated");
            e.printStackTrace();
        }

        try {
            byte[] profileByteArray = profileHandler.getScheduledProfileByteArray();
            short profileChecksum = ServerUtil.computeChecksum(profileByteArray);
            profileHandler.setProfileChecksum(profileChecksum);

            logger.debug("Profile checksum: " + profileHandler.getProfileChecksum());

            getSession().saveOrUpdate(profileHandler);
            return profileHandler;
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    @SuppressWarnings("unchecked")
    public Long getGlobalProfileHandlerId() {
        List<Object> globalPHId = null;
        Long globalProfileHandlerId = null;
        try {
            globalPHId = getSession().createSQLQuery("select profile_handler_id from company").list();
            globalProfileHandlerId = Long.valueOf(globalPHId.get(0).toString());
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return globalProfileHandlerId;
    }

    public ProfileHandler getProfileHandlerById(Long id) {
        ProfileHandler profileHandler = null;
        try {
            profileHandler = (ProfileHandler) getSession().createCriteria(ProfileHandler.class)
                    .setFetchMode("morningProfile", FetchMode.JOIN).setFetchMode("dayProfile", FetchMode.JOIN)
                    .setFetchMode("eveningProfile", FetchMode.JOIN).setFetchMode("nightProfile", FetchMode.JOIN)
                    .setFetchMode("morningProfileWeekEnd", FetchMode.JOIN)
                    .setFetchMode("dayProfileWeekEnd", FetchMode.JOIN)
                    .setFetchMode("eveningProfileWeekEnd", FetchMode.JOIN)
                    .setFetchMode("nightProfileWeekEnd", FetchMode.JOIN)
                    .setFetchMode("morningProfileHoliday", FetchMode.JOIN)
                    .setFetchMode("dayProfileHoliday", FetchMode.JOIN)
                    .setFetchMode("eveningProfileHoliday", FetchMode.JOIN)
                    .setFetchMode("nightProfileHoliday", FetchMode.JOIN)
                    .setFetchMode("profileConfiguration", FetchMode.JOIN).add(Restrictions.idEq(id)).uniqueResult();
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return profileHandler;
    }

    public ProfileHandler fetchProfileHandlerById(Long id) {
        ProfileHandler profileHandler = null;
        try {
            getSession().clear();
            profileHandler = (ProfileHandler) getSession().createCriteria(ProfileHandler.class)
                    .setFetchMode("morningProfile", FetchMode.JOIN).setFetchMode("dayProfile", FetchMode.JOIN)
                    .setFetchMode("eveningProfile", FetchMode.JOIN).setFetchMode("nightProfile", FetchMode.JOIN)
                    .setFetchMode("morningProfileWeekEnd", FetchMode.JOIN)
                    .setFetchMode("dayProfileWeekEnd", FetchMode.JOIN)
                    .setFetchMode("eveningProfileWeekEnd", FetchMode.JOIN)
                    .setFetchMode("nightProfileWeekEnd", FetchMode.JOIN)
                    .setFetchMode("morningProfileHoliday", FetchMode.JOIN)
                    .setFetchMode("dayProfileHoliday", FetchMode.JOIN)
                    .setFetchMode("eveningProfileHoliday", FetchMode.JOIN)
                    .setFetchMode("nightProfileHoliday", FetchMode.JOIN)
                    .setFetchMode("profileConfiguration", FetchMode.JOIN).add(Restrictions.idEq(id)).uniqueResult();
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return profileHandler;
    }

    @SuppressWarnings("unchecked")
    public Long getProfileHandlerIDByGroupName(String sGroupName) {
        List<Object> results = null;
        Long lPFID = 1L;
        try {
            String hsql = "select profile_handler_id from groups where name=?";
            Query q = getSession().createSQLQuery(hsql.toString());
            q.setParameter(0, sGroupName);
            results = q.list();
            lPFID = Long.valueOf(results.get(0).toString());
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return lPFID;
    }

	public int getOnLevel(Long fixtureId, int dayOfWeek, int minOfDay) { 
        String hsql = "SELECT  getOnLightLevelForFixture ( " + BigInteger.valueOf((fixtureId)) + ","+ dayOfWeek +"," + minOfDay + ")";    
        Query q = getSession().createSQLQuery(hsql.toString());
        int onLevel = (Integer)q.uniqueResult();
        return onLevel;
	}
	
	public int getMinLevel(Long fixtureId, int dayOfWeek, int minOfDay) {      
	  String hsql = "SELECT  getMinLightLevelForFixture ( " + BigInteger.valueOf((fixtureId)) + ","+ dayOfWeek +"," + minOfDay + ")";    
	  Query q = getSession().createSQLQuery(hsql.toString());
	  int minLevel = (Integer)q.uniqueResult();
	  return minLevel;	  
	}	 
	
	public void updateProfileHandler(ProfileHandler profileHandler) {
		Session session = getSession();
		if (profileHandler.getProfileConfiguration() != null) {
			logger.debug("Config id is: "
					+ profileHandler.getProfileConfiguration().getId());
		}
		session.saveOrUpdate(profileHandler);
		logger.debug("Updated Profile Handler");
	}
	
	public ProfileHandler getProfileHandlerByFixtureId(Long fixtureId) {
		Session session = getSession();
		
		Long fixtureGroupId;
		fixtureGroupId = ((Fixture) session.get(Fixture.class,fixtureId)).getGroupId();
		
		ProfileHandler profileHandler = getProfileHandlerByGroupId(fixtureGroupId);
		
		if (profileHandler != null) {
			profileHandler.setDayProfile((Profile) getObject(Profile.class,
					profileHandler.getDayProfile().getId()));
			profileHandler.setDayProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getDayProfileHoliday()
							.getId()));
			profileHandler.setDayProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getDayProfileWeekEnd()
							.getId()));

			profileHandler.setEveningProfile((Profile) getObject(Profile.class,
					profileHandler.getEveningProfile().getId()));
			profileHandler.setEveningProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getEveningProfileHoliday()
							.getId()));
			profileHandler.setEveningProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getEveningProfileWeekEnd()
							.getId()));

			profileHandler.setMorningProfile((Profile) getObject(Profile.class,
					profileHandler.getMorningProfile().getId()));
			profileHandler.setMorningProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getMorningProfileHoliday()
							.getId()));
			profileHandler.setMorningProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getMorningProfileWeekEnd()
							.getId()));

			profileHandler.setNightProfile((Profile) getObject(Profile.class,
					profileHandler.getNightProfile().getId()));
			profileHandler.setNightProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getNightProfileHoliday()
							.getId()));
			profileHandler.setNightProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getNightProfileWeekEnd()
							.getId()));

			profileHandler
					.setProfileConfiguration((ProfileConfiguration) getObject(
							ProfileConfiguration.class, profileHandler
									.getProfileConfiguration().getId()));
		}
		return profileHandler;
	}

	public ProfileHandler getProfileHandlerByGroupId(Long groupId) {
		Session session = getSession();
		ProfileHandler profileHandler = ((Groups) session.get(Groups.class,
				groupId)).getProfileHandler();
		if (profileHandler != null) {
			profileHandler.setDayProfile((Profile) getObject(Profile.class,
					profileHandler.getDayProfile().getId()));
			profileHandler.setDayProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getDayProfileHoliday()
							.getId()));
			profileHandler.setDayProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getDayProfileWeekEnd()
							.getId()));

			profileHandler.setEveningProfile((Profile) getObject(Profile.class,
					profileHandler.getEveningProfile().getId()));
			profileHandler.setEveningProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getEveningProfileHoliday()
							.getId()));
			profileHandler.setEveningProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getEveningProfileWeekEnd()
							.getId()));

			profileHandler.setMorningProfile((Profile) getObject(Profile.class,
					profileHandler.getMorningProfile().getId()));
			profileHandler.setMorningProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getMorningProfileHoliday()
							.getId()));
			profileHandler.setMorningProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getMorningProfileWeekEnd()
							.getId()));

			profileHandler.setNightProfile((Profile) getObject(Profile.class,
					profileHandler.getNightProfile().getId()));
			profileHandler.setNightProfileHoliday((Profile) getObject(
					Profile.class, profileHandler.getNightProfileHoliday()
							.getId()));
			profileHandler.setNightProfileWeekEnd((Profile) getObject(
					Profile.class, profileHandler.getNightProfileWeekEnd()
							.getId()));

			profileHandler
					.setProfileConfiguration((ProfileConfiguration) getObject(
							ProfileConfiguration.class, profileHandler
									.getProfileConfiguration().getId()));
		}
		return profileHandler;
	}
	
	@SuppressWarnings("unchecked")
	public List<WeekDay> loadAllWeekByProfileConfigurationId(Long profileConfigurationId) {
		List<WeekDay> weekList = null;
        String hsql = "from WeekDay w where w.profileConfiguration.id=? order by w.id";
        Query q = getSession().createQuery(hsql.toString());
        q.setParameter(0, profileConfigurationId);
        weekList = q.list();
        if (!ArgumentUtils.isNullOrEmpty(weekList)) {
            return weekList;
        }
        return null;
	}
	

}
