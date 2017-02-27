package com.emcloudinstance.jdbcutils.rowmapper;



import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.emcloudinstance.util.DateUtil;
import com.emcloudinstance.vo.EcSyncVo;


public class EcSyncVoExtractor implements ResultSetExtractor {
	
	private Boolean zbUpdate ;

	@Override
	public Object extractData(ResultSet rs) throws SQLException,
			DataAccessException {
		EcSyncVo record = new EcSyncVo();
		record.setLevelId(rs.getLong("floorId"));
		record.setCaptureAt(DateUtil.parseString(
				rs.getString("captureAt"), "yyyy-MM-dd HH:mm"));
		record.setBaseEnergy( rs.getBigDecimal("basePowerUsed").doubleValue());
		record.setEnergy(rs.getBigDecimal("powerUsed"));
		record.setOccSavings(rs.getBigDecimal("occSavings"));
		record.setAmbientSavings(rs.getBigDecimal("ambientSavings"));
		record.setTuneupSavings(rs.getBigDecimal("tuneUpSavings"));
		record.setManualSavings(rs.getBigDecimal("manualSavings"));
		record.setSavedEnergy(rs.getBigDecimal("savedPowerUsed"));
		record.setBaseCost(rs.getDouble("baseCost"));
		record.setCost(rs.getDouble("cost"));
		record.setSavedCost(rs.getDouble("savedCost"));
		record.setMinTemp(rs.getBigDecimal("minTemp").floatValue());
		record.setAvgTemp(rs.getBigDecimal("avgTemp").floatValue());
		record.setMaxTemp(rs.getBigDecimal("maxTemp").floatValue());
		record.setMinAmb(rs.getBigDecimal("minLightLevel").floatValue());
		record.setAvgAmb(rs.getBigDecimal("avgLightLevel").floatValue());
		record.setMaxAmb(rs.getBigDecimal("maxLightLevel").floatValue());
		record.setMotionEvents(rs.getBigDecimal("totalMbits").longValue());
		record.setPrice((float) rs.getDouble("maxPrice"));
		// as they are zb update set it to true
		record.setZbUpdate(zbUpdate);
		return record;
	}

	public Boolean getZbUpdate() {
		return zbUpdate;
	}

	public void setZbUpdate(Boolean zbUpdate) {
		this.zbUpdate = zbUpdate;
	}

}
