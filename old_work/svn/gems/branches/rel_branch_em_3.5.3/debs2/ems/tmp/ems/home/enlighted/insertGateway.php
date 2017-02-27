<?php

$env_arr  = parse_ini_file('/etc/environment');

$mac_address;
$ip_address;
$subnet_mask;
$device_type = 1;
$comm_type = 3;
$empty_str = "";
$network_id = "6854";
$channel_no = "4";
$psql_cmd = "psql -U postgres ems -x -c ";

$args_arr = parseArgs($argv);
if (sizeof($args_arr) != 3) {
	showHelp();
} else {
	$ip_address = $args_arr["ip"];
	$mac_address = $args_arr["mac"];
	$subnet_mask = $args_arr["mask"];
	if (($ip_address == $empty_str) or ($mac_address == $empty_str) or ($subnet_mask == $empty_str)) {
		showHelp();
	} else {
        $emsmode = exec("head -n 1 ".$GLOBALS['env_arr']['ENL_APP_HOME']."/Enlighted/emsmode");
        if (strpos($emsmode, 'NORMAL') !== false) {
            insertGateway($ip_address, $mac_address, $subnet_mask, $device_type, $comm_type, "DISCOVERED", $network_id, $channel_no);
	    	updateGateway($ip_address, $mac_address);
    		updateInventoryDevice($ip_address, $mac_address);
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
	$help = $help . "php insertGateway.php --ip=XXX.XXX.XXX.XXX --mac=XXXXXXXXXXXX --mask=XXX.XXX.XXX.XXX\r\n";
	print($help);
}

function updateInventoryDevice($ipAddress, $macAddress) {

  $update_gw_sql = "\"UPDATE inventorydevice SET ip_address = ";
  $update_gw_sql = $update_gw_sql . "'" . $ipAddress . "' ";
  $update_gw_sql = $update_gw_sql . "WHERE mac_address = ";
  $update_gw_sql = $update_gw_sql . "'" . $macAddress . "'\"";

  $result = exec($GLOBALS["psql_cmd"] . $update_gw_sql);

}

function getShelloutput($cmd){
	$out = shell_exec($cmd);
	if ( is_null($out) ){
		return null;
	}else{
		return trim($out);
	}
}

function updateGateway($ipAddress, $macAddress) {

  $select_gw_sql = "\"select g.ip_address from device d, gateway g where g.id = d.id and d.mac_address ='" .$macAddress. "'\"" ;
  $cmd = "psql -U postgres ems  -q -t -c" . $select_gw_sql;
  echo "Command is " .$cmd. " \n";
  $result_select = getShelloutput($cmd);
  $ip_prev="";
  if(!empty($result_select) && !is_null($result_select)){
     $ip_prev=$result_select;
  } 
  echo "Previous ip is " .$ip_prev. "Updated IP is " .$ipAddress. "\n";
  
  $update_gw_sql = "\"UPDATE gateway SET ip_address = ";
  $update_gw_sql = $update_gw_sql . "'" . $ipAddress . "' ";
  $update_gw_sql = $update_gw_sql . "WHERE id = (select id from device where mac_address = ";
  $update_gw_sql = $update_gw_sql . "'" . $macAddress . "')\"";

  $result = exec($GLOBALS["psql_cmd"] . $update_gw_sql);
  echo "update result - " . $result;
  if( $ip_prev !== $ipAddress ){
	    echo "calling webservice to update gateway ip\n"       ;
        $link = "sh ".$GLOBALS['env_arr']['ENL_APP_HOME']."/webapps/ems/adminscripts/callusingCurl.sh admin  -H \"Content-Type:application/xml\" -X POST https://localhost/ems/api/org/gateway/updateGatewayIP/macaddr/$macAddress/ipaddr/$ipAddress";
        echo "Calling link is " .$link. " \n";
        $fp = popen ( $link, "r");

        $rs = array();
        while ($rec = fgets($fp)){
             $rs[] = trim($rec);
        }
        $result_ws=500;
        foreach($rs as $val){
                $result_ws = $val;
        }
        echo "Result Code is ". $result_ws."\n";
        if( 200 == $result_ws){
                echo "update result - 1";
        }else{
                echo "update result - 0";
        }
  }

}

function insertGateway($ipAddress, $macAddress, $subnetMask, $deviceType, $commType, $status, $network_id, $channel_no) {
	$insert_gw_sql = "\"INSERT INTO inventorydevice (ip_address, mac_address, subnet_mask, snap_address, device_type, comm_type, status, discovered_time, network_id, channel, id) values (";
	$insert_gw_sql = $insert_gw_sql . "'" . $ipAddress . "', ";
	$insert_gw_sql = $insert_gw_sql . "'" . $macAddress . "', ";
	$insert_gw_sql = $insert_gw_sql . "'" . $subnetMask . "', ";
	$insert_gw_sql = $insert_gw_sql . "'" . getSNAPAddrFromMAC($macAddress) . "', ";
	$insert_gw_sql = $insert_gw_sql . $deviceType . ", ";
	$insert_gw_sql = $insert_gw_sql . $commType . ", ";
	$insert_gw_sql = $insert_gw_sql . "'" . $status. "', ";
	$insert_gw_sql = $insert_gw_sql . "CURRENT_TIMESTAMP" . ", ";
	$insert_gw_sql = $insert_gw_sql . "'" . $network_id. "', ";
	$insert_gw_sql = $insert_gw_sql . $channel_no . ", ";
	$insert_gw_sql = $insert_gw_sql . "nextval('inventory_device_seq'))\"";
	$result = exec($GLOBALS["psql_cmd"] . $insert_gw_sql);
	$index = strpos($result, "INSERT 0 1");
	if ($index === false) {
		echo "FAILED: Gateway adding failed!";
	} else {
		echo "SUCCESS: Gateway added successfully...";
	}
}

function getSNAPAddrFromMAC($macAddress) {
	$snapAddr = "0:0:1";
	$snapAddr = substr($macAddress, 9);
	return $snapAddr;
}

?>


