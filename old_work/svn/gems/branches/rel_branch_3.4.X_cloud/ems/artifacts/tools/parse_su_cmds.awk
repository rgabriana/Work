BEGIN {
	targets=1
	snap_location=0
	snaps_address=""
	ST_TIME=startTime
	ET_TIME=endTime
	print ST_TIME " " ET_TIME
}
{
	if ($3 == "INFO") {
		logtime = $1 " " $2
		# NOTE ST_TIME < ET_TIME
		strContinue="TRUE" 
		if (strContinue == "TRUE" || (logtime >= ST_TIME && logtime < ET_TIME)) {
			#Receiving packet parser for incoming packets for SU
			if ($7 == "received" &&  $10 == 65 && $11 == 73 && $14 == 2) {
				#print ">>" $0
				if ($30 == "bb") {
					print "[" $1 " " $2 "] [" $5 " " $6 "] Received [" $32$33$34$35 "] [" $30 " " $31 "] [" $27 ":" $28 ":" $29 "]"
					ACK_PKTS[$30]++
				}
				else {
					print "[" $1 " " $2 "] [" $5 " " $6 "] Received [" $23$24$25$26 "] [" $30 "] [" $27 ":" $28 ":" $29 "]"
					RECV_PKTS[$30]++
				}
			} else {
				#Sending packet parser for outgoing packets for SU
				if ($7 == "sending" &&  $10 == 65 && $11 == 73 && $14 == 18) {
					if ($27 == "5a") {
						snaps_address=""
						targets=$28*3
						snap_location=29
						if(targets > 0) {
							#print ">>" $0
							for (i = 1; i <= targets; i+=1) {
								snaps_address = snaps_address "" $snap_location 
								snap_location+=1
								if (i % 3 == 0) {
									if (i < targets)
										snaps_address = snaps_address ","
								}else {
									snaps_address = snaps_address ":"
								}
							}
							print "[" $1 " " $2 "] [" $5 " " $6 "] Sending [" $23$24$25$26 "] [" $snap_location "] [" snaps_address "]"
							SENT_PKTS[$snap_location]++
						}
					}else {
						print "[" $1 " " $2 "] [" $5 " " $6 "] Broadcast [" $23$24$25$26 "] [" $27 "]"
						BROADCAST_PKTS[$27]++
					}
				}
			}
		}
	}
}
END {
	for (msg in RECV_PKTS) {
		print msg  " received " RECV_PKTS[msg] " times."
	}
	for (msg in ACK_PKTS) {
		print msg  " received " ACK_PKTS[msg] " times."
	}
	for (msg in SENT_PKTS) {
		print msg  " sent " SENT_PKTS[msg] " times."
	}
	for (msg in BROADCAST_PKTS) {
		print msg  " sent " BROADCAST_PKTS[msg] " times."
	}
}
