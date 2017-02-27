<?php
	$year = 2010;
	$number_of_months = 11;
	$fixtureid = 1;
	
	//getCMD($argc, $argv);

	//echo "(Year, Month, Fixture) => (" . $GLOBALS["year"] . ", " . $GLOBALS["number_of_months"] . ", " . $GLOBALS['fixture'] . ")\n";

	generateData();

	function generateData()
	{
		$fixtures = array(1, 350);
		$nooffixtures = count($fixtures);
		$min = 0;
		$count = 0;
		$mins = 0;
		$hour = 0;
		for ($month = 1; $month <= $GLOBALS["number_of_months"]; $month++) {
			//echo "Number of Days: " . MonthDays($month, $GLOBALS["year"]) . "\n";
			$noofdays = MonthDays($month, $GLOBALS["year"]);
			$days = 0;
			for ($day = 1; $day < $noofdays; $day++) { 
				$hour = 0;
				$mins = 0;
				addHeader();
				for ($min = 0; $min <= 1440; $min+=5) {
					$capture_date = date("Y-m-d H:i:s", mktime(0, $min, 0, $month, $day, $GLOBALS["year"]));
					//echo (date("Y-m-d H:i:s", mktime(0, $min, 0, $month, $day, $GLOBALS["year"]))) . "\n";
					$count++;
					if ($mins != 60) {
						$mins += 5;
						for ($fid = 0; $fid < $nooffixtures; $fid++) {
							Insert_energy_consumption($capture_date, $count, $hour, $fixtures[$fid]);
							if ($fid != $nooffixtures - 1) {
								$count++;
								echo ", ";
							}
						}
					} 
					if ($mins == 60) {
						$hour++;
						echo ";\n";
						$mins = 0;
						echo "select aggregateHourlyEnergyConsumption('" . $capture_date . "');" . "\n";
						addHeader();
					}else {
						if ($min < 1440)
							echo ", ";
						else 
							echo ";\n";
					}
				}
				$capture_day = date("Y-m-d H:i:s", mktime(0, 0, 0, $month, $day, $GLOBALS["year"]));
				echo "select aggregateDailyEnergyConsumption('" . $capture_day . "');" . "\n";
			}
		}
		//echo "Number of records: " . $count . "\n";
	}

	
	function MonthDays($someMonth, $someYear)
	{
		return date("t", strtotime($someYear . "-" . $someMonth . "-01"));
	}

	function addHeader()
	{
		echo "insert into energy_consumption (min_temperature, max_temperature, avg_temperature, light_on_seconds, light_min_level, light_max_level, light_avg_level, light_on, light_off, power_used, occ_in, occ_out, occ_count, dim_percentage, dim_offset, bright_percentage, bright_offset, capture_at, fixture_id, cost, price, base_power_used, base_cost, saved_power_used, saved_cost, occ_saving, tuneup_saving, ambient_saving, manual_saving, id) values ";
	}

	function Insert_energy_consumption($capture_date, $uid, $hour, $fid)
	{
		$bulbWattage = 32;
		$noOfLamps = 2;
		$maxvolts = 100;
		$usedvolts = rand(95, $maxvolts);

		$fixture_id = $fid; //$GLOBALS["fixtureid"];
		if ($hour > 6 && $hour < 9) {
			$price = 0.12;
			$usedvolts = rand(10, 25);
		} else if ($hour > 9 && $hour < 15) {
			$price = 0.15;
			$usedvolts = rand(95, 100);
		} else if ($hour > 15 && $hour < 20) {
			$price = 0.20;
			$usedvolts = rand(75, 90);
		} else if ($hour > 20 && $hour < 23) { 
			$price = 0.25; 
			$usedvolts = rand(50, 75);
		} else {
			$price = 0.10;
			$usedvolts = rand(0, 5);
		}

		$power_used = getAvgPowerFromVolts($usedvolts, $noOfLamps);
		$energy_consumed = $power_used * 5 / 60;	
		$cost = $energy_consumed * $price / 1000;
		$base_power_used = $bulbWattage * $noOfLamps;
		$basecost = ($base_power_used * 5 * $price) / (1000 * 60);
		$saved_power_used = $base_power_used - $power_used;
		$saved_cost = $basecost - $cost;
		$occ_saving = 0;
		$tuneup_saving = 0;
		$ambinet_saving = 0;
		$manual_saving = 0;
		$saving_type = rand(0,3);

		if ($saving_type == 0)
			$occ_saving = $saved_power_used;
		else if ($saving_type == 1)
			$tuneup_saving = $saved_power_used;
		else if ($saving_type == 2)
			$ambinet_saving = $saved_power_used;
		else if ($saving_type == 3)
			$manual_saving = $saved_power_used;

		echo "(" . rand(1,100) . "," . rand(1, 100) . "," .  rand(1,100) . "," .  rand(1, 60) . "," .  rand(1, 100) . "," .  rand(1, 100) . "," .  rand(1, 100) . "," .  rand(1, 60) . "," .  rand(1, 60) . "," .  $power_used . "," . rand(1, 100) . "," .  rand(1, 100) . "," .  rand(1, 100) . "," .  rand(1, 100) . "," .  rand(1, 100) . "," .  rand(1, 100) . "," .  rand(1, 100) . ", '" .  $capture_date . "', " .  $fixture_id . "," .  $cost . "," .  $price . "," .  $base_power_used . "," .  $basecost . "," .  $saved_power_used . "," .  $saved_cost . "," .  $occ_saving . "," .  $tuneup_saving . "," .  $ambinet_saving . "," .  $manual_saving . "," . $uid . ")";
	}

	function getAvgPowerFromVolts($volts, $noOfLamps) 
	{
		$voltPowerMap = array(10 => 100, 9.5 => 100, 8.5 => 100, 8 => 96.4, 7.5 => 94.5, 7 => 89.1, 6.5 => 81.8, 6 => 76.4, 5.5 => 72.7, 5 => 63.6, 4.5 => 58.2, 4 => 52.7, 3.5 => 47.3, 3 => 38.2, 2.5 => 32.7, 2 => 27.3, 1.5 => 25.5, 1 => 23.6, 0.5 => 23.6, 0 => 23.6);
		$rem = $volts % 5;
		$bulbWattage = 32;

		if ($rem > 2) {
			$volts += (5 - $rem);
		} else {
			$volts -= $rem;
		}
		$ballastFactor = 1; 

		$voltsPowerFactor = $voltPowerMap[($volts / 10)];
		return ($ballastFactor * $bulbWattage * $noOfLamps * $voltsPowerFactor) / 100;	
	}

	function getCMD($argc, $argv) 
	{
		if ($argc < 6) {
			help();
		} else {
			for ($i = 0; $i < $argc; $i++) {
				//echo "$i => $argv[$i]\n";
				if ($argv[$i] == "--year") {
					$GLOBALS["year"] = $argv[$i + 1];
					$i++;
				} else if ($argv[$i] == "--months") {
					$GLOBALS["number_of_months"] = $argv[$i + 1];
					if (is_numeric($GLOBALS["number_of_months"]) == false || $GLOBALS["number_of_months"] < 0 || $GLOBALS["number_of_months"] > 12 ) {
						echo "Number of Months can only be from 1 - 12\n";
						break;
					}
					$i++;
				} else if ($argv[$i] == "--fixture") {
					$GLOBALS["fixture"] = $argv[$i + 1];
					$i++;
				}
			}	
		}
	}

	function help()
	{
		echo "Usage: php ec.php --year 2010 --months [1-12] --fixture 1" . "\n\n"; 
		echo "php  PHP interface executable, followed by the name of the script" . "\n";
		echo "--year	Data to be generated for the year" . "\n";
		echo "--months	Data to be generated for the months " . "\n";
		echo "--fixture	Fixture ID for which the data is generated" . "\n";
	}
?>

