package com.emscloud.ws;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;

import com.communication.types.CloudParamType;
import com.communication.types.TaskProgressStatus;
import com.communication.types.TaskStatus;
import com.communication.utils.CloudRequest;
import com.communication.utils.JsonUtil;
import com.emscloud.model.EmInstance;
import com.emscloud.model.EmTasks;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.service.EmTasksManager;
import com.sun.jersey.multipart.FormDataParam;


@Controller
@Path("/org/emtask")
public class EmTasksService {
	
	@Resource
	EmTasksManager emTasksManager;
	@Resource
	EmInstanceManager emInstanceManager;
	
	String baseDirForLog = "/var/lib/tomcat6/Enlighted/" ;
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
	
	@Path("upload/log/{mac}")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response uploadLog(@PathParam("mac") String mac , @FormDataParam("logZip") InputStream otherDataStream) {
		Boolean fail = true ;
		
		OutputStream out = null;
		try{
			EmInstance em = emInstanceManager.loadEmInstanceByMac(mac);
			if(em != null){
				String customerDirPath = baseDirForLog+"/customers/" ;
				String emDirPath = customerDirPath + "em_" + em.getCustomer().getId() + "_" +  em.getId()+"/" ;
				String zipFilePath = emDirPath+"log_"+Calendar.getInstance().getTime().toString()+".zip" ;
				if(!new File(customerDirPath).exists())
				{
					File customerDir = new File(customerDirPath) ;
					FileUtils.forceMkdir(customerDir) ;
				}
				if(!new File(emDirPath).exists())
				{
					File emDir = new File(emDirPath) ;
					FileUtils.forceMkdir(emDir) ;
				}
				File zipFile = new File(zipFilePath) ;
				out=new FileOutputStream(zipFile);
				  byte buf[]=new byte[1024];
				  int len;
				  while((len=otherDataStream.read(buf))>0)
					  out.write(buf,0,len);
				fail=false ;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace() ;
			fail=true ;
		}
		finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(otherDataStream);
		}
		
		if(fail) {
			return Response.status(500).entity("").build();
		}
		else {
			return Response.status(200).entity("").build();
		}
	}
}
