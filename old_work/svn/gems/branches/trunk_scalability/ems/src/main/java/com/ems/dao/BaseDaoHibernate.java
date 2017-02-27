package com.ems.dao;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository("dao")
@Transactional(propagation = Propagation.REQUIRED)
public class BaseDaoHibernate {
    protected final Log log = LogFactory.getLog(getClass());

    @Resource
    SessionFactory sessionFactory;

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public void save(Object o) {
      sessionFactory.getCurrentSession().save(o);
    }
    
    public void update(Object o) {
        sessionFactory.getCurrentSession().update(o);
    }
    
    /**
     * @see com.ems.dao.hibernate.ChildObjectDaoImpl.dao.Dao#saveObject(java.lang.Object)
     */
    public Object saveObject(Object o) {
        sessionFactory.getCurrentSession().saveOrUpdate(o);
        return o;
    }

    public void saveObjectUpload(Object o) {
        sessionFactory.getCurrentSession().saveOrUpdate(o);
    }

    @SuppressWarnings("rawtypes")
	public Object getObject(Class clazz, Serializable id) {
        Object o = sessionFactory.getCurrentSession().get(clazz, id);
        if (o == null) {
            throw new ObjectRetrievalFailureException(clazz, id);
        }
        return o;
    }
    
    @SuppressWarnings("rawtypes")
	public Object getStatelessObject(Class clazz, Serializable id) {
    	StatelessSession session = sessionFactory.openStatelessSession();
        Object o = session.get(clazz, id);
        if (o == null) {
            throw new ObjectRetrievalFailureException(clazz, id);
        }
        return o;
    }
    
    @SuppressWarnings({ "rawtypes" })
    public Object loadObject(Class clazz, Serializable id) {
        Object o = sessionFactory.getCurrentSession().load(clazz, id);
        if (o == null) {
            throw new ObjectRetrievalFailureException(clazz, id);
        }
        return o;
    }

    @SuppressWarnings("rawtypes")
	public List loadAll(Class clazz) {
        return sessionFactory.getCurrentSession().createCriteria(clazz).list();

    }

    @SuppressWarnings({ "rawtypes" })
    public void removeObject(Class clazz, Serializable id) {
        sessionFactory.getCurrentSession().delete(getObject(clazz, id));
    }
    
    public void flush() {
        sessionFactory.getCurrentSession().flush();
    }
    
    public void evict(Object obj) {
        sessionFactory.getCurrentSession().evict(obj);
    }
}
