#include "rf1/rf1telemetry.hpp"

#include <Windows.h>
#include "telemetry_commons.hpp"
#include "filesystem.h"
#include "logging.h"
#include "common.h"
#include "util.h"

// plugin information
unsigned g_uPluginID          = 0;
char     g_szPluginName[]     = "rFactor Dynamic HUD";
unsigned g_uPluginVersion     = 001;
unsigned g_uPluginObjectCount = 1;
RFDynHUDPluginInfo g_PluginInfo;

// interface to plugin information
extern "C" __declspec( dllexport ) const char* __cdecl GetPluginName() { return ( g_szPluginName ); }
//extern "C" __declspec( dllexport ) unsigned __cdecl GetPluginVersion() { return ( g_uPluginVersion ); }
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

const char RFDynHUDPlugin::m_szName[] = "rfDynHUD";
const char RFDynHUDPlugin::m_szSubType[] = "Internals";
const unsigned RFDynHUDPlugin::m_uID = 1;
const unsigned RFDynHUDPlugin::m_uVersion = 3;  // set to 3 for InternalsPluginV3 functionality and added graphical and vehicle info

PluginObjectInfo* RFDynHUDPlugin::GetInfo()
{
    return ( &g_PluginInfo );
}


void RFDynHUDPlugin::Startup()
{
    onGameStartup( 1255 );
}

void RFDynHUDPlugin::Shutdown()
{
    onGameShutdown();
}

void RFDynHUDPlugin::StartSession()
{
    onSessionStarted();
}

void RFDynHUDPlugin::EndSession()
{
    onSessionEnded();
}

void RFDynHUDPlugin::EnterRealtime()
{
    onRealtimeEntered();
}

void RFDynHUDPlugin::ExitRealtime()
{
    onRealtimeExited();
}


void RFDynHUDPlugin::UpdateTelemetry( const TelemInfoV2 &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->jvmConn.telemFuncs.copyTelemetryBuffer( (void*)&info, sizeof( TelemInfoV2 ) );
        char result = handshake->jvmConn.telemFuncs.call_onTelemetryDataUpdated();
        /*handshake->*/_checkRenderModeResult( "UpdateTelemetry()", result );
    }
}

void RFDynHUDPlugin::UpdateScoring( const ScoringInfoV2 &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->isInRealtime = info.mInRealtime;
        
        handshake->jvmConn.telemFuncs.copyScoringInfoBuffer( (void*)&info, sizeof( ScoringInfoV2 ) );
        
        for ( long i = 0; i < info.mNumVehicles; i++ )
        {
            handshake->jvmConn.telemFuncs.copyVehicleScoringInfoBuffer( i, (void*)&(info.mVehicle[i]), sizeof( VehicleScoringInfoV2 ), ( i == info.mNumVehicles - 1 ) );
        }
        
        char result = handshake->jvmConn.telemFuncs.call_onScoringInfoUpdated();
        /*handshake->*/_checkRenderModeResult( "UpdateScoring()", result );
    }
}

void RFDynHUDPlugin::UpdateGraphics( const GraphicsInfoV2 &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->jvmConn.telemFuncs.copyGraphicsInfoBuffer( (void*)&info, sizeof( GraphicsInfoV2 ) );
        
        //if ( handshake->viewportWidth > 0 )
        //{
        //    char result = handshake->jvmConn.telemFuncs.call_onGraphicsInfoUpdated( handshake->viewportX, handshake->viewportY, handshake->viewportWidth, handshake->viewportHeight );
        //    /*handshake->*/_checkRenderModeResult( "onGraphicsInfoUpdated()", result );
        //}
    }
}


bool RFDynHUDPlugin::RequestCommentary( CommentaryRequestInfo &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
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
