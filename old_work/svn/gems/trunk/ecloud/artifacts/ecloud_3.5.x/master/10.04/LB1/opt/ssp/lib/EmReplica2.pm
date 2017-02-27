package DB::EmReplica;

use strict;
use warnings;
use lib qw/lib/;
use base qw/DB/;
use DB;
use DBI;

sub new($$$) {
    my $class = shift;
    my $replica = shift;
    my $db = shift;
    my $self = $class->SUPER::new("dbi:Pg:dbname=$db;host=$replica");
    bless $self, $class;
    return $self;
}

sub getGatewayInfo($) {
    my $self = shift;
    my $dbh = $self->connection();
    my $sth = $dbh->prepare("SELECT g.id, g.status, g.ip_address, g.snap_address, g.channel, g.wireless_networkid, f.name FROM gateway g, floor f, device d WHERE g.commissioned=true AND d.id=g.id AND d.floor_id=f.id");
    $sth->execute();
    my @gws = map { DB::hashify(['id', 'status', 'ipAdress', 'macAddress', 'channel', 'panID', 'floor'], $_) } @{$sth->fetchall_arrayref};
    foreach my $gw (@gws) {
        $sth = $dbh->prepare("SELECT count(*) FROM fixture WHERE gateway_id=$gw->{id} AND state='COMMISSIONED'");
        $sth->execute();
        $gw->{numberOfFixtures} = $sth->fetchall_arrayref()->[0]->[0];

        $sth = $dbh->prepare("SELECT count(*) FROM fixture WHERE gateway_id=$gw->{id} AND state='COMMISSIONED' AND is_hopper=1");
        $sth->execute();
        $gw->{numberOfHoppers} = $sth->fetchall_arrayref()->[0]->[0];
    }

    return \@gws;
}

sub getSensorCountForFloor($$) {
    my $self = shift;
    my $floor = shift;
    my $dbh = $self->connection();
    my $sth = $dbh->prepare("SELECT count(f.*) FROM fixture f, device d WHERE d.id=f.id AND f.state='COMMISSIONED' AND d.floor_id=$floor");
    $sth->execute();
    return $sth->fetchall_arrayref()->[0]->[0];
}

sub getOccyFixtureCount($$$$) {
    my $self = shift;
    my $floor = shift;
    my $from = shift;
    my $to = shift;
    my $dbh = $self->connection();
    my $sth = $dbh->prepare("SELECT count(e.*)/12 FROM energy_consumption e, device d WHERE d.id=e.fixture_id AND d.floor_id=$floor AND e.capture_at>'$from' AND e.capture_at<='$to' AND e.motion_bits!=0");
    $sth->execute();
    return $sth->fetchall_arrayref()->[0]->[0];
}

sub getOccyFixtureMonth($$$$) {
    my $self = shift;
    my $floor = shift;
    my $from = shift;
    my $to = shift;
    my $dbh = $self->connection();
    my $sth = $dbh->prepare(" SELECT floor_id, to_char(capture_at - 0.0001 * INTERVAL '1 second', 'YYYY-MM-DD HH24:00:00') as hour, count(e.*)/12 FROM energy_consumption e, device d WHERE d.id=e.fixture_id AND e.capture_at>'$from' AND e.capture_at<='$to' AND e.motion_bits!=0 group by floor_id, to_char(capture_at - 0.0001 * INTERVAL '1 second', 'YYYY-MM-DD HH24:00:00')");
    $sth->execute();
    my @gws = map { DB::hashify(['floor_id', 'hour', 'CT'], $_) } @{$sth->fetchall_arrayref};
    my %h;
    foreach my $gw (@gws) {
	$h{$gw->{hour} . $gw->{floor_id}} = $gw->{CT};
    }
    # Dummy entry for the start time since it doesn't have the hour and minute

    return \%h;
}

sub getOccyFixtureArr($$$$) {
    my $self = shift;
    my $floor = shift;
    my $from = shift;
    my $to = shift;
    my $dbh = $self->connection();
    my $sth = $dbh->prepare("SELECT floor_id, count(e.*)/12 as CT FROM energy_consumption e, device d WHERE d.id=e.fixture_id AND e.capture_at>'$from' AND e.capture_at<='$to' AND e.motion_bits!=0 group by floor_id");
    $sth->execute();
    my @gws = map { DB::hashify(['floor_id', 'CT'], $_) } @{$sth->fetchall_arrayref};
    my %h;
    foreach my $gw (@gws) {
	$h{$from . $gw->{floor_id}} = $gw->{CT};
    }

    return \%h;
}

