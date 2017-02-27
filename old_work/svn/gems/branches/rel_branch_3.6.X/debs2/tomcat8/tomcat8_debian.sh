#!/bin/bash

ARCH=$1

if [ "$ARCH" = "a32" ]; then
    cp src/apache-tomcat-8.0.26.tar.gz tomcat8_x86/
    dpkg-deb -b tomcat8_x86/ tomcat8_i386.deb
else
    cp src/apache-tomcat-8.0.26.tar.gz tomcat8_x64/
    dpkg-deb -b tomcat8_x64/ tomcat8_amd64.deb
fi
