package com.emcloudinstance.jdbcutils.rowmapper;



import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class EcSyncVoMapper implements RowMapper{
	
	private Boolean zbUpdate ;

	@Override
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			EcSyncVoExtractor extractor = new EcSyncVoExtractor();
			extractor.setZbUpdate(zbUpdate);
			return extractor.extractData(rs);
	}

	public Boolean getZbUpdate() {
		return zbUpdate;
	}

	public void setZbUpdate(Boolean zbUpdate) {
		this.zbUpdate = zbUpdate;
	}

}
