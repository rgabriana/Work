#!/usr/bin/perl

use strict;
use warnings;
use lib qw/lib/;
use DB::Emscloud;
use DB::EmReplica;
use SiteData;
use DateTime;
use DBI;
use Data::Dumper;
use LWP::UserAgent;

my $customer = $ARGV[0];
my $emscloud = DB::Emscloud->new();

sub getLowBaselineSites() {
    my $ua = LWP::UserAgent->new;
    my $dt = DateTime->now();
    $dt->subtract(days => 3);
    my $response = $ua->get("https://65.61.168.47:8181/sim/huntr/searchr?start=" . $dt->ymd() . "&floor=0&search=LOW");
    die $response->status unless $response->is_success;
    my $retVal = {};
    my $nodeSiteMap = {};

    my @lines = split(/\n/, $response->content);
    for my $line (@lines) {
        next if $line =~/FOUND/;
        my @toks = split(/\t/, $line);
        my $siteId;
        if(!exists $nodeSiteMap->{$toks[0]}) {
            $nodeSiteMap->{$toks[0]} = $emscloud->getSiteForNodeId($toks[0]);
        }
        $siteId = $nodeSiteMap->{$toks[0]};

        if(!exists $retVal->{$siteId}) {
            $retVal->{$siteId} = {
                exposed => {},
                total => 0
            };
            for my $em (@{ $emscloud->getEMsForSite($siteId) }) {
                my $replica;
                eval {
                    $replica = DB::EmReplica->new($em->{replicaServerIP}, $em->{dbName});
                };
                if($@) {
                    next;
                }

                $retVal->{$siteId}->{total} += $replica->getSensorCount();
            }
        }

        $retVal->{$siteId}->{exposed}->{$toks[2]} = 1;
    }

    for my $site (keys %$retVal) {
        my @exposed = keys %{$retVal->{$site}->{exposed}};
        $retVal->{$site}->{exposed} = $#exposed + 1;
    }

    return $retVal;
}

