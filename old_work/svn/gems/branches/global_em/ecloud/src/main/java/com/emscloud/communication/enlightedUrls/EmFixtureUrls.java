package com.emscloud.communication.enlightedUrls;

public class EmFixtureUrls {

	public static String heartBeatUrls = "/api/org/uem/common/em/check/connectivity";

	public static String getListOfFixture = "/api/org/fixture/list/";
	public static String getAlternateListOfFixture = "/api/org/fixture/list/alternate/filter/";
	public static String getFixtureCount = "/api/org/fixture/count/";
	public static String getFixtureId = "/api/org/fixture/details/";
	public static String getCommissionStatus = "/api/org/fixture/getcommissionstatus";
	public static String getDimLevel = "/api/org/fixture/getDimLevels";

	public static String getListOfGateways = "/api/org/gateway/list/";

	public static String getGatewayDetails = "/api/org/gateway/details/";

	public static String getFixtureDetails = "/api/org/fixture/details/";

	public static String getFixtureObjectDetails = "/api/org/fixture/detailsobject/";

	public static String dimFixture = "/api/org/fixture/op/dim/";

	public static String applyModeToFixture = "/api/org/fixture/op/mode/";

	public static String getFixtureRealTimeStats = "/api/org/fixture/op/realtime/";

	public static String getGatewayRealTimeStats = "/api/org/gateway/op/realtime/";

	public static String getEMInstanceServerTimeOffsetFromGMT = "/api/org/getServerTimeOffsetFromGMT/";

	public static String getFixtureCountByProfileGroupId = "/api/org/fixture/count/group/";

	public static String getFixtureCountByProfileTemplateId = "/api/org/fixture/count/profiletemplate/";

	public static String bulkassignProfileToFixtures = "/api/org/profile/bulkassign/";

	public static String getEMInstanceFacilityTree = "/api/uem/getEmFacilityTree";
	
	public static String getCompany = "/api/org/company/";
    public static String getFloorList = "/api/org/floor/list";
    public static String getTimeZone = "/api/org/getServerDateTimezone";
    public static String getFloorplan = "/api/org/floor/";
    public static String getEMTemplates = "/api/org/profiletemplate/getallderivedemtemplates";
    public static String getEMProfiles = "/api/org/profile/getallderivedemprofiles";
    public static String updateEMProfiles = "/api/org/profile/updateemprofiles";
    public static String pushNewEMProfile = "/api/org/profile/pushnewprofiletoem";
    public static String getEMEvents = "/api/events/list/EmEventList/";
}
