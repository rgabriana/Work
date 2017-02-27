netstat -a | grep postgres | grep PGSQL | sed 's/^.*PGSQL\.//'
