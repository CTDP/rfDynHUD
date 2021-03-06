#ifndef _OVERLAY_TEXTURE_H
#define _OVERLAY_TEXTURE_H

#include "jvm_connection.hpp"
#include "d3d9.h"
#include "d3dx9math.h"

const unsigned char SOFT_MAX_NUM_WIDGETS = 48;
const unsigned char MAX_TOTAL_NUM_RECTANGLES = 255;
const unsigned char MAX_NUM_TEXTURES = MAX_TOTAL_NUM_RECTANGLES - SOFT_MAX_NUM_WIDGETS + 1;

unsigned char extractColorDepthFromPixelFormat( long pixelFormat );

/**
 * This callback is used to get access to the pixel buffer (array of RGBA bytes)
 * of source textures by index. It is instantiated by the plugin designer.
 */
class PixelBufferCallback
{
public:
    /**
     * This function must be implemented and return the pixel buffer
     * (array of RGBA values) for the requested source texture.
     * It is guaranteed, that the getPixelBuffer() function is never called a second time
     * before the releasePixelBuffer() function is called for the returned buffer.
     * 
     * @param textureIndex the zero-based index of the source texture
     * @param userObject a pointer to a pointer to a user defined object, that is simply passed back to the releasePixelBuffer() function
     * 
     * @return the array of RGBA values for the given texture. It is not manipulated by the caller.
     */
    virtual unsigned char* getPixelBuffer( const unsigned char textureIndex, void** userObject );
    
    /**
     * This function must be implemented, but doesn't necessarily have to do anything.
     * It is called when the pixel data is not needed anymore by the internal code.
     * It is guaranteed, that the getPixelBuffer() function is never called a second time
     * before the releasePixelBuffer() function is called for the returned buffer.
     * 
     * @param textureIndex the zero-based index of the source texture
     * @param buffer the array of RGBA values for the given texture
     * @param userObject a user defined object, that is simply taken from the getPixelBuffer() function and passed back to this function
     */
    virtual void releasePixelBuffer( const unsigned char textureIndex, unsigned char* buffer, void* userObject );
};

/**
 * This interface is used to render overlays on top of rFactor.
 * It is designed to render the overlay to a byte array without any need for D3D bindings.
 * The arrays are compact and perfectly useful for efficient usage even in combination with other languages (like Java).
 */
class OverlayTextureManager
{
public:
    /**
     * Sets up the manager's internal representation of the given source textures.
     * This method must be called when the Widget configuration has changed (possibly on realtime entered event).
     * 
     * @param numTextures the number of source textures to setup
     * @param textureSizes this array must keep at least 'numTextures' pairs of short values for width and height (for each source-texture)
     * @param numRectangles an array of at least 'numTextures' length, that keeps the number of (used) rectangles per texture
     * @param rectangles the (used) rectangles per source texture. Each rectangle takes four short values (left, top, width, height) in the buffer.
     */
    virtual bool setupTextures( const unsigned char numTextures, const unsigned short* textureSizes, const unsigned char* numRectangles, const unsigned short* rectangles ) = 0;
    
    /**
     * Renders the given textures and updates them in case.
     * 
     * @param numTextures the number of source textures to render
     * @param dirtyRectsBuffers and array of (minimum) size 'numTextures', where each element is an array of quadruples of unsigned shorts for the dirty rectangles to update (left, top, width, height)
     * @param visibleFlags an array of chars (one for each source texture). for each element: 0 = invisible, -1 = just made invisible (will be set to 0), 1 = visible, 2 = just made visible (will be set to 1)
     * @param rectangleVisibleFlags a compact array of visible flags for each rectangle of each source texture (all in a row)
     * @param isTransformed one char for each source texture. 0 = not transformed (all following parameters will be ignored for that texture), 1 = transformed
     * @param translations two unsigned shorts for each source texture (translation-x, translation-y), translation from the upper left of the screen
     * @param rotCenters two unsigned shorts for each source texture (rotCenter-x, rotCenter-y), relative to the upper left of the texture
     * @param rotations one float for each source texture. rotation around the rotCenter point
     * @param scales two floats for each source texture (scale-x, scale-y)
     * @param clipRects four unsigned shorts for each source texture defining a clip-rect for the scaled image
     */
    virtual void render( const float postScaleX, const float postScaleY, const unsigned char numTextures, unsigned short** dirtyRectsBuffers, PixelBufferCallback* pixBuffCallback, char* visibleFlags, char* rectangleVisibleFlags, const char* isTransformed, const float* translations, const unsigned short* rotCenters, const float* rotations, const float* scales, const unsigned short* clipRects ) = 0;
};

