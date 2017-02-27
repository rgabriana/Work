package com.ems.ws;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.ems.model.SwitchFixtures;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.SwitchManager;
import com.ems.types.UserAuditActionType;
import com.ems.ws.util.Response;


@Controller
@Path("/org/switchfixtures")
public class SwitchFixtureService {
	
	@Resource
	UserAuditLoggerUtil userAuditLoggerUtil;
	
	@Resource(name = "switchManager")
	private SwitchManager switchManager;
	
	
	/**
	 * Return SwitchFixtures list based on Switch Id
	 * 
	 * @param switchId
	 *            switch unique identifier
	 * @return SwitchFixtures list for the selected switch
	 */
	@Path("/list/sid/{switchid}")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<SwitchFixtures> loadSwitchFixturesBySwitchId(@PathParam("switchid") long switchId) {
		return switchManager.loadSwitchFixturesBySwitchId(switchId);
	}
	
	/**
	 * Remove Switch Fixture mapping entry
	 * 
	 * @param id
	 * 			SwitchFixture unique identifier
	 */
	@Path("delete/{id}")
	@GET
	public Response deleteSwitchFixture(@PathParam("id") long id){
		switchManager.deleteSwitchFixture(id);
		userAuditLoggerUtil.log("Delete switch fixture with id: " + id, UserAuditActionType.Switch_Fixture_Update.getName());
		return new Response();
	}
	
	/**
	 * Save SwitchFixtures mapping
	 * 
	 * @param switchFixture
	 * 				list of SwitchFixture
	 * 				<switchFixturess><switchFixtures><id></id><fixtureid>131</fixtureid><switchid>115</switchid></switchFixtures></switchFixturess>
	 * 
	 * @return
	 */
	@Path("saveswitchfixture")
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response saveSwitchFixtures(List<SwitchFixtures> switchFixtures) {
		switchManager.saveSwitchFixturesList(switchFixtures);
		userAuditLoggerUtil.log("Save switch fixtures list", UserAuditActionType.Switch_Fixture_Update.getName());
		return new Response();
	}
	
}
