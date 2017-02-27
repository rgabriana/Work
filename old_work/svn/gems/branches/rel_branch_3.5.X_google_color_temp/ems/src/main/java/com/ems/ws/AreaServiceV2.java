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
import javax.ws.rs.core.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.model.Area;
import com.ems.service.AreaManager;

@Controller
@Path("/org/area/v2")
public class AreaServiceV2 {
	@Resource(name = "areaManager")
    private AreaManager areaManager;
	public AreaServiceV2() {
	}
	/**
     * @param floorid
     * @return area list for the floor id
     */
	@PreAuthorize("hasAnyRole('Admin','Bacnet')")
    @Path("list/{floorid}")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Area> getAreaList(@PathParam("floorid") Long floorid) {
    	List<Area> areaResult = areaManager.getAllAreasByFloorId(floorid); 
    	List<Area> areaList = new ArrayList<Area>();
        if (areaResult != null && !areaResult.isEmpty()) {
            Iterator<Area> areaIt = areaResult.iterator();
            while (areaIt.hasNext()) {
                Area area = (Area) areaIt.next();
                Area newArea = new Area();
                newArea.setId(area.getId());
                newArea.setName(area.getName());
                newArea.setDescription(area.getDescription());
                newArea.setZoneSensorEnable(area.getZoneSensorEnable());
                newArea.setOccupancyState(null);
                areaList.add(newArea);
            }
        }
      return areaList;
    }
} // end of class AreaServiceV2
