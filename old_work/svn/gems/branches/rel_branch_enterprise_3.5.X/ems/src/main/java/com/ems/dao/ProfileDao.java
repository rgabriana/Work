package com.ems.dao;

import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.GroupECRecord;
import com.ems.model.Holiday;
import com.ems.model.Profile;
import com.ems.model.ProfileConfiguration;
import com.ems.model.ProfileHandler;
import com.ems.server.util.ServerUtil;
import com.ems.utils.DateUtil;

@Repository("profileDao")
@Transactional(propagation = Propagation.REQUIRED)
public class ProfileDao extends BaseDaoHibernate {

    static final Logger logger = Logger.getLogger("ProfileLogger");
    static final String className = ProfileDao.class.getName();

    @SuppressWarnings("unchecked")
    public List<Profile> getAllProfiles() {
        List<Profile> profiles = null;
        try {
            profiles = getSession().createQuery("from Profile").list();
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return profiles;
    }

    public void saveProfile(Profile profile) {
        try {
            getSession().saveOrUpdate(profile);
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    public void updateProfileDetails(String column, String value, String id) {
        try {
            String hql = "update Profile set " + column + " = :newValue where id = :id";
            Query query = getSession().createQuery(hql);
            if ("time_of_day".equals(column)) {
                query.setString("newValue", value);
            } else {
                query.setLong("newValue", new Long(value));
            }
            query.setLong("id", new Long(id));
            query.executeUpdate();
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    /**
     * update ProfileConfiguration details
     * 
     * @param column
     *            name of column need to update
     * @param value
     *            new value
     * @param id
     *            database id(primary key)
     */
    public void updateProfileConfigurationTime(String column, String value, String id) {
        try {
            String hql = "update ProfileConfiguration set " + column + " = :newValue where id = :id";
            Query query = getSession().createQuery(hql);
            query.setString("newValue", value);
            query.setLong("id", new Long(id));
            query.executeUpdate();
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }

    /**
     * update WeekDay details
     * 
     * @param column
     *            name of column need to update
     * @param value
     *            new value
     * @param id
     *            database id(primary key)
     */
    public void updateProfileConfigurationWeekDay(String column, String value, String id) {
        try {
            String hql = "update WeekDay set " + column + " = :newValue where id = :id";
            Query query = getSession().createQuery(hql);
            query.setString("newValue", value);
            query.setLong("id", new Long(id));
            query.executeUpdate();
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    }
    
    //TODO not required.
    /**
     * Load all holidays for given config.
     * 
     * @param configId
     *            ProfileConfiguration id.
     * @return collection of com.ems.model.Holiday
     */
    @SuppressWarnings("unchecked")
    public List<Holiday> loadHolidaysByConfigId(Long configId) {
        List<Holiday> results = null;
        try {
            String hsql = "from Holiday h where h.profileConfiguration.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, configId);
            results = q.list();
        } catch (HibernateException hbe) {
            logger.error(className, hbe.fillInStackTrace());
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return results;
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
                    .setFetchMode("override5", FetchMode.JOIN)
                    .setFetchMode("override6", FetchMode.JOIN)
                    .setFetchMode("override7", FetchMode.JOIN)
                    .setFetchMode("override8", FetchMode.JOIN)
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
                    .setFetchMode("override5", FetchMode.JOIN)
                    .setFetchMode("override6", FetchMode.JOIN)
                    .setFetchMode("override7", FetchMode.JOIN)
                    .setFetchMode("override8", FetchMode.JOIN)
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
//		int onLevel = 0;
//		 Connection con = null;
//	     CallableStatement toesUp = null;
//	        try {
//	            con = getSession().connection();
//	            con.setAutoCommit(false);
//	            toesUp = con.prepareCall("{ call getOnLightLevelForFixture ('"+ BigInteger.valueOf((fixtureId)) + ","+ dayOfWeek +"," + minOfDay +"')}");
//	            toesUp.execute();
//	            ResultSet resultSet = toesUp.getResultSet();
//	            while (resultSet.next()) {
//	               onLevel = resultSet.getInt(0);
//	            }
//	            toesUp.close();
//	            con.close();
//	        } catch (SQLException e) {
//	            e.printStackTrace();
//	        }
//            return onLevel;
            
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
	
	public void profileUpgrade(Long oldGroupId, Long newGroupId,String currentGroupName)
	{
		//Assign newGroupId to the fixtures who were associated with oldGroupId, as we have created new custom profile 		
		String hql = "update Fixture set pushProfile = :newProfile , pushGlobalProfile =:newGlobal ,groupId =:newGroup , currentProfile =:currentProfileName where groupId =:oldGrp";
		Query query = getSession().createQuery(hql);
		query.setBoolean("newProfile", true);
		query.setBoolean("newGlobal", true);
		query.setLong("newGroup", newGroupId);
		query.setLong("oldGrp", oldGroupId);		
		query.setString("currentProfileName", currentGroupName);
		query.executeUpdate();		
	}
	
	 
	

}
