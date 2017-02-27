BEGIN {
        ST_DAY=startDay
        ET_DAY=endDay
        ST_TOD=startTime
        ET_TOD=endTime
	ZONE=zone
        #print ST_DAY " " ST_TOD " " ET_DAY " " ET_TOD
	COUNT=0
}
{
	COUNT++
	#print $1
	split($1, dayA, " ")
	if (dayA[1] >= ST_DAY && dayA[1] <= ET_DAY) {
		if (dayA[2] >= ST_TOD && dayA[2] <= ET_TOD) {
			DAY_TOTAL[dayA[1]]++
			#print dayA[1]
			if ($2 > 1) {
				DAY_OCC[dayA[1]]++
			}
		}else {
			#print $1 " " $2
		}
	}else {
		#print "**" $1 " " $2
	}
}
END {
	PERIOD_OCC=0
	PERIOD_TOTAL=0
        for (day in DAY_OCC) {
		PERIOD_OCC+=DAY_OCC[day]
		PERIOD_TOTAL+=DAY_TOTAL[day]
                #print day " => " DAY_OCC[day] " / " DAY_TOTAL[day] " (" DAY_OCC[day] * 100 / DAY_TOTAL[day] "%)"
        }
	#print "Records " COUNT ",  Total => " PERIOD_OCC " / " PERIOD_TOTAL " (" PERIOD_OCC * 100 / PERIOD_TOTAL "%)"
	print ZONE " Total => " PERIOD_OCC " of " COUNT " (" PERIOD_OCC * 100 / COUNT "%)"
}

