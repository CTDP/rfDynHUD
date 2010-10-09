#ifndef _JVM_CONNECTION_H
#define _JVM_CONNECTION_H

#include <Windows.h>

void deleteLogFile( const char* PLUGIN_FOLDER );

void logg( const char* message );

bool launchEditor( const char* PLUGIN_PATH );

void destroyJVM();

#endif // _JVM_CONNECTION_H
