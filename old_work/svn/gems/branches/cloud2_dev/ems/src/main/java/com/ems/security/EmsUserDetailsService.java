package com.ems.security;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ems.model.User;
import com.ems.service.UserManager;

public class EmsUserDetailsService implements UserDetailsService {

    @Resource
    UserManager userManager;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        User user = userManager.loadUserByUserName(username);
        return new EmsAuthenticatedUser(user);
    }

}
