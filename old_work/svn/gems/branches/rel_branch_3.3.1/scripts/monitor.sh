#!/bin/sh
if (( ! $# )); then
    echo "Usage: $0:t <PID> <minutes to monitor>" >&2
	exit 1;
fi

ticks=`expr $2 \* 60 / 60`
echo "#No %CPU %MEM ElapsedTime Size(KB)"> usage.data
for i in $(seq 1 $ticks)
do
	var=`ps -p $1 -o pcpu,pmem,etime,size | grep -v %`
	echo $i $var >> usage.data
	sleep 60
done
