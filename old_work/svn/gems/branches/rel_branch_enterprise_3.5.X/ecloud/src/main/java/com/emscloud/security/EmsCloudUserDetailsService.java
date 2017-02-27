package com.emscloud.security;

import javax.annotation.Resource;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.UserDao;
import com.emscloud.model.Users;

@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class EmsCloudUserDetailsService implements UserDetailsService {

    public static final Logger log = Logger.getLogger("EMS_DASHBOARD");

    @Resource
    UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException, DataAccessException {
        try {
            Users user = userDao.getUserByEmail(userName);

            if (user != null) {
                return new EmsCloudAuthenticatedUser(user);
            }
        } catch (NoResultException nre) {
            log.info("No user found with userName: " + userName);
        }

        return null;
    }
}
