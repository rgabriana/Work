package com.emscloud.ws;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.communication.types.CloudParamType;
import com.communication.types.TaskProgressStatus;
import com.communication.types.TaskStatus;
import com.communication.utils.CloudRequest;
import com.communication.utils.JsonUtil;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmInstanceList;
import com.emscloud.model.EmTasks;
import com.emscloud.model.EmTasksList;
import com.emscloud.service.EmTasksManager;
import com.emscloud.util.UTCConverter;

@Controller
@Path("/org/emtask")
public class EmTasksService {
	
	@Resource
	EmTasksManager emTasksManager;
	
	@Path("update")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String updateTask(String request) {
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();
		
		EmTasks emTask = emTasksManager.getEmTasksById(Long.parseLong(reqMap.get(CloudParamType.TaskId)));
		
		for(CloudParamType key: reqMap.keySet()) {
			switch (key) {
				case TaskAttempts : {
					emTask.setNumberOfAttempts(Integer.parseInt(reqMap.get(CloudParamType.TaskAttempts)));
					break;
				}
				case TaskProgressStatus: {
					emTask.setProgressStatus(TaskProgressStatus.valueOf(reqMap.get(CloudParamType.TaskProgressStatus)));
					break;
				}
				case TaskStatus: {
					emTask.setTaskStatus(TaskStatus.valueOf(reqMap.get(CloudParamType.TaskStatus)));
					break;
				}
				default : {
				}
			}
		}
		
		emTasksManager.saveObject(emTask);
		return "S";
	}
	
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
		
		for(EmTasks emTasks :emTasksList){
			
			emTasks.setUtcStartTime(UTCConverter.getUTCTime(emTasks.getStartTime()));
			
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