/**
 * This part of the API receives some calls to abstracted key functions of the Direct3D device class.
 * You can use it to render an overlay or anything you want.
 * 
 * The Direct3D device is passed to the methods as a void pointer to avoid the need
 * to install a DirectX SDK and to include d3d9.h. Cast it to LPDIRECT3DDEVICE9, if
 * you want to use it.
 * 
 * Note to ISI: Possibly this class could be merged into the PluginObject class or InternalsPluginV4 or something like that.
 */
class D3DManager
{
public:
    
    /**
     * This method is called when the D3D device is created.
     * 
     * @param d3dDev the Direct3D device. Cast it to LPDIRECT3DDEVICE9 to use it.
     * @param resX the resolution width
     * @param resX the resolution height
     * @param colorDepth the colorDepth
     * @param windowed windowed mode?
     * @param fullscreenRefreshHz the refresh rate of the screen in Hz for fullscreen mode
     * @param deviceWindowHandle the window handle of the device's window
     * @param textureManager the TextureOverlayManager to render overlay textures
     */
    virtual void initialize( void* d3dDev, const unsigned short resX, const unsigned short resY, const unsigned char colorDepth, const bool windowed, const unsigned short fullscreenRefreshHz, const HWND deviceWindowHandle, OverlayTextureManager* textureManager );
    
    /**
     * This method is called within a BeginScene()/EndScene() block at the end of the scene.
     * 
     * @param d3dDev the Direct3D device. Cast it to LPDIRECT3DDEVICE9 to use it.
     * @param resX the resolution width
     * @param resX the resolution height
     * @param viewport the viewport
     * @param colorDepth the colorDepth
     * @param textureManager the TextureOverlayManager to render overlay textures
     */
    virtual void renderOverlay( void* d3dDev, const float postScaleX, const float postScaleY, JVMD3DUpdateFunctions* d3dFuncs );
	
    /**
     * This method is called at the beginning of the device's Reset() method.
     * 
     * @param d3dDev the Direct3D device. Cast it to LPDIRECT3DDEVICE9 to use it.
     */
    virtual void preReset( void* d3dDev );
	
    /**
     * This method is called at the end of the device's Reset() method.
     * 
     * @param d3dDev the Direct3D device. Cast it to LPDIRECT3DDEVICE9 to use it.
     * @param resX the resolution width
     * @param resX the resolution height
     * @param colorDepth the colorDepth
     * @param windowed windowed mode?
     * @param fullscreenRefreshHz the refresh rate of the screen in Hz for fullscreen mode
     * @param deviceWindowHandle the window handle of the device's window
     */
	virtual void postReset( void* d3dDev, const unsigned short resX, const unsigned short resY, const unsigned char colorDepth, const bool windowed, const unsigned short fullscreenRefreshHz, const HWND deviceWindowHandle );
	
    /**
     * This method is called when the device is about to be released.
     * 
     * @param d3dDev the Direct3D device. Cast it to LPDIRECT3DDEVICE9 to use it.
     */
	virtual void release( void* d3dDev );
};

/**
 * A pointer to an instance of this interface is passed to the Plugin initialization method.
 * The plugins tells rFactor the number of input actions it defines and the localized texts
 * to be displayed in a separate group of input bindings in the mappings menu.
 * The InputCallback's methods are used to tell the plugin, if a key or button bounds to any
 * of the actions has changed state.
 * 
 * The callback will use the actions' indices to identify them.
 * 
 * Note to ISI: It should be possible to do mappings with modifiers like Shift+A or Ctrl+Shift+G.
 */
class InputSystem
{
public:
    /**
     * This method can be called by the plugin designer to define custom input actions,
     * that will be mappable from within rFactor's mapping menu in a separate group.
     * The Plugin's name will be used to label that group.
     * 
     * @param numActions the number of actions defined by the plugin. The given array must keep at least that number of strings.
     * @param localizedActionTexts the texts to be displayed in the mappings menu. The plugin must take care of localization.
     */
    virtual void defineActions( const unsigned char numActions, const char** localizedActionTexts ) {};
};

/**
 * The InputCallback needs plugin-space implementations of its methods to tell the plugin
 * when one of the defined actions was invoked.
 * The invoking thread will be the same as the one calling all the other plugin event methods.
 * So you can safely update your internal input handling between two calls to renderOverlay().
 */
