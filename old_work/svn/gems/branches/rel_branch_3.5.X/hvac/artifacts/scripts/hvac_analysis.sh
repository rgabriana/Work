#!/bin/bash
ST=$(date +"%Y-%m-%d 00:00:00")
ET=$(date +"%Y-%m-%d 00:00:00" -d "+1 days")

function usage
{
    echo "usage: hvac_analysis.sh [-st <YYYY-MM-DD HH:MM:SS>] | [-h]"
}

function queryZones
{
        echo "Processing " $ST 
	ZONES=$(psql -U postgres almaden_hvac -F"," -t -A -c "select * from zones")
	for ZONE in $ZONES
	do
		IFS="," ZD=$(echo $ZONE | tr ',' '\n')
		IFS=" " 
		ZA=( $ZD )
		#echo ${ZA[0]} " => " ${ZA[1]}
		if [ ${ZA[1]} != "" ]; then
			queryZoneData ${ZA[0]} ${ZA[1]}
		fi
	done
}

function queryZoneData
{
	echo "Processing " $2
	psql -U postgres almaden_hvac -F"," -A -c "select sh.capture_at, bit_or(sh.motion_bits) as occ_bits, avg(sh.avg_temperature) as avgTemp, avg(sh.avg_ambient_light) as avgAmbientLevel, sum(sh.power_used) as powerUsed, sum(sh.base_power_used) as basePowerUsed, count(zero_bucket) as fxReportingCount from sensor_history sh join sensor s on s.mac_address = sh.mac_address join zones_sensor zs on s.id = zs.sensor_id where  capture_at > '$ST' and zs.zone_id=$1 and zero_bucket=0 group by sh.capture_at order by sh.capture_at desc" > $2.csv
}

##### Main
interactive=
while [ "$1" != "" ]; do
    case $1 in
        -st | --startime)       shift
                                ST=$1
                                ;;
        -et | --endtime)        shift
                                ET=$1
                                ;;
        -h | --help )           usage
                                exit
                                ;;
        * )                     usage
                                exit 1
    esac
    shift
done

queryZones

