#include "telemetry_commons.hpp"

#include <Windows.h>
#include "filesystem.h"
#include "logging.h"
#include "common.h"
#include "util.h"

static const char* RFACTOR_PATH = getRFactorPath();
static const char* PLUGIN_PATH = getPluginPath();

static char* fileBuffer = (char*)malloc( MAX_PATH );

Handshake* handshake = NULL;

HMODULE getDLLHandle()
{
    // dummy implementation for dll_handle.h's getDLLHandle()
    logg( "ERROR: The dummy function getDLLHandle() should never be called!!!" );
    return ( NULL );
}

bool _initializePlugin()
{
    logg( "Starting up rfDynHUD Plugin (telemetry part)..." );
    
    if ( handshake == NULL )
    {
        logg( "Handshake wasn't successful, even if it stated, is was." );
        return ( false );
    }
    
    handshake->jvmConn.telemFuncs.call_onStartup();
    
    logg( "Successfully started up rfDynHUD Plugin." );
    
    return ( true );
}

void _checkRenderModeResult( const char* source, const char result )
{
    //logg2( "Checking result from ", source, false );
    //loggi( ": ", result, true );
    
    if ( result == 0 )
    {
        handshake->isInRenderMode = false;
    }
    else if ( result == 1 )
    {
        handshake->isInRenderMode = true;
    }
    else if ( result == 2 )
    {
        handshake->isInRenderMode = true;
        handshake->onTextureRequested();
    }
}

void onGameStartup( const long version )
{
    if ( ( handshake != NULL ) && ( handshake->state == HANDSHAKE_STATE_ERROR ) )
        return;
    
    fileBuffer = (char*)malloc( MAX_PATH );
    initLogFilename( RFACTOR_PATH, PLUGIN_PATH );
    
    switch ( doHandshake( false, &handshake ) )
    {
        case 0: // Incomplete. (step 1 completed)
            handshake->initializePlugin = &_initializePlugin;
            break;
        case 1: // Complete. (step 2 completed)
            if ( handshake->initializeD3D != NULL )
            {
                if ( !handshake->initializeD3D() )
                    return;
            }
            else
            {
                handshake->state = HANDSHAKE_STATE_ERROR;
            }
            _initializePlugin();
            break;
    }
}

void onGameShutdown()
{
    if ( handshake != NULL )
    {
        if ( ( handshake->state == HANDSHAKE_STATE_COMPLETE ) || ( handshake->state == HANDSHAKE_STATE_D3D_DISPOSED ) )
        {
            logg( "Shutting down rfDynHUD Plugin..." );
            
            handshake->jvmConn.telemFuncs.call_onShutdown();
            
            logg( "Successfully shut down rfDynHUD Plugin." );
            
            if ( handshake->state == HANDSHAKE_STATE_D3D_DISPOSED )
            {
                handshake->jvmConn.destroy();
                handshake->state = HANDSHAKE_STATE_DISPOSED;
            }
            else
            {
                handshake->state = HANDSHAKE_STATE_PLUGIN_DISPOSED;
            }
        }
        
        handshake = NULL;
    }
}

Handshake* getHandshakeIfComplete()
{
    if ( handshake == NULL )
        return ( NULL );
    
    if ( handshake->state != HANDSHAKE_STATE_COMPLETE )
        return ( NULL );
    
    return ( handshake );
}

void onSessionStarted()
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->isSessionRunning = true;
        
        char result = handshake->jvmConn.telemFuncs.call_onSessionStarted();
        /*handshake->*/_checkRenderModeResult( "StartSession()", result );
    }
}

void onSessionEnded()
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->jvmConn.telemFuncs.call_onSessionEnded();
        
        handshake->isSessionRunning = false;
        handshake->isInRenderMode = false;
    }
}

void onRealtimeEntered()
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        char result = handshake->jvmConn.telemFuncs.call_onRealtimeEntered();
        /*handshake->*/_checkRenderModeResult( "EnterRealtime()", result );
        
        handshake->isInRealtime = true;
        handshake->onRealtimeEntered();
    }
}

void onRealtimeExited()
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->isInRealtime = false;
        
        handshake->jvmConn.telemFuncs.call_onRealtimeExited();
        //handshake->isInRenderMode = false;
        handshake->onRealtimeExited();
    }
}
