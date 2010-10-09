#include "timing.h"

#include <Windows.h>
//#include <time.h>
#include <stdio.h>

/*
SYSTEMTIME st;
FILETIME ft;

LARGE_INTEGER getNanoTime()
{
    GetSystemTime( &st );
    SystemTimeToFileTime( &st, &ft );
    
    LARGE_INTEGER li;
    li.LowPart = ft.dwLowDateTime;
    li.HighPart = ft.dwHighDateTime;
    
    return ( li );
}

int timeDiff( LARGE_INTEGER* i1, LARGE_INTEGER* i2 )
{
    LARGE_INTEGER delta;
    
    delta.High
}
*/

__int64 getSystemMicroTime()
{
    FILETIME ft;
    GetSystemTimeAsFileTime( &ft );
    __int64 th = ft.dwHighDateTime;
    __int64 tl = ft.dwLowDateTime;
    __int64 tt = ( th << 32 ) | tl;
    
    return ( tt );
}

void writeInt2ToBuffer( WORD i, char* buffer )
{
    if ( i < 10 )
    {
        *buffer = '0';
        buffer += 1;
        _itoa( i, buffer, 10 );
        buffer += 1;
    }
    else
    {
        _itoa( i, buffer, 10 );
        buffer += 2;
    }
}

unsigned int getFileTimeString( const char* filename, char* buffer )
{
    FILETIME ft;
    SYSTEMTIME st0, st;
    HANDLE hFile = CreateFile( filename, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL );
    GetFileTime( hFile, NULL, NULL, &ft );
    CloseHandle( hFile );
    
    FileTimeToSystemTime( &ft, &st0 );
    SystemTimeToTzSpecificLocalTime( NULL, &st0, &st );
    
    char* b0 = buffer;
    _itoa( st.wYear, buffer, 10 );
    buffer += 4;
    *buffer = '-';
    buffer += 1;
    writeInt2ToBuffer( st.wMonth, buffer );
    buffer += 2;
    *buffer = '-';
    buffer += 1;
    writeInt2ToBuffer( st.wDay, buffer );
    buffer += 2;
    *buffer = '_';
    buffer += 1;
    writeInt2ToBuffer( st.wHour, buffer );
    buffer += 2;
    *buffer = '-';
    buffer += 1;
    writeInt2ToBuffer( st.wMinute, buffer );
    buffer += 2;
    *buffer = '-';
    buffer += 1;
    writeInt2ToBuffer( st.wSecond, buffer );
    buffer += 2;
    
    return ( 19 );
}
