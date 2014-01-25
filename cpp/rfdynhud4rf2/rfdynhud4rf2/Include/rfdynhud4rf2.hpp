//‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹‹
//›                                                                         ﬁ
//› Module: Internals Example Header File                                   ﬁ
//›                                                                         ﬁ
//› Description: Declarations for the Internals Example Plugin              ﬁ
//›                                                                         ﬁ
//›                                                                         ﬁ
//› This source code module, and all information, data, and algorithms      ﬁ
//› associated with it, are part of CUBE technology (tm).                   ﬁ
//›                 PROPRIETARY AND CONFIDENTIAL                            ﬁ
//› Copyright (c) 1996-2008 Image Space Incorporated.  All rights reserved. ﬁ
//›                                                                         ﬁ
//›                                                                         ﬁ
//› Change history:                                                         ﬁ
//›   tag.2005.11.30: created                                               ﬁ
//›                                                                         ﬁ
//ﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂﬂ

#ifndef _INTERNALS_EXAMPLE_H
#define _INTERNALS_EXAMPLE_H

#include "InternalsPlugin.hpp"

// This is used for the app to use the plugin for its intended purpose
static const int RF_PLUGIN_VERSION = 6;
class RFDynHUD4rf2InternalsPlugin : public InternalsPluginV06
{
public:
    
    // Constructor/destructor
    RFDynHUD4rf2InternalsPlugin() {}
    ~RFDynHUD4rf2InternalsPlugin() {}
    
    // These are the functions derived from base class InternalsPlugin
    // that can be implemented.
    void Startup( long version );  // game startup
    void Shutdown();               // game shutdown
    
    void EnterRealtime();          // entering realtime
    void ExitRealtime();           // exiting realtime
    
    void StartSession();           // session has started
    void EndSession();             // session has ended
    
    // GAME OUTPUT
    long WantsTelemetryUpdates() { return( 1 ); } // CHANGE TO 1 TO ENABLE TELEMETRY EXAMPLE!
    void UpdateTelemetry( const TelemInfoV01& info );
    
    bool WantsGraphicsUpdates() { return( true ); } // CHANGE TO TRUE TO ENABLE GRAPHICS EXAMPLE!
    void UpdateGraphics( const GraphicsInfoV02& info );
    
    // GAME INPUT
    bool HasHardwareInputs() { return ( false ); } // CHANGE TO TRUE TO ENABLE HARDWARE EXAMPLE!
    void UpdateHardware( const float fDT ) {  } // update the hardware with the time between frames
    void EnableHardware() {  }             // message from game to enable hardware
    void DisableHardware() {  }           // message from game to disable hardware
    
    // See if the plugin wants to take over a hardware control.  If the plugin takes over the
    // control, this method returns true and sets the value of the float pointed to by the
    // second arg.  Otherwise, it returns false and leaves the float unmodified.
    bool CheckHWControl( const char* const controlName, float& fRetVal ) { return ( false ); }
    
    bool ForceFeedback( float& forceValue ) { return ( false ); }  // SEE FUNCTION BODY TO ENABLE FORCE EXAMPLE
    
    // SCORING OUTPUT
    bool WantsScoringUpdates() { return( true ); } // CHANGE TO TRUE TO ENABLE SCORING EXAMPLE!
    void UpdateScoring( const ScoringInfoV01& info );
    
    // COMMENTARY INPUT
    bool RequestCommentary( CommentaryRequestInfoV01& info );  // SEE FUNCTION BODY TO ENABLE COMMENTARY EXAMPLE
    
    // VIDEO EXPORT (sorry, no example code at this time)
    virtual bool WantsVideoOutput() { return( false ); }         // whether we want to export video
    virtual bool VideoOpen( const char* const szFilename, float fQuality, unsigned short usFPS, unsigned long fBPS,
                            unsigned short usWidth, unsigned short usHeight, char* cpCodec = NULL ) { return( false ); } // open video output file
    virtual void VideoClose() {}                                 // close video output file
    virtual void VideoWriteAudio( const short* pAudio, unsigned int uNumFrames ) {} // write some audio info
    virtual void VideoWriteImage( const unsigned char* pImage ) {} // write video image
    
    virtual bool WantsToViewVehicle( CameraControlInfoV01& camControl );   // set ID and camera type and return true
    
    // MESSAGE BOX INPUT
    virtual bool WantsToDisplayMessage( MessageInfoV01& msgInfo );         // set message and return true
    
    virtual void SetEnvironment( const EnvironmentInfoV01& info );         // may be called whenever the environment changes
    
    // SCREEN INFO NOTIFICATIONS
    virtual void InitScreen( const ScreenInfoV01& info );                  // Now happens right after graphics device initialization
    virtual void UninitScreen( const ScreenInfoV01& info );                // Now happens right before graphics device uninitialization
    
    virtual void DeactivateScreen( const ScreenInfoV01& info );            // Window deactivation
    virtual void ReactivateScreen( const ScreenInfoV01& info );            // Window reactivation
    
    virtual void RenderScreenBeforeOverlays( const ScreenInfoV01& info );  // before rFactor overlays
    virtual void RenderScreenAfterOverlays( const ScreenInfoV01& info );   // after rFactor overlays
    
    void PreReset( const ScreenInfoV01 &info );                    // after detecting device lost but before resetting
    void PostReset( const ScreenInfoV01 &info );                   // after resetting
    
    virtual void SetPhysicsOptions( PhysicsOptionsV01 &options );
    
    virtual bool InitCustomControl( CustomControlInfoV01& info );
    
    void ThreadStarted( long type );                                       // called just after a primary thread is started (type is 0=multimedia or 1=simulation)
    void ThreadStopping( long type );                                      // called just before a primary thread is stopped (type is 0=multimedia or 1=simulation)
};

#endif // _INTERNALS_EXAMPLE_H
