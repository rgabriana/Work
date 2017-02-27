#!/bin/bash
source /etc/environment
export KEY_DIR="$ENL_APP_HOME/Enlighted/"
{
# Input parameters
export ACTION="$1"
export SECURITYKEY="$2"

saveKey() {
   rm -rf $KEY_DIR/recoverykey.key
   touch $KEY_DIR/recoverykey.key
   OUT=$?
   if [ $OUT -eq 0 ]
   then
   		echo -n $SECURITYKEY >> $KEY_DIR/recoverykey.key
   		OUT=$?
   		echo $OUT
   fi
}

if [ $ACTION=="save" ]
then
    saveKey
fi

}
