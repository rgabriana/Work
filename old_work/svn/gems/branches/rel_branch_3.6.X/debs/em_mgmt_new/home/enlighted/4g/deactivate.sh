#!/bin/bash

set -e

delete_crontab_entry() {
    echo "Deleting cron job..."
    sudo crontab -l | grep -v reboot_4g | grep -v check_4g_health | sudo crontab -
    echo "Done"
}

remove_log_rotation() {
    echo "Removing log rotation..."
    sudo rm -f /etc/customLogrotate.d/4g_health
    echo "Done"
}

delete_crontab_entry
remove_log_rotation
