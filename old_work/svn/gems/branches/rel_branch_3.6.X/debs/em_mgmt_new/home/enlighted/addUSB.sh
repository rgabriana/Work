#!/bin/sh
usb1=$(ls /dev/usb1)
if [ -z "$usb1" ]
then
        umount -l /dev/usb1
        echo "usb1"
        exit
fi 
usb2=$(ls /dev/usb2)
if [ -z "$usb2" ]
then
        umount -l /dev/usb2
        echo "usb2"
        exit
fi 
usb3=$(ls /dev/usb3)
if [ -z "$usb3" ]
then 
        umount -l /dev/usb3
        echo "usb3"
        exit
fi
usb4=$(ls /dev/usb4)
if [ -z "$usb4" ]
then
        umount -l /dev/usb4
        echo "usb4"
        exit
fi
