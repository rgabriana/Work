package com.emscloud.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.CloudBuilding;
import com.emscloud.model.CloudCampus;
import com.emscloud.model.CloudFloor;
import com.emscloud.model.Customer;
import com.emscloud.types.FacilityType;
import com.emscloud.utils.tree.TreeNode;

@Repository("customerDao")
@Transactional(propagation = Propagation.REQUIRED)
public class CustomerDao {
	static final Logger logger = Logger.getLogger(CustomerDao.class.getName());
	
	@Resource
    SessionFactory sessionFactory;
	
	 /**
     * load customer by name
     * 
     * @param customerName Customer Name
     *            
     * @return Load Customer details by name
     */
    @SuppressWarnings("unchecked")
    public Customer loadCustomerByName(String customerName) {
        try {
            List<Customer> results = null;
            String hsql = "from Customer u where u.name=?";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            q.setParameter(0, customerName);
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	Customer customer = (Customer) results.get(0);
                  // user.getRole().getModulePermissions().size();
                   return customer;
               
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
    }

	public List<Customer> loadAllCustomers() {
		List<Customer> results = new ArrayList<Customer>();
		try {
            
            String hsql = "from Customer order by id";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
            return results ;
            }
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return results;
	}

	public void saveOrUpdate(Customer customer) {
		sessionFactory.getCurrentSession().saveOrUpdate(customer) ;
		
	}

	public Customer loadCustomerById(Long customerId) {
		 try {
	            List<Customer> results = null;
	            String hsql = "from Customer u where u.id=?";
	            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
	            q.setParameter(0, customerId);
	            results = q.list();
	            if (results != null && !results.isEmpty()) {
	            	Customer customer = (Customer) results.get(0);
	                  // user.getRole().getModulePermissions().size();
	                   return customer;
	               
	            }
	        } catch (HibernateException hbe) {
	            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
	        }
	        return null;
	}

	public TreeNode<FacilityType> loadCustomerHierarchy() {
	        TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
	        rootNode.setNodeId(0L);
	        rootNode.setName("Root");
	        rootNode.setNodeType(FacilityType.ROOT);

	        TreeNode<FacilityType> companyNode = new TreeNode<FacilityType>();
	             
	        //Get companies
	        Customer customer = loadCustomer();
	        
	        if (customer !=null) {
	            companyNode.setNodeId(customer.getId());
	            companyNode.setName(customer.getName());
	            companyNode.setNodeType(FacilityType.CUSTOMER);
	            rootNode.addTreeNode(companyNode);

	            //Get campuses
	            List<CloudCampus> campuslist =customer.getCampusList(customer.getCloudCampuses());
	            Collections.sort(campuslist, new CampusComparator());
	            
	            for (CloudCampus campus : campuslist) {
	                TreeNode<FacilityType> campusNode = new TreeNode<FacilityType>();
	                campusNode.setNodeId(campus.getId());
	                campusNode.setName(campus.getName());
	                campusNode.setNodeType(FacilityType.CAMPUS);
	                companyNode.addTreeNode(campusNode);
	                
	                //Get buildings
	                List<CloudBuilding> buildinglist = campus.getBuildingsList(campus.getCloudBuildings());
	                Collections.sort(buildinglist, new BuildingComparator());
	                
	                for (CloudBuilding building : buildinglist) {
	                    TreeNode<FacilityType> buildingNode = new TreeNode<FacilityType>();
	                    buildingNode.setNodeId(building.getId());
	                    buildingNode.setName(building.getName());
	                    buildingNode.setNodeType(FacilityType.BUILDING);
	                    campusNode.addTreeNode(buildingNode);
	                    
	                    //Get floors
	                    List<CloudFloor> floorlist = building.getFloorsList(building.getCloudFloors());
	                    Collections.sort(floorlist, new FloorComparator());
	                    
	                    for (CloudFloor floor : floorlist) {
	                        TreeNode<FacilityType> floorNode = new TreeNode<FacilityType>();
	                        floorNode.setNodeId(floor.getId());
	                        floorNode.setName(floor.getName());
	                        floorNode.setNodeType(FacilityType.FLOOR);
	                        buildingNode.addTreeNode(floorNode);
	                    }
	                }
	            }
	        }
	        return rootNode;
	    }
	
	 /**
     * 
     * 
     * @return Customer com.emscloud.model.Customer object load only id,name,address,contact details of customer other details
     *         loads as null.
     */
    @SuppressWarnings("unchecked")
    public Customer loadCustomer() {
    	Customer customer = null ;
        ArrayList<Customer> companyList = new ArrayList<Customer>() ;
        companyList = (ArrayList<Customer>) getAllCustomer() ;
        	if(companyList!=null && !companyList.isEmpty()){
        		customer = companyList.get(0);
        	}
      
        return customer ;
    }

    /**
     * 
     * @return the list of all customers
     */
    @SuppressWarnings("unchecked")
    public List<Customer> getAllCustomer() {
        List<Customer> companies = sessionFactory.getCurrentSession().createQuery("from Customer order by id").list();
        return companies;
    }
    
    public class CampusComparator implements Comparator<CloudCampus>{
        @Override
        public int compare(CloudCampus c1, CloudCampus c2) {
           //return c1.getName().compareTo(c2.getName());
            return c1.getName().toLowerCase().compareTo(c2.getName().toLowerCase());
        }     
    }
    
    public class BuildingComparator implements Comparator<CloudBuilding>{
        @Override
        public int compare(CloudBuilding b1, CloudBuilding b2) {
            return b1.getName().toLowerCase().compareTo(b2.getName().toLowerCase());
        }
    }
    
    public class FloorComparator implements Comparator<CloudFloor>{
        @Override
        public int compare(CloudFloor f1, CloudFloor f2) {
            return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
        }
    }
    
}
