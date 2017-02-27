package com.emscloud.ws ;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.List;

import javax.annotation.Resource;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
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


import com.emscloud.model.Upgrades;

import com.emscloud.service.UpgradesManager;
import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;


@Controller
@Path("/org/upgradesmanagement")
public class UpgradesManagementService {
	
	@Resource
	UpgradesManager upgradesManager;
	
    private static final Logger m_Logger = Logger.getLogger("WSLogger");

    public UpgradesManagementService() {
    }

    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    
    /**
     * Returns list of all available debians with type and location
     * 
     * @return customer list
     */
    @Path("listUpgrades")
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Upgrades> getUpgradesList() {    	
    	List<Upgrades> upgrades = upgradesManager.loadallUpgrades();    	
    	if(upgrades != null && !upgrades.isEmpty()) {
    		return upgrades;
    	}
    	else {
    		return null ;
    	}    	
    }
    
    /**
     * Service returns the file specified by the aid to download and save, aid is the the id of the file which exist in upgrades table. 
     * @param aid
     * @return
     */
    @Path("downloaddebianfile")
    @POST
    @Produces("application/octet-stream")
    public Response downloadDebianFile(@FormParam("aid") Long aid) {   	
    	Upgrades mUpgrades = upgradesManager.loadDebianById(aid);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mResponseBuilder.entity(rb);
		return mResponseBuilder.build();
    	}
    	else
    		return null;
    }
    
    
}
