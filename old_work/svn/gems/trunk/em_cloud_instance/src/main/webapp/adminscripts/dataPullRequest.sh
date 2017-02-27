#!/bin/bash

taskId=$1
dbName=$2
tableName=$3
fromDate=$4
toDate=$5


if [ -z $1 ]
then
    exit 1
fi

output=0 

dataRequestDir="/var/lib/tomcat6/Enlighted/dataPullRequests/$taskId"
rm -rf $dataRequestDir
mkdir -p $dataRequestDir

if [ ! -d $dataRequestDir ]
then
    exit 1
fi

cd $dataRequestDir

dataPrep() {

    [ ! -z "$taskId" ] && [ ! -z "$dbName" ]  && [ ! -z "$tableName" ] && [ ! -z "$fromDate" ] && [ ! -z "$toDate" ] && echo "Starting data pull request with $taskId, $dbName, $tableName, $fromDate, $toDate" || echo "ERROR: Missing arguments. Required taskId databaseName tableName fromDate toDate"

    [ ! -z "$taskId" ] && [ ! -z "$dbName" ]  && [ ! -z "$tableName" ] && [ ! -z "$fromDate" ] && [ ! -z "$toDate" ] || exit 1

    tableName=`echo $tableName | tr '[:upper:]' '[:lower:]'`
    dbName=`echo $dbName | tr '[:upper:]' '[:lower:]'`
    echo "lower db name = $dbName and table name = $tableName"

    TBL=`psql -U postgres ${dbName} -t -c "select table_name from information_schema.tables where table_catalog = '${dbName}' and table_name = '$tableName' " | sed -e 's/[[:space:]]*//' `

    if [ ! -z $TBL ]
    then
                
        if [ $tableName = 'energy_consumption' -o $tableName = 'energy_consumption_hourly' -o $tableName = 'plugload_energy_consumption' -o $tableName = 'plugload_energy_consumption_hourly' ]
        then
            fromDateInt=$(date -d "$fromDate" +"%Y%m%d")
            toDateInt=$(date -d "$toDate" +"%Y%m%d")
        else
            fromDateInt=$(date -d "$fromDate" +"%Y%m")
            toDateInt=$(date -d "$toDate" +"%Y%m")
        fi

        if [ $tableName = 'energy_consumption' ]
        then
            backup_directory="/home/enlighted/backups/$dbName/EC_CSV"
        elif [ $tableName = 'energy_consumption_hourly' ]
        then
            backup_directory="/home/enlighted/backups/$dbName/ECH_CSV"
        elif [ $tableName = 'energy_consumption_daily' ]
        then
            backup_directory="/home/enlighted/backups/$dbName/ECD_CSV"
        elif [ $tableName = 'plugload_energy_consumption' ]
        then
            backup_directory="/home/enlighted/backups/$dbName/PLUGLOAD_EC_CSV"
        elif [ $tableName = 'plugload_energy_consumption_hourly' ]
        then
            backup_directory="/home/enlighted/backups/$dbName/PLUGLOAD_ECH_CSV"
        else 
            backup_directory="/home/enlighted/backups/$dbName/PLUGLOAD_ECD_CSV"
        fi

        echo "from date int = $fromDateInt and to date int = $toDateInt and backup directory = $backup_directory"
        

        if [ -d $backup_directory ]
        then
            for f in $backup_directory/*.gz; do
                FDATE=`echo $f | rev | cut -d'/' -f1 | rev | cut -d'_' -f2 | cut -d'.' -f1`
                if [ $FDATE -ge $fromDateInt -a $FDATE -le $toDateInt ] 
                then
	                echo $f >> backupfiles
                fi
		    done
        else
            echo "backup directory $backup_directory does not exist."
        fi

        pg_dump -U postgres "$dbName" -v -s -t "$tableName" -f schema.sql
        
        dropdb -U postgres datapulldb
        createdb -U postgres datapulldb

        psql -U postgres datapulldb < schema.sql
        if [ $? -ne 0 ]
		then
			echo "ERROR restoring schema"
            output=1
            return
	    fi

        if [ -f backupfiles ]
        then
            while read p; do
                rm -f temp.gz temp
                cp $p temp.gz
                gunzip temp.gz
		        flines=`wc -l temp | cut -d' ' -f1`
		        if [ $flines -eq 1 ]
		        then
			        echo "Ignoring empty file $p "
		        else
			        echo "Restoring file $p "
			        header=$(head -n 1 temp)
			        sed -i '1d' temp
		            psql -U postgres -d datapulldb -c "\\copy $tableName (${header}) from temp CSV"
			        if [ $? -ne 0 ]
			        then
				        echo "ERROR restoring file $p "
                        output=1
                        return
			        fi
		        fi
            done < backupfiles
            rm -f temp.gz temp
        fi

        EC_END_DATE=`psql -U postgres datapulldb -t -c "select coalesce(max(capture_at), '2100-01-01 00:00:00') from $tableName" | sed -e 's/[[:space:]]*//' `
        EC_START_DATE=`psql -U postgres datapulldb -t -c "select coalesce(min(capture_at), '2000-01-01 00:00:00') from $tableName" | sed -e 's/[[:space:]]*//' `


        if [ -z "$EC_END_DATE" -o -z "$EC_START_DATE" ]
        then
            echo "ERROR readin data"
            output=1
            return
        else
            echo "min date = $EC_START_DATE and max date = $EC_END_DATE"
        fi  
      
        psql -U postgres $dbName -c "copy (select * from $tableName where (capture_at < '$EC_START_DATE' and capture_at >= '$fromDate') or (capture_at > '$EC_END_DATE' and capture_at <= '$toDate') order by capture_at asc) to STDOUT" | psql -U postgres datapulldb -c "\copy $tableName FROM STDIN"
        
        if [ $? -ne 0 ]
		then
			echo "ERROR restoring data"
            output=1
            return
	    fi

        psql -U postgres datapulldb -c "delete from $tableName where capture_at < '$fromDate' or capture_at > '$toDate'"

        if [ $? -ne 0 ]
		then
			echo "ERROR pruning data"
            output=1
            return
	    fi

        psql -U postgres datapulldb -c "copy (select * from $tableName order by capture_at asc) to STDOUT CSV HEADER" | gzip > $taskId.gz
        if [ $? -ne 0 ]
		then
			echo "ERROR creating final dump"
            output=1
            return
	    fi
    else
        echo "ERROR: container does not exist for $dbName and $tableName"
        output=1
    fi

}

dataPrep > request.log 2>&1
exit $output
