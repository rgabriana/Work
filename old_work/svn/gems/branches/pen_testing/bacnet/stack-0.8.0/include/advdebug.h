#include <stdbool.h>

#define panic(...) sys_panic( __FILE__,__LINE__, __VA_ARGS__ )
#define chkPtr(ptr) sys_chkPtr( __FILE__,__LINE__, ptr )

bool sys_chkPtr(const char *filename, const int line, void *ptr );
void sys_panic(const char *filename, const int line, const char *msg, ...);
