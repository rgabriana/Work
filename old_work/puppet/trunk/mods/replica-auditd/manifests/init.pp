class replica-auditd {
	package { 'auditd':
		ensure	=> 'present',
	}
	service { 'auditd':
		ensure	=> 'running',
		enable	=> 'true',
		subscribe => File['/etc/audit/audit.rules','/etc/audit/auditd.conf','/etc/audisp/plugins.d/syslog.conf'],
		require	=> Package['auditd'],
	}
	File {
		ensure  => 'file',
		notify	=> Service['auditd'],
		group   => 'root',
		mode    => '640',
		owner   => 'root',
		replace => 'true',
	}
	file { 
	'/etc/audit/audit.rules':
	source  => "puppet:///modules/replica-auditd/audit.rules";
	
	'/etc/audit/auditd.conf':
	source	=> "puppet:///modules/replica-auditd/auditd.conf";
	
	'/etc/audisp/plugins.d/syslog.conf': # STIG V-38471
	source	=> "puppet:///modules/replica-auditd/syslog.conf";
	}
}
