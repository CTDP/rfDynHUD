#include "telemetry.hpp"

#include <Windows.h>
#include "filesystem.h"
#include "logging.h"
#include "common.h"
#include "util.h"
#include "handshake.hpp"

// plugin information
unsigned g_uPluginID          = 0;
char     g_szPluginName[]     = "rFactor Dynamic HUD";
unsigned g_uPluginVersion     = 001;
unsigned g_uPluginObjectCount = 1;
RFDynHUDPluginInfo g_PluginInfo;

// interface to plugin information
extern "C" __declspec( dllexport ) const char* __cdecl GetPluginName() { return ( g_szPluginName ); }
extern "C" __declspec( dllexport ) unsigned __cdecl GetPluginVersion() { return ( g_uPluginVersion ); }
extern "C" __declspec( dllexport ) unsigned __cdecl GetPluginObjectCount() { return ( g_uPluginObjectCount ); }

// get the plugin-info object used to create the plugin.
extern "C" __declspec(dllexport) PluginObjectInfo* __cdecl GetPluginObjectInfo( const unsigned uIndex )
{
    switch ( uIndex )
    {
        case 0:
            return ( &g_PluginInfo );
        default:
            return ( 0 );
    }
}

// RFDynHUDPluginInfo class

RFDynHUDPluginInfo::RFDynHUDPluginInfo()
{
    // put together a name for this plugin
    sprintf( m_szFullName, "%s - %s", g_szPluginName, RFDynHUDPluginInfo::GetName() );
}

const char*    RFDynHUDPluginInfo::GetName()     const { return ( RFDynHUDPlugin::GetName() ); }
const char*    RFDynHUDPluginInfo::GetFullName() const { return ( m_szFullName ); }
const char*    RFDynHUDPluginInfo::GetDesc()     const { return ( "rFactor Dynamic HUD" ); }
const unsigned RFDynHUDPluginInfo::GetType()     const { return ( RFDynHUDPlugin::GetType() ); }
const char*    RFDynHUDPluginInfo::GetSubType()  const { return ( RFDynHUDPlugin::GetSubType() ); }
const unsigned RFDynHUDPluginInfo::GetVersion()  const { return ( RFDynHUDPlugin::GetVersion() ); }
void*          RFDynHUDPluginInfo::Create()      const { return ( new RFDynHUDPlugin() ); }

// InternalsPlugin class

const char RFDynHUDPlugin::m_szName[] = "rfDynHUD 4 rf1";
const char RFDynHUDPlugin::m_szSubType[] = "Internals";
const unsigned RFDynHUDPlugin::m_uID = 1;
const unsigned RFDynHUDPlugin::m_uVersion = 3;  // set to 3 for InternalsPluginV3 functionality and added graphical and vehicle info

PluginObjectInfo* RFDynHUDPlugin::GetInfo()
{
    return ( &g_PluginInfo );
}

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

void RFDynHUDPlugin::Startup()
{
    const long version = 1255;
    
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

void RFDynHUDPlugin::Shutdown()
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

void RFDynHUDPlugin::StartSession()
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->isSessionRunning = true;
        
        char result = handshake->jvmConn.telemFuncs.call_onSessionStarted();
        /*handshake->*/_checkRenderModeResult( "StartSession()", result );
    }
}

void RFDynHUDPlugin::EndSession()
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->jvmConn.telemFuncs.call_onSessionEnded();
        
        handshake->isSessionRunning = false;
        handshake->isInRenderMode = false;
    }
}

void RFDynHUDPlugin::EnterRealtime()
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        char result = handshake->jvmConn.telemFuncs.call_onCockpitEntered();
        /*handshake->*/_checkRenderModeResult( "EnterRealtime()", result );
        
        result = handshake->jvmConn.telemFuncs.call_onDrivingAidsUpdated();
        /*handshake->*/_checkRenderModeResult( "EnterRealtime()/UpdateDrivingAids", result );
        
        handshake->isInRealtime = true;
        handshake->onRealtimeEntered();
    }
}

void RFDynHUDPlugin::ExitRealtime()
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->isInRealtime = false;
        
        handshake->jvmConn.telemFuncs.call_onCockpitExited();
        //handshake->isInRenderMode = false;
        handshake->onRealtimeExited();
    }
}


const unsigned int TELEM_INFO_SIZE = sizeof( TelemInfoV2 );

void RFDynHUDPlugin::UpdateTelemetry( const TelemInfoV2 &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        char result = handshake->jvmConn.telemFuncs.call_onTelemetryDataUpdated( (void*)&info, TELEM_INFO_SIZE );
        /*handshake->*/_checkRenderModeResult( "UpdateTelemetry()", result );
    }
}

const unsigned int SCORING_INFO_SIZE = sizeof( ScoringInfoV2 );
const unsigned int VEHICLE_SCORING_INFO_SIZE = sizeof( VehicleScoringInfoV2 );

void RFDynHUDPlugin::UpdateScoring( const ScoringInfoV2 &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        char result = handshake->jvmConn.telemFuncs.call_onScoringInfoUpdated( info.mNumVehicles, (void*)&info, SCORING_INFO_SIZE, (void*)(info.mVehicle), VEHICLE_SCORING_INFO_SIZE );
        /*handshake->*/_checkRenderModeResult( "UpdateScoring()", result );
        
        handshake->isInRealtime = info.mInRealtime;
    }
}

const unsigned int COMMENTARY_INFO_SIZE = sizeof( CommentaryRequestInfo );

bool RFDynHUDPlugin::RequestCommentary( CommentaryRequestInfo &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->jvmConn.telemFuncs.call_onCommentaryRequestInfoUpdated( (void*)&info, COMMENTARY_INFO_SIZE );
    }
    
    return ( false );
}

const unsigned int GRAPHICS_INFO_SIZE = sizeof( GraphicsInfoV2 );

void RFDynHUDPlugin::UpdateGraphics( const GraphicsInfoV2 &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        char result = handshake->jvmConn.telemFuncs.call_onGraphicsInfoUpdated( (void*)&info, GRAPHICS_INFO_SIZE );
        /*handshake->*/_checkRenderModeResult( "onGraphicsInfoUpdated()", result );
    }
}
