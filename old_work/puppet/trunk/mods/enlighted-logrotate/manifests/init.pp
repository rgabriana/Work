class enlighted-logrotate {

	package { 'logrotate':
		ensure => 'installed',
		before => File['/etc/logrotate.conf'],
	}

	exec {	'logrotate':
		command	=> 'logrotate -f /etc/logrotate.conf',
		path	=> '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
		subscribe	=> File['/etc/logrotate.conf', '/etc/logrotate.d/apache2', '/etc/logrotate.d/apport', '/etc/logrotate.d/apt', '/etc/logrotate.d/aptitude', '/etc/logrotate.d/checksecurity', '/etc/logrotate.d/dpkg', '/etc/logrotate.d/postgresql-common', '/etc/logrotate.d/puppet', '/etc/logrotate.d/rsyslog', '/etc/logrotate.d/tomcat6', '/etc/logrotate.d/ufw', '/etc/logrotate.d/unattended-upgrades', '/etc/logrotate.d/upstart' ],
		refreshonly	=> 'true',
	}

	File {
		ensure	=> 'file',
		group	=> 'root',
		owner	=> 'root',
		mode 	=> '644',
	}

	file {	'/etc/logrotate.d':
			ensure	=> 'directory',
			mode	=> '755';

		'/etc/logrotate.conf':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.conf";

		'/etc/logrotate.d/apache2':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/apache2";

		'/etc/logrotate.d/apport':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/apport";

		'/etc/logrotate.d/apt':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/apt";

		'/etc/logrotate.d/aptitude':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/aptitude";

		'/etc/logrotate.d/checksecurity':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/checksecurity";

	 	'/etc/logrotate.d/dpkg':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/dpkg";

		'/etc/logrotate.d/postgresql-common':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/postgresql-common";

		'/etc/logrotate.d/puppet':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/puppet";

		'/etc/logrotate.d/rsyslog':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/rsyslog";

		'/etc/logrotate.d/tomcat6':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/tomcat6";

		'/etc/logrotate.d/ufw':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/ufw";

		'/etc/logrotate.d/unattended-upgrades':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/unattended-upgrades";

		'/etc/logrotate.d/upstart':
			source  => "puppet:///modules/enlighted-logrotate/logrotate.d/upstart";
	}
}
