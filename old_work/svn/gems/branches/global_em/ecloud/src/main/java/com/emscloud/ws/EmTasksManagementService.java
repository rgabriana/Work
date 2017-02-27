package com.emscloud.ws;

import java.text.ParseException;

import java.util.List;

import javax.annotation.Resource;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.emscloud.model.EmTasks;
import com.emscloud.model.EmTasksList;
import com.emscloud.service.EmTasksManager;
import com.emscloud.util.UTCConverter;

@Controller
@Path("/org/emtaskmanagement")
public class EmTasksManagementService {
	
	@Resource
	EmTasksManager emTasksManager;
	
	@Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<EmTasks> getEmTasksList() {
    	List<EmTasks> emTaskList = emTasksManager.getEmTasksList();
    	
    	if(emTaskList != null && !emTaskList.isEmpty())
    	{
    		return emTaskList;
    	}
    	return null ;
    }
	
	@Path("loadEmTasksByEmInstanceId/{emInstanceId}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmTasksList loadEmTasksListByEmInstanceId(
    		@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway,@PathParam("emInstanceId") long emInstanceId ) throws ParseException {
    	
    	
    	EmTasksList oEmTasksList = emTasksManager.loadEmTasksByEmInstanceId(orderway, (page - 1) * EmTasksList.DEFAULT_ROWS, EmTasksList.DEFAULT_ROWS,emInstanceId);
    	oEmTasksList.setPage(page);
		List<EmTasks> emTasksList = oEmTasksList.getEmTasks();
		
		if(emTasksList != null && !emTasksList.isEmpty()){
			for(EmTasks emTasks :emTasksList){
				
				emTasks.setUtcStartTime(UTCConverter.getUTCTime(emTasks.getStartTime()));
				
			}
		}
		
		oEmTasksList.setEmTasks(emTasksList);
		return oEmTasksList;
    }
	
	@Path("loadEmTasks")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public EmTasksList loadEmTasksList(
    		@FormParam("page") Integer page,
			@FormParam("sidx") String orderby,
			@FormParam("sord") String orderway) throws ParseException {
    	
    	
    	EmTasksList oEmTasksList = emTasksManager.loadEmTasks(orderway, (page - 1) * EmTasksList.DEFAULT_ROWS, EmTasksList.DEFAULT_ROWS);
    	oEmTasksList.setPage(page);
		List<EmTasks> emTasksList = oEmTasksList.getEmTasks();
		oEmTasksList.setEmTasks(emTasksList);
		return oEmTasksList;
    }
		
}
