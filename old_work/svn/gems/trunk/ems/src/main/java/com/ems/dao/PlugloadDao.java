package com.ems.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Device;
import com.ems.model.Fixture;
import com.ems.model.Gateway;
import com.ems.model.Plugload;
import com.ems.model.PlugloadGroups;
import com.ems.model.PlugloadProfile;
import com.ems.model.PlugloadProfileConfiguration;
import com.ems.model.PlugloadProfileHandler;
import com.ems.server.ServerConstants;
import com.ems.types.FacilityType;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.model.PlugloadList;

@Repository("plugloadDao")
@Transactional(propagation = Propagation.REQUIRED)
public class PlugloadDao extends BaseDaoHibernate {
	
	static final Logger logger = Logger.getLogger("PlugloadLogger");

	public Plugload saveOrUpdatePlugload(Plugload plugload) {
		getSession().saveOrUpdate(plugload);
		return plugload;
	}
	
	public Plugload update(Plugload plugload) {
		Session session = getSession();
		session.saveOrUpdate(plugload);
		return plugload;
	}
	
	/**
	 * update Gateway Id for all plugload in case of GW RMA
	 *
	 * @param fromGatewayId
	 *            Old Gateway Id
	 * @param toGatewayId
	 *            New Gateway Id
	 * @return 
	 */
	public void updateGatewayId(Long fromGatewayId, Long toGatewayId){
		try {
			Query query = getSession().createQuery("Update Plugload set " + "gateway.id = :newGWId "
					+ " where gateway.id = :oldGWId");			
			
			query.setLong("newGWId", toGatewayId);
			query.setLong("oldGWId", fromGatewayId);
			query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Load plugload details.load all plugloads of given floor
	 *
	 * @param id
	 *            floor id
	 * @return com.ems.model.Plugload collection load only id,plugloadid,floor
	 *         id,area id,subArea id, x axis,y axis details of plugload other
	 *         details loads as null.
	 */
	@SuppressWarnings("unchecked")
	public List<Plugload> loadPlacedPlugloadsByFloorId(Long id) {
		
		Session session = getSession();
		List<Plugload> plugloadList = session.createCriteria(Plugload.class).createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.eq("state", 	ServerConstants.PLUGLOAD_STATE_PLACED_STR))
				.add(Restrictions.eq("floor.id", id)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
			return plugloadList;
		} else {
			return null;
		}
		
	} //end of method loadPlacedPlugloadsByFloorId
	
	/**
	 * Load plugload by state
	 * @param String
	 *            plugload state
	 * @return lugload list
	 */
	@SuppressWarnings("unchecked")
	public List<Plugload> loadPlugloadsByState(String state) {
		
		Session session = getSession();
		List<Plugload> plugloadList = session.createCriteria(Plugload.class).add(Restrictions.eq("state", state))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
			return plugloadList;
		} else {
			return null;
		}
		
	} //end of method loadPlugloadsByState
		
	public Plugload getPlugloadById(Long id) {
	//	System.out.println("invoking plugload dao method");
		Session session = getSession();
		return (Plugload) session.createCriteria(Plugload.class)
				.add(Restrictions.eq("id", id))	.uniqueResult();		
		
	}
	
	
	public Plugload getPlugloadBySnapAddress(String snapAddr) {
		Session session = getSession();
		return (Plugload) session.createCriteria(Plugload.class)
				.add(Restrictions.eq("snapAddress", snapAddr)).uniqueResult();		
	}
	
	public Long loadAllCommissionedPlugloadsCount() {
		List results = getSession()
		.createCriteria(Plugload.class)
		.setProjection(Projections.rowCount())
		.add(Restrictions.eq("state", 	ServerConstants.PLUGLOAD_STATE_COMMISSIONED_STR))
		.list();
		Long count = (Long)results.get(0);		
		if(count==null) count = 0l;
		return count;		
	} 
	
