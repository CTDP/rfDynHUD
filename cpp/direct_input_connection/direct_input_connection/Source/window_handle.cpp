#include "window_handle.h"

#include <Windows.h>
#include <stdio.h>

DWORD PROCESS_ID = -1;
const char* searchedWindowTitle = NULL;
char* winTextBuffer = NULL;

bool windowHandleSearched = false;
HWND windowHandle = 0;

bool stringsEqual( const char* s1, const char* s2 )
{
    unsigned int l1 = strlen( s1 );
    unsigned int l2 = strlen( s2 );
    
    if ( l1 != l2 )
        return ( false );
    
    for ( unsigned int i = 0; i < l1; i++ )
    {
        if ( *s1++ != *s2++ )
            return ( false );
    }
    
    return ( true );
}

BOOL CALLBACK EnumWindowsProc( HWND hWnd, LPARAM lParam )
{
    DWORD processId;
    GetWindowThreadProcessId( hWnd, &processId );
    if ( processId != PROCESS_ID )
        return ( TRUE );
    
    if ( GetWindowTextA( hWnd, winTextBuffer, 256 ) <= 0 )
        winTextBuffer[0] = '\0';
    
    if ( stringsEqual( winTextBuffer, searchedWindowTitle ) )
    {
        windowHandle = hWnd;
        
        return ( FALSE );
    }
    
    return ( TRUE );
}

HWND getWindowHandle( const char* windowTitle )
{
    PROCESS_ID = GetCurrentProcessId();
    searchedWindowTitle = windowTitle;
    winTextBuffer = (char*)malloc( 256 );
    
    windowHandle = 0;
    
    EnumWindows( EnumWindowsProc, NULL );
    
    free( winTextBuffer );
    winTextBuffer = NULL;
    searchedWindowTitle = NULL;
    PROCESS_ID = -1;
    
    HWND result = windowHandle;
    windowHandle = 0;
    
    return ( result );
}
