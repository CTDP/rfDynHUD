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
    bool isInRenderMode;
    bool isSessionRunning;
    bool isInRealtime;
    JVMConnection jvmConn;
    
    bool isPluginEnabled;
    
    bool (*initializeD3D)( void );
    
    bool (*initializePlugin)( void );
    
    unsigned short viewportX, viewportY, viewportWidth, viewportHeight;
    
    void (*onTextureRequested)( void );
    void (*onRealtimeEntered)( void );
    void (*onRealtimeExited)( void );
    void (*checkRenderModeResult)( const char*, const int );
    
    Handshake()
    {
        state = HANDSHAKE_STATE_NONE;
        isInRenderMode = false;
        isSessionRunning = false;
        isInRealtime = false;
        
        initializeD3D = NULL;
        initializePlugin = NULL;
        
        viewportX = 0;
        viewportY = 0;
        viewportWidth = 0;
        viewportHeight = 0;
        
        onTextureRequested = NULL;
        onRealtimeEntered = NULL;
        onRealtimeExited = NULL;
        checkRenderModeResult = NULL;
    }
    
    bool doSanityCheck( const char* RFACTOR_PATH, const char* PLUGIN_PATH, char* fileBuffer );
};

char doHandshake( bool isD3DCalling, Handshake** handshake );

#endif // _HANDSHAKE_H
