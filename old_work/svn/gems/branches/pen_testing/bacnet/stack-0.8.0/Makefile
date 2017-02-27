# Main Makefile for BACnet-stack project with GCC

# tools - only if you need them.
# Most platforms have this already defined
# CC = gcc
# AR = ar
# MAKE = make
# SIZE = size
#
# Assumes rm and cp are available

include elf_version.mk

# configuration
# If BACNET_DEFINES has not already been set, configure to your needs here
MY_BACNET_DEFINES = -DBACAPP_ALL
# MY_BACNET_DEFINES += -DPRINT_ENABLED=1
# MY_BACNET_DEFINES += -DBACFILE
# MY_BACNET_DEFINES += -DINTRINSIC_REPORTING
BACNET_DEFINES ?= $(MY_BACNET_DEFINES)

# un-comment the next line to build the routing demo application
BACNET_DEFINES += -DBAC_ROUTING

#BACDL_DEFINE=-DBACDL_ETHERNET=1
#BACDL_DEFINE=-DBACDL_ARCNET=1
#BACDL_DEFINE=-DBACDL_MSTP=1
BACDL_DEFINE?=-DBACDL_BIP=1

ifeq (${NO_EM},1)
ENLIGHTED_NO_EM ?= -DNO_EM
endif

# Adding defines for Enlighted Inc.
ENLIGHTED_DEFINE ?= -DENLIGHTED_INC=1

ENLIGHTED_WEB_SERVICES ?= -DUSING_WEB_SERVICES=1

# Define WEAK_FUNC for [...somebody help here; I can't find any uses of it]
DEFINES = $(BACNET_DEFINES) $(BACDL_DEFINE) $(ENLIGHTED_DEFINE)  -DELF_VERSION=\"${ELF_VERSION}\"
DEFINES += $(MAKE_DEFINE)

ifeq (${ENL_TARGET},HVAC)
DEFINES += -DUEM
else
DEFINES += -DEM
endif

# BACnet Ports Directory
BACNET_PORT ?= linux
BACNET_PORT_DIR = ../ports/${BACNET_PORT}
BACNET_HANDLER = ../demo/handler
BACNET_CORE = ../src
BACNET_INCLUDE = ../include

# Enlighted Inc.
BACNET_ENLIGHTED_SRC = ../../enlighted


BUILD=debug

# compiler configuration
INCLUDES = -I$(BACNET_INCLUDE) -I$(BACNET_PORT_DIR) -I$(BACNET_OBJECT) -I$(BACNET_HANDLER) -I$(BACNET_ENLIGHTED_SRC)
INCLUDE += -I/usr/include/x86_64-linux-gnu

ifeq (${CC},g++)
WARNINGS = -Wall
else
WARNINGS = -Wall -Wmissing-prototypes
endif

ifeq (${BUILD},debug)
OPTIMIZATION = -O0
# DEBUGGING = -ggdb -fstack-protector-all -fmudflap -DDEBUG_ENABLED=1 todo review mudflap
DEBUGGING = -ggdb -fstack-protector-all -DDEBUG_ENABLED=1
ifeq (${BACDL_DEFINE},-DBACDL_BIP=1)
DEFINES += -DBIP_DEBUG
endif
endif
CFLAGS  =  $(DEBUGGING) $(OPTIMIZATION) $(WARNINGS) $(STANDARDS) $(INCLUDES) $(DEFINES)
# CFLAGS  = -m32 $(INCLUDES) $(DEFINES)

# Export the variables defined here to all subprocesses
# (see http://www.gnu.org/software/automake/manual/make/Special-Targets.html)
.EXPORT_ALL_VARIABLES:

all: library demos
.PHONY : all library demos clean

library:
	$(MAKE) -C lib all

demos:
	$(MAKE) -C demo all

clean:
	$(MAKE) -s -C lib clean
	$(MAKE) -s -C demo clean
	$(MAKE) -s -C ../enlighted clean
	$(MAKE) -s -C demo/gateway clean
