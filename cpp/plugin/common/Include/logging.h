#ifndef _LOGGING_H
#define _LOGGING_H

void initLogFilename( const char* RFACTOR_PATH, const char* PLUGIN_PATH );

void logg( const char* message, const bool newLine );
void logg( const char* message );
void logg2( const char* message1, const char* message2, const bool newLine );
void logg2( const char* message1, const char* message2 );
void logg3( const char* message1, const char* message2, const char* message3, const bool newLine );
void logg3( const char* message1, const char* message2, const char* message3 );
void loggf( const char* message, const float value, const bool newLine );
void loggf( const char* message, const float value );
void loggDTmys( const char* message, const __int64 t0, const __int64 t1 );
void loggDTs( const char* message, const __int64 t0, const __int64 t1 );
void loggui( const char* message, const unsigned int value, const bool newLine );
void loggui( const char* message, const unsigned int value );
void loggui2( const char* message, const unsigned int value1, const unsigned int value2, const bool newLine );
void loggui2( const char* message, const unsigned int value1, const unsigned int value2 );
void loggResolution( const unsigned short resX, const unsigned short resY );
void loggui3( const char* message, const unsigned int value1, const unsigned int value2, const unsigned int value3, const bool newLine );
void loggui3( const char* message, const unsigned int value1, const unsigned int value2, const unsigned int value3 );
void loggui4( const char* message, const unsigned int value1, const unsigned int value2, const unsigned int value3, const unsigned int value4, const bool newLine );
void loggui4( const char* message, const unsigned int value1, const unsigned int value2, const unsigned int value3, const unsigned int value4 );
void loggi( const char* message, const int value, const bool newLine );
void loggi( const char* message, const int value );
void loggb( const char* message, const bool value, const bool newLine );
void loggb( const char* message, const bool value );

#endif // _LOGGING_H
