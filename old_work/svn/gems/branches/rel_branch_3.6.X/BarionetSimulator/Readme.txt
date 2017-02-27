Instructions for Usage:-

- Create local subinterfaces on eth1 interface on your machine with IPs of range 169.254.0.XX other with netmask 255.255.0.0 and gateway - 169.254.0.1
- Make sure you are able to ping EM eth1 IP from it  i.e. 169.254.0.1.
- Run the JAR file named BarionetSimulator.jar. This should loop through  the local subinterfaces where each one of them is treated as a separate barionet device and send UDP unicast to discovery request from EM. The TCP servers are also invoked from the same JAR file and they start listening on the local sockets to receive any commands from EM and write statechange commands to EM.
- There are .cfg files present in the src/com/ems/BarionetTCPServer folder with names similar to "10-D5-6E-D9-93-73.cfg". Any statechange commands to be sent to EM should be written inside the respective "macAddress.cfg" file and the command is sent to EM.