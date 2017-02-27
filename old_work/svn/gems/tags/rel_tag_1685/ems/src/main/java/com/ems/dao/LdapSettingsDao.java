package com.ems.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.LdapSettings;

@Repository("ldapSettingsDao")
@Transactional(propagation = Propagation.REQUIRED)
public class LdapSettingsDao extends BaseDaoHibernate
{

	public LdapSettings loadById(Long id) {
		try {
            List<LdapSettings> results = null;
            String hsql = "from LdapSettings ls where ls.id=?";
            Query q = getSession().createQuery(hsql.toString());
            q.setParameter(0, id);
            results = q.list();
            if (results != null && !results.isEmpty()) {
                return (LdapSettings) results.get(0);
            }
        } catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
	}

}
