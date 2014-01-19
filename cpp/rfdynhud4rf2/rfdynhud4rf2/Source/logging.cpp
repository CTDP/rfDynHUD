#include "logging.h"

#include "filesystem.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <Windows.h>
#include "timing.h"
#include "util.h"
#include "common.h"
#include <vector>

char* LOG_FILENAME = NULL;

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

int findLastSeparator( const char* filename )
{
    for ( int i = strlen( filename ) - 1; i >= 0; i-- )
    {
        if ( filename[i] == '\\' )
            return ( i );
    }
    
    return ( -1 );
}

void renameOldLogFile()
{
    //_unlink( LOG_FILENAME );
    
    char* buffer = (char*)malloc( MAX_PATH );
    int folderLength = findLastSeparator( LOG_FILENAME );
    memcpy( buffer, LOG_FILENAME, folderLength + 1 );
    memcpy( buffer + folderLength + 1, "rfdynhud-", 9 );
    unsigned int timeLength = getFileTimeString( LOG_FILENAME, buffer + folderLength + 1 + 9 );
    memcpy( buffer + folderLength + 1 + 9 + timeLength, ".log", 4 + 1 );
    
    MoveFile( LOG_FILENAME, buffer );
    
    free( buffer );
}

void handleArchivedLogFiles()
{
    char* buffer = (char*)malloc( 16 );
    getPluginIniSetting( "GENERAL", "numArchivedLogFiles", "5", buffer, 16 );
    unsigned int numArchivedLogFiles = (unsigned int)max( 0, atoi( buffer ) );
    free( buffer );
    buffer = NULL;
    
    const unsigned int folderLength = findLastSeparator( LOG_FILENAME );
    char* filename = (char*)malloc( MAX_PATH );
    memcpy( filename, LOG_FILENAME, folderLength + 1 );
    memcpy( filename + folderLength + 1, "rfdynhud-*.log", 15 );
    
    char** files = (char**)malloc( 64 * sizeof( char* ) );
    unsigned int numFiles = 0;
    WIN32_FIND_DATA data;
    HANDLE hFile = FindFirstFile( filename, &data );
    if ( hFile != INVALID_HANDLE_VALUE )
    {
        do
        {
            if ( ( ( data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY ) == 0 ) && ( data.cFileName[0] != '.' ) )
            {
                unsigned int len = strlen( data.cFileName );
                char* filename2 = (char*)malloc( len + 1 );
                memcpy( filename2, data.cFileName, len + 1 );
                files[numFiles++] = filename2;
            }
        }
        while ( FindNextFile( hFile, &data ) && ( numFiles < 64 ) );
    }
    
    unsigned int numFiles2 = numFiles;
    while ( numFiles2 > numArchivedLogFiles )
    {
        unsigned int smallestIndex = -1;
        for ( unsigned int i = 0; i < numFiles; i++ )
        {
            if ( files[i] != NULL )
            {
                smallestIndex = i;
                break;
            }
        }
        
        for ( unsigned int i = smallestIndex + 1; i < numFiles; i++ )
        {
            if ( ( files[i] != NULL ) && ( strcmp( files[i], files[smallestIndex] ) < 0 ) )
                smallestIndex = i;
        }
        
        memcpy( filename + folderLength + 1, files[smallestIndex], strlen( files[smallestIndex] ) + 1 );
        _unlink( filename );
        free( files[smallestIndex] );
        files[smallestIndex] = NULL;
        numFiles2--;
    }
    
    for ( unsigned int i = 0; i < numFiles; i++ )
    {
        if ( files[i] != NULL )
        {
            free( files[i] );
            files[i] = NULL;
        }
    }
    
    free( files );
    free( filename );
}

