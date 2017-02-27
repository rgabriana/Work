#!/bin/bash
network=`echo $(psql -q -U postgres -d ems -t -c"select interface_name from network_interface_mapping n, network_types nt, network_settings ns where n.network_type_id=nt.id and n.network_settings_id=ns.id and nt.name='Corporate'" sed 's,^ *,,; s, *$,,')`
ifconfig | grep HWaddr | grep $network | head -n 1 | sed 's/^.*HWaddr\ *//'
