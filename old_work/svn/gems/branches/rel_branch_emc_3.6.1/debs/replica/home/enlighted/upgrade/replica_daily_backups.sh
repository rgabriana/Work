#!/bin/bash
TOMCAT_HOME=/var/lib/tomcat6/

backup() {
    db="$1"
    backup_directory="/home/enlighted/backups/${db}"
    mkdir -p ${backup_directory}
    cd "${backup_directory}"
    DAILY_BACKUP_PREFIX="daily_dump"
    rm -f ${DAILY_BACKUP_PREFIX}*backup
    rm -f CLOUD_BACKUP_VERSION

    BACKUP_VERSION=$(psql -x -U postgres ${db} -c "select value from system_configuration where name like 'gems.version.build'" | grep value | cut -d " " -f3)
    today=$(date '+%m'-'%d'-'%Y'_'%H'-'%M'-'%S')

    DAILY_BACKUP_FILE="${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}_$today.backup"
    DAILY_TEMP_TAR="${DAILY_BACKUP_PREFIX}.temptartz"

    # If the version of the EM is older than 3.6.0, create a backup file in the old format (so that it can be restored by the older version EM).
    BACKUP_VERSION_INT=$(echo "${BACKUP_VERSION}")
    BACKUP_VERSION_INT=$(echo $BACKUP_VERSION_INT | sed "s/\.//g")
    COMPARE_TO_VERSION=3.6.0.0000
    COMPARE_TO_VERSION_INT=$(echo "${COMPARE_TO_VERSION}")
    COMPARE_TO_VERSION_INT=$(echo $COMPARE_TO_VERSION_INT | sed "s/\.//g")

    if [ "$COMPARE_TO_VERSION_INT" -gt "$BACKUP_VERSION_INT" ]
    then
        backup_old
    else
        backup_new
    fi
}

backup_old() {
    # Create a single file backup for EMs older than 3.6.0
    echo ${BACKUP_VERSION} > CLOUD_BACKUP_VERSION

    pg_dump -U postgres ${db} | gzip > "${DAILY_BACKUP_FILE}"

    if [ $? -eq 0 ]
    then
        tar -zcvf "${DAILY_TEMP_TAR}" $DAILY_BACKUP_FILE CLOUD_BACKUP_VERSION
        sleep 2
        rm "$DAILY_BACKUP_FILE" CLOUD_BACKUP_VERSION
    else
        exit
    fi

    backups_no=$(ls -l $DAILY_BACKUP_PREFIX*.tar.gz | wc -l )
    while [ "${backups_no}" -gt 1 ]
    do
        REMOVE_FILE=$(ls -ltr --time-style="+%Y-%m-%d %H:%M:%S" | grep $DAILY_BACKUP_PREFIX | head -n 1 | tr -s ' ' ' ' | cut -d" " -f8)
        if [ ! -z $REMOVE_FILE ]
        then
            rm -fv "${REMOVE_FILE}"
            backups_no=$(ls -l $DAILY_BACKUP_PREFIX*.tar.gz | wc -l )
        else
            break
        fi
    done

    mv $DAILY_TEMP_TAR "${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}_$today.tar.gz"
}

