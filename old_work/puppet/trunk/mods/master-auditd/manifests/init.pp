class master-auditd {
	package { 'auditd':
		ensure	=> 'present',
	}

	service { 'auditd':
		ensure	=> 'running',
		enable	=> 'true',
		subscribe => File['/etc/audit/audit.rules','/etc/audisp/plugins.d/syslog.conf','/etc/audit/auditd.conf'],
		require	=> Package['auditd'],
	}

	File {
		ensure  => 'file',
		group   => 'root',
		mode    => '640',
		owner   => 'root',
		replace => 'true',
		notify	=> Service['auditd'],
	}

	file {
	'/etc/audit/audit.rules':
	source  => "puppet:///modules/master-auditd/audit.rules";
	
	'/etc/audit/auditd.conf':
	source  => "puppet:///modules/master-auditd/auditd.conf";
	
	'/etc/audisp/plugins.d/syslog.conf': # STIG V-38471
	source	=> "puppet:///modules/master-auditd/syslog.conf";
	}
}
