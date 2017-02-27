s|[\t ]*sensor_id|<fixtures>|g
/------/d
s|[\t ]Sensor\([0-9a-f][0-9a-f]\)\([0-9a-f][0-9a-f]\)\([0-9a-f][0-9a-f]\)|<fixture><macaddress>\1:\2:\3</macaddress></fixture>|g
s|0\([0-9a-f]\)|\1|g
s|([0-9]* rows)|</fixtures>|g
