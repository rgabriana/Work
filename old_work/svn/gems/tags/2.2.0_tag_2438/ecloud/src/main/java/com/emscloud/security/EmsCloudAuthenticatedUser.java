package com.emscloud.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.emscloud.model.Users;
import com.emscloud.types.Status;

public class EmsCloudAuthenticatedUser implements UserDetails {

    /**
     * 
     */
    private static final long serialVersionUID = -9113309270869030578L;
    private Users user;

    public EmsCloudAuthenticatedUser(Users user) {
        if (user == null) {
            throw new IllegalArgumentException("Authenticated User needs a valid user");
        }
        this.user = user;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return null;
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
        boolean nonExpired = false;
        Status status = user.getStatus();

        switch (status) {
        case A: {
            nonExpired = true;
            break;
        }
        }
        return nonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        boolean notLocked = false;
        Status status = user.getStatus();

        switch (status) {
        case A: {
            notLocked = true;
            break;
        }
        }
        return notLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        boolean notExpired = false;
        Status status = user.getStatus();

        switch (status) {
        case A: {
            notExpired = true;
            break;
        }
        }
        return notExpired;
    }

    @Override
    public boolean isEnabled() {
        boolean enabled = false;
        Status status = user.getStatus();

        switch (status) {
        case A: {
            enabled = true;
            break;
        }
        }
        return enabled;
    }

}
