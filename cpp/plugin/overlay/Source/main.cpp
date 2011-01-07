#include "main.h"

#include <windows.h>
#include <stdio.h>

#include "d3d9.h"

#include "window_handle.h"

#include "filesystem.h"
#include "common.h"
#include "logging.h"

HHOOK hook;
HMODULE DLL_HANDLE;

HWND storedWindowHandle = 0;

HMODULE getDLLHandle()
{
    return ( DLL_HANDLE );
}

LRESULT CALLBACK CallWndProc( int nCode, WPARAM wParam, LPARAM lParam )
{
    if ( ( wParam != 0 ) && ( storedWindowHandle == 0 ) ) // called by this thread
        storedWindowHandle = getWindowHandle();
    
    //if ( wParam != 0 ) // called by this thread
    {
        CWPSTRUCT* cwps = (CWPSTRUCT*)lParam;
        
        //if ( ( cwps->hwnd == storedWindowHandle ) && ( cwps->message == 28 ) ) // nc_activate
        /*
        if ( cwps->hwnd == storedWindowHandle )
        {
            bool activated = ( cwps->wParam != 0 );
            FILE* fff;
            fopen_s( &fff, "hook.log", "a" );
            fprintf( fff, "hWnd = %d, hWnd = %d, message = %d, wParam = %d, lParam = %d\n", storedWindowHandle, cwps->hwnd, cwps->message, cwps->wParam, cwps->lParam );
            fclose( fff );
        }
        */
        
        if ( ( cwps->hwnd == storedWindowHandle ) && ( cwps->message == WM_IME_SETCONTEXT ) )
        {
            bool activated = ( cwps->wParam != 0 );
            notifyOnWindowActivation( activated );
        }
        
        /*
        if ( ( cwps->hwnd == storedWindowHandle ) && ( cwps->message == WM_WINDOWPOSCHANGING ) )
        {
            WINDOWPOS* wp = (WINDOWPOS*)cwps->lParam;
            
            FILE* fff;
            fopen_s( &fff, "hook.log", "a" );
            fprintf( fff, "hWnd = %d, hWnd = %d, message = %d, wParam = %d, lParam = %d, flags = %d, hideWindow = %d, showWindow = %d\n", storedWindowHandle, cwps->hwnd, cwps->message, cwps->wParam, cwps->lParam, wp->flags, wp->flags & SWP_HIDEWINDOW, wp->flags & SWP_SHOWWINDOW );
            fclose( fff );
        }
        */
    }
    
    return ( CallNextHookEx( hook, nCode, wParam, lParam ) );
}

char* getParentD3D9DLL( const char* rfPath )
{
    int rfPathLen = strlen( rfPath );
    char* parentD3D = (char*)malloc( MAX_PATH * sizeof( char) );
    memcpy( parentD3D, rfPath, rfPathLen );
    memcpy( parentD3D + rfPathLen, "\\d3d9_2.dll", 12 );
    if ( checkFileExists( parentD3D, false ) == 0 )
    {
        UINT len = GetSystemDirectory( parentD3D, MAX_PATH );
        memcpy( parentD3D + len, "\\d3d9.dll", 10 );
    }
    
    return ( parentD3D );
}

BOOL WINAPI DllMain( HMODULE hDll, DWORD dwReason, PVOID pvReserved )
{
    if ( dwReason == DLL_PROCESS_ATTACH )
    {
        DLL_HANDLE = hDll;
        
        hook = SetWindowsHookEx( WH_CALLWNDPROC, &CallWndProc, hDll, GetCurrentThreadId() );
        
        DisableThreadLibraryCalls( hDll );
        
        char* rfPath = getRFactorPath();
        char* pluginPath = getPluginPath();
        
        initPluginIniFilename( rfPath, pluginPath );
        initLogFilename( rfPath, pluginPath );
        
        char* parentD3D = getParentD3D9DLL( rfPath );
        
        free( pluginPath );
        free( rfPath );
        
        HMODULE hMod = LoadLibrary( parentD3D );
        if ( hMod == NULL )
        {
            //MessageBox( NULL, "Error loading original 32 bit d3d9.dll", "Error loading original 32 bit d3d9.dll", MB_OK );
            logg3( "Error loading original 32 bit d3d9.dll from \"", parentD3D, "\"." );
            
            free( parentD3D );
            
            return ( FALSE );
        }
        
        logg3( "Using parent d3d9.dll \"", parentD3D, "\"." );
        
        free( parentD3D );
        
        oDirect3DCreate9 = (tDirect3DCreate9)GetProcAddress( hMod, "Direct3DCreate9" );
        
        return ( TRUE );
    }
    
    if ( dwReason == DLL_PROCESS_DETACH )
    {
        UnhookWindowsHookEx( hook );
    }
    
    return ( TRUE );
}

void* DetourFunc( BYTE* src, const BYTE* dst, const int len )
{
    BYTE* jmp = (BYTE*)malloc( len + 5 );
    DWORD dwback;
    
    VirtualProtect( src, len, PAGE_READWRITE, &dwback );
    
    memcpy( jmp, src, len ); jmp += len;
    
    jmp[0] = 0xE9;
    *(DWORD*)( jmp + 1 ) = (DWORD)( src + len - jmp ) - 5;
    
    src[0] = 0xE9;
    *(DWORD*)( src + 1 ) = (DWORD)( dst - src ) - 5;
    
    VirtualProtect( src, len, dwback, &dwback );
    
    return ( jmp - len );
}

bool RetourFunc( BYTE* src, BYTE* restore, const int len )
{
    DWORD dwback;
    
    if ( !VirtualProtect( src, len, PAGE_READWRITE, &dwback ) )
        return ( false );
    
    if ( !memcpy( src, restore, len ) )
       return ( false );
    
    restore[0] = 0xE9;
    *(DWORD*)( restore + 1 ) = (DWORD)( src - restore ) - 5;
    
    if ( !VirtualProtect( src, len, dwback, &dwback ) )
       return ( false );
    
    return ( true );
}
