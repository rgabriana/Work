class replica-apache2 {

	package { 'apache2':
		ensure	=> 'present',
	}

	Exec {	
		path	=> '/sbin:/usr/sbin:/bin:/usr/bin',
	}

	$apache_server = $operatingsystem ? {
		default	=> 'apache2',
		Ubuntu	=> 'apache2',
	}

	exec { 
	"enable_apache2_ssl":
	command => "/usr/sbin/a2enmod ssl",
	creates => '/etc/apache2/mods-enabled/ssl.load';

	"enable_apache2_proxy":
        command => "/usr/sbin/a2enmod proxy",
	creates => '/etc/apache2/mods-enabled/proxy.load';

	"enable_apache2_proxy_http":
        command => "/usr/sbin/a2enmod proxy_http",
	creates => '/etc/apache2/mods-enabled/proxy_http.load';

	"enable_apache2_proxy_ajp":
        command => "/usr/sbin/a2enmod proxy_ajp",
	creates => '/etc/apache2/mods-enabled/proxy_ajp.load';

	"enable_apache2_rewrite":
	command => "/usr/sbin/a2enmod rewrite",
        creates => '/etc/apache2/mods-enabled/rewrite.load';
	}

	service { 'apache2':
		ensure 	=> 'running',
		enable 	=> 'true',
		require	=> Package['apache2'],
	}

	File {
		ensure  => 'file',
		group   => 'root',
		mode    => '644',
		owner   => 'root',
		replace	=> 'true',
	}

	file { 

	'/etc/apache2/mods-available/ssl.load':
	notify 	=> Service['apache2'],
	source	=> "puppet:///modules/replica-apache2/ssl.load";

	'/etc/apache2/mods-available/proxy.load':
	notify 	=> Service['apache2'],
	source	=> "puppet:///modules/replica-apache2/proxy.load";

	'/etc/apache2/mods-available/proxy_http.load':
	notify 	=> Service['apache2'],
	source	=> "puppet:///modules/replica-apache2/proxy_http.load";

	'/etc/apache2/mods-available/proxy_ajp.load':
	notify 	=> Service['apache2'],
	source	=> "puppet:///modules/replica-apache2/proxy_ajp.load";

	'/etc/apache2/mods-available/rewrite.load':
	notify 	=> Service['apache2'],
	source	=> "puppet:///modules/replica-apache2/rewrite.load";

	'/etc/apache2/sites-available/000-default-replica.conf':
	notify	=> Service['apache2'],
	group   => 'tomcat6',
	owner   => 'tomcat6',
	source	=> [
	"puppet:///modules/replica-apache2/000-default-replica.$hostname"
	];

	'/etc/apache2/sites-enabled/000-default-replica.conf':
	ensure => 'link',
	mode   => '777',
	target => '/etc/apache2/sites-available/000-default-replica.conf';
	}
}
