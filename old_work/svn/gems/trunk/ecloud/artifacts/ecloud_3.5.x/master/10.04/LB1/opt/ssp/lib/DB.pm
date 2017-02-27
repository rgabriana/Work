package DB;

use strict;
use warnings;
use lib qw/lib/;
use DBI;

# Class Definition
sub new($$) {
    my $class = shift;
    my $source = shift;
    my $self = {};
    $self->{_source} = $source;
    $self->{_dbh} = DBI->connect($source, 'postgres', '', {AutoCommit => 0});
    if(!defined $self->{_dbh}) {
        die "Could not conenct to db: $source. $DBI::errstr";
    }
    bless $self, $class;
    return $self;
}

sub source($) {
    my $self = shift;
    return $self->{_source};
}

sub connection($) {
    my $self = shift;
    return $self->{_dbh};
}

sub query($$;$) {
    my $self = shift;
    my $query = shift;
    my $keys = shift;
    my $dbh = $self->connection();
    my $sth = $dbh->prepare($query);
    $sth->execute();
    if($keys) {
        return [map { hashify($keys, $_) } @{$sth->fetchall_arrayref()}];
    }
    else {
        return $sth->fetchall_arrayref();
    }
}

sub DESTROY($) {
    my $self = shift;
    $self->{_dbh}->disconnect() if $self->{_dbh};
}

# End Class Definition

sub hashify($$) {
    my $keys = shift;
    my $vals = shift;
    my $retval = {};

    if($#{$keys} != $#{$vals}) {
        die "Key and values have different lengths: keys - $#{$keys}, vals - $#{$vals}";
    }

    for(my $i=0;$i<=$#{$keys};$i++) {
        $retval->{$keys->[$i]} = $vals->[$i];
    }

    return $retval;
}

1;
