Migration Steps for dev users from DB based admin authentication to file based authentication. DO NOT CHECK IN adminpasswd file.

1) Create adminpasswd file under your working directory at workspace_location/gems/ems/src/Enlighted/
2) Get admin password value from database using query 'select password from users where email = 'admin' limit 1'
3) Put only the password value without any space or new line at the first line of adminpasswd file and save the file.
4) You should be able to log in again.

Due to this migration, changed password for admin user will not work while you are running tomcat using mvn.
