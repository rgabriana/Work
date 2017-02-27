#!/bin/bash

if [ $# -ne 3 ]
then
  echo "Usage: cu_status.sh start_time(yyyy-mm-dd) end_time(yyyy-mm-dd) database_name"
  exit
fi

start_time=$1
end_time=$2
db_name=$3
file="cu_status.txt"

echo "Resistor problem" > $file
echo "" >> $file

query="(select fixture_id as \"Fixture Id\", count(*) as \"No Of Rows\", min(capture_at) as \"First Time\", max(capture_at) as \"Last Time\" from energy_consumption where capture_at > '$start_time' and capture_at <= '$end_time' and power_calc = 1 and zero_bucket = 0 and cu_status = 4096 group by fixture_id order by \"No Of Rows\" desc) except (select ec1.fixture_id, ec1.no_of_rows, ec1.first_time, ec1.last_time from (select fixture_id, count(*) as no_of_rows, min(capture_at) as first_time, max(capture_at) as last_time from energy_consumption where capture_at > '$start_time' and capture_at <= '$end_time' and power_calc = 1 and zero_bucket = 0 and cu_status = 4096 group by fixture_id) as ec1, (select fixture_id, max(capture_at) as last_time from energy_consumption where capture_at > '$start_time' and capture_at <= '$end_time' and zero_bucket = 0 and power_Calc = 2 group by fixture_id ) ec where ec1.fixture_id = ec.fixture_id and ec.last_time > ec1.last_time order by ec1.fixture_id)"

#query="select fixture_id as \"Fixture Id\", fixture_name as \"Fixrure Name\", count(*) as \"No Of Rows\", min(capture_at) as \"First Time\", max(capture_at) as \"Last Time\" from energy_consumption as ec, fixture as f where capture_at > '$start_time' and capture_at <= '$end_time' and f.id = ec.fixture_id and zero_bucket = 0 and power_calc = 1 and cu_status = 4096 and fixture_id not in (select distinct fixture_id from energy_consumption where capture_at > '$start_time' and capture_at <= '$end_time' and zero_bucket = 0 and power_calc = 2) group by fixture_id, fixture_name order by \"No Of Rows\" desc"

psql -F, -A -Upostgres $db_name -o "cu_status.csv" -c "$query"

cat cu_status.csv >> $file
echo "" >> $file

echo "Mind wipe propblem or received serial timeout possibly bad cable" >> $file
echo "" >> $file

query="(select fixture_id as \"Fixture Id\", count(*) as \"No Of Rows\", min(capture_at) as \"First Time\", max(capture_at) as \"Last Time\" from energy_consumption where capture_at > '$start_time' and capture_at <= '$end_time' and power_calc = 1 and zero_bucket = 0 and cu_status = 8192 group by fixture_id order by \"No Of Rows\" desc) except (select ec1.fixture_id, ec1.no_of_rows, ec1.first_time, ec1.last_time from (select fixture_id, count(*) as no_of_rows, min(capture_at) as first_time, max(capture_at) as last_time from energy_consumption where capture_at > '$start_time' and capture_at <= '$end_time' and power_calc = 1 and zero_bucket = 0 and cu_status = 8192 group by fixture_id) as ec1, (select fixture_id, max(capture_at) as last_time from energy_consumption where capture_at > '$start_time' and capture_at <= '$end_time' and zero_bucket = 0 and power_Calc = 2 group by fixture_id ) ec where ec1.fixture_id = ec.fixture_id and ec.last_time > ec1.last_time order by ec1.fixture_id)"

#query="select fixture_id as \"Fixture Id\", fixture_name as \"Fixrure Name\", count(*) as \"No Of Rows\", min(capture_at) as \"First Time\", max(capture_at) as \"Last Time\" from energy_consumption as ec, fixture as f where capture_at > '$start_time' and capture_at <= '$end_time' and f.id = ec.fixture_id and zero_bucket = 0 and power_calc = 1 and cu_status = 8192 and fixture_id not in (select distinct fixture_id from energy_consumption where capture_at > '$start_time' and capture_at <= '$end_time' and zero_bucket = 0 and power_calc = 2) group by fixture_id, fixture_name order by \"No Of Rows\" desc"

psql -F, -A -Upostgres $db_name -o "cu_status.csv" -c "$query"

cat cu_status.csv >> $file
