#include "logging.h"

#include "filesystem.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <Windows.h>

char* LOG_FILENAME = NULL;

/*
const char* getOSName( DWORD verMajor, DWORD verMinor, DWORD buildNumber, DWORD productType )
{
    if ( verMajor == 6 )
    {
        if ( verMinor == 1 )
        {
            if ( productType == VER_NT_WORKSTATION )
                return ( "Windows 7" );
            
            return ( "Windows Server 2008 R2" );
        }
        
        if ( verMinor == 0 )
        {
            if ( productType == VER_NT_WORKSTATION )
                return ( "Windows Vista" );
            
            return ( "Windows Server 2008" );
        }
    }
    else if ( verMajor == 5 )
    {
        if ( verMinor == 2 )
        {
            if ( GetSystemMetrics( SM_SERVERR2 ) == 0 )
                return ( "Windows Server 2003" );
            
            return ( "Windows Server 2003 R2" );
        }
        
        if ( verMinor == 1 )
        {
            return ( "Windows XP" );
        }
        
        if ( verMinor == 0 )
        {
            return ( "Windows 2000" );
        }
    }
    
    return ( "UNKNOWN" );
}
*/

void writeProcessIdAndWinVersion( DWORD procId )
{
    //OSVERSIONINFOEX osvi;
    OSVERSIONINFO osvi;
    //BOOL bIsWindowsXPorLater;
    
    //ZeroMemory( &osvi, sizeof( OSVERSIONINFOEX ) );
    //osvi.dwOSVersionInfoSize = sizeof( OSVERSIONINFOEX );
    ZeroMemory( &osvi, sizeof( OSVERSIONINFO ) );
    osvi.dwOSVersionInfoSize = sizeof( OSVERSIONINFO );
    
    GetVersionEx( &osvi );
    
    //bIsWindowsXPorLater = ( ( osvi.dwMajorVersion > 5 ) || ( ( osvi.dwMajorVersion == 5 ) && ( osvi.dwMinorVersion >= 1 ) ) );
    
    FILE* f;
    fopen_s( &f, LOG_FILENAME, "w" );
    
    fprintf( f, "Process-ID: %d\n", procId );
    
    //const char* osName = "UNKNOWN"; //getOSName( osvi.dwMajorVersion, osvi.dwMinorVersion, osvi.dwBuildNumber, ovsi.wProductType );
    //fprintf( f, "Windows version: %s, %d.%d.%d\n", osName, osvi.dwMajorVersion, osvi.dwMinorVersion, osvi.dwBuildNumber );
    fprintf( f, "Windows version: %d.%d.%d\n", osvi.dwMajorVersion, osvi.dwMinorVersion, osvi.dwBuildNumber );
    
    fclose( f );
}

void checkLogfile()
{
    DWORD procId = GetCurrentProcessId();
    
    WIN32_FIND_DATA data;
    HANDLE hFile = FindFirstFile( LOG_FILENAME, &data );
    if ( hFile == INVALID_HANDLE_VALUE )
    {
        // File does not exist.
        writeProcessIdAndWinVersion( procId );
        
        return;
    }
    
    DWORD size = GetFileSize( hFile, NULL );
    if ( size < 32 )
    {
        // File too small. Last run seems to have crashed.
        _unlink( LOG_FILENAME );
        
        writeProcessIdAndWinVersion( procId );
        
        return;
    }
    
    char* buffer = (char*)malloc( 32 );
    FILE* f;
    fopen_s( &f, LOG_FILENAME, "r" );
    fread( buffer, 1, 32, f );
    fclose( f );
    
    for ( unsigned int i = 12; i < 32; i++ )
    {
        if ( ( buffer[i] == '\r' ) || ( buffer[i] == '\n') )
        {
            buffer[i] = '\0';
            break;
        }
    }
    DWORD loggedProcId = atoi( buffer + 12 );
    
    free( buffer );
    
    if ( loggedProcId != procId )
    {
        // File seems to be from the last run. Hence we delete and recreate it.
        _unlink( LOG_FILENAME );
        
        writeProcessIdAndWinVersion( procId );
        
        return;
    }
    
    // File exists and contains the correct process-ID. Hence we must not delete it.
}

void createFallbackLogfilename( const char* RFACTOR_PATH )
{
    const char* filename = "\\rfdynhud.log";
    unsigned int l1 = strlen( RFACTOR_PATH );
    unsigned int l2 = strlen( filename );
    
    LOG_FILENAME = (char*)malloc( l1 + l2 + 1 );
    memcpy( LOG_FILENAME, RFACTOR_PATH, l1 );
    memcpy( LOG_FILENAME + l1, filename, l2 + 1 );
}

