package CaptureTime;

use strict;
use warnings;

sub new ($$) {
    my $class = shift;
    my $captureAt = shift;
    my $self = {};
    if($captureAt =~ /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/) {
        $self->{_year} = $1;
        $self->{_month} = $2;
        $self->{_day} = $3;
        $self->{_hour} = $4;
        $self->{_minute} = $5;
        $self->{_second} = $6;

        bless $self, $class;
        return $self;

    }
    die "capture time not parseable: $captureAt";
}

sub asString($) {
    my $self = shift;
    return $self->year() . "-" . $self->month() . "-" .$self->day() . " " . $self->hour() . ":" . $self->minute() . ":" . $self->second();
}

sub year($) {
    my $self = shift;
    return $self->{_year};
}

sub month($) {
    my $self = shift;
    return $self->{_month};
}

sub day($) {
    my $self = shift;
    return $self->{_day};
}

sub hour($) {
    my $self = shift;
    return $self->{_hour};
}

sub minute($) {
    my $self = shift;
    return $self->{_minute};
}

sub second($) {
    my $self = shift;
    return $self->{_second};
}

sub compare($$) {
    my $self = shift;
    my $rhs = shift;

    return -1 if $self->year() < $rhs->year();
    return 1 if $self->year() > $rhs->year();

    return -1 if $self->month() < $rhs->month();
    return 1 if $self->month() > $rhs->month();

    return -1 if $self->day() < $rhs->day();
    return 1 if $self->day() > $rhs->day();

    return -1 if $self->hour() < $rhs->hour();
    return 1 if $self->hour() > $rhs->hour();

    return -1 if $self->minute() < $rhs->minute();
    return 1 if $self->minute() > $rhs->minute();

    return -1 if $self->second() < $rhs->second();
    return 1 if $self->second() > $rhs->second();

    return 0;
}

sub equals($$) {
    my $self = shift;
    my $rhs = shift;

    return 1 if $self->compare($rhs) == 0;
    return 0;
}

1;
