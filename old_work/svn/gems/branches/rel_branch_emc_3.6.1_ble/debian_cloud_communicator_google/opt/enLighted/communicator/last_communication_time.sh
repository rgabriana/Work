#!/bin/bash
source /etc/environment
date +%s%N | cut -b1-13 > $OPT_ENLIGHTED/communicator/last_communication_time
