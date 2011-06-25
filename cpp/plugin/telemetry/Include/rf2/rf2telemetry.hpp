#ifndef _RF2_TELEMETRY_H
#define _RF2_TELEMETRY_H

#include "rf2InternalsPlugin.hpp"

// This is used for the app to use the plugin for its intended purpose
class RFDynHUDPlugin2 : public InternalsPluginV04
{
public:
    
    // Constructor/destructor
    RFDynHUDPlugin2();
    ~RFDynHUDPlugin2();
    
    // These are the functions derived from base class InternalsPlugin, that can be implemented.
    void Startup( long version );  // game startup
    void Shutdown();               // game shutdown
    
    void Load();                   // scene/track load
    void Unload();                 // scene/track unload
    
    void EnterRealtime();          // entering realtime
    void ExitRealtime();           // exiting realtime
    
    void StartSession();           // session has started
    void EndSession();             // session has ended
    
    // SCORING OUTPUT
    bool WantsScoringUpdates() { return( true ); }
    void UpdateScoring( const ScoringInfoV01 &info );
    
    // GAME OUTPUT
    long WantsTelemetryUpdates() { return ( true ); }
    void UpdateTelemetry( const TelemInfoV01 &info );
    
    bool WantsGraphicsUpdates() { return( true ); }
    void UpdateGraphics( const GraphicsInfoV01 &info );
    
    // COMMENTARY INPUT
    bool RequestCommentary( CommentaryRequestInfoV01 &info );
    
    // ERROR FEEDBACK
    void Error( const char * const msg );
    
    /*
    // VEHICLE CONTROL (PHYSICS/REPLAY)
    bool WantsToAddVehicle( const long id, NewVehicleDataV01 &data );
    bool WantsToDeleteVehicle( long &id );
    void InitVehicle( const VehicleAndPhysicsV01 &data );
    PluginControlRequest ResetVehicle( const VehicleAndPhysicsV01 &data );
    void UninitVehicle( const long id );
    void SetVehicleLocation( StartingVehicleLocationV01 &data );
    void StartVehicle( const long id, const bool instant );
    bool GetVehicleState( VehicleStateV01 &data );
    
    // ENVIRONMENT
    void SetEnvironment( const EnvironmentInfoV01 &info );
    */
};

#endif __RF2_TELEMETRY_H
