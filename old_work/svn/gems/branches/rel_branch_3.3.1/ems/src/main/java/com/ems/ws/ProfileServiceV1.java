/**
 * 
 */
package com.ems.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.ems.cache.FixtureCache;
import com.ems.model.Fixture;
import com.ems.model.Groups;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.FixtureManager;
import com.ems.service.GroupManager;
import com.ems.vo.Profile;

/**
 * @author yogesh
 * 
 */
@Controller
@Path("/org/profile/v1")
public class ProfileServiceV1 {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
  @Resource(name = "fixtureManager")
  private FixtureManager fixtureManager;
  @Resource(name = "groupManager")
  private GroupManager groupManager;

  private static final Logger m_Logger = Logger.getLogger("WSLogger");
  
  public ProfileServiceV1() {
  	
  }

  @Context
  UriInfo uriInfo;
  @Context
  Request request;
     
  @Path("list/{fixture_id}")
  @GET
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public List<Profile> getFixtureApplicableProfiles(@PathParam("fixture_id") Long fixtureId) {
  	
  	Fixture fixture = null;
  	if(FixtureCache.getInstance().getDevice(fixtureId) != null) {
  		fixture = FixtureCache.getInstance().getDevice(fixtureId).getFixture();
  	}
  	if(fixture == null) {
  		fixture = fixtureManager.getFixtureById(fixtureId);
  	}
  	Long groupId = fixture.getGroupId();
  	List<Groups> derivedGroups = groupManager.getAllDerivedGroups(groupId);
  	ArrayList<Profile> profilesList = new ArrayList<Profile>();
  	Iterator<Groups> iter = derivedGroups.iterator();
  	while(iter.hasNext()) {
  		Groups group = iter.next();
  		Profile profile = new Profile();
  		profile.setName(group.getName());
  		profile.setGroupId(group.getId());
  		profilesList.add(profile);
  	}
  	return profilesList;
  	
  } //end of method getFixtureApplicableProfiles
  
} //end of class ProfileServiceV1
