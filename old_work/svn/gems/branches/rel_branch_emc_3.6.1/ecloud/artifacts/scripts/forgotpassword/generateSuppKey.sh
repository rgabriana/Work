#!/bin/bash
if [ -z "$1" ]
  then
        echo "Specify the Directory Name where jar is located. Please see pattern below"
	echo "sh generateSuppKey.sh /home/dhanesh/forgotpassword/ RX0UdtNQBIiMxXowFKhOCvTKggLIggT4XUKPV4zhYK9zgl1S93i64sVmSxMtQ+l47r9dWo+CaxL0YBQv82DSj7KgZqTdS9TFqb4VjUXCvlkv1ewwr3VG+FO3A7U/+Ib2klJFJgvn3muGfiONCqmkozbjabEVEd2v44PqhHhFC0M="
        echo "Exiting......."
        exit 1
fi
if [ -z "$2" ]
  then
        echo "Specify the token that you have generated..Please see pattern below.."
	echo "sh generateSuppKey.sh /home/dhanesh/forgotpassword/ RX0UdtNQBIiMxXowFKhOCvTKggLIggT4XUKPV4zhYK9zgl1S93i64sVmSxMtQ+l47r9dWo+CaxL0YBQv82DSj7KgZqTdS9TFqb4VjUXCvlkv1ewwr3VG+FO3A7U/+Ib2klJFJgvn3muGfiONCqmkozbjabEVEd2v44PqhHhFC0M="
        exit 1
fi
cd $1
rm -rf supp_key.txt
java -jar cryptography.jar $2 >supp_key.txt
