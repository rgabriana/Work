package com.ems.service;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.LdapSettingsDao;
import com.ems.dao.SystemConfigurationDao;
import com.ems.model.LdapSettings;
import com.ems.model.SystemConfiguration;

@Service("ldapSettingsManager")
@Transactional(propagation = Propagation.REQUIRED)
public class LdapSettingsManager {

	@Resource
    private LdapSettingsDao ldapSettingsDao;
	
	public void delete(Long id) {
		ldapSettingsDao.removeObject(LdapSettings.class, id);
    }


    public LdapSettings save(LdapSettings ldapSettings) {
        return (LdapSettings) ldapSettingsDao.saveObject(ldapSettings);
    }
    
    public LdapSettings update(LdapSettings ldapSettings) {
        return (LdapSettings) ldapSettingsDao.saveObject(ldapSettings);
    }
    
    public LdapSettings loadById(Long id) {
    	 return ldapSettingsDao.loadById(id);
    }
 
}
