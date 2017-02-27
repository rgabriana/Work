package com.ems.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Gateway;
import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("gatewayDao")
@Transactional(propagation = Propagation.REQUIRED)
public class GatewayDao extends BaseDaoHibernate {

    // public static final String GATEWAY_CONTRACTOR = "Select new Gateway(g.id," +
    // "g.gatewayName," +
    // "g.floor.id," +
    // "g.campusId," +
    // "g.buildingId," +
    // "g.uniqueIdentifierId," +
    // "g.xaxis," +
    // "g.yaxis," +
    // "g.status," +
    // "g.commissioned," +
    // "ca.name," +
    // "b.name," +
    // "f.name," +
    // "g.ipAddress, g.port, g.snapAddress, g.gatewayType, g.serialPort, " +
    // "g.channel, g.aesKey, g.macAddress, g.userName, g.password," +
    // "g.ethSecType, g.ethSecIntegrityType, g.ethSecEncryptType, g.ethSecKey, g.ethIpaddrType," +
    // "g.wirelessNetworkId, g.wirelessEncryptType, g.wirelessEncryptKey, g.wirelessRadiorate," +
    // "g.app1Version, g.app2Version, g.lastConnectivityAt, g.lastStatsRcvdTime, " +
    // "g.subnetMask, g.defaultGw, g.noOfSensors, g.upgradeStatus, g.bootLoaderVersion, g.currUptime, g.location) " +
    // "from Gateway g,Campus ca,Building b,Floor f " +
    // "where g.campusId = ca.id and g.buildingId = b.id and " +
    // "g.floor.id = f.id ";

    /**
     * Return gatways details if id given
     * 
     * @param id
     * @return com.ems.model.Gateway
     */
    public Gateway loadGateway(Long id) {
        Session session = getSession();
        Gateway gateway = (Gateway) session.get(Gateway.class, id);
        return gateway;

        // try {
        // String hsql = GATEWAY_CONTRACTOR + " and id=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, id);
        // return (Gateway) q.uniqueResult();
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
    }

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

        // try {
        // String hsql = GATEWAY_CONTRACTOR + " and g.uniqueIdentifierId=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, uid);
        // return (Gateway) q.uniqueResult();
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
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
                .add(Restrictions.eq("macAddress", macAddress)).uniqueResult();
        return gateway;

        // try {
        // String hsql = GATEWAY_CONTRACTOR + " and g.macAddress=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, macAddress);
        // q.setMaxResults(1);
        // return (Gateway) q.uniqueResult();
        // } catch (HibernateException hbe) {
        // return null;
        // }
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
        //
        // try {
        // List<Gateway> results = null;
        // String hsql = GATEWAY_CONTRACTOR + " and g.campusId=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, campusId);
        // results = q.list();
        // if (results != null && !results.isEmpty()) {
        // return results;
        // }
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
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

        // try {
        // List<Gateway> results = null;
        // String hsql = GATEWAY_CONTRACTOR + " and g.buildingId=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, buildingId);
        // results = q.list();
        // if (results != null && !results.isEmpty()) {
        // return results;
        // }
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
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

        // try {
        // List<Gateway> results = null;
        // String hsql = GATEWAY_CONTRACTOR + " and g.floor.id=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, floorId);
        // results = q.list();
        // if (results != null && !results.isEmpty()) {
        // return results;
        // }
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
    }

    @SuppressWarnings("unchecked")
    public List<Gateway> loadAllGateways() {

        Session session = getSession();
        List<Gateway> gatewayList = session.createCriteria(Gateway.class).list();
        return gatewayList;
        /*
        if (!ArgumentUtils.isNullOrEmpty(gatewayList)) {
            return gatewayList;
        } else {
            return null;
        }*/

        // List<Gateway> results = null;
        // try {
        // String hsql = GATEWAY_CONTRACTOR;
        // Query q = getSession().createQuery(hsql.toString());
        // results = q.list();
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return results;
    }

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

        // try {
        // List<Gateway> results = null;
        // String hsql = GATEWAY_CONTRACTOR + " and g.commissioned=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, false);
        // results = q.list();
        // if (results != null && !results.isEmpty()) {
        // return results;
        // }
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
    }

    public Gateway getGatewayByIp(String ip) {
        Session session = getSession();
        Gateway gateway = (Gateway) session.createCriteria(Gateway.class).add(Restrictions.eq("ipAddress", ip))
                .uniqueResult();
        return gateway;

        // Gateway gw = null;
        // try {
        // String hsql = GATEWAY_CONTRACTOR + " and ip_address = ?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, ip);
        // gw = (Gateway) q.uniqueResult();
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return gw;

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
            session.saveOrUpdate("gatewayName", gateway);
            session.saveOrUpdate("wirelessNetworkId", gateway);
            session.saveOrUpdate("wirelessEncryptType", gateway);
            session.saveOrUpdate("wirelessEncryptKey", gateway);
            session.saveOrUpdate("ethSecEncryptType", gateway);
            session.saveOrUpdate("ethSecKey", gateway);
            session.saveOrUpdate("channel", gateway);

        } catch (HibernateException hbe) {
            hbe.printStackTrace();
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
    } // end of method updateGatewayParameters

    public void saveGatewayInfo(Gateway gw) {
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

}
