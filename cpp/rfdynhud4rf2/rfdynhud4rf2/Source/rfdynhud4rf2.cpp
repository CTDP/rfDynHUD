#include "rfdynhud4rf2.hpp"          // corresponding header file

#include <Windows.h>
#include "filesystem.h"
#include "logging.h"
#include "common.h"
#include "util.h"
#include "overlay_texture.h"
#include "direct_input.h"
#include "jvm_connection.hpp"

//#define DIRECT_THREAD_DETACH

static const char* RFACTOR_PATH = getRFactorPath();
static const char* PLUGIN_PATH = getPluginPath();

HWND hWnd = NULL;
unsigned short lastKnownScreenResolutionX = 0;
unsigned short lastKnownScreenResolutionY = 0;

Global global;

D3DManager* d3dManager = NULL;
OverlayTextureManagerImpl* overlayTextureManager = NULL;

Global* getGlobal()
{
    return ( &global );
}

// plugin information

extern "C" __declspec( dllexport )
const char* __cdecl GetPluginName()
{
    return ( "rfDynHUD 4 rf2" );
}

extern "C" __declspec( dllexport )
PluginObjectType __cdecl GetPluginType()
{
    return ( PO_INTERNALS );
}

extern "C" __declspec( dllexport )
int __cdecl GetPluginVersion()
{
    return ( RF_PLUGIN_VERSION );
}

extern "C" __declspec( dllexport )
PluginObject* __cdecl CreatePluginObject()
{
    return ( (PluginObject*) new RFDynHUD4rf2InternalsPlugin() );
}

extern "C" __declspec( dllexport )
void __cdecl DestroyPluginObject( PluginObject* obj )
{
    delete ( (RFDynHUD4rf2InternalsPlugin*) obj );
}

