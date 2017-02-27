package com.emscloud.dao;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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

    @SuppressWarnings("unchecked")
    public Object getObject(Class clazz, Serializable id) {
        // sessionFactory.getCurrentSession().clear();
        Object o = sessionFactory.getCurrentSession().get(clazz, id);

        if (o == null) {
            throw new ObjectRetrievalFailureException(clazz, id);
        }

        return o;
    }

    /*
     * @SuppressWarnings("unchecked") public List getObjects(Class clazz) { return getSession().loadAll(clazz); }
     */

    public List loadAll(Class clazz) {
        return sessionFactory.getCurrentSession().createCriteria(clazz).list();

    }

    @SuppressWarnings("unchecked")
    public void removeObject(Class clazz, Serializable id) {
        sessionFactory.getCurrentSession().delete(getObject(clazz, id));
    }
}
