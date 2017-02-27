# == Class: stigs
#
# Full description of class stigs here.
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
#  class { 'stigs':
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
class enlighted-stigs {
	Package	{	
		ensure => 'purged',
	}
	package {
# STIG V-38587, V-38589, 
		'telnetd':; 
		'tftp':;
# STIG V-38591
		'rsh-server':; 
# STIG V-38598
		'rexecd':; 
# STIG V-38602
		'rlogind':; 
		'qpidd':;
		'screen':;
#		ensure => 'installed';
	}

	exec { 'update-grub':
		command	=> 'update-grub',
		path	=> '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
		subscribe => File['/etc/default/grub'],
		refreshonly => 'true',
	}

	File {
		ensure	=> 'file',
		group	=> 'root',
		owner	=> 'root',
		mode	=> '600',
	}

	file	{
	
	'/home/rgabriana':
		ensure	=> 'directory',
		owner	=> 'rgabriana',
		group	=> 'rgabriana',
		mode	=> '750';
	
	'/etc/ipblocks.txt':
		source	=> "puppet:///modules/enlighted-stigs/ipblocks.txt";

#STIG V-38668
	'/etc/init/control-alt-delete.conf': 
		source	=> "puppet:///modules/enlighted-stigs/control-alt-delete.conf";

#STIG V-38653
 	'/etc/snmp/snmpd.conf': 
		ensure => 'absent';

	'/etc/default/grub': 
#STIG V-38438
		notify 	=> Exec["update-grub"],
		audit	=> 'content',
		mode	=> '644',
		source	=> "puppet:///modules/enlighted-stigs/default.grub";

# STIG V-38491
	'/etc/hosts.equiv': 
		ensure => 'absent';

	'/etc/login.defs':
		mode	=> '644',
		source	=> "puppet:///modules/enlighted-stigs/login.defs";
	

	'/etc/sysctl.conf':
		source	=> "puppet:///modules/enlighted-stigs/sysctl.conf";

	'/etc/security/limits.conf':
		source	=> "puppet:///modules/enlighted-stigs/limits.conf";

	'/etc/default/useradd':
		source	=> "puppet:///modules/enlighted-stigs/default.useradd",
		mode	=> '644';

	'/etc/securetty':
		source	=> "puppet:///modules/enlighted-stigs/securetty";

# STIG V-38502
	'/etc/shadow': 
		mode 	=> '644';

	'/etc/gshadow':
		mode    => '644';

	'/etc/passwd':
		mode	=> '644';

	'/etc/group':
		mode	=> '644';
	
	'/etc/modprobe.d/blacklist.conf':
		source	=> "puppet:///modules/enlighted-stigs/blacklist.conf";

	'/etc/modprobe.d/iwlwifi.conf':
		source	=> "puppet:///modules/enlighted-stigs/iwlwifi.conf";

	'/etc/modprobe.d/blacklist-firewire.conf':
		source	=> "puppet:///modules/enlighted-stigs/blacklist-firewire.conf";

	'/etc/modprobe.d/mlx4.conf':
		source	=> "puppet:///modules/enlighted-stigs/mlx4.conf";

	'/etc/modprobe.d/fbdev-blacklist.conf':
		source	=> "puppet:///modules/enlighted-stigs/fbdev-blacklist.conf";

	'/etc/modprobe.d/blacklist-rare-network.conf':
		source	=> "puppet:///modules/enlighted-stigs/blacklist-rare-network.conf";

	'/etc/modprobe.d/blacklist-ath_pci.conf':
		source	=> "puppet:///modules/enlighted-stigs/blacklist-ath_pci.conf";

	'/etc/modprobe.d/blacklist-watchdog.conf':
		source => "puppet:///modules/enlighted-stigs/blacklist-watchdog.conf";

	'/etc/modprobe.d/blacklist-framebuffer.conf':
		source	=> "puppet:///modules/enlighted-stigs/blacklist-framebuffer.conf";

# STIG V-38580, V-38581
	'/boot/grub/grub.cfg': 
		mode	=> '444';

	'/etc/sudoers':
		mode    => '440',
		source	=> "puppet:///modules/enlighted-stigs/sudoers";

	'/etc/hosts.allow':
		source	=> "puppet:///modules/enlighted-stigs/hosts.allow";

	'/etc/hosts':
		mode	=> '644';
	
	'/etc/hosts.deny':
		mode	=> '644';

	'/etc/bash.bashrc':
		mode	=> '644',
		source	=> "puppet:///modules/enlighted-stigs/bash.bashrc";
	}
	
	Host {
		ensure	=> 'absent',
#		name	=> Pattern[/\A[a-z].*/],
	}	

	host { 
	
	'localhost':
	ensure	=> 'present',
	ip => '127.0.0.1',
	host_aliases	=> 'localhost';
	
	'test-cloudui.enlightedinc.info':
	ensure		=> 'present',
	ip 		=> '10.4.0.203',
	host_aliases	=> 'test-cloudui';

	'replica-test.enlightedinc.info':
	ensure		=> 'present',
	ip		=> '10.4.0.202',
	host_aliases	=> 'replica-test';

	'pmserver.enlightedinc.info':
	ensure		=> 'present',
	ip		=> '10.4.0.201',
	host_aliases	=> 'pmserver';

	'herodotus.enlightedinc.info':
	ensure		=> 'present',
	ip		=> '10.4.0.200',
	host_aliases	=> 'herodotus';
	}

	user { 'rgabriana':
	ensure           => 'present',
	comment          => 'Rolando R. Gabriana,,,',
	groups           => ['sgt'],
	home             => '/home/rgabriana',
	password         => '$6$vcm.FH/n$M1n8UULKc8BCEbwnP2XMnhkB.LKehqnQDvbb2xdcLaKdhgJxRUYhjIcddg0acDaEba0LrV./PqEJbjjusSyT1/',
	password_max_age => '99999',
	password_min_age => '0',
	shell            => '/bin/bash',
	}

	group { 'sgt':
	ensure	=> 'present',
	}
}
