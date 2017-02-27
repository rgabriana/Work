<?php
/********************************************************************************/
// php insertBallastCurve.php --ballastid=10 --map="/usr/local/google/home/enlighted/88_voltmap.csv"
/********************************************************************************/

$args_arr = parseArgs($argv);
$fixtureId = $args_arr["fixtureid"];
$curvedatafile = $args_arr["map"];

if ($fixtureId != null && $curvedatafile != null) {
		addFixtureCurve($fixtureId, $curvedatafile);
}else {
	showHelp();
}

function addFixtureCurve($fixture_id, $curvedatafile) {
	$select_flc_sql = "psql -U postgres ems -c \"select id from fixture_lamp_calibration where fixture_id = " . $fixture_id . "\" | awk 'NR == 3 {print $0}'";	
	$VOLT_POWER_MAP_ID = trim(exec($select_flc_sql));
	echo $VOLT_POWER_MAP_ID . "\n";
	if ($VOLT_POWER_MAP_ID == "(0 rows)") {
			$insert_flc_sql = "psql -U postgres ems -c \"insert into fixture_lamp_calibration (id, capture_at, fixture_id, initial) values (nextval('fixture_lamp_calibration_seq'), CURRENT_TIMESTAMP, " . $fixture_id . ", 't')\"";	
			echo $insert_flc_sql . "\n";
			$result = exec($insert_flc_sql);
			echo $result . "\n";
			$select_flc_sql = "psql -U postgres ems -c \"select id from fixture_lamp_calibration where fixture_id = " . $fixture_id . "\" | awk 'NR == 3 {print $0}'";	
			$VOLT_POWER_MAP_ID = trim(exec($select_flc_sql));
			echo $VOLT_POWER_MAP_ID . "\n";
			if ($VOLT_POWER_MAP_ID != "(0 rows)") {
					$volt_power_curve_sql = "psql -U postgres ems -c \"insert into fixture_calibration_map(id, fixture_lamp_calibration_id, volt, power, lux) values " . "\n";
					$row = 0;
					if (($handle = fopen($curvedatafile, "r")) !== FALSE) {
						$volt = 0;
						$power = 0.0;
						while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
							$row++;
							if ($row == 1)
								continue;
							$num = count($data);
							if ($num == 3) {
								if ($row > 2) {
									$volt_power_curve_sql .= ", \n";
								}
								$volt = $data[0];
								$power = $data[1];
								$lux = $data[2];
							}
							$volt_power_curve_sql .= "(nextval('fixture_calibration_map_seq'), " . $VOLT_POWER_MAP_ID. "," . $volt . "," .  $power . "," . $lux . ")";
						}
						fclose($handle);
					}
					$volt_power_curve_sql .= "\"";
					//echo $volt_power_curve_sql . "\n";
					$result = exec($volt_power_curve_sql);
					echo $result;
			}
	}else {
			$row = 0;
			if (($handle = fopen($curvedatafile, "r")) !== FALSE) {
				$volt = 0;
				$power = 0.0;
				while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
					$row++;
					if ($row == 1)
						continue;
					$num = count($data);
					if ($num == 3) {
						$volt = $data[0];
						$power = $data[1];
						$lux = $data[2];
					}
					$update_volt_power_curve_sql = "psql -U postgres ems -c \"update fixture_calibration_map set power=" . $power . " where fixture_lamp_calibration_id=" . $VOLT_POWER_MAP_ID . " and volt=" . $volt . "\"";
					//echo $update_volt_power_curve_sql . "\n";
					$update_volt_power_curve_result = exec($update_volt_power_curve_sql);
					echo $update_volt_power_curve_result . "\n";
				}
				fclose($handle);
			}
	}
}

function parseArgs($argv) {
    array_shift($argv);
    $out = array();
    foreach ($argv as $arg){
        if (substr($arg,0,2) == '--'){
            $eqPos = strpos($arg,'=');
            if ($eqPos === false){
                $key = substr($arg,2);
                $out[$key] = isset($out[$key]) ? $out[$key] : true;
            } else {
                $key = substr($arg,2,$eqPos-2);
                $out[$key] = substr($arg,$eqPos+1);
            }
        } else {
            $out[] = $arg;
        }
    }
    return $out;
}

function showHelp() {
	$help = "Usage:\r\n";
	$help .="php insertBallastCurve.php --ballastid=<ballastid> --map=\"/usr/local/google/home/enlighted/voltmap.csv\"";
	print($help);
}
?>
