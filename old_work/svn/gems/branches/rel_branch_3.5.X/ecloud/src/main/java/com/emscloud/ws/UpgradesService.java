package com.emscloud.ws ;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;


import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.communication.types.CloudParamType;
import com.communication.types.TaskProgressStatus;
import com.communication.types.TaskStatus;
import com.communication.utils.CloudRequest;
import com.communication.utils.JsonUtil;
import com.emscloud.model.EmTasks;
import com.emscloud.model.Upgrades;
import com.emscloud.service.EmTasksManager;
import com.emscloud.service.UpgradesManager;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;


@Controller
@Path("/org/upgrades")
public class UpgradesService {
	
	@Resource
	UpgradesManager upgradesManager;
	@Resource
	EmTasksManager emTasksManager;

    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public UpgradesService() {
    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    
    
    
    /**
     * Service returns the file specified by the aid to download and save, aid is the the id of the file which exist in upgrades table. 
     * @param aid
     * @return
     */
    @Path("getdebian")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDebian(String request) {
    	
		JsonUtil<CloudRequest> jsonUtil = new JsonUtil<CloudRequest>();
		CloudRequest cloudrequest = jsonUtil.getObject(request, CloudRequest.class);
		HashMap<CloudParamType, String> reqMap = cloudrequest.getNameValueMap();
		
		EmTasks emTask = emTasksManager.getEmTasksById(Long.parseLong(reqMap.get(CloudParamType.TaskId)));
		emTask.setProgressStatus(TaskProgressStatus.ImageDownloadRequested);
		emTask.setTaskStatus(TaskStatus.IN_PROGRESS);
		emTask.setNumberOfAttempts(emTask.getNumberOfAttempts() + 1);
		
		emTasksManager.saveObject(emTask);
		
    	Upgrades mUpgrades = upgradesManager.loadDebianById(Long.parseLong(reqMap.get(CloudParamType.DownloadImageId)));
    	if(mUpgrades != null) {
    		
    		String mFileLocation = mUpgrades.getLocation();    	
    		File mFile = new File(mFileLocation);    	
    		ResponseBuilder mResponseBuilder = new ResponseBuilderImpl();
    		mResponseBuilder.type("application/octet-stream");
    		mResponseBuilder.header("Content-Disposition",  "attachment; filename="+mUpgrades.getName());    	
    		byte[] rb = null;
			try {
				RandomAccessFile rFile = new RandomAccessFile(mFile, "r");
				rb = new byte[(int)rFile.length()];
				rFile.read(rb, 0, (int)rFile.length());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mResponseBuilder.entity(rb);
			return mResponseBuilder.build();
    	}
    	else {
    		return null;
    	}   
	}
    
    
}
