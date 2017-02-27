node default {

        include enlighted-logrotate
        include enlighted-pam
        include enlighted-ssh
	include enlighted-stigs
}

node enlighted-apps inherits default {
	include	enlighted-psql
	include enlighted-tomcat
#	include enlighted-puppet
}

node 'master.enlightedcloud.net' inherits enlighted-apps {
	include enlighted-cert_scripts
	include	master-apache2
	include	master-auditd
	include master-cron
	include master-ufw
	include master-oddities
	include master-denyhosts
}

node /^replica\d+\.enlightedcloud\.net$/ inherits enlighted-apps {
	include	replica-apache2
	include replica-auditd
	include replica-cron
	include replica-ufw
}

node 'puppet' inherits default {
	include puppet-ufw
	#include enlighted-puppet_master
}
