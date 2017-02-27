package com.emscloud.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.model.EmTasksUUID;

@Repository("emTasksUUIDDao")
@Transactional(propagation = Propagation.REQUIRED,readOnly=false)
public class EmTasksUUIDDao extends BaseDaoHibernate{
	
	public EmTasksUUID getEmTasksUUIDById(Long id){        
        Object obj = getObject(EmTasksUUID.class, id);
    	if(obj == null )
    		return null;
    	else
    		return (EmTasksUUID)obj;
    }

	public EmTasksUUID saveOrUpdate(EmTasksUUID emTasksUUID) {
		sessionFactory.getCurrentSession().saveOrUpdate(emTasksUUID);
		return emTasksUUID;
	}

}
