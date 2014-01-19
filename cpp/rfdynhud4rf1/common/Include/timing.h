#ifndef _TIMING_H
#define _TIMING_H

#include <Windows.h>

__int64 getSystemMicroTime();

unsigned int getFileTimeString( const char* filename, char* buffer );

#endif // _TIMING_H
