# == Class: pam
#
# Full description of class pam here.
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
# Here you should define a list of variables that this modules would require.
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
#  class { 'pam':
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
class enlighted-pam {

	File {
		ensure	=> 'file',
		owner	=> 'root',
		group	=> 'root',
		mode	=> '640',
	}

	file {	'/etc/pam.d/accountsservice':
		source	=> "puppet:///modules/enlighted-pam/accountsservice";

		'/etc/pam.d/chfn':
		source	=> "puppet:///modules/enlighted-pam/chfn";

		'/etc/pam.d/chpasswd':
		source	=> "puppet:///modules/enlighted-pam/chpasswd";

		'/etc/pam.d/chsh':
		source	=> "puppet:///modules/enlighted-pam/chsh";

		'/etc/pam.d/common-account':
		source	=> "puppet:///modules/enlighted-pam/common-account";

		'/etc/pam.d/common-auth':
		source => "puppet:///modules/enlighted-pam/common-auth";

		'/etc/pam.d/common-password':
		source => "puppet:///modules/enlighted-pam/common-password";

		'/etc/pam.d/common-session':
		source => "puppet:///modules/enlighted-pam/common-session";

		'/etc/pam.d/common-session-noninteractive':
		source => "puppet:///modules/enlighted-pam/common-session-noninteractive";

		'/etc/pam.d/cron':
		source => "puppet:///modules/enlighted-pam/cron";

		'/etc/pam.d/login':
		source => "puppet:///modules/enlighted-pam/login";

		'/etc/pam.d/newusers':
		source => "puppet:///modules/enlighted-pam/newusers";

		'/etc/pam.d/other':
		source => "puppet:///modules/enlighted-pam/other";

		'/etc/pam.d/passwd':
		source => "puppet:///modules/enlighted-pam/passwd";

		'/etc/pam.d/ppp':
		source => "puppet:///modules/enlighted-pam/ppp";

		'/etc/pam.d/sshd':
		source => "puppet:///modules/enlighted-pam/sshd";

		'/etc/pam.d/sudo':
		source => "puppet:///modules/enlighted-pam/sudo";

		'/etc/pam.d/su':
		source => "puppet:///modules/enlighted-pam/su";
	}

	Package {
		ensure => 'purged',
	}

	package { 'screen':; # STIG V-38590
 		'xinetd':; # STIG V-38584
	}
}
