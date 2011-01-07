#ifndef _OVERLAY_TEXTURE_H
#define _OVERLAY_TEXTURE_H

#include "d3d9.h"
#include "extended_ISI_API.h"

const unsigned char SOFT_MAX_NUM_WIDGETS = 48;
const unsigned char MAX_TOTAL_NUM_RECTANGLES = 255;
const unsigned char MAX_NUM_TEXTURES = MAX_TOTAL_NUM_RECTANGLES - SOFT_MAX_NUM_WIDGETS + 1;

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
    
    OverlayTextureManagerImpl( LPDIRECT3DDEVICE9 device, const unsigned short resX, const unsigned short resY )
        : m_device( device ),
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