backup_new() {
    EC_SCHEMA="ec_schema_dump.backup"
    EC_CSV_DIR="EC_CSV"
    ECD_CSV_DIR="ECD_CSV"
    ECH_CSV_DIR="ECH_CSV"
    PLUGLOAD_EC_CSV_DIR="PLUGLOAD_EC_CSV"
    PLUGLOAD_ECD_CSV_DIR="PLUGLOAD_ECD_CSV"
    PLUGLOAD_ECH_CSV_DIR="PLUGLOAD_ECH_CSV"

    echo ${BACKUP_VERSION} > CLOUD_BACKUP_VERSION
    echo "FORMAT:CSV_SPLIT"  >> CLOUD_BACKUP_VERSION

    pg_dump -U postgres -T energy_consumption -T energy_consumption_daily -T energy_consumption_hourly -T plugload_energy_consumption -T plugload_energy_consumption_daily -T plugload_energy_consumption_hourly ${db} | gzip > "${DAILY_BACKUP_FILE}"
    pg_dump -i -U postgres -v -s -t energy_consumption -t energy_consumption_hourly -t energy_consumption_daily -t plugload_energy_consumption -t plugload_energy_consumption_hourly -t plugload_energy_consumption_daily -f  "$EC_SCHEMA" ${db}

########################################  *** sachink  Energy Consumption ***  #################################################################
# first run the tigger and function creation script
	psql -Upostgres ${db} < $TOMCAT_HOME/webapps/em_cloud_instance/adminscripts/ec_trigger.sql

	BACKUP_SUCCESS=0
    EC_END_DATE=`psql -Upostgres ${db} -t -c "select max(capture_at) from energy_consumption;" | cut -d' ' -f2`
    if [[ -n $EC_END_DATE ]]
    then
	    INT_EC_END_DATE=$(date -d $EC_END_DATE +"%Y%m%d")
	    EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='ec_last_capture_at';" | cut -d' ' -f2`

	    if [[ -z $EC_START_DATE ||  $EC_START_DATE -eq -1 ]]
	    then
	        EC_START_DATE=`psql -Upostgres ${db} -t -c "select min(capture_at) from energy_consumption;" | cut -d' ' -f2`
		INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")
		psql -Upostgres ${db} -c "update cloud_config set val=${INT_EC_START_DATE} where name='ec_last_capture_at';"
	    fi
	    INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")
	    mkdir -p $EC_CSV_DIR
	    while [ $INT_EC_START_DATE -le $INT_EC_END_DATE ]; do
			echo "$(date) : CREATING FILE EC_DATE=$INT_EC_START_DATE"
			YEAR=${INT_EC_START_DATE:0:4}
            MONTH=${INT_EC_START_DATE:4:2}
            DAY=${INT_EC_START_DATE:6:2}
            ST_DT=${YEAR}${MONTH}${DAY}
			ST_DT_PLUS_ONE_DAY=$(date -d $INT_EC_START_DATE" +1 days" +"%Y%m%d")
	        `psql -U postgres ${db} -c "\\copy (select * from energy_consumption where capture_at  >= '${ST_DT}' and capture_at < '${ST_DT_PLUS_ONE_DAY}' order by capture_at asc) to STDOUT CSV HEADER" | gzip > $EC_CSV_DIR/ec_$INT_EC_START_DATE.gz`
	        BACKUP_SUCCESS=$?
	        if [ $BACKUP_SUCCESS -ne 0 ]
	        then
	        	break
	        fi
	        INT_EC_START_DATE=$(date -d $INT_EC_START_DATE" +1 days" +"%Y%m%d")
	    done
	    if [ $BACKUP_SUCCESS -eq 0 ]
	    then
			INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")
		    NEW_EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='ec_last_capture_at';" | cut -d' ' -f2`
		    NEW_INT_EC_START_DATE=$(date -d $NEW_EC_START_DATE +"%Y%m%d")
		    if [[ -n $NEW_INT_EC_START_DATE && $NEW_INT_EC_START_DATE -eq $INT_EC_START_DATE ]]
		    then
			psql -Upostgres ${db} -c "update cloud_config set val=${INT_EC_END_DATE} where name='ec_last_capture_at';"
		    fi

		    DATE_6M=$(date -d "-6 months" +"%Y%m%d")
		    NUM_RECORDS_DELETED=$(psql -U postgres ${db} -c "delete from energy_consumption where capture_at < '${DATE_6M}';")
		    if [ "${NUM_RECORDS_DELETED}" = "DELETE 0" ]
		    then
		    	echo "$(date) : DB DOES NOT CONTAIN ENERGY_CONSUMPTION RECORDS OLDER THAN 6 MONTHS TO PRUNE"
		    else
		    	echo "$(date) : ${NUM_RECORDS_DELETED} : DB CONTAINS ENERGY_CONSUMPTION RECORDS OLDER THAN 6 MONTHS PRUNED FROM DB BACKUP EXISTS IN CSV FILES"
		    fi
        else
                echo "$(date) : **** BACKUP OF ENERGY_CONSUMPTION WAS INTERRUPTED NOT PRUNING DB **"
		fi

	    DATE_3M=$(date -d "-2 months" +"%Y%m%d")
	    rm -rf ec_csvfiles
	    for f in $EC_CSV_DIR/*.gz; do
		    if [ "$f" = "$EC_CSV/*.gz" ]; then
	                break
	        fi

	        FDATE=`echo $f |cut -d'/' -f2 | cut -d'_' -f2 | cut -d'.' -f1`
			if [ "$FDATE" -ge "$DATE_3M" ]
			then
				echo $f >> ec_csvfiles
			fi
	    done

    fi

########################################  *** sachink  Energy Consumption Daily ***  ###########################################################
# first run the tigger and function creation script
	psql -Upostgres ${db} < $TOMCAT_HOME/webapps/em_cloud_instance/adminscripts/ecd_trigger.sql

    BACKUP_SUCCESS=0
    EC_END_DATE=`psql -Upostgres ${db} -t -c "select max(capture_at) from energy_consumption_daily;" | cut -d' ' -f2`
    if [[ -n $EC_END_DATE ]]
    then
	    INT_EC_END_DATE=$(date -d $EC_END_DATE +"%Y%m")
	    EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='ecd_last_capture_at';" | cut -d' ' -f2`
	    if [[ -z $EC_START_DATE ||  $EC_START_DATE -eq -1 ]]
	    then
	        EC_START_DATE=`psql -Upostgres ${db} -t -c "select min(capture_at) from energy_consumption_daily;" | cut -d' ' -f2`
			INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m")
			psql -Upostgres ${db} -c "update cloud_config set val='${INT_EC_START_DATE}01' where name='ecd_last_capture_at';"
	    fi
	    INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m")
	    mkdir -p $ECD_CSV_DIR
	    while [ $INT_EC_START_DATE -le $INT_EC_END_DATE ]; do
			echo "$(date) : CREATING FILE ECD_DATE=$INT_EC_START_DATE"
    		YEAR=${INT_EC_START_DATE:0:4}
    		MONTH=${INT_EC_START_DATE:4:2}
    		ST_DT=${YEAR}'-'${MONTH}'-01 00:00:00'
    		ST_DT_PLUS_ONE_MONTH=$(date -d $INT_EC_START_DATE"01 +1 months" +"%Y%m%d")
        	`psql -U postgres ${db} -c "\\copy (select * from energy_consumption_daily where capture_at >= '${ST_DT}' and capture_at <  '${ST_DT_PLUS_ONE_MONTH}' order by capture_at asc) to STDOUT CSV HEADER" | gzip > $ECD_CSV_DIR/ecd_$INT_EC_START_DATE.gz`
	        BACKUP_SUCCESS=$?
	        if [ $BACKUP_SUCCESS -ne 0 ]
	        then
	        	break
	        fi

	    		INT_EC_START_DATE=$(date -d $INT_EC_START_DATE"01 +1 months" +"%Y%m")
	    done
	    if [ $BACKUP_SUCCESS -eq 0 ]
	    then

		    INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m")
		    NEW_EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='ecd_last_capture_at';" | cut -d' ' -f2`
		    NEW_INT_EC_START_DATE=$(date -d $NEW_EC_START_DATE +"%Y%m")
		    if [[ -n $NEW_INT_EC_START_DATE && $NEW_INT_EC_START_DATE -eq $INT_EC_START_DATE ]]
		    then
				psql -Upostgres ${db} -c "update cloud_config set val='${INT_EC_END_DATE}01' where name='ecd_last_capture_at';"
		    fi
		    DATE_6M=$(date -d "-6 months" +"%Y%m%d")
		    NUM_RECORDS_DELETED=$(psql -U postgres ${db} -c "delete from energy_consumption_daily where capture_at < '${DATE_6M}';")
		    if [ "${NUM_RECORDS_DELETED}" = "DELETE 0" ]
		    then
		    	echo "$(date) : DB DOES NOT CONTAIN ENERGY_CONSUMPTION_DAILY RECORDS OLDER THAN 6 MONTHS TO PRUNE"
		    else
		    	echo "$(date) : ${NUM_RECORDS_DELETED} : DB CONTAINS ENERGY_CONSUMPTION_DAILY RECORDS OLDER THAN 6 MONTHS PRUNED FROM DB BACKUP EXISTS IN CSV FILES"
		    fi
        else
                echo "$(date) : **** BACKUP OF ENERGY_CONSUMPTION_DAILY WAS INTERRUPTED NOT PRUNING DB **"
		fi
	    DATE_10Y=$(date -d "-10 years" +"%Y%m")
	    rm -rf ecd_csvfiles
	    for f in $ECD_CSV_DIR/*.gz; do
	    	if [ "$f" = "$ECD_CSV/*.gz" ]; then
            	    break
        	fi

	        FDATE=`echo $f |cut -d'/' -f2 | cut -d'_' -f2 | cut -d'.' -f1`
			if [ "$FDATE" -ge "$DATE_10Y" ]
			then
				echo $f >> ecd_csvfiles
			fi
	    done


    fi
########################################  *** sachink  Energy Consumption Hourly ***  ##########################################################
# first run the tigger and function creation script
	psql -Upostgres ${db} < $TOMCAT_HOME/webapps/em_cloud_instance/adminscripts/ech_trigger.sql

    BACKUP_SUCCESS=0
    EC_END_DATE=`psql -Upostgres ${db} -t -c "select max(capture_at) from energy_consumption_hourly;" | cut -d' ' -f2`
    if [[ -n $EC_END_DATE ]]
    then
	    INT_EC_END_DATE=$(date -d $EC_END_DATE +"%Y%m%d")
	    EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='ech_last_capture_at';" | cut -d' ' -f2`

	    if [[ -z $EC_START_DATE ||  $EC_START_DATE -eq -1 ]]
	    then
	        EC_START_DATE=`psql -Upostgres ${db} -t -c "select min(capture_at) from energy_consumption_hourly;" | cut -d' ' -f2`
			INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")
			psql -Upostgres ${db} -c "update cloud_config set val=${INT_EC_START_DATE} where name='ech_last_capture_at';"
	    fi
	    INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")
	    mkdir -p $ECH_CSV_DIR
	    while [ $INT_EC_START_DATE -le $INT_EC_END_DATE ]; do
			echo "$(date) : CREATING FILE ECH_DATE=$INT_EC_START_DATE"
			YEAR=${INT_EC_START_DATE:0:4}
            MONTH=${INT_EC_START_DATE:4:2}
            DAY=${INT_EC_START_DATE:6:2}
            ST_DT=${YEAR}${MONTH}${DAY}
			ST_DT_PLUS_ONE_DAY=$(date -d $INT_EC_START_DATE" +1 days" +"%Y%m%d")
	        `psql -U postgres ${db} -c "\\copy (select * from energy_consumption_hourly where capture_at >= '$INT_EC_START_DATE' and capture_at < '${ST_DT_PLUS_ONE_DAY}' order by capture_at asc) to STDOUT CSV HEADER" | gzip > $ECH_CSV_DIR/ech_$INT_EC_START_DATE.gz`
	        BACKUP_SUCCESS=$?
	        if [ $BACKUP_SUCCESS -ne 0 ]
	        then
	        	break
	        fi

	        INT_EC_START_DATE=$(date -d $INT_EC_START_DATE" +1 days" +"%Y%m%d")
	    done
	    if [ $BACKUP_SUCCESS -eq 0 ]
	    then

		    INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")

		    NEW_EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='ech_last_capture_at';" | cut -d' ' -f2`
		    NEW_INT_EC_START_DATE=$(date -d $NEW_EC_START_DATE +"%Y%m%d")
		    if [[ -n $NEW_INT_EC_START_DATE && $NEW_INT_EC_START_DATE -eq $INT_EC_START_DATE ]]
		    then
				psql -Upostgres ${db} -c "update cloud_config set val=${INT_EC_END_DATE} where name='ech_last_capture_at';"
		    fi

		    DATE_6M=$(date -d "-6 months" +"%Y%m%d")
		    NUM_RECORDS_DELETED=$(psql -U postgres ${db} -c "delete from energy_consumption_hourly where capture_at < '${DATE_6M}';")
		    if [ "$NUM_RECORDS_DELETED" = "DELETE 0" ]
		    then
		    	echo "$(date) : DB DOES NOT CONTAIN ENERGY_CONSUMPTION_HOURLY RECORDS OLDER THAN 6 MONTHS TO PRUNE"
		    else
		    	echo "$(date) : ${NUM_RECORDS_DELETED} : DB CONTAINS ENERGY_CONSUMPTION_HOURLY RECORDS OLDER THAN 6 MONTHS PRUNED FROM DB BACKUP EXISTS IN CSV FILES"
		    fi
        else
            echo "$(date) : **** BACKUP OF ENERGY_CONSUMPTION_HOURLY WAS INTERRUPTED NOT PRUNING DB **"
		fi
	    DATE_2Y=$(date -d "-2 years" +"%Y%m%d")
	    rm -rf ech_csvfiles
	    for f in $ECH_CSV_DIR/*.gz; do
		    if [ "$f" = "$ECH_CSV/*.gz" ]; then
	                break
	        fi

	        FDATE=`echo $f |cut -d'/' -f2 | cut -d'_' -f2 | cut -d'.' -f1`
			if [ "$FDATE" -ge "$DATE_2Y" ]
			then
				echo $f >> ech_csvfiles
			fi
	    done

    fi

#################################################################################################################################
########################################  *** sachink  Plugload Energy Consumption ***  ####################################################
# check if plugload table exists
PL_TABL=`psql -Upostgres ${db} -t -c "select table_name from information_schema.tables where table_catalog = '${db}' and table_name='plugload_energy_consumption';"`
if [ -n $PL_TABL ]
then
	# first run the tigger and function creation script
		psql -Upostgres ${db} < $TOMCAT_HOME/webapps/em_cloud_instance/adminscripts/plugload_ec_trigger.sql

	    BACKUP_SUCCESS=0
	    EC_END_DATE=`psql -Upostgres ${db} -t -c "select max(capture_at) from plugload_energy_consumption;" | cut -d' ' -f2`
	    if [[ -n $EC_END_DATE ]]
	    then
		    INT_EC_END_DATE=$(date -d $EC_END_DATE +"%Y%m%d")
		    EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='plugload_ec_last_capture_at';" | cut -d' ' -f2`

		    if [[ -z $EC_START_DATE ||  $EC_START_DATE -eq -1 ]]
		    then
		        EC_START_DATE=`psql -Upostgres ${db} -t -c "select min(capture_at) from plugload_energy_consumption;" | cut -d' ' -f2`
			INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")
			psql -Upostgres ${db} -c "update cloud_config set val=${INT_EC_START_DATE} where name='plugload_ec_last_capture_at';"
		    fi
		    INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")
		    mkdir -p $PLUGLOAD_EC_CSV_DIR
		    while [ $INT_EC_START_DATE -le $INT_EC_END_DATE ]; do
				echo "$(date) : CREATING PLUGLOAD_FILE EC_DATE=$INT_EC_START_DATE"
				YEAR=${INT_EC_START_DATE:0:4}
	            MONTH=${INT_EC_START_DATE:4:2}
	            DAY=${INT_EC_START_DATE:6:2}
	            ST_DT=${YEAR}${MONTH}${DAY}
	            ST_DT_PLUS_ONE_DAY=$(date -d $INT_EC_START_DATE" +1 days" +"%Y%m%d")
		        `psql -U postgres ${db} -c "\\copy (select * from plugload_energy_consumption where capture_at >= '$INT_EC_START_DATE' and capture_at < '${ST_DT_PLUS_ONE_DAY}' order by capture_at asc) to STDOUT CSV HEADER" | gzip > $PLUGLOAD_EC_CSV_DIR/plugloadec_$INT_EC_START_DATE.gz`
		        BACKUP_SUCCESS=$?
		        if [ $BACKUP_SUCCESS -ne 0 ]
		        then
		        	break
		        fi

		        INT_EC_START_DATE=$(date -d $INT_EC_START_DATE" +1 days" +"%Y%m%d")
		    done
		    if [ $BACKUP_SUCCESS -eq 0 ]
		    then

				INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")
			    NEW_EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='plugload_ec_last_capture_at';" | cut -d' ' -f2`
			    NEW_INT_EC_START_DATE=$(date -d $NEW_EC_START_DATE +"%Y%m%d")
			    if [[ -n $NEW_INT_EC_START_DATE && $NEW_INT_EC_START_DATE -eq $INT_EC_START_DATE ]]
			    then
				psql -Upostgres ${db} -c "update cloud_config set val=${INT_EC_END_DATE} where name='plugload_ec_last_capture_at';"
			    fi
			    DATE_6M=$(date -d "-6 months" +"%Y%m%d")
			    NUM_RECORDS_DELETED=$(psql -U postgres ${db} -c "delete from plugload_energy_consumption where capture_at < '${DATE_6M}';")
			    if [ "$NUM_RECORDS_DELETED" = "DELETE 0" ]
			    then
			    	echo "$(date) : DB DOES NOT CONTAIN PLUGLOAD_ENERGY_CONSUMPTION RECORDS OLDER THAN 6 MONTHS TO PRUNE"
			    else
			    	echo "$(date) : ${NUM_RECORDS_DELETED} : DB CONTAINS PLUGLOAD_ENERGY_CONSUMPTION RECORDS OLDER THAN 6 MONTHS PRUNED FROM DB BACKUP EXISTS IN CSV FILES"
			    fi
	            else
	                echo "$(date) : **** BACKUP OF PLUGLOAD_ENERGY_CONSUMPTION WAS INTERRUPTED NOT PRUNING DB **"
			fi
		    DATE_3M=$(date -d "-2 months" +"%Y%m%d")
		    rm -rf plugload_ec_csvfiles
		    for f in $PLUGLOAD_EC_CSV_DIR/*.gz; do
				if [ "$f" = "$PLUGLOAD_EC_CSV_DIR/*.gz" ]; then
	                break
	        	fi

		        FDATE=`echo $f |cut -d'/' -f2 | cut -d'_' -f2 | cut -d'.' -f1`
				if [ "$FDATE" -ge "$DATE_3M" ]
				then
					echo $f >> plugload_ec_csvfiles
				fi
		    done
	    fi
fi

########################################  *** sachink  Plugload Energy Consumption Daily ***  ##################################################
# check if plugload table exists
PL_TABL=`psql -Upostgres ${db} -t -c "select table_name from information_schema.tables where table_catalog = '${db}' and table_name='plugload_energy_consumption_daily';"`
if [ -n $PL_TABL ]
then

	# first run the tigger and function creation script
		psql -Upostgres ${db} < $TOMCAT_HOME/webapps/em_cloud_instance/adminscripts/plugload_ecd_trigger.sql

	    BACKUP_SUCCESS=0
	    EC_END_DATE=`psql -Upostgres ${db} -t -c "select max(capture_at) from plugload_energy_consumption_daily;" | cut -d' ' -f2`
	    if [[ -n $EC_END_DATE ]]
	    then
		    INT_EC_END_DATE=$(date -d $EC_END_DATE +"%Y%m")
		    EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='plugload_ecd_last_capture_at';" | cut -d' ' -f2`
		    if [[ -z $EC_START_DATE ||  $EC_START_DATE -eq -1 ]]
		    then
		        EC_START_DATE=`psql -Upostgres ${db} -t -c "select min(capture_at) from plugload_energy_consumption_daily;" | cut -d' ' -f2`
				INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m")
				psql -Upostgres ${db} -c "update cloud_config set val='${INT_EC_START_DATE}01' where name='plugload_ecd_last_capture_at';"
		    fi
		    INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m")
		    mkdir -p $PLUGLOAD_ECD_CSV_DIR
		    while [ $INT_EC_START_DATE -le $INT_EC_END_DATE ]; do
				echo "$(date) : CREATING PLUGLOAD_FILE ECD_DATE=$INT_EC_START_DATE"
	            YEAR=${INT_EC_START_DATE:0:4}
	            MONTH=${INT_EC_START_DATE:4:2}
	            ST_DT=${YEAR}'-'${MONTH}'-01 00:00:00'
				ST_DT_PLUS_ONE_MONTH=$(date -d $INT_EC_START_DATE"01 +1 months" +"%Y%m%d")
		    	`psql -U postgres ${db} -c "\\copy (select * from plugload_energy_consumption_daily where capture_at >= '${ST_DT}' and capture_at <  '${ST_DT_PLUS_ONE_MONTH}' order by capture_at asc) to STDOUT CSV HEADER" | gzip > $PLUGLOAD_ECD_CSV_DIR/plugloadecd_$INT_EC_START_DATE.gz`
		        BACKUP_SUCCESS=$?
		        if [ $BACKUP_SUCCESS -ne 0 ]
		        then
		        	break
		        fi

		    	INT_EC_START_DATE=$(date -d $INT_EC_START_DATE"01 +1 months" +"%Y%m")
		    done

		    if [ $BACKUP_SUCCESS -eq 0 ]
		    then

				INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m")
			    NEW_EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='plugload_ecd_last_capture_at';" | cut -d' ' -f2`
			    NEW_INT_EC_START_DATE=$(date -d $NEW_EC_START_DATE +"%Y%m")
			    if [[ -n $NEW_INT_EC_START_DATE && $NEW_INT_EC_START_DATE -eq $INT_EC_START_DATE ]]
			    then
					psql -Upostgres ${db} -c "update cloud_config set val='${INT_EC_END_DATE}01' where name='plugload_ecd_last_capture_at';"
			    fi

			    DATE_6M=$(date -d "-6 months" +"%Y%m%d")
			    NUM_RECORDS_DELETED=$(psql -U postgres ${db} -c "delete from plugload_energy_consumption_daily where capture_at < '${DATE_6M}';")
			    if [ "$NUM_RECORDS_DELETED" = "DELETE 0" ]
			    then
			    	echo "$(date) : DB DOES NOT CONTAIN PLUGLOAD_ENERGY_CONSUMPTION_DAILY RECORDS OLDER THAN 6 MONTHS TO PRUNE"
			    else
			    	echo "$(date) : ${NUM_RECORDS_DELETED} : DB CONTAINS PLUGLOAD_ENERGY_CONSUMPTION_DAILY RECORDS OLDER THAN 6 MONTHS PRUNED FROM DB BACKUP EXISTS IN CSV FILES"
			    fi
	            else
	                echo "$(date) : **** BACKUP OF PLUGLOAD_ENERGY_CONSUMPTION_DAILY WAS INTERRUPTED NOT PRUNING DB **"
			fi
		    DATE_10Y=$(date -d "-10 years" +"%Y%m")
		    rm -rf plugload_ecd_csvfiles
		    for f in $PLUGLOAD_ECD_CSV_DIR/*.gz; do
				if [ "$f" = "$PLUGLOAD_ECD_CSV_DIR/*.gz" ]; then
	                break
	        	fi

		        FDATE=`echo $f |cut -d'/' -f2 | cut -d'_' -f2 | cut -d'.' -f1`
				if [ "$FDATE" -ge "$DATE_10Y" ]
				then
					echo $f >> plugload_ecd_csvfiles
				fi
		    done
	    fi
fi
########################################  *** sachink Plugload  Energy Consumption Hourly ***  ###############################################
# check if plugload table exists
PL_TABL=`psql -Upostgres ${db} -t -c "select table_name from information_schema.tables where table_catalog = '${db}' and table_name='plugload_energy_consumption_hourly';"`
if [ -n $PL_TABL ]
then

	# first run the tigger and function creation script
		psql -Upostgres ${db} < $TOMCAT_HOME/webapps/em_cloud_instance/adminscripts/plugload_ech_trigger.sql

	    BACKUP_SUCCESS=0
	    EC_END_DATE=`psql -Upostgres ${db} -t -c "select max(capture_at) from plugload_energy_consumption_hourly;" | cut -d' ' -f2`
	    if [[ -n $EC_END_DATE ]]
	    then
		    INT_EC_END_DATE=$(date -d $EC_END_DATE +"%Y%m%d")
		    EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='plugload_ech_last_capture_at';" | cut -d' ' -f2`

		    if [[ -z $EC_START_DATE ||  $EC_START_DATE -eq -1 ]]
		    then
		        EC_START_DATE=`psql -Upostgres ${db} -t -c "select min(capture_at) from plugload_energy_consumption_hourly;" | cut -d' ' -f2`
				INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")
				psql -Upostgres ${db} -c "update cloud_config set val=${INT_EC_START_DATE} where name='plugload_ech_last_capture_at';"
		    fi
		    INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")
		    mkdir -p $PLUGLOAD_ECH_CSV_DIR
		    while [ $INT_EC_START_DATE -le $INT_EC_END_DATE ]; do
				echo "$(date) : CREATING PLUGLOAD_FILE ECH_DATE=$INT_EC_START_DATE"
				YEAR=${INT_EC_START_DATE:0:4}
	            MONTH=${INT_EC_START_DATE:4:2}
	            DAY=${INT_EC_START_DATE:6:2}
	            ST_DT=${YEAR}${MONTH}${DAY}
				ST_DT_PLUS_ONE_DAY=$(date -d $INT_EC_START_DATE" +1 days" +"%Y%m%d")
		        `psql -U postgres ${db} -c "\\copy (select * from plugload_energy_consumption_hourly where capture_at >= '$INT_EC_START_DATE' and capture_at < '${ST_DT_PLUS_ONE_DAY}'  order by capture_at asc) to STDOUT CSV HEADER" | gzip > $PLUGLOAD_ECH_CSV_DIR/plugloadech_$INT_EC_START_DATE.gz`
		        	        BACKUP_SUCCESS=$?
		        if [ $BACKUP_SUCCESS -ne 0 ]
		        then
		        	break
		        fi

		        INT_EC_START_DATE=$(date -d $INT_EC_START_DATE" +1 days" +"%Y%m%d")
		    done

		    if [ $BACKUP_SUCCESS -eq 0 ]
		    then

			    INT_EC_START_DATE=$(date -d $EC_START_DATE +"%Y%m%d")

			    NEW_EC_START_DATE=`psql -Upostgres ${db} -t -c "select val from cloud_config where name='plugload_ech_last_capture_at';" | cut -d' ' -f2`
			    NEW_INT_EC_START_DATE=$(date -d $NEW_EC_START_DATE +"%Y%m%d")
			    if [[ -n $NEW_INT_EC_START_DATE && $NEW_INT_EC_START_DATE -eq $INT_EC_START_DATE ]]
			    then
					psql -Upostgres ${db} -c "update cloud_config set val=${INT_EC_END_DATE} where name='plugload_ech_last_capture_at';"
			    fi
			    DATE_6M=$(date -d "-6 months" +"%Y%m%d")
			    NUM_RECORDS_DELETED=$(psql -U postgres ${db} -c "delete from plugload_energy_consumption_hourly where capture_at < '${DATE_6M}';")
			    if [ "$NUM_RECORDS_DELETED" = "DELETE 0" ]
			    then
			    	echo "$(date) : DB DOES NOT CONTAIN PLUGLOAD_ENERGY_CONSUMPTION_HOURLY RECORDS OLDER THAN 6 MONTHS TO PRUNE"
			    else
			    	echo "$(date) : ${NUM_RECORDS_DELETED} : DB CONTAINS PLUGLOAD_ENERGY_CONSUMPTION_HOURLY RECORDS OLDER THAN 6 MONTHS PRUNED FROM DB BACKUP EXISTS IN CSV FILES"
			    fi
	            else
	                echo "$(date) : **** BACKUP OF PLUGLOAD_ENERGY_CONSUMPTION_HOURLY WAS INTERRUPTED NOT PRUNING DB **"
			fi
			    DATE_2Y=$(date -d "-2 years" +"%Y%m%d")
			    rm -rf plugload_ech_csvfiles
			    for f in $PLUGLOAD_ECH_CSV_DIR/*.gz; do
					if [ "$f" = "$PLUGLOAD_ECH_CSV_DIR/*.gz" ]; then
		                break
		        	fi

			        FDATE=`echo $f |cut -d'/' -f2 | cut -d'_' -f2 | cut -d'.' -f1`
					if [ "$FDATE" -ge "$DATE_2Y" ]
					then
						echo $f >> plugload_ech_csvfiles
					fi
			    done
	    fi
fi
#################################################################################################################################
echo "Now creating tar file with setup data and EC schema"

    tar -cvf "${DAILY_TEMP_TAR}" "$DAILY_BACKUP_FILE" "$EC_SCHEMA" CLOUD_BACKUP_VERSION
    sleep 2
echo "remove work files now that they are in the tar file"
    rm "$DAILY_BACKUP_FILE" "$EC_SCHEMA" CLOUD_BACKUP_VERSION

############################# now create a list of csv files for last 3 months and append them to the tar file #################

    if [ -f ec_csvfiles ]
    then
    	tar rvf "${DAILY_TEMP_TAR}" -T ec_csvfiles
    	echo "added energy_consumption files to tar file"
    fi
    if [  -f ecd_csvfiles ]
    then
    	tar rvf "${DAILY_TEMP_TAR}" -T ecd_csvfiles
    	echo "added energy_consumption_daily files to tar file"
    fi
    if [  -f ech_csvfiles ]
    then
    	tar rvf "${DAILY_TEMP_TAR}" -T ech_csvfiles
    	echo "added energy_consumption_hourly files to tar file"
    fi
    if [ -f plugload_ec_csvfiles ]
    then
    	tar rvf "${DAILY_TEMP_TAR}" -T plugload_ec_csvfiles
    	echo "added plugload_energy_consumption files to tar file"
    fi
    if [  -f plugload_ecd_csvfiles ]
    then
    	tar rvf "${DAILY_TEMP_TAR}" -T plugload_ecd_csvfiles
    	echo "added plugload_energy_consumption_daily files to tar file"
    fi
    if [  -f plugload_ech_csvfiles ]
    then
    	tar rvf "${DAILY_TEMP_TAR}" -T plugload_ech_csvfiles
    	echo "added plugload_energy_consumption_hourly files to tar file"
    fi

    sleep 2

echo "Compressing (gzip) the tar file"
    gzip -9 "${DAILY_TEMP_TAR}"
echo "checking number of backup files and remove if more than 2"
    backups_no=$(ls -l $DAILY_BACKUP_PREFIX*.tar.gz | wc -l )


    while [ "${backups_no}" -gt 1 ]
    do
        REMOVE_FILE=$(ls -ltr --time-style="+%Y-%m-%d %H:%M:%S" | grep $DAILY_BACKUP_PREFIX.*tar.gz | head -n 1 | tr -s ' ' ' ' | cut -d" " -f8)
        if [ ! -z $REMOVE_FILE ]
        then
            rm -f "${REMOVE_FILE}"
            echo "removed backup file ${REMOVE_FILE} since it is older than the latest two"
            backups_no=$(ls -l $DAILY_BACKUP_PREFIX*.tar.gz | wc -l )
        else
            break
        fi
    done

    mv $DAILY_TEMP_TAR".gz" "${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}_$today.tar.gz"
    echo "moved temp tar file to file with todays timestamp filename is : ${DAILY_BACKUP_PREFIX}_${BACKUP_VERSION}_$today.tar.gz"

}

# create backups dir and also create a list of DB's to be backed up
mkdir -p /home/enlighted/backups
echo "create a list of DB's to backup"
psql -x -U postgres -c "select datname from pg_database where datname like 'em_%' order by datname" | grep datname | cut -d" " -f3 > "/home/enlighted/backups/alldbs"

# iterate through the list and run backup on each DB.
echo "# iterate through the list and run backup on each DB."
while read line
do
	echo "running backup on DB : $line "
    backup "$line"
done < /home/enlighted/backups/alldbs

