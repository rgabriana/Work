package com.emcloudinstance.service;

import java.io.InputStream;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.emcloudinstance.dao.JdbcConnectionTemplate;
import com.emcloudinstance.dao.UtilDao;

@Service("databaseManager")
public class DatabaseManager {
	
	@Resource
	UtilDao utilDao;
	
	@Resource
	CloudConfigManager cloudConfigManager;
	
	@Resource
	JdbcConnectionTemplate jdbcConnectionTemplate;
	
	
	
	public boolean walLogsUpdate(final String mac, final String maxWalLogDataId, final List<String> walData){
		
		TransactionTemplate transactionTemplate = new TransactionTemplate(jdbcConnectionTemplate.getTransactionManager(mac));
		
		return (Boolean)transactionTemplate.execute(new TransactionCallback() {
			// the code in this method executes in a transactional context
		      public Object doInTransaction(TransactionStatus status) {
		    	  boolean success = true;
		  		int lengthOfBatch = walData.size();
		  		if (lengthOfBatch > 0) {
		  			for(int i=0; i <= lengthOfBatch/1000; i++){
		  				int startIndex = i*1000;
		  			    int endIndex = (i+1)* 1000 -1;
		  			    if(endIndex > lengthOfBatch){
		  			    	endIndex = lengthOfBatch -1;
		  			    }
		  			    String[] batchSql = walData.subList(startIndex, endIndex + 1).toArray(new String[1]);
		  			    utilDao.updateBatch(mac,batchSql);
		  			} 
		  		}
		  		
		  		cloudConfigManager.updateCloudConfig(mac, "lastWalSyncId", maxWalLogDataId);
		  		return success;		
		      }
		});
	}
		
	

	public boolean synchTableData(String macId, String tableName,
			InputStream otherDataStream) {
		return this.utilDao.synchTableData(macId, tableName, otherDataStream);
	}

	public Long getLastMinTableDataSynched(String macId, String tableName) {
		return this.utilDao.getLastMinTableDataSynched(macId, tableName);
	}


}
