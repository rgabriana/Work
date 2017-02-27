# == Class: cert_scripts
#
# Full description of class cert_scripts here.
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
#  class { 'cert_scripts':
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
class enlighted-cert_scripts {
	File {
		ensure	=> 'file',
		owner	=> 'enlighted',
		group	=> 'enlighted',
		mode	=> '755',
	}
	file { 	'/etc/enlighted/CA/scripts':
		ensure	=> 'directory';

		'/etc/enlighted/CA/scripts/generateClientcert.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/generateClientcert.sh";

		'/etc/enlighted/CA/scripts/makeCA.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/makeCA.sh";

		'/etc/enlighted/CA/scripts/getCertDates.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/getCertDates.sh";

		'/etc/enlighted/CA/scripts/testClient.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/testClient.sh";

		'/etc/enlighted/CA/scripts/generateRScert.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/generateRScert.sh";

		'/etc/enlighted/CA/scripts/setup.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/setup.sh"; 

		'/etc/enlighted/CA/scripts/generateCRL.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/generateCRL.sh";

		'/etc/enlighted/CA/scripts/generateServercert.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/generateServercert.sh";

		'/etc/enlighted/CA/scripts/revokeCert.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/revokeCert.sh";

		'/etc/enlighted/CA/scripts/generateCAcert.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/generateCAcert.sh";

		'/etc/enlighted/CA/scripts/regenerateRScert.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/regenerateRScert.sh";

		'/etc/enlighted/CA/scripts/regenerateServercert.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/regenerateServercert.sh";

		'/etc/enlighted/CA/scripts/verifyCert.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/verifyCert.sh";

		'/etc/enlighted/CA/scripts/revokeClientCert.sh':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/revokeClientCert.sh";

		'/etc/enlighted/CA/scripts/openssl.my.cnf':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/openssl.my.cnf",
		mode	=> '644';

		'/etc/enlighted/CA/ssl/openssl.my.cnf':
		source	=> "puppet:///modules/enlighted-cert_scripts/scripts/openssl.my.cnf",
		mode	=> '644';

		'/etc/enlighted/CA/ssl/pfx/enlighted.ts':
		source	=> "puppet:///modules/enlighted-cert_scripts/ssl/pfx/enlighted.ts",
		mode	=> '644';
	}
}
