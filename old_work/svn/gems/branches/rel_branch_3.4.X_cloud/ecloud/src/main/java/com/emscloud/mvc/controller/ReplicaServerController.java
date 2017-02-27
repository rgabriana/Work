package com.emscloud.mvc.controller;


import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emscloud.model.ReplicaServer;
import com.emscloud.service.ReplicaServerManager;
import com.emscloud.types.CloudAuditActionType;
import com.emscloud.util.CloudAuditLoggerUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Controller
@RequestMapping("/replicaserver")
public class ReplicaServerController {
	
	@Resource
	ReplicaServerManager replicaServerManager;
	
	@Resource
	CloudAuditLoggerUtil cloudAuditLoggerUtil;
	
	@RequestMapping(value = "/list.ems")
	public String listReplicaServers(Model model) {
		
		return "replicaserver/list";

	}
	
	@RequestMapping(value = "/create.ems")
    public String createReplicaServer(Model model) {
		ReplicaServer replicaServer = new ReplicaServer();
		model.addAttribute("mode","Add");
		model.addAttribute("replicaServer", replicaServer);
		return "replicaserver/details";
    }
	
	@RequestMapping("/save.ems")
    public String saveReplicaServer(ReplicaServer replicaServer) {
		
		String macId = replicaServer.getMacId();
		
		boolean isEdit = false;
		
		if (replicaServer.getId() != null ){
			isEdit = true;
		}
		 
        MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        md.update(macId.getBytes());
 
        byte byteData[] = md.digest();
 
        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
 
    	replicaServer.setUid(sb.toString());
		
		replicaServerManager.saveOrUpdate(replicaServer);
		
		if(isEdit){
			cloudAuditLoggerUtil.log("Updated Replica server: "+replicaServer.getName()+" ( Mac Id :"+macId+" )", CloudAuditActionType.Replica_Server_Updated.getName());
		}else{
			cloudAuditLoggerUtil.log("Created Replica server: "+replicaServer.getName()+" ( Mac Id :"+macId+" )", CloudAuditActionType.Replica_Server_Created.getName());
		}
				
        return "redirect:/replicaserver/list.ems";
    }
	
	@RequestMapping("/edit.ems")
    public String editReplicaServer(Model model, @RequestParam("id") Long id) {

		ReplicaServer replicaServer;
		replicaServer = replicaServerManager.getReplicaServersbyId(id);
		model.addAttribute("mode","Edit");
		model.addAttribute("replicaServer", replicaServer);
		return "replicaserver/details";
    }
	
}
