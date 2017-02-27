package com.emscloud.ws;


import java.util.HashMap;


import javax.annotation.Resource;
import javax.ws.rs.Consumes;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;

import com.communication.types.CloudParamType;
import com.communication.types.TaskProgressStatus;
import com.communication.types.TaskStatus;
import com.communication.utils.CloudRequest;
import com.communication.utils.JsonUtil;

import com.emscloud.model.EmTasks;

import com.emscloud.service.EmTasksManager;


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
		
}
