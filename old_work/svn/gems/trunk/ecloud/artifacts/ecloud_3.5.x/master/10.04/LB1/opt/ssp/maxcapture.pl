#!/usr/bin/perl

use strict;
use warnings;
use lib qw/lib/;
use DB::Emscloud;
use DB::EmReplica;

my $customer = $ARGV[0];
if(!defined $customer) {
    die "No customer ID defined";
}

my $c = DB::Emscloud->new();
my $sites = $c->getSiteList($customer);

for my $site (@$sites) {
    print "$site->{geoLoc} $site->{name}\n";
    my $siteID = $site->{id};
    my $ems = $c->getEMsForSite($siteID);
    next unless defined $ems;
    
    for my $em (@$ems) {
        print "$em->{emName} ";
        eval {
            my $r = DB::EmReplica->new($em->{replicaServerIP}, $em->{dbName});
            print $r->query("SELECT MAX(capture_at) FROM energy_consumption")->[0]->[0] . "\n";
        };
        if($@) {
            print "NO DATA\n";
        }
    }
}
