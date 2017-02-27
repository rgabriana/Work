#!/usr/bin/perl

use strict;
use warnings;
use lib qw/lib/;
use CaptureTime;
use DB::Emscloud;
use DB::EmReplica;

my $c = DB::Emscloud->new();
my $sites = $c->getSiteList($ARGV[0]);

my $siteEmHist = {
    1 => 0,
    2 => 0,
    3 => 0,
    4 => 0,
    5 => 0,
    6 => 0,
    7 => 0,
    8 => 0,
    9 => 0,
    10 => 0,
    more => 0
};

my $emGWHist = {
    1 => 0,
    3 => 0,
    5 => 0,
    7 => 0,
    9 => 0,
    more => 0
};

my $emSUHist = {
    100 => 0,
    200 => 0,
    300 => 0,
    400 => 0,
    500 => 0,
    600 => 0,
    700 => 0,
    800 => 0,
    900 => 0,
    1000 => 0,
    1100 => 0,
    1200 => 0,
    more => 0,
};

sub histInsert($$) {
    my $hist = shift;
    my $val = shift;

    for my $key (sort { return 1 if $a eq "more"; return -1 if $b eq "more"; return $a <=> $b } keys %$hist) {
        next if $key eq "more";

        if($val <= $key) {
            $hist->{$key}++;
            return;
        }
    }
    $hist->{more}++;
}

sub histPrint($) {
    my $hist = shift;
    for my $key (sort { return 1 if $a eq "more"; return -1 if $b eq "more"; return $a <=> $b } keys %$hist) {
        print "$key, $hist->{$key}\n";
    }
}

my $aveEmPerSite = 0;
my $aveGwPerEM = 0;
my $aveSUPerEM = 0;
my $aveSUPerGW = 0;

my $numEms = 0;
my $numGws = 0;
for my $site (@$sites) {
    my $rd = CaptureTime->new($site->{releaseDate});
    next if($rd->year() == 2015 && $rd->month() > 10);
    my $ems = $c->getEMsForSite($site->{id});
    next unless defined $ems;

    my $emCount = $#{$ems} + 1;
    histInsert($siteEmHist, $emCount);
    $aveEmPerSite += $emCount;
    $numEms += $emCount;

    for my $em (@$ems) {
        my $r;
        eval {
            $r = DB::EmReplica->new($em->{replicaServerIP}, $em->{dbName});
        };
        next if $@;
        my $sensorCount = $r->getSensorCount();
        my $gws = $r->getGatewayInfo();
        my $gwCount = $#{$gws} + 1;
        $numGws += $gwCount;

        histInsert($emGWHist, $gwCount);
        histInsert($emSUHist, $sensorCount);
        $aveGwPerEM += $gwCount;
        $aveSUPerEM += $sensorCount;
        $aveSUPerGW += $sensorCount;
    }
}

print "Num Sites, " . ($#{$sites} + 1) . "\n";
print "Num EMs, $numEms\n";
print "Num GWs, $numGws\n";
print "Num SUs, $aveSUPerEM\n"; # The ave variable contains the total at this point (we divide below)

$aveEmPerSite = $aveEmPerSite/($#{$sites} + 1);
$aveGwPerEM = $aveGwPerEM/$numEms;
$aveSUPerEM = $aveSUPerEM/$numEms;
$aveSUPerGW = $aveSUPerGW/$numGws;

print "Ave EM Per Site, $aveEmPerSite\n";
print "Ave GW Per EM, $aveGwPerEM\n";
print "Ave SU Per EM, $aveSUPerEM\n";
print "Ave SU Per GW, $aveSUPerGW\n";

print "EM Per Site\n";
histPrint($siteEmHist);
print "GW Per EM\n";
histPrint($emGWHist);
print "SU Per EM\n";
histPrint($emSUHist);
