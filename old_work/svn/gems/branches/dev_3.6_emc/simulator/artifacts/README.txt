0. Prepare you m/c for the gateway
NOTE: 
+ Select a interface card on GW machine and assign a static IP address as 169.254.0.x and gateway as 169.254.0.1
+ Plug this machine on to the building network where the GEMS is connected

1. Simulate Gateway in the database
php ../../install/debian-install/home/enlighted/insertGateway.php --ip=169.254.0.X --mac=68:54:f5:xx:xx:xx --mask=255.255.0.0
NOTE:
This is the IP of the static address you have given to you GW m/c

2. Now the Sensors
Approach A
----------
php createfixtures.php | psql -U postgres ems
NOTE: 
+ The createfixture.php have a fixture primary key as the starting point, so make sure that you use the max(id) from the fixture table to start with and then list the no of sensors you want to simulate in the createfixture.php script
+ Also you will have to bump up the sequence no (this will enable the new sensors that are to be really discovered work well). so please login on the psql and do the following
psql>select setval('fixture_seq', max(id)) from fixture;

Approach B
----------
The simulator now has a option of simulating the discovery as well, start the simulator with range of unique sensor macs and then run through the GEMS discovery screens.

3. Simulation of Motion bits - via http Framework
Motion simulation
PMstats packet has motion bits spread out in two mask motion_mask_1 and motion_mask_2 (this having the latest 5 min data part)
Idea is to simulate this motion bits part so that application like space analysis program can take advantage.

1) Simulator will not listen on port localhost:9999 and will expose two services

2) Requesting last status of the mbits that was set
GET http://localhost:9999/motion
Response:
[{"mac":"aa:aa:1","mbits":10101010110,"lastupdated":"15 Mar, 2016 4:27:40 PM"},{"mac":"aa:aa:2","mbits":0,"lastupdated":"15 Mar, 2016 4:25:01 PM"}]

Description: The reponse will return what was the last mbits set for each sensors that is beign simulated.

3) Setting a new mbits for each sensor
curl -v -X POST --data "mac=aa:aa:1&mbits=1" http://localhost:9999/motion
Response:{"status":0} 
Description: status 0 indicates that the mbits were set on the given mac.

4) On each PM stats (till the simulator is on) it will not keep sending this mbits 

5) In order to simulate a pattern, we can write an external shell script which can keep on simulating the mbits per 5 mins for each sensor(s)
 