	public void resetPushProfileForPlugload(Long plugloadId) {
		
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, plugloadId);
		plugload.setPushProfile(false);
		
	} //end of method resetPushProfileForPlugload

	public void resetPushGlobalProfileForPlugload(Long plugloadId) {
		
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, plugloadId);
		plugload.setPushGlobalProfile(false);
		
	} //end of method resetPushGlobalProfileForPlugload

	@SuppressWarnings("unchecked")
	public List<Plugload> loadAllPlugloads() {
		Session session = getSession();
		List<Plugload> plugloadList = session
				.createCriteria(Plugload.class)					
				.add(Restrictions.ne("state",
						ServerConstants.PLUGLOAD_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.PLUGLOAD_STATE_PLACED_STR))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		//System.out.println("plugload list in dao is "+plugloadList.size());
		//System.out.println("name is "+plugloadList.get(0).getGateway().getName());
		loadPlugloadList();
		if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
			return plugloadList;
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Plugload> loadAllCommissionedPlugloads() {
		Session session = getSession();
		List<Plugload> plugloadList = session
				.createCriteria(Plugload.class)					
				.add(Restrictions.eq("state",
						ServerConstants.PLUGLOAD_STATE_COMMISSIONED_STR))				
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		//System.out.println("plugload list in dao is "+plugloadList.size());
		//System.out.println("name is "+plugloadList.get(0).getGateway().getName());
		loadPlugloadList();
		if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
			return plugloadList;
		} else {
			return null;
		}
	}
	
	/**
	 * Load plugload details.load all plugloads of given group
	 *
	 * @param id
	 *            group id
	 * @return com.ems.model.Plugload collection load only id,plugloadId,floor
	 *         id,area id,subArea id, x axis,y axis, group id details of plugload
	 *         other details loads as null.
	 */
	@SuppressWarnings("unchecked")
	public List<Plugload> loadPlugloadByPlugloadGroupId(Long id) {
		Session session = getSession();
		List<Plugload> plugloadList = session
				.createCriteria(Plugload.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.PLUGLOAD_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.PLUGLOAD_STATE_PLACED_STR))
				.add(Restrictions.eq("groupId", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
			return plugloadList;
		} else {
			return null;
		}
	}
		/**
	 * Loads all the plugloads belonging to an floor
	 *
	 * @param id
	 * @return com.ems.model.Plugload collection
	 */
	@SuppressWarnings("unchecked")
	public List<Plugload> loadPlugloadByFloorId(Long id) {
		Session session = getSession();
		List<Plugload> plugloadList = session
				.createCriteria(Plugload.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.PLUGLOAD_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.PLUGLOAD_STATE_PLACED_STR))
				.add(Restrictions.eq("floor.id", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
			return plugloadList;
		} else {
			return null;
		}
	}

	
	@SuppressWarnings("unused")
	public List<Plugload> loadPlugloadByProfileTemplateId(Long id) {
		List<Plugload> plugloadList= new ArrayList<Plugload>();
		List<Object> tempPlugloadList=null;
		try {
		    Session s = getSession();
		    String hsql = "select pl.id, pl.name, pl.macAddress, pl.version, pl.gateway.id, pl.gateway.name, pl.location, pl.state, pl.isHopper,pl.lastConnectivityAt from Plugload as pl, PlugloadGroups as plg where pl.state!='DELETED' and pl.state!='PLACED' and pl.groupId=plg.id and plg.plugloadProfileTemplate.id=:plugloadProfileTemplateId";
		    Query query = s.createQuery(hsql);
			query.setParameter("plugloadProfileTemplateId",id);
			tempPlugloadList = query.list();
			
			if (tempPlugloadList != null && !tempPlugloadList.isEmpty())
		    {
				Iterator<Object> it = tempPlugloadList.iterator();
	              while (it.hasNext()) {
	            	  Object[] rowResult = (Object[]) it.next();
	            	  Plugload plugload = new Plugload();
	            	  plugload.setId((Long)rowResult[0]);
	            	  plugload.setName((String)rowResult[1]);
	            	  Gateway gateway = new Gateway();
	            	  gateway.setGatewayName((String)rowResult[5]);
	            	  gateway.setId((Long)rowResult[4]);
	            	  plugload.setGateway(gateway);
	            	  plugload.setVersion((String)rowResult[3]);
	            	  plugload.setMacAddress((String)(rowResult[2]));
	            	  plugload.setLocation((String)rowResult[6]);
	            	  plugload.setState((String)rowResult[7]);
	            	  plugload.setIsHopper((Integer)rowResult[8]);
	            	  plugload.setLastConnectivityAt((Date)rowResult[9]);
	            	  plugloadList.add(plugload);
	              }
	        }
			return plugloadList;
		  } catch (Exception e) {
		    e.printStackTrace();
		  }
		  return null;
	}
	/**
	 * Loads all the plugloads belonging to an area
	 *
	 * @param id
	 * @return com.ems.model.Plugload collection
	 */
	@SuppressWarnings("unchecked")
	public List<Plugload> loadPlugloadByAreaId(Long id) {
		Session session = getSession();
		List<Plugload> plugloadList = session
				.createCriteria(Plugload.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.PLUGLOAD_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.PLUGLOAD_STATE_PLACED_STR))
				.add(Restrictions.eq("area.id", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
			return plugloadList;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Plugload> loadPlugloadByState(String state) {
		
				Session session = getSession();
			List<Plugload> plugloadList = session
					.createCriteria(Plugload.class)
					.add(Restrictions.eq("state",
							state)).list();/*
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();*/
			
			if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
				return plugloadList;
			} else {
				return null;
			}	
	}
	
	public void updatePlugloads(List<Plugload> plugloads) {

		Session session = getSession();
		for (Plugload plugload : plugloads) {
			session.saveOrUpdate(plugload);
		}
	}
	
	public void updateProfileHandler(PlugloadProfileHandler plugloadProfileHandler) {
		Session session = getSession();
		if (plugloadProfileHandler.getPlugloadProfileConfiguration() != null) {
			logger.debug("Config id is: "
					+ plugloadProfileHandler.getPlugloadProfileConfiguration().getId());
		}
		session.saveOrUpdate(plugloadProfileHandler);
		logger.debug("Updated PlugloadProfile Handler");
	}
	
	@SuppressWarnings("unchecked")
	public PlugloadList loadPlugloadList() {
		PlugloadList plugloadList = new PlugloadList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;		
		/*if (property != null && pid != null) {*/
			oRowCount = getSession()
					.createCriteria(Plugload.class, "pg")
					.add(Restrictions.ne("pg.state",
							ServerConstants.PLUGLOAD_STATE_DELETED_STR))
					.add(Restrictions.ne("pg.state",
							ServerConstants.PLUGLOAD_STATE_PLACED_STR))
					.createAlias("gateway", "gw",
							CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("gw", FetchMode.JOIN)
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
					.setProjection(Projections.rowCount());
			
			oCriteria = getSession()
					.createCriteria(Plugload.class, "pg")
					.add(Restrictions.ne("pg.state",
							ServerConstants.PLUGLOAD_STATE_DELETED_STR))
					.add(Restrictions.ne("pg.state",
							ServerConstants.PLUGLOAD_STATE_PLACED_STR))
					.createAlias("gateway", "gw",
							CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("gw", FetchMode.JOIN)
					.setProjection(Projections.projectionList().add(Projections.property("pg.id"), "id")
					.add(Projections.property("pg.snapAddress"), "snapAddress")
					.add(Projections.property("pg.state"), "state")
					.add(Projections.property("pg.lastConnectivityAt"), "lastConnectivityAt")
					.add(Projections.property("pg.name"), "name")
					.add(Projections.property("pg.isHopper"), "isHopper")
					.add(Projections.property("pg.currApp"), "currApp")
                    .add(Projections.property("pg.version"), "version")
                    .add(Projections.property("pg.firmwareVersion"), "firmwareVersion")
					.add(Projections.property("pg.currentProfile"), "currentProfile")
					.add(Projections.property("pg.groupId"), "groupId")
					.add(Projections.property("pg.upgradeStatus"), "upgradeStatus")					
					.add(Projections.property("gw.name"), "gatewayNameForFilter")
					.add(Projections.property("gw.id"), "gatewayIdForFilter")					
					).add(Restrictions.ne("pg.state", ServerConstants.FIXTURE_STATE_DELETED_STR))					
					.setResultTransformer(Transformers.aliasToBean(Plugload.class));
			
			List<Object> output = (List<Object>) oRowCount.list();
			Long count = (Long) output.get(0);
			//System.out.println("count is "+count);
			if (count.compareTo(new Long("0")) > 0) {
				plugloadList.setTotal(count);
				plugloadList.setPlugload(oCriteria.list());
			}
		/*}*/
		return plugloadList;
	}
	
	public void updatePlugloadProfileHandler(PlugloadProfileHandler profileHandler) {
		Session session = getSession();
		if (profileHandler.getPlugloadProfileConfiguration() != null) {
			logger.debug("Config id is: "
					+ profileHandler.getPlugloadProfileConfiguration().getId());
		}
		session.saveOrUpdate(profileHandler);
		logger.debug("Updated PlugoadProfile Handler");
	}
	
	public void changePlugloadProfile(Long plugloadId, Long plugloadGroupId,
			Long globalPFId, String currentPlugloadProfile, String originalPlugloadProfileFrom) {
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, plugloadId);
		PlugloadGroups plugloadGroups = (PlugloadGroups) session.get(PlugloadGroups.class, plugloadGroupId);
		String currProfile = plugload.getCurrentProfile();
		plugload.setGroupId(plugloadGroupId);
		if(currProfile!=null && !currProfile.equals(currentPlugloadProfile))
		{
			plugload.setCurrentProfile(plugloadGroups.getName());
			plugload.setOriginalProfileFrom(currProfile);
		}
	}
	
	public void updatePlugloadProfileHandlerIdForPlugload(Long plugloadId,
			Long profileHandlerId, String currentPlugloadProfile,
			String originalPlugloadProfileFrom) {
		Session s = getSession();
		Plugload plugload = (Plugload) s.get(Plugload.class, plugloadId);
		
		PlugloadGroups plugloadGroups = ((PlugloadGroups) s.get(PlugloadGroups.class,plugload.getGroupId()));
		plugloadGroups.setPlugloadProfileHandler((PlugloadProfileHandler) s.load(PlugloadProfileHandler.class,	profileHandlerId));
		
		plugload.setCurrentProfile(currentPlugloadProfile);
		plugload.setOriginalProfileFrom(originalPlugloadProfileFrom);

	}

	public Plugload updatePosition(Long plugloadId, Integer x, Integer y,
			String state) {
		Session session = getSession();
		Plugload plugload = (Plugload) session.load(Plugload.class, plugloadId);
		plugload.setXaxis(x);
		plugload.setYaxis(y);
		return plugload;
	}	
	/**
	 * get the plugload by snap address
	 *
	 * @param snapAddr
	 * @return the plugload by snap address
	 */
	public Plugload getDeletedPlugloadBySnapAddr(String snapAddr) {
		
		Session session = getSession();
		Plugload plugload = (Plugload) session.createCriteria(Plugload.class)				
				.add(Restrictions.ne("state", ServerConstants.PLUGLOAD_STATE_DELETED_STR))
				.add(Restrictions.eq("snapAddress", snapAddr)).uniqueResult();
		return plugload;

	} // end of method getDeletedPlugloadBySnapAddr
	
	public List<Plugload> getUnCommissionedPlugloadList(long gatewayId) {
		
    Session session = getSession();
    List<Plugload> plugloadList = session.createCriteria(Plugload.class)
    		.add(Restrictions.ne("state", ServerConstants.PLUGLOAD_STATE_DELETED_STR))
    		.add(Restrictions.ne("state", ServerConstants.PLUGLOAD_STATE_COMMISSIONED_STR))
    		.add(Restrictions.eq("gateway.id", gatewayId)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
    		.list();
    return plugloadList;
    
	} //end of method getUnCommissionedPlugloadList
	
	public void updateCommissionStatus(int[] plIds, int status) {
		
		try {
			String queryStr = "update Plugload set commission_status = :commission_status where id in (";
				int noOfPlugloads = plIds.length;
			for (int i = 0; i < noOfPlugloads; i++) {
				if (i > 0) {
					queryStr += ", ";
				}
				queryStr += "" + plIds[i];
			}
			queryStr += ")";

			SQLQuery query = getSession().createSQLQuery(queryStr);
			query.setInteger("commission_status", status);
			query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	} //end of method updateCommissionStatus
	
	/**
	 * return list of Plugload object associated with gateway
	 *
	 * @param gatewayId
	 * @return list of Plugload object associated with gateway
	 */
	@SuppressWarnings("unchecked")
	public List<Plugload> loadAllPlugloadsByGatewayId(Long gatewayId) {
		
		Session session = getSession();
		List<Plugload> plugloadList = session.createCriteria(Plugload.class)
				.add(Restrictions.ne("state", ServerConstants.PLUGLOAD_STATE_DELETED_STR))
				.add(Restrictions.ne("state", ServerConstants.PLUGLOAD_STATE_PLACED_STR))
				.add(Restrictions.eq("gateway.id", gatewayId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
			return plugloadList;
		} else {
			return null;
		}
		
	} //end of method loadAllPlugloadsByGatewayId
	
	public void setImageUpgradeStatus(long plugloadId, String status) {
		
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, plugloadId);
		plugload.setUpgradeStatus(status);

	} // end of method setImageUpgradeStatus
	
	public void updateFirmwareVersion(String version, long id, long gwId) {
		
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, id);
		plugload.setLastConnectivityAt(new java.util.Date());
		plugload.setVersion(version);
		plugload.setCurrApp((short) 1);
		plugload.setSecGwId(gwId);
		
	} // end of method updateFirmwareVersion
	
	public void updateVersion(String version, long id, long gwId) {

		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, id);
		plugload.setLastConnectivityAt(new java.util.Date());
		plugload.setVersion(version);
		plugload.setCurrApp((short) 2);
		plugload.setSecGwId(gwId);

	} // end of method updateVersion
	
	public void updatePlugloadVersionSyncedState(Plugload plugload) {
	
		Session session = getSession();
		Plugload dbpl = (Plugload) session.get(Plugload.class, plugload.getId());
		dbpl.setVersionSynced(plugload.getVersionSynced());
		
	} // end of method updatePlugloadVersionSyncedState

	@SuppressWarnings("unchecked")
	public PlugloadList loadPlugloadListWithSpecificAttrs(String property, Long pid, String order,
			String orderWay, Boolean bSearch, String searchField,
			String searchString, String searchOper, int offset, int limit) {
		PlugloadList plugloadList = new PlugloadList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;
		
		String statusOrder1 = null;
		String statusOrder2 = null;
		String statusOrder3 = null;

		FacilityType orgType = FacilityType.valueOf(property.toUpperCase());
		if (property != null && pid != null) {
			oRowCount = getSession()
					.createCriteria(Plugload.class, "pg")
					.add(Restrictions.ne("pg.state",
							ServerConstants.PLUGLOAD_STATE_DELETED_STR))					
					.createAlias("gateway", "gw",
							CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("gw", FetchMode.JOIN)
					.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
					.setProjection(Projections.rowCount());

			oCriteria = getSession()
					.createCriteria(Plugload.class, "pg")
					.add(Restrictions.ne("pg.state",
							ServerConstants.PLUGLOAD_STATE_DELETED_STR))					
					.createAlias("gateway", "gw",
							CriteriaSpecification.LEFT_JOIN)
					.setFetchMode("gw", FetchMode.JOIN)
					.setProjection(Projections.projectionList().add(Projections.property("pg.id"), "id")
					.add(Projections.property("pg.snapAddress"), "snapAddress")
					.add(Projections.property("pg.state"), "state")
					.add(Projections.property("pg.lastConnectivityAt"), "lastConnectivityAt")
					.add(Projections.property("pg.name"), "name")
					.add(Projections.property("pg.isHopper"), "isHopper")
					.add(Projections.property("pg.currApp"), "currApp")
                    .add(Projections.property("pg.version"), "version")
                    .add(Projections.property("pg.firmwareVersion"), "firmwareVersion")
					.add(Projections.property("pg.currentProfile"), "currentProfile")
					.add(Projections.property("pg.groupId"), "groupId")
					.add(Projections.property("pg.upgradeStatus"), "upgradeStatus")					
					.add(Projections.property("gw.name"), "gatewayNameForFilter")
					.add(Projections.property("gw.id"), "gatewayIdForFilter")					
					).add(Restrictions.ne("pg.state", ServerConstants.PLUGLOAD_STATE_DELETED_STR))					
					.setResultTransformer(Transformers.aliasToBean(Plugload.class));
					

			switch (orgType) {
			case CAMPUS: {
				oRowCount.add(Restrictions.eq("pg.campusId", pid));
				oCriteria.add(Restrictions.eq("pg.campusId", pid));
				break;
			}
			case BUILDING: {
				oRowCount.add(Restrictions.eq("pg.buildingId", pid));
				oCriteria.add(Restrictions.eq("pg.buildingId", pid));
				break;
			}
			case FLOOR: {
				oRowCount.add(Restrictions.eq("pg.floor.id", pid));
				oCriteria.add(Restrictions.eq("pg.floor.id", pid));
				break;
			}
			case AREA: {
				oRowCount.add(Restrictions.eq("pg.area.id", pid));
				oCriteria.add(Restrictions.eq("pg.area.id", pid));
				break;
			}
			case GROUP: {
				oRowCount.add(Restrictions.eq("pg.groupId", pid));
				oCriteria.add(Restrictions.eq("pg.groupId", pid));
				break;
			}
			default: {
				// company level all fixtures
			}

			}
			if (bSearch) {
				if (searchField.equals("id")) {
					oRowCount.add(Restrictions.eq("pg.id",
							Long.parseLong(searchString)));
					oCriteria.add(Restrictions.eq("pg.id",
							Long.parseLong(searchString)));
				} else if (searchField.equals("name")) {
					oRowCount.add(Restrictions.like("pg.name", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("pg.name", "%"
							+ searchString + "%"));
				} else if (searchField.equals("snapaddress")) {
					oRowCount.add(Restrictions.like("pg.snapAddress", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("pg.snapAddress", "%"
							+ searchString + "%"));
				} else if (searchField.equals("state")) {
					oRowCount.add(Restrictions.like("pg.state", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("pg.state", "%"
							+ searchString + "%"));
				} else if (searchField.equals("currentprofile")) {
					oRowCount.add(Restrictions.like("pg.currentProfile", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("pg.currentProfile", "%"
							+ searchString + "%"));
				} else if (searchField.equals("upgradestatus")) {
					oRowCount.add(Restrictions.like("pg.upgradeStatus", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("pg.upgradeStatus", "%"
							+ searchString + "%"));
				} else if (searchField.equals("currapp")) {
					oRowCount.add(Restrictions.like("pg.currApp", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("pg.currApp", "%"
							+ searchString + "%"));
				} else if (searchField.equals("version")) {
					oRowCount.add(Restrictions.like("pg.version", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("pg.version", "%"
							+ searchString + "%"));
				} else if (searchField.equals("gateway")) {
					oRowCount.add(Restrictions.like("gw.name", "%"
							+ searchString + "%"));
					oCriteria.add(Restrictions.like("gw.name", "%"
							+ searchString + "%"));
				}
			}
			if (limit > 0) {
				oCriteria.setMaxResults(limit).setFirstResult(offset);
			}
			if (order != null && !"".equals(order)) {
				if (order.equals("name")) {
					order = "pg.name";
				} else if(order.equals("snapaddress")) {
				} else if (order.equals("currapp")) {
					order = "pg.currApp";
				} else if (order.equals("version")) {
					order = "version";
				} else if (order.equals("gateway")) {
					order = "gw.name";
				} else if (order.equals("currentprofile")) {
					order = "pg.currentProfile";
				} else if (order.equals("upgradestatus")) {
					order = "pg.upgradeStatus";
				} else if (order.equals("state")) {
					order = "pg.state";
				} else if (order.equals("status")) {
					statusOrder1 = "pg.state";
					statusOrder2 = "pg.isHopper";
					statusOrder3 = "pg.lastConnectivityAt";
				} else {
					order = "pg.id";
				}
				if ("desc".equals(orderWay)) {
					if(order.equals("snapaddress")) {
						oCriteria.addOrder(Order.desc("mac1ForFilter"))
						.addOrder(Order.desc("mac2ForFilter"))
						.addOrder(Order.desc("mac3ForFilter"));
					} else if(order.equals("status")){
						oCriteria.addOrder(Order.desc(statusOrder1));
						oCriteria.addOrder(Order.desc(statusOrder2));
						oCriteria.addOrder(Order.desc(statusOrder3));
					}
					else {
						oCriteria.addOrder(Order.desc(order));
					}
					
				} else {
					if(order.equals("snapaddress")) {
						oCriteria.addOrder(Order.asc("mac1ForFilter"))
						.addOrder(Order.asc("mac2ForFilter"))
						.addOrder(Order.asc("mac3ForFilter"));
					} else if (order.equals("status")){
						oCriteria.addOrder(Order.asc(statusOrder1));
						oCriteria.addOrder(Order.asc(statusOrder2));
						oCriteria.addOrder(Order.asc(statusOrder3));
					}
					else {
						oCriteria.addOrder(Order.asc(order));
					}
				}
			} else {
				oCriteria.addOrder(Order.desc("id"));
			}
			List<Object> output = (List<Object>) oRowCount.list();
			Long count = (Long) output.get(0);
			if (count.compareTo(new Long("0")) > 0) {
				plugloadList.setTotal(count);
				plugloadList.setPlugload(oCriteria.list());
			}
		}
		return plugloadList;
	}
	
	//this is called as part of node boot info which is called from app2
	// so set app2 as current app
	public void updateBootInfo(Plugload pl, String upgrStatus) {
						
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, pl.getId());
		plugload.setVersion(pl.getVersion());		
		plugload.setBootLoaderVersion(pl.getBootLoaderVersion());
		plugload.setLastConnectivityAt(pl.getLastConnectivityAt());
		plugload.setSecGwId(pl.getSecGwId());
		plugload.setCurrApp(pl.getCurrApp());
		plugload.setFirmwareVersion(pl.getFirmwareVersion());
		plugload.setCuVersion(pl.getCuVersion());
		plugload.setResetReason(pl.getResetReason());
		if(!upgrStatus.equals(ServerConstants.IMG_UP_STATUS_NOT_PENDING)) {
			plugload.setUpgradeStatus(pl.getUpgradeStatus());
		}
		plugload.setIsHopper(pl.getIsHopper());
		plugload.setLastBootTime(new Date());

	} // end of method updateBootInfo
	
	public void enablePushProfileForPlugload(Long plugloadId) {
		
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, plugloadId);
		plugload.setPushProfile(true);
				
	} //end of method enablePushProfileForPlugload

	public void updateState(Plugload plugload) {
		
		try {
			Session session = getSession();
			Plugload plug = (Plugload) session.get(Plugload.class, plugload.getId());
			plug.setState(plugload.getState());
			if (plug.getState().equals(ServerConstants.FIXTURE_STATE_DELETED_STR)) {
				plug.setGateway(null);
			}
			session.update(plug);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	} //end of method updateState
	
	public void updateRealtimeStats(Plugload plugload) {
		
		Session session = getSession();
		Plugload dbPlugload = (Plugload) session.get(Plugload.class, plugload.getId());
		dbPlugload.setLastConnectivityAt(plugload.getLastConnectivityAt());		
		dbPlugload.setGlobalProfileChecksum(plugload.getGlobalProfileChecksum());
		dbPlugload.setScheduledProfileChecksum(plugload.getScheduledProfileChecksum());
		dbPlugload.setCurrentState(plugload.getCurrentState());
		dbPlugload.setManagedLoad(plugload.getManagedLoad());
		dbPlugload.setAvgTemperature(plugload.getAvgTemperature());
		dbPlugload.setIsHopper(plugload.getIsHopper());
		dbPlugload.setAvgVolts(plugload.getAvgVolts());
		dbPlugload.setSecGwId(plugload.getSecGwId());
		dbPlugload.setUnmanagedLoad(plugload.getUnmanagedLoad());
		dbPlugload.setLastOccupancySeen(plugload.getLastOccupancySeen());
		
	} //end of method updateRealtimeStats
	
	public void enablePushGlobalProfileForPlugload(Long plugloadId) {
		
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, plugloadId);
		plugload.setPushGlobalProfile(true);
		
	} //end of method enablePushGlobalProfileForPlugload
	
	public PlugloadProfileHandler getProfileHandlerByGroupId(Long groupId) {
		
		Session session = getSession();
		PlugloadProfileHandler profileHandler = ((PlugloadGroups) session.get(PlugloadGroups.class, groupId)).getPlugloadProfileHandler();
		
		if (profileHandler != null) {
			profileHandler.setDayProfile((PlugloadProfile) getObject(PlugloadProfile.class, profileHandler.getDayProfile().getId()));
			profileHandler.setDayProfileHoliday((PlugloadProfile) getObject(PlugloadProfile.class, 
					profileHandler.getDayProfileHoliday().getId()));
			profileHandler.setDayProfileWeekEnd((PlugloadProfile) getObject(PlugloadProfile.class, 
					profileHandler.getDayProfileWeekEnd().getId()));

			profileHandler.setEveningProfile((PlugloadProfile) getObject(PlugloadProfile.class, profileHandler.getEveningProfile().getId()));
			profileHandler.setEveningProfileHoliday((PlugloadProfile) getObject(PlugloadProfile.class, 
					profileHandler.getEveningProfileHoliday().getId()));
			profileHandler.setEveningProfileWeekEnd((PlugloadProfile) getObject(PlugloadProfile.class, 
					profileHandler.getEveningProfileWeekEnd().getId()));

			profileHandler.setMorningProfile((PlugloadProfile) getObject(PlugloadProfile.class, profileHandler.getMorningProfile().getId()));
			profileHandler.setMorningProfileHoliday((PlugloadProfile) getObject(PlugloadProfile.class, 
					profileHandler.getMorningProfileHoliday().getId()));
			profileHandler.setMorningProfileWeekEnd((PlugloadProfile) getObject(PlugloadProfile.class, 
					profileHandler.getMorningProfileWeekEnd().getId()));

			profileHandler.setNightProfile((PlugloadProfile) getObject(PlugloadProfile.class, profileHandler.getNightProfile().getId()));
			profileHandler.setNightProfileHoliday((PlugloadProfile) getObject(PlugloadProfile.class, 
					profileHandler.getNightProfileHoliday().getId()));
			profileHandler.setNightProfileWeekEnd((PlugloadProfile) getObject(PlugloadProfile.class, 
					profileHandler.getNightProfileWeekEnd().getId()));
						
			profileHandler.setOverride5((PlugloadProfile) getObject(PlugloadProfile.class, profileHandler.getOverride5().getId()));
			profileHandler.setOverride6((PlugloadProfile) getObject(PlugloadProfile.class, profileHandler.getOverride6().getId()));
			profileHandler.setOverride7((PlugloadProfile) getObject(PlugloadProfile.class, profileHandler.getOverride7().getId()));
			profileHandler.setOverride8((PlugloadProfile) getObject(PlugloadProfile.class, profileHandler.getOverride8().getId()));

			profileHandler.setPlugloadProfileConfiguration((PlugloadProfileConfiguration) getObject(PlugloadProfileConfiguration.class, 
					profileHandler.getPlugloadProfileConfiguration().getId()));
		}
		return profileHandler;
		
	} //end of method getProfileHandlerByGroupId
	
	public PlugloadProfileHandler getProfileHandlerByPlugloadId(Long plugloadId) {
		
		Session session = getSession();		
		Long groupId = ((Plugload) session.get(Plugload.class,plugloadId)).getGroupId();		
		PlugloadProfileHandler profileHandler = getProfileHandlerByGroupId(groupId);	
		return profileHandler;
		
	} //end of method getProfileHandlerByPlugloadId
		
	public int getProfileNoForPlugload(long plugloadId) {
		
		String sql = "from PlugloadGroups g where g.id = (SELECT groupId from Plugload p WHERE p.id = ?)"; 
		Query q = getSession().createQuery(sql.toString());
    q.setParameter(0, plugloadId);
    PlugloadGroups group = (PlugloadGroups)q.uniqueResult();
		return group.getProfileNo();
		
	} //end of method getProfileNoForPlugload
	
	public Long assignGroupProfileToPlugload(long plugloadId, PlugloadGroups plugGroup) {
		
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, plugloadId);
		plugload.setGroupId(plugGroup.getId());
		plugload.setOriginalProfileFrom(plugload.getCurrentProfile()); // Update this first
		plugload.setCurrentProfile(plugGroup.getName());
		return plugGroup.getId();
		
	} //end of method assignGroupProfileToPlugload

	public void updateStateAndLastConnectivityTime(Plugload plugload) {
		try {
			Session session = getSession();
			Plugload obj = (Plugload) session
					.get(Plugload.class, plugload.getId());
			obj.setCommissionStatus(plugload.getCommissionStatus());
			obj.setState(plugload.getState());
			obj.setLastConnectivityAt(plugload.getLastConnectivityAt());
			if (obj.getState().equals(
					ServerConstants.FIXTURE_STATE_DELETED_STR)) {
				obj.setCommissionStatus(0);
				obj.setGateway(null);
			}
			session.update(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void enablePushProfileAndGlobalPushProfile(Long plugloadId,boolean pushProfileStatus, boolean globalPushProfileStatus) {
				
		Session session = getSession();
		Plugload dbPlugload = (Plugload) session.get(Plugload.class, plugloadId);
		dbPlugload.setPushProfile(pushProfileStatus);
		dbPlugload.setPushGlobalProfile(globalPushProfileStatus);
		
	} //end of method enablePushProfileAndGlobalPushProfile
	
	public void changeGroupsSyncPending(Plugload plugload) {
		
		Session session = getSession();
		Plugload dbPlugload = (Plugload) session.get(Plugload.class, plugload.getId());
		dbPlugload.setGroupsSyncPending(plugload.getGroupsSyncPending());
		
	} //end of method changeGroupsSyncPending
	
	public void changeGroupsSyncPending(long id, boolean bEnable) {
        Session session = getSession();
        Plugload fixture = (Plugload) session.get(Plugload.class, id);
        fixture.setGroupsSyncPending(bEnable);
    }
	
	public void updateStats(Plugload plugload) {
	
		Session session = getSession();
		Plugload dbPlugload = (Plugload) session.get(Plugload.class, plugload.getId());		
				
		dbPlugload.setLastConnectivityAt(plugload.getLastConnectivityAt());
		dbPlugload.setLastStatsRcvdTime(plugload.getLastStatsRcvdTime());
		dbPlugload.setCurrApp(plugload.getCurrApp());
		dbPlugload.setCurrentState(plugload.getCurrentState());
		dbPlugload.setManagedLoad(plugload.getManagedLoad());
		dbPlugload.setUnmanagedLoad(plugload.getUnmanagedLoad());
		if(plugload.getSecGwId() != null) {
			dbPlugload.setSecGwId(plugload.getSecGwId());
		}
		dbPlugload.setAvgVolts(plugload.getAvgVolts());
		dbPlugload.setAvgTemperature(plugload.getAvgTemperature());
		dbPlugload.setManagedBaselineLoad(plugload.getManagedBaselineLoad());
		dbPlugload.setUnmanagedBaselineLoad(plugload.getUnmanagedBaselineLoad());
		dbPlugload.setVersionSynced(plugload.getVersionSynced());		
		
	} // end of method updateStats
	
	/**
	 * This method will to bulk assignment of profiles to selected Plugloads'
	 * list.
	 * 
	 * @param plugloadList
     *           
     * @param profileGroupid
     *            Id of the selected plugload group to be assigned
     * @param currentPlugloadProfile
     *         	  Name of the selected plugload profile
	 * @return totalRecordUpdated
	 */
	public Long bulkProfileAssignToPlugload(String plugloadIdsList, Long plugloadGroupid,
			String currentPlugloadProfile) {
		int totalRecordUpdated = 0;
		try {
			if(plugloadIdsList!=null && plugloadIdsList.length()>0)
			{
				String hsql1 = "update Plugload set original_profile_from=current_profile, current_profile=:currentprofileName, group_id=:groupId where id in ("+plugloadIdsList+") and current_profile!=:currentprofileName and group_id!=:groupId";
				Query query1 = getSession().createSQLQuery(hsql1);
				query1.setString("currentprofileName", currentPlugloadProfile);
				query1.setLong("groupId", plugloadGroupid);
				query1.executeUpdate();
				
				// Update push profile and push global profile flag 
				String hsql2 = "update Plugload set push_profile=:pushFlag, push_global_profile=:pushFlag where id in ("+plugloadIdsList+")";
				Query query2 = getSession().createSQLQuery(hsql2);
				query2.setBoolean("pushFlag", true);
				totalRecordUpdated = query2.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (long) totalRecordUpdated;
	}
	
	/**
	 * return list of Gateway object associated with secondary gateway
	 *
	 * @param secGwId
	 * @return list of Gateway object associated with secondary gateway
	 */
	@SuppressWarnings("unchecked")
	public List<Plugload> loadAllPlugloadBySecondaryGatewayId(Long secGwId) {
		Session session = getSession();
		List<Plugload> plugloadList = session
				.createCriteria(Plugload.class)
				.createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state",
						ServerConstants.PLUGLOAD_STATE_DELETED_STR))
				.add(Restrictions.ne("state",
						ServerConstants.PLUGLOAD_STATE_PLACED_STR))
				.add(Restrictions.eq("secGwId", secGwId))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.addOrder(Order.asc("lastConnectivityAt")).list();
		if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
			return plugloadList;
		} else {
			return null;
		}
	}

	public String getCommissionStatus(long plugloadId) {
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, plugloadId);
		String sStatus = plugload.getState();
		if (sStatus != null) {
			return sStatus;
		}
		return "";
	} // end of method isCommissioned
	
	/**
	 * Fetch plugload count by the property association
	 *
	 * @param property
	 * @param pid
	 * @return Long count of plugload.
	 */
	public Long getPlugloadCount(String property, Long pid) {
		
		Long totalPlugloadCount = 0L;
		if (property.equals("company")) {
			totalPlugloadCount = (Long) getSession().createCriteria(Plugload.class).add(Restrictions.ne("state",	
					ServerConstants.PLUGLOAD_STATE_DELETED_STR)).add(Restrictions.ne("state",
							ServerConstants.PLUGLOAD_STATE_PLACED_STR)).setProjection(Projections.sum("noOfPlugload"))
							.uniqueResult();
		} else {
			totalPlugloadCount = (Long) getSession().createCriteria(Plugload.class).add(Restrictions.eq(property, pid))
					.add(Restrictions.ne("state", ServerConstants.PLUGLOAD_STATE_DELETED_STR))
					.add(Restrictions.ne("state", ServerConstants.PLUGLOAD_STATE_PLACED_STR))
					.setProjection(Projections.sum("noOfPlugloads")).uniqueResult();
		}
		if (totalPlugloadCount == null) {
			totalPlugloadCount = 0L;
		}
		return totalPlugloadCount;
		
	} //end of method getPlugloadCount
	
	
	public List<Long> loadPlugloaddIdWithGroupSynchFlagTrue() {
        Query query = getSession().createQuery(
                " Select new java.lang.Long(f.id) from Plugload f where f.groupsSyncPending = :synchFlag ");
        query.setBoolean("synchFlag", true);
        List<Long> fixtureIds = query.list();
        return fixtureIds;
    }
	
	@SuppressWarnings("unchecked")
	public List<Plugload> getPlacedPlugloadList(long gatewayId) {
		
		Session session = getSession();
		List<Plugload> plugloadList = session.createCriteria(Plugload.class)
				.add(Restrictions.eq("state", ServerConstants.PLUGLOAD_STATE_PLACED_STR))
				.add(Restrictions.eq("gateway.id", gatewayId)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
			return plugloadList;
		} else {
			return null;
		}
		
	} //end of method getPlacedPlugloadList
	
	@SuppressWarnings("unchecked")
	public List<Plugload> loadPlacedAndCommissionedPlugloadsByFloorId(Long id) {
		
		Session session = getSession();
		List<Plugload> plugloadList = session.createCriteria(Plugload.class).createAlias("area", "area", Criteria.LEFT_JOIN)
				.add(Restrictions.ne("state", ServerConstants.PLUGLOAD_STATE_DELETED_STR))
				.add(Restrictions.ne("state", ServerConstants.PLUGLOAD_STATE_DISCOVER_STR))
				.add(Restrictions.eq("floor.id", id))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		if (!ArgumentUtils.isNullOrEmpty(plugloadList)) {
			return plugloadList;
		} else {
			return null;
		}
		
	}
	
	public Integer getPlugloadHopperStatus(long plugloadId) {
		
		Session session = getSession();
		Plugload plugload = (Plugload) session.get(Plugload.class, plugloadId);
		Integer isHopper = plugload.getIsHopper();
		return isHopper;

	} //end of method getPlugloadHopperStatus
	
	/*
	 * replace the old plugload with the new plugload attributes used for RMA
	 */
	public void replacePlugload(long plugloadId, String plugloadName,
			String macAddr, String snapAddr, String modelNo,
			Integer commisionStatus, String state) {
        try {
        	Session session = getSession();
        	Plugload dbPlugload = (Plugload) session.get(Plugload.class, plugloadId);
        	dbPlugload.setName(plugloadName);
        	dbPlugload.setMacAddress(macAddr);
        	//dbPlugload.setPlugloadId(sensorId);
        	dbPlugload.setSnapAddress(snapAddr);
        	dbPlugload.setCommissionStatus(commisionStatus);
        	dbPlugload.setState(state);
        	dbPlugload.setModelNo(modelNo);           	
            session.flush();
                        
        } catch (Exception e) {
            logger.debug("Error in replacing the plugload: " + e.getMessage());
        }

	} // end of method replacePlugload

	public Plugload updateAreaID(Plugload oPlugload) {
			Session session = getSession();
			Plugload plugloadval = (Plugload) session.load(Plugload.class, oPlugload.getId());
			plugloadval.setAreaId(oPlugload.getAreaId());
			session.update(plugloadval);
			return plugloadval;
	}
	
	@SuppressWarnings("unchecked")
	public List<Device> loadDeviceById(Long id) {
		
		Session session = getSession();
		List<Device> deviceList = session.createCriteria(Device.class)
				.add(Restrictions.eq("id", id)).list();
		if (!ArgumentUtils.isNullOrEmpty(deviceList)) {
			return deviceList;
		} else {
			return null;
		}
		
	}
		
} //end of class PlugloadDao
