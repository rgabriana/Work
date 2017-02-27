<?php
	////////////////////////////////////////////////////////////////////////////////	
	// Globals
	////////////////////////////////////////////////////////////////////////////////	
	$cookiefile = '/tmp/curl-session';
	$VERBOSE = 0;
	$USER = "admin";
	$PASS = "";
	//ZONE1 => Chetan row, ZONE2 => Sampath row, ZONE3 => Yogesh, ZONE4 => sharad row, ZONE5 => Router row
	$ZONE_LIST = array ("ZONE1" => "2074, 2082, 2086", 
				"ZONE2" => "2084, 2083, 2078",
				"ZONE3" => "2076, 2081, 2085",
				"ZONE4" => "9, 2080, 2079",
				"ZONE5" => "2075"
			);

	$START_AC_PARAMS_BY_ZONE = array (
				"ZONE1" => array("DEVICEID", "FUNCTIONID"),
				"ZONE2" => array("DEVICEID", "FUNCTIONID"),
				"ZONE3" => array("DEVICEID", "FUNCTIONID"),
				"ZONE4" => array("DEVICEID", "FUNCTIONID"),
				"ZONE5" => array("DEVICEID", "FUNCTIONID")
				);

	$STOP_AC_PARAMS_BY_ZONE = array (
				"ZONE1" => array("DEVICEID", "FUNCTIONID"),
				"ZONE2" => array("DEVICEID", "FUNCTIONID"),
				"ZONE3" => array("DEVICEID", "FUNCTIONID"),
				"ZONE4" => array("DEVICEID", "FUNCTIONID"),
				"ZONE5" => array("DEVICEID", "FUNCTIONID")
				);
	$DELTA_IN_MINS = 3;
	
	$TIME_DIFF_IN_SECS = abs($DELTA_IN_MINS * 60);

	////////////////////////////////////////////////////////////////////////////////	
	logMsg("START\n");
	login();
	getRealTimeStats();
	logMsg("Waiting for realtime stats...\n");
	sleep(10);
	getOccData();
	logout();
	logMsg("END\n\n");
	////////////////////////////////////////////////////////////////////////////////	

	////////////////////////////////////////////////////////////////////////////////	
	// Functions...
	////////////////////////////////////////////////////////////////////////////////	
	function login() {
		logMsg("Login...\n");
		$url = 'https://localhost/ems/j_spring_security_check';
		$params = "j_username=" . $GLOBALS["USER"] . "&j_password=" . $GLOBALS["PASS"];

		$ch = curl_init();
		curl_setopt($ch, CURLOPT_VERBOSE, $GLOBALS["VERBOSE"]);
		curl_setopt($ch, CURLOPT_URL, $url);
		curl_setopt($ch, CURLOPT_COOKIEFILE, $GLOBALS["cookiefile"]);
		curl_setopt($ch, CURLOPT_COOKIEJAR, $GLOBALS["cookiefile"]);
		curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $params);
		$response = curl_exec($ch);
		$http_status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
		curl_close($ch);
		logMsg($http_status . " " . $response . "\n\n");
	}

	function logout() {
		logMsg("Logout...\n");
		$url = 'https://localhost/ems/j_spring_security_logout';
		$params = array(
		);        

		$ch = curl_init();
		curl_setopt($ch, CURLOPT_VERBOSE, $GLOBALS["VERBOSE"]);
		curl_setopt($ch, CURLOPT_URL, $url);
		curl_setopt($ch, CURLOPT_COOKIEFILE, $GLOBALS["cookiefile"]);
		curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $params);
		$response = curl_exec($ch);
		$http_status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
		curl_close($ch);
		logMsg($http_status . " " . $response . "\n\n");
		unlink($GLOBALS["cookiefile"]);
	}

	function getRealTimeStats() {
		logMsg("Get Realtime...\n");
		$url = 'https://localhost/ems/services/org/fixture/op/realtime';
		$params = "<fixtures>";
		foreach ($GLOBALS["ZONE_LIST"] as $zone => $fxList) {
			$fxArr = explode(",", $fxList);
			for ($count = 0; $count < sizeof($fxArr); $count++) {
				$params = $params . "<fixture>";
				$params = $params . "<id>" . trim($fxArr[$count]) . "</id>";
				$params = $params . "</fixture>";
			}
		}
		$params = $params . "</fixtures>";

		$ch = curl_init();
		curl_setopt($ch, CURLOPT_VERBOSE, $GLOBALS["VERBOSE"]);
		curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/xml'));
		curl_setopt($ch, CURLOPT_URL, $url);
		curl_setopt($ch, CURLOPT_COOKIEFILE, $GLOBALS["cookiefile"]);
		curl_setopt($ch, CURLOPT_COOKIEJAR, $GLOBALS["cookiefile"]);
		curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $params);
		$response = curl_exec($ch);
		$http_status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
		curl_close($ch);
		logMsg($http_status . " " . $response . "\n\n");
	}
	
	function getOccData() {
		logMsg("Get Occdata...\n");
		foreach ($GLOBALS["ZONE_LIST"] as $zone => $fxList) {
			$NoOfFxInZone = count(split(",", $fxList));
			// Fetch the count of fixture in the zone whose last occupancy seen is less than the delta and connectivity is recent.
			$lastconnectivityquery = "select count(id) from fixture where id in (" . $fxList . ") and last_connectivity_at > CURRENT_TIMESTAMP - interval '" . $GLOBALS["DELTA_IN_MINS"] . " minutes'";
			$lastconnectivityCount = exec("psql -U postgres ems -x -c \"". $lastconnectivityquery . "\" | awk 'FNR == 2 {print $3}'");

			if ($lastconnectivityCount > 0) {
				$query = "select count(id) from fixture where id in (" . $fxList . ") and last_occupancy_seen <= " . $GLOBALS["TIME_DIFF_IN_SECS"] . " and last_connectivity_at > CURRENT_TIMESTAMP - interval '" . $GLOBALS["DELTA_IN_MINS"] . " minutes'";

				$zoneFxCount = exec("psql -U postgres ems -x -c \"". $query . "\" | awk 'FNR == 2 {print $3}'");
				if ($zoneFxCount != 0) {
					$message = $zone . ": Start AC";
					logMsg($message . "\n");
					startAC($zone);
				}else {
					$message = $zone . ": Stop AC";
					logMsg($message . "\n");
					stopAC($zone);
				}
			}else {
				logMsg($zone . " Realtime Not available\n");
			}
		}
	}

	function startAC($zone) {
		$ZONE_PARMS = $GLOBALS["START_AC_PARAMS_BY_ZONE"];
		$DEVICEID = $ZONE_PARMS[$zone][0];
		$FUNCTIONID = $ZONE_PARMS[$zone][1];
		//$output = exec("/var/lib/tomcat6/Enlighted/modbus_app -o " . $DEVICEID . " "  . $FUNCTIONID);
		//logMsg($output . "\n");
	}

	function stopAC($zone) {
		$ZONE_PARMS = $GLOBALS["STOP_AC_PARAMS_BY_ZONE"];
		$DEVICEID = $ZONE_PARMS[$zone][0];
		$FUNCTIONID = $ZONE_PARMS[$zone][1];
		//$output = exec("/var/lib/tomcat6/Enlighted/modbus_app -o " . $DEVICEID . " "  . $FUNCTIONID);
		//logMsg($output . "\n");
	}


	function logMsg($message) {
		echo date("Y-m-d H:i:s") . " " . $message;
	}
?>
