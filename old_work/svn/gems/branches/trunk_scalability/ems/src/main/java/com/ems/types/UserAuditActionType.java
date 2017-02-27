package com.ems.types;

public enum UserAuditActionType {
	
    Login,
    Change_Password,
    Organization_Update,
    Campus_Update,
    Building_Update,
    Floor_Update,
    Area_Update,
    Fixture_Update,
    Fixture_Image_Upgrade,
    Fixture_Discovery,
    Fixture_Commission,
    Placed_Fixture_Commission,
    Fixture_Profile_Update,
    Fixture_Dimming,
    Fixture_Mode_Change,
    Fixture_Discovered_Delete,
    Placed_Fixture_Delete,
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
    User_Facility_Update,
    Tenant_Update,
    Switch_Update,
    ERC_Update,
    Switch_Fixture_Update,
    Switch_Fixture_Dimming,
    Scene_Update,
    Locator_Device_Update,
    Locator_Device_Update_Position,
    Locator_Device_Delete,
    Locator_Device_Create,
    Bacnet,
    LDAP_Update,
    Network_Update,
    Tenant_Facility,
    Unauthorised_Access,
    EM_Management_Validation,
    EM_Management_Upgrade__Restore,
    EM_Management,
    DHCP_Update,
    ERC_Discovery,
    ERC_Commission,
    Motion_Bits_update,
    Motion_Group_Update,
    Cloud_Server_Setup,
    ERC_Image_Upgrade,
    Fixture_Calibration,
    Initiate_Power_Usage_Characterization,
    Retrieve_Power_Usage_Characterization,
    Enable__Disable_Volt_Participation_In_LORP,
    Import_Ballast_Curve,
    Ballast_Change,
    Forget_Fixture,
    Forget_Ballast,
    DR,
    Assign_Fixture_Type,
    Bulb_Add,
    Ballast_Add,
    FixtureType_Add,
    Bulb_Edit,
    Ballast_Edit,
    FixtureType_Edit,
    Bulb_Delete,
    Ballast_Delete,
    FixtureType_Delete
    ; 
	
    public String getName() {
        return this.toString().replaceAll("__", "/").replaceAll("_", " ");
    }

}