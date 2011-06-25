#include "rf2/rf2telemetry.hpp" // corresponding header file
#include "telemetry_commons.hpp"
#include "filesystem.h"
#include "logging.h"
#include "common.h"
#include "util.h"


// plugin information

//extern "C" __declspec( dllexport ) const char* __cdecl GetPluginName()                   { return( "rfDynHUD" ); }
extern "C" __declspec( dllexport ) PluginObjectType __cdecl GetPluginType()               { return( PO_INTERNALS ); }
extern "C" __declspec( dllexport ) int __cdecl GetPluginVersion()                         { return( 4 ); } // InternalsPluginV04 functionality
extern "C" __declspec( dllexport ) PluginObject* __cdecl CreatePluginObject()            { return( (PluginObject*) new RFDynHUDPlugin2 ); }
extern "C" __declspec( dllexport ) void __cdecl DestroyPluginObject( PluginObject* obj )  { delete( (RFDynHUDPlugin2*) obj ); }


// RFDynHUDPlugin2 class

RFDynHUDPlugin2::RFDynHUDPlugin2()
{
}

RFDynHUDPlugin2::~RFDynHUDPlugin2()
{
}

void RFDynHUDPlugin2::Startup( long version )
{
    onGameStartup( version );
}

void RFDynHUDPlugin2::Shutdown()
{
    onGameShutdown();
}

void RFDynHUDPlugin2::Load()
{
}

void RFDynHUDPlugin2::Unload()
{
}

void RFDynHUDPlugin2::StartSession()
{
    onSessionStarted();
}

void RFDynHUDPlugin2::EndSession()
{
    onSessionEnded();
}

void RFDynHUDPlugin2::EnterRealtime()
{
    onRealtimeEntered();
}

void RFDynHUDPlugin2::ExitRealtime()
{
    onRealtimeExited();
}

void RFDynHUDPlugin2::UpdateTelemetry( const TelemInfoV01 &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->jvmConn.telemFuncs.copyTelemetryBuffer( (void*)&info, sizeof( TelemInfoV01 ) );
        char result = handshake->jvmConn.telemFuncs.call_onTelemetryDataUpdated();
        /*handshake->*/_checkRenderModeResult( "UpdateTelemetry()", result );
    }
}

void RFDynHUDPlugin2::UpdateScoring( const ScoringInfoV01 &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->isInRealtime = info.mInRealtime;
        
        handshake->jvmConn.telemFuncs.copyScoringInfoBuffer( (void*)&info, sizeof( ScoringInfoV01 ) );
        
        for ( long i = 0; i < info.mNumVehicles; i++ )
        {
            handshake->jvmConn.telemFuncs.copyVehicleScoringInfoBuffer( i, (void*)&(info.mVehicle[i]), sizeof( VehicleScoringInfoV01 ), ( i == info.mNumVehicles - 1 ) );
        }
        
        char result = handshake->jvmConn.telemFuncs.call_onScoringInfoUpdated();
        /*handshake->*/_checkRenderModeResult( "UpdateScoring()", result );
    }
}

void RFDynHUDPlugin2::UpdateGraphics( const GraphicsInfoV01 &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->jvmConn.telemFuncs.copyGraphicsInfoBuffer( (void*)&info, sizeof( GraphicsInfoV01 ) );
        
        //if ( handshake->viewportWidth > 0 )
        //{
        //    char result = handshake->jvmConn.telemFuncs.call_onGraphicsInfoUpdated( handshake->viewportX, handshake->viewportY, handshake->viewportWidth, handshake->viewportHeight );
        //    /*handshake->*/_checkRenderModeResult2( "onGraphicsInfoUpdated()", result );
        //}
    }
}

bool RFDynHUDPlugin2::RequestCommentary( CommentaryRequestInfoV01 &info )
{
    Handshake* handshake = getHandshakeIfComplete();
    if ( handshake != NULL )
    {
        handshake->jvmConn.telemFuncs.copyCommentaryInfoBuffer( &info, sizeof( CommentaryRequestInfoV01 ) );
    }
    
    return ( false );
}

void RFDynHUDPlugin2::Error( const char* const msg )
{
    logg2( "ERROR: ", msg );
}
