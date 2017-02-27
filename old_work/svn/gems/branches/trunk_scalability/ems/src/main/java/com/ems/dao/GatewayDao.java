package com.ems.dao;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Gateway;
import com.ems.server.ServerConstants;
import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("gatewayDao")
@Transactional(propagation = Propagation.REQUIRED)
public class GatewayDao extends BaseDaoHibernate {

    /**
     * Return gatways details if sid given
     * 
     * @param sid
     * @return com.ems.model.Gateway
     */
    public Gateway loadGatewayByUid(String uid) {
        Session session = getSession();
        Gateway gateway = (Gateway) session.createCriteria(Gateway.class)
                .add(Restrictions.eq("uniqueIdentifierId", uid)).uniqueResult();
        return gateway;

    }

    /**
     * Return gatways details if mac address given
     * 
     * @param macAddress
     * @return com.ems.model.Gateway
     */
    public Gateway loadGatewayByMacAddress(String macAddress) {
        Session session = getSession();
        Gateway gateway = (Gateway) session.createCriteria(Gateway.class)
                .add(Restrictions.eq("macAddress", macAddress))
                .createAlias("floor", "floor").uniqueResult();
        return gateway;
    }
    
   
    public Gateway loadGateway(Long id) {
        return (Gateway) getObject(Gateway.class, id);
    }

    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    @SuppressWarnings("unchecked")
    public List<Gateway> loadCampusGateways(Long campusId) {
        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class).add(Restrictions.eq("campusId", campusId))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
            return gatewayList;
        } else {
            return null;
        }
    }
    /**
     * Return commisioned gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    @SuppressWarnings("unchecked")
    public List<Gateway> loadCommisionedCampusGateways(Long campusId) {
    	 Session session = getSession();
         List<Gateway> gatewayList = session.createCriteria(Gateway.class).add(Restrictions.eq("campusId", campusId)).add(Restrictions.eq("commissioned", true))
                 .list();
         if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
         	
             return gatewayList;
         } else {
             return null;
         }
    }
    
    
    /**
     * To find the duplicate gateways
     * @return
     */
    
    @SuppressWarnings("unchecked")
    public int loadCommisionedGateways(String gatewayName) {
    	 Session session = getSession();
         List<Gateway> gatewayList = session.createCriteria(Gateway.class).add(Restrictions.eq("commissioned", true)).add(Restrictions.eq("name", gatewayName))
                 .list();
         if (gatewayList != null && !gatewayList.isEmpty()) {
             return gatewayList.size();
         }else {
             return 0;
         }
    }
    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    @SuppressWarnings("unchecked")
    public List<Gateway> loadCampusGatewaysWithActiveSensors(Long campusId) {
        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class).add(Restrictions.eq("campusId", campusId))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
        	getActiveGatewaySensors(gatewayList);
            return gatewayList;
        } else {
            return null;
        }
    }

    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    @SuppressWarnings("unchecked")
    public List<Gateway> loadBuildingGateways(Long buildingId) {
        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class)
                .add(Restrictions.eq("buildingId", buildingId)).list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
            return gatewayList;
        } else {
            return null;
        }

    }
    
    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    @SuppressWarnings("unchecked")
    public List<Gateway> loadBuildingGatewaysWithActiveSensors(Long buildingId) {
        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class)
                .add(Restrictions.eq("buildingId", buildingId)).list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
        	getActiveGatewaySensors(gatewayList);
            return gatewayList;
        } else {
            return null;
        }

    }
    /**
     * Return commisioned gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    @SuppressWarnings("unchecked")
    public List<Gateway> loadCommissionedBuildingGateways(Long buildingId) {
        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class)
                .add(Restrictions.eq("buildingId", buildingId)).add(Restrictions.eq("commissioned", true)).list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
            return gatewayList;
        } else {
            return null;
        }

    }


    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    @SuppressWarnings("unchecked")
    public List<Gateway> loadFloorGateways(Long floorId) {
        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class).add(Restrictions.eq("floor.id", floorId))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
            return gatewayList;
        } else {
            return null;
        }

    }
    
    /**
     * Return gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    @SuppressWarnings("unchecked")
    public List<Gateway> loadFloorGatewaysWithActiveSensors(Long floorId) {
        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class).add(Restrictions.eq("floor.id", floorId))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
        	getActiveGatewaySensors(gatewayList);
            return gatewayList;
        } else {
            return null;
        }

    }
    /**
     * Return commissioned gatways details if campusId given
     * 
     * @param campusId
     * @return com.ems.model.Gateway
     */
    @SuppressWarnings("unchecked")
    public List<Gateway> loadCommissionedFloorGateways(Long floorId) {
        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class).add(Restrictions.eq("floor.id", floorId)).add(Restrictions.eq("commissioned", true))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
        	
            return gatewayList;
        } else {
            return null;
        }

    }
    @SuppressWarnings("unchecked")
    public List<Gateway> loadAllGateways() {
		Session session = getSession();
		List<Gateway> gatewayList = session.createCriteria(Gateway.class).list();
        return gatewayList;
    }
    
    @SuppressWarnings("unchecked")
    public List<Gateway> loadAllGatewaysWithActiveSensors() {

        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class).list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
        	getActiveGatewaySensors(gatewayList);
        }
        return gatewayList;
    }
    @SuppressWarnings("unchecked")
    public List<Gateway> loadAllCommissionedGateways() {

        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class).add(Restrictions.eq("commissioned", true)).list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
        	getActiveGatewaySensors(gatewayList);
        }
        return gatewayList;
    }
    /**
     * Return all commissioned gateways
     * 
     * @return List com.ems.model.Gateway
     */
    @SuppressWarnings("unchecked")
    public List<Gateway> loadCommissionedGateways() {
        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class).add(Restrictions.eq("commissioned", true))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
            return gatewayList;
        } else {
            return null;
        }
        
    } //end of method loadCommissionedGateways
    
    

        
  
    
    /**
     * Return all uncommissioned gateways
     * 
     * @return List com.ems.model.Gateway
     */
    @SuppressWarnings("unchecked")
    public List<Gateway> loadUnCommissionedGateways() {
        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class).add(Restrictions.eq("commissioned", false))
                .list();
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
            return gatewayList;
        } else {
            return null;
        }

    }
    
    @SuppressWarnings("unchecked")
	public List<Gateway> getActiveGatewaySensors(List<Gateway> list) {
    	StringBuffer gateway_ids = new StringBuffer("(");
    	boolean first = true;
    	for(Gateway gw: list) {
    		if(first) {
    			first = false;
    		}
    		else {
    			gateway_ids.append(", ");
    		}
    		gateway_ids.append(gw.getId());
    	}
    	gateway_ids.append(")");
    	
    	String hsql = "select gw.id, count(gw.id) from fixture fx, gateway gw " +
    					"where gw.id = fx.gateway_id and fx.state != 'DELETED' and gw.id in " + gateway_ids.toString()
    					+ " group by gw.id";
        Query q = getSession().createSQLQuery(hsql.toString());
        List<Object[]> results = (List<Object[]>)q.list();
        Map<Long,Integer> map = new HashMap<Long,Integer>();
        if (results != null && !results.isEmpty()) {
        	for(Object[] a: results) {
        		map.put(Long.parseLong(((BigInteger)a[0]).toString()), Integer.parseInt(((BigInteger)a[1]).toString()));
        	}
        }
        for(Gateway gw: list) {
        	gw.setNoOfActiveSensors(map.get(gw.getId()));
        }
        return list;
    }

    public Gateway getGatewayByIp(String ip) {
        Session session = getSession();
        Gateway gateway = (Gateway) session.createCriteria(Gateway.class).add(Restrictions.eq("ipAddress", ip))
                .uniqueResult();
        return gateway;

    } // end of method getGatewayByIp

    public Gateway getUEMGateway() {
        Session session = getSession();
        Gateway gateway = (Gateway) session.createCriteria(Gateway.class).add(Restrictions.eq("gatewayType", ServerConstants.UEM_GW))
                .uniqueResult();
        return gateway;

    } // end of method getGatewayByIp

    public void updateGateway(Gateway gateway) {
        if(gateway.getWirelessEncryptKey().equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
          gateway.setWirelessEncryptKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
        }
        updateGw(gateway);
    }
    
    public void updateGw(Gateway gateway) {
        Session session = getSession();
        session.clear();
        session.update(gateway);
        session.flush();
    }

    public void updateGatewayParameters(Gateway gw) {
      if(gw.getWirelessEncryptKey().equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
        gw.setWirelessEncryptKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
      }
      updateGw(gw);
    } // end of method updateGatewayParameters

    public void saveGatewayInfo(Gateway gw) {
      if(gw.getWirelessEncryptKey().equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
        gw.setWirelessEncryptKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
      }
      updateGw(gw);
    } // end of method saveGatewayInfo

    /**
     * Sets no of sensors for this gateway.
     */
    public Gateway updateFields(Gateway gw) {
      if(gw.getWirelessEncryptKey().equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
        gw.setWirelessEncryptKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
      }
      updateGw(gw);
      return gw;
    } // end of method updateFields

    public void setVersions(Gateway gw) {
      if(gw.getWirelessEncryptKey().equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
        gw.setWirelessEncryptKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
      }
      updateGw(gw);
    } // end of method setVersions

}
