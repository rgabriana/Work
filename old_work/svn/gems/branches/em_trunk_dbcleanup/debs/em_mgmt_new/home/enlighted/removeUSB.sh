#!/bin/sh
usb1=$(ls /dev/usb1)
if [ -z "$usb1" ]
then
        umount /dev/usb1
fi
usb2=$(ls /dev/usb2)
if [ -z "$usb2" ]
then
        umount /dev/usb2
fi
usb3=$(ls /dev/usb3)
if [ -z "$usb3" ]
then
        umount /dev/usb3
fi
usb4=$(ls /dev/usb4)
if [ -z "$usb4" ]
then
        umount /dev/usb4
fi
