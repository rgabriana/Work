#!/bin/bash

test -x /usr/sbin/logrotate || exit 0
/usr/sbin/logrotate /etc/customLogrotate.conf