<?php
/********************************************************************************/
// php sql/insertballast.php --bn="Others" --iv="100-277" --lt=LED --ln=4 \
// --bf=1 --vpm=1 --wa=59 --mf="Others"
/********************************************************************************/
$psql_cmd = "psql -U postgres ems -x -c ";
$args_arr = parseArgs($argv);
$item_num = $args_arr["in"];
$ballast_name = $args_arr["bn"];
$input_voltage = $args_arr["iv"];
$lamp_type = $args_arr["lt"];
$lamp_num = $args_arr["ln"];
$ballast_factor = $args_arr["bf"];
$volt_power_map_id = $args_arr["vpm"];
$wattage = $args_arr["wa"];
$manufacturer = $args_arr["mf"];

/*
echo "item_num =" .  $args_arr["in"] . "\r\n";
echo "ballast_name =" .  $args_arr["bn"] . "\r\n";
echo "input_voltage =" .  $args_arr["iv"] . "\r\n";
echo "lamp_type =" .  $args_arr["lt"] . "\r\n";
echo "lamp_num =" .  $args_arr["ln"] . "\r\n";
echo "ballast_factor =" .  $args_arr["bf"] . "\r\n";
echo "volt_power_map_id =" .  $args_arr["vpm"] . "\r\n";
echo "wattage =" .  $args_arr["wa"] . "\r\n";
echo "manufacturer =" .  $args_arr["mf"] . "\r\n";
*/

if ($ballast_name != null && $input_voltage != null && $lamp_type != null & $lamp_num != null && $ballast_factor != null && $wattage != null && $manufacturer != null) {
	if ($volt_power_map_id == null)
		$volt_power_map_id = 1;
	if ($item_num == null)
		$item_num = "(select MAX(id)+1 from ballasts)";
	addBallasts($item_num, $ballast_name, $input_voltage, $lamp_type, $lamp_num, $ballast_factor, $volt_power_map_id, $wattage, $manufacturer);
} else {
	showHelp();
}

function addBallasts($item_num, $ballast_name, $input_voltage, $lamp_type, $lamp_num, $ballast_factor, $volt_power_map_id, $wattage, $manufacturer) {
	$insert_ballast_sql = "\"INSERT into ballasts (id, item_num, ballast_name, input_voltage, lamp_type, lamp_num, ballast_factor, volt_power_map_id, wattage, manufacturer) values ((select MAX(id)+1 from ballasts), $item_num, '$ballast_name', '$input_voltage', '$lamp_type', $lamp_num, $ballast_factor, $volt_power_map_id, $wattage, '$manufacturer')\"";
	$result = exec($GLOBALS["psql_cmd"] . $insert_ballast_sql);
	$index = strpos($result, "INSERT 0 1");
	if ($index === false) {
		echo "FAILED: Ballast adding failed!";
	} else {
		echo "SUCCESS: Ballast added successfully...";
	}
	//echo $insert_ballast_sql . "\r\n";
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
	$help = $help . "php insertBallasts.php [--in=] --bn= --iv= --lt= --ln= --bf= [--vpm=1] --wa= --mf=\r\n";
	$help = $help . "--in:	Optional, item_num\r\n";
	$help = $help . "--bn:	Ballast name\r\n";
	$help = $help . "--iv:	Input voltage\r\n";
	$help = $help . "--lt:	Lamp type\r\n";
	$help = $help . "--ln:	Lamp Num\r\n";
	$help = $help . "--bf:	Ballast Factor\r\n";
	$help = $help . "--vpm:	Optional, Volt Power Map Id\r\n";
	$help = $help . "--wa:	Wattage\r\n";
	$help = $help . "--mf:	Manufacturer\r\n";
	print($help);
}
?>
