<?php
	////////////////////////////////////////////////////////////////////////////////	
	// Globals
	////////////////////////////////////////////////////////////////////////////////	
	logMsg("Started ..");
	//var_dump($argc);
	if($argc <= 1){
		logErr(" NO argument passed to the script. Exiting...");
		die("Exiting From script..No Arguments passed\n");
	}
	$EM_ID_STR = $argv[1];
	//var_dump($argv);
	$EM_ID_ARR = explode(',', $EM_ID_STR);
	foreach ($EM_ID_ARR as $EM_ID) {
		if(!is_numeric($EM_ID)){
			logErr("EM IS IS NOT NUMERIC:" . $EM_ID);
			die(" Exiting the script...\n");
		}
	}

	logMsg('EM IDs passed are: ' . $EM_ID_STR);
	//die("FINAL: Exiting From script..\n");
	//logMsg(" You should not be here");
	////////////////////////////////////////////////////////////////////////////////	
	// Logic
	getSppaEnabledEM();
        ##Modify following line to include a proper backup server domain.
	#exec("rsync -azvv /etc/enlighted/CA/ enlighted@backup_server:/home/enlighted/rsyncs/CA");
	////////////////////////////////////////////////////////////////////////////////	

	////////////////////////////////////////////////////////////////////////////////	
	// Functions
	////////////////////////////////////////////////////////////////////////////////	
	function getSppaEnabledEM() 
	{
		// Working directory needs to be set as follows
		chdir("/etc/enlighted/CA/scripts/");

		// Initialize DB setup
		$DBServer = "localhost";
		$DBport = "5432";
		$dbUser = "postgres";
		$dbPass = "postgres";
		$DBname = "emscloud"; 

		//Connection to the database
		$dbhandle = pg_connect("host=" . $DBServer . " port=" . $DBport . " dbname=" . $DBname . " user=postgres password=postgres")
		or die("Couldn't connect to Server on $DBServer"); 

		// Execute query
		$query = "SELECT id, customer_id, replica_server_id, database_name, cert_start_date, cert_end_date from em_instance where sppa_enabled = 't' and id in(" . $GLOBALS['EM_ID_STR'] . ")";
		$result = pg_exec($dbhandle, $query);
		
		// Fetch number of rows
		$numRows = pg_numrows($result); 
		logMsg(" " . $numRows . " Row" . ($numRows == 1 ? "" : "s") . " Returned\n"); 

		// Iterate and Create objects for manipulation 
		$emList = array();
		while($row = pg_fetch_array($result))
		{
			$objEm = (object) array('id' => '', 'id' => '', 'customerid' => '', 'replicaserverid' => '', 'dbname' => '', 'certstartdate' => '', 'certenddate' => '');
			$objEm->id = $row["id"];
			$objEm->customerid = $row["customer_id"];
			$objEm->replicaserverid = $row["replica_server_id"];
			$objEm->dbname = $row["database_name"];
			$objEm->certstartdate = $row["cert_start_date"];
			$objEm->certenddate = $row["cert_end_date"];
			array_push($emList, $objEm);
		}

		// Check if certificate exists and verified and ensure to update expiry dates if not already present 
		$modified = 0;
		$count = 0;
		$CERTPATH = "/etc/enlighted/CA/ssl/pfx/";
		foreach($emList as $emObj) {
			logMsg($count . " Processing " . $emObj->dbname . "\n");
			if (file_exists($CERTPATH . $emObj->dbname . ".pfx")) {
				if(verifyCert($emObj)) {
					logMsg("\t" . $CERTPATH . $emObj->dbname . ".pfx" .  " exists and verified\n");
					getCertDate($dbhandle, $emObj);
				}else {
					logMsg("\t" . $CERTPATH . $emObj->dbname . ".pfx" .  " exists but could not verify\n");
				}
			} else {
				logMsg("\t" . $CERTPATH . $emObj->dbname . ".pfx" .  " does not exists, creating...\n");
				if (generateClientCertificate($emObj)) {
					getCertDate($dbhandle, $emObj);
					$modified = 1;
				}
			}
			$count++;
		}
		logMsg("Total: " . $count . "\n");

		//close the connection at the end, we are using the handle to update the certificate expiry times.
		pg_close($dbhandle);

		if ($modified == 1) {
			// Sync certificates on master server with replica servers
			// Change Working directory needs to be set as follows
			$MASTER_SERVER_PASS = "master_server_pass";
                        $output = exec("echo " . $MASTER_SERVER_PASS . " | sudo -S /etc/init.d/apache2 restart");
			logMsg($output . "\n");
			syncCertificates();
		}
	}


	function verifyCert($emObj) {
		$output = exec("./verifyCert.sh " . $emObj->dbname);
		if (strstr($output, ": OK")) {
			return true;
		}
		return false;
	}

	function getCertDate($dbhandle, $emObj) {
		exec("./getCertDates.sh " . $emObj->dbname, $output);
		$startDate = date('Y-m-d H:m:s', strtotime(preg_replace('/notBefore=/s', '', $output[0])));
		$endDate = date('Y-m-d H:m:s', strtotime(preg_replace('/notAfter=/s', '', $output[1])));
		logMsg("\t\t" . $startDate . " " . $endDate . "\n");
		if ($emObj->certstartdate == "" && $emObj->certenddate == "") {
			$updatequery = "UPDATE em_instance set cert_start_date='" . $startDate . "', cert_end_date='" . $endDate . "' where id=" . $emObj->id;
			$result = pg_exec($dbhandle, $updatequery);
			$cmdtuples = pg_affected_rows($result);
			logMsg($cmdtuples . " row are affected.\n");
		}
	}

	function generateClientCertificate($emObj) {
		exec("./generateClientcert.sh " . $emObj->dbname . " > /dev/null 2>&1", $output);
		if (verifyCert($emObj)) {
			return true;
		}
		return false;
	}

	function syncCertificates() {
		chdir("/etc/enlighted/");
		$replicaServersList = array(
			"replica_1" => array("enlighted", "pass"),
			"replica_2" => array("enlighted", "pass")
		);

		foreach ($replicaServersList as $replicaServer => $details) {
			logMsg($replicaServer . " " . $details[0] . " " . $details[1] . "\n");
			logMsg("Syncing CA repo to " . $replicaServer . "\n");
			$output = exec("sshpass -p '$details[1]' rsync -avHx  CA/* " . $details[0] . "@" . $replicaServer . ":/etc/enlighted/CA/");			
			logMsg($output . "\n");

			$connection = ssh2_connect($replicaServer, 22); 
			ssh2_auth_password($connection, $details[0], $details[1]);


			$stream = ssh2_exec($connection, 'echo $details[1] | sudo -S /etc/init.d/apache2 restart');
			//$stream = ssh2_exec($connection, 'echo ' . $details[1] . '| sudo -S ls -lh /');
			//$stream = ssh2_exec($connection, 'ls -lh /home/enlighted');
			$errorStream = ssh2_fetch_stream($stream, SSH2_STREAM_STDERR);

			// Enable blocking for both streams
			stream_set_blocking($errorStream, true);
			stream_set_blocking($stream, true);

			logMsg("Output: " . stream_get_contents($stream) . "\n");
			logErr("Error: " . stream_get_contents($errorStream) . "\n");

			fclose($errorStream);
			fclose($stream);

			ssh2_exec($connection, 'exit;');
			unset($connection);
		}
	}

	function logMsg($message) {
		echo date("Y-m-d H:i:s") . " " . $message;
		//error_log("INFO:" . " " . $message, 0);
	}

	function logErr($message) {
		echo date("Y-m-d H:i:s") . " " . $message;
		//error_log("ERROR:" . " " . $message, 0);
	}
?>

