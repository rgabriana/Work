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


import com.ems.model.Plugload;
import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadProfile;
import com.ems.model.PlugloadProfileConfiguration;
import com.ems.model.PlugloadProfileHandler;


@Repository("plugloadProfileDao")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadProfileDao extends BaseDaoHibernate {
	
	static final Logger logger = Logger.getLogger("Plugload"
			+ "+ProfileLogger");
    static final String className = PlugloadProfileDao.class.getName();
	
	public PlugloadProfileHandler getProfileHandlerById(Long id) {
        PlugloadProfileHandler profileHandler = null;
        try {
            profileHandler = (PlugloadProfileHandler) getSession().createCriteria(PlugloadProfileHandler.class)
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
                    .setFetchMode("plugloadProfileConfiguration", FetchMode.JOIN).add(Restrictions.idEq(id)).uniqueResult();
            System.out.println("profile handler is "+profileHandler);
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return profileHandler;
    }
	public PlugloadProfileHandler savePlugloadProfileHandler(PlugloadProfileHandler plugloadProfileHandler) {
/*        try {
           // byte[] globalProfileByteArray = profileHandler.getGlobalProfileByteArray();
            //short globalProfileChecksum = ServerUtil.computeChecksum(globalProfileByteArray);
            //profileHandler.setGlobalProfileChecksum(globalProfileChecksum);
            logger.debug("Global profile checksum: " + profileHandler.getGlobalProfileChecksum());
        } catch (Exception e) {
            logger.warn("Global profile checksum could not be calculated");
            e.printStackTrace();
        }
*/
        try {
/*            byte[] profileByteArray = profileHandler.getScheduledProfileByteArray();
            short profileChecksum = ServerUtil.computeChecksum(profileByteArray);
            profileHandler.setProfileChecksum(profileChecksum);

            logger.debug("Profile checksum: " + profileHandler.getProfileChecksum());
*/
        	
            getSession().saveOrUpdate(plugloadProfileHandler);
            return plugloadProfileHandler;
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }
	public PlugloadProfile saveProfile(PlugloadProfile profile) {
		
		getSession().saveOrUpdate(profile);
		return profile;
	}
	public PlugloadProfileConfiguration savePlugloadProfileConfiguration(
			PlugloadProfileConfiguration profileConfiguration) {
		getSession().saveOrUpdate(profileConfiguration);
		return profileConfiguration;
	}
	public PlugloadProfileHandler fetchPlugloadProfileHandlerById(Long id) {
		PlugloadProfileHandler plugloadProfileHandler = null;
        try {
            getSession().clear();
            plugloadProfileHandler = (PlugloadProfileHandler) getSession().createCriteria(PlugloadProfileHandler.class)
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
                    .setFetchMode("plugloadProfileConfiguration", FetchMode.JOIN).add(Restrictions.idEq(id)).uniqueResult();
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return plugloadProfileHandler;
	}
	
	public void changePlugloadProfile(Long plugloadId, Long groupId,
			String currentProfile, String originalProfileFrom) {
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, plugloadId);
		PlugloadGroups groups = (PlugloadGroups) session.get(PlugloadGroups.class, groupId);
		String currProfile = plugload.getCurrentProfile();
		plugload.setGroupId(groupId);
		if(!currProfile.equals(currentProfile))
		{
			plugload.setCurrentProfile(groups.getName());
			plugload.setOriginalProfileFrom(currProfile);
		}
		session.saveOrUpdate(plugload);
	}

	public int getProfileModeForPlugload(Long plugloadId, int dayOfWeek, int minOfDay) {
          
		String hsql = "SELECT  getProfileModeForPlugload ( " + BigInteger.valueOf((plugloadId)) + ","+ dayOfWeek +"," + minOfDay + ")";    
		Query q = getSession().createSQLQuery(hsql.toString());
		int onLevel = (Integer)q.uniqueResult();
		return onLevel;
		
	} //end of method getProfileModeForPlugload
	
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
	
}
