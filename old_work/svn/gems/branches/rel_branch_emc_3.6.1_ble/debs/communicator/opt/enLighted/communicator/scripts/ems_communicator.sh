#!/bin/bash
source /etc/environment
cd $OPT_ENLIGHTED/communicator/scripts/
java -jar $OPT_ENLIGHTED/communicator/ems_communicator.jar  $ENL_APP_HOME/Enlighted/urls.properties SendDashBoardDetailsHourlyData