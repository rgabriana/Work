<?php
	////////////////////////////////////////////////////////////////////////////////
	// Globals
	////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////
	// Logic
	getSppaEnabledEM();
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
		$query = "SELECT id, customer_id, replica_server_id, database_name, cert_start_date, cert_end_date from em_instance where sppa_enabled = 't'";
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

		$modified = 1;
		if ($modified == 1) {
			syncCertificates();
		}
	}


	function verifyCert($emObj) {
		logMsg("Start verifyCert");
		$output = exec("/etc/enlighted/CA/scripts/verifyCert.sh " . $emObj->dbname); //changed to absolute path -Rolando 05/06/16
		if (strstr($output, ": OK")) {
			return true;
		}
		return false;
	}

	function getCertDate($dbhandle, $emObj) {
		logMsg("start getCertDate");
		exec("/etc/enlighted/CA/scripts/getCertDates.sh " . $emObj->dbname, $output);  // changed to absolute path -Rolando 05/06/16
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
		logMsg("start generateClientCertificate");
		exec("/etc/enlighted/CA/scripts/generateClientcert.sh " . $emObj->dbname . " >> /var/log/enlighted/generateClientCert.log 2>&1", $output);  // changed to absolute path -Rolando  05/06/16
		if (verifyCert($emObj)) {
			return true;
		}
		return false;
	}

	function syncCertificates() {
		logMsg("Starting syncCertificates");
		chdir("/etc/enlighted/");

                $targetHostList = array(
//		    "192.168.0.228" => 'replica-test'
                );

                foreach (array_keys($targetHostList) as $host) {
                  unset($output);
                  logMsg(exec("/usr/bin/rsync -avcz --delete-after /etc/enlighted/CA/ssl $host:/etc/enlighted/CA", $output, $retVal) . "\n");
                  if (sizeof($output) > 4) {
                    logMsg(exec("/usr/bin/ssh $host \"/usr/bin/sudo /usr/sbin/service apache2 restart\"") . "\n");
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
