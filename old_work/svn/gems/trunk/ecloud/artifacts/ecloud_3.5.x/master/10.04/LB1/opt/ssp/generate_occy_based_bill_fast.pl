#!/usr/bin/perl

use strict;
use warnings;
use lib qw/lib/;
use DB::Emscloud;
use DB::EmReplica;
use CaptureTime;
use DateTime;
use DBI;

my $emscloud = DB::Emscloud->new();
my $customer = $ARGV[0];
my $siteList = $emscloud->getSiteList($customer);

# Think about parameters
#     - time constraints when occy based billing is in effect (sweep timer times for example)
#     - percentage of sensors occupied to consider floor occyd
# 
# Get sites
#     Get EMs
#         Get floors (filter out empty floors somehow)
#             Get hourly data
#             Get # commissioned fixtures
#                 For each hour
#                     Get occupied sensor count
# 
# For Site
#     Output hourly data
#         Capture at, Occy count, Fixture count, Floor 1 baseline, Floor 1 consumed, Floor 2 baseline, Floor 2 consumed, ... Floor N baseline, Floor N consumed
#     Output Sum data? -- start in Excel until algo is fine tuned.

my @gapEms = qw/2502 2500 2503 2501/;
my @oldNavyEms = qw/2497 2498 2496 2495 2499/;
my $from = $ARGV[0];
my $to = $ARGV[1];
my $prefix = $ARGV[2];

sub dumpData($$$$) {
    my $ems = shift;
    my $from = shift;
    my $to = shift;
    my $file = shift;

    my $data = {};
    my @floors = ();
    for my $em (@$ems) {
        my $r = DB::EmReplica->new("replica8.enlightedcloud.net", "em_500_$em");
        my $floors = $r->query("select id, name from floor", ["id", "name"]);
        for my $floor (@$floors) {
            my $fixtureCount = $r->getSensorCountForFloor($floor->{id});
            next unless $fixtureCount;
            push @floors, "$em-$floor->{name}";
    
            my $hData = $r->getFloorLevelHourlyData($floor->{id}, $from, $to);
            my $prev_hour = $from;
            for my $entry (@$hData) {
                my $capture = $entry->{capture_at};
                if(!exists $data->{$capture}) {
                    $data->{$capture} = {};
                }
                my $occyCount = $r->getOccyFixtureCount($floor->{id}, $prev_hour, $capture);
                $data->{$capture}->{"$em-$floor->{name}"} = {
                    baseline => $entry->{base_power_used},
                    consumed => $entry->{power_used},
                    occy => $entry->{occy},
                    tuning => $entry->{tuning},
                    ambient => $entry->{ambient},
                    manual => $entry->{manual},
                    fixtureCount => $fixtureCount,
                    occyFixtureCount => $occyCount,
                    occyPct => sprintf("%.0f", 100*$occyCount/$fixtureCount)
                };
                $prev_hour = $capture;
            }
        }
    }

    open(PHILE, ">$file") or die "Cannot open $file: $!";

    print PHILE "Capture";
    for my $floor (sort @floors) {
        print PHILE ",$floor occupied, $floor fixture count, $floor occupied percent, $floor baseline, $floor consumed, $floor occupancy savings, $floor task tuning, $floor ambient savings, $floor manual savings";
    }
    print PHILE "\n";
    
    for my $capture (sort {
        my $lDate = CaptureTime->new($a);
        my $rDate = CaptureTime->new($b);
        $lDate->compare($rDate);
        } keys %$data) {
        print PHILE "$capture";
        for my $floor (sort keys %{$data->{$capture}}) {
            print PHILE ",$data->{$capture}->{$floor}->{occyFixtureCount}, $data->{$capture}->{$floor}->{fixtureCount}, $data->{$capture}->{$floor}->{occyPct}, $data->{$capture}->{$floor}->{baseline}, $data->{$capture}->{$floor}->{consumed}, $data->{$capture}->{$floor}->{occy}, $data->{$capture}->{$floor}->{tuning}, $data->{$capture}->{$floor}->{ambient}, $data->{$capture}->{$floor}->{manual}";
        }
        print PHILE "\n";
        if($capture =~ /^2015-\d\d-\d\d 00:00/) {
            print PHILE "\n";
        }
    }
}

dumpData(\@gapEms, $from, $to, "${prefix}_gap.csv");
dumpData(\@oldNavyEms, $from, $to, "${prefix}_old_navy.csv");