bool doSanityCheck()
{
    char* fileBuffer = (char*)malloc( MAX_PATH );
    
    logg( "Doing a sanity check..." );
    
    bool warningsDetected = false;
    
    if ( checkDirectoryExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD\\log", fileBuffer ), false ) == 0 )
    {
        logg( "    WARNING: log directory not found. Using rFactor root folder." );
        warningsDetected = true;
        //free( fileBuffer );
        //return ( false );
    }
    
    if ( checkDirectoryExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD", fileBuffer ), false ) == 0 )
    {
        logg( "    ERROR: rfDynHud directory not found in the rFactor Plugins folder. The plugin won't work." );
        free( fileBuffer );
        return ( false );
    }
    
    if ( checkFileExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD\\rfdynhud.jar", fileBuffer ), false ) == 0 )
    {
        logg( "    ERROR: rfdynhud.jar not found in the rFactor Plugins\\rfDynHUD folder. The plugin won't work." );
        free( fileBuffer );
        return ( false );
    }
    
    //readIniString( ini_filename, "GENERAL", "numArchivedLogFiles", "5", buffer, 16 );
    if ( checkDirectoryExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD\\config", fileBuffer ), false ) == 0 )
    {
        logg( "    WARNING: config directory not found in the rFactor Plugins\\rfDynHUD folder. Fallback config will be used.\r\nUse the editor to create a valid config and store it as overlay.ini in the config folder. Please see the readme.txt in the config folder for more info." );
        warningsDetected = true;
        //free( fileBuffer );
        //return ( false );
    }
    
    if ( checkFileExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD\\config\\overlay.ini", fileBuffer ), false ) == 0 )
    {
        logg( "    WARNING: overlay.ini configuration file not found in the rFactor Plugins\\rfDynHUD\\config folder. If no mod specific configurations are stored (which is not checked here), fallback config will be used.\r\nUse the editor to create a valid config and store it as overlay.ini in the config folder. Please see the readme.txt in the config folder for more info." );
        warningsDetected = true;
        //free( fileBuffer );
        //return ( false );
    }
    
    if ( checkFileExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD\\config\\three_letter_codes.ini", fileBuffer ), false ) == 0 )
    {
        logg( "    WARNING: three_letter_codes.ini file not found in the rFactor Plugins\\rfDynHUD\\config folder. Driver names will be displayed in full length regardless of the configuration.\r\nPlease see the readme.txt in the config folder for more info." );
        warningsDetected = true;
        //free( fileBuffer );
        //return ( false );
    }
    
    free( fileBuffer );
    
    if ( warningsDetected )
        logg( "Sanity check passed, but warnings detected. The plugin may not work as expected." );
    else
        logg( "Sanity check passed." );
    
    return ( true );
}

bool directInputInitialized = false;

bool appRunning = false;
boolean sane = false;

/**
 * Thread A
 */
void RFDynHUD4rf2InternalsPlugin::Startup( long version )
{
    appRunning = true;
    
    initLogFilename( RFACTOR_PATH, PLUGIN_PATH );
    
    sane = false;
    
    if ( !doSanityCheck() )
        return;
    
    if ( !directInputInitialized )
    {
        logg( "Initializing DirectInput..." );
        if ( !initDirectInput( hWnd ) )
        {
            logg( "ERROR: DirectInput not initialized. Plugin won't work." );
            
            return;
        }
        
        logg( "Successfully initialized DirectInput." );
        directInputInitialized = true;
    }
    
    if ( !global.jvmConn.init( PLUGIN_PATH, lastKnownScreenResolutionX, lastKnownScreenResolutionY ) )
        return;
    
    sane = true;
    
    logg( "Starting up rfDynHUD Plugin..." );
    
    global.jvmConn.telemFuncs.call_onStartup();
    
    logg( "Successfully started up rfDynHUD Plugin." );
}

bool RFDynHUD4rf2InternalsPlugin::InitCustomControl( CustomControlInfoV01 &info )
{
    return ( false );
}

void RFDynHUD4rf2InternalsPlugin::Shutdown()
{
    if ( sane )
    {
        global.jvmConn.attachCurrentThread();
        
        logg( "Shutting down rfDynHUD Plugin..." );
        
        global.jvmConn.telemFuncs.call_onShutdown();
        
        logg( "Successfully shut down rfDynHUD Plugin." );
        
        if ( directInputInitialized )
        {
            logg( "Disposing DirectInput..." );
            disposeDirectInput();
            logg( "Successfully disposed DirectInput." );
        }
        
        global.jvmConn.destroy();
    }
    
    appRunning = false;
}

void RFDynHUD4rf2InternalsPlugin::ThreadStarted( long type )
{
}

void RFDynHUD4rf2InternalsPlugin::ThreadStopping( long type )
{
    if ( type == 0 ) // multimedia
    {
    }
    else if ( type == 1 ) // simulation
    {
        if ( sane )
            global.jvmConn.detachCurrentThread();
    }
}

void checkRenderModeResult( const char* source, const char result )
{
    //logg2( "Checking result from ", source, false );
    //loggi( ": ", result, true );
    
    if ( result == 0 ) // deactivated
    {
        global.isInRenderMode = false;
    }
    else if ( result == 1 ) // still active
    {
        global.isInRenderMode = true;
    }
    else if ( result == 2 ) // reactivated
    {
        global.isInRenderMode = true;
        
        logg( "Texture requested. Updating textures..." );
        
        global.jvmConn.d3dFuncs.updateAllTextureInfos();
        if ( overlayTextureManager->setupTextures( global.jvmConn.d3dFuncs.getNumTextures(), global.jvmConn.d3dFuncs.textureSizes, global.jvmConn.d3dFuncs.numUsedRectangles, global.jvmConn.d3dFuncs.usedRectangles ) )
            logg( "Textures successfully updated." );
        else
            logg( "Textures update failed." );
    }
}

/**
 * Thread A
 */
void RFDynHUD4rf2InternalsPlugin::StartSession()
{
    if ( !sane )
        return;
    
    bool attach = global.jvmConn.attachCurrentThread();
    
    global.isSessionRunning = true;
    
    char result = global.jvmConn.telemFuncs.call_onSessionStarted();
    checkRenderModeResult( "StartSession()", result );
    
    #ifdef DIRECT_THREAD_DETACH
    if ( attach )
        global.jvmConn.detachCurrentThread();
    #endif
}

void RFDynHUD4rf2InternalsPlugin::EndSession()
{
    if ( !sane )
        return;
    
    bool attach = global.jvmConn.attachCurrentThread();
    
    global.jvmConn.telemFuncs.call_onSessionEnded();
    
    global.isSessionRunning = false;
    global.isInRenderMode = false;
    
    #ifdef DIRECT_THREAD_DETACH
    if ( attach )
        global.jvmConn.detachCurrentThread();
    #endif
}

/**
 * Thread B
 */
void RFDynHUD4rf2InternalsPlugin::EnterRealtime()
{
    if ( !sane )
        return;
    
    bool attach = global.jvmConn.attachCurrentThread();
    
    char result = global.jvmConn.telemFuncs.call_onRealtimeEntered();
    checkRenderModeResult( "EnterRealtime()", result );
    
    #ifdef DIRECT_THREAD_DETACH
    if ( attach )
        global.jvmConn.detachCurrentThread();
    #endif
}

/**
 * Thread B
 */
void RFDynHUD4rf2InternalsPlugin::ExitRealtime()
{
    if ( !sane )
        return;
    
    bool attach = global.jvmConn.attachCurrentThread();
    
    global.isInRealtime = false;
    
    global.jvmConn.telemFuncs.call_onRealtimeExited();
    
    #ifdef DIRECT_THREAD_DETACH
    if ( attach )
        global.jvmConn.detachCurrentThread();
    #endif
}

const unsigned int TELEM_INFO_SIZE = sizeof( TelemInfoV01 );

/**
 * Thread B
 */
void RFDynHUD4rf2InternalsPlugin::UpdateTelemetry( const TelemInfoV01& info )
{
    if ( !sane )
        return;
    
    bool attach = global.jvmConn.attachCurrentThread();
    
    char result = global.jvmConn.telemFuncs.call_onTelemetryDataUpdated( (void*)&info, TELEM_INFO_SIZE );
    checkRenderModeResult( "UpdateTelemetry()", result );
    
    #ifdef DIRECT_THREAD_DETACH
    if ( attach )
        global.jvmConn.detachCurrentThread();
    #endif
}

const unsigned int SCORING_INFO_SIZE = sizeof( ScoringInfoV01 );
const unsigned int VEHICLE_SCORING_INFO_SIZE = sizeof( VehicleScoringInfoV01 );

/**
 * Thread B
 */
void RFDynHUD4rf2InternalsPlugin::UpdateScoring( const ScoringInfoV01& info )
{
    if ( !sane )
        return;
    
    bool attach = global.jvmConn.attachCurrentThread();
    
    char result = global.jvmConn.telemFuncs.call_onScoringInfoUpdated( info.mNumVehicles, (void*)&info, SCORING_INFO_SIZE, (void*)(info.mVehicle), VEHICLE_SCORING_INFO_SIZE );
    checkRenderModeResult( "UpdateScoring()", result );
    
    global.isInRealtime = info.mInRealtime;
    
    #ifdef DIRECT_THREAD_DETACH
    if ( attach )
        global.jvmConn.detachCurrentThread();
    #endif
}

const unsigned int COMMENTARY_INFO_SIZE = sizeof( CommentaryRequestInfoV01 );

/**
 * Thread B
 */
bool RFDynHUD4rf2InternalsPlugin::RequestCommentary( CommentaryRequestInfoV01& info )
{
    if ( !sane )
        return ( false );
    
    bool attach = global.jvmConn.attachCurrentThread();
    
    global.jvmConn.telemFuncs.call_onCommentaryRequestInfoUpdated( (void*)&info, COMMENTARY_INFO_SIZE );
    
    #ifdef DIRECT_THREAD_DETACH
    if ( attach )
        global.jvmConn.detachCurrentThread();
    #endif
    
    return ( false );
}

const unsigned int GRAPHICS_INFO_SIZE = sizeof( GraphicsInfoV02 );

/**
 * Thread A
 */
void RFDynHUD4rf2InternalsPlugin::UpdateGraphics( const GraphicsInfoV02& info )
{
    if ( !sane )
        return;
    
    bool attach = global.jvmConn.attachCurrentThread();
    
    global.jvmConn.telemFuncs.call_onGraphicsInfoUpdated( (void*)&info, GRAPHICS_INFO_SIZE );
    
    #ifdef DIRECT_THREAD_DETACH
    if ( attach )
        global.jvmConn.detachCurrentThread();
    #endif
}

/**
 * set ID and camera type and return true
 */
bool RFDynHUD4rf2InternalsPlugin::WantsToViewVehicle( CameraControlInfoV01& camControl )
{
    return ( false );
}

/**
 * MESSAGE BOX INPUT
 * 
 * set message and return true
 */
bool RFDynHUD4rf2InternalsPlugin::WantsToDisplayMessage( MessageInfoV01& msgInfo )
{
    return ( false );
}

/**
 * may be called whenever the environment changes
 */
void RFDynHUD4rf2InternalsPlugin::SetEnvironment( const EnvironmentInfoV01& info )
{
}

void RFDynHUD4rf2InternalsPlugin::SetPhysicsOptions( PhysicsOptionsV01 &options )
{
}

void ensureD3DManager( void* d3dDevice, const unsigned short resX, const unsigned short resY, const unsigned char colorDepth, const bool isWindowed, const unsigned short refreshRate, const HWND hWnd )
{
    if ( d3dManager == NULL )
    {
        d3dManager = new D3DManager();
        
        overlayTextureManager = new OverlayTextureManagerImpl( d3dDevice, resX, resY );
        
        d3dManager->initialize( d3dDevice, resX, resY, colorDepth, isWindowed, refreshRate, hWnd, overlayTextureManager );
    }
}

void disposeD3DManager( void* device )
{
    if ( d3dManager != NULL )
    {
        overlayTextureManager->release( true );
        delete( overlayTextureManager );
        overlayTextureManager = NULL;
        d3dManager->release( device );
        delete( d3dManager );
        d3dManager = NULL;
    }
}

/**
 * Track load
 */
void RFDynHUD4rf2InternalsPlugin::InitScreen( const ScreenInfoV01& info )
{
    if ( !sane )
        return;
    
    hWnd = info.mAppWindow;
    lastKnownScreenResolutionX = info.mWidth;
    lastKnownScreenResolutionY = info.mHeight;
    
    //ensureD3DManager( info );
}

/**
 * Track unload
 */
void RFDynHUD4rf2InternalsPlugin::UninitScreen( const ScreenInfoV01& info )
{
    if ( !sane )
        return;
    
    //disposeD3DManager( info.mDevice );
}

/**
 * Window deactivation
 */
void RFDynHUD4rf2InternalsPlugin::DeactivateScreen( const ScreenInfoV01& info )
{
logg( ">>>>DeactivateScreen" );
    // This function is called at application start. This is unexpected and must be suppressed.
    if ( !appRunning )
        return;
    
    if ( !sane )
        return;
    
    bool attach = global.jvmConn.attachCurrentThread();
    
    //overlayTextureManager->release( false );
    //d3dManager->preReset( info.mDevice );
    //d3dManager->postReset(info.mDevice, info.mWidth, info.mHeight, extractColorDepthFromPixelFormat( info.mPixelFormat ), info.mWindowed, (unsigned short)info.mRefreshRate, info.mAppWindow );
    
    #ifdef DIRECT_THREAD_DETACH
    if ( attach )
        global.jvmConn.detachCurrentThread();
    #endif
}

/**
 * Window reactivation
 */
void RFDynHUD4rf2InternalsPlugin::ReactivateScreen( const ScreenInfoV01& info )
{
    // This function is called at application start. This is unexpected and must be suppressed.
    if ( !appRunning )
        return;
    
    if ( !sane )
        return;
    
    /*
    bool attach = global.jvmConn.attachCurrentThread();
    
    #ifdef DIRECT_THREAD_DETACH
    if ( attach )
        global.jvmConn.detachCurrentThread();
    #endif
    */
}


/**
 * before rFactor overlays
 */
void RFDynHUD4rf2InternalsPlugin::RenderScreenBeforeOverlays( const ScreenInfoV01& info )
{
}

/**
 * after rFactor overlays
 * 
 * Thread A
 */
void RFDynHUD4rf2InternalsPlugin::RenderScreenAfterOverlays( const ScreenInfoV01& info )
{
    if ( !sane )
        return;
    
    bool attach = global.jvmConn.attachCurrentThread();
    
    hWnd = info.mAppWindow;
    
    unsigned char colorDepth = extractColorDepthFromPixelFormat( info.mPixelFormat );
    ensureD3DManager( info.mDevice, info.mWidth, info.mHeight, colorDepth, info.mWindowed, info.mRefreshRate, hWnd );
    
    if ( global.isSessionRunning )
    {
        char result = global.jvmConn.telemFuncs.call_beforeRender( info.mOptionsLeft, info.mOptionsUpper, info.mOptionsWidth, info.mOptionsHeight );
        checkRenderModeResult( "beforeRender()", result );
        
        //if ( global.isInRenderMode && global.isInRealtime )
        if ( global.isInRenderMode )
        {
            result = global.jvmConn.inputFuncs.updateInput( &global.isPluginEnabled );
            checkRenderModeResult( "updateInput()", result );
            
            if ( global.isInRenderMode && global.isPluginEnabled )
            {
                result = global.jvmConn.d3dFuncs.call_update();
                checkRenderModeResult( "update()", result );
            }
        }
    }
    
    if ( appRunning && global.isInRenderMode && global.isPluginEnabled )
    {
        const float postScaleX = (float)info.mWidth / (float)info.mOptionsWidth;
        const float postScaleY = (float)info.mHeight / (float)info.mOptionsHeight;
            
        d3dManager->renderOverlay( info.mDevice, postScaleX, postScaleY, &global.jvmConn.d3dFuncs );
    }
    
    #ifdef DIRECT_THREAD_DETACH
    if ( attach )
        global.jvmConn.detachCurrentThread();
    #endif
}

/**
 * after detecting device lost but before resetting
 */
void RFDynHUD4rf2InternalsPlugin::PreReset( const ScreenInfoV01 &info )
{
logg( ">>>>prereset" );
    if ( !sane )
        return;
    
    overlayTextureManager->release( false );
    d3dManager->preReset( info.mDevice );
}

/**
 * after resetting
 */
void RFDynHUD4rf2InternalsPlugin::PostReset( const ScreenInfoV01 &info )
{
logg( ">>>>postreset" );
    if ( !sane )
        return;
    
    lastKnownScreenResolutionX = (unsigned short)info.mWidth;
    lastKnownScreenResolutionY = (unsigned short)info.mHeight;
        
    unsigned char colorDepth = extractColorDepthFromPixelFormat( info.mPixelFormat );
    
    d3dManager->postReset( info.mDevice, info.mWidth, info.mHeight, colorDepth, info.mWindowed, info.mRefreshRate, info.mAppWindow );
}
