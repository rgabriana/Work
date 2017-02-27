package com.emcloudinstance.service;

import java.io.InputStream;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.emcloudinstance.dao.JdbcConnectionTemplate;
import com.emcloudinstance.dao.UtilDao;

@Service("databaseManager")
public class DatabaseManager {
	private static final Logger logger = Logger.getLogger("EmCloudInstance");
	@Resource
	UtilDao utilDao;
	
	@Resource
	CloudConfigManager cloudConfigManager;
	
	@Resource
	JdbcConnectionTemplate jdbcConnectionTemplate;
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean walLogsUpdate(final String mac, final String maxWalLogDataId, final List<String> walData, TransactionTemplate transactionTemplate, final JdbcTemplate jdbcTemplate){
		
		return (Boolean)transactionTemplate.execute(new TransactionCallback() {
			// the code in this method executes in a transactional context
		      public Object doInTransaction(TransactionStatus status) {
		    	  boolean success = true;
		    		int lengthOfBatch = walData.size();
		      		if (lengthOfBatch > 0) {
		      			int batchRows = lengthOfBatch/1000;
		      			int lastRow = lengthOfBatch % 1000;
		      			logger.info(mac + " {maxwallogid: " + maxWalLogDataId
							+ ", bucketsize: " + walData.size() + ", rows(~): "
							+ batchRows + "}");
		    		    int startIndex = 0;
		    		    int endIndex = 0;
		      			for(int i=0; i < batchRows; i++){
		      				startIndex = i*1000;
		      				endIndex = startIndex + 1000;
		      				logger.debug(mac + " {idx: " + i + ", start: "
								+ startIndex + ", end: " + endIndex + "}");
		      			    String[] batchSql = walData.subList(startIndex, endIndex).toArray(new String[1]);
		      			    utilDao.updateBatch(mac,batchSql, jdbcTemplate);
		      			} 
		      			if (lastRow != 0) {
		      				startIndex = endIndex;
		      				endIndex = lengthOfBatch;
		      				logger.debug(mac + " {start: " + startIndex + ", end: "
								+ endIndex + "}");
		      			    String[] batchSql = walData.subList(startIndex, endIndex).toArray(new String[1]);
		      			    utilDao.updateBatch(mac,batchSql, jdbcTemplate);
		      			}
		      		}
			  		cloudConfigManager.updateCloudConfig(mac, "lastWalSyncId", maxWalLogDataId, jdbcTemplate);
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
