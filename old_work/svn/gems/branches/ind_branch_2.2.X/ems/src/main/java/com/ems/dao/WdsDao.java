/**
 * 
 */
package com.ems.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.ButtonManipulation;
import com.ems.model.Wds;
import com.ems.server.ServerConstants;
import com.ems.utils.ArgumentUtils;

/**
 * @author yogesh
 * 
 */
@Repository("wdsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class WdsDao extends BaseDaoHibernate {

    public String getNextWdsNo() {
        String hsql = "select nextval('wds_no_seq') as wdsno";
        Query q = getSession().createSQLQuery(hsql.toString());
        List<Object> oResult = q.list();
        BigInteger value = (BigInteger)oResult.get(0);
        return String.format("%06d", value);
    }

    public List<Wds> loadAllWds() {
        Session session = getSession();       
        return session.createCriteria(Wds.class).add(Restrictions.eq("state", ServerConstants.WDS_STATE_COMMISSIONED_STR)).list();
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

}
