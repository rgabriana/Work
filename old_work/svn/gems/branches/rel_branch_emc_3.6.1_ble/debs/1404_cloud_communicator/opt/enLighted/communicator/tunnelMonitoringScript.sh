if [ "`ps -eaf | grep ssh | grep localhost | grep $1 | grep $2 |  grep -v grep`" ] ; 
then
 echo "SSH tunnel is up"
else
 echo "SSH tunnel is down"
fi
