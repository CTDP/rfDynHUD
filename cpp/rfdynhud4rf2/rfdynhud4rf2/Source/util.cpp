#include "util.h"

#include <Windows.h>
#include <string.h>

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

bool stringContains( const char* s, const char* t )
{
    unsigned int l1 = strlen( s );
    unsigned int l2 = strlen( t );
    
    if ( l1 < l2 )
        return ( false );
    
    const char* t0 = t;
    for ( unsigned int i = 0; i < l1; i++ )
    {
        if ( *s++ == *t )
        {
            const char* s0 = s;
            t = t0 + 1;
            for ( unsigned int j = 1; j < l2; j++ )
            {
                if ( *s++ != *t++ )
                    break;
                else if ( j == l2 - 1 )
                    return ( true );
            }
            
            s = s0;
            t = t0;
        }
    }
    
    return ( false );
}

bool stringStartsWith( const char* str, const char* start )
{
    const unsigned int len = strlen( str );
    const unsigned int l2 = strlen( start );
    
    if ( len < l2 )
        return ( false );
    
    for ( unsigned int i = 0; i < l2; i++ )
    {
        if ( *str++ != *start++ )
            return ( false );
    }
    
    return ( true );
}

unsigned short readIniString( const char* file, const char* group, const char* key, const char* defaultValue, char* buffer, const unsigned short bufferLength )
{
    unsigned int len = GetPrivateProfileString( group, key, defaultValue, buffer, bufferLength, file );
    
    if ( len == 0 )
        return ( len );
    
    if ( buffer[0] == '"' )
    {
        for ( unsigned short i = 1; i < len; i++ )
        {
            if ( buffer[i] == '"' )
            {
                memcpy( buffer, buffer + 1, i - 1 );
                buffer[i - 1] = '\0';
                return ( i - 1 );
            }
        }
        
        return ( len );
    }
    
    for ( unsigned short i = 1; i < len; i++ )
    {
        if ( buffer[i] == ' ' )
        {
            buffer[i] = '\0';
            return ( i );
        }
        
        if ( buffer[i] == '#' )
        {
            buffer[i] = '\0';
            return ( i );
        }
        
        if ( ( buffer[i] == '/' ) && ( i < len - 1 ) && ( buffer[i + 1] == '/' ) )
        {
            buffer[i] = '\0';
            return ( i );
        }
    }
    
    return ( len );
}
