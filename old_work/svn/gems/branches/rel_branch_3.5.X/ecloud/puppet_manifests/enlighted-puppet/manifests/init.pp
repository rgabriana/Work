class enlighted-puppet {

	file { '/etc/puppet/puppet.conf':
		ensure  => 'file',
		group   => '0',
		mode    => '640',
		owner   => '0',
		replace => 'true',
		source  => "puppet:///modules/enlighted-puppet/puppet.conf",
	}	

}
