#include "filesystem.h"
#include <string.h>
#include <Windows.h>

char* cropBuffer( const char* src, const unsigned int length )
{
    char* result = (char*)malloc( length );
    memcpy( result, src, length );
    
    return ( result );
}

char* cropBuffer2( const char* src )
{
    return ( cropBuffer( src, strlen( src ) + 1 ) );
}

char* getRFactorPath()
{
    char* buffer = (char*)malloc( MAX_PATH );
    unsigned int len = GetModuleFileName( NULL, buffer, MAX_PATH );
    
    for ( unsigned int i = len; i >= 0; i-- )
    {
        if ( buffer[i] == '\\' )
        {
            buffer[i] = '\0';
            len = i;
            break;
        }
    }
    
    char* result = cropBuffer( buffer, len + 1 );
    free( buffer );
    
    return ( result );
}

unsigned int getPluginPath_( char* buffer )
{
    unsigned int len = GetModuleFileName( NULL, buffer, MAX_PATH );
    
    for ( unsigned int j = 0; j < 2; j++ )
    {
        for ( unsigned int i = len; i >= 0; i-- )
        {
            if ( buffer[i] == '\\' )
            {
                buffer[i] = '\0';
                len = i;
                break;
            }
        }
    }
    
    //memcpy( buffer + len, "\\Plugins\\rfDynHUD", 18 );
    
    //return ( len + 17 );
    return ( len );
}

char* getPluginPath()
{
    char* buffer = (char*)malloc( MAX_PATH );
    unsigned int len = getPluginPath_( buffer );
    
    char* result = cropBuffer( buffer, len + 1 );
    free( buffer );
    
    return ( result );
}

char* getLogFolder()
{
    char* buffer = (char*)malloc( MAX_PATH );
    unsigned int len = getPluginPath_( buffer );
    
    memcpy( buffer + len, "\\log", 5 );
    
    char* result = cropBuffer( buffer, len + 5 );
    free( buffer );
    
    return ( result );
}

char* getLogFilename()
{
    char* buffer = (char*)malloc( MAX_PATH );
    unsigned int len = getPluginPath_( buffer );
    
    memcpy( buffer + len, "\\log\\rfdynhud_editor.log", 25 );
    
    char* result = cropBuffer( buffer, len + 25 );
    free( buffer );
    
    return ( result );
}

unsigned int getConfigPath_( char* buffer )
{
    unsigned int len = GetModuleFileName( NULL, buffer, MAX_PATH );
    
    for ( unsigned int i = len; i >= 0; i-- )
    {
        if ( buffer[i] == '\\' )
        {
            buffer[i] = '\0';
            len = i;
            break;
        }
    }
    
    memcpy( buffer + len, "\\Plugins\\rfDynHUD\\config", 25 );
    
    return ( len + 24 );
}

char* getConfigPath()
{
    char* buffer = (char*)malloc( MAX_PATH );
    unsigned int len = getConfigPath_( buffer );
    
    char* result = cropBuffer( buffer, len + 1 );
    free( buffer );
    
    return ( result );
}

unsigned int getFullPath( const char* path, const char* file, char* buffer )
{
    unsigned int pathLen = strlen( path );
    memcpy( buffer, path, pathLen );
    if ( ( path[pathLen - 1] != '\\' ) && ( file[0] != '\\' ) )
    {
        buffer[pathLen++] = '\\';
    }
    
    buffer += pathLen;
    
    unsigned int fileLen = strlen( file );
    memcpy( buffer, file, fileLen + 1 );
    
    return ( pathLen + fileLen );
}

char* getFullPath2( const char* path, const char* file, char* buffer )
{
    getFullPath( path, file, buffer );
    
    return ( buffer );
}

char* setBuffer( const char* src, char* buffer )
{
    memcpy( buffer, src, strlen( src ) + 1 );
    
    return ( buffer );
}

unsigned int appendPath( const char* file, char* buffer, bool insertBackslash )
{
    unsigned int pathLen = strlen( buffer );
    if ( insertBackslash && ( buffer[pathLen - 1] != '\\' ) && ( file[0] != '\\' ) )
    {
        buffer[pathLen++] = '\\';
    }
    unsigned int fileLen = strlen( file );
    buffer += pathLen;
    memcpy( buffer, file, fileLen + 1 );
    
    return ( pathLen + fileLen );
}

char* appendPath2( const char* file, char* buffer, bool insertBackslash )
{
    appendPath( file, buffer, insertBackslash );
    
    return ( buffer );
}

char* addPreFix( const char* prefix, char* buffer )
{
    unsigned int length = strlen( buffer );
    unsigned int pfLen = strlen( prefix );
    memcpy( buffer + pfLen, buffer, length + 1 );
    memcpy( buffer, prefix, pfLen );
    
    return ( buffer );
}

char* addPostFix( const char* postfix, char* buffer )
{
    unsigned int length = strlen( buffer );
    unsigned int pfLen = strlen( postfix );
    memcpy( buffer + length, postfix, pfLen + 1 );
    
    return ( buffer );
}

char checkDirectoryExists( const char* dirname, bool checkReadOnly )
{
    DWORD attribs = GetFileAttributes( dirname );
    
    if ( attribs == INVALID_FILE_ATTRIBUTES )
        return ( 0 );
    
    if ( ( attribs & FILE_ATTRIBUTE_DIRECTORY ) == 0 )
        return ( 0 );
    
    if ( checkReadOnly && ( ( attribs & FILE_ATTRIBUTE_READONLY ) != 0 ) )
        return ( -1 );
    
    return ( 1 );
}

char checkFileExists( const char* filename, bool checkReadOnly )
{
    DWORD attribs = GetFileAttributes( filename );
    
    if ( attribs == INVALID_FILE_ATTRIBUTES )
        return ( 0 );
    
    if ( ( attribs & FILE_ATTRIBUTE_DIRECTORY ) != 0 )
        return ( 0 );
    
    if ( checkReadOnly && ( ( attribs & FILE_ATTRIBUTE_READONLY ) != 0 ) )
        return ( -1 );
    
    return ( 1 );
}
