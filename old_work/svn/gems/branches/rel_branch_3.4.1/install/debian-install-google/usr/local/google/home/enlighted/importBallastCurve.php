<?php
/********************************************************************************/
// php insertBallastCurve.php --ballastid=10 --map="/usr/local/google/home/enlighted/88_voltmap.csv"
/********************************************************************************/

$args_arr = parseArgs($argv);
$ballastId = $args_arr["ballastid"];
$curvedatafile = $args_arr["map"];

if ($ballastId != null && $curvedatafile != null) {
		addBallastCurve($ballastId, $curvedatafile);
}else {
	showHelp();
}

function addBallastCurve($ballast_id, $curvedatafile) {
	$volt_power_map_sql = "psql -U postgres ems -c \"select volt_power_map_id from ballasts where id = " . $ballast_id . "\" | awk 'NR == 3 {print $1}'";	
	$VOLT_POWER_MAP_ID = exec($volt_power_map_sql);
	echo $VOLT_POWER_MAP_ID . "\n";
	if ($VOLT_POWER_MAP_ID <= 2) {
			$volt_power_map_sql = "psql -U postgres ems -c \"select max(volt_power_map_id) from ballast_volt_power\" | awk 'NR == 3 {print $1}'";	
			$VOLT_POWER_MAP_ID = exec($volt_power_map_sql) + 1;
			$volt_power_curve_sql = "psql -U postgres ems -c \"insert into ballast_volt_power(id, ballast_id, volt_power_map_id, volt, power) values " . "\n";
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
					$volt_power_curve_sql .= "(nextval('ballast_volt_power_seq'), " . $ballast_id . "," . $VOLT_POWER_MAP_ID . "," . $volt . "," .  $power . ") ";
				}
				fclose($handle);
			}
			$volt_power_curve_sql .= "\"";
			//echo $volt_power_curve_sql;
			$volt_power_curve_result = exec($volt_power_curve_sql);
			echo $volt_power_curve_result . "\n";
			$updateBallastSQL = "psql -U postgres ems -c \"update ballasts set volt_power_map_id=" . $VOLT_POWER_MAP_ID . " where id=" . $ballast_id . "\"";	
			$result = exec($updateBallastSQL);
			echo $result . "\n";
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
					$update_volt_power_curve_sql = "psql -U postgres ems -c \"update ballast_volt_power set power=" . $power . " where volt_power_map_id=" . $VOLT_POWER_MAP_ID . " and volt=" . $volt . "\"";
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
