# == Class: tomcat
#
# Full description of class tomcat here.
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
#  class { 'tomcat':
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
class enlighted-tomcat {
	service { 'tomcat6':
		ensure 	=> 'running',
		enable	=> 'true',
	}

	File {
		ensure  => 'file',
		group   => 'tomcat6',
		mode    => '644',
		owner   => 'tomcat6',
		replace	=> 'true',
	}

	file { '/var/lib/tomcat6/conf/context.xml':
		owner	=> 'root',
		source	=> "puppet:///modules/enlighted-tomcat/context.xml.$hostname";

		'/var/lib/tomcat6/Enlighted/':
		ensure 	=> 'directory';

		'/var/lib/tomcat6/Enlighted/connection_config.properties':
		source	=> "puppet:///modules/enlighted-tomcat/connection_config.properties";

		'/var/lib/tomcat6/Enlighted/config.properties':
		source	=> "puppet:///modules/enlighted-tomcat/config.properties";

		'/usr/share/tomcat6/bin/catalina.sh':
		group   => 'root',
		mode    => '755',
		owner   => 'root',
		source	=> "puppet:///modules/enlighted-tomcat/catalina.sh";

		'/var/lib/tomcat6/webapps/ROOT/index.html':
		source	=> "puppet:///modules/enlighted-tomcat/index.html.$hostname";

		'/etc/default/tomcat6':
		group   => 'root',
		owner   => 'root',
		source	=> "puppet:///modules/enlighted-tomcat/tomcat6";
		
		'/var/lib/tomcat6/Enlighted/ems_log4j':
		ensure	=> 'directory';
		
		'/var/lib/tomcat6/Enlighted/ems_log4j/log4j.properties':
		source	=> "puppet:///modules/enlighted-tomcat/log4j.properties";
	}
}
