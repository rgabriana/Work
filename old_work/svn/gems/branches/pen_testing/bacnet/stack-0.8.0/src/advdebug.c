
#include <stdio.h>
#include <syslog.h>
#include <stdarg.h>
#include <stdbool.h>

#include "version.h"
#include "advdebug.h"

extern bool interactiveExplore;

void log_printf(int level, const char *fmt, ...);

bool sys_chkPtr(const char *filename, const int line, void *ptr)
{
	if (ptr == NULL)
	{
		sys_panic(filename, line, "NULL pointer");
		return true;
	}
	return false;
}

void sys_panic( const char *filename, const int line, const char *msg, ...)
{
    char tbuf[500];
    int ptr ;

    va_list args ;
    va_start( args, msg );
    ptr = snprintf(tbuf, sizeof(tbuf), "panic: %s, %d : ", filename, line );
    vsnprintf(&tbuf[ptr], sizeof(tbuf)-ptr, msg, args );
    va_end( args );

#if __GNUC__ == 4 && __GNUC_MINOR__ == 6 && __GNUC_PATCHLEVEL__ == 3
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wformat-security"
#endif

	if (interactiveExplore)
	{
		fprintf(stderr, "%s", tbuf);
		fprintf(stderr, "\n\r");
	}

#if __GNUC__ == 4 && __GNUC_MINOR__ == 6 && __GNUC_PATCHLEVEL__ == 3
#pragma GCC diagnostic pop
#endif
	log_printf(LOG_CRIT, "%s %s", BACnet_Version, tbuf);
}
