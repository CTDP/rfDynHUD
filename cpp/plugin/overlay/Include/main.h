#define WIN32_LEAN_AND_MEAN

#ifndef _MAIN_H
#define _MAIN_H

#include <windows.h>
#include "dll_handle.h"

void notifyOnWindowActivation( bool activated );

void* DetourFunc( BYTE* src, const BYTE* dst, const int len );
bool RetourFunc( BYTE* src, BYTE* restore, const int len );

#endif // _MAIN_H
