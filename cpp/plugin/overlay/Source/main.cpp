#include "main.h"

#include <windows.h>
#include <stdio.h>

#include "d3d9.h"

#include "window_handle.h"

#include "filesystem.h"
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

BOOL WINAPI DllMain( HMODULE hDll, DWORD dwReason, PVOID pvReserved )
{
    if ( dwReason == DLL_PROCESS_ATTACH )
    {
        DLL_HANDLE = hDll;
        
        hook = SetWindowsHookEx( WH_CALLWNDPROC, &CallWndProc, hDll, GetCurrentThreadId() );
        
        DisableThreadLibraryCalls( hDll );
        
        char sysd3d[MAX_PATH];
        UINT len = GetSystemDirectory( sysd3d, MAX_PATH );
        memcpy( sysd3d + len, "\\d3d9.dll", 10 );
        /*
        FILE* fff;
        fopen_s( &fff, "sysdir.log", "w" );
        fprintf( fff, sysd3d );
        fclose( fff );
        */
        
        HMODULE hMod = LoadLibrary( sysd3d );
        if ( hMod == NULL )
        {
            //MessageBox( NULL, "Error loading original 32 bit d3d9.dll", "Error loading original 32 bit d3d9.dll", MB_OK );
            initLogFilename( getRFactorPath(), getPluginPath() );
            logg3( "Error loading original 32 bit d3d9.dll from \"", sysd3d, "\"." );
            return ( FALSE );
        }
        
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
