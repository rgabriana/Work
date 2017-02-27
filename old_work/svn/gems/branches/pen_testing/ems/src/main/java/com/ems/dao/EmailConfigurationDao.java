/**
 * 
 */
package com.ems.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.hvac.utils.CryptographyUtil;
import com.ems.model.EmailConfiguration;
import com.ems.util.Constants;

@Repository("emailConfigurationDao")
@Transactional(propagation = Propagation.REQUIRED)
public class EmailConfigurationDao extends BaseDaoHibernate {

	public EmailConfiguration loadEmailConfiguration() {
		List<EmailConfiguration> results = new ArrayList<EmailConfiguration>();
		try {
            
            String hsql = "from EmailConfiguration order by id";
            Query q = sessionFactory.getCurrentSession().createQuery(hsql.toString());
            results = q.list();
            if (results != null && !results.isEmpty()) {
            	return results.get(0) ;
            }
            }
         catch (HibernateException hbe) {
            throw SessionFactoryUtils.convertHibernateAccessException(hbe);
        }
        return null;
	}
	
	public Properties loadEmailConfigurationProperties(){
		final EmailConfiguration config = this.loadEmailConfiguration();
		final Properties props = new Properties();
		props.put("mail.smtp.host",config.getHost());
		props.put("mail.smtp.port",config.getPort());
		props.put("username",config.getUser());
		props.put("password", CryptographyUtil.getLocalDecryptedString(config.getPass(),Constants.ALGORITHM_LOCAL));
		props.put("mail.transport.protocol",config.getProtocol());
		props.put("mail.smtp.auth",config.getFlagAuth());
		props.put("mail.smtp.starttls.enable",config.getFlagTls());
		return props;
    	
	}
}
