echo "Running gateway simulator........................."
echo "save-energy" | sudo ifconfig eth0:1 169.254.0.100 netmask 255.255.0.0
sleep 10
echo "save-energy" | php ~/insertGateway.php --ip=169.254.0.100 --mac=68:54:f5:00:01:00 --mask=255.255.0.0
sleep 10
echo "save-energy" | sudo chmod +rwx /home/enlighted/Desktop/simulator_WDS/GW100/startLoad_usingconfig.sh
sleep 10
echo "inside GW100 floder"
cd /home/enlighted/Desktop/simulator_WDS/GW100/
echo "running startLoad_usingconfig.sh for GW100 "
echo "save-energy" | sudo sh /home/enlighted/Desktop/simulator_WDS/GW100/startLoad_usingconfig.sh
sleep 10
cd /home/enlighted/qa_automation
sleep 10