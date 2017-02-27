class enlighted-puppet {
	Package {
		ensure => 'present',
	}

	package { 

		'puppet':;
		'puppet-common':;
		'puppetlabs-release':;
		
	}

	service {'puppet':
		enable	=> 'true',
		ensure	=> 'running',
		subscribe	=> File['/etc/puppet/puppet.conf'],

	}
	
	file { '/etc/puppet/puppet.conf':
		notify	=> Service['puppet'],
		ensure  => 'file',
		group   => 'root',
		mode    => '640',
		owner   => 'root',
		replace => 'true',
		source  => "puppet:///modules/enlighted-puppet/puppet.conf",
	
	}	
}
