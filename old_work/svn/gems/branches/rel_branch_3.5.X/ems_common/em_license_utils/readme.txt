Steps to generate a license file
1.go to ems_common/em_license_utils folder
2.execute the following command (example : java -jar target/em_license_utils.jar uuid=b3a03d8f-55c4-42ed-9faa-d908720cfb92 em_devices=2000 bacnet=false bacnet_devices=0 zoneSensorsEnabled=false noOfZoneSensors=0 hvac=false emProductId=EM-SW-1000 > filePath/licensefilename.license )
3.uuid : Unique identifier visible on EM licenses page (Administration --> licenses).This will be unique for each EM.
4.em_devices : no of additional Sensors allowed in the EM.( if no additional sensors to be added , then enter 0 )
5.bacnet : false/true
6.bacnet_devices : no of additional bacnet devices ( if no addition bacnet devices to be added, then enter 0 )
7.zoneSensorsEnabled : false/true
8.noOfZoneSensors : no of additional Zone Sensors ( if no addition Zone Sensors to be added, then enter 0 )
9.hvac : false/true
10.emProductId : EM-SW-1000 for regular 1000 pack sensors / EM-NWS-1 for the "networked" sensors
11.all the above eight parameters are mandatory.