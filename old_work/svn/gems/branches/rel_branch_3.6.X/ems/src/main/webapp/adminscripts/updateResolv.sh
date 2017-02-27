#!/bin/bash
function contains(){
	## $2 is the reference array and $1 is the element to check whether the reference array contains $1
	a=("${!2}")
	for chkval in ${a[@]}; 
	do
		if [ "$chkval" == "$1"  ]; 
		then 
			return 0;
		fi; 
	done;
	return 1;
}
nameservers=''
search=''
if [ -z "$1" ];
then
	nameservers=$(psql -q -U postgres -d ems -t -c"select array_to_string(array_agg(s.dns), ' ') from network_settings s where s.port_enabled='t' and s.configure_ipv4 != 'DHCP'")
	search=$(psql -q -U postgres -d ems -t -c"select array_to_string(array_agg(s.search_domain), ' ')  from network_settings s where s.port_enabled='t' and s.configure_ipv4 != 'DHCP'")
else
	nameservers=$1
	search=$2
fi
sudo cp /etc/resolv.conf /etc/resolv.conf_BKP
newNameserverArr=`echo "$nameservers" | tr " " "\n"`
origNameserverArr=$(cat /etc/resolv.conf | grep "nameserver" | awk '{print $2} ')
for x in $newNameserverArr; 
do 
	contains $x origNameserverArr[@] ;
	if [ $? -eq 1 ];
	then
		## APPEND THE resolve.conf  with this new val;
		echo -e "nameserver $x " | sudo tee -a /etc/resolv.conf;
	fi
done;
origSearchStr=`cat /etc/resolv.conf | grep "search" | cut -d ' ' -f3-`
origSearchArr=$(cat /etc/resolv.conf | grep "search" | cut -d ' ' -f3-)
newSearchArr=`echo "$search" | tr " " "\n"`
for x in $newSearchArr;
do
	contains $x origSearchArr[@];
	if [ $? -eq 1 ];
	then 
		origSearchStr="$origSearchStr $x";
	fi;
done;
if [ ! -z "$origSearchStr" ]
then
	sudo sed -i '/search/d' /etc/resolv.conf
	echo -e "search $origSearchStr" | sudo tee -a /etc/resolv.conf
fi
