In case of RMA of an EM which is from version below 3.6 a new script to restore the EM from
backup on cloud ( replica server ) is provided in this folder, it is called new_em_restore.sh
In order to restore such an EM to its previous state you will have to run this script with the 
backup.tar.gz file as a parameter, first copy the new_em_restore.sh and the daily_dump backup file
to the EM, change the permission on the new_em_restore.sh file to execute and then run the script. 

e.g.
scp new_em_restore.sh enlighted@IP.OF.EM
scp daily_dump_3.6.0.7420_03-08-2016_06-33-56.tar.gz enlighted@IP.OF.EM

ssh enlighted@IP.OF.EM
chmod +x ./new_em_restore.sh
sudo ./new_em_restore.sh daily_dump_3.6.0.7420_03-08-2016_06-33-56.tar.gz

This script will extract the dump file and retore the EM back to its original state from backup.
The logs are in the folder /var/lib/tomcat6/Enlighted/adminlogs/ , the log files are
upgradegems.log, upgradegems_error.log respectively.

This new script is needed because with the 3.6 release a new format with a mechanism for backup where the six 
energy consumption tables are backed up as CSV files is used.