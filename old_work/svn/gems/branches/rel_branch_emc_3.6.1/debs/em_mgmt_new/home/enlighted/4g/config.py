#!/usr/bin/python

################################################################################################
#                                                                                              #
# Put configuration values in for ALL items in this section.                                   #
#                                                                                              #
################################################################################################

logFile         = os.environ["TOMCAT_LOG"]+'/4g_health.log' # Full path to log file
logLevel        = 'DEBUG' # Log level as defined in the logger python library
dbFile          = os.environ["ENL_APP_HOME"]+'/Enlighted/4g_health_db' # Full path to DB file
healthProto     = 'https' # Protocol to talk to health check server (http or https)
healthHost      = 'us-tx-m-p-6854f5fff88e.enlightedcloud.net' # What server to health check
healthPath      = '/ecloud/login.jsp' # What path to check (for example, /ecloud/login.jsp) on server
gwHost          = '192.168.13.31' # 4G gateway's hostname or IP address
gwUser          = 'user' # User name for logging into 4G gateway
gwPass          = '12345' # Password for logging into 4G gateway
failureThresh   = 3 # The number of failed health checks before rebooting the 4G gateway
