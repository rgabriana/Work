<?php
$last_connectivity_at = date("Y-m-d H:i:s");
$last_stats_rcvd_time = date("Y-m-d H:i:s");
$ballast_last_changed = date("Y-m-d H:i:s");
$general_date = date("Y-m-d H:i:s");

$maxx = 750;
$maxy = 950;
$x = 1;
$y = 1;

$no_of_fixtures = 100;
$subnet = 1;
$no_of_nodes_count = 1;
$startingfixtureindex = 1;

echo "insert into fixture (id, sensor_id, floor_id, area_id, sub_area_id, profile_id, type, ballast_type, ballast_last_changed, campus_id, building_id, x, y, no_of_bulbs, bulb_wattage, wattage, ballast_manufacturer, bulb_manufacturer, profile_handler_id, current_profile, original_profile_from, dimmer_control, current_state, savings_type, last_occupancy_seen, light_level, snap_address, fixture_name, mac_address, channel, version, aes_key, location, gateway_id, sec_gw_id, description, notes, bulbs_last_service_date, ballast_last_service_date, active, state, ballast_id, bulb_id, bulb_life, no_of_fixtures, last_connectivity_at, ip_address, comm_type, last_stats_rcvd_time) values ";
$totalcount = $startingfixtureindex + $no_of_fixtures;
for ($count = $startingfixtureindex; $count <= $totalcount; $count++) {
	if ($no_of_nodes_count > 254) {
		$subnet = $subnet + 1;
		$no_of_nodes_count = 1;
	}
	$no_of_nodes_count = $no_of_nodes_count + 1;
	$ip_address = "10.96." . $subnet . "." . $no_of_nodes_count;
	$mac_address = unique_id();
	$snap_address = substr($mac_address, 4,2) . ":" . substr($mac_address, 6, 2) . ":" . substr($mac_address, 8, 2);
	$name = "Sensor" . substr($mac_address, 4,2) . substr($mac_address, 6, 2) . substr($mac_address, 8, 2);
	$campus_id = 1;
	$building_id = 1;
	$floor_id = 1;
	$ballast_id = 9;
	$gateway_id = 1;
	$sec_gw_id = 1;

	if ($x < $maxx) {
		$x = $x + 3;
	} else {
		$x = 1;
	}

	if ($y < $maxy) {
		$y = $y + 3;
	} else {
		$y = 1;
	}

	echo "(" . $count . ", '" .  $name . "', " .  $floor_id . ", " .  0 . ", " .  0 . ", " .  1 . ", " .  "''" . ", " .  "''" . ", '" .  $ballast_last_changed . "', " .  $campus_id . ", " .  $building_id . ", " .  $x . ", " .  $y . ", " .  2 . ", " .  18 . ", " .  32 . ", " .  "'OSRAM'" . ", " .  "'Philips'" . ", " .  1 . ", " .  "'Default'" . ", " .  "'Default'" . ", " .  5 . ", " .  "''" . ", " .  "''" . ", " .  1 . ", " .  177 . ", " .  "'" . $snap_address . "', '" .  $name . "', '" .  $mac_address . "', " .  0 . ", " .  "'1.4.2 b147'" . ", " .  "''" . ", " .  "'1st Floor'" . ", " .  $gateway_id . ", " . $sec_gw_id . ", " . "''" . ", " .  "''" . ", '" .  $general_date . "', '" .  $general_date . "', " . "'t'" . ", " .  "'COMMISSIONED'" . ", " .  $ballast_id . ", " .  1 . ", " .  100 . ", " .  1 . ", '" .  $last_connectivity_at . "', '" .  $ip_address . "', " .  2 . ", '" .  $last_stats_rcvd_time . "')";  

	if ($count == $totalcount)
		echo ";\n";
	else
		echo ",\n";
}

#
function unique_id(){
	$better_token = md5(uniqid(rand(), true));
	$unique_code = substr($better_token, 16, 10);
	$uniqueid = $unique_code;
	return $uniqueid;
}

?>
