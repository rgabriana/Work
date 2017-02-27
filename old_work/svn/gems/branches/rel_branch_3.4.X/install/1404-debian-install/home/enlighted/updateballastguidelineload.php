<?php
/********************************************************************************/
// php sql/insertballast.php --bn="Others" --iv="100-277" --lt=LED --ln=4 \
// --bf=1 --vpm=1 --wa=59 --mf="Others"
/********************************************************************************/
$psql_cmd = "psql -U postgres ems -x -c ";
$args_arr = parseArgs($argv);
$display_label = $args_arr["dl"];
$guideline_load = $args_arr["gl"];

echo "display label =" .  $args_arr["dl"] . "\r\n";
echo "guideline load =" .  $args_arr["gl"] . "\r\n";

if ($display_label != null && $guideline_load != null) {
	updateBallasts($display_label, $guideline_load);
} else {
	showHelp();
}

function updateBallasts($display_label, $guideline_load) {
	$update_ballast_sql = "\"UPDATE ballasts SET baseline_load = $guideline_load WHERE display_label = '$display_label'\"";
	$result = exec($GLOBALS["psql_cmd"] . $update_ballast_sql);
	echo $result;
	$index = strpos($result, "UPDATE 0");
	if ($index === 0) {
		echo "\r\nFAILED: Ballast update failed!\r\n";
	        echo $update_ballast_sql . "\r\n";
	} else {
		echo "\r\nSUCCESS: Ballast updated successfully...\r\n";
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
	$help = $help . "php updateBallasts.php --dl= --gl= \r\n";
	$help = $help . "--dl:	Display Label\r\n";
	$help = $help . "--gl:	Guideline Load\r\n";
	print($help);
}
?>
