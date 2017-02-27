package DB::Emscloud;

use strict;
use warnings;
use lib qw/lib/;
use base qw/DB/;
use DB;
use DBI;

sub new($) {
    my $class = shift;
    my $self = $class->SUPER::new('dbi:Pg:dbname=emscloud;host=192.168.0.231');
    bless $self, $class;
    return $self;
}

sub getSiteList($;$) {
    my $self = shift;
    my $customer = shift;
    my $custConstraint = "";

    if(defined $customer) {
        $custConstraint = "AND customer_id=$customer ";
    }

    return $self->query("SELECT id, geo_location, name, bill_start_date, block_purchase_energy FROM site where bill_start_date IS NOT NULL $custConstraint ORDER BY name",
        ['id', 'geoLoc', 'name', 'releaseDate', 'blockPurchaseEnergy']);
}

sub getSiteInfo($$) {
    my $self = shift;
    my $id = shift;

    # Just return the first row since there should only be one (ID is a unique column)
    return $self->query("SELECT id, geo_location, name, bill_start_date, block_purchase_energy FROM site where bill_start_date IS NOT NULL and id=$id",
        ['id', 'geoLoc', 'name', 'releaseDate', 'blockPurchaseEnergy'])->[0];
}

sub getBillEntries($;$) {
    my $self = shift;
    my $customer = shift;
    my $siteList = $self->getSiteList($customer);
    my @sites = map { $_->{id} } @$siteList;
    my $sites = join(',', @sites);

    my $billEntries = $self->query("SELECT b.bill_end_date, s.id, s.geo_location, s.name, s.sppa_price, b.baseline_energy, b.consumed_energy, b.baseline_energy - b.consumed_energy, b.no_of_days, (b.baseline_energy - b.consumed_energy)/b.no_of_days, b.block_energy_remaining, b.block_term_remaining FROM sppa_bill b, site s WHERE b.em_instance_id=s.id AND s.id in ($sites) ORDER BY s.name, b.bill_end_date, b.id desc",
        ['billEndDate', 'siteID', 'geoLoc', 'siteName', 'geoRate', 'baselineEnergy', 'consumedEnergy', 'savedEnergy', 'numberOfDays', 'savedPerDay', 'blockEnergyRemaining', 'blockTermRemaining']);

    # Add bill entries to the site list. We are relying on the fact that they are both sorted by site name to
    # merge them together without having to convert one to a hash or some other convoluted process.
    my $currSite=0;
    my $lastDate = "";

    my $j = 0;
    for(my $i=0;$i<=$#{$siteList};$i++) {
        $siteList->[$i]->{billEntries} = [];
        $siteList->[$i]->{maxSavedPerDay} = 0;
        $lastDate = "";

        while($j <= $#{$billEntries} && $siteList->[$i]->{id} == $billEntries->[$j]->{siteID}) {
            if($lastDate ne $billEntries->[$j]->{billEndDate}) {
                $lastDate = $billEntries->[$j]->{billEndDate};
                for my $key (qw/baselineEnergy consumedEnergy savedEnergy numberOfDays savedPerDay blockEnergyRemaining blockTermRemaining/) {
                    if(!defined $billEntries->[$j]->{$key}) {
                        $billEntries->[$j]->{$key} = 0;
                    }
                }

                push @{$siteList->[$i]->{billEntries}}, $billEntries->[$j];
                if(defined $billEntries->[$j]->{savedPerDay} && $siteList->[$i]->{maxSavedPerDay} < $billEntries->[$j]->{savedPerDay}) {
                    $siteList->[$i]->{maxSavedPerDay} = $billEntries->[$j]->{savedPerDay};
                }
            }
            $j++;
        }
    }

    if($j <= $#{$billEntries}) {
        die "Bill entries are still left after traversing all sites. $j $#{$billEntries}";
    }

    return $siteList;
}

sub getEMsForSite($$) {
    my $self = shift;
    my $siteId = shift;
    my $dbh = $self->connection();
    return $self->query("SELECT e.id, e.name, e.mac_id, r.internal_ip, e.database_name, e.last_connectivity_at FROM em_instance e, em_site es, replica_server r WHERE es.em_id=e.id AND es.site_id=$siteId AND e.replica_server_id=r.id",['emID', 'emName', 'macAddress', 'replicaServerIP', 'dbName', 'lastConnectivity']);
}

sub getEMs($) {
    my $self = shift;
    my $dbh = $self->connection();
    return $self->query("SELECT e.id, e.name, e.mac_id, r.internal_ip, e.database_name, e.last_connectivity_at FROM em_instance e, replica_server r WHERE e.replica_server_id=r.id",['emID', 'emName', 'macAddress', 'replicaServerIP', 'dbName', 'lastConnectivity']);
}

sub getSiteForNodeId($$) {
    my $self = shift;
    my $node = shift;
    my $dbh = $self->connection();
    my $sth = $dbh->prepare("SELECT s.id FROM site s, facility_em_mapping fe, em_site es WHERE fe.em_id=es.em_id AND s.id=es.site_id AND fe.facility_id=$node");
    $sth->execute();
    return $sth->fetchall_arrayref()->[0]->[0];
}

sub getEMForNodeId($$) {
    my $self = shift;
    my $node = shift;
    my $dbh = $self->connection();

    return $self->query("SELECT e.id, e.name, e.mac_id, r.internal_ip, e.database_name, e.last_connectivity_at FROM em_instance e, facility_em_mapping fe, replica_server r WHERE fe.em_id=e.id AND fe.facility_id=$node AND e.replica_server_id=r.id",['emID', 'emName', 'macAddress', 'replicaServerIP', 'dbName', 'lastConnectivity'])->[0];
}

1;
