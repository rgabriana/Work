package com.ems.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
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
        /*if(gateway != null && gateway.getId() != null) {
        	List<Gateway> list = new ArrayList<Gateway>();
        	list.add(gateway);
        	getActiveGatewaySensors(list);
        }*/
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
        /*if(gateway != null && gateway.getId() != null) {
        	List<Gateway> list = new ArrayList<Gateway>();
        	list.add(gateway);
        	getActiveGatewaySensors(list);
        }*/
        return gateway;
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
        	getActiveGatewayDevices(gatewayList);
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
        	getActiveGatewayDevices(gatewayList);
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
        	getActiveGatewayDevices(gatewayList);
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
        	getActiveGatewayDevices(gatewayList);
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
    
    //TODO
    
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

    @SuppressWarnings("unchecked")
   	public List<Gateway> getActiveGatewayDevices(List<Gateway> list) {
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
    	
    	String hsql = "select gw.id, count(fx.id),'FIXTURE' as DEVICETYPE from fixture fx, gateway gw " +
    					"where gw.id = fx.gateway_id and fx.state != 'DELETED' and gw.id in " + gateway_ids.toString()
    					+ " group by gw.id union select gw.id, count(wd.id),'WDS' as DEVICETYPE from wds wd, gateway gw " +
    					"where gw.id = wd.gateway_id and wd.state != 'DELETED' and gw.id in " + gateway_ids.toString()
    					+ " group by gw.id union select gw.id, count(pl.id),'PLUGLOAD' as DEVICETYPE from plugload pl, gateway gw " +
    					"where gw.id = pl.gateway_id and pl.state != 'DELETED' and gw.id in " + gateway_ids.toString()
    					+ " group by gw.id";
        Query q = getSession().createSQLQuery(hsql.toString());
        List<Object[]> results = (List<Object[]>)q.list();
        Map<Long,Integer> map = new HashMap<Long,Integer>();
        if (results != null && !results.isEmpty()) {
        		for(Object[] a: results) {
        			Long dbGw = Long.parseLong(((BigInteger)a[0]).toString());
            		int totalDevices=Integer.parseInt(((BigInteger)a[1]).toString());
        			if(!map.containsKey(dbGw))
        			{
        				map.put(dbGw, totalDevices);
        			}else
        			{
        				Integer val = map.get(dbGw);
        				map.put(dbGw, val+totalDevices);
        			}
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
        /*if(gateway != null && gateway.getId() != null) {
        	List<Gateway> list = new ArrayList<Gateway>();
        	list.add(gateway);
        	getActiveGatewaySensors(list);
        }*/
        return gateway;

    } // end of method getGatewayByIp

    public Gateway getUEMGateway() {
        Session session = getSession();
        Gateway gateway = (Gateway) session.createCriteria(Gateway.class).add(Restrictions.eq("gatewayType", ServerConstants.UEM_GW))
                .uniqueResult();
        return gateway;

    } // end of method getGatewayByIp

    public void updateGatewayPosition(Gateway gateway) {
        // try {
        Session session = getSession();
        Gateway gw = (Gateway) session.get(Gateway.class, gateway.getId());
        gw.setXaxis(gateway.getXaxis());
        gw.setYaxis(gateway.getYaxis());
        // session.saveOrUpdate("x", gw);
        // session.saveOrUpdate("y", gw);

        // } catch (HibernateException hbe) {
        // hbe.printStackTrace();
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
    }

    public void commission(long gatewayId) {
        // try {
        Session session = getSession();
        Gateway gw = (Gateway) session.get(Gateway.class, gatewayId);
        gw.setCommissioned(true);
        // session.saveOrUpdate("commissioned", gw);
        //
        // } catch (HibernateException hbe) {
        // hbe.printStackTrace();
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
    }

    public boolean isCommissioned(long gatewayId) {
        Session session = getSession();
        Gateway gw = (Gateway) session.get(Gateway.class, gatewayId);
        return gw.isCommissioned();

        // Boolean bStatus = (Boolean) getSession().createQuery("Select commissioned from Gateway where id = :id")
        // .setLong("id", gatewayId).uniqueResult();
        // if (bStatus != null)
        // return bStatus.booleanValue();
        // return false;
    }

    public void updateGateway(Gateway gateway) {
        Session session = getSession();
        if(gateway.getWirelessEncryptKey().equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
          gateway.setWirelessEncryptKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
        }
        session.update(gateway);
        // try {
        // Session session = getSession();
        // Gateway gw = (Gateway) session.get(Gateway.class, gateway.getId());
        // gw.setFloor(gateway.getFloor());
        // gw.setBuildingId(gateway.getBuildingId());
        // gw.setCampusId(gateway.getCampusId());
        // gw.setGatewayName(gateway.getGatewayName());
        // gw.setWirelessNetworkId(gateway.getWirelessNetworkId());
        // gw.setWirelessEncryptType(gateway.getWirelessEncryptType());
        // gw.setWirelessEncryptKey(gateway.getWirelessEncryptKey());
        // gw.setEthSecEncryptType(gateway.getEthSecEncryptType());
        // gw.setEthSecKey(gateway.getEthSecKey());
        // gw.setChannel(gateway.getChannel());
        // gw.setLocation(gateway.getLocation());
        // session.saveOrUpdate("g.floor.id", gw);
        // session.saveOrUpdate("g.buildingId", gw);
        // session.saveOrUpdate("g.campusId", gw);
        // session.saveOrUpdate("location", gw);
        // session.saveOrUpdate("gatewayName", gw);
        // session.saveOrUpdate("wirelessNetworkId", gw);
        // session.saveOrUpdate("wirelessEncryptType", gw);
        // session.saveOrUpdate("wirelessEncryptKey", gw);
        // session.saveOrUpdate("ethSecEncryptType", gw);
        // session.saveOrUpdate("ethSecKey", gw);
        // session.saveOrUpdate("channel", gw);
        //
        // } catch (HibernateException hbe) {
        // hbe.printStackTrace();
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
    }

    public void updateGatewayParameters(Gateway gw) {
      if(gw.getWirelessEncryptKey().equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
        gw.setWirelessEncryptKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
      }
        try {
            Session session = getSession();
            Gateway gateway = (Gateway) session.get(Gateway.class, gw.getId());
            gateway.setGatewayName(gw.getGatewayName());
            gateway.setWirelessNetworkId(gw.getWirelessNetworkId());
            gateway.setWirelessEncryptType(gw.getWirelessEncryptType());
            gateway.setWirelessEncryptKey(gw.getWirelessEncryptKey());
            gateway.setEthSecEncryptType(gw.getEthSecEncryptType());
            gateway.setEthSecKey(gw.getEthSecKey());
            gateway.setChannel(gw.getChannel());
            if(gw.getIpAddress() != null){
            	gateway.setIpAddress(gw.getIpAddress());
            }
            session.saveOrUpdate("gatewayName", gateway);
            session.saveOrUpdate("wirelessNetworkId", gateway);
            session.saveOrUpdate("wirelessEncryptType", gateway);
            session.saveOrUpdate("wirelessEncryptKey", gateway);
            session.saveOrUpdate("ethSecEncryptType", gateway);
            session.saveOrUpdate("ethSecKey", gateway);
            session.saveOrUpdate("channel", gateway);
            if(gw.getIpAddress() != null){
            	session.saveOrUpdate("ipAddress", gateway);
            }
        } catch (HibernateException hbe) {
            hbe.printStackTrace();
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    } // end of method updateGatewayParameters

    public void saveGatewayInfo(Gateway gw) {
      if(gw.getWirelessEncryptKey().equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
        gw.setWirelessEncryptKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
      }
        Session session = getSession();
        session.update(gw);

        // try {
        // Session session = getSession();
        // Gateway gateway = (Gateway) session.get(Gateway.class, gw.getId());
        //
        // gateway.setWirelessNetworkId(gw.getWirelessNetworkId());
        // gateway.setWirelessEncryptKey(gw.getWirelessEncryptKey());
        // gateway.setDefaultGw(gw.getDefaultGw());
        // gateway.setSubnetMask(gw.getSubnetMask());
        // gateway.setWirelessRadiorate(gw.getWirelessRadiorate());
        // gateway.setChannel(gw.getChannel());
        //
        // session.saveOrUpdate("wirelessNetworkId", gateway);
        // session.saveOrUpdate("wirelessEncryptKey", gateway);
        // session.saveOrUpdate("defaultGw", gateway);
        // session.saveOrUpdate("subnetMask", gateway);
        // session.saveOrUpdate("wirelessRadiorate", gateway);
        // session.saveOrUpdate("channel", gateway);
        //
        // } catch (HibernateException hbe) {
        // hbe.printStackTrace();
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }

    } // end of method saveGatewayInfo

    /**
     * Sets no of sensors for this gateway.
     */
    public Gateway updateFields(Gateway gw) {
      if(gw.getWirelessEncryptKey().equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
        gw.setWirelessEncryptKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
      }
        Session session = getSession();
        session.update(gw);
        return gw;

        // try {
        // Session session = getSession();
        // Gateway gateway = (Gateway) session.get(Gateway.class, gw.getId());
        // gateway.setNoOfSensors(gw.getNoOfSensors());
        // session.saveOrUpdate("noOfSensors", gateway);
        //
        // } catch (HibernateException hbe) {
        // hbe.printStackTrace();
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return gw;
    } // end of method updateFields

    public void setImageUpgradeStatus(long gwId, String status) {
        Session session = getSession();
        Gateway gateway = (Gateway) session.get(Gateway.class, gwId);
        gateway.setUpgradeStatus(status);

        // SQLQuery query = getSession().createSQLQuery("update gateway set upgrade_status" +
        // " = :status where id = :id");
        // query.setLong("id", gwId);
        // query.setString("status", status);
        // query.executeUpdate();

    } // end of method setImageUpgradeStatus

    public void setVersions(Gateway gw) {
      if(gw.getWirelessEncryptKey().equals(ServerConstants.GW_DEF_WLESS_KEY_DISP_STR)) {
        gw.setWirelessEncryptKey(ServerConstants.DEF_WLESS_SECURITY_KEY);
      }
        Session session = getSession();
        session.update(gw);

        // try {
        // Session session = getSession();
        // Gateway gateway = (Gateway) session.get(Gateway.class, gw.getId());
        //
        // gateway.setApp1Version(gw.getApp1Version());
        // gateway.setApp2Version(gw.getApp2Version());
        // gateway.setBootLoaderVersion(gw.getBootLoaderVersion());
        //
        // session.saveOrUpdate("app1Version", gateway);
        // session.saveOrUpdate("app2Version", gateway);
        // session.saveOrUpdate("bootLoaderVersion", gateway);
        //
        // } catch (HibernateException hbe) {
        // hbe.printStackTrace();
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }

    } // end of method setVersions

	public List<Gateway> loadUnCommissionedGatewaysByFloorId(Long floorId) {
		  Session session = getSession();
	        List<Gateway> gatewayList = session.createCriteria(Gateway.class).add(Restrictions.eq("floor.id", floorId)).add(Restrictions.eq("commissioned", false))
	                .list();
	        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
	        	
	            return gatewayList;
	        } else {
	            return null;
	        }
	}

}
