Steps to set up and run Aggregator App 
*> go to postgres and create database 
		1> psql -U postgres
		2> create database motion ;		
*> run motionInstallSQL.sql on "motion" db.
*> go to target copy the em_motion_controller_app.jar to desktop
*> copy config.properties file from artifact/config/ to desktop
*> run sudo java -jar  em_motion_controller_app.jar  (you need to be root to run the jar)
*> See log output using 'tail -f em_motion.log' (File is generated beside the jar . After app starts.)

Dependencies 
The app have been tested on linux ubuntu (preferably Enlighted Base ISO). 
Stack  required for running the app postgresql db , java 6 . 
Stack required for development postgresql db , java 6, eclipse , Maven with java and maven Environment path set.
Best thing would be to flash gems Base iso on any gems box and run the app on it. 
Make sure to stop tomcat service on the EM box as EM udp server also listne on 8084.
for stopping the server use 'sudo service tomcat6 stop' 



FAQs .....
1> How to change port on which udp server of aggregator app listen? 
-> port no is taken from database from system_configuration table. use cmd
   update system_configuration set value='<newPortNo>' where name='motion.udp.port' ; 
   Restart the app to Listen on new port. 
2> where can I see the motion packet recieved from sensor and send to E_Motion App ?
-> motion_packets table in database contain recieved packets and display_data table contain
   the packets that are send to E-Motion App.
3> How to add fixture and floor information ?
-> Use Config File in the artifact to place fixture and floor plan information. you will need to restart the app. 
   Fixture and Floor plan details are inserted if not present in the database or updated if present. 
4> How to change Remote Port and Ip on which aggregator send xml packets ?
-> there is a file called config.properties. These propeties are set there. You can change them from there. 
   You will need to jar.   
5> How to change the Delay time to send XML data to remote Motion app ?
-> Use config.properties file there is property called "dataSendDelay" which can be used.
   No need to  restart the app for this change. 
6> how to know Config file properties meaning ?
-> Given inline with the properties in the config file itself.
   Also if editing config file check for trailing spaces. they should not creep in :)

