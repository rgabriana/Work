package com.emscloud.api ;


import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.emscloud.api.vo.Fixtures;
import com.emscloud.communication.vos.Fixture;


@Controller
@Path("/test")
public class TestService {
	
   
    /**
     * Returns input value.
     * It's a sample test method as a reference for checkstyle implementation.
     * @param key name of variable
     * @return key string.
     */
    @Path("test/{param}")
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    public String test(@PathParam("param") String key) {
    	System.out.println("Testing: " + key);
    	return key;
    }
    
    
    /**
     * Returns input value.
     * It's a sample test method to check access denied for user role.
     * @param key name of variable
     * @return key string.
     */
    @Path("userrole/{param}")
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    @PreAuthorize("hasRole('ROLE_USER')")
    public String userrole(@PathParam("param") String key) {
    	System.out.println("Testing: " + key);
    	return key;
    }
    
    
    /**
     * Returns input value.
     * It's a sample test method to check access allowed for authenticated role.
     * @param key name of variable
     * @return key string.
     */
    @Path("authenticatedrole/{param}")
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED')")
    public String authenticatedrole(@PathParam("param") String key) {
    	System.out.println("Testing: " + key);
    	return key;
    }
    
    
    /**
     * Returns input value.
     * It's a sample test method to check access allowed for any of the two roles user and authenticated.
     * @param key name of variable
     * @return key string.
     */
    @Path("anyrole/{param}")
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    @PreAuthorize("hasAnyRole('ROLE_AUTHENTICATED', 'ROLE_USER')")
    public String anyrole(@PathParam("param") String key) {
    	System.out.println("Testing: " + key);
    	return key;
    }
    
    /**
     * Returns input value.
     * It's a sample test method to check access denied if both roles user and authenticated are not assigned.
     * @param key name of variable
     * @return key string.
     */
    @Path("bothrole/{param}")
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    @PreAuthorize("hasRole('ROLE_AUTHENTICATED') && hasRole('ROLE_USER')")
    public String bothrole(@PathParam("param") String key) {
    	System.out.println("Testing: " + key);
    	return key;
    }
    
    /**
     * Returns input value.
     * It's a sample test method to check if request has user and authenticated role.
     * @param key name of variable
     * @return key string.
     */
    @Path("hasrole/{param}")
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    public String hasrole(@PathParam("param") String key) {
    	System.out.println("User Role: " + hasRole("ROLE_USER"));
    	System.out.println("User Role: " + hasRole("ROLE_AUTHENTICATED"));
    	return key;
    }
    
    /**
     * Returns custom fixture list.
     * @return fixtures.
     */
    @Path("fixtures")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Fixtures getFixtures() {
    	Fixtures f = new Fixtures();
    	f.setFixture(new ArrayList<Fixture>());
    	Fixture f1 = new Fixture();
    	f1.setId(1L);
    	Fixture f2 = new Fixture();
    	f2.setId(2L);
    	f.getFixture().add(f1);
    	f.getFixture().add(f2);
    	return f;
    }
    
    /**
     * Throws exception.
     * It's a sample test method to test before and after throwing exception aspect.
     * @return key string.
     */
    @Path("exception")
    @GET
    @Produces({ MediaType.TEXT_PLAIN })
    public String throwException() {
    	Integer a = 1/0;
    	return a.toString();
    }
    
    /**
     * Returns boolean value.
     * Check if role is assigned.
     * @param role name of role
     * @return boolean if role is assigned.
     */
    @SuppressWarnings("unchecked")
	private boolean hasRole(String role) {
	  Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>)SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	  boolean hasRole = false;
	  for (GrantedAuthority authority : authorities) {
	     hasRole = authority.getAuthority().equals(role);
	     if (hasRole) {
		  break;
	     }
	  }
	  return hasRole;
	}
    
}