class InputCallback
{
public:
    /**
     * This method is called when two defined actions are bound to the same key or button.
     * In most cases you won't want to allow that. Return false in that case. In some cases
     * conflicting mappings can be valid, e.g. if you want to use the same key for different actions
     * in different situations (being in the garage or in the pits or being on track, etc.)
     * 
     * This method won't be called, if the action conflicts with an rFactor internal action.
     * This is simply disallowed and will be avoided internally.
     * 
     * @param actions1 the first conflicting action's index (like defined in InputSystem::defineActions())
     * @param actions2 the second conflicting action's index (like defined in InputSystem::defineActions())
     * 
     * @return true, if the conflict is valid and is to be allowed, false, if the used is to be prompted about the conflict.
     */
    virtual bool resolveMappingConflict( const unsigned char action1, const unsigned char action2 ) = 0;
    
    /**
     * This method is called, if one of the bound keys or buttons of a defined action has changed its state.
     * This method is guaranteed to be called from the same thread as the rest of the plugin event methods.
     * 
     * @param actionIndex the index of the action, which's bound key's or button's state has changed. The index is used as defined in the InputSystem::defineActions() method.
     * @param state the state of the bound key or button
     */
    virtual void onInputActionStateChanged( const unsigned char actionIndex, const bool state ) = 0;
};

class TextureAtlas
{
private:
    static const unsigned char MAX_SOURCE_TEXTURES = MAX_NUM_TEXTURES;
    static const unsigned char MAX_RECTANGLES = SOFT_MAX_NUM_WIDGETS;
    static const unsigned int  MAX_TOTAL_RECTS = (unsigned int)MAX_TOTAL_NUM_RECTANGLES;
    static const unsigned char MAX_SUB_RECTS = 32;
    static const unsigned int  MAX_TOTAL_SUBRECTS = MAX_TOTAL_RECTS * (unsigned int)MAX_SUB_RECTS;
    
    const unsigned short m_resX;
    const unsigned short m_resY;
    
    unsigned char m_numSourceTextures;
    unsigned short m_totalNumRectangles;
    unsigned char* m_numRectangles;
    RECT** m_rectangles;
    unsigned char** m_numSubRects;
    unsigned short m_totalNumSubRects;
    RECT** m_srcSubRects;
    RECT** m_trgSubRects;
    
    unsigned short m_width;
    unsigned short m_height;
    unsigned short m_nettoHeight;
    
    bool m_justBuilt;
    
    D3DXMATRIX m_projMatrix;
    D3DXMATRIX m_worldMatrix;
    D3DXMATRIX m_postScaleMatrix;
    D3DXMATRIX m_texMatrix;
    
    void init();
    
    void buildAtlas( const unsigned char numSourceTextures, const unsigned char* numRectangles, const unsigned short* rectangles, const unsigned short width, const unsigned short stripeHeight );
    
public:
    TextureAtlas( const unsigned short resX, const unsigned short resY )
        : m_resX( resX ),
        m_resY( resY )
    {
        init();
        
        // Compose projection matrix for orthogonal project on screen resolution
        const float resXHalf = (float)resX / 2.0f;
        const float resYHalf = (float)resY / 2.0f;
        D3DXMatrixOrthoOffCenterLH( &m_projMatrix, -resXHalf, resXHalf, -resYHalf, resYHalf, -1.0f, +1.0f );
        
        // Shift origo to top-left
        m_projMatrix._41 -= 1.0f;
        m_projMatrix._42 += 1.0f;
        
        // Flip the y-axis (scale by -1.0)
        m_projMatrix._22 *= -1.0f;
        
        D3DXMatrixIdentity( &m_worldMatrix );
        D3DXMatrixIdentity( &m_texMatrix );
    }
    
    unsigned short getWidth()
    {
        return ( m_width );
    }
    
    unsigned short getHeight()
    {
        return ( m_height );
    }
    
    unsigned short getNettoHeight()
    {
        return ( m_nettoHeight );
    }
    
    unsigned short getTotalNumRectangles()
    {
        return ( m_totalNumRectangles );
    }
    
    unsigned short getTotalNumSubRects()
    {
        return ( m_totalNumSubRects );
    }
    
    void buildAtlas( const unsigned char numSourceTextures, const unsigned char* numRectangles, const unsigned short* rectangles );
    
    void updateVertexBuffer( IDirect3DVertexBuffer9* vertexBuffer, const bool updateAll, char* texVisibleFlags, char* rectangleVisibleFlags, const char* isTransformed );
    
