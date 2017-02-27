package com.emsdashboard.dao;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emsdashboard.model.GemsServer;
import com.emsdashboard.types.FacilityType;
import com.emsdashboard.utils.tree.TreeNode;

@Service("gemsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class GemsDao {

	static final Logger logger = Logger.getLogger(GemsDao.class.getName());

	@Resource
	SessionFactory sessionFactory;

	public TreeNode<FacilityType> loadGEMShierarchy() {

		TreeNode<FacilityType> rootNode = new TreeNode<FacilityType>();
		rootNode.setNodeId((long) -1);
		rootNode.setName("Root");
		rootNode.setNodeType(FacilityType.ROOT);

		TreeNode<FacilityType> gemsRoot = new TreeNode<FacilityType>();
		gemsRoot.setNodeId((long) 0);
		gemsRoot.setName("GEMS");
		gemsRoot.setNodeType(FacilityType.GEMS);

		List<GemsServer> groupList = loadGEMSData();
		if (groupList != null) {
			for (GemsServer gems : groupList) {
			    if(gems.getStatus()!='I')
			    {
    				TreeNode<FacilityType> gemsNode = new TreeNode<FacilityType>();
    				gemsNode.setNodeId(gems.getId());
    				gemsNode.setName(gems.getName());
    				gemsNode.setNodeType(FacilityType.GEMS);
    				gemsNode.setApiKey(gems.getApiKey());
    				gemsNode.setIpAddress(gems.getGemsIpAddress());
    				gemsNode.setPort(gems.getPort());
    				gemsNode.setLeaf(true);
    				gemsRoot.addTreeNode(gemsNode);
			    }
			}
			rootNode.addTreeNode(gemsRoot);
		}
		return rootNode;
	}

	/**
	 * Method will load all GEMS data present in database tables (GEMS) and
	 * return back XML list of all GEMS
	 * 
	 * @return XML list of GEMS present in table
	 */
	@SuppressWarnings("unchecked")
	public List<GemsServer> loadGEMSData() {
		Session session = sessionFactory.getCurrentSession();
		List<GemsServer> serverList = null;
		Criteria criteria = session.createCriteria(GemsServer.class);
		serverList = criteria.list();
		return serverList;
	}

	/**
	 * Method will save GEMS data into database tables (GEMS)
	 * 
	 * @param data
	 * @return void
	 */
	public void saveGEMSData(GemsServer data) {
		Session session = sessionFactory.getCurrentSession();
		GemsServer gems = new GemsServer();
		gems.setGemsIpAddress(data.getGemsIpAddress());
		gems.setPort(data.getPort());
		gems.setName(data.getName());
		gems.setStatus('I');
		session.save(gems);
	}

	/**
     * Method will update GEMS data into database tables (GEMS)
     * 
     * @param data
     * @return void
     */
    public void updateGEMSData(GemsServer data) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(data);
    }
	/**
	 * Method will save GEMS data into database tables (GEMS) : OLD 2.0 for backward compatibility
	 * 
	 * @param data
	 * @return void
	 */
	public void saveGEMSData(String data) {
		Session session = sessionFactory.getCurrentSession();
		String delimiter = ",";
		String[] gemsData;
		gemsData = data.split(delimiter);
		GemsServer gems = new GemsServer();
		gems.setGemsIpAddress(gemsData[1]);
		long port = Long.parseLong(gemsData[2]);
		gems.setPort((long) port);
		gems.setName(gemsData[0]);
		// gems.setGemsUniqueAddress(null);
		// gems.setMacId(null);
		// gems.setVersion(null);
		session.save(gems);
	}

	/**
	 * Method will delete GEMS data (GEMS of given id) from database tables
	 * (GEMS)
	 * 
	 * @param GEMS
	 *            id
	 * @return void
	 */
	public void removeGEMSData(long id) {
		Session session = sessionFactory.getCurrentSession();
		GemsServer gemSer = (GemsServer) session.load(GemsServer.class, id);
		session.delete(gemSer);
	}

	/**
	 * Method will retrieve GEMS version from database tables (GEMS)
	 * 
	 * @param GEMS
	 *            id
	 * @return void
	 */
	public String getGEMSVersion(long GEMSId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(GemsServer.class);
		criteria.add(Restrictions.eq("id", GEMSId));
		GemsServer gemSer = (GemsServer) criteria.uniqueResult();
		return gemSer.getVersion().toString();
	}

	/**
	 * load user details if id is given.
	 * 
	 * @param id
	 * @return User com.ems.model.User object
	 */
	public GemsServer loadGEMSById(Long id) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(GemsServer.class);
		criteria.add(Restrictions.eq("id", id));
		GemsServer gemSer = (GemsServer) criteria.uniqueResult();
		return gemSer;
	}

	/**
	 * load user details if id is given.
	 * 
	 * @param GemsIp
	 * @return User com.ems.model.Gems object
	 */
	public GemsServer loadGEMSByGemsIp(String gemsIp) {
		try {

			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(GemsServer.class);
			criteria.add(Restrictions.eq("gemsIpAddress", gemsIp));
			GemsServer gemSer = (GemsServer) criteria.uniqueResult();
			return gemSer;
		} catch (Exception e) {
			
			
		}
		return null ;
	}

	/**
	 * Method will save GEMS data with API Key into database tables (GEMS)
	 * 
	 * @param data
	 * @return void
	 */
	public void saveGEMSDataWithApiKey(GemsServer data) {
		Session session = sessionFactory.getCurrentSession();
		GemsServer gems = new GemsServer();
		gems.setGemsIpAddress(data.getGemsIpAddress());
		gems.setPort(data.getPort());
		gems.setName(data.getName());
		gems.setApiKey(data.getApiKey());
		gems.setStatus('I');
		session.save(gems);
	}
	
	/**
     * Method will Activate said GEMS
     * 
     * @param data
     * @return void
     */

    public void activateGEMS(Long gemID) {
        Session session = sessionFactory.getCurrentSession();
        GemsServer gems = loadGEMSById(gemID);
        gems.setStatus('A');
        session.save(gems);
    }
    
    /**
     * Method will deactivate said GEMS
     * 
     * @param data
     * @return void
     */
    public void deActivateGEMS(Long gemID) {
        Session session = sessionFactory.getCurrentSession();
        GemsServer gems = loadGEMSById(gemID);
        gems.setStatus('I');
        session.save(gems);
    }

	public String getGEMSStatus(String gemsIp) {
	        GemsServer gems = loadGEMSByGemsIp(gemsIp);  
	        return String.valueOf(gems.getStatus()) ;
		
	}
}
