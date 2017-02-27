*******************************************************************************
NOTE: The ids in the sample maps to my database, please update the ids based on 
your database details
********************************************************************************
--------------------------------------------------------------------------------
Accessing Webservice via curl
How to Login...
*** NOTE: cookie.tmp will be save in the same directory. ***
--------------------------------------------------------------------------------
curl -d "j_username=admin&j_password=xxx" -c cookie.tmp -k https://localhost:8443/ems/j_spring_security_check

--------------------------------------------------------------------------------
How to access Webservice (sample)
Company details
--------------------------------------------------------------------------------
curl --get -b cookie.tmp -k https://localhost:8443/ems/services/org/company

--------------------------------------------------------------------------------
Dim Fixtures (sample)
NOTE: fixture.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @fixture.data -k https://localhost:8443/ems/services/org/fixture/op/dim/{rel|abs}/-50/60
{
	@fixture.data = "<fixtures><fixture><id>1</id></fixture><fixture><id>2</id></fixture></fixtures>"
	-50: percentage (-100 | 0 | 100)
	60: time (minutes)
}

--------------------------------------------------------------------------------
Fixtures Modes (Auto, bypass, baseline)
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @fixture.data -k https://localhost:8443/ems/services/org/fixture/op/mode/{AUTO|BYPASS|BASELINE}
{
	@fixture.data = "<fixtures><fixture><id>1</id></fixture><fixture><id>2</id></fixture></fixtures>"
}

--------------------------------------------------------------------------------
Get realtime stats for Fixture(s)
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @fixture.data -k https://localhost:8443/ems/services/org/fixture/op/realtime
{
	@fixture.data = "<fixtures><fixture><id>1</id></fixture><fixture><id>2</id></fixture></fixtures>"
}

--------------------------------------------------------------------------------
Update Fixture(s) position
fixturepos.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @fixturepos.data -k https://localhost:8443/ems/services/org/fixture/du/updateposition
{
	@fixturepos.data = "<fixtures><fixture><id>1</id><xaxis>100</xaxis>100<yaxis></yaxis></fixture><fixture><id>2</id><xaxis>140</xaxis>140<yaxis></yaxis></fixture></fixtures>"
}

--------------------------------------------------------------------------------
Update Gateway(s) position
gatewaypos.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @gatewaypos.data -k https://localhost:8443/ems/services/org/gateway/du/updateposition
{
	@gatewaypos.data = "<gateways><gateway><id>1</id><xaxis>100</xaxis>100<yaxis></yaxis></gateway><gateway><id>2</id><xaxis>140</xaxis>140<yaxis></yaxis></gateway></gateways>"
}

--------------------------------------------------------------------------------
Decommission Gateway(s)
decommissiongateway.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @decommissiongateway.data -k https://localhost:8443/ems/services/org/gateway/decommission
{
	@decommissiongateway.data = "<gateway><id>1</id></gateway>"
}

--------------------------------------------------------------------------------
Get realtime stats for Gateway(s)
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @gateway.data -k https://localhost:8443/ems/services/org/gateway/op/realtime
{
	@gateway.data = "<gateways><gateway><id>1</id><ipaddress>169.254.0.100</ipaddress></gateway><gateway><id>2</id><ipaddress>169.254.0.101</ipaddress></gateway></gateways>"
}

--------------------------------------------------------------------------------
Update Switch(s) position
switchpos.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @switchpos.data -k https://localhost:8443/ems/services/org/switch/du/updateposition
{
	@switchpos.data = "<switches><switch><id>1</id><xaxis>100</xaxis>100<yaxis></yaxis></switch><switch><id>2</id><xaxis>140</xaxis>140<yaxis></yaxis></switch></switches>"
}

--------------------------------------------------------------------------------
DeCommission Fixtures 
decommissionfixture.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @decommissionfixture.data -k https://localhost:8443/ems/services/org/fixture/decommission
{
	@decommissionfixture.data = "<fixtures><fixture><id>1013</id></fixture><fixture><id>1014</id></fixture></fixtures>"
}

