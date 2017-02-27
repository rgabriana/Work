#!/usr/bin/perl
$| = 1;
while (<STDIN>) {
	$input = "$_";
	$emsmode = `head -n 1 /var/lib/tomcat6/Enlighted/emsmode`;
	chomp($emsmode);
        if ($emsmode !~ m/NORMAL/) {
		$input=~s/.*$/REDIRECTURL/;
		print $input;
	}
	else {
		print $input;
	}
}
