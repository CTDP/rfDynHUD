#ifndef _UTIL_H
#define _UTIL_H

bool stringsEqual( const char* s1, const char* s2 );
bool stringContains( const char* s, const char* t );
bool stringStartsWith( const char* str, const char* start );
unsigned short readIniString( const char* file, const char* group, const char* key, const char* defaultValue, char* buffer, const unsigned short bufferLength );

bool isRFactor2();

#endif // _UTIL_H