void checkLogfile()
{
    DWORD procId = GetCurrentProcessId();
    
    WIN32_FIND_DATA data;
    HANDLE hFile = FindFirstFile( LOG_FILENAME, &data );
    CloseHandle( hFile );
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
        renameOldLogFile();
        handleArchivedLogFiles();
        
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
        renameOldLogFile();
        handleArchivedLogFiles();
        
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
    
    initPluginIniFilename( RFACTOR_PATH, PLUGIN_PATH );
    
    char* log_folder = (char*)malloc( MAX_PATH );
    getFolderFromPluginIni( "GENERAL", "logFolder", "log", log_folder, MAX_PATH );
    
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
            
            logg3( "WARNING: log folder \"", log_folder, "\" cound not be created. Logging to plugin folder." );
            
            return;
        }
        
        usePluginFolder = true;
    }
    else if ( result == 0 )
    {
        // Log folder doesn't exist. Try to create it.
        
        if ( !createDirectoryWithParents( log_folder ) )
            result = 0;
        else
            result = checkDirectoryExists( log_folder, true );
        
        if ( result != 1 )
        {
            // Plugin directory doesn't exist or is readonly. We cannot do more than log into the rfactor folder.
            
            createFallbackLogfilename( RFACTOR_PATH );
            
            checkLogfile();
            
            logg3( "WARNING: log folder \"", log_folder, "\" cound not be created. Logging to rFactor root folder." );
            
            return;
        }
        
        directoryCreated = true;
    }
    
    const char* filename = "\\rfdynhud.log";
    unsigned int l2 = strlen( filename );
    
    if ( usePluginFolder )
    {
        unsigned int l1 = strlen( PLUGIN_PATH );
        
        LOG_FILENAME = (char*)malloc( l1 + l2 + 1 );
        memcpy( LOG_FILENAME, PLUGIN_PATH, l1 );
        memcpy( LOG_FILENAME + l1, filename, l2 + 1 );
    }
    else
    {
        unsigned int l1 = strlen( log_folder );
        
        LOG_FILENAME = (char*)malloc( l1 + l2 + 1 );
        memcpy( LOG_FILENAME, log_folder, l1 );
        memcpy( LOG_FILENAME + l1, filename, l2 + 1 );
    }
    
    if ( log_folder != NULL )
    {
        free( log_folder );
        log_folder = NULL;
    }
    
    checkLogfile();
    
    if ( directoryCreated )
        logg( "INFO: Log directory not found. It has been created." );
}

void logg( const char* message, const bool newLine )
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

void logg2( const char* message1, const char* message2, const bool newLine )
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

void logg3( const char* message1, const char* message2, const char* message3, const bool newLine )
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

void loggf( const char* message, const float value, const bool newLine )
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

void loggf( const char* message, const float value )
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

void loggDTmys( const char* message, const __int64 t0, const __int64 t1 )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            fprintf( f, "%s%d microseconds^10\n", message, (int)( t1 - t0 ) );
            fclose( f );
        }
    }
}

void loggDTs( const char* message, const __int64 t0, const __int64 t1 )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            fprintf( f, "%s%f seconds\n", message, (float)( (double)( t1 - t0 ) / 10000000.0 ) );
            fclose( f );
        }
    }
}

void loggui( const char* message, const unsigned int value, const bool newLine )
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

void loggui( const char* message, const unsigned int value )
{
    loggui( message, value, true );
}

void loggui2( const char* message, const unsigned int value1, const unsigned int value2, const bool newLine )
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

void loggui2( const char* message, const unsigned int value1, const unsigned int value2 )
{
    loggui2( message, value1, value2, true );
}

void loggResolution( const unsigned short resX, const unsigned short resY )
{
    if ( LOG_FILENAME != NULL )
    {
        FILE* f;
        fopen_s( &f, LOG_FILENAME, "a" );
        if ( f )
        {
            fprintf( f, "Game resolution: %dx%d\n", resX, resY );
            fclose( f );
        }
    }
}

void loggui3( const char* message, const unsigned int value1, const unsigned int value2, const unsigned int value3, const bool newLine )
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

void loggui3( const char* message, const unsigned int value1, const unsigned int value2, const unsigned int value3 )
{
    loggui3( message, value1, value2, value3, true );
}

void loggui4( const char* message, const unsigned int value1, const unsigned int value2, const unsigned int value3, const unsigned int value4, const bool newLine )
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

void loggui4( const char* message, const unsigned int value1, const unsigned int value2, const unsigned int value3, const unsigned int value4 )
{
    loggui4( message, value1, value2, value3, value4, true );
}

void loggi( const char* message, const int value, const bool newLine )
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

void loggi( const char* message, const int value )
{
    loggi( message, value, true );
}

void loggb( const char* message, const bool value, const bool newLine )
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

void loggb( const char* message, const bool value )
{
    loggb( message, value, true );
}
