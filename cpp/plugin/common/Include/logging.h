#ifndef _LOGGING_H
#define _LOGGING_H

void initLogFilename( const char* RFACTOR_PATH, const char* PLUGIN_PATH );

void logg( const char* message, bool newLine );
void logg( const char* message );
void logg2( const char* message1, const char* message2, bool newLine );
void logg2( const char* message1, const char* message2 );
void logg3( const char* message1, const char* message2, const char* message3, bool newLine );
void logg3( const char* message1, const char* message2, const char* message3 );
void loggf( const char* message, float value, bool newLine );
void loggf( const char* message, float value );
void loggui( const char* message, unsigned int value, bool newLine );
void loggui( const char* message, unsigned int value );
void loggui2( const char* message, unsigned int value1, unsigned int value2, bool newLine );
void loggui2( const char* message, unsigned int value1, unsigned int value2 );
void loggui3( const char* message, unsigned int value1, unsigned int value2, unsigned int value3, bool newLine );
void loggui3( const char* message, unsigned int value1, unsigned int value2, unsigned int value3 );
void loggui4( const char* message, unsigned int value1, unsigned int value2, unsigned int value3, unsigned int value4, bool newLine );
void loggui4( const char* message, unsigned int value1, unsigned int value2, unsigned int value3, unsigned int value4 );
void loggi( const char* message, int value, bool newLine );
void loggi( const char* message, int value );
void loggb( const char* message, bool value, bool newLine );
void loggb( const char* message, bool value );

#endif // _LOGGING_H
