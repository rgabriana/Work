class enlighted-logrotate {

	package { 'logrotate':
		ensure => 'installed',
		before => File['/etc/logrotate.conf'],
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
	}
}
