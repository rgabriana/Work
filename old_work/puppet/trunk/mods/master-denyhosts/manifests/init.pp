# == Class: denyhosts
#
# Full description of class denyhosts here.
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
#  class { 'denyhosts':
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
class master-denyhosts {
	service {'daemon-control':
	ensure	=> 'running',
	enable	=> 'true',
	}
	File {
		owner	=> 'root',
		group	=> 'root',
		mode 	=> '700',
		replace	=> 'true',
		ensure 	=> 'file',
	}
	file { 
	'/etc/denyhosts.conf':
	notify	=> Service['daemon-control'],
	source	=> "puppet:///modules/master-denyhosts/denyhosts.conf",
	mode	=> '600';
	
	'/etc/rc.local':
	source	=> "puppet:///modules/master-denyhosts/rc.local";

	'/etc/init.d/daemon-control':
	source	=> "puppet:///modules/master-denyhosts/daemon-control";
	
	'/usr/sbin/denyhosts':
	notify	=> Service['daemon-control'],
	source	=> "puppet:///modules/master-denyhosts/denyhosts";
	}
}
