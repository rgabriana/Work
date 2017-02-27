class enlighted-ssh {# puppet module for the SSH software, this manages the configurations and version of the software.  

	package { 'openssh-server':
		  ensure => 'installed', # this ensures the SSH package is installed 
		  before => File['/etc/ssh/sshd_config'], # this establishes the relationship between teh software package and its config files
	}
	file { '/etc/ssh/sshd_config':
		 notify	=> Service['ssh'],
		 ensure  => 'file',
		 group   => 'root',
		 mode    => '640',
		 owner   => 'root',
		 replace => 'true',
		 source  => "puppet:///modules/enlighted-ssh/sshd_config", # Source location of the reference sshd file. 
	}
	service { 'ssh': # Service control for SSH
		  ensure => 'running',
		  enable => 'true',
		  subscribe => File['/etc/ssh/sshd_config'], # This flags the service to restart when a configuration change was detected. 
	}
}
