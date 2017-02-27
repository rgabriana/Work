package com.emscloud.types;

public enum CloudAuditActionType {
	
    Login,
    Change_Password,
    User_Create,
    User_Update,
    User_Delete,
    Unauthorised_Access,
    Customer_Create,
    Customer_Update,
    Customer_Delete,
    Customer_Bill_Generate,
    Customer_Bill_Regenerate,
    Customer_Bill_Payment,
    Customer_Bill_View,
    Customer_Bill_Download,
    Facility_Update,
    Facility_Create,
    Facility_Delete,
    User_Customers_Update,
    Em_Instance_Activation,
    Em_Instance_Updated,
    Em_Instance_Unreg_Deleted,
    Em_Instance_Upgrade_Task,
    Em_Instance_Log_Upload_Task,
    Replica_Server_Created,
    Replica_Server_Updated,
    Replica_Server_Deleted,
    Upload_Upgrade_File,
    Customer_Site_Create,
    Customer_Site_Update,
    Customer_Site_Delete,
    Cloud_Em_Instance_Mapping; 
	
    public String getName() {
        return this.toString().replaceAll("__", "/").replaceAll("_", " ");
    }

}
