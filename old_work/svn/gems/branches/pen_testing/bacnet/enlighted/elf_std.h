#ifndef __ELF_STD_H__
#define __ELF_STD_H__

// #include <stdio.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
// #include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/stat.h>
// #include <sys/time.h>
// #include <errno.h>
#include <fcntl.h>
#include <netinet/in.h>
// #include <memory.h>
#include <string.h>
#include <sys/select.h>
#include <linux/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
// #include <syslog.h>
#include <termios.h>
// #include <signal.h>

#include "bacenum.h"
#include "device.h"

// typedef unsigned char NODE_ADDRESS[3];

// These symbols should have been defined by the makefile. Just in case they have not been,
// set a default here.

#if ! defined ( UEM ) && ! defined ( EM )

#error Define one or the other symbols (EM or UEM), in makefile, or in project settings!

// In makefile   ENL_TARGET=HVAC      -> UEM
//               ENL_TARGET=Lighting  -> EM

#endif

// for "IntelliTrace" in Eclipse
#ifndef ENLIGHTED_INC
#error Define one or the other symbols, in makefile, or in project settings!
#endif

#ifndef ELF_VERSION
#error Define one or the other symbols, in makefile, or in project settings!
#endif

// todonext - adding FLOOR_OFFSET_ID to understand structures better.
#define FLOOR_OFFSET_ID 1000

typedef enum
{
    ELF_RETURN_OK = 0,
    ELF_RETURN_FAIL = -1,
} ELF_RETURN;

#endif /* __ELF_STD_H__ */

