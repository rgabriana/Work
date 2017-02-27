source /etc/environment
logger "php insertGateway.php --ip=$1 --mac=$2 --mask=$3"
/usr/bin/php $ENLIGHTED_HOME/insertGateway.php --ip=$1 --mac=$2 --mask=$3
