package val;

sub isScalar($) {
    my $val = shift;
    return ref($val) eq '';    
}

sub isNumber($) {
    my $val = shift;

    return $val =~ /^\d+(\.\d+)?$/;
}

sub isHash($) {
    my $val = shift;
    return ref($val) eq 'HASH';
}

1;