--------------------------------------------------------------------------------
DeCommission Fixtures Without Ack (forced retry)
decommissionfixture.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @decommissionfixture.data -k https://localhost:8443/ems/services/org/fixture/decommissionwithoutack
{
	@decommissionfixture.data = "<fixtures><fixture><id>1013</id></fixture><fixture><id>1014</id></fixture></fixtures>"
}

--------------------------------------------------------------------------------
wsaction login - Bridge between GEMS 2.0 and GEMS 3.0
--------------------------------------------------------------------------------
curl -v -X POST -c cookie.tmp -H"Content-Type: application/xml" -d @wsaction.login.data -k https://localhost:8443/ems/wsaction.action

--------------------------------------------------------------------------------
Logout (Need to include the session id in the http header)
--------------------------------------------------------------------------------
curl --get -b cookie.tmp  -k https://localhost:8443/ems/j_spring_security_logout

--------------------------------------------------------------------------------
Update Fixtures during fixture commissioning
fixtureduringcommission.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @fixtureduringcommission.data -k https://localhost:8443/ems/services/org/fixture/updateduringcommission
{
	@fixtureduringcommission.data = "<fixture>
									<id>166</id><noofbulbs>1</noofbulbs><currentprofile>Warehouse</currentprofile><name>Sensor000446</name>
									<description></description><notes></notes>
									<ballast><id>9</id><name></name><lampnum></lampnum></ballast>
									<bulb><id>3</id><name></name></bulb>
									<nooffixtures>1</nooffixtures>
									<voltage>277</voltage>
									</fixture>"
}

--------------------------------------------------------------------------------
Save Scenes List
scenelist.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @scenelist.data -k https://localhost:8443/ems/services/org/scene/savescenelist
{
	@scenelist.data = "<scenes>
						<scene><id></id><name>All On</name><switchid>122</switchid></scene>
						<scene><id></id><name>All Off</name><switchid>122</switchid></scene>
						</scenes>"
}

--------------------------------------------------------------------------------
Save SceneLevel
scenelevellist.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @scenelevellist.data -k https://localhost:8443/ems/services/org/scene/savescenelevel
{
	@scenelevellist.data = "<sceneLevels><sceneLevel><id></id><switchid>122</switchid><sceneid>86</sceneid><fixtureid>131</fixtureid><lightlevel>100</lightlevel></sceneLevel></sceneLevels>"
}

--------------------------------------------------------------------------------
Save SwitchFixture
switchfixtureslist.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @switchfixtureslist.data -k https://localhost:8443/ems/services/org/switchfixtures/saveswitchfixture
{
	@switchfixtureslist.data = "<switchFixturess>
								<switchFixtures><id></id><fixtureid>131</fixtureid><switchid>115</switchid></switchFixtures>
								</switchFixturess>"
}

================================================================================
Profile services
================================================================================
--------------------------------------------------------------------------------
Assign Profile to fixtures
assignprofile.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @assignprofile.data -k https://localhost:8443/ems/services/org/profile/assign/to/{currentprofile}/from/{originalprofile}/gid/{groupid}
{
	@assignprofile.data = "<fixture><id>1</id></fixture>"
}

================================================================================
Multicast Group services
================================================================================
--------------------------------------------------------------------------------
List Group Types
--------------------------------------------------------------------------------
curl --get -b cookie.tmp -k https://localhost:8443/ems/services/org/gemsgroups/grouptypelist/

--------------------------------------------------------------------------------
Create group type
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @gemsgrouptype.data.data -k https://localhost:8443/ems/services/org/gemsgroups/op/creategrouptype

--------------------------------------------------------------------------------
CreateGroup
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @gemsgroups.data -k https://localhost:8443/ems/services/org/gemsgroups/op/creategroup

--------------------------------------------------------------------------------
List Groups
--------------------------------------------------------------------------------
curl --get -b cookie.tmp -k https://localhost:8443/ems/services/org/gemsgroups/list

