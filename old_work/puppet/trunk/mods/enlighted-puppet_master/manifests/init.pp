# == Class: puppet_master
#
# Full description of class puppet_master here.
#
# === Parameters
#
# Document parameters here.
#
# [*sample_parameter*]
#   Explanation of what this parameter affects and what it defaults to.
#   e.g. "Specify one or more upstream ntp servers as an array."
#
# === Variables
#
# Here you should define a list of variables that this module would require.
#
# [*sample_variable*]
#   Explanation of how this variable affects the funtion of this class and if
#   it has a default. e.g. "The parameter enc_ntp_servers must be set by the
#   External Node Classifier as a comma separated list of hostnames." (Note,
#   global variables should be avoided in favor of class parameters as
#   of Puppet 2.6.)
#
# === Examples
#
#  class { 'puppet_master':
#    servers => [ 'pool.ntp.org', 'ntp.local.company.com' ],
#  }
#
# === Authors
#
# Author Name <author@domain.com>
#
# === Copyright
#
# Copyright 2015 Your name here, unless otherwise noted.
#
class enlighted-puppet_master {

	Package {
		ensure => 'present',
	}

	package { 

		'facter':;
		'hiera':;
		'puppet':;
		'puppet-common':;
		'puppet-lint':;
		'puppetlabs-release':;
		'puppetmaster-common':;
		'puppetmaster-passenger':;
		'vim-puppet':;
		
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
		source  => "puppet:///modules/enlighted-puppet_master/puppet.conf",
	}	
	
	file { '/var/lib/puppet/trusted.conf':
		ensure	=> 'file',
		group	=> 'root',
		owner	=> 'root',
		mode	=> '644',
		replace	=> 'true',
		source	=> "puppet:///modules/enlighted-puppet_master/trusted.conf",
	}
}
