<?php
    
    //Run: php /home/enlighted/utils/generateSPPACerts.php >> /home/enlighted/utils/gencerts.log 2>&1
	
	// Logic
    $pid = getmypid();
    logMsg("current process pid = " . $pid);
    $sk = exec(" /home/enlighted/utils/skipOrKillProcess.sh " . $pid);
    logMsg("skipkill output = " . $sk);
    if ($sk != "0") {
	    getSppaEnabledEM();
    }

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
		$query = "SELECT id, customer_id, replica_server_id, database_name, cert_start_date, cert_end_date from em_instance where sppa_enabled = 't'";
		$result = pg_exec($dbhandle, $query);
		
		// Fetch number of rows
		$numRows = pg_numrows($result); 
		logMsg(" " . $numRows . " Row" . ($numRows == 1 ? "" : "s") . " Returned"); 

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
		    $count++;
			logMsg($count . " Processing " . $emObj->dbname);
			if (file_exists($CERTPATH . $emObj->dbname . ".pfx")) {
				logMsg("\t" . $CERTPATH . $emObj->dbname . ".pfx" .  " exists");
			} else {
				logMsg("\t" . $CERTPATH . $emObj->dbname . ".pfx" .  " does not exists, creating...");
				if (generateClientCertificate($emObj)) {
					getCertDate($dbhandle, $emObj);
					$modified = 1;
				}
			}
		}
		logMsg("Total: " . $count);

		//close the connection at the end, we are using the handle to update the certificate expiry times.
		pg_close($dbhandle);
		
        syncCertificates($modified);

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
		logMsg("\t\t" . $startDate . " " . $endDate);
		if ($emObj->certstartdate == "" && $emObj->certenddate == "") {
			$updatequery = "UPDATE em_instance set cert_start_date='" . $startDate . "', cert_end_date='" . $endDate . "' where id=" . $emObj->id;
			$result = pg_exec($dbhandle, $updatequery);
			$cmdtuples = pg_affected_rows($result);
			logMsg($cmdtuples . " row are affected.");
		}
	}

	function generateClientCertificate($emObj) {
		exec("./generateClientcert.sh " . $emObj->dbname . " > /dev/null 2>&1", $output);
		if (verifyCert($emObj)) {
			return true;
		}
		return false;
	}

	function syncCertificates($modified) {
        logMsg("sync certificates to replica servers. modified=". $modified);
		chdir("/etc/enlighted/");

        include 'replicaservers.php';

		foreach ($replicaServersList as $replicaServer => $details) {
			#logMsg($replicaServer . " " . $details[0] . " " . $details[1]);
			logMsg("Syncing CA repo to " . $replicaServer);
			$output = exec("sshpass -p '$details[1]' rsync -aiHx -e \"ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 \" CA/* " . $details[0] . "@" . $replicaServer . ":/etc/enlighted/CA/");			
			logMsg("sync output for replica " . $replicaServer . "::" . $output);
            if( $modified == 1 or !empty($output) ) {
			    $connection = ssh2_connect($replicaServer, 22); 
			    $connstatus = @ssh2_auth_password($connection, $details[0], $details[1]);
                var_dump($connstatus);
                if ($connstatus === true) {
			        $stream = ssh2_exec($connection, 'echo ' . $details[1] . ' | sudo -S /etc/init.d/apache2 restart');
			        stream_set_blocking($stream, true);
			        logMsg("Output: " . stream_get_contents($stream));
			        fclose($stream);
			        ssh2_exec($connection, 'exit;');
                }
			    unset($connection);
            }
		}
	}

	function logMsg($message) {
		echo date("Y-m-d H:i:s") . " " . $message . "\n";
	}

	function logErr($message) {
		echo date("Y-m-d H:i:s") . " " . $message . "\n";
	}
?>
