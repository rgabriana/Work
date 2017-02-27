#!/bin/bash

date +%s%N | cut -b1-13 > /opt/enLighted/communicator/last_communication_time
