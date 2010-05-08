#include "telemetry.hpp"

#include <Windows.h>
#include "handshake.hpp"
#include "filesystem.h"
#include "rf_files.h"
#include "logging.h"

// plugin information
unsigned g_uPluginID          = 0;
char     g_szPluginName[]     = "rFactor Dynamic HUD";
unsigned g_uPluginVersion     = 001;
unsigned g_uPluginObjectCount = 1;
RFDynHUDPluginInfo g_PluginInfo;

// interface to plugin information
extern "C" __declspec(dllexport) const char* __cdecl GetPluginName() { return ( g_szPluginName ); }
extern "C" __declspec(dllexport) unsigned __cdecl GetPluginVersion() { return ( g_uPluginVersion ); }
extern "C" __declspec(dllexport) unsigned __cdecl GetPluginObjectCount() { return ( g_uPluginObjectCount ); }

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

const char RFDynHUDPlugin::m_szName[] = "rfDynHUD";
const char RFDynHUDPlugin::m_szSubType[] = "Internals";
const unsigned RFDynHUDPlugin::m_uID = 1;
const unsigned RFDynHUDPlugin::m_uVersion = 3;  // set to 3 for InternalsPluginV3 functionality and added graphical and vehicle info

PluginObjectInfo* RFDynHUDPlugin::GetInfo()
{
    return ( &g_PluginInfo );
}


static const char* RFACTOR_PATH = getRFactorPath();
static const char* PLUGIN_PATH = getPluginPath();
static const char* PROFILE_NAME = getActivePlayerProfileName( RFACTOR_PATH );
static const char* PROFILE_PATH = getActivePlayerProfileFolder( RFACTOR_PATH );

static char* fileBuffer = (char*)malloc( MAX_PATH );

Handshake* handshake = NULL;

HMODULE getDLLHandle()
{
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
    
    handshake->isModSupported = isModSupported( PROFILE_PATH, PROFILE_NAME );
    if ( !handshake->isModSupported )
    {
        logg( "WARNING: Mod is not supported. You won't see anything." );
    }
    
    logg( "Successfully started up rfDynHUD Plugin." );
    
    return ( true );
}

void RFDynHUDPlugin::Startup()
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

void RFDynHUDPlugin::StartSession()
{
    if ( ( handshake != NULL ) && ( handshake->state == HANDSHAKE_STATE_COMPLETE ) )
    {
        handshake->isModSupported = isModSupported( PROFILE_PATH, PROFILE_NAME );
        
        handshake->jvmConn.telemFuncs.call_onSessionStarted();
    }
}

void RFDynHUDPlugin::EndSession()
{
    if ( ( handshake != NULL ) && ( handshake->state == HANDSHAKE_STATE_COMPLETE ) )
    {
        handshake->jvmConn.telemFuncs.call_onSessionEnded();
    }
}

void RFDynHUDPlugin::EnterRealtime()
{
    if ( ( handshake != NULL ) && ( handshake->state == HANDSHAKE_STATE_COMPLETE ) )
    {
        handshake->isInRealtime = true;
        
        handshake->jvmConn.telemFuncs.call_onRealtimeEntered();
        handshake->onRealtimeEntered();
    }
}

void RFDynHUDPlugin::ExitRealtime()
{
    if ( ( handshake != NULL ) && ( handshake->state == HANDSHAKE_STATE_COMPLETE ) )
    {
        handshake->isInRealtime = false;
        
        handshake->jvmConn.telemFuncs.call_onRealtimeExited();
        handshake->onRealtimeExited();
    }
}


void RFDynHUDPlugin::UpdateTelemetry( const TelemInfoV2 &info )
{
    if ( ( handshake != NULL ) && ( handshake->state == HANDSHAKE_STATE_COMPLETE ) && handshake->isModSupported )
    {
        handshake->jvmConn.telemFuncs.copyTelemetryBuffer( (void*)&info, sizeof( TelemInfoV2 ) );
        handshake->jvmConn.telemFuncs.call_onTelemetryDataUpdated();
    }
}

void RFDynHUDPlugin::UpdateScoring( const ScoringInfoV2 &info )
{
    if ( ( handshake != NULL ) && ( handshake->state == HANDSHAKE_STATE_COMPLETE ) && handshake->isModSupported )
    {
        handshake->isInRealtime = info.mInRealtime;
        
        handshake->jvmConn.telemFuncs.copyScoringInfoBuffer( (void*)&info, sizeof( ScoringInfoV2 ) );
        
        for ( long i = 0; i < info.mNumVehicles; i++ )
        {
            handshake->jvmConn.telemFuncs.copyVehicleScoringInfoBuffer( i, (void*)&(info.mVehicle[i]), sizeof( VehicleScoringInfoV2 ), ( i == info.mNumVehicles - 1 ) );
        }
        
        handshake->jvmConn.telemFuncs.call_onScoringInfoUpdated();
    }
}

void RFDynHUDPlugin::UpdateGraphics( const GraphicsInfoV2 &info )
{
    if ( ( handshake != NULL ) && ( handshake->state == HANDSHAKE_STATE_COMPLETE ) && handshake->isModSupported )
    {
        handshake->jvmConn.telemFuncs.copyGraphicsInfoBuffer( (void*)&info, sizeof( GraphicsInfoV2 ) );
    }
}


bool RFDynHUDPlugin::RequestCommentary( CommentaryRequestInfo &info )
{
    if ( ( handshake != NULL ) && ( handshake->state == HANDSHAKE_STATE_COMPLETE ) && handshake->isModSupported )
    {
        handshake->jvmConn.telemFuncs.copyCommentaryInfoBuffer( &info, sizeof( CommentaryRequestInfo ) );
    }
    
    return ( false );
}


bool RFDynHUDPlugin::CheckHWControl( const char * const controlName, float &fRetVal )
{
    // Note that incoming value is the game's computation, in case you're interested.
    
    // Sorry, no control allowed over actual vehicle inputs ... would be too easy to cheat!
    // However, you can still look at the values.
    
    // Note: since the game calls this function every frame for every available control, you might consider
    // doing a binary search if you are checking more than 7 or 8 strings, just to keep the speed up.
    
    return ( false );
}


bool RFDynHUDPlugin::ForceFeedback( float &forceValue )
{
    // Note that incoming value is the game's computation, in case you're interested.
    // I think the bounds are -11500 to 11500 ...
    
    // CHANGE COMMENTS TO ENABLE FORCE EXAMPLE
    
    return ( false );
}
