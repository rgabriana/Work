#!/bin/sh

EMGRPID=$1

O2=$(psql -U postgres -d ems -t -A -F','  -c "select * from groups where id=$EMGRPID"  | awk -F"," '{print $4 " " $5}')
EM_PF_ID=$(echo $O2 | awk '{print $1}')
EM_PF_NO=$(echo $O2 | awk '{print $2}')
echo "#Profile Handler ID:\n$EM_PF_ID\n#ProfileNO:\n$EM_PF_NO"

O3=$(psql -U postgres -d ems -t -A -F','  -c "select * from profile_handler where id=$EM_PF_ID")
echo "#Profile configuration:\n$O3"
PR_ST=$(echo $O3 | awk -F"," '{print $2}')
PR_EN=$(echo $O3 | awk -F"," '{print $13}')
PR_CF=$(echo $O3 | awk -F"," '{print $14}')

O4=$(psql -U postgres -d ems -t -A -F','  -c "select * from profile where id>=$PR_ST and id<=$PR_EN order by id")
echo "#Profiles:\n$O4"

O5=$(psql -U postgres -d ems -t -A -F','  -c "select * from profile_configuration where id=$PR_CF")
echo "#Profile Configuration:\n$O5"

O6=$(psql -U postgres -d ems -t -A -F','  -c "select * from weekday where profile_configuration_id=$PR_CF order by short_order")
echo "#Weekday:\n$O6"
