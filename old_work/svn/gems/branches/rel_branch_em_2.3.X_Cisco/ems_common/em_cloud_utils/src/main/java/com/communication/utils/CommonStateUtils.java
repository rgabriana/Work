package com.communication.utils;

import com.communication.types.DatabaseState;

public class CommonStateUtils {
	 
	// Table name 
		public static String energyTableName = "energy_consumption" ;
		public static String energyHourlyTableName = "energy_consumption_hourly" ;
		public static String energyDailyTableName = "energy_consumption_daily" ;
		public static String motionBitTableName = "em_motion_bits" ;
		
		
	public static String getStateSuccessAccordingToTableName(String tableName2)
	{
			if(tableName2.equalsIgnoreCase(energyTableName))
			{
				return DatabaseState.ENERGY_CONSUMPTION_SUCCESS.getName() ;
			}else if(tableName2.equalsIgnoreCase(energyHourlyTableName))
			{
				return DatabaseState.ENERGY_CONSUMPTION_HOURLY_SUCCESS.getName();
			} else if(tableName2.equalsIgnoreCase(energyDailyTableName))
			{
				return DatabaseState.ENERGY_CONSUMPTION_DAILY_SUCCESS.getName() ;
			} else if(tableName2.equalsIgnoreCase(motionBitTableName))
			{
				return DatabaseState.MOTION_BIT_SUCCESS.getName() ;
			}
			else
			{
				return null ;
			}
		}
	public static String getStateFailAccordingToTableName(String tableName2)
	{
			if(tableName2.equalsIgnoreCase(energyTableName))
			{
				return DatabaseState.ENERGY_CONSUMPTION_FAIL.getName() ;
			}else if(tableName2.equalsIgnoreCase(energyHourlyTableName))
			{
				return DatabaseState.ENERGY_CONSUMPTION_HOURLY_FAIL.getName();
			} else if(tableName2.equalsIgnoreCase(energyDailyTableName))
			{
				return DatabaseState.ENERGY_CONSUMPTION_DAILY_FAIL.getName() ;
			} else if(tableName2.equalsIgnoreCase(motionBitTableName))
			{
				return DatabaseState.MOTION_BIT_FAIL.getName() ;
			}
			else
			{
				return null ;
			}
		}
	public static String getStateInProgressAccordingToTableName(String tableName2)
	{
			if(tableName2.equalsIgnoreCase(energyTableName))
			{
				return DatabaseState.ENERGY_CONSUMPTION_IN_PROGRESS.getName() ;
			}else if(tableName2.equalsIgnoreCase(energyHourlyTableName))
			{
				return DatabaseState.ENERGY_CONSUMPTION_HOURLY_IN_PROGRESS.getName();
			} else if(tableName2.equalsIgnoreCase(energyDailyTableName))
			{
				return DatabaseState.ENERGY_CONSUMPTION_DAILY_IN_PROGRESS.getName() ;
			} else if(tableName2.equalsIgnoreCase(motionBitTableName))
			{
				return DatabaseState.MOTION_BIT_IN_PROGRESS.getName() ;
			}
			else
			{
				return null ;
			}
		}
	
	public static String getFailState(DatabaseState databaseState) {
		String state = null ;
		switch(databaseState)
		{
		case ENERGY_CONSUMPTION_DAILY_IN_PROGRESS:
		case ENERGY_CONSUMPTION_DAILY_START:
		case ENERGY_CONSUMPTION_DAILY_SUCCESS:
			state =  DatabaseState.ENERGY_CONSUMPTION_DAILY_FAIL.getName() ;
			break;
		case ENERGY_CONSUMPTION_HOURLY_IN_PROGRESS:
		case ENERGY_CONSUMPTION_HOURLY_START:
		case ENERGY_CONSUMPTION_HOURLY_SUCCESS:
			state =  DatabaseState.ENERGY_CONSUMPTION_HOURLY_FAIL.getName() ;
			break;
		case ENERGY_CONSUMPTION_IN_PROGRESS:
		case ENERGY_CONSUMPTION_START:
		case ENERGY_CONSUMPTION_SUCCESS:
			state =  DatabaseState.ENERGY_CONSUMPTION_FAIL.getName() ;
			break;
		case MIGRATION_IN_PROGRESS:
		case MIGRATION_READY:
		case MIGRATION_SUCCESS:
			state =  DatabaseState.MIGRATION_FAIL.getName() ;
			break;
		case MOTION_BIT_IN_PROGRESS:
		case MOTION_BIT_START:
		case MOTION_BIT_SUCCESS:
			state =  DatabaseState.MOTION_BIT_FAIL.getName() ;
			break;
		default:
			state = null ;
			break;
		}
		return state;
	}
	public static String getTableNameAccordingToState(DatabaseState databaseState) {
		String tableName = null ;
		switch(databaseState)
		{
		case ENERGY_CONSUMPTION_DAILY_IN_PROGRESS:
		case ENERGY_CONSUMPTION_DAILY_START:
		case ENERGY_CONSUMPTION_DAILY_SUCCESS:
		case ENERGY_CONSUMPTION_DAILY_FAIL:
			tableName = energyDailyTableName ;
		break;
		case ENERGY_CONSUMPTION_HOURLY_IN_PROGRESS:
		case ENERGY_CONSUMPTION_HOURLY_START:
		case ENERGY_CONSUMPTION_HOURLY_SUCCESS:
		case ENERGY_CONSUMPTION_HOURLY_FAIL:
			tableName =  energyHourlyTableName ;
			break;
		case ENERGY_CONSUMPTION_IN_PROGRESS:
		case ENERGY_CONSUMPTION_START:
		case ENERGY_CONSUMPTION_SUCCESS:
		case ENERGY_CONSUMPTION_FAIL:
			tableName =  energyTableName ;
			break ;
		case MOTION_BIT_IN_PROGRESS:
		case MOTION_BIT_START:
		case MOTION_BIT_SUCCESS:
		case MOTION_BIT_FAIL:
			tableName =  motionBitTableName ;
			break;
		default:
			tableName = null ;
			break;
		}
		return tableName;
	}


}