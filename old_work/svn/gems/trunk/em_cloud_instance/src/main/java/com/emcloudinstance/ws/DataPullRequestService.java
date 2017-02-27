package com.emcloudinstance.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.stereotype.Component;

import com.communication.types.DataPullRequestStateType;
import com.emcloudinstance.service.DataPullRequestManager;
import com.emcloudinstance.util.DatabaseUtil;
import com.emcloudinstance.util.SchedulerManager;


@Component
@Path("/org/datapullrequest")
public class DataPullRequestService {
	
	final Logger logger = Logger.getLogger(DataPullRequestService.class.getName());
	
	@Resource
	DatabaseUtil databaseUtil;
	
	@Resource
	DataPullRequestManager dataPullRequestManager;
	
	
	@Path("getdata/{taskId}")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getData(@PathParam("taskId") Long id) {
		String path = "/var/lib/tomcat6/Enlighted/dataPullRequests";
		final File mFile = new File(path + "/" + id + "/" + id + ".gz");
		
		if(mFile.exists()) {
			try {
				StreamingOutput fileStream =  new StreamingOutput() {
		            @Override
		            public void write(OutputStream output) {
		            	FileInputStream is = null; 
		                try {
		                	is = new FileInputStream(mFile);
		                	if(is != null) {
		                		IOUtils.copy(is, output);
		                	}
		                } 
		                catch (Exception e) {
		                	logger.error(e.getMessage(), e);
		                }
		                finally {
		                	if(is != null) {
		                		try {
		                			is.close();
		                		}
		                		catch (Exception e) {
		                			e.printStackTrace();
		                		}
		                	}
		                	if(output != null) {
		                		try {
		                			output.close();
		                		}
		                		catch (Exception e) {
		                			e.printStackTrace();
		                		}
		                	}
		                }
		            }
		        };
		        return Response
		                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
		                .header("content-disposition","attachment; filename = " + id + ".gz")
		                .build();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
		}
    	return null;
	}
	
	
	
	@Path("canceldelete/{op}/{taskId}")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String canceldelete(@PathParam("op") String op, @PathParam("taskId") Long id) {
		
		if(op != null && ("cancel".equals(op) || "delete".equals(op))) {
			logger.info("Starting " + op + " operation for data pull request with id = " + id);
			String jobId = "DataPullJob";
			boolean cleanUp = true;
			try {
				JobKey jk = new JobKey(jobId, SchedulerManager.getInstance().getScheduler().getSchedulerName());
				if(SchedulerManager.getInstance().getScheduler().checkExists(jk)) {
					JobDetail jd = SchedulerManager.getInstance().getScheduler().getJobDetail(jk);
					if (jd != null) {
						JobDataMap map = jd.getJobDataMap();
						if(map != null) {
							logger.info("Current running data pull request for Id = " + map.getString("taskId"));
							if(map.getString("taskId").equals(id.toString())) {
								databaseUtil.dropDatabase("datapulldb");
								cleanUp = SchedulerManager.getInstance().getScheduler().deleteJob(
										new JobKey(jobId, SchedulerManager.getInstance().getScheduler().getSchedulerName()));
								
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
				cleanUp = false;
			}
			
			if(cleanUp && op.equals("delete")) {
				File f = new File("/var/lib/tomcat6/Enlighted/dataPullRequests/" + id );
				if(f.exists()) {
					try {
						FileUtils.forceDelete(f);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						cleanUp = false;
					}
				}
			}
			
			if(cleanUp) {
				if(op.equals("cancel")) {
					cleanUp = dataPullRequestManager.updateDataPullJobState(id, DataPullRequestStateType.Cancelled);
				}
				else {
					cleanUp = dataPullRequestManager.updateDataPullJobState(id, DataPullRequestStateType.Deleted);
				}
			}
			return cleanUp ? "S" : "F";
		}
		
    	return "F";
	}

}
