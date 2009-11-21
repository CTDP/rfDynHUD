#include "window_handle.h"

#include <Windows.h>
#include <util.h>

DWORD PROCESS_ID = -1;
char* winTextBuffer = NULL;

bool windowHandleSearched = false;
HWND windowHandle = 0;

BOOL CALLBACK EnumWindowsProc( HWND hWnd, LPARAM lParam )
{
    DWORD processId;
    GetWindowThreadProcessId( hWnd, &processId );
    if ( processId != PROCESS_ID )
        return ( TRUE );
    
    if ( GetWindowTextA( hWnd, winTextBuffer, 256 ) <= 0 )
        winTextBuffer[0] = '\0';
    
    /*
    FILE* fff;
    fopen_s( &fff, "windowhandle.log", "a" );
    fprintf( fff, "%d, %s\n", hWnd, winTextBuffer );
    fclose( fff );
    */
    
    if ( stringContains( winTextBuffer, "rFactor v" ) )
    {
        windowHandle = hWnd;
        
        return ( FALSE );
    }
    
    return ( TRUE );
}

HWND getWindowHandle()
{
    if ( !windowHandleSearched )
    {
        PROCESS_ID = GetCurrentProcessId();
        winTextBuffer = (char*)malloc( 256 );
        
        /*
        FILE* fff;
        fopen_s( &fff, "windowhandle.log", "w" );
        fprintf( fff, "WindowHandles for ProcessId: %d\n", PROCESS_ID );
        fclose( fff );
        */
        
        EnumWindows( EnumWindowsProc, NULL );
        
        free( winTextBuffer );
        winTextBuffer = NULL;
        PROCESS_ID = -1;
        
        /*
        fopen_s( &fff, "windowhandle.log", "a" );
        fprintf( fff, "Found WindowHandle: %d\n", windowHandle );
        fclose( fff );
        */
    }
    
    return ( windowHandle );
}
