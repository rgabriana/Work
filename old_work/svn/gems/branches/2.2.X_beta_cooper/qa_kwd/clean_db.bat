dropdb -U postgres ems
createdb -U postgres ems
psql -U postgres ems < ..\install\debian-install\home\enlighted\InstallSQL.sql
