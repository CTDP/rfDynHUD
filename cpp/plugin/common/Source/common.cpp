#include "common.h"

#include "filesystem.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <Windows.h>
#include "util.h"

char* INI_FILENAME = NULL;
char* _PLUGIN_PATH = NULL;

void initPluginIniFilename( const char* RFACTOR_PATH, const char* PLUGIN_PATH )
{
    if ( INI_FILENAME != NULL )
        return;
    
    _PLUGIN_PATH = (char*)malloc( strlen( PLUGIN_PATH ) + 1 );
    memcpy( _PLUGIN_PATH, PLUGIN_PATH, strlen( PLUGIN_PATH ) + 1 );
    
    INI_FILENAME = (char*)malloc( MAX_PATH );
    const unsigned int pluginPathLength = strlen( PLUGIN_PATH );
    memcpy( INI_FILENAME, PLUGIN_PATH, pluginPathLength );
    memcpy( INI_FILENAME + pluginPathLength, "\\rfdynhud.ini", 14 );
}

unsigned short getPluginIniSetting( const char* group, const char* setting, const char* defaultValue, char* result, const unsigned short bufferLength )
{
    if ( INI_FILENAME == NULL )
    {
        int len = strlen( defaultValue );
        memcpy( result, defaultValue, len );
        
        return ( len );
    }
    
    return ( readIniString( INI_FILENAME, group, setting, defaultValue, result, bufferLength ) );
}

int indexOf( const char* str, const char* test, const int offset, int len )
{
    bool found = false;
    int testLen = strlen( test );
    for ( int i = offset; i < len - testLen + 1; i++ )
    {
        found = true;
        for ( int j = 0; j < testLen; j++ )
        {
            if ( str[i + j] != test[j] )
            {
                found = false;
                break;
            }
        }
        
        if ( found )
            return ( i );
    }
    
    return ( -1 );
}

unsigned short getFolderFromPluginIni( const char* group, const char* setting, const char* defaultValue, char* result, const unsigned short bufferLength )
{
    unsigned short len;
    if ( INI_FILENAME == NULL )
    {
        len = (unsigned short)strlen( defaultValue );
        memcpy( result, defaultValue, len + 1 );
    }
    else
    {
        len = readIniString( INI_FILENAME, group, setting, defaultValue, result, bufferLength );
    }
    
    char* tmp = (char*)malloc( MAX_PATH );
    char* varName = (char*)malloc( MAX_PATH );
    char* var = (char*)malloc( MAX_PATH );
    size_t varLen;
    
    int p = 0;
    while ( ( p = indexOf( result, "${", p, len ) ) >= 0 )
    {
        int p2 = indexOf( result, "}", p + 2, len );
        if ( p2 < 0 )
            break;
        
        memcpy( varName, result + p + 2, p2 - p - 2 );
        varName[p2 - p - 2] = '\0';
        
        char* tmp = var;
        if ( ( _dupenv_s( &var, &varLen, varName ) != 0 ) || ( varLen == 0 ) )
        {
            var = tmp;
            memcpy( var, "N_A", 4 );
            varLen = 3;
        }
        
        if ( p > 0 )
            memcpy( tmp, result, p );
        
        memcpy( tmp + p, var, varLen );
        p += varLen;
        
        int l = strlen( result ) - p2 - 1;
        if ( l > 0 )
        {
            memcpy( tmp + p - 1, result + p2 + 1, l + 1 );
        }
        
        memcpy( result, tmp, p + l );
        
        len = p + l - 1;
    }
    
    if ( ( len < 3 ) || ( ( result[0] != '\\' ) && ( result[1] != ':' ) ) )
    {
        int l = strlen( _PLUGIN_PATH );
        memcpy( tmp, _PLUGIN_PATH, l );
        tmp[l] = '\\';
        memcpy( tmp + l + 1, result, len + 1 );
        len += l + 1;
        memcpy( result, tmp, len + 1 );
    }
    
    free( var );
    free( varName );
    free( tmp );
    
    return ( len );
}