sub getSensorCount($) {
    my $self = shift;
    my $dbh = $self->connection();
    #my $sth = $dbh->prepare("SELECT count(*) FROM fixture WHERE state='COMMISSIONED'");
    my $sth = $dbh->prepare("SELECT count(*) FROM fixture");
    $sth->execute();
    return $sth->fetchall_arrayref()->[0]->[0];
}

sub getEventCount($) {
    my $self = shift;
    my $dbh = $self->connection();
    my $sth = $dbh->prepare("SELECT count(*) FROM events_and_fault");
    $sth->execute();
    return $sth->fetchall_arrayref()->[0]->[0];
}

sub getFixtureInfo($$) {
    my $self = shift;
    my $id = shift;

    if($id) {
        return $self->query("SELECT f.sensor_id, f.gateway_id, f.sec_gw_id, d.version FROM fixture f, device d WHERE f.id=d.id and f.id=$id",
            ["name", "gateway_id", "sec_gw_id", "version"])->[0];
    }

    return $self->query("SELECT f.sensor_id, f.gateway_id, f.sec_gw_id, d.version FROM fixture f, device d WHERE f.id=d.id",
        ["name", "gateway_id", "sec_gw_id", "version"]);
}

sub getEnergyData($$$$$;$) {
    my $self = shift;
    my $granularity = shift;
    my $from = shift;
    my $to = shift;
    my $cols = shift;
    my $additionalConstraints = shift;
    my $table;

    if($additionalConstraints) {
        $additionalConstraints = " AND $additionalConstraints";
    }
    else {
        $additionalConstraints = "";
    }

    if($granularity eq '5min') {
        $table = "energy_consumption";
    }
    elsif($granularity eq 'hour') {
        $table = "energy_consumption_hourly";
    }
    elsif($granularity eq 'day') {
        $table = "energy_consumption_daily";
    }
    else {
        die "granularity $granularity is not 5min, hour, or day";
    }

    return $self->query("SELECT " . join(",", @$cols) . " FROM $table WHERE capture_at>'$from' AND capture_at<='$to' $additionalConstraints ORDER BY capture_at, fixture_id",
        $cols);
}

sub getSummedEnergyData($$$$;$) {
    my $self = shift;
    my $granularity = shift;
    my $from = shift;
    my $to = shift;
    my $additionalConstraints = shift;
    my $table;
    my $used = "SUM(power_used)";
    my $base = "SUM(base_power_used)";
    my $saved = "SUM(saved_power_used)";

    if($additionalConstraints) {
        $additionalConstraints = " AND $additionalConstraints";
    }
    else {
        $additionalConstraints = "";
    }

    if($granularity eq '5min') {
        $table = "energy_consumption";
        $used = "ROUND($used/12,2)";
        $base = "ROUND($base/12,2)";
        $saved = "ROUND($saved/12,2)";
    }
    elsif($granularity eq 'hour') {
        $table = "energy_consumption_hourly";
    }
    elsif($granularity eq 'day') {
        $table = "energy_consumption_daily";
    }
    else {
        die "granularity $granularity is not 5min, hour, or day";
    }

    return $self->query("SELECT fixture_id, $used, $base, $saved FROM $table WHERE capture_at>'$from' AND capture_at<='$to' $additionalConstraints GROUP BY fixture_id ORDER BY fixture_id",
        ['fixture_id', 'power_used', 'base_power_used', 'saved_power_used']);
}

sub getFloorLevelHourlyData($$$$) {
    my $self = shift;
    my $floor = shift;
    my $from = shift;
    my $to = shift;

    return $self->query("SELECT capture_at, SUM(e.base_power_used), SUM(e.power_used), SUM(e.occ_saving), SUM(e.tuneup_saving), SUM(e.ambient_saving), SUM(e.manual_saving) FROM energy_consumption_hourly e, device d WHERE e.fixture_id=d.id AND d.floor_id=$floor AND e.capture_at>'$from' AND capture_at<='$to' GROUP BY capture_at ORDER BY capture_at", ['capture_at', 'base_power_used', 'power_used', 'occy', 'tuning', 'ambient', 'manual']);
}

1;
