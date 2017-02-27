# Sccript is usefull to drop 50 copies sample databases with different name.
pgDbName=150
while [ $pgDbName -le 201 ]
do
        echo "drop database em_100_$pgDbName"
        dropdb -U postgres "em_100_$pgDbName"
        pgDbName=$(($pgDbName + 1))
done

echo "All Databases droped..."



