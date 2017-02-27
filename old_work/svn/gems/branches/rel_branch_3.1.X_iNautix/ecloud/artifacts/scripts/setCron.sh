cronExists=$(grep 'pruneTables\.sh' /var/spool/cron/crontabs/root)
if [[ "$cronExists" =~ "pruneTables" ]]
then
    echo "Setting pruneing Table script"
    sudo sed -i '/pruneTables/d' /var/spool/cron/crontabs/root
    echo "55 23 * * * /var/lib/tomcat6/Enlighted/scripts/pruneTables.sh" >> /var/spool/cron/crontabs/root
    echo "pruneing Table script to run every once a day"
else
    echo "55 23 * * * /var/lib/tomcat6/Enlighted/scripts/pruneTables.sh" >> /var/spool/cron/crontabs/root
    echo "pruneing Table script to run every once a day"
fi




sudo service cron restart 
