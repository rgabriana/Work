package com.emcloudinstance.dao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.util.Log;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.emcloudinstance.util.DiagnosticsConstant;
import com.emcloudinstance.vo.EmHealthDataVO;
import com.emcloudinstance.vo.EnergyConsumptionHourlyVO;
import com.emcloudinstance.vo.FixtureDiagnosticsVO;
import com.emcloudinstance.vo.FixtureHealthDataVO;
import com.emcloudinstance.vo.GatewayHealthDataVO;

@Repository("monitoringDao")
public class MonitoringDao extends AbstractJdbcDao{
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	/**
	 * This fucntion copies the last connectivity from fixture table to fixture_diagnostics table
	 * @param mac
	 */
	public void updateFixtureConnectivityAndState(String mac){
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);	
		jdbcTemplate.update("update fixture_diagnostics set last_connectivity_at  = fixture.last_connectivity_at, "
				+ " state = fixture.state from fixture where fixture_diagnostics.fixture_id = fixture.id and fixture.state = 'COMMISSIONED'");
	}
	
	/**
	 * This finds the status of fixture based on the data received
	 * @param mac
	 */
	public void updateFixtureDiagnostic(String mac){
		try{
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);	
		
		Date latestTimeStamp = null;
		String queryForMaxTimeStamp = "select max(capture_at) from energy_consumption_hourly";
		latestTimeStamp = jdbcTemplate.queryForObject(queryForMaxTimeStamp, Date.class);
		
		//Let's check if the new hourly data has arrived in energy consumption hourly table
		String queryForLastDiagnosticsValue = "select val from cloud_config where name = 'diagnostics.last_capture_at'";
		String captureTimeStampValue = jdbcTemplate.queryForObject(queryForLastDiagnosticsValue, String.class);
		Date captureTimeStamp = null;
		if(captureTimeStampValue != null){
			try {
				captureTimeStamp = sdf.parse(captureTimeStampValue);
				
			} catch (ParseException e) {
				Log.error(e.getMessage());
			}
		}else{
			captureTimeStamp = latestTimeStamp;
		}
		
		//Let's update fixture connectivity
		updateFixtureConnectivityAndState(mac);
		
		if(captureTimeStamp != null && latestTimeStamp.compareTo(captureTimeStamp) > 0 ){
			
		}else{
			return;
		}
	
		//Ok we have to do diagnostics now
		//Let's get the fixture list with diagnostics records
		int hour = latestTimeStamp.getHours();
		String queryForDiagnostics = " select f.id as fixtureId, fd.id as fixtureDiagnosticsId, fdr.id as fixtureDiagnosticsReferenceId, " +
									 " fdr.hour_of_day as hourofDay, fdr.power_used_average as powerUsedAverage,fdr.power_used_variance as powerUsedVariance " +
									 "	from Fixture f left join Fixture_Diagnostic_Reference fdr on fdr.fixture_id = f.id and fdr.hour_of_day =  "+ hour +
									 "	left join Fixture_Diagnostics fd on f.id = fd.fixture_id where f.state = 'COMMISSIONED' " +
									"	order by f.id ";
		
		List<FixtureDiagnosticsVO> fixtureDiagnostisVOList =jdbcTemplate.query(queryForDiagnostics, new RowMapper<FixtureDiagnosticsVO>(){

			@Override
			public FixtureDiagnosticsVO mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				FixtureDiagnosticsVO fixtureDiagnosticsVO = new FixtureDiagnosticsVO();
				fixtureDiagnosticsVO.setFixtureId(rs.getLong("fixtureId"));
				fixtureDiagnosticsVO.setFixtureDiagnosticReferenceId(rs.getLong("fixtureDiagnosticsReferenceId"));
				fixtureDiagnosticsVO.setFixtureDiagnosticsId(rs.getLong("fixtureDiagnosticsId"));
				fixtureDiagnosticsVO.setHourOfDay(rs.getInt("hourofDay"));
				fixtureDiagnosticsVO.setPowerUsedAverage(rs.getDouble("powerUsedAverage"));
				fixtureDiagnosticsVO.setPowerUsedVariance(rs.getDouble("powerUsedVariance"));
				return fixtureDiagnosticsVO;
			}
			
		});
		
		String queryForEnergyConsumption = " select id as id, fixture_id as fixtureId, capture_at as captureAt, power_used as powerUsed " + 
		                                   " from energy_consumption_hourly ech where ech.capture_at = '" + latestTimeStamp + "'";
		
		List<EnergyConsumptionHourlyVO> energyConsumptionHourlyList = jdbcTemplate.query(queryForEnergyConsumption, new RowMapper<EnergyConsumptionHourlyVO>(){

			@Override
			public EnergyConsumptionHourlyVO mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				EnergyConsumptionHourlyVO energyConsumptionHourlyVO = new EnergyConsumptionHourlyVO();
				energyConsumptionHourlyVO.setId(rs.getLong("id"));
				energyConsumptionHourlyVO.setFixtureId(rs.getLong("fixtureId"));
				energyConsumptionHourlyVO.setCaptureAt(rs.getDate("captureAt"));
				energyConsumptionHourlyVO.setPowerUsed(rs.getDouble("powerUsed"));
				return energyConsumptionHourlyVO;
			}});
		
		Map<Long,EnergyConsumptionHourlyVO> ecHourlyMap = new HashMap<Long,EnergyConsumptionHourlyVO>();
		for(EnergyConsumptionHourlyVO vo: energyConsumptionHourlyList){
			ecHourlyMap.put(vo.getFixtureId(), vo);			
		}
		
		for(FixtureDiagnosticsVO fixtureDiagnosticsVO:fixtureDiagnostisVOList){
			//Let's get the new average and variance
			String queryForDiagnosticsReference = " Select avg(power_used) as powerUsedAvg, variance(power_used) as powerUsedVariance from energy_consumption_hourly " +
			                                      " where fixture_id = " + fixtureDiagnosticsVO.getFixtureId() + " and " + " extract(hour from(capture_at)) =  " + hour;			
			Map diagnosticsReferenceMap = jdbcTemplate.queryForMap(queryForDiagnosticsReference);
			BigDecimal avgpowerUsed =  (diagnosticsReferenceMap.get("powerUsedAvg")!=null?((BigDecimal)diagnosticsReferenceMap.get("powerUsedAvg")):new BigDecimal(0)) ;
			BigDecimal powerUsedVariance = (diagnosticsReferenceMap.get("powerUsedVariance")!=null?((BigDecimal)diagnosticsReferenceMap.get("powerUsedVariance")):new BigDecimal(0));
			fixtureDiagnosticsVO.setPowerUsedAverage(avgpowerUsed.doubleValue());
			fixtureDiagnosticsVO.setPowerUsedVariance(powerUsedVariance.doubleValue());
			
			
			//Let's check if the reference row is there, if not let's create one
			if(fixtureDiagnosticsVO.getFixtureDiagnosticReferenceId() != null && fixtureDiagnosticsVO.getFixtureDiagnosticReferenceId() > 0){
				String queryForUpdateOfFixtureDiagnosticReferenceRow = "update fixture_diagnostic_reference set power_used_average = " + String.valueOf(avgpowerUsed.doubleValue()) +
					       " ,power_used_variance = " + String.valueOf(powerUsedVariance.doubleValue());
			    jdbcTemplate.execute(queryForUpdateOfFixtureDiagnosticReferenceRow); 				
			}else{
				String queryForFixtureDiagnosticReferenceRow = "insert into fixture_diagnostic_reference(id, fixture_id,hour_of_day,power_used_average,power_used_variance) " +
		                 " values( nextval('fixture_diagnostic_reference_seq')," + fixtureDiagnosticsVO.getFixtureId() +"," + hour +","+ String.valueOf(avgpowerUsed.doubleValue())+"," + String.valueOf(powerUsedVariance.doubleValue()) + ")";
		    	jdbcTemplate.execute(queryForFixtureDiagnosticReferenceRow);  
		    	
		    	//Let's get the fixture diagnostics reference id
		    	String queryForFixtureDiagnosticsRefernce =  " select fdr.id as fixtureDiagnosticsReferenceId " +
						 " 	from Fixture_Diagnostic_Reference fdr where fdr.fixture_id = " + fixtureDiagnosticsVO.getFixtureId() +" and fdr.hour_of_day =  "+ hour ;
		    	Long fdrId = jdbcTemplate.queryForLong(queryForFixtureDiagnosticsRefernce);
		    	fixtureDiagnosticsVO.setFixtureDiagnosticReferenceId(fdrId);
			}
			
			//Let's compare the value and set the results in fixture diagnostics
			EnergyConsumptionHourlyVO ecHourlyVO = ecHourlyMap.get(fixtureDiagnosticsVO.getFixtureId());
			
			if(ecHourlyVO != null){
				Double powerUsedAvg = fixtureDiagnosticsVO.getPowerUsedAverage();
				Double stdDeviation = Math.pow(powerUsedVariance.doubleValue(), 0.5);
				String status =  DiagnosticsConstant.normal;
				
				if(ecHourlyVO.getPowerUsed() <= (powerUsedAvg + 2*stdDeviation) && ecHourlyVO.getPowerUsed() >= (powerUsedAvg - 2*stdDeviation)){
					status = DiagnosticsConstant.normal;
				}else if(ecHourlyVO.getPowerUsed() <= (powerUsedAvg + 3*stdDeviation) && ecHourlyVO.getPowerUsed() >= (powerUsedAvg - 3*stdDeviation)){
					status = DiagnosticsConstant.high;
				}else{
					status = DiagnosticsConstant.critical;
				}
				
				if(fixtureDiagnosticsVO.getFixtureDiagnosticsId() != null && fixtureDiagnosticsVO.getFixtureDiagnosticsId() > 0){
					String queryToUpdateFixtureDiagnosticsRow = " update fixture_diagnostics set fixture_diagnostic_reference_id = " + fixtureDiagnosticsVO.getFixtureDiagnosticReferenceId() +
							            " , energy_consumption_hourly_id = " + ecHourlyVO.getId() + " , power_used_status = '" + status +"'";
					jdbcTemplate.execute(queryToUpdateFixtureDiagnosticsRow);
				}else{
					String queryToCreateFixtureDiagnosticsRow = "insert into fixture_diagnostics(id, fixture_id, fixture_diagnostic_reference_id, energy_consumption_hourly_id, power_used_status) " +
	                           " values( nextval('fixture_diagnostics_seq'),  " + fixtureDiagnosticsVO.getFixtureId() +"," + fixtureDiagnosticsVO.getFixtureDiagnosticReferenceId() + "," +
	                           ecHourlyVO.getId() + ",'" + status +"')";
		            jdbcTemplate.execute(queryToCreateFixtureDiagnosticsRow);
				}
			}
		}
		
		//Let's update the capture time
		String queryForLastDiagnosticsUpdate = "update cloud_config set val = '" + sdf.format(latestTimeStamp)  + "' where name = 'diagnostics.last_capture_at'";
		jdbcTemplate.execute(queryForLastDiagnosticsUpdate);
		}catch(Exception ex){
		
			ex.printStackTrace() ;
	}
		
   }

	public EmHealthDataVO getDeviceHealthData(String mac) {
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		
		EmHealthDataVO emHealthDataVO = new EmHealthDataVO();
		
		String queryForLastDiagnosticsValue = "select val from cloud_config where name = 'diagnostics.last_capture_at'";
		String captureTimeStampValue = jdbcTemplate.queryForObject(queryForLastDiagnosticsValue, String.class);
		
		
		String queryForFixtureHealth = "select fixture_id as fixtureId, EXTRACT(EPOCH FROM ('" + captureTimeStampValue  +"' - last_connectivity_at))/60 as lastConnectivityInMinutes, "
				+ "power_used_status as powerUsedStatus from fixture_diagnostics where state = 'COMMISSIONED'";
	
		List<Integer> sensorStatusList = jdbcTemplate.query(queryForFixtureHealth, new ResultSetExtractor<List<Integer>>(){

			@Override
			public List<Integer> extractData(ResultSet rs) throws SQLException {
				
				int totalSensors = 0;
				int underObservationSensor = 0;
				int criticalSensors = 0;
				
				while(rs.next()){					
					totalSensors++;
					
					Double  lastConnectivityInMinutes = rs.getDouble("lastConnectivityInMinutes");
					String powerUsedStatus = rs.getString("powerUsedStatus");
					
					String finalStatus = DiagnosticsConstant.normal;
					
					if(powerUsedStatus.equalsIgnoreCase(DiagnosticsConstant.critical) || lastConnectivityInMinutes > 10080){
						finalStatus = DiagnosticsConstant.critical;
					}else if(powerUsedStatus.equalsIgnoreCase(DiagnosticsConstant.critical) || (lastConnectivityInMinutes <= 10080 && lastConnectivityInMinutes > 15) ){
						finalStatus = DiagnosticsConstant.high;
					}
					
					if(finalStatus.equals(DiagnosticsConstant.critical)){
						criticalSensors++;
					}else if(finalStatus.equals(DiagnosticsConstant.high)){
						underObservationSensor++;
					}					
					
				}	
				
				List<Integer> sensorStatusList = new ArrayList<Integer>(3);
				sensorStatusList.add(0,totalSensors);
				sensorStatusList.add(1,underObservationSensor);
				sensorStatusList.add(2,criticalSensors);
				return sensorStatusList;
			}
			
		});
		emHealthDataVO.setSensorsTotal(sensorStatusList.get(0));
		emHealthDataVO.setSensorsUnderObservationNo(sensorStatusList.get(1));
		emHealthDataVO.setSensorsCriticalNo(sensorStatusList.get(2));	
		
		
		String queryForGatewayHealth = "select id as gatewayId, EXTRACT(EPOCH FROM ('" + captureTimeStampValue  +"' - last_connectivity_at))/60 as lastConnectivityInMinutes "
				+ " from gateway where commissioned='t'";
		
		List<Integer> gatewayStatusList = jdbcTemplate.query(queryForGatewayHealth, new ResultSetExtractor<List<Integer>>(){

			@Override
			public List<Integer> extractData(ResultSet rs) throws SQLException {
				
				int gatewaysCriticalNo = 0;
				int gatewaysUnderObservationNo = 0;	
				int gatewaysTotal = 0;
				
				while(rs.next()){					
					gatewaysTotal++;
					Double  lastConnectivityInMinutes = rs.getDouble("lastConnectivityInMinutes");
					
					String finalStatus = DiagnosticsConstant.normal;
					
					if( lastConnectivityInMinutes > 10080){
						finalStatus = DiagnosticsConstant.critical;
					}else if( (lastConnectivityInMinutes <= 10080 && lastConnectivityInMinutes > 15) ){
						finalStatus = DiagnosticsConstant.high;
					}
					
					if(finalStatus.equals(DiagnosticsConstant.critical)){
						gatewaysCriticalNo++;
					}else if(finalStatus.equals(DiagnosticsConstant.high)){
						gatewaysUnderObservationNo++;
					}					
					
				}	
				
				List<Integer> gatewayStatusList = new ArrayList<Integer>(3);
				gatewayStatusList.add(0,gatewaysTotal);
				gatewayStatusList.add(1,gatewaysUnderObservationNo);
				gatewayStatusList.add(2,gatewaysCriticalNo);
				return gatewayStatusList;
			}
			
		});
		
		emHealthDataVO.setGatewaysTotal(gatewayStatusList.get(0));
		emHealthDataVO.setGatewaysUnderObservationNo(gatewayStatusList.get(1));
		emHealthDataVO.setGatewaysCriticalNo(gatewayStatusList.get(2));	
		
		return emHealthDataVO;
	}

	public List<GatewayHealthDataVO> getGatewayStats(String mac) {
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		String queryForGatewayHealth = "select d.id as id , d.mac_address as mac , d.name as name, g.app1_version as version, cast (g.last_connectivity_at as timestamp(0)) as lastConectivity, " + 
                "g.no_of_sensors as no_of_sensor from gateway g, device d where g.id=d.id and g.commissioned= 't'";
		List<GatewayHealthDataVO> gatewayHealthDataVO =jdbcTemplate.query(queryForGatewayHealth, new RowMapper<GatewayHealthDataVO>(){

			@Override
			public GatewayHealthDataVO mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				GatewayHealthDataVO gatewayHealthDataVO = new GatewayHealthDataVO();
				gatewayHealthDataVO.setGatewayId(rs.getLong("id"));
				gatewayHealthDataVO.setGatewayName(rs.getString("name"));
				gatewayHealthDataVO.setGatewayMac(rs.getString("mac"));
				gatewayHealthDataVO.setGatewayVersion(rs.getString("version"));
				gatewayHealthDataVO.setLastGatewayConnectivity(rs.getString("lastConectivity"));
				gatewayHealthDataVO.setNoOfSensor(rs.getLong("no_of_sensor")) ;
				return gatewayHealthDataVO;
			}
			
		});
		
		return gatewayHealthDataVO;
	}

	public List<FixtureHealthDataVO> getFixtureStats(String mac) {
		JdbcTemplate jdbcTemplate = this.getJdbcTemplate(mac);
		String queryForFixtureHealth = "select d.id as id, d.mac_address as mac, d.name as name, cast(f.last_connectivity_at as timestamp(0)) as lastConectivity, f.firmware_version as version ,d.location as location " +
				"from fixture f, device d where f.id=d.id and f.state='COMMISSIONED'";
		List<FixtureHealthDataVO> fixtureHealthDataVO =jdbcTemplate.query(queryForFixtureHealth, new RowMapper<FixtureHealthDataVO>(){

			@Override
			public FixtureHealthDataVO mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				FixtureHealthDataVO fixtureHealthDataVO = new FixtureHealthDataVO();
				fixtureHealthDataVO.setFixtureId(rs.getLong("id"));
				fixtureHealthDataVO.setFixtureName(rs.getString("name"));
				fixtureHealthDataVO.setFixtureMac(rs.getString("mac"));
				fixtureHealthDataVO.setFixtureVersion(rs.getString("version"));
				fixtureHealthDataVO.setLastFixtureConnectivity(rs.getString("lastConectivity"));
				fixtureHealthDataVO.setLocation(rs.getString("location")) ;
				return fixtureHealthDataVO;
			}
			
		});
		
		return fixtureHealthDataVO;
	}

	
	
}
