#include "overlay_texture.h"
#include <Windows.h>
#include "filesystem.h"
#include "common.h"
#include "util.h"
#include "jvm_connection.hpp"
#include "logging.h"

static const char* RFACTOR_PATH = getRFactorPath();
static const char* PLUGIN_PATH = getPluginPath();
static const char* CONFIG_PATH = getConfigPath();

static char* fileBuffer = (char*)malloc( MAX_PATH );

OverlayTextureManager* textureManager = NULL;

unsigned short resX, resY;

static PixelBufferCallback* pixBuffCallback = new PixelBufferCallback();

unsigned char* PixelBufferCallback::getPixelBuffer( const unsigned char textureIndex, void** userObject )
{
    return ( getGlobal()->jvmConn.d3dFuncs.getPixelData( textureIndex ) );
}

void PixelBufferCallback::releasePixelBuffer( const unsigned char textureIndex, unsigned char* buffer, void* userObject )
{
    getGlobal()->jvmConn.d3dFuncs.releasePixelData( textureIndex, buffer );
}

void D3DManager::renderOverlay( void* d3dDev, const float postScaleX, const float postScaleY, JVMD3DUpdateFunctions* d3dFuncs )
{
    textureManager->render( postScaleX, postScaleY, d3dFuncs->getNumTextures(), d3dFuncs->dirtyRectsBuffers, pixBuffCallback, d3dFuncs->textureVisibleFlags, d3dFuncs->rectangleVisibleFlags, d3dFuncs->textureIsTransformedFlags, d3dFuncs->textureTranslations, d3dFuncs->textureRotationCenters, d3dFuncs->textureRotations,d3dFuncs->textureScales, d3dFuncs->textureClipRects );
}

void D3DManager::initialize( void* d3dDev, const unsigned short _resX, const unsigned short _resY, const unsigned char colorDepth, const bool windowed, const unsigned short fullscreenRefreshHz, const HWND deviceWindowHandle, OverlayTextureManager* _textureManager )
{
    fileBuffer = (char*)malloc( MAX_PATH );
    initPluginIniFilename( RFACTOR_PATH, PLUGIN_PATH );
    initLogFilename( RFACTOR_PATH, PLUGIN_PATH );
    
    resX = _resX;
    resY = _resY;
    
    loggResolution( resX, resY );
    
    textureManager = _textureManager;
}

void D3DManager::preReset( void* d3dDev )
{
    //logg( "preReset()" );
}

void D3DManager::postReset( void* d3dDev, const unsigned short resX, const unsigned short resY, const unsigned char colorDepth, const bool windowed, const unsigned short fullscreenRefreshHz, const HWND deviceWindowHandle )
{
    //logg( "postReset()" );
    
    Global* global = getGlobal();
    
    if ( global->isInRenderMode/* && global->isInRealtime*/ )
    {
        logg( "postReset(). Updating Textures..." );
        
        global->jvmConn.d3dFuncs.updateAllTextureInfos();
        if ( textureManager->setupTextures( global->jvmConn.d3dFuncs.getNumTextures(), global->jvmConn.d3dFuncs.textureSizes, global->jvmConn.d3dFuncs.numUsedRectangles, global->jvmConn.d3dFuncs.usedRectangles ) )
            logg( "Textures successfully updated." );
        else
            logg( "Textures update failed." );
    }
}

void D3DManager::release( void* d3dDev )
{
    //logg( "Release()" );
}

bool InputCallback::resolveMappingConflict( const unsigned char action1, const unsigned char action2 ) { return ( true ); }
void InputCallback::onInputActionStateChanged( const unsigned char actionIndex, const bool state ) {}
