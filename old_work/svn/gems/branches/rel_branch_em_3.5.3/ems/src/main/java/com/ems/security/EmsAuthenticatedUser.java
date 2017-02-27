package com.ems.security;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

import com.ems.model.Role;
import com.ems.model.Tenant;
import com.ems.model.User;

public class EmsAuthenticatedUser implements UserDetails {

    private static final Logger logger = Logger.getLogger(EmsAuthenticatedUser.class);

    private User user;

    public EmsAuthenticatedUser(User user) {
        if (user == null) {
            logger.info("Authenticated User needs a valid user");
        }
        this.user = user;
    }

    public Role getRole() {
        return user.getRole();
    }

    public Tenant getTenant() {
        return user.getTenant();
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
        grantedAuthorities.add(new GrantedAuthorityImpl(user.getRole().getRoleType().toString()));
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return getUserStatus();
    }

    @Override
    public boolean isAccountNonLocked() {
        return getUserStatus();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return getUserStatus();
    }

    @Override
    public boolean isEnabled() {
        return getUserStatus();
    }

    public User getUser() {
        return user;
    }

    private boolean getUserStatus() {
        boolean status = false;
        if (user != null) {
            switch (user.getStatus()) {
            case ACTIVE: {
                status = true;
                break;
            }
            case INACTIVE: {
                status = false;
                break;
            }
            }
        }
        return status;
    }

}