sub findSiteIssues($$) {
    my $siteId = shift;
    my $lbSites = shift;
    my $siteData = SiteData->new($siteId);
    my $ems = $emscloud->getEMsForSite($siteId);
    my @hopperIssues = ();
    my @eventIssues = ();
    my $radioParams = {};
    my @fixtureVersions = ();
    my $retVal = {};

    for my $em (@$ems) {
        my $replica;
        eval {
            $replica = DB::EmReplica->new($em->{replicaServerIP}, $em->{dbName});
        };
        if($@) {
            next;
        }
        my $gwInfo = $replica->getGatewayInfo();
        foreach my $gw (@$gwInfo) {
            next unless $gw->{numberOfFixtures};

            my $chan = $gw->{channel};
            my $pan = $gw->{panID};
            if(exists $radioParams->{$pan}) {
                if(exists $radioParams->{$pan}->{$chan}) {
                    push @{$radioParams->{$pan}->{$chan}}, "$em->{emID}:$em->{emName}:$gw->{id}:$gw->{macAddress}";
                }
                else {
                    $radioParams->{$pan}->{$chan} = ["$em->{emID}:$em->{emName}:$gw->{id}:$gw->{macAddress}"];
                }
            }
            else {
                $radioParams->{$pan}->{$chan} = ["$em->{emID}:$em->{emName}:$gw->{id}:$gw->{macAddress}"];
            }

            if($gw->{numberOfHoppers} > 7) {
                push @hopperIssues, "$em->{emID}:$em->{emName}:$gw->{id}:$gw->{macAddress}:$gw->{numberOfHoppers}";
            }
        }

        my $eventCount = $replica->getEventCount();
        if($eventCount >= 100000) {
            push @eventIssues, "$em->{emID}:$em->{emName}:$eventCount";
        }

        my $fixtureInfo = $replica->getFixtureInfo();
        my $total = 0;
        for my $fixture (@$fixtureInfo) {
            if($fixture->{version} =~ /2\.6\.7/) {
                $total++;
            }
        }
        if($total) {
            push @fixtureVersions, "$em->{emID}:$em->{emName}:$total";
        }
    }

    if($#eventIssues >= 0) {
        $siteData->tooManyEventsIssues(join(',', @eventIssues));
        $retVal->{tooManyEvents} = "X";
    }
    else {
        $siteData->tooManyEventsIssues('');
        $retVal->{tooManyEvents} = "";
    }

    if($#hopperIssues >= 0) {
        $siteData->hopperIssues(join(',', @hopperIssues));
        $retVal->{tooManyHoppers} = "X";
    }
    else {
        $siteData->hopperIssues('');
        $retVal->{tooManyHoppers} = "";
    }

    my @duplicates = ();
    my @bleed = ();
    for my $pan (keys %$radioParams) {
        my $start;
        my $end;
        my $bleedData = "";
        for my $chan (sort { $a <=> $b } keys %{$radioParams->{$pan}}) {
            if($#{$radioParams->{$pan}->{$chan}} > 0) {
                push @duplicates, "$chan/$pan=" . join('/', @{$radioParams->{$pan}->{$chan}});
            }
            if(defined $start) {
                if(($chan - $end) > 1) {
                    if($start != $end) {
                        push @bleed, "$start,$end/$pan=$bleedData";
                    }
                    $start = $chan;
                    $end = $chan;
                    $bleedData = join(',',@{$radioParams->{$pan}->{$chan}});
                }
                else {
                    $end = $chan;
                    $bleedData .= "," . join(',',@{$radioParams->{$pan}->{$chan}});
                }
            }
            else {
                $start = $chan;
                $end = $chan;
                $bleedData = join(',',@{$radioParams->{$pan}->{$chan}});
            }
        }

        if($start != $end) {
            push @bleed, "$start,$end/$pan=$bleedData";
        }
    }

    if($#duplicates >= 0) {
        $siteData->radioParamIssues(join(',', @duplicates));
        $retVal->{duplicateRadioParams} = $#duplicates + 1;
    }
    else {
        $siteData->radioParamIssues('');
        $retVal->{duplicateRadioParams} = "";
    }

    if($#bleed >= 0) {
        $siteData->channelBleedIssues(join(',', @bleed));
        $retVal->{channelBleed} = $#bleed + 1;
    }
    else {
        $siteData->channelBleedIssues('');
        $retVal->{channelBleed} = "";
    }

    if(exists $lbSites->{$siteId}) {
        $siteData->lowBaselineIssues("$lbSites->{$siteId}->{exposed}/$lbSites->{$siteId}->{total}");
        $retVal->{lowBaseline} = "$lbSites->{$siteId}->{exposed} out of $lbSites->{$siteId}->{total}";
    }
    else {
        $siteData->lowBaselineIssues('');
        $retVal->{lowBaseline} = "";
    }

    if($#fixtureVersions >= 0) {
        $siteData->fixtureVersionIssues(join(',', @fixtureVersions));
        $retVal->{fixtureVersion} = "X";
    }
    else {
        $siteData->fixtureVersionIssues('');
        $retVal->{fixtureVersion} = "";
    }

    #$siteData->commit();

    return $retVal;
}

