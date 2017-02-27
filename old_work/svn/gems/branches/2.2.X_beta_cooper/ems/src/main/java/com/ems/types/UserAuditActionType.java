package com.ems.types;

public enum UserAuditActionType {
	
    Login,
    Change_Password,
    Company_Update,
    Campus_Update,
    Building_Update,
    Floor_Update,
    Area_Update,
    Fixture_Update,
    Fixture_Image_Upgrade,
    Fixture_Discovery,
    Fixture_Commission,
    Fixture_Profile_Update,
    Fixture_Dimming,
    Fixture_Mode_Change,
    Fixture_Discovered_Delete,
    FIxture_RMA,
    Gateway_Update,
    Gateway_Add,
    Gateway_Commission,
    Gateway_Image_Upgrade,
    Profile_Update,
    Group_Update,
    Group_Fixture_Update,
    Pricing_Update,
    Events_Update,
    User_Create,
    User_Update,
    User_Delete,
    Tenant_Update,
    Switch_Update,
    Switch_Fixture_Update,
    Switch_Fixture_Dimming,
    Scene_Update,
    Locator_Device_Update,
    Locator_Device_Update_Position,
    Locator_Device_Delete,
    Locator_Device_Create,
    Bacnet,
    Ldap_Update,
    Network_Update,
    Tenant_Facility,
    Unauthorised_Access,
    EM_Management_Validation,
    EM_Management_Upgrade__Restore,
    EM_Management,
    DHCP_Update,
    EWS_Discovery,
    EWS_Commission,
    Motion_Bits_update,
    Cloud_Server_Setup,
    EWS_Image_Upgrade
    ; 
	
    public String getName() {
        return this.toString().replaceAll("__", "/").replaceAll("_", " ");
    }

}