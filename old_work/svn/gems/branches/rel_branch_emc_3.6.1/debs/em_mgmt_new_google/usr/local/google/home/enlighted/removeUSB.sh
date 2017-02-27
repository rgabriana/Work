#!/bin/sh
usb1=$(ls /dev/usb1)
if [ -z "$usb1" ]
then
        umount -l /dev/usb1
fi
usb2=$(ls /dev/usb2)
if [ -z "$usb2" ]
then
        umount -l /dev/usb2
fi
usb3=$(ls /dev/usb3)
if [ -z "$usb3" ]
then
        umount -l /dev/usb3
fi
usb4=$(ls /dev/usb4)
if [ -z "$usb4" ]
then
        umount -l /dev/usb4
fi