    void copyDirtyRectsToTexture( const unsigned char texIndex, const unsigned short numDirtyRects, const unsigned short* dirtyRectsBuffer, const unsigned char* sourceBuffer, const unsigned int sourceTexPitch, unsigned int* destBuffer, const unsigned int trgPitch, IDirect3DTexture9* texture );
    
    void render( LPDIRECT3DDEVICE9 device, IDirect3DTexture9* overlayTexture, IDirect3DVertexBuffer9* vertexBuffer, const bool updateAll, const float postScaleX, const float postScaleY, char* texVisibleFlags, char* rectangleVisibleFlags, const char* isTransformed, const float* translations, const unsigned short* rotCenters, const float* rotations, const float* scales, const unsigned short* clipRects );
};

class OverlayTextureManagerImpl : public OverlayTextureManager
{
private:
    const LPDIRECT3DDEVICE9 m_device;
    const unsigned short m_resX;
    const unsigned short m_resY;
    
    TextureAtlas* m_atlas;
    
    unsigned short* m_sourceTexWidths;
    unsigned short* m_sourceTexHeights;
    unsigned int* m_sourceTexPitches;
    
    ID3DXSprite* createSprite();
    
    unsigned short m_texWidth;
    unsigned short m_texHeight;
    
    const bool m_useProxyTexture;
    
    IDirect3DTexture9* m_overlayTexture;
    IDirect3DTexture9* m_proxyTexture;
    IDirect3DVertexBuffer9* m_vertexBuffer;
    
    bool m_completeTextureUpdateForced;
    
    unsigned short* m_completeDirtyRect;
    
    //void renderTexture( const bool isTransformed, const float transX, const float transY, const unsigned short rotCenterX, const unsigned short rotCenterY, const float rotation, const float scaleX, const float scaleY, const char* rectangleVisibleFlags );
    
    const unsigned short* getDirtyRectsBuffer( unsigned short** dirtyRectsBuffers, const unsigned char texIndex );
    void copyDirtyRectsToTexture( const unsigned char numTextures, unsigned short** dirtyRectsBuffers, PixelBufferCallback* pixBuffCallback );
    
    bool setupD3DTexture( const unsigned short width, const unsigned short height, const bool forceCompleteUpdate );
    
    void releaseInternal( const bool intern, const bool released );
    
public:
    
    bool setupTextures( const unsigned char numTextures, const unsigned short* textureSizes, const unsigned char* numRectangles, const unsigned short* rectangles );
    
    void render( const float postScaleX, const float postScaleY, const unsigned char numTextures, unsigned short** dirtyRectsBuffers, PixelBufferCallback* pixBuffCallback, char* visibleFlags, char* rectangleVisibleFlags, const char* isTransformed, const float* translations, const unsigned short* rotCenters, const float* rotations, const float* scales, const unsigned short* clipRects );
    
    /**
     * This method must be called internally when the device is resetted.
     */
    void release( const bool released )
    {
        releaseInternal( false, released );
    }
    
    OverlayTextureManagerImpl( void* device, const unsigned short resX, const unsigned short resY )
        : m_device( (LPDIRECT3DDEVICE9)device ),
        m_resX( resX ),
        m_resY( resY ),
        m_useProxyTexture( true )
    {
        m_sourceTexWidths = (unsigned short*)malloc( MAX_NUM_TEXTURES * sizeof( unsigned short ) );
        m_sourceTexHeights = (unsigned short*)malloc( MAX_NUM_TEXTURES * sizeof( unsigned short ) );
        m_sourceTexPitches = (unsigned int*)malloc( MAX_NUM_TEXTURES * sizeof( unsigned int ) );
        
        m_atlas = new TextureAtlas( resX, resY );
        
        m_texWidth = 0;
        m_texHeight = 0;
        
        m_overlayTexture = NULL;
        m_proxyTexture = NULL;
        m_vertexBuffer = NULL;
        
        m_completeTextureUpdateForced = true;
        m_completeDirtyRect = (unsigned short*)malloc( ( 1 + 4 ) * sizeof( unsigned short ) );
    }
    
    ~OverlayTextureManagerImpl()
    {
        free( m_sourceTexWidths );
        free( m_sourceTexHeights );
        free( m_sourceTexPitches );
        free( m_completeDirtyRect );
        release( true );
        delete( m_atlas );
    }
};

#endif // _OVERLAY_TEXTURE_H
