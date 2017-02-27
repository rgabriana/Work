package com.emcloudinstance.ws;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.emcloudinstance.service.FacilityTreeManager;
import com.emcloudinstance.util.tree.TreeNode;


@Component
@Path("/org/facilitytree/")
public class FacilityTreeService {
	
	@Resource
    FacilityTreeManager facilityTreeManager;

	@Path("getEmFacilityTree")
	@GET
	@Produces({ MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON })
	public TreeNode<String> getEmFacilityTree(@Context HttpHeaders headers) {
		String emMac = null;
		try {
			emMac = headers.getRequestHeader("em_mac").get(0);
		}
		catch (Exception e) {
		}
		TreeNode<String> tree= facilityTreeManager.getEmFacilityTree(emMac);
		return tree;
		
	}
	
}
