#ifndef _FILESYSTEM_H
#define _FILESYSTEM_H

char* cropBuffer( const char* src, const unsigned int length );
char* cropBuffer2( const char* src );

char* getRFactorPath();
char* getPluginPath();
char* getConfigPath();

unsigned int getFullPath( const char* path, const char* file, char* buffer );
char* getFullPath2( const char* path, const char* file, char* buffer );

char* setBuffer( const char* src, char* buffer );

unsigned int appendPath( const char* file, char* buffer, bool insertBackslash );
char* appendPath2( const char* file, char* buffer, bool insertBackslash );

char* addPreFix( const char* prefix, char* buffer );
char* addPostFix( const char* postfix, char* buffer );

char checkDirectoryExists( const char* dirname, bool checkReadOnly );
char checkFileExists( const char* filename, bool checkReadOnly );

#endif // _FILESYSTEM_H
