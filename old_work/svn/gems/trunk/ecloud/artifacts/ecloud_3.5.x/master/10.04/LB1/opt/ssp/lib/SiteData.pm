package SiteData;

use strict;
use warnings;
use lib qw/lib/;
use val;
use Data::Dumper;
use File::Copy;
use Time::gmtime;

my $dataDir = '/home/enlighted/ssp/site_data';

our $open = 'open';
our $causeFound = 'cause found';
our $fixed = 'fixed';
our $npf = 'no problem found';
my @states = ($open, $causeFound, $fixed, $npf);

# Class method
sub list() {
    my $retval = [];
    opendir(DH, $dataDir) || die "Could not open directory $dataDir: $!";
    while(my $entry = readdir DH) {
        next unless -f "$dataDir/$entry";
        next if $entry =~ /\.last$/;

        push @$retval, $entry;
    }
    closedir(DH);
    return $retval;
}

sub new($$) {
    my $class = shift;
    my $siteId = shift;
    my $self = {};
    $self->{_fileName} = $dataDir . '/' . $siteId;

    if(!-e $self->{_fileName}) {
        $self->{_data} = {
            _comments => [],
            _state => $open,
            _billGap => 0,
            _tooManyHoppers => '',
            _duplicateRadioParams => '',
            _channelBleed => '',
            _cu3Calibration => '',
            _lowBaseline => '',
            _tooManyEvents => '',
            _fixtureVersionIssues => '',
            _otherData => {}
        };
    }
    else {
        if(!-r $self->{_fileName}) {
            die "Site data file $self->{_fileName} is not readable";
        }
        $self->{_data} = do $self->{_fileName};
        if(!defined $self->{_data}) {
            die "Could not load data:\n$!\n$@";
        }
    }

    bless $self, $class;
    return $self;
}

sub state($;$) {
    my $self = shift;
    my $state = shift;

    if(defined $state) {
        if(!$state ~~ @states) {
            die "$state is not a valid state: " . join(',', @states);
        }
        $self->{_data}->{_state} = $state;
    }
    return $self->{_data}->{_state};
}

sub comments($) {
    my $self = shift;
    return $self->{_data}->{_comments};
}

sub addComment($;$) {
    my $self = shift;
    my $comment = shift;
    die "Comment is not a scalar: $comment" if(!val::isScalar($comment));
    push @{$self->{_data}->{_comments}}, { timestamp => Time::gmtime::gmctime(), comment => $comment};
}

sub billGap($;$) {
    my $self = shift;
    my $gap = shift;

    if(defined $gap) {
        if(!val::isNumber($gap)) {
            die "Gap must be a number: $gap";
        }
        $self->{_data}->{_gap} = $gap;
    }
    return $self->{_data}->{_gap};
}

sub hopperIssues($;$) {
    my $self = shift;
    my $val = shift;
    if(defined $val) {
        $self->{_data}->{_tooManyHoppers} = $val;
    }
    return $self->{_data}->{_tooManyHoppers};
}

sub radioParamIssues($;$) {
    my $self = shift;
    my $val = shift;
    if(defined $val) {
        $self->{_data}->{_duplicateRadioParams} = $val;
    }
    return $self->{_data}->{_duplicateRadioParams};
}

sub channelBleedIssues($;$) {
    my $self = shift;
    my $val = shift;
    if(defined $val) {
        $self->{_data}->{_channelBleed} = $val;
    }
    return $self->{_data}->{_channelBleed};
}

sub cuCalibrationIssues($;$) {
    my $self = shift;
    my $val = shift;
    if(defined $val) {
        $self->{_data}->{_cu3Calibration} = $val;
    }
    return $self->{_data}->{_cu3Calibration};
}

sub lowBaselineIssues($;$) {
    my $self = shift;
    my $val = shift;
    if(defined $val) {
        $self->{_data}->{_lowBaseline} = $val;
    }
    return $self->{_data}->{_lowBaseline};
}

sub tooManyEventsIssues($;$) {
    my $self = shift;
    my $val = shift;
    if(defined $val) {
        $self->{_data}->{_tooManyEvents} = $val;
    }
    return $self->{_data}->{_tooManyEvents};
}

sub fixtureVersionIssues($;$) {
    my $self = shift;
    my $val = shift;
    if(defined $val) {
        $self->{_data}->{_fixtureVersionIssues} = $val;
    }
    return $self->{_data}->{_fixtureVersionIssues};
}

sub otherData($;$) {
    my $self = shift;
    my $data = shift;

    if(defined $data) {
        if(!val::isHash($data)) {
            die "Other data must be a HASH ref: " . $data;
        }
        $self->{_data}->{_otherData} = $data;
    }
    return $self->{_data}->{_otherData};
}

sub commit($) {
    my $self = shift;
    my $fh;
    if(-e $self->{_fileName}) {
        File::Copy::copy($self->{_fileName}, "$self->{_fileName}.last") || die "Could not archive previous file: $self->{_fileName}";
    }
    open($fh, ">$self->{_fileName}");
    print $fh Dumper($self->{_data});
    close($fh);
}

sub DESTROY($) {
    my $self = shift;
    $self->commit();
}

1;
