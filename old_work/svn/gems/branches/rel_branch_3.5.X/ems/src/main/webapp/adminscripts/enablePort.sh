#!/bin/bash

. /etc/lsb-release


############################   Usage  ######################################################################
#### call in the following fashion
#### CASE I:
########################################################################## 
#### enablePort.sh 587 588 eth1 eth2
####	-- port 587 will replace 588 for the said network config eth1 in the above case.
####	-- if eth1 is not passed as arg then default network interface will be picked up from db
####	-- forther param eth2 optional. If not present then third will be taken and port will be removed from that interface
#######################################################################
#### CASE II:
######################################################################
#### enablePort.sh DISABLE/ENABLE 0 eth1
####	-- disables all the ports on eth1
####	-- just put 0 as is so that network will be taken as argument third
####	-- if eth1 is not passed as arg then default network interface will be picked up from db	
####
#############################################################################################################

current=$DISTRIB_RELEASE
min=10.04
res=$(echo $current $min | awk '{if ($1 > $2) print 1; else print 0}')
#if [ $res -gt 0 ]
#then
        ##############################14.04#################################
		
#fi

enablePort=-1

if [ -z $1 ]
then
	echo "Properly execute the script"
	exit 3
fi

if [ "$1" -eq "$1" ] 2>/dev/null
then
	enablePort=-1
else
	if [ $1 == 'DISABLE'  ]
	then
		enablePort=0
	elif [ $1 == 'ENABLE'  ]
	then
		enablePort=1
	else
		echo "Argument is not propert is should be DISBALE/ENABLE"
		exit 3
	fi
fi

network=`echo $(psql -q -U postgres -d ems -h localhost -p 5433 -t -c"select interface_name from network_interface_mapping n, network_types nt, network_settings ns where n.network_type_id=nt.id and n.network_settings_id=ns.id and nt.name='Corporate'" sed 's,^ *,,; s, *$,,')`

if [ ! -z $3 ]
then
	network=$3
fi

################################ COMMON CODE APPLICABLE TO BOTH 1404 and 1004 ##############################################3
if [ $enablePort -eq -1 ];
then
	grep -q "$network.*dport $1" /etc/iptables.rules
	a=$?
	if [ $a -eq 0 ];
	then
        	echo "Already Exists"
        	if [ ! -z $2 ]
			then
				if [ -z $4 ]
				then
					sudo sed -i '/'$network'.*--dport '$2' /d' /etc/iptables.rules
					sudo sed -i '/'$network'.*--sport '$2' /d' /etc/iptables.rules
				else
					## handle case if params are 589 587 eth0 eth1 - should delete 587 on eth1
					## handle case if params are 589 587 eth0 eth0 - should delete 587 on eth0
					## handle case if params are 587 587 eth0 eth1 - should delete 587 on eth1
					## handle case if params are 587 587 eth0 eth1 -- nothing should happend
					deleteFlag=1
					if [ "$4" == "$network" ] && [ $2 -eq $1 ]
					then
						deleteFlag=0
					fi
					if [ $deleteFlag -eq 1 ]
					then
						sudo sed -i '/'$4'.*--dport '$2' /d' /etc/iptables.rules
						sudo sed -i '/'$4'.*--sport '$2' /d' /etc/iptables.rules
					fi
				fi
			fi  
	else
		
		cnt=`sudo cat /etc/iptables.rules | grep "$network -p udp -m udp -j ACCEPT" | wc -l`
		if [ $cnt -eq '0'  ]
		then
			if [ "$network" != "null" ]
			then
				sudo sed -i '/COMMIT/i -A INPUT -i '$network' -p udp -m udp -j ACCEPT' /etc/iptables.rules
				sudo sed -i '/COMMIT/i -A INPUT -i '$network' -p icmp -m icmp --icmp-type any -j ACCEPT' /etc/iptables.rules
			fi
		fi
			if [ $1 -ne 0 ]
			then
	        	sudo sed -i '/INPUT -i '$network' -p udp -m udp -j ACCEPT/i -A INPUT -i '$network' -p tcp -m tcp --dport '$1' -j ACCEPT' /etc/iptables.rules
		        sudo sed -i '/INPUT -i '$network' -j DROP/i -A INPUT -i '$network' -p tcp -m tcp --sport '$1' -m state --state ESTABLISHED -j ACCEPT' /etc/iptables.rules
		        sudo sed -i '/COMMIT/i -A OUTPUT -o '$network' -p tcp -m tcp --dport '$1' -m state --state NEW,ESTABLISHED -j ACCEPT' /etc/iptables.rules
	        fi
		if [ ! -z $2 ]
		then
			if [ -z $4 ]
			then
				sudo sed -i '/'$network'.*--dport '$2' /d' /etc/iptables.rules
				sudo sed -i '/'$network'.*--sport '$2' /d' /etc/iptables.rules
			else
				## handle case if params are 589 587 eth0 eth1 - should delete 587 on eth1
				## handle case if params are 589 587 eth0 eth0 - should delete 587 on eth0
				## handle case if params are 587 587 eth0 eth1 - should delete 587 on eth1
				## handle case if params are 587 587 eth0 eth1 -- nothing should happend
				deleteFlag=1
				if [ "$4" == "$network" ] && [ $2 -eq $1 ]
				then
					deleteFlag=0
				fi
				if [ $deleteFlag -eq 1 ]
				then
					sudo sed -i '/'$4'.*--dport '$2' /d' /etc/iptables.rules
					sudo sed -i '/'$4'.*--sport '$2' /d' /etc/iptables.rules
				fi
			fi
		fi  
	fi

else
	###### ENABLE OR DISABLE ALL PORTS NOW
	if [ $enablePort -eq 0  ]
	then
		## Disable all ports on network
		sudo sed -i '/^.*'$network'.*--dport /s/ACCEPT/DROP/g' /etc/iptables.rules
		sudo sed -i '/^.*'$network'.*--sport /s/ACCEPT/DROP/g' /etc/iptables.rules
	elif [ $enablePort -eq 1  ]
	then
		## ENable all ports on network
		sudo sed -i '/^.*'$network'.*--dport /s/DROP/ACCEPT/g' /etc/iptables.rules
		sudo sed -i '/^.*'$network'.*--sport /s/DROP/ACCEPT/g' /etc/iptables.rules
	else
		echo "Error occured.. enablePort value can not be $enablePort"
	fi
fi


sudo iptables-restore < /etc/iptables.rules


