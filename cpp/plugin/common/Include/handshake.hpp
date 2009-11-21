#ifndef _HANDSHAKE_H
#define _HANDSHAKE_H

#include "jvm_connection.hpp"

enum HandshakeState
{
    HANDSHAKE_STATE_ERROR = -1,
    
    HANDSHAKE_STATE_NONE = 0,
    
    HANDSHAKE_STATE_WAITING_FOR_OTHER_SIDE = 1,
    HANDSHAKE_STATE_COMPLETE = 2,
    HANDSHAKE_STATE_PLUGIN_DISPOSED = 3,
    HANDSHAKE_STATE_D3D_DISPOSED = 4,
    HANDSHAKE_STATE_DISPOSED = 5,
};

class Handshake
{
public:
    HandshakeState state;
    bool isModSupported;
    bool isInRealtime;
    JVMConnection jvmConn;
    
    bool isPluginEnabled;
    
    bool (*initializeD3D)( void );
    
    bool (*initializePlugin)( void );
    
    void (*onRealtimeEntered)( void );
    void (*onRealtimeExited)( void );
    
    Handshake()
    {
        state = HANDSHAKE_STATE_NONE;
        isModSupported = false;
        isInRealtime = false;
        
        initializeD3D = NULL;
        initializePlugin = NULL;
        
        onRealtimeEntered = NULL;
        onRealtimeExited = NULL;
    }
    
    bool doSanityCheck( const char* RFACTOR_PATH, const char* PLUGIN_PATH, char* fileBuffer );
};

char doHandshake( bool isD3DCalling, Handshake** handshake );

#endif // _HANDSHAKE_H
