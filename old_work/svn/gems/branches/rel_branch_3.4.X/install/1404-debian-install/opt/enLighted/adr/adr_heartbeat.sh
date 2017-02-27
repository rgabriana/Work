#!/bin/bash

SED_ARG1="-i -e 's/\(<span\ id=\"drtype\">\).*\(<\/span>\)/\1$1\2/' -e 's/\(<span\ id=\"level\">\).*\(<\/span>\)/\1$2\2/' -e 's/\(<span\ id=\"starttime\">\).*\(<\/span>\)/\1$3\2/' -e 's/\(<span\ id=\"duration\">\).*\(<\/span>\)/\1$4\2/' /var/lib/tomcat6/webapps/ROOT/heartbeat.jsp"

eval sed "$SED_ARG1"

