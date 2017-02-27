package com.ems.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.Area;
import com.ems.model.PlanMap;
import com.ems.utils.ArgumentUtils;

/**
 * 
 * @author pankaj kumar chauhan
 * 
 */
@Repository("areaDao")
@Transactional(propagation = Propagation.REQUIRED)
public class AreaDao extends BaseDaoHibernate {

	//TODO no use case found. getAllAreasByFloorId exists.
    /**
     * Load Area details.
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Area collection. load only id,name,description details of area object. other details loads
     *         as null.
     */
    @SuppressWarnings("unchecked")
    public List<Area> loadAreaByFloorId(Long id) {
        Session session = getSession();
        List<Area> areaList = session.createCriteria(Area.class).add(Restrictions.eq("floor.id", id)).list();
        if (!ArgumentUtils.isNullOrEmpty(areaList)) {
            return areaList;
        } else {
            return null;
        }
        // try{
        // List<Area> results = null;
        // String hsql =
        // "Select new Area(a.id,a.name,a.description,a.floor.id,a.profileHandler.id,a.planMap.id) from Area a where a.floor.id=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, id);
        // results = q.list();
        // if(results != null && !results.isEmpty()){
        // return results;
        // }
        // }catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
    }

    public List<Area> getAllAreasByFloorId(Long floorId) {
        Session session = getSession();
        List<Area> areaList = session.createCriteria(Area.class).add(Restrictions.eq("floor.id", floorId)).list();
        if (!ArgumentUtils.isNullOrEmpty(areaList)) {
            return areaList;
        } else {
            return new ArrayList<Area>();
        }
        // Query query = getSession().createQuery("from Area where floor.id=:floorId");
        // query.setLong("floorId", floorId);
        // List<Area> areas = query.list();
        // return areas;
    }

    public List<Object[]> getAllAreasByFloorIdWithNoOfSensors(long floorId) {
  		
    	String hsql = "SELECT d.area_id, a.name, lighting_occ_count, percentage_sensors_faulty, zone_sensor_enable, count(*) " +
    			"AS total_count FROM area a, device d WHERE a.id = d.area_id AND d.floor_id = " + floorId + " GROUP BY " +
    			"d.area_id, a.name, lighting_occ_count, percentage_sensors_faulty, zone_sensor_enable";
    			
  		try {			
  			Query q = getSession().createSQLQuery(hsql);
  			List<Object[]> output = q.list();
  			return output;
  		} catch (Exception e) {
  			e.printStackTrace();
  		}		
  		return null;
  		
    } //end of method getAllAreasByFloorIdWithNoOfSensors
    
    public HashMap<Long, BigInteger> getAllAreasByFloorIdWithNoOfFaultySensors(long floorId) {
    	
    	String hsql = "SELECT area_id, count(*) AS faulty_count FROM area a, device d, fixture f WHERE a.id = d.area_id " +
    			"AND d.id = f.id AND d.floor_id = " + floorId + " AND f.last_connectivity_at < now() - interval '1 min' * " +
    			"a.sensor_faulty_time GROUP BY area_id;";
    	HashMap<Long, BigInteger> reachableAreasMap = new HashMap<Long, BigInteger>();
  		try {			
  			Query q = getSession().createSQLQuery(hsql);
  			List<Object[]> output = q.list();
  			Iterator<Object[]> iter = output.iterator();
  			while(iter.hasNext()) {
  				Object[] nextObj = iter.next();
  				reachableAreasMap.put(((BigInteger)nextObj[0]).longValue(), (BigInteger)nextObj[1]);
  			}
  			return reachableAreasMap;
  		} catch (Exception e) {
  			e.printStackTrace();
  		}		
  		return null;
  		
    } //end of method getAllAreasByFloorIdWithNoOfFaultySensors
    
    public Object[] getOccupancyStatusOfArea(long areaId) {
    	
    	String hsql = "SELECT lighting_occ_count, percentage_sensors_faulty, zone_sensor_enable, " +
    			"count(*) AS total_count, faulty_count FROM area a, device d, (SELECT count(*) AS faulty_count FROM " +
    			"fixture f, area a, device d WHERE d.id = f.id AND d.area_id = a.id AND area_id = " + areaId + " AND " +
    			"f.last_connectivity_at < now() - interval '1 min' * a.sensor_faulty_time) AS sq WHERE a.id = d.area_id " +
    			"AND area_id = " + areaId + " GROUP BY lighting_occ_count, percentage_sensors_faulty, " +
    			"zone_sensor_enable, sq.faulty_count";
    	
  		try {			
  			Query q = getSession().createSQLQuery(hsql);
  			List<Object[]> output = q.list();
  			return output.get(0);
  		} catch (Exception e) {
  			e.printStackTrace();
  		}		
  		return null;
  		
    } //end of method getOccupancyStatusOfArea

    //TODO no use case found.
    /**
     * load area details by id
     * 
     * @param id
     *            database id(primary key)
     * @return com.ems.model.Area
     */
    public Area loadArea(Long id) {
        Session session = getSession();
        Area area = (Area) session.get(Area.class, id);
        return area;

        // try {
        // List<Area> results = null;
        // String hsql =
        // "Select new Area(a.id,a.name,a.description,a.floor.id,a.profileHandler.id,a.planMap.id) from Area a where a.id=?";
        // Query q = getSession().createQuery(hsql.toString());
        // q.setParameter(0, id);
        // results = q.list();
        // if (results != null && !results.isEmpty()) {
        // return (Area) results.get(0);
        // }
        // } catch (HibernateException hbe) {
        // throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        // }
        // return null;
    }

    public Area editName(Area area) {
        Session session = getSession();
        session.update(area);
        return area;

        // getSession().createQuery("Update Area set name = :name where id = :id").setString("name", area.getName())
        // .setLong("id", area.getId()).executeUpdate();
        // return loadArea(area.getId());
    }

    //TODO not required.
    public Area updateAreaPlan(Long areaId, PlanMap planMap) {
        Session session = getSession();
        Area area = (Area) session.get(Area.class, areaId);
        area.setPlanMap(planMap);
        return area;

        // Area area = (Area) getSession().get(Area.class, areaId);
        // area.setPlanMap(planMap);
        // getSession().saveOrUpdate("planMap", area);
        // return area;
    }

	public List<Area> getAllZoneEnableAreas() {
		 Session session = getSession();
	        List<Area> areaList = session.createCriteria(Area.class).add(Restrictions.eq("zoneSensorEnable", true)).list();
	        if (!ArgumentUtils.isNullOrEmpty(areaList)) {
	            return areaList;
	        } else {
	            return null;
	        }
	}

}
