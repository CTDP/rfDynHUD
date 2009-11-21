#ifndef _RF_FILES_H
#define _RF_FILES_H

char* getActivePlayerProfileFolder( const char* rfPath );
char* getActivePlayerProfileName( const char* rfPath );

bool isModSupported( const char* profilePath, const char* profileFilename );

#endif // _RF_FILES_H