void initLogFilename( const char* RFACTOR_PATH, const char* PLUGIN_PATH )
{
    if ( LOG_FILENAME != NULL )
        return;
    
    char* log_folder = (char*)malloc( MAX_PATH );
    memcpy( log_folder, PLUGIN_PATH, strlen( PLUGIN_PATH ) );
    memcpy( log_folder + strlen( PLUGIN_PATH ), "\\log", 5 );
    
    bool usePluginFolder = false;
    bool directoryCreated = false;
    
    char result = checkDirectoryExists( log_folder, true );
    if ( result == -1 )
    {
        // Log folder exists, but is readonly. Try plugin folder below...
        
        free( log_folder );
        log_folder = NULL;
        
        result = checkDirectoryExists( PLUGIN_PATH, true );
        if ( result != 1 )
        {
            // Plugin directory doesn't exist or is readonly. We cannot do more than log into the rfactor folder.
            
            createFallbackLogfilename( RFACTOR_PATH );
            
            checkLogfile();
            
            logg( "WARNING: log folder cound not be created. Logging to rFactor root folder." );
            
            return;
        }
        
        usePluginFolder = true;
    }
    else if ( result == 0 )
    {
        // Log folder doesn't exist. Try to create it.
        
        result = checkDirectoryExists( PLUGIN_PATH, true );
        if ( result != 1 )
        {
            // Plugin directory doesn't exist or is readonly. We cannot do more than log into the rfactor folder.
            
            createFallbackLogfilename( RFACTOR_PATH );
            
            checkLogfile();
            
            logg( "WARNING: log folder cound not be created. Logging to rFactor root folder." );
            
            return;
        }
        
        CreateDirectory( log_folder, NULL );
        directoryCreated = true;
        
        logg( "INFO: Log directory not found. It has been created." );
    }
    
    if ( log_folder != NULL )
    {
        free( log_folder );
        log_folder = NULL;
    }
    
    const char* filename = usePluginFolder ? "\\rfdynhud.log" : "\\log\\rfdynhud.log";
    unsigned int l1 = strlen( PLUGIN_PATH );
    unsigned int l2 = strlen( filename );
    
    LOG_FILENAME = (char*)malloc( l1 + l2 + 1 );
    memcpy( LOG_FILENAME, PLUGIN_PATH, l1 );
    memcpy( LOG_FILENAME + l1, filename, l2 + 1 );
    
    checkLogfile();
    
    if ( directoryCreated )
        logg( "INFO: Log directory not found. It has been created." );
}

void logg( const char* message, bool newLine )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            if ( newLine )
                fprintf( f, "%s\n", message );
            else
                fprintf( f, "%s", message );
            fclose( f );
        }
    }
}

void logg( const char* message )
{
    logg( message, true );
}

void logg2( const char* message1, const char* message2, bool newLine )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            if ( newLine )
                fprintf( f, "%s%s\n", message1, message2 );
            else
                fprintf( f, "%s%s", message1, message2 );
            fclose( f );
        }
    }
}

void logg2( const char* message1, const char* message2 )
{
    logg2( message1, message2, true );
}

void logg3( const char* message1, const char* message2, const char* message3, bool newLine )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            if ( newLine )
                fprintf( f, "%s%s%s\n", message1, message2, message3 );
            else
                fprintf( f, "%s%s%s", message1, message2, message3 );
            fclose( f );
        }
    }
}

void logg3( const char* message1, const char* message2, const char* message3 )
{
    logg3( message1, message2, message3, true );
}

void loggf( const char* message, float value, bool newLine )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            if ( newLine )
                fprintf( f, "%s%f\n", message, value );
            else
                fprintf( f, "%s%f", message, value );
            fclose( f );
        }
    }
}

void loggf( const char* message, float value )
{
    loggf( message, value, true );
}

/*
void loggv( const char* message, ... )
{
    if ( LOG_FILENAME != NULL )
    {
        va_start();
        
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            fprintf( f, "%s%d\n", message, value );
            fclose( f );
        }
    }
}
*/

void loggui( const char* message, unsigned int value, bool newLine )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            if ( newLine )
                fprintf( f, "%s%d\n", message, value );
            else
                fprintf( f, "%s%d", message, value );
            fclose( f );
        }
    }
}

void loggui( const char* message, unsigned int value )
{
    loggui( message, value, true );
}

void loggui2( const char* message, unsigned int value1, unsigned int value2, bool newLine )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            if ( newLine )
                fprintf( f, "%s%d, %d\n", message, value1, value2 );
            else
                fprintf( f, "%s%d, %d", message, value1, value2 );
            fclose( f );
        }
    }
}

void loggui2( const char* message, unsigned int value1, unsigned int value2 )
{
    loggui2( message, value1, value2, true );
}

void loggui3( const char* message, unsigned int value1, unsigned int value2, unsigned int value3, bool newLine )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            if ( newLine )
                fprintf( f, "%s%d, %d, %d\n", message, value1, value2, value3 );
            else
                fprintf( f, "%s%d, %d, %d", message, value1, value2, value3 );
            fclose( f );
        }
    }
}

void loggui3( const char* message, unsigned int value1, unsigned int value2, unsigned int value3 )
{
    loggui3( message, value1, value2, value3, true );
}

void loggui4( const char* message, unsigned int value1, unsigned int value2, unsigned int value3, unsigned int value4, bool newLine )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            if ( newLine )
                fprintf( f, "%s%d, %d, %d, %d\n", message, value1, value2, value3, value4 );
            else
                fprintf( f, "%s%d, %d, %d, %d", message, value1, value2, value3, value4 );
            fclose( f );
        }
    }
}

void loggui4( const char* message, unsigned int value1, unsigned int value2, unsigned int value3, unsigned int value4 )
{
    loggui4( message, value1, value2, value3, value4, true );
}

void loggi( const char* message, int value, bool newLine )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            if ( newLine )
                fprintf( f, "%s%d\n", message, value );
            else
                fprintf( f, "%s%d", message, value );
            fclose( f );
        }
    }
}

void loggi( const char* message, int value )
{
    loggi( message, value, true );
}

void loggb( const char* message, bool value, bool newLine )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            if ( value )
            {
                if ( newLine )
                    fprintf( f, "%strue\n", message );
                else
                    fprintf( f, "%strue", message );
            }
            else
            {
                if ( newLine )
                    fprintf( f, "%sfalse\n", message );
                else
                    fprintf( f, "%sfalse", message );
            }
            fclose( f );
        }
    }
}

void loggb( const char* message, bool value )
{
    loggb( message, value, true );
}
