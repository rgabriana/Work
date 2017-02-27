package com.ems.ws;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.model.ImageUpgradeDBJob;
import com.ems.model.ImageUpgradeJobList;
import com.ems.mvc.util.UserAuditLoggerUtil;
import com.ems.service.ImageUpgradeJobManager;



@Controller
@Path("/org/imageupgradejob")
public class ImageUpradeJobService {
    
    @Resource
    UserAuditLoggerUtil userAuditLoggerUtil;

    @Resource(name = "imageUpgradeJobManager")
    private ImageUpgradeJobManager imageUpgradeJobManager;
    
    @Path("list")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<ImageUpgradeDBJob> loadAllImageUpgradeJobs() {
       return imageUpgradeJobManager.loadAllImageUpgradeJobs();
    }
    
    @Path("loadImageUpgradeJobList")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public ImageUpgradeJobList loadImageUpgradeJobList (@RequestParam("data") String userdata) throws  UnsupportedEncodingException,ParseException {
    	
    	String[] input = userdata.split("&");
		StringBuffer output = new StringBuffer("{");
		int page = 0;
		//long total, records = 0;
		String orderBy = null;
		String orderWay = null;
		String query = null;
		String searchField = null;
		String searchString = null;
		Boolean bSearch = false;
		
		
		String[] params = null;

		if (input != null && input.length > 0) {
			for (String each : input) {
				String[] keyval = each.split("=", 2);
				if (keyval[0].equals("page")) {
					page = Integer.parseInt(keyval[1]);
				} else if (keyval[0].equals("userData")) {
					query = URLDecoder.decode(keyval[1], "UTF-8");
					output.append("\"" + keyval[0] + "\": \"" + query + "\"");
					params = query.split("#");
				} else if (keyval[0].equals("sidx")) {
					orderBy = keyval[1];
				} else if (keyval[0].equals("sord")) {
					orderWay = keyval[1];
				}
			}
		}
		
		
		if (params != null && params.length > 0) {
			if (params[1] != null && !"".equals(params[1])) {
				searchField = params[1];
			} else {
				searchField = null;
			}

			if (params[2] != null && !"".equals(params[2])) {
				searchString = URLDecoder.decode(params[2], "UTF-8");
			} else {
				searchString = null;
			}
			
			if (params[3] != null && !"".equals(params[3])) {
				bSearch = true;
			} else {
				bSearch = false;
			}

		}
		
    	ImageUpgradeJobList imageUpgradeJobList =  imageUpgradeJobManager.loadImageUpgradeJobList(orderBy,orderWay, bSearch, searchField, searchString,(page - 1) * ImageUpgradeJobList.DEFAULT_ROWS, ImageUpgradeJobList.DEFAULT_ROWS);
    	imageUpgradeJobList.setPage(page);
		
		if(imageUpgradeJobList.getImageUpgradeJobs() == null || imageUpgradeJobList.getImageUpgradeJobs().isEmpty()){
			imageUpgradeJobList.setImageUpgradeJobs(new ArrayList<ImageUpgradeDBJob>());
		}
		
		return imageUpgradeJobList;
	}
    

}
