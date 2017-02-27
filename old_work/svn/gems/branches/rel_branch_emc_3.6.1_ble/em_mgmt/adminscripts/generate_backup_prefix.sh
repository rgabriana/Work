#!/bin/bash
source /etc/environment
prefix=$(psql -q -U postgres -h localhost ems -t -c "select trim(substring(regexp_replace(name, '[^a-z_A-Z0-9]', '', 'g') from 1 for 20)) from company where (select count(1) from company) = 1 limit 1" | sed -e 's/[ \t]*//g');
if [ ! -z $prefix ]
then
    prefix="${prefix}_"
    campus=$(psql -q -U postgres -h localhost ems -t -c "select trim(substring(regexp_replace(name, '[^a-z_A-Z0-9]', '', 'g') from 1 for 20)) from campus where (select count(1) from campus) = 1 limit 1"  | sed -e 's/[ \t]*//g');
    if [ ! -z $campus ]
    then
        prefix="${prefix}${campus}_"
        building=$(psql -q -U postgres -h localhost ems -t -c "select trim(substring(regexp_replace(name, '[^a-z_A-Z0-9]', '', 'g') from 1 for 20)) from building where (select count(1) from building) = 1 limit 1"  | sed -e 's/[ \t]*//g');
        if [ ! -z $building ]
        then
            prefix="${prefix}${building}_"
            floor=$(psql -q -U postgres -h localhost ems -t -c "select trim(substring(regexp_replace(name, '[^a-z_A-Z0-9]', '', 'g') from 1 for 20)) from floor where (select count(1) from floor) = 1 limit 1"  | sed -e 's/[ \t]*//g');
            if [ ! -z $floor ]
            then
                prefix="${prefix}${floor}_"
            fi
        fi
    fi
fi
mac=$($ENLIGHTED_HOME/getMac.sh | sed -e 's/[ \t:]*//g')
if [ ! -z $mac ]
then
    prefix="${prefix}${mac}_"
fi
echo $prefix;
