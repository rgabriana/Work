# == Class: syslog
#
# Full description of class syslog here.
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
#  class { 'syslog':
#    servers => [ 'pool.ntp.org', 'ntp.local.company.com' ],
#  }
#
# === Authors
#
# Author Name <author@domain.com>
#
# === Copyright
#
# Copyright 2016 Your name here, unless otherwise noted.
#
class enlighted-syslog {
package { 'rsyslog':
	  ensure => 'present',
}

service { 'rsyslog':
	  ensure => 'running',
	  enable => 'true',
}

File	{
	ensure => 'file',
	owner => 'root',
	group => 'root',
	mode => '644',
	replace => 'true',
	notify	=> Service['rsyslog'],
}

file	{
	'/etc/rsyslog.conf':
	source => [
	"puppet:///modules/enlighted-syslog/rsyslog.conf.$hostname",
	"puppet:///modules/enlighted-syslog/rsyslog.conf"
	];
	'/etc/rsyslog.d/00-bash.conf':
	source	=> [
	"puppet:///modules/enlighted-syslog/rsyslog.d/00-bash.conf"
	];

	'/etc/rsyslog.d/20-ufw.conf':
	source => [
	"puppet:///modules/enlighted-syslog/rsyslog.d/20-ufw.conf.$hostname",
	"puppet:///modules/enlighted-syslog/rsyslog.d/20-ufw.conf"
	];

	'/etc/rsyslog.d/50-default.conf':
	source => "puppet:///modules/enlighted-syslog/rsyslog.d/50-default.conf";
	}
}
