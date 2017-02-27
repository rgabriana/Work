package com.emscloud.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import com.emscloud.model.CloudBuilding;
import com.emscloud.model.CloudCampus;
import com.emscloud.model.CloudFloor;
import com.emscloud.model.EmBuilding;
import com.emscloud.model.EmCampus;
import com.emscloud.model.EmFloor;
import com.emscloud.model.EmInstance;
import com.emscloud.tree.TreeNode;
import com.emscloud.types.FacilityType;

@Repository("emInstanceDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmInstanceDao {
	static final Logger logger = Logger.getLogger(EmInstanceDao.class.getName());
	
	@Resource
    SessionFactory sessionFactory;
	
	@Resource
	CloudCampusDao		cloudCampusDao;

	@Resource
	CloudBuildingDao	cloudBuildingDao;

	@Resource
	CloudFloorDao		cloudFloorDao;

	@Resource
	EmCampusDao			emCampusDao;

	@Resource
	EmBuildingDao		emBuildingDao;

	@Resource
	EmFloorDao			emFloorDao;

	public List<EmInstance> loadAllEmInstances() {
		List<EmInstance> results = new ArrayList<EmInstance>();
		try {
            
	    	 List<EmInstance> emInstanceList = sessionFactory.getCurrentSession().createCriteria(EmInstance.class).addOrder(Order.asc("name")).list();
	    	 if (emInstanceList != null && !emInstanceList.isEmpty()) {
	 			return emInstanceList;
	 		} else {
	 			return null;
	 		}

/*	    	String hsql = "from EmInstance order by id";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	return results ;
            }*/
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
//        return emInstanceList;
	}

	public List<EmInstance> loadEmInstancesByCustomerId(long id) {
		List<EmInstance> results = new ArrayList<EmInstance>();
		try {
            
	    	 List<EmInstance> emInstanceList = sessionFactory.getCurrentSession().createCriteria(EmInstance.class)
	    			 .add(Restrictions.eq("customer.id", id))
	    			 .addOrder(Order.asc("name")).list();
	    	 if (emInstanceList != null && !emInstanceList.isEmpty()) {
	 			return emInstanceList;
	 		} else {
	 			return null;
	 		}

/*	    	String hsql = "from EmInstance order by id";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	return results ;
            }*/
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
//        return emInstanceList;
	}

	public EmInstance loadEmInstanceById(long id) {
		try {
            
	    	 EmInstance emInstance = (EmInstance)sessionFactory.getCurrentSession().createCriteria(EmInstance.class)
	    			 .add(Restrictions.eq("id", id)).uniqueResult();
	    			 

	    	 return emInstance;
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
	}

	public void saveOrUpdate(EmInstance instance) {
		sessionFactory.getCurrentSession().saveOrUpdate(instance) ;
		
	}
	
	public void deleteById(Long id)
	{
		String hsql = "delete from EmInstance where id=?";
        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
        q.setParameter(0, id);
		q.executeUpdate();
	}

	public TreeNode<FacilityType> loadEmFacilityHierarchy(long id)
	{
		EmInstance emInstance = loadEmInstanceById(id);

		TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
		rootNode.setNodeId("0");
		rootNode.setName("Root");
		rootNode.setNodeType(FacilityType.ROOT);
		
		Set<EmCampus> campusSet = emInstance.getEmCampuses();
		
		Iterator<EmCampus> campusItr = campusSet.iterator();

		while(campusItr.hasNext())
		{
			EmCampus campus = campusItr.next();
		
			TreeNode<FacilityType> campusNode = new TreeNode<FacilityType>();
			Long campusId = campus.getId();
			
		    campusNode.setNodeId(campusId.toString());
		    campusNode.setName(campus.getName());
		    campusNode.setNodeType(FacilityType.CAMPUS);
		    campusNode.setcloudFacilityId(campus.getCloudCampus().getId());
		    rootNode.addTreeNode(campusNode);
		
		    Set<EmBuilding> bldgSet = campus.getEmBuildings();
		    
		    Iterator<EmBuilding> bldgItr = bldgSet.iterator();
		    
		    while(bldgItr.hasNext())
		    {
		    	EmBuilding bldg = bldgItr.next();
		    	
		        TreeNode<FacilityType> buildingNode = new TreeNode<FacilityType>();
		        Long bldgId = bldg.getId();
		        buildingNode.setNodeId(bldgId.toString());
		        buildingNode.setName(bldg.getName());
		        buildingNode.setNodeType(FacilityType.BUILDING);
		        
		        campusNode.addTreeNode(buildingNode);
		        
		        Set<EmFloor> floorSet = bldg.getEmFloors();
		        
		        Iterator<EmFloor> floorItr = floorSet.iterator();
		        
		        while(floorItr.hasNext())
		        {
		        	EmFloor floor = floorItr.next();
		        	Long floorId = floor.getId();
		            TreeNode<FacilityType> floorNode = new TreeNode<FacilityType>();
		            floorNode.setNodeId(floorId.toString());
		            floorNode.setName(floor.getName());
		            floorNode.setNodeType(FacilityType.FLOOR);
		            
		            buildingNode.addTreeNode(floorNode);
		        }
		    }
		
		}

		return rootNode;
	}	
	
	public void saveFacilityMapping(String[] assignedFacilities) {
		for (String facility : assignedFacilities) {
			String[] facilityDetail = facility.split("_");
			String facilityType = facilityDetail[0];
			Long emId = Long.parseLong(facilityDetail[1]);
			Long cloudId = Long.parseLong(facilityDetail[2]);

			if ("campus".equalsIgnoreCase(facilityType)) {
				CloudCampus cldCampus = null;
				if(cloudId != 0)
				{
					cldCampus = (CloudCampus)cloudCampusDao.getObject(CloudCampus.class, cloudId);
				}
				EmCampus emCampus = (EmCampus) emCampusDao.getObject(EmCampus.class, emId);
				emCampus.setCloudCampus(cldCampus);
				emCampusDao.saveObject(emCampus);
			} else if ("building".equalsIgnoreCase(facilityType)) {
				CloudBuilding cldBldg = null;
				if(cloudId != 0)
				{
					cldBldg = (CloudBuilding) cloudBuildingDao.getObject(CloudBuilding.class, cloudId);
				}
				EmBuilding emBldg = (EmBuilding) emBuildingDao.getObject(EmBuilding.class, emId);
				emBldg.setCloudBuilding(cldBldg);
				emBuildingDao.saveObject(emBldg);
			} else if ("floor".equalsIgnoreCase(facilityType)) {
				CloudFloor cldFloor = null;
				if(cloudId != 0)
				{
					cldFloor = (CloudFloor)cloudFloorDao.getObject(CloudFloor.class, cloudId);
				}
				EmFloor emFloor = (EmFloor) emFloorDao.getObject(EmFloor.class, emId);
				emFloor.setCloudFloor(cldFloor);
				emFloorDao.saveObject(emFloor);
			}
		}
	}
}
