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