sub dumpSiteInfo($$$) {
    my $entry = shift;
    my $sr = shift;
    my $lbSites = shift;
    my $i = 0;
    return $sr if(!defined $entry->{id} || $entry->{id} < 0);

    my $issues = findSiteIssues($entry->{id}, $lbSites);
    my $billEntries = $entry->{billEntries};
    my $lastIdx = $#{$billEntries};
    my $rowOne = $sr + 3;
    my $rowN = $sr + 3 + $lastIdx;
    my $retRow;
    my $ctt = "=(D".($sr + 1)."/((SUM(W${rowOne}:W${rowN})/SUM(X${rowOne}:X${rowN})/1000)))/365.25";
    my $lstt = "=(D".($sr + 1)."/((SUMIF(AC${rowOne}:AC${rowN},\"=1\",W${rowOne}:W${rowN})/SUMIF(AC${rowOne}:AC${rowN},\"=1\",X${rowOne}:X${rowN})/1000)))/365.25";
    my $pctS = "=100*(SUM(W${rowOne}:W${rowN})/SUM(U${rowOne}:U${rowN}))";
    my $lsPctS = "=100*(SUMIF(AC${rowOne}:AC${rowN},\"=1\",W${rowOne}:W${rowN})/SUMIF(AC${rowOne}:AC${rowN},\"=1\",U${rowOne}:U${rowN}))";
    my $wtt = "=D" . ($sr + 1) . "*F" . ($sr + 1);
    my $wlstt = "=D" . ($sr + 1) . "*G" . ($sr + 1);

    if($#{$billEntries} >= 0) {
        print "SH|Geo Loc|Site Name|Block Purchased|Current Term Remaining|Computed Total Term|Left Shift Total Term|%Savings|Left Shift % Savings|Weighted Term|Weighted LS Term|GEO Rate|Too Many Events|Dup Radio Params|Channel Bleed|Low Baseline|CU3 Calibration|Too Many Hoppers\n";
        print "SE|$entry->{geoLoc}|$entry->{name}|$entry->{blockPurchaseEnergy}|" . ($billEntries->[$lastIdx]->{blockTermRemaining}/365.25) .
            "|$ctt|$lstt|$pctS|$lsPctS|$wtt|$wlstt|$entry->{geoRate}|$issues->{tooManyEvents}|$issues->{duplicateRadioParams}|$issues->{channelBleed}|$issues->{lowBaseline}||$issues->{tooManyHoppers}\n";

        print "BH|||||||||||||||||||Bill Date|Baseline|Consumed|Saved|Num Days|Savings Per Day|% Savings|Block Energy Remaining|Block Term Remaining|Count\n";

        for($i = 0;$i <= $#{$entry->{billEntries}};$i++) {
            my $be = $entry->{billEntries}->[$i];
            my $savingsPct;
            if($be->{baselineEnergy} != 0){
                $savingsPct = 100*($be->{savedEnergy}/$be->{baselineEnergy});
            }
            else {
                $savingsPct = 0;
            }

            my $count = 0;
            if($entry->{maxSavedPerDay} && $be->{savedPerDay}/$entry->{maxSavedPerDay} >= 0.75) {
                $count = 1;
            }

            print "BE|||||||||||||||||||$be->{billEndDate}|$be->{baselineEnergy}|$be->{consumedEnergy}|$be->{savedEnergy}|$be->{numberOfDays}|$be->{savedPerDay}|$savingsPct|$be->{blockEnergyRemaining}|$be->{blockTermRemaining}|$count\n";
        }
        print "\n";

        $retRow = $rowN + 2;
    }
    else {
        print "SH|Site ID|Site Name|Block Purchased|Current Term Remaining|Computed Total Term|Left Shift Total Term|%Savings|Left Shift % Savings|Weighted Term|Weighted LS Term|GEO Rate\n";
        print "SE|$entry->{id}|$entry->{name}|$entry->{blockPurchaseEnergy}|NA|NA|NA|NA|NA|NA|NA|NA\n";
        print "\n";

        $retRow = $sr + 3;
    }

    return $retRow;
}

my $bes = $emscloud->getBillEntries($customer);
my $lbSites = getLowBaselineSites();

print "RH|Length|Weighted Avg Term|Left Shift Weighted Avg Term\n";
print "RE|<20|=SUMIF(F7:F1048576,B2,J7:J1048576)/SUMIF(F7:F1048576,B2,D7:D1048576)|=SUMIF(G7:G1048576,B2,K7:K1048576)/SUMIF(G7:G1048576,B2,D7:D1048576)\n";
print "RE|<17|=SUMIF(F7:F1048576,B3,J7:J1048576)/SUMIF(F7:F1048576,B3,D7:D1048576)|=SUMIF(G7:G1048576,B3,K7:K1048576)/SUMIF(G7:G1048576,B3,D7:D1048576)\n";
print "RE|<15|=SUMIF(F7:F1048576,B4,J7:J1048576)/SUMIF(F7:F1048576,B4,D7:D1048576)|=SUMIF(G7:G1048576,B4,K7:K1048576)/SUMIF(G7:G1048576,B4,D7:D1048576)\n";
print "RE|<14|=SUMIF(F7:F1048576,B5,J7:J1048576)/SUMIF(F7:F1048576,B5,D7:D1048576)|=SUMIF(G7:G1048576,B5,K7:K1048576)/SUMIF(G7:G1048576,B5,D7:D1048576)\n";
print "\n";

my $row = 7;
for my $entry (@$bes) {
    $row = dumpSiteInfo($entry, $row, $lbSites);
}
