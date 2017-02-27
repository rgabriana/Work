<?php
/********************************************************************************/
// php sql/insertbubl.php --bn="Others" --iv="100-277" --lt=LED --ln=4 \
// --bf=1 --vpm=1 --wa=59 --mf="Others"
/********************************************************************************/
$psql_cmd = "psql -U postgres ems -x -c ";
$args_arr = parseArgs($argv);
$type = $args_arr["type"];
$bulb_name = $args_arr["name"];
$initial_lumens = $args_arr["init_lum"];
$design_lumens = $args_arr["des_lum"];
$energy = $args_arr["energy"];
$life_ins_start = $args_arr["life_ins_st"];
$life_prog_start = $args_arr["life_pro_st"];
$diameter = $args_arr["diam"];
$manufacturer = $args_arr["mf"];
$length = $args_arr["len"];
$cri = $args_arr["cri"];
$color_temp = $args_arr["temp"];

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

if ($bulb_name != null && $type != null && $energy != null & $life_ins_start != null && $manufacturer != null&& $life_prog_start != null) {
	addBulb($bulb_name, $type, $energy, $life_ins_start, $manufacturer, $life_prog_start, $length, $diameter, $cri, $color_temp, $initial_lumens, $design_lumens);
} else {
	showHelp();
}

function addBulb($bulb_name,  $type, $energy, $life_ins_start, $manufacturer, $life_prog_start, $length, $diameter, $cri, $color_temp, $initial_lumens, $design_lumens) {
	$insert_bulb_sql = "\"INSERT into bulbs (id, manufacturer, bulb_name, type, energy, life_ins_start, life_prog_start";
	if($initial_lumens != null) {
		$insert_bulb_sql = $insert_bulb_sql .  ", initial_lumens";
	}
	if($design_lumens != null) {
		$insert_bulb_sql = $insert_bulb_sql .  ", design_lumens";
	}	
	if($diameter != null) {
		$insert_bulb_sql = $insert_bulb_sql .  ", diameter";
	}
	if($length != null) {
		$insert_bulb_sql = $insert_bulb_sql .  ", length";
	}
	if($cri != null) {
		$insert_bulb_sql = $insert_bulb_sql .  ", cri";
	}
	if($color_temp != null) {
		$insert_bulb_sql = $insert_bulb_sql .  ", color_temp";
	}
 	$insert_bulb_sql = $insert_bulb_sql . ") values ((select MAX(id)+1 from bulbs), '$manufacturer', '$bulb_name', '$type', $energy, $life_ins_start, $life_prog_start";

	if($initial_lumens != null) {
		$insert_bulb_sql = $insert_bulb_sql .  ", $initial_lumens";
	}
	if($design_lumens != null) {
		$insert_bulb_sql = $insert_bulb_sql .  ", $design_lumens";
	}	
	if($diameter != null) {
		$insert_bulb_sql = $insert_bulb_sql .  ", $diameter";
	}
	if($length != null) {
		$insert_bulb_sql = $insert_bulb_sql .  ", $length";
	}
	if($cri != null) {
		$insert_bulb_sql = $insert_bulb_sql .  ", $cri";
	}
	if($color_temp != null) {
		$insert_bulb_sql = $insert_bulb_sql . ", $color_temp";
	}
	$insert_bulb_sql = $insert_bulb_sql .  ")\"";
	print "sql -- " . $insert_bulb_sql;
	$result = exec($GLOBALS["psql_cmd"] . $insert_bulb_sql);
	$index = strpos($result, "INSERT 0 1");
	if ($index === false) {
		echo "FAILED: Bulb adding failed!";
	} else {
		echo "SUCCESS: Bulb added successfully...";
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
	$help = $help . "php insertbulb.php --name= --type= --mf= --life_ins_st= --life_pro_start= --energy= [--init_lum=] [--des_lum=] [--diam=] [--len=] [--cri=] [--temp=] \r\n";
	$help = $help . "--name: 	Bulb name \r\n";
	$help = $help . "--type: 	Bulb type \r\n";
	$help = $help . "--mf:	 	Manufacturer \r\n";
	$help = $help . "--life_ins_st:	Life with instant start ballast \r\n";
	$help = $help . "--life_pro_st:	Life with program start ballast \r\n";
	$help = $help . "--energy: 	Energy used \r\n";
	$help = $help . "--init_lum: 	Initial Lumen(optional)\r\n";
	$help = $help . "--des_lum: 	Design Lumen(optional)\r\n";
	$help = $help . "--diam: 	diameter(optional)\r\n";
	$help = $help . "--len:		length(optional)\r\n";
	$help = $help . "--cri:		CRI(optional)\r\n";
	$help = $help . "--temp: 	Color temperature(optional)\r\n";
	print($help);
}
?>
