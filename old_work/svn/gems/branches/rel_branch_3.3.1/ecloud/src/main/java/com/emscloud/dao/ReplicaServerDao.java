package com.emscloud.dao;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.ReplicaServer;

@Repository("replicaServerDao")
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class ReplicaServerDao extends BaseDaoHibernate{

	static final Logger logger = Logger.getLogger(ReplicaServerDao.class
			.getName());

	@Resource
	SessionFactory sessionFactory;

	public ReplicaServer getReplicaServersbyId(Long id) {
		try {
            
			ReplicaServer replicaServer = (ReplicaServer)sessionFactory.getCurrentSession().createCriteria(ReplicaServer.class)
	    			 .add(Restrictions.eq("id", id)).uniqueResult();
	    			 

	    	 return replicaServer;
           }
        catch (HibernateException hbe) {
           throw SessionFactoryUtils.convertHibernateAccessException(hbe);
       }
	}
	
	public ReplicaServer getReplicaServersbyName(String name) {
		try {
            
			ReplicaServer replicaServer = (ReplicaServer)sessionFactory.getCurrentSession().createCriteria(ReplicaServer.class)
	    			 .add(Restrictions.eq("name", name)).uniqueResult();
	    			 

	    	 return replicaServer;
           }
        catch (HibernateException hbe) {
           throw SessionFactoryUtils.convertHibernateAccessException(hbe);
       }
	}
	
	public ReplicaServer getReplicaServersbyUid(String uid) {
		try {
            
			ReplicaServer replicaServer = (ReplicaServer)sessionFactory.getCurrentSession().createCriteria(ReplicaServer.class)
	    			 .add(Restrictions.eq("uid", uid)).uniqueResult();
	    			 

	    	 return replicaServer;
           }
        catch (HibernateException hbe) {
           throw SessionFactoryUtils.convertHibernateAccessException(hbe);
       }
	}
	
	public ReplicaServer getReplicaServersbyIp(String ip) {
		try {
            
			ReplicaServer replicaServer = (ReplicaServer)sessionFactory.getCurrentSession().createCriteria(ReplicaServer.class)
	    			 .add(Restrictions.eq("ip", ip)).uniqueResult();
	    			 

	    	 return replicaServer;
           }
        catch (HibernateException hbe) {
           throw SessionFactoryUtils.convertHibernateAccessException(hbe);
       }
	}
	
	public ReplicaServer getReplicaServersbyInternalIp(String internalIp) {
		try {
            
			ReplicaServer replicaServer = (ReplicaServer)sessionFactory.getCurrentSession().createCriteria(ReplicaServer.class)
	    			 .add(Restrictions.eq("internalIp", internalIp)).uniqueResult();
	    			 

	    	 return replicaServer;
           }
        catch (HibernateException hbe) {
           throw SessionFactoryUtils.convertHibernateAccessException(hbe);
       }
	}
	
	public ReplicaServer getReplicaServersbyMacId(String macId) {
		try {
            
			ReplicaServer replicaServer = (ReplicaServer)sessionFactory.getCurrentSession().createCriteria(ReplicaServer.class)
	    			 .add(Restrictions.eq("macId", macId)).uniqueResult();
	    			 

	    	 return replicaServer;
           }
        catch (HibernateException hbe) {
           throw SessionFactoryUtils.convertHibernateAccessException(hbe);
       }
	}
	
	public void saveOrUpdate(ReplicaServer replicaServer) {
		sessionFactory.getCurrentSession().saveOrUpdate(replicaServer) ;
		
	}
	
	public void deleteById(Long id)
	{
		String hsql = "delete from ReplicaServer where id=?";
        Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
        q.setParameter(0, id);
		q.executeUpdate();
	}

}
