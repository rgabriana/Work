package com.emscloud.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emscloud.dao.ReplicaServerDao;
import com.emscloud.model.ReplicaServer;

@Service("replicaServerManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ReplicaServerManager {
	
	@Resource
	private ReplicaServerDao replicaServerDao;
	
	@SuppressWarnings("unchecked")
	public List<ReplicaServer> getAllReplicaServers(){
		return replicaServerDao.loadAll(ReplicaServer.class);
	}
	
	public ReplicaServer getReplicaServersbyId(Long id){
		return replicaServerDao.getReplicaServersbyId(id);
	}
	
	public void saveOrUpdate(ReplicaServer replicaServer) {		
		replicaServerDao.saveOrUpdate(replicaServer) ;	
	}
	
	public void delete(Long id)
	{
		replicaServerDao.deleteById(id);
	}
	
	public ReplicaServer getReplicaServersbyName(String name){
		return replicaServerDao.getReplicaServersbyName(name);
	}
	
	public ReplicaServer getReplicaServersbyIp(String ip){
		return replicaServerDao.getReplicaServersbyIp(ip);
	}
	
	public ReplicaServer getReplicaServersbyInternalIp(String internalIp){
		return replicaServerDao.getReplicaServersbyInternalIp(internalIp);
	}
	
	public ReplicaServer getReplicaServersbyUid(String uid){
		return replicaServerDao.getReplicaServersbyUid(uid);
	}
	
	public ReplicaServer getReplicaServersbyMacId(String macId){
		return replicaServerDao.getReplicaServersbyMacId(macId);
	}

}
