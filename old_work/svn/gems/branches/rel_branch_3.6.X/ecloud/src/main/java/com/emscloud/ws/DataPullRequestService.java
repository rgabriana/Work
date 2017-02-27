package com.emscloud.ws;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.communication.types.DataPullRequestStateType;
import com.emscloud.communication.adaptor.CloudAdapter;
import com.emscloud.constant.ReplicaServerWebserviceUrl;
import com.emscloud.model.DataPullRequest;
import com.emscloud.model.EmInstance;
import com.emscloud.service.DataPullRequestManager;
import com.emscloud.service.EmInstanceManager;
import com.emscloud.vo.DataPullRequestList;

@Controller
@Path("/org/datapullrequest")
public class DataPullRequestService {
	
	@Resource
	DataPullRequestManager dataPullRequestManager; 
	
	@Resource
	CloudAdapter cloudAdapter;
	
	@Resource
	EmInstanceManager emInstanceManager;
	
	@Path("getdata/{taskId}")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
	@PreAuthorize("hasAnyRole('Admin')")
    public Response getData(@PathParam("taskId") Long id) {
		
		if(id != null) {
			DataPullRequest d = dataPullRequestManager.getDataPullRequestById(id);
			if(d != null && d.getState().compareTo(DataPullRequestStateType.Successful) == 0) {
				final EmInstance em = d.getEm();
				final Long taskId = id.longValue();
				if(em != null) {
					try {
						StreamingOutput fileStream =  new StreamingOutput() {
				            @Override
				            public void write(OutputStream output) {
				            	InputStream is = null; 
				                try {
				                	is = cloudAdapter.downloadFile(em, ReplicaServerWebserviceUrl.GET_DATA_PULL_REQUEST_DATA + taskId);
				                	if(is != null) {
				                		IOUtils.copy(is, output);
				                	}
				                } 
				                catch (Exception e) {
				                    e.printStackTrace();
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
			}
		}
		return null;
		
		
	}
	
	@Path("cancel/requestId/{requestId}")
	@GET
	@Produces({ MediaType.TEXT_PLAIN})
	@PreAuthorize("hasAnyRole('Admin')")
	public String cancelDataPullRequest(@PathParam("requestId") Long requestId) {
		return dataPullRequestManager.purgeRequest(dataPullRequestManager.getDataPullRequestById(requestId), "cancel");
	}
	
	@Path("delete/requestId/{requestId}")
	@GET
	@Produces({ MediaType.TEXT_PLAIN})
	@PreAuthorize("hasAnyRole('Admin')")
	public String deleteDataPullRequest(@PathParam("requestId") Long requestId) {
		return dataPullRequestManager.purgeRequest(dataPullRequestManager.getDataPullRequestById(requestId), "delete");
	}
	
	
	@Path("loaddata")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@PreAuthorize("hasAnyRole('Admin')")
    public DataPullRequestList loaddata(@RequestParam("data") String userdata) throws UnsupportedEncodingException,ParseException {

		String[] input = userdata.split("&");
		int page = 0;
		String orderBy = null;
		String orderWay = null;
		Long customerId = null;

		if (input != null && input.length > 0) {
			for (String each : input) {
				String[] keyval = each.split("=", 2);
				if (keyval[0].equals("page")) {
					page = Integer.parseInt(keyval[1]);
				} else if (keyval[0].equals("sidx")) {
					orderBy = keyval[1];
				} else if (keyval[0].equals("sord")) {
					orderWay = keyval[1];
				}
				else if (keyval[0].equals("customerId")) {
					customerId = Long.parseLong(keyval[1]);
			}
			}
		}	
		
		if(customerId != null) {
	    	DataPullRequestList list = dataPullRequestManager.loadDataByCustomerId(customerId, orderBy,orderWay,(page - 1) * DataPullRequestList.DEFAULT_ROWS, DataPullRequestList.DEFAULT_ROWS);
	    	list.setPage(page);
			return list;
		}
		return new DataPullRequestList();
    }

}
