#!/bin/bash
source /etc/environment
trap "exit 1" TERM
export TOP_PID=$$

updateConfigVal(){
        arg=0
        if [ -z "$1" ]
          then
                arg=1
        fi
        if [ -z "$2" ]
        then
                arg=1
        fi
        if [ $arg -gt 0 ]
          then
                        echo "Arguments not proper.."
                        echo "Please specify arguments in the fashion : key value file_path"
                        echo "updateConfigVal ENL_APP_HOME= /etc/environment $ENL_APP_HOME"
                        echo "Exiting Now.."
                        exit 1
        fi

        cnt=`sudo cat $2 | grep $1 | wc -l`
        if [ $cnt -gt 0 ]
        then
                ## Replace the line
                sudo sed -i '/'$1'/c\'$1'"'$3'"' $2
        else
                ## insert line at the end of file
                sudo sed -i '$ a '$1'"'$3'"' $2
        fi

}


function updateIptablesRules(){
	arg1=0
        if [ -z "$1" ]
          then
                arg1=1
        fi
        if [ -z "$2" ]
        then
                arg1=1
        fi
        acceptDrop='ACCEPT'
        if [ ! -z "$3" ]
        then
              acceptDrop=$3
        fi
        if [ $arg1 -gt 0 ]
        then
                        echo "Arguments not proper.. : COMMA_SEPERATED_STATIC_PORTS new_interface ACCEPT/DROP"
                        echo "updateIptablesRules "80,443,8443" eth4 ACCEPT"
                        echo "Exiting Now.."
                        exit 1
        fi
	tempFile="$ENL_APP_HOME/Enlighted/iptables.rules"
	echo "*filter" > $tempFile
	echo ":INPUT ACCEPT [1574:461387]" >> $tempFile
	echo ":FORWARD ACCEPT [0:0]" >> $tempFile
	echo ":OUTPUT ACCEPT [1699:472705]" >> $tempFile
	echo "" >> $tempFile
	echo "COMMIT" >> $tempFile
	
	sudo sed -i '/COMMIT/i -A INPUT -i '$2' -p udp -m udp -j ACCEPT' $tempFile
	sudo sed -i '/COMMIT/i -A INPUT -i '$2' -p icmp -m icmp --icmp-type any -j ACCEPT' $tempFile
	sudo sed -i '/COMMIT/i -A INPUT -i '$2' -j DROP' $tempFile

	arr=$(echo $1 | tr "," "\n")
	if [ "$acceptDrop" = 'ACCEPT' ]
	then
        	for x in $arr
        	do
                	sudo sed -i '/INPUT -i '$2' -p udp -m udp -j ACCEPT/i -A INPUT -i '$2' -p tcp -m tcp --dport '$x' -j ACCEPT' $tempFile
	                sudo sed -i '/INPUT -i '$2' -j DROP/i -A INPUT -i '$2' -p tcp -m tcp --sport '$x' -m state --state ESTABLISHED -j ACCEPT' $tempFile
        	        sudo sed -i '/COMMIT/i -A OUTPUT -o '$2' -p tcp -m tcp --dport '$x' -m state --state NEW,ESTABLISHED -j ACCEPT' $tempFile
        	done
	fi
	sudo cp /etc/iptables.rules /etc/iptables.rules_BKP
	sudo mv $tempFile /etc/iptables.rules
}

function configureDHCPServer(){
	if [ -f /etc/init.d/isc-dhcp-server ] 
	then
	sudo cp /etc/default/isc-dhcp-server /etc/default/isc-dhcp-server_BKP
	if [ -z $4 ] || [ "$4" = 'null' ]
	then
	        updateConfigVal INTERFACES= /etc/default/isc-dhcp-server ""
	else
        	updateConfigVal INTERFACES= /etc/default/isc-dhcp-server $4
	fi
	fi
	
	if [ -f /etc/init.d/dhcp3-server ] 
	then
	sudo cp /etc/default/dhcp3-server /etc/default/dhcp3-server_BKP
	if [ -z $4 ] || [ "$4" = 'null' ]
	then
	        updateConfigVal INTERFACES= /etc/default/dhcp3-server ""
	else
        	updateConfigVal INTERFACES= /etc/default/dhcp3-server $4
	fi
	fi
}


function rollback(){
	echo "***********ERROR: ROLLBACK Started*****************"
	sudo ifdown -a

	if [ -f /etc/iptables.rules_BKP ]
	then
        	sudo mv /etc/iptables.rules_BKP /etc/iptables.rules
	fi

	if [ -f /etc/default/isc-dhcp-server_BKP ]
	then
        	sudo mv /etc/default/isc-dhcp-server_BKP /etc/default/isc-dhcp-server
	fi

	if [ -f /etc/default/dhcp3-server_BKP ]
	then
        	sudo mv /etc/default/dhcp3-server_BKP /etc/default/dhcp3-server
	fi
	
	if [ -f /etc/network/interfaces_BKP ]
	then
        	sudo mv /etc/network/interfaces_BKP /etc/network/interfaces
	fi

	sudo ifup -a
	sudo iptables-restore < /etc/iptables.rules
	
	if [ -f /etc/default/isc-dhcp-server ]
	then
        	sudo service isc-dhcp-server restart
	fi
	
	if [ -f /etc/default/dhcp3-server ]
	then
        	sudo /etc/init.d/dhcp3-server restart
	fi
	
	
	echo "***********ERROR: Network Settings RollBacked*****************"
	exit 1
}

function checkAndExit(){
        status=`echo $?`
        if [ $status -ne 0  ]
        then
		rollback
                kill -s TERM $TOP_PID
        fi
}

function changeInterfacesAndApplyOnEM(){

 	arg=0
        if [ -z "$5" ]
        then
                arg=1
        fi

        if [ $arg -gt 0 ]
        then
                echo "Arguments not proper.."
                echo "Please specify arguments in the fashion : ENTIRE_STRING_INTERFACES_FILE"
                echo "changeInterfacesAndApplyOnEM "Hello\nWorld" "
                echo "Exiting now....."
                exit 1
        fi

	echo -e $5 >$ENL_APP_HOME/Enlighted/interfaces
	checkAndExit
	sudo ifdown -a
	checkAndExit
	sudo mv /etc/network/interfaces /etc/network/interfaces_BKP
	checkAndExit
	sudo mv $ENL_APP_HOME/Enlighted/interfaces /etc/network/interfaces
	checkAndExit
	sudo ifup -a
	checkAndExit
	sudo iptables-restore < /etc/iptables.rules
	checkAndExit
	
	if [ -f /etc/default/isc-dhcp-server ]
	then
        	sudo service isc-dhcp-server stop
	fi
	
	if [ -f /etc/default/dhcp3-server ]
	then
        	sudo /etc/init.d/dhcp3-server stop
	fi
	
	
	
	if [ ! -z $4 ] && [ "$4" != 'null' ]
        then
		if [ -f /etc/default/isc-dhcp-server ]
	then
        	sudo service isc-dhcp-server start
	fi
	
	if [ -f /etc/default/dhcp3-server ]
	then
        	sudo /etc/init.d/dhcp3-server start
	fi        	
		checkAndExit
	fi
}

updateIptablesRules "$@"
checkAndExit
configureDHCPServer "$@"
checkAndExit
changeInterfacesAndApplyOnEM "$@" 
checkAndExit
curr_dir=`dirname "$0"`
if [ "$6" != "null" ]
then
	sh $6 $7 $8 $9 $10
	echo "***Updated the bacnet port configured********"
else
	echo "********bacnet port is not enabled*************8"
fi
exit 0