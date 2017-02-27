#!/bin/bash
if [ `psql -q -Upostgres ems -t -c "select count(*) from users"`  ]
    then
	echo "upgrade"
    else
        su postgres -c /home/enlighted/installdb.sh > /home/enlighted/installdb.log
fi
rm $0