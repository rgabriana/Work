/**
 * 
 */
package com.ems.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.BACnetConfiguration;
import com.ems.model.BacnetObjectsCfg;
import com.ems.model.BacnetReportConfiguration;
import com.ems.model.BacnetReportConfigurationList;
import com.ems.server.ServerMain;
import com.ems.util.Constants;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author NileshS
 * 
 */
@Repository("bacnetConfigurationDao")
@Transactional(propagation = Propagation.REQUIRED)
public class BACnetConfigurationDao extends BaseDaoHibernate {

	public static final Logger logger = Logger.getLogger("SysLog");
	private static final Logger bacnet_logger =  Logger.getLogger("BacnetLog");
	/*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.BACnetConfigurationDao#loadAllBACnetConfig()
     */

	public List<BACnetConfiguration> loadAllBACnetConfig() {
        try {
            List<BACnetConfiguration> results = null;
            String hsql = "from BACnetConfiguration bc order by bc.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
        	logger.error("unable to loadAllBACnetConfig" , hbe);
        	throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
	
	/*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.BACnetConfigurationDao#loadAllBacnetObjectCfgs()
     */

	public List<BacnetObjectsCfg> loadAllBacnetObjectCfgs() {
        try {
            List<BacnetObjectsCfg> results = null;
            String hsql = "from BacnetObjectsCfg boc order by boc.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
        	bacnet_logger.error("unable to loadAllBacnetObjectCfgs" , hbe);
        	throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
	
	/*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.BACnetConfigurationDao#loadAllBacnetObjectCfgs()
     */

	public List<BacnetObjectsCfg> getAllBacnetObjectCfgsForUI() {
        try {
            List<BacnetObjectsCfg> results = null;
            String hsql = "from BacnetObjectsCfg boc where boc.pointkeyword in ('EMEnergyLighting','EMEnergyPlugload','EMADRlevel','EMEmergency','AreaEnergyLighting','AreaEnergyTotalPlugload','AreaEmergency','AreaOccupancy','AreaFixtureOutCount','FixtureEnergyLighting','FixtureDimLevel','FixtureOutage','PlugloadEnergyTotal','PlugloadStatus') order by boc.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
        	bacnet_logger.error("unable to getAllBacnetObjectCfgsForUI" , hbe);
        	throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
	
	
	public List<BACnetConfiguration> loadBACnetConfigForUI() {
        try {
            List<BACnetConfiguration> results = null;
            String hsql = "from BACnetConfiguration bc where bc.isallowedtoshow=true order by bc.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
        	logger.error("unable to loadAllBACnetConfig" , hbe);
        	throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.BACnetConfigurationDao#loadAllBACnetConfigMap()
     */

    public HashMap<String, String> loadAllBACnetConfigMap() {
        HashMap<String, String> oBCMap = new HashMap<String, String>();
        try {
            List<BACnetConfiguration> results = null;
            String hsql = "from BACnetConfiguration bc order by bc.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                for (BACnetConfiguration sc : results)
                	oBCMap.put(sc.getName(), sc.getValue());
            }
        } catch (HibernateException hbe) {
        	logger.error("unable to loadAllBACnetConfigMap" , hbe);
        	throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return oBCMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.BACnetConfigurationDao#loadBACnetConfigById(java.lang.Long)
     */

    public BACnetConfiguration loadBACnetConfigById(Long id) {
        try {
            List<BACnetConfiguration> results = null;
            String hsql = "from BACnetConfiguration bc where bc.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (BACnetConfiguration) results.get(0);
            }
        } catch (HibernateException hbe) {
        	logger.error("unable to loadBACnetConfigById" , hbe);
        	throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.BACnetConfigurationDao#loadBacnetObjectsCfgById(java.lang.Long)
     */

    public BacnetObjectsCfg loadBacnetObjectsCfgById(Long id) {
        try {
            List<BacnetObjectsCfg> results = null;
            String hsql = "from BacnetObjectsCfg bc where bc.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (BacnetObjectsCfg) results.get(0);
            }
        } catch (HibernateException hbe) {
        	logger.error("unable to loadBacnetObjectsCfgById" , hbe);
        	throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.BACnetConfigurationDao#loadBACnetConfigByName(java.lang.String)
     */

    public BACnetConfiguration loadBACnetConfigByName(String name) {
        try {
            List<BACnetConfiguration> results = null;
            String hsql = "from BACnetConfiguration bc where bc.name=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, name);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (BACnetConfiguration) results.get(0);
            }
        } catch (HibernateException hbe) {
        	logger.error("unable to loadBACnetConfigByName" , hbe);
        	throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }
    
    public List<BACnetConfiguration> getIntialBACnetConfig() {
       final List<BACnetConfiguration> list = new ArrayList<BACnetConfiguration>();
       Properties props = new Properties();
       String bacnetFile = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/bacnet/config/bacnet.conf";
       
       try {
    	   props.load(new FileInputStream(bacnetFile));
       } catch (Exception ex) {
    	   logger.error("Exception Occured while initail loading of bacnet.conf file",ex);
       }
       final List<String> allowedParamsToDisplay = Arrays.asList(new String[]{"AreaBaseInstance","EnergyManagerBaseInstance","ListenPort","NetworkId","VendorId"});
       if (props != null && !props.isEmpty()) {
    	   final Set<Object> keySet = props.keySet();
    	   for (final Object keyObj: keySet){
    		   final String key = (String) keyObj;
    		   BACnetConfiguration bacnetConfiguration = new BACnetConfiguration();
    		   bacnetConfiguration.setName(key);
    		   bacnetConfiguration.setValue(props.getProperty(key));
    		   if (allowedParamsToDisplay.contains(key)){
    			   bacnetConfiguration.setIsallowedtoshow(true);
    		   } else {
    			   bacnetConfiguration.setIsallowedtoshow(false); 
    		   }
    		   list.add(bacnetConfiguration);
    	   }
       } else {
           return null;
       }
       return list;
    }
    
    public List<BacnetObjectsCfg> getIntialBacnetObjectCfg() {
    	final List<BacnetObjectsCfg> list = new ArrayList<BacnetObjectsCfg>();
    	FileReader fileReader = null;
		BufferedReader bufferedReader = null;
    	try {
    		String bacnetObjectCfgFile = ServerMain.getInstance().getTomcatLocation() + "../../Enlighted/bacnet/config/bacnet_objects.cfg";
    		File file = new File(bacnetObjectCfgFile);
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if(line.startsWith("#") || line.trim().equals("")){
					continue;
				}
				String[] arrlist = line.toString().split(":");
				if(arrlist!=null && arrlist.length == 5){
					BacnetObjectsCfg bocObj = new BacnetObjectsCfg();
					bocObj.setBacnetobjecttype(arrlist[0].trim());
					bocObj.setBacnetobjectinstance(Long.parseLong(arrlist[1].trim()));
					bocObj.setBacnetobjectdescription(arrlist[2].trim());
					bocObj.setIsvalidobject(arrlist[3].trim());
					bocObj.setPointkeyword(arrlist[4].trim());
					if((arrlist[4].trim()).startsWith(Constants.BACNET_AREASUBHEADER)){
						bocObj.setBacnetpointtype(Constants.BACNET_AREASUBHEADER);
					} else if((arrlist[4].trim()).startsWith(Constants.BACNET_FIXSUBHEADER)){
						bocObj.setBacnetpointtype(Constants.BACNET_FIXSUBHEADER);
					} else if((arrlist[4].trim()).startsWith(Constants.BACNET_PLSUBHEADER)){
						bocObj.setBacnetpointtype(Constants.BACNET_PLSUBHEADER);
					} else if((arrlist[4].trim()).startsWith(Constants.BACNET_SWITCHSUBHEADER)){
						bocObj.setBacnetpointtype(Constants.BACNET_SWITCHSUBHEADER);
					} else if((arrlist[4].trim()).startsWith(Constants.BACNET_EMHEADER)){
						bocObj.setBacnetpointtype(Constants.BACNET_EMHEADER);
					}
					list.add(bocObj);
				}
			}
		} catch (Exception e) {
			bacnet_logger.error("Exception Occured while initial loading of bacnet_objects.cfg file",e);
		} finally{
			try{
				fileReader.close();
				bufferedReader.close();
			} catch (Exception e) {
				bacnet_logger.error("Exception Occured while closing fileReader",e);
			}
		}
    	
    	return list;
    }
	
	/*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.BACnetConfigurationDao#loadAllBacnetReportCfgs()
     */

	public List<BacnetReportConfiguration> loadAllBacnetReportCfgs() {
        try {
            List<BacnetReportConfiguration> results = null;
            String hsql = "from BacnetReportConfiguration brc where objectname!='unknown' order by brc.id";
            Query q = getSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return results;
            }
        } catch (HibernateException hbe) {
        	bacnet_logger.error("unable to loadAllBacnetObjectCfgs" , hbe);
        	throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

	/*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.BACnetConfigurationDao#loadAllBacnetReportCfgs()
     */

	public BacnetReportConfigurationList loadAllBacnetReportCfgs(String order,
			String orderway, Boolean bSearch, String searchField,
			String searchString, String searchOper, int offset, int limit) {

		BacnetReportConfigurationList bacnetReportConfigurations = new BacnetReportConfigurationList();

		Criteria oCriteria = null;
		Criteria oRowCount = null;

		oRowCount = sessionFactory.getCurrentSession().createCriteria(
				BacnetReportConfiguration.class, "bacnetReportConfiguration").setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(
				BacnetReportConfiguration.class, "bacnetReportConfiguration");
		oRowCount.add(Restrictions.ne("bacnetReportConfiguration.objectname", "unknown"));
		oCriteria.add(Restrictions.ne("bacnetReportConfiguration.objectname", "unknown"));
		if (bSearch) {
			if (searchField.equals("deviceid")) {
				oRowCount.add(Restrictions.like("bacnetReportConfiguration.deviceid", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.like("bacnetReportConfiguration.deviceid", "%"
						+ searchString + "%"));
			} else if (searchField.equals("objecttype")) {
				oRowCount.add(Restrictions.like("bacnetReportConfiguration.objecttype", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.like("bacnetReportConfiguration.objecttype", "%"
						+ searchString + "%"));
			} else if (searchField.equals("objectinstance")) {
				oRowCount.add(Restrictions.like("bacnetReportConfiguration.objectinstance", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.like("bacnetReportConfiguration.objectinstance", "%"
						+ searchString + "%"));
			} else if (searchField.equals("objectname")) {
				oRowCount.add(Restrictions.like("bacnetReportConfiguration.objectname", "%"
						+ searchString + "%"));
				oCriteria.add(Restrictions.like("bacnetReportConfiguration.objectname", "%"
						+ searchString + "%"));
			}
		}
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}

		if ("desc".equals(orderway)) {
			if (order.equalsIgnoreCase("deviceid")) {
				oCriteria.addOrder(Order.desc("bacnetReportConfiguration.deviceid"));
			} else if (order.equalsIgnoreCase("objecttype")) {
				oCriteria.addOrder(Order.desc("bacnetReportConfiguration.objecttype"));
			} else if (order.equalsIgnoreCase("objectinstance")) {
				oCriteria.addOrder(Order.desc("bacnetReportConfiguration.objectinstance"));
			} else if (order.equalsIgnoreCase("objectname")) {
				oCriteria.addOrder(Order.desc("bacnetReportConfiguration.objectname"));
			}
		} else {
			if (order.equalsIgnoreCase("deviceid")) {
				oCriteria.addOrder(Order.asc("bacnetReportConfiguration.deviceid"));
			} else if (order.equalsIgnoreCase("objecttype")) {
				oCriteria.addOrder(Order.asc("bacnetReportConfiguration.objecttype"));
			} else if (order.equalsIgnoreCase("objectinstance")) {
				oCriteria.addOrder(Order.asc("bacnetReportConfiguration.objectinstance"));
			} else if (order.equalsIgnoreCase("objectname")) {
				oCriteria.addOrder(Order.asc("bacnetReportConfiguration.objectname"));
			}
		}
		
		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			bacnetReportConfigurations.setTotal(count);
			bacnetReportConfigurations.setBacnetReportConfiguration(oCriteria.list());
			return bacnetReportConfigurations;
		}
		
		return bacnetReportConfigurations;
    }
	
	
	/*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.BACnetConfigurationDao#getIsAllowedToAccessBacnetPoint()
     */
	
	public Boolean getIsAllowedToAccessBacnetPoint(String pointkeyword){
		Boolean isAllowed = false;
		try {
			List<Object> list =null;
			String sql = "select brc.isvalidobject from bacnet_objects_cfg brc where brc.pointkeyword='"+pointkeyword+"'";
			Query q = getSession().createSQLQuery(sql);
			list = q.list();
	        if(list != null && list.size() > 0){
	        	if("y".equals(list.get(0).toString())){
	        		isAllowed = true;
	        	} else if("n".equals(list.get(0).toString())){
	        		isAllowed = false;
	        	}
	        }
		} catch (HibernateException e) {
			logger.error("unable to getIsAllowedToAccessBacnetPoint" , e);
        	throw SessionFactoryUtils.convertHibernateAccessException(e);
		}
		return isAllowed;
	}
	
	/*
     * (non-Javadoc)
     * 
     * @see com.ems.dao.BACnetConfigurationDao#truncateBacnetReportCfgs()
     */
	
	public void truncateBacnetReportCfgs(){
		try {
			String hsql = "delete from BacnetReportConfiguration";
			Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
			q.executeUpdate();
		} catch (HibernateException e) {
			logger.error("unable to truncateBacnetReportCfgs " , e);
        	throw SessionFactoryUtils.convertHibernateAccessException(e);
		}
	}
}
