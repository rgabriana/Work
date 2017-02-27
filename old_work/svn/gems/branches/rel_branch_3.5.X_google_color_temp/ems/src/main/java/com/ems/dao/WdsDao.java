/**
 * 
 */
package com.ems.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.ButtonManipulation;
import com.ems.model.Wds;
import com.ems.server.ServerConstants;
import com.ems.types.DeviceType;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.model.WdsList;

/**
 * @author yogesh
 * 
 */
@Repository("wdsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class WdsDao extends BaseDaoHibernate {
	
	static final Logger logger = Logger.getLogger("SwitchLogger");

    public String getNextWdsNo() {
        String hsql = "select nextval('wds_no_seq') as wdsno";
        Query q = getSession().createSQLQuery(hsql.toString());
        List<Object> oResult = q.list();
        BigInteger value = (BigInteger)oResult.get(0);
        return String.format("%06d", value);
    }

    public List<Wds> loadAllWds() {
        Session session = getSession();
        return session.createCriteria(Wds.class).list();
    }

    public List<Wds> loadAllNonCommissionedWds() {
        Session session = getSession();
        return session.createCriteria(Wds.class)
                .add(Restrictions.eq("state", ServerConstants.WDS_STATE_DISCOVER_STR))
                .list();
    }
    
    public List<Wds> loadAllCommissionedWdsByGatewayId(Long secGwId) {
        Session session = getSession();
        List<Wds> wdsList = session.createCriteria(Wds.class)
                .add(Restrictions.eq("state", ServerConstants.WDS_STATE_COMMISSIONED_STR))
                .add(Restrictions.eq("gatewayId", secGwId)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .addOrder(Order.asc("id")).list();
        if (!ArgumentUtils.isNullOrEmpty(wdsList)) {
            return wdsList;
        } else {
            return null;
        }
    }

    public Wds getWdsSwitchById(Long wdsId) {
        Session session = getSession();
        Wds wds = (Wds) session.createCriteria(Wds.class).add(Restrictions.eq("id", wdsId))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).addOrder(Order.asc("id")).uniqueResult();
        return wds;
    }
    
    public Wds getWdsSwitchByName(String wdsname) {
        Session session = getSession();
        Wds wds = (Wds) session.createCriteria(Wds.class).add(Restrictions.eq("name", wdsname))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).addOrder(Order.asc("id")).uniqueResult();
        return wds;
    }

    public List<Wds> getUnCommissionedWDSList(long gatewayId) {
        Session session = getSession();
        List<Wds> wdsList = session.createCriteria(Wds.class)
                .add(Restrictions.ne("state", ServerConstants.WDS_STATE_DELETED_STR))
                .add(Restrictions.ne("state", ServerConstants.WDS_STATE_COMMISSIONED_STR))
                .add(Restrictions.eq("gatewayId", gatewayId)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();
        return wdsList;
    }

    public Wds getWdsSwitchBySnapAddress(String snapAddress) {
        Session session = getSession();
        Wds wds = (Wds) session.createCriteria(Wds.class).add(Restrictions.eq("macAddress", snapAddress))
                .uniqueResult();
        return wds;
    }

    public String getCommissioningStatus(long wdsId) {
        Session session = getSession();
        Wds oWds = (Wds) session.get(Wds.class, wdsId);
        String sStatus = oWds.getState();
        if (sStatus != null) {
            return sStatus;
        }
        return "";
    }

    public Wds AddWdsSwitch(Wds oWds) {
        return (Wds) saveObject(oWds);
    }

    public List<Wds> loadWdsByCampusId(Long id) {
        List<Wds> results = new ArrayList<Wds>();
        Session session = getSession();
        List<Wds> wdsList = session.createCriteria(Wds.class).add(Restrictions.eq("campusId", id))
                .add(Restrictions.eq("state", ServerConstants.WDS_STATE_COMMISSIONED_STR)).list();
        if (!ArgumentUtils.isNullOrEmpty(wdsList)) {
            return wdsList;
        }
        return results;
    }

    public List<Wds> loadWdsByBuildingId(Long id) {
        List<Wds> results = new ArrayList<Wds>();
        Session session = getSession();
        List<Wds> wdsList = session.createCriteria(Wds.class).add(Restrictions.eq("buildingId", id))
                .add(Restrictions.eq("state", ServerConstants.WDS_STATE_COMMISSIONED_STR)).list();
        if (!ArgumentUtils.isNullOrEmpty(wdsList)) {
            return wdsList;
        }
        return results;
    }

    public List<Wds> loadWdsByFloorId(Long id) {
        List<Wds> results = new ArrayList<Wds>();
        Session session = getSession();
        List<Wds> wdsList = session.createCriteria(Wds.class).add(Restrictions.eq("floorId", id))
                .add(Restrictions.eq("state", ServerConstants.WDS_STATE_COMMISSIONED_STR)).list();
        if (!ArgumentUtils.isNullOrEmpty(wdsList)) {
            return wdsList;
        }
        return results;
    }

    public List<Wds> loadWdsByAreaId(Long id) {
        List<Wds> results = new ArrayList<Wds>();
        Session session = getSession();
        List<Wds> wdsList = session.createCriteria(Wds.class).add(Restrictions.eq("areaId", id))
                .add(Restrictions.eq("state", ServerConstants.WDS_STATE_COMMISSIONED_STR)).list();
        if (!ArgumentUtils.isNullOrEmpty(wdsList)) {
            return wdsList;
        }
        return results;
    }

    public List<Wds> loadCommissionedWdsBySwitchId(Long id) {
        List<Wds> results = new ArrayList<Wds>();
        Session session = getSession();
        List<Wds> wdsList = session.createCriteria(Wds.class).add(Restrictions.eq("wdsSwitch.id", id))
                .add(Restrictions.eq("state", ServerConstants.WDS_STATE_COMMISSIONED_STR))
                .add(Restrictions.eq("associationState", ServerConstants.WDS_STATE_ASSOCIATED)).list();
        if (!ArgumentUtils.isNullOrEmpty(wdsList)) {
            return wdsList;
        }
        return results;
    }

    public List<Wds> loadNotAssociatedWdsBySwitchId(Long id) {
        List<Wds> results = new ArrayList<Wds>();
        Session session = getSession();
        List<Wds> wdsList = session.createCriteria(Wds.class).add(Restrictions.eq("wdsSwitch.id", id))
                .add(Restrictions.ne("state", ServerConstants.WDS_STATE_DISCOVER_STR))
                .add(Restrictions.eq("associationState", ServerConstants.WDS_STATE_NOT_ASSOCIATED)).list();
        if (!ArgumentUtils.isNullOrEmpty(wdsList)) {
            return wdsList;
        }
        return results;
    }

    public ButtonManipulation loadWdsButtonManipulationById(Long buttonMapId) {
        Session session = getSession();
        ButtonManipulation oBM = (ButtonManipulation)session.createCriteria(ButtonManipulation.class)
                .add(Restrictions.eq("buttonMapId", buttonMapId)).uniqueResult();
        return oBM;
    }

    public void updateState(Wds oWds) {
        Session session = getSession();
        Wds wdsObj = (Wds) session.get(Wds.class, oWds.getId());
        wdsObj.setState(oWds.getState());
        session.update(wdsObj);
    }

    public void update(Wds oWds) {
        Session session = getSession();
        session.update(oWds);
    }

    public Wds updatePosition(Long Id, Integer x, Integer y) {
        Session session = getSession();
        Wds oWds = (Wds) session.load(Wds.class, Id);
        oWds.setXaxis(x);
        oWds.setYaxis(y);
        session.saveOrUpdate("xaxis", oWds);
        session.saveOrUpdate("yaxis", oWds);
        return oWds;
    }
    
    public void setImageUpgradeStatus(long wdsId, String status) {
    	
  		Session session = getSession();
  		Wds wds = (Wds) session.get(Wds.class, wdsId);
  		wds.setUpgradeStatus(status);

  	} // end of method setImageUpgradeStatus
    
    public void updateVersion(String version, long id, long gwId) {
    	
  		Session session = getSession();
  		Wds wds = (Wds) session.get(Wds.class, id);  		
  		wds.setVersion(version);  		
  		wds.setGatewayId(gwId);

  	} // end of method updateVersion
    
    /**
	 * update Gateway Id for all wds in case of GW RMA
	 *
	 * @param fromGatewayId
	 *            Old Gateway Id
	 * @param toGatewayId
	 *            New Gateway Id
	 * @return 
	 */
	public void updateGatewayId(Long fromGatewayId, Long toGatewayId){
		try {
			Query query = getSession().createQuery("Update Wds set " + "gatewayId = :newGWId"
					+ " where gatewayId = :oldGWId");			
			
			query.setLong("newGWId", toGatewayId);
			query.setLong("oldGWId", fromGatewayId);
			query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	@SuppressWarnings("unchecked")
	public WdsList loadWdsList(String orderby,String orderway,int offset, int limit){
		WdsList wdsList = new WdsList();
		Criteria oCriteria = null;
		Criteria oRowCount = null;

		oRowCount = sessionFactory.getCurrentSession()
				.createCriteria(Wds.class, "wds")
				.setProjection(Projections.rowCount());
		oCriteria = sessionFactory.getCurrentSession().createCriteria(
				Wds.class, "wds");

		
		if (orderby != null && !"".equals(orderby)) {
			if (orderby.equals("name")) {
				orderby = "wds.name";
			} else if (orderby.equals("location")) {
				orderby = "wds.location";
			} else if (orderby.equals("batteryLevel")) {
				orderby = "wds.batteryVoltage";
			} else if (orderby.equals("captureAtStr")) {
				orderby = "wds.voltageCaptureAt";
			} else {
				orderby = "wds.name";
			}
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc(orderby));
			}else{
				oCriteria.addOrder(Order.asc(orderby));
			}
			
		} else {
			if ("desc".equals(orderway)){
				oCriteria.addOrder(Order.desc("name"));
			}else{
				oCriteria.addOrder(Order.asc("name"));
			}
		}
		
		if (limit > 0) {
			oCriteria.setMaxResults(limit).setFirstResult(offset);
		}

		List<Object> output = (List<Object>) oRowCount.list();
		Long count = (Long) output.get(0);
		if (count.compareTo(new Long("0")) > 0) {
			wdsList.setTotal(count);
			wdsList.setWds(oCriteria.list());
			return wdsList;
		}

		return wdsList; 
	}
	
	
	/*
	 * replace the old wds with the new wds attributes used for RMA
	 */
	public void replaceWdS(long wdsId, String wdsName,
			String macAddr,String modelNo,
			String state,String hlaSerialNo,String hlaPartNo,String pcbaPartNo,String pcbaSerialNo) {
        try {
        	Session session = getSession();
        	Wds dbWds = (Wds) session.get(Wds.class, wdsId);
        	dbWds.setName(wdsName);
        	dbWds.setMacAddress(macAddr);
        	dbWds.setModelNo(modelNo);
        	dbWds.setState(state);
        	dbWds.setHlaSerialNo(hlaSerialNo);
        	dbWds.setHlaPartNo(hlaPartNo);
        	dbWds.setPcbaPartNo(pcbaPartNo);
        	dbWds.setPcbaSerialNo(pcbaSerialNo);
            session.flush();
                        
        } catch (Exception e) {
            logger.debug("Error in replacing the wds: " + e.getMessage());
        }

	} // end of method replaceWdS
	public List<Object[]> getErcCountByVersionNo() {
		List<Object[]> fxList = new ArrayList<Object[]>();
        String hql = "select distinct d.version, count(d.id) from device d join wds w on w.id=d.id where d.type =:type and w.state=:state group by d.version";
        Session s = getSession();
        Query q = s.createSQLQuery(hql);
        q.setParameter("type",DeviceType.WDS.getName());
        q.setParameter("state",ServerConstants.FIXTURE_STATE_COMMISSIONED_STR);
        fxList = q.list();
        return  fxList;
	}
}
