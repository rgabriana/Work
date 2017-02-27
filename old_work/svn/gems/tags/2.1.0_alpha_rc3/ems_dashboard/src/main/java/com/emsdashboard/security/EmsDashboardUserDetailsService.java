package com.emsdashboard.security;

import javax.annotation.Resource;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emsdashboard.dao.UserDao;
import com.emsdashboard.model.User;

@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class EmsDashboardUserDetailsService implements UserDetailsService {

    public static final Logger log = Logger.getLogger(EmsDashboardUserDetailsService.class);

    @Resource
    UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException, DataAccessException {
        try {
            User user = userDao.getUserByEmail(userName);

            if (user != null) {
                return new EmsDashBoardAuthenticatedUser(user);
            }
        } catch (NoResultException nre) {
            log.info("No user found with userName: " + userName);
        }

        return null;
    }
}
