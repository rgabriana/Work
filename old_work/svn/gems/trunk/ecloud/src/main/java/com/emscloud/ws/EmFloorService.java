package com.emscloud.ws;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.service.SiteManager;
import com.emscloud.service.SppaManager;
import com.emscloud.vo.EmFloor;
import com.emscloud.vo.EmFloorList;

/**
 * @author mark.clark
 */
@Controller
@Path("/org/floor")
public class EmFloorService {

	private static final Logger logger = Logger.getLogger("WSAPI");

	@Resource
	SiteManager siteManager;

	@Resource
	SppaManager sppaManager;

	@Path("getfloorlistbysiteid/{siteId}")
	@GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public EmFloorList getEmFloorsBySiteId(@PathParam("siteId") long siteId) throws ParseException {

		logger.info("Getting a list of floors for site id: " + siteId);
		EmFloorList floorList = new EmFloorList();
		List<EmFloor> emFloors = new ArrayList<EmFloor>();
		EmInstanceList emInstanceList = siteManager.loadEmInstBySiteId(siteId, "");
		logger.debug("There are " + emInstanceList.getEmInsts().size() + " EM instances for site id: " + siteId);

		// May have more than one instance of the same EM in the EM List
		// Make sure it is only used once
		Set <Long> emIds = new HashSet<Long>();
		for (EmInstance emInstance : emInstanceList.getEmInsts()) {
			if (!emIds.contains(emInstance.getId())) {
				emIds.add(emInstance.getId());	// Remember that we have processed this EM
				List<Long> floorIds = sppaManager.getFloorsBySiteId(emInstance, siteId);
				logger.debug("There are " + floorIds.size() + " floors in EM database: " + emInstance.getDatabaseName() + " associated with site id: " + siteId);
				for (Long floor : floorIds) {
					EmFloor emFloor = new EmFloor();
					emFloor.setEmId(emInstance.getId());
					emFloor.setFloorId(floor);
					emFloors.add(emFloor);
				}
			}
		}
		if (emFloors.size() > 0) {
			floorList.setFloors(emFloors);
		}
		return floorList;
	}
}
