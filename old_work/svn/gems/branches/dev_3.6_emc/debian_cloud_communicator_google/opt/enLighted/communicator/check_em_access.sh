#!/bin/bash
{
cd /tmp/
pingStatus=$(wget --no-check-certificate --timeout=10 --tries=3 https://localhost/ems/heartbeat.jsp 2>&1)
rm -f heartbeat.jsp*
echo "${pingStatus}"
}