--------------------------------------------------------------------------------
List Gems Group Fixtures
--------------------------------------------------------------------------------
curl --get -b cookie.tmp -k https://localhost:8443/ems/services/org/gemsgroupfixture/list/1

--------------------------------------------------------------------------------
Manage Gems Group Fixtures
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @removefixturefromgroup.data -k https://localhost:8443/ems/services/org/gemsgroupfixture/op/managegroup/4

--------------------------------------------------------------------------------
Apply Gems Group Fixtures
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @applyfixturetogroup.data -k https://localhost:8443/ems/services/org/gemsgroupfixture/op/applygroup/4

================================================================================
DR services
================================================================================
--------------------------------------------------------------------------------
Get Energy consumed by individual group within the two time periods.
--------------------------------------------------------------------------------
curl --get -b cookie.tmp -k https://localhost:8443/ems/services/org/dr/group/ec/{fdate}/{tdate}

--------------------------------------------------------------------------------
Get individual group DR sensitivity
--------------------------------------------------------------------------------
curl --get -b cookie.tmp -k https://localhost:8443/ems/services/org/dr/group/sensitivity

--------------------------------------------------------------------------------
Get current pricing
--------------------------------------------------------------------------------
curl --get -b cookie.tmp -k https://localhost:8443/ems/services/org/dr/pricing/current

--------------------------------------------------------------------------------
Send dimming command to dim by group
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -k https://localhost:8443/ems/services/org/dr/op/dim/group/{groupid}/{percentage}/{time}

--------------------------------------------------------------------------------
Assign fixtures to area
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @assignfixturestoarea.data -k https://localhost:8443/ems/services/org/area/{aid}/assignfixtures

--------------------------------------------------------------------------------
Send dimming command to dim by scene id and switch id, mostly using mode as 102
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -k https://localhost:8443/ems/services/org/switch/op/dim/switch/{switchid}/scene/{sceneid}/{percentage}/{time}
{
	percentage: (0 (full off) | 100 (full on) | 101 (auto) | 102 (pick scene light levels))
	time: (minutes)
}

--------------------------------------------------------------------------------
Send dimming command to dim by switch id
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -k https://localhost:8443/ems/services/org/switch/op/dim/switch/{switchid}/{percentage}/{time}
{
	percentage: (0 (full off) | 100 (full on) | 101 (auto))
	time: (minutes)
}

--------------------------------------------------------------------------------
Trigger Motion bit on Fixtures (sample)
NOTE: fixture.data: Is a file: contents shown below
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -d @fixture.data -k https://localhost:8443/ems/services/org/fixture/op/motionbit/bitlevel/{bitlevel}/frequency/{frequency}/action/{action}
{
	@fixture.data = "<fixtures><fixture><id>1</id></fixture><fixture><id>2</id></fixture></fixtures>"
	1: bitlevel (1 | 2 | 0)
	5: frequency (minutes) (1 | 5)
	1: action (motion_detection_duration) (0 (to stop motion) | 1 (default))
}


--------------------------------------------------------------------------------
Push Switch configuration to fixture
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -k https://localhost:8443/ems/services/org/switch/op/push/cfg/switch/{switchid}/fixture/{fixtureid}

--------------------------------------------------------------------------------
Push Wds group configuration to fixture(s) in the gems / switch group.
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -k https://localhost:8443/ems/services/org/switch/op/push/wdscfg/switch/{switchid}


--------------------------------------------------------------------------------
Delete Wds and Switch
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -k https://localhost:8443/ems/services/org/wds/deleteWds/{wdsid}
curl -v --get -b cookie.tmp  -k https://localhost:8443/ems/services/org/switch/delete/{switchid}

--------------------------------------------------------------------------------
Send switchgroup action command to switchgroup by refering switchid
--------------------------------------------------------------------------------
curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -k https://localhost:8443/ems/services/org/switch/op/{switchid}/action/{action}/argument/{argument}
{
	switchid (database ID for the switch, to which the switch group and the fixtures are associated)
	action (auto | scene | dimup | dimdown), default is auto
	argument (auto {0} | scene {0-8} | dimup {10} | dimdown {10})
}
