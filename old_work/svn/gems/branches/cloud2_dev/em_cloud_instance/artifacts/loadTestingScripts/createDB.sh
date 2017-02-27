# Sccript is usefull to create 50 copies sample databases with different name.
pgDbName=150
while [ $pgDbName -le 201 ]
do
	echo "creating database em_100_$pgDbName"
	createdb -U postgres -O postgres -T em_100_100 "em_100_$pgDbName"
	pgDbName=$(($pgDbName + 1))
done

echo "All Databases created..."

