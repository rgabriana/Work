package com.communication.types;

public enum DatabaseState {
	REPLICA_UNREACHABLE,
	NOT_MIGRATED,
    MIGRATION_READY, 
    MIGRATION_IN_PROGRESS,
    MIGRATION_SUCCESS,
    MIGRATION_FAIL,
    PLUGLOAD_ENERGY_CONSUMPTION_START,
    PLUGLOAD_ENERGY_CONSUMPTION_FAIL,
    PLUGLOAD_ENERGY_CONSUMPTION_IN_PROGRESS,
    PLUGLOAD_ENERGY_CONSUMPTION_SUCCESS,    
    PLUGLOAD_ENERGY_CONSUMPTION_DAILY_START,
    PLUGLOAD_ENERGY_CONSUMPTION_DAILY_FAIL,
    PLUGLOAD_ENERGY_CONSUMPTION_DAILY_IN_PROGRESS,
    PLUGLOAD_ENERGY_CONSUMPTION_DAILY_SUCCESS,    
    PLUGLOAD_ENERGY_CONSUMPTION_HOURLY_START,
    PLUGLOAD_ENERGY_CONSUMPTION_HOURLY_FAIL,
    PLUGLOAD_ENERGY_CONSUMPTION_HOURLY_IN_PROGRESS,
    PLUGLOAD_ENERGY_CONSUMPTION_HOURLY_SUCCESS,
    
    ENERGY_CONSUMPTION_START,
    ENERGY_CONSUMPTION_IN_PROGRESS,
    ENERGY_CONSUMPTION_SUCCESS,
    ENERGY_CONSUMPTION_FAIL,
    ENERGY_CONSUMPTION_HOURLY_START ,
    ENERGY_CONSUMPTION_HOURLY_IN_PROGRESS,
    ENERGY_CONSUMPTION_HOURLY_SUCCESS,
    ENERGY_CONSUMPTION_HOURLY_FAIL,
    ENERGY_CONSUMPTION_DAILY_START,
    ENERGY_CONSUMPTION_DAILY_IN_PROGRESS,
    ENERGY_CONSUMPTION_DAILY_SUCCESS,
    ENERGY_CONSUMPTION_DAILY_FAIL,
    MOTION_BIT_START,
    MOTION_BIT_IN_PROGRESS,
    MOTION_BIT_SUCCESS,
    MOTION_BIT_FAIL,
    SYNC_READY,
    SYNC_FAILED,
    REMIGRATION_REQUIRED;
    
    
    public String getName() {
        return this.toString();
    }

    public String getLowerCaseName() {
        return this.toString().toLowerCase();
    }
}