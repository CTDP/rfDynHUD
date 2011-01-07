#include "overlay_texture.h"

#include "d3d9.h"
#include "image_util.h"
#include "logging.h"
//#include "timing.h"

const float ZERO_Z_INDEX = 0.0f;
const float ONE_Z_INDEX = -1.0f;

//#define DEBUG_TEXTURE

void TextureAtlas::init()
{
    m_numRectangles = (unsigned char*)malloc( (unsigned int)MAX_SOURCE_TEXTURES * sizeof( unsigned char ) );
    m_rectangles = (RECT**)malloc( MAX_TOTAL_RECTS * sizeof( RECT* ) );
    m_numSubRects = (unsigned char**)malloc( (unsigned int)MAX_SOURCE_TEXTURES * sizeof( unsigned char* ) );
    m_srcSubRects = (RECT**)malloc( MAX_TOTAL_SUBRECTS * sizeof( RECT* ) );
    m_trgSubRects = (RECT**)malloc( MAX_TOTAL_SUBRECTS * sizeof( RECT* ) );
    
    unsigned int i, j;
    
    for ( i = 0; i < MAX_TOTAL_RECTS; i++ )
    {
        m_rectangles[i] = NULL;
    }
    
    for ( i = 0; i < MAX_TOTAL_SUBRECTS; i++ )
    {
        m_srcSubRects[i] = NULL;
        m_trgSubRects[i] = NULL;
    }
    
    m_totalNumRectangles = 0;
    
    for ( i = 0; i < MAX_SOURCE_TEXTURES; i++ )
    {
        m_numRectangles[i] = 0;
        m_numSubRects[i] = (unsigned char*)malloc( (unsigned int)MAX_RECTANGLES * sizeof( unsigned char ) );
        
        for ( j = 0; j < MAX_RECTANGLES; j++ )
        {
            m_numSubRects[i][j] = 0;
        }
    }
}

static const unsigned short TILE_OVERLAP = 1;
static const unsigned short BORDER = +1;

void TextureAtlas::buildAtlas( const unsigned char numSourceTextures, const unsigned char* numRectangles, const unsigned short* rectangles, const unsigned short width, const unsigned short stripeHeight )
{
    unsigned char idx_t, idx_r, idx_sr;
    unsigned short i, j;
    
    short sx0, sy0, sx, sy, w0, w, h0, h, tx, ty, restW;
    
    m_width = width;
    tx = BORDER;
    ty = BORDER;
    restW = m_width;
    
    RECT* rect;
    RECT* src;
    RECT* trg;
    
    m_numSourceTextures = numSourceTextures;
    m_totalNumRectangles = 0;
    m_totalNumSubRects = 0;
    
    i = 0;
    j = 0;
    for ( idx_t = 0; idx_t < m_numSourceTextures; idx_t++ )
    {
        m_totalNumRectangles += numRectangles[idx_t];
        m_numRectangles[idx_t] = numRectangles[idx_t];
        
        for ( idx_r = 0; idx_r < m_numRectangles[idx_t]; idx_r++ )
        {
//loggui2( "tex, rect: ", idx_t, idx_r );
            sx0 = rectangles[i * 4 + 0];
            sx = sx0;
            sy0 = rectangles[i * 4 + 1];
            sy = sy0;
            w0 = rectangles[i * 4 + 2];
            w = w0;
            h0 = rectangles[i * 4 + 3];
            h = h0;
//loggui4( "rect: ", sx, sy, w0, h0 );
            
            rect = m_rectangles[i];
            if ( rect == NULL )
            {
                rect = new RECT();
                m_rectangles[i] = rect;
            }
            rect->left = sx0;
            rect->right = sx0 + w0;
            rect->top = sy0;
            rect->bottom = sy0 + h0;
            
            m_numSubRects[idx_t][idx_r] = 0;
            
            while ( ( w > 0 ) || ( h > 0  ) )
            {
                idx_sr = m_numSubRects[idx_t][idx_r]++;
                m_totalNumSubRects++;
                
                if ( idx_sr >= MAX_SUB_RECTS )
                {
                    m_height = 8192; // this will lead to recalculating the atlas with a larger width.
                    return;
                }
                
                src = m_srcSubRects[j];
                if ( src == NULL )
                {
                    src = new RECT();
                    m_srcSubRects[j] = src;
                }
                
                trg = m_trgSubRects[j];
                if ( trg == NULL )
                {
                    trg = new RECT();
                    m_trgSubRects[j] = trg;
                }
                
                if ( w + BORDER > restW )
                {
                    if ( h + BORDER > stripeHeight )
                    {
//logg( "a" );
                        src->left = sx;
                        src->right = sx + restW - BORDER;
                        src->top = sy;
                        src->bottom = sy + stripeHeight - BORDER;
                        
                        trg->left = tx;
                        trg->right = tx + restW - BORDER;
                        trg->top = ty;
                        trg->bottom = ty + stripeHeight - BORDER;
                        
                        sx += restW - TILE_OVERLAP - BORDER;
                        w -= restW - TILE_OVERLAP - BORDER;
                        
                        tx = BORDER;
                        restW = m_width - BORDER;
                        ty += stripeHeight;
//loggui4( "a1: ", sx, sy, w, h );
//loggui3( "a2: ", tx, ty, restW );
                    }
                    else //if ( h + BORDER <= stripeHeight )
                    {
//logg( "b" );
                        src->left = sx;
                        src->right = sx + restW - BORDER;
                        src->top = sy;
                        src->bottom = sy + h;
                        
                        trg->left = tx;
                        trg->right = tx + restW - BORDER;
                        trg->top = ty;
                        trg->bottom = ty + h;
                        
                        sx += restW - TILE_OVERLAP - BORDER;
                        w -= restW - TILE_OVERLAP - BORDER;
                        
                        tx = BORDER;
                        restW = m_width - BORDER;
                        ty += stripeHeight;
//loggui4( "b1: ", sx, sy, w, h );
//loggui3( "b2: ", tx, ty, restW );
                    }
                }
                else //if ( w + BORDER <= restW )
                {
                    if ( h + BORDER > stripeHeight )
                    {
//logg( "c" );
                        src->left = sx;
                        src->right = sx + w;
                        src->top = sy;
                        src->bottom = sy + stripeHeight - BORDER;
                        
                        trg->left = tx;
                        trg->right = tx + w;
                        trg->top = ty;
                        trg->bottom = ty + stripeHeight - BORDER;
                        
//loggui4( "c1a: ", sx, sy, w, h );
//loggui3( "c2a: ", tx, ty, restW );
                        tx += w + BORDER;
                        restW -= w + BORDER;
                        
                        sx = sx0;
                        w = w0;
                        sy += stripeHeight - TILE_OVERLAP - BORDER;
                        h -= stripeHeight - TILE_OVERLAP - BORDER;
//loggui4( "c1: ", sx, sy, w, h );
//loggui3( "c2: ", tx, ty, restW );
                    }
                    else //if ( h + BORDER <= stripeHeight )
                    {
//logg( "d" );
                        src->left = sx;
                        src->right = sx + w;
                        src->top = sy;
                        src->bottom = sy + h;
                        
                        trg->left = tx;
                        trg->right = tx + w;
                        trg->top = ty;
                        trg->bottom = ty + h;
                        
                        tx += w + BORDER;
                        restW -= w + BORDER;
                        
                        w = 0;
                        h = 0;
//loggui4( "d1: ", sx, sy, w, h );
//loggui3( "d2: ", tx, ty, restW );
                    }
                }
//loggui4( "src: ", src->left, src->top, src->right, src->bottom );
//loggui4( "trg: ", trg->left, trg->top, trg->right, trg->bottom );
                
                j++;
            }
            
            i++;
            
            if ( restW < BORDER + 1 )
            {
                tx = 0;
                ty += stripeHeight - BORDER;
            }
        }
    }
    
    m_nettoHeight = ( tx > 0 ) ? ty + stripeHeight : ty;
    m_height = (unsigned short)roundUpPower2( m_nettoHeight );
}

void TextureAtlas::buildAtlas( const unsigned char numSourceTextures, const unsigned char* numRectangles, const unsigned short* rectangles )
{
    unsigned char idx_t, idx_r;
    unsigned short i;
    
    short h;
    
//loggui( "numSourceTextures: ", numSourceTextures );
//for ( i = 0; i < numSourceTextures; i++ )
//    loggui2( "numRectangles: ", i, numRectangles[i] );
//loggui4( "rect0: ", rectangles[numRectangles[0] * 4 + 0], rectangles[numRectangles[0] * 4 + 1], rectangles[numRectangles[0] * 4 + 2], rectangles[numRectangles[0] * 4 + 3] );
    
    unsigned short stripeHeight = rectangles[3];
//loggui4( "yyyy: ", rectangles[0], rectangles[1], rectangles[2], rectangles[3] );
    i = 0;
    for ( idx_t = 0; idx_t < numSourceTextures; idx_t++ )
    {
//loggui( "xxxx: ", numRectangles[idx_t] );
        for ( idx_r = 0; idx_r < numRectangles[idx_t]; idx_r++ )
        {
            if ( i > 0 )
            {
                h = rectangles[i * 4 + 3];
//loggui4( "yyyy: ", rectangles[i * 4 + 0], rectangles[i * 4 + 1], rectangles[i * 4 + 2], rectangles[i * 4 + 3] );
                if ( h < stripeHeight )
                {
                    stripeHeight = h;
                }
            }
            
            i++;
        }
    }
    
//loggui( "stripeHeight: ", stripeHeight );
    stripeHeight = min( max( 50, stripeHeight ), 255 );
//loggui( "stripeHeight: ", stripeHeight );
    
    unsigned short width = 64;
    do
    {
        buildAtlas( numSourceTextures, numRectangles, rectangles, width, stripeHeight );
        width <<= 1;
    }
    while ( ( width < m_height ) || ( m_height > 4096 ) );
    
    m_justBuilt = true;
}

/*
const DWORD D3DFVF_TLVERTEX = D3DFVF_XYZRHW | D3DFVF_TEX1 | D3DFVF_TEXCOORDSIZE2(0);

struct TLVERTEX
{
    float x, y, z, rhw;
    float s, t;
};
*/
const DWORD D3DFVF_TLVERTEX = D3DFVF_XYZ | D3DFVF_TEX1 | D3DFVF_TEXCOORDSIZE2(0);

struct TLVERTEX
{
    float x, y, z;
    float s, t;
};

const float VERTEX_OFFSET_X = -0.5f;
const float VERTEX_OFFSET_Y = -0.5f;
const float TEX_COORD_PIXEL_OFFSET_LT = 0.0f;
const float TEX_COORD_PIXEL_OFFSET_RB = 0.0f;

void TextureAtlas::updateVertexBuffer( IDirect3DVertexBuffer9* vertexBuffer, const bool updateAll, char* texVisibleFlags, char* rectangleVisibleFlags, const char* isTransformed )
{
    if ( vertexBuffer == NULL )
        return;
    
    TLVERTEX* vertices;
    
    // Lock the vertex buffer
    vertexBuffer->Lock( 0, 0, (void**)&vertices, D3DLOCK_DISCARD );
    
    unsigned char idx_t, idx_r, idx_sr;
    unsigned short r, sr, v;
    bool texVis;
    RECT* rect = NULL;
    RECT* srcRect = NULL;
    RECT* trgRect = NULL;
    float px_v_off_l, px_v_off_t, px_v_off_r, px_v_off_b;
    float px_tc_off_l, px_tc_off_t, px_tc_off_r, px_tc_off_b;
    
    const float zIndexUnit = ( ONE_Z_INDEX - ZERO_Z_INDEX ) / (float)getTotalNumRectangles();
    float z;
    
    r = 0;
    sr = 0;
    v = 0;
    for ( idx_t = 0; idx_t < m_numSourceTextures; idx_t++ )
    {
        texVis = updateAll || ( idx_t > 0 ) || ( texVisibleFlags[idx_t] > 0 );
        for ( idx_r = 0; idx_r < m_numRectangles[idx_t]; idx_r++ )
        {
            z = 0.5f; //ZERO_Z_INDEX + ( (float)r * zIndexUnit );
            
            if ( updateAll || ( texVis && ( rectangleVisibleFlags[r] > 0 ) ) )
            {
                rect = m_rectangles[r];
                
                for ( idx_sr = 0; idx_sr < m_numSubRects[idx_t][idx_r]; idx_sr++ )
                {
                    srcRect = m_srcSubRects[sr];
                    trgRect = m_trgSubRects[sr];
                    
                    //if ( isTransformed[idx_t] )
                    {
                        px_v_off_l = ( srcRect->left == rect->left )     ? 0.00f : +0.50f;
                        px_v_off_t = ( srcRect->top == rect->top )       ? 0.00f : +0.50f;
                        px_v_off_r = ( srcRect->right == rect->right )   ? 0.00f : -0.50f;
                        px_v_off_b = ( srcRect->bottom == rect->bottom ) ? 0.00f : -0.50f;
                        
                        px_tc_off_l = ( srcRect->left == rect->left )     ? 0.00f : +0.50f;
                        px_tc_off_t = ( srcRect->top == rect->top )       ? 0.00f : +0.50f;
                        px_tc_off_r = ( srcRect->right == rect->right )   ? 0.00f : -0.50f;
                        px_tc_off_b = ( srcRect->bottom == rect->bottom ) ? 0.00f : -0.50f;
                    }
                    /*
                    else
                    {
                        px_v_off_l = ( srcRect->left == rect->left )     ? 0.00f : 0.00f;
                        px_v_off_t = ( srcRect->top == rect->top )       ? 0.00f : (float)TILE_OVERLAP;
                        px_v_off_r = ( srcRect->right == rect->right )   ? 0.00f : 0.00f;
                        px_v_off_b = ( srcRect->bottom == rect->bottom ) ? 0.00f : (float)TILE_OVERLAP;
                        
                        px_tc_off_l = ( srcRect->left == rect->left )     ? 0.00f : 0.00f;
                        px_tc_off_t = ( srcRect->top == rect->top )       ? 0.00f : (float)TILE_OVERLAP;
                        px_tc_off_r = ( srcRect->right == rect->right )   ? 0.00f : 0.00f;
                        px_tc_off_b = ( srcRect->bottom == rect->bottom ) ? 0.00f : (float)TILE_OVERLAP;
                    }
                    */
                    
                    vertices[v + 0].x = (float)srcRect->left + VERTEX_OFFSET_X + px_v_off_l;
                    vertices[v + 0].y = (float)srcRect->top + VERTEX_OFFSET_Y + px_v_off_t;
                    vertices[v + 0].z = z;
                    //vertices[v + 0].rhw = 1.0f;
                    vertices[v + 0].s = ( (float)trgRect->left + TEX_COORD_PIXEL_OFFSET_LT + px_tc_off_l ) / m_width;
                    vertices[v + 0].t = ( (float)trgRect->top + TEX_COORD_PIXEL_OFFSET_LT + px_tc_off_t ) / m_height;
                    
                    vertices[v + 1].x = (float)srcRect->left + VERTEX_OFFSET_X + px_v_off_l;
                    vertices[v + 1].y = (float)srcRect->bottom + VERTEX_OFFSET_Y + px_v_off_b;
                    vertices[v + 1].z = z;
                    //vertices[v + 1].rhw = 1.0f;
                    vertices[v + 1].s = ( (float)trgRect->left + TEX_COORD_PIXEL_OFFSET_LT + px_tc_off_l ) / m_width;
                    vertices[v + 1].t = ( (float)trgRect->bottom + TEX_COORD_PIXEL_OFFSET_RB + px_tc_off_b ) / m_height;
                    
                    vertices[v + 2].x = (float)srcRect->right + VERTEX_OFFSET_X + px_v_off_r;
                    vertices[v + 2].y = (float)srcRect->bottom + VERTEX_OFFSET_Y + px_v_off_b;
                    vertices[v + 2].z = z;
                    //vertices[v + 2].rhw = 1.0f;
                    vertices[v + 2].s = ( (float)trgRect->right + TEX_COORD_PIXEL_OFFSET_RB + px_tc_off_r ) / m_width;
                    vertices[v + 2].t = ( (float)trgRect->bottom + TEX_COORD_PIXEL_OFFSET_RB + px_tc_off_b ) / m_height;
                    
                    vertices[v + 3].x = (float)srcRect->left + VERTEX_OFFSET_X + px_v_off_l;
                    vertices[v + 3].y = (float)srcRect->top + VERTEX_OFFSET_Y + px_v_off_t;
                    vertices[v + 3].z = z;
                    //vertices[v + 3].rhw = 1.0f;
                    vertices[v + 3].s = ( (float)trgRect->left + TEX_COORD_PIXEL_OFFSET_LT + px_tc_off_l ) / m_width;
                    vertices[v + 3].t = ( (float)trgRect->top + TEX_COORD_PIXEL_OFFSET_LT + px_tc_off_t ) / m_height;
                    
                    vertices[v + 4].x = (float)srcRect->right + VERTEX_OFFSET_X + px_v_off_r;
                    vertices[v + 4].y = (float)srcRect->bottom + VERTEX_OFFSET_Y + px_v_off_b;
                    vertices[v + 4].z = z;
                    //vertices[v + 4].rhw = 1.0f;
                    vertices[v + 4].s = ( (float)trgRect->right + TEX_COORD_PIXEL_OFFSET_RB + px_tc_off_r ) / m_width;
                    vertices[v + 4].t = ( (float)trgRect->bottom + TEX_COORD_PIXEL_OFFSET_RB + px_tc_off_b ) / m_height;
                    
                    vertices[v + 5].x = (float)srcRect->right + VERTEX_OFFSET_X + px_v_off_r;
                    vertices[v + 5].y = (float)srcRect->top + VERTEX_OFFSET_Y + px_v_off_t;
                    vertices[v + 5].z = z;
                    //vertices[v + 5].rhw = 1.0f;
                    vertices[v + 5].s = ( (float)trgRect->right + TEX_COORD_PIXEL_OFFSET_RB + px_tc_off_r ) / m_width;
                    vertices[v + 5].t = ( (float)trgRect->top + TEX_COORD_PIXEL_OFFSET_LT + px_tc_off_t ) / m_height;
                    
                    v += 6;
                    sr++;
                }
            }
            else
            {
                sr += m_numSubRects[idx_t][idx_r];
            }
            
            r++;
        }
    }
    
#ifdef DEBUG_TEXTURE
    vertices[0].x = 0 + VERTEX_OFFSET_X;
    vertices[0].y = 150 + VERTEX_OFFSET_Y;
    vertices[0].z = ZERO_Z;
    vertices[0].s = 0;
    vertices[0].t = 0;
    
    vertices[1].x = 0 + VERTEX_OFFSET_X;
    vertices[1].y = 150 + m_height + VERTEX_OFFSET_Y;
    vertices[1].z = ZERO_Z;
    vertices[1].s = 0;
    vertices[1].t = 1;
    
    vertices[2].x = m_width + VERTEX_OFFSET_X;
    vertices[2].y = 150 + m_height + VERTEX_OFFSET_Y;
    vertices[2].z = ZERO_Z;
    vertices[2].s = 1;
    vertices[2].t = 1;
    
    vertices[3].x = 0 + VERTEX_OFFSET_X;
    vertices[3].y = 150 + VERTEX_OFFSET_Y;
    vertices[3].z = ZERO_Z;
    vertices[3].s = 0;
    vertices[3].t = 0;
    
    vertices[4].x = m_width + VERTEX_OFFSET_X;
    vertices[4].y = 150 + m_height + VERTEX_OFFSET_Y;
    vertices[4].z = ZERO_Z;
    vertices[4].s = 1;
    vertices[4].t = 1;
    
    vertices[5].x = m_width + VERTEX_OFFSET_X;
    vertices[5].y = 150 + VERTEX_OFFSET_Y;
    vertices[5].z = ZERO_Z;
    vertices[5].s = 1;
    vertices[5].t = 0;
#endif
    
    vertexBuffer->Unlock();
}

bool testCommonRect( const RECT* rect1, const RECT* rect2 )
{
    if ( rect1->right <= rect2->left )
        return ( false );
    
    if ( rect1->bottom <= rect2->top )
        return ( false );
    
    if ( rect1->left >= rect2->right )
        return ( false );
    
    if ( rect1->top >= rect2->bottom )
        return ( false );
    
    return ( true );
}

bool getCommonRect( const RECT* rect1, const RECT* rect2, RECT* commonRect )
{
    commonRect->left = max( rect1->left, rect2->left );
    commonRect->right = min( rect1->right, rect2->right );
    commonRect->top = max( rect1->top, rect2->top );
    commonRect->bottom = min( rect1->bottom, rect2->bottom );
    
    return ( ( commonRect->right - commonRect->left > 0 ) && ( commonRect->bottom - commonRect->top > 0 ) );
}

void TextureAtlas::copyDirtyRectsToTexture( const unsigned char texIndex, const unsigned short numDirtyRects, const unsigned short* dirtyRectsBuffer, const unsigned char* sourceBuffer, const unsigned int sourceTexPitch, unsigned int* destBuffer, const unsigned int trgPitch, IDirect3DTexture9* texture )
{
    if ( texture == NULL )
        return;
    
    unsigned char idx_t, idx_r, idx_sr;
    unsigned short i, j;
    const unsigned char* pSrc;
    unsigned int* pDest;
    unsigned short nRow, nPixel;
    unsigned short left, top, width, height;
    RECT dirtyRect, commonRect, commonRect2;
    unsigned char a, r, g, b;
    
    for ( unsigned short d = 0; d < numDirtyRects; d++ )
    {
        left = *(dirtyRectsBuffer++);
        top = *(dirtyRectsBuffer++);
        width = *(dirtyRectsBuffer++);
        height = *(dirtyRectsBuffer++);
        
        dirtyRect.left = left;
        dirtyRect.top = top;
        dirtyRect.right = left + width;
        dirtyRect.bottom = top + height;
        
        i = 0;
        j = 0;
        for ( idx_t = 0; idx_t < m_numSourceTextures; idx_t++ )
        {
            if ( idx_t == texIndex )
            {
                for ( idx_r = 0; idx_r < m_numRectangles[idx_t]; idx_r++ )
                {
                    if ( testCommonRect( &dirtyRect, m_rectangles[i] ) )
                    {
                        for ( idx_sr = 0; idx_sr < m_numSubRects[idx_t][idx_r]; idx_sr++ )
                        {
                            if ( getCommonRect( &dirtyRect, m_srcSubRects[j], &commonRect ) )
                            {
                                width = (unsigned short)( commonRect.right - commonRect.left );
                                height = (unsigned short)( commonRect.bottom - commonRect.top );
                                
                                commonRect2.left = m_trgSubRects[j]->left + commonRect.left - m_srcSubRects[j]->left;
                                commonRect2.top = m_trgSubRects[j]->top + commonRect.top - m_srcSubRects[j]->top;
                                commonRect2.right = m_trgSubRects[j]->right - ( m_srcSubRects[j]->right - commonRect.right );
                                commonRect2.bottom = m_trgSubRects[j]->bottom - ( m_srcSubRects[j]->bottom - commonRect.bottom );
                                
                                for ( nRow = 0; nRow < height; nRow++ )
                                {
                                    // set destination pointer for this row
                                    
                                    pSrc  = sourceBuffer + ( commonRect.top + nRow )  * sourceTexPitch + commonRect.left * 4;
                                    pDest = destBuffer   + ( commonRect2.top + nRow ) * trgPitch       + commonRect2.left;
                                    
                                    // copy the row
                                    
                                    for ( nPixel = 0; nPixel < width; nPixel++ )
                                    {
                                        // extract pixel data
                                        
                                        r = *pSrc++;
                                        g = *pSrc++;
                                        b = *pSrc++;
                                        a = *pSrc++;
                                        
                                        // write color word to texture
                                        
                                        (*pDest++) = (a << 24) | (r << 16) | (g << 8) | b;
                                    }
                                }
                                
                                if ( texture != NULL )
                                    texture->AddDirtyRect( &commonRect2 );
                            }
                            
                            j++;
                        }
                    }
                    else
                    {
                        j += m_numSubRects[idx_t][idx_r];
                    }
                    
                    i++;
                }
            }
            else
            {
                i += m_numRectangles[idx_t];
                for ( idx_r = 0; idx_r < m_numRectangles[idx_t]; idx_r++ )
                {
                    j += m_numSubRects[idx_t][idx_r];
                }
            }
        }
    }
}

const unsigned short* OverlayTextureManagerImpl::getDirtyRectsBuffer( unsigned short** dirtyRectsBuffers, const unsigned char texIndex )
{
    if ( m_completeTextureUpdateForced )
    {
        m_completeDirtyRect[0] = 1;
        
        m_completeDirtyRect[1] = 0;
        m_completeDirtyRect[2] = 0;
        m_completeDirtyRect[3] = m_sourceTexWidths[texIndex];
        m_completeDirtyRect[4] = m_sourceTexHeights[texIndex];
        
        return ( m_completeDirtyRect );
    }
    
    return ( dirtyRectsBuffers[texIndex] );
}

void OverlayTextureManagerImpl::copyDirtyRectsToTexture( const unsigned char numTextures, unsigned short** dirtyRectsBuffers, PixelBufferCallback* pixBuffCallback )
{
    RECT usedRect;
    
    usedRect.left = 0;
    usedRect.top = 0;
    usedRect.right = usedRect.left + m_atlas->getWidth();
    usedRect.bottom = usedRect.top + m_atlas->getNettoHeight();
    
    D3DLOCKED_RECT lockedRect;
    
    IDirect3DTexture9* texture = ( m_proxyTexture != NULL ) ? m_proxyTexture : m_overlayTexture;
    
    if ( ( texture != NULL ) && ( SUCCEEDED( texture->LockRect( 0, &lockedRect, &usedRect, D3DLOCK_NO_DIRTY_UPDATE ) ) ) )
    {
        const unsigned short* dirtyRectsBuffer;
        unsigned short numDirtyRects;
        unsigned char* sourceBuffer;
        void* userObject = NULL;
        
        for ( unsigned char i = 0; i < numTextures; i++ )
        {
            dirtyRectsBuffer = getDirtyRectsBuffer( dirtyRectsBuffers, i );
            numDirtyRects = *dirtyRectsBuffer++;
            
            if ( numDirtyRects > 0 )
            {
                sourceBuffer = pixBuffCallback->getPixelBuffer( i, &userObject );
                
                m_atlas->copyDirtyRectsToTexture( i, numDirtyRects, dirtyRectsBuffer, sourceBuffer, m_sourceTexPitches[i], (unsigned int*)lockedRect.pBits, lockedRect.Pitch >> 2, texture );
                
                pixBuffCallback->releasePixelBuffer( i, sourceBuffer, userObject );
                sourceBuffer = NULL;
                userObject = NULL;
            }
        }
        
        texture->UnlockRect( 0 );
        
        if ( m_proxyTexture != NULL )
            m_device->UpdateTexture( m_proxyTexture, m_overlayTexture );
    }
}

char handleVisibleFlag( char* visibleFlags, const unsigned short i )
{
    if ( visibleFlags == NULL )
        return ( 1 );
    
    char result = visibleFlags[i];
    
    if ( result == -1 )
    {
        // made invisible
        visibleFlags[i] = 0;
    }
    else if ( result == +2 )
    {
        // made visible
        visibleFlags[i] = 1;
    }
    
    return ( result );
}

bool checkVisibleFlags( char* texVisibleFlags, const unsigned char numTextures, const char* isTransformed, char* rectangleVisibleFlags, const unsigned char* numRectangles )
{
    unsigned short i = 0;
    bool result = false;
    char b;
    
    for ( unsigned char idx_t = 0; idx_t < numTextures; idx_t++ )
    {
        b = handleVisibleFlag( texVisibleFlags, idx_t );
        //if ( ( idx_t == 0 ) && ( ( b == -1 ) || ( b == +2 ) ) )
        if ( ( isTransformed[idx_t] == 0 ) && ( ( b == -1 ) || ( b == +2 ) ) )
            result = true;
        
        for ( unsigned char idx_r = 0; idx_r < numRectangles[idx_t]; idx_r++ )
        {
            b = handleVisibleFlag( rectangleVisibleFlags, i );
            //if ( ( idx_t == 0 ) && ( ( b == -1 ) || ( b == +2 ) ) )
	        if ( ( isTransformed[idx_t] == 0 ) && ( ( b == -1 ) || ( b == +2 ) ) )
                result = true;
            
            i++;
        }
    }
    
    return ( result );
}

void composeMatrix( const float tx, const float ty, const float tz, const float rcx, const float rcy, const float r, const float sx, const float sy, D3DXMATRIX* m )
{
    const float sinr = sin( r );
    const float cosr = cos( r );
    
    m->_11 =  cosr * sx;                           m->_12 = sinr * sx;                             m->_13 = 0.0f;  m->_14 = 0.0f;
    m->_21 = -sinr * sy;                           m->_22 = cosr * sy;                             m->_23 = 0.0f;  m->_24 = 0.0f;
    m->_31 = 0.0f;                                 m->_32 = 0.0f;                                  m->_33 = 1.0f;  m->_34 = 0.0f;
    m->_41 = cosr * -rcx + sinr * rcy + rcx + tx;  m->_42 = sinr * -rcx + cosr * -rcy + rcy + ty;  m->_43 = tz;    m->_44 = 1.0f;
}

void composeMatrix( const float tx, const float ty, const float tz, const float sx, const float sy, D3DXMATRIX* m )
{
    m->_11 = sx;    m->_12 = 0.0f;  m->_13 = 0.0f;   m->_14 = 0.0f;
    m->_21 = 0.0f;  m->_22 = sy;    m->_23 = 0.0f;   m->_24 = 0.0f;
    m->_31 = 0.0f;  m->_32 = 0.0f;  m->_33 = 1.0f;   m->_34 = 0.0f;
    m->_41 = tx;    m->_42 = ty;    m->_43 = tz;     m->_44 = 1.0f;
}

static const unsigned char TRANSFORM_FLAG_TRANSFORMED = 1;
static const unsigned char TRANSFORM_FLAG_TRANSLATION = 2;
static const unsigned char TRANSFORM_FLAG_ROTATION    = 4;
static const unsigned char TRANSFORM_FLAG_SCALE       = 8;

void TextureAtlas::render( LPDIRECT3DDEVICE9 device, IDirect3DTexture9* overlayTexture, IDirect3DVertexBuffer9* vertexBuffer, const bool updateAll, const float postScaleX, const float postScaleY, char* texVisibleFlags, char* rectangleVisibleFlags, const char* isTransformed, const float* translations, const unsigned short* rotCenters, const float* rotations, const float* scales, const unsigned short* clipRects )
{
    if ( ( overlayTexture == NULL ) || ( vertexBuffer == NULL ) )
        return;
    
    //IDirect3DSurface9* backBuffer;
    //device->GetBackBuffer( 0, 0, D3DBACKBUFFER_TYPE_MONO, &backBuffer );
    //device->SetRenderTarget( 0, backBuffer );
    
    //device->BeginStateBlock();
    
    
    device->SetTextureStageState( 0, D3DTSS_COLOROP, D3DTOP_SELECTARG1 );
    device->SetTextureStageState( 0, D3DTSS_COLORARG1, D3DTA_TEXTURE );
    //device->SetTextureStageState( 0, D3DTSS_COLORARG2, D3DTA_DIFFUSE );   //Ignored
    device->SetTextureStageState( 0, D3DTSS_ALPHAOP, D3DTOP_SELECTARG1 );
    device->SetTextureStageState( 0, D3DTSS_ALPHAARG1, D3DTA_TEXTURE );
    
    
    device->SetVertexShader( NULL );
    device->SetPixelShader( NULL );
    device->SetRenderState( D3DRS_LIGHTING, FALSE );
    device->SetRenderState( D3DRS_ALPHABLENDENABLE, TRUE );
    device->SetRenderState( D3DRS_SRCBLEND, D3DBLEND_SRCALPHA );
    device->SetRenderState( D3DRS_DESTBLEND, D3DBLEND_INVSRCALPHA );
    device->SetRenderState( D3DRS_ZENABLE, D3DZB_FALSE );
    device->SetTextureStageState( 0, D3DTSS_ALPHAOP, D3DTOP_MODULATE );
    device->SetTextureStageState( 0, D3DTSS_COLOROP, D3DTOP_MODULATE );
    
    //IDirect3DStateBlock9* stateBlock = NULL;
    //device->EndStateBlock( &stateBlock );
    
    device->SetTexture( 0, overlayTexture );
    
    device->SetFVF( D3DFVF_TLVERTEX );
    
    device->SetTransform( D3DTS_PROJECTION, &m_projMatrix );
    
    D3DXMatrixIdentity( &m_worldMatrix );
    device->SetTransform( D3DTS_VIEW, &m_worldMatrix );
    device->SetTransform( D3DTS_WORLD, &m_worldMatrix );
    
    D3DXMatrixIdentity( &m_texMatrix );
    device->SetTransform( D3DTS_TEXTURE0, &m_texMatrix );
    
    if ( checkVisibleFlags( texVisibleFlags, m_numSourceTextures, isTransformed, rectangleVisibleFlags, m_numRectangles ) || m_justBuilt )
        updateVertexBuffer( vertexBuffer, updateAll, texVisibleFlags, rectangleVisibleFlags, isTransformed );
    
    m_justBuilt = false;
    
    bool isClipping = false;
    
    if ( SUCCEEDED( device->SetStreamSource( 0, vertexBuffer, 0, sizeof( TLVERTEX ) ) ) )
    {
#ifndef DEBUG_TEXTURE
        RECT clipRect;
        unsigned char idx_t, idx_r;
        unsigned short r, numSubRects, vertexOffset;
        bool texVis;
        
        r = 0;
        vertexOffset = 0;
        for ( idx_t = 0; idx_t < m_numSourceTextures; idx_t++ )
        {
            if ( isTransformed[idx_t] == 0 )
            {
                texVis = true;
                
                if ( ( postScaleX == 0.0f ) && ( postScaleY == 0.0f ) )
                {
                    D3DXMatrixIdentity( &m_worldMatrix );
                    device->SetTransform( D3DTS_WORLD, &m_worldMatrix );
                }
                else
                {
                    D3DXMatrixScaling( &m_postScaleMatrix, postScaleX, postScaleY, 1.0f );
                    device->SetTransform( D3DTS_WORLD, &m_postScaleMatrix );
                }
                
                //D3DXMatrixIdentity( &m_texMatrix );
                //device->SetTransform( D3DTS_TEXTURE0, &m_texMatrix );
                
                if ( isClipping )
                {
                    device->SetRenderState( D3DRS_SCISSORTESTENABLE, FALSE );
                    isClipping = false;
                }
            }
            else
            {
                texVis = ( ( texVisibleFlags[idx_t] > 0 ) && ( rectangleVisibleFlags[r] > 0 ) );
                
                if ( texVis )
                {
                    const float tx = translations[idx_t * 2 + 0];
                    const float ty = translations[idx_t * 2 + 1];
                    if ( ( isTransformed[idx_t] & TRANSFORM_FLAG_ROTATION ) != 0 )
                    {
                        composeMatrix( tx, ty, 0.0f,
                                       (float)rotCenters[idx_t * 2 + 0], (float)rotCenters[idx_t * 2 + 1],
                                       rotations[idx_t],
                                       scales[idx_t * 2 + 0], scales[idx_t * 2 + 1],
                                       &m_worldMatrix
                                     );
                    }
                    else
                    {
                        composeMatrix( tx, ty, 0.0f,
                                       scales[idx_t * 2 + 0], scales[idx_t * 2 + 1],
                                       &m_worldMatrix
                                     );
                    }
                    
                    if ( ( postScaleX != 0.0f ) || ( postScaleY != 0.0f ) )
                    {
                        D3DXMatrixScaling( &m_postScaleMatrix, postScaleX, postScaleY, 1.0f );
                        device->SetTransform( D3DTS_WORLD, &m_postScaleMatrix );
                        
                        memcpy( &m_texMatrix, &m_worldMatrix, sizeof( D3DXMATRIX ) );
                        //D3DXMatrixMultiply( &m_worldMatrix, &m_postScaleMatrix, &m_texMatrix );
                        D3DXMatrixMultiply( &m_worldMatrix, &m_texMatrix, &m_postScaleMatrix );
                    }
                    
                    device->SetTransform( D3DTS_WORLD, &m_worldMatrix );
                    
                    clipRect.left = clipRects[idx_t * 4 + 0];
                    clipRect.top = clipRects[idx_t * 4 + 1];
                    clipRect.right = clipRect.left + clipRects[idx_t * 4 + 2];
                    clipRect.bottom = clipRect.top + clipRects[idx_t * 4 + 3];
                    if ( ( clipRect.right - clipRect.left > 0 ) && ( clipRect.bottom - clipRect.top > 0 ) )
                    {
                        clipRect.left = (short)floor( ( clipRect.left + tx ) * postScaleX );
                        clipRect.right = (short)ceil( ( clipRect.right + tx ) * postScaleX );
                        clipRect.top = (short)floor( ( clipRect.top + ty ) * postScaleY );
                        clipRect.bottom = (short)ceil( ( clipRect.bottom + ty ) * postScaleY );
                        device->SetScissorRect( &clipRect );
                        if ( !isClipping )
                            device->SetRenderState( D3DRS_SCISSORTESTENABLE, TRUE );
                        isClipping = true;
                    }
                    else if ( isClipping )
                    {
                        device->SetRenderState( D3DRS_SCISSORTESTENABLE, FALSE );
                        isClipping = false;
                    }
                }
            }
            
            numSubRects = 0;
            for ( idx_r = 0; idx_r < m_numRectangles[idx_t]; idx_r++ )
            {
                //if ( ( idx_t > 0 ) || ( texVis && ( rectangleVisibleFlags[r] > 0 ) ) )
                if ( ( isTransformed[idx_t] != 0 ) || ( texVis && ( rectangleVisibleFlags[r] > 0 ) ) )
                {
                    numSubRects += m_numSubRects[idx_t][idx_r];
                }
                
                r++;
            }
            
            if ( texVis )
                device->DrawPrimitive( D3DPT_TRIANGLELIST, vertexOffset, numSubRects * 2 );
            
            vertexOffset += numSubRects * 6;
        }
#else
        device->DrawPrimitive( D3DPT_TRIANGLELIST, 0, 2 );
#endif
    }
    
    //stateBlock->Apply();
    
    device->SetRenderState( D3DRS_ZENABLE, D3DZB_TRUE );
    
    if ( isClipping )
    {
        device->SetRenderState( D3DRS_SCISSORTESTENABLE, FALSE );
        isClipping = false;
    }
}

void OverlayTextureManagerImpl::render( const float postScaleX, const float postScaleY, const unsigned char numTextures, unsigned short** dirtyRectsBuffers, PixelBufferCallback* pixBuffCallback, char* visibleFlags, char* rectangleVisibleFlags, const char* isTransformed, const float* translations, const unsigned short* rotCenters, const float* rotations, const float* scales, const unsigned short* clipRects )
{
    bool hasDirtyRects = false;
    
    if ( m_completeTextureUpdateForced )
    {
        hasDirtyRects = true;
    }
    else
    {
        // check if one of the textures has at least one dirty rect...
        for ( unsigned char i = 0; i < numTextures; i++ )
        {
            if ( *(dirtyRectsBuffers[i]) > 0 )
            {
                hasDirtyRects = true;
                break;
            }
        }
    }
    
    if ( hasDirtyRects )
    {
        copyDirtyRectsToTexture( numTextures, dirtyRectsBuffers, pixBuffCallback );
    }
    
    bool updateAll = m_completeTextureUpdateForced;
    m_completeTextureUpdateForced = false;
    
    if ( m_overlayTexture != NULL )
    {
        m_atlas->render( m_device, m_overlayTexture, m_vertexBuffer, updateAll, postScaleX, postScaleY, visibleFlags, rectangleVisibleFlags, isTransformed, translations, rotCenters, rotations, scales, clipRects );
    }
}

void clearTexture( IDirect3DTexture9* texture, const unsigned short width, const unsigned short height )
{
    D3DLOCKED_RECT lockedRect;
    
    if ( ( texture != NULL ) && ( SUCCEEDED( texture->LockRect( 0, &lockedRect, NULL, 0 ) ) ) )
    {
        //logg( "zeroing out texture" );
        unsigned int* pixels = (unsigned int*)lockedRect.pBits;
        const unsigned int numPixels = width * height;
        //ZeroMemory( pixels, numPixels );
        for ( unsigned int i = 0; i < numPixels; i++ )
            *(pixels++) = 0;
        
        texture->UnlockRect( 0 );
    }
}

IDirect3DTexture9* createTexture( const LPDIRECT3DDEVICE9 device, const unsigned short width, const unsigned short height, const bool useGraphicsMem )
{
    if ( useGraphicsMem )
        logg( "    (Re)Creating overlay texture... ", false );
    else
        logg( "    (Re)Creating proxy texture... ", false );
    
    IDirect3DTexture9* texture = NULL;
    
    unsigned int result = 0;
    if ( useGraphicsMem )
        result = D3DXCreateTexture( device, width, height, 1, D3DUSAGE_DYNAMIC, D3DFMT_A8R8G8B8, D3DPOOL_DEFAULT, &texture );
    else
        result = D3DXCreateTexture( device, width, height, 1, D3DUSAGE_DYNAMIC, D3DFMT_A8R8G8B8, D3DPOOL_SYSTEMMEM, &texture );
    
    switch ( result )
    {
        case D3D_OK:
            logg( "Texture created successfully.", true );
            break;
        case D3DERR_INVALIDCALL:
            logg( "Texture creation failed (D3DERR_INVALIDCALL)", true );
            break;
        case D3DERR_NOTAVAILABLE:
            logg( "Texture creation failed (D3DERR_NOTAVAILABLE)", true );
            break;
        case D3DERR_OUTOFVIDEOMEMORY:
            logg( "Texture creation failed (D3DERR_OUTOFVIDEOMEMORY). Choose less or smaller Widgets.", true );
            break;
        case E_OUTOFMEMORY:
            logg( "Texture creation failed (E_OUTOFMEMORY). Choose less or smaller Widgets.", true );
            break;
        default:
            logg( "Texture creation failed (Unknown return value)", true );
            break;
    }
    
    if ( !useGraphicsMem )
    {
        clearTexture( texture, width, height );
    }
    
    return ( texture );
}

IDirect3DVertexBuffer9* createVertexBuffer( LPDIRECT3DDEVICE9 device, TextureAtlas* atlas )
{
    IDirect3DVertexBuffer9* vertexBuffer = NULL;
    device->CreateVertexBuffer( sizeof( TLVERTEX ) * 6 * atlas->getTotalNumSubRects(), NULL, D3DFVF_TLVERTEX, D3DPOOL_MANAGED, &vertexBuffer, NULL );
    
    //atlas->updateVertexBuffer( vertexBuffer, NULL, NULL );
    
    return ( vertexBuffer );
}

bool OverlayTextureManagerImpl::setupD3DTexture( const unsigned short width, const unsigned short height, const bool forceCompleteUpdate )
{
    //__int64 t0 = getSystemMicroTime();

    if ( ( width != m_texWidth ) || ( height != m_texHeight ) )
    {
        releaseInternal( true, false );
        
        m_texWidth = width;
        m_texHeight = height;
    }
    else
    {
        if ( m_useProxyTexture )
        {
            if ( m_proxyTexture != NULL )
            {
                clearTexture( m_proxyTexture, m_texWidth, m_texHeight );
            }
        }
        else if ( m_overlayTexture != NULL )
        {
            clearTexture( m_overlayTexture, m_texWidth, m_texHeight );
        }
    }
    
    if ( m_overlayTexture == NULL )
    {
        m_overlayTexture = createTexture( m_device, m_texWidth, m_texHeight, true );
        
        if ( m_overlayTexture == NULL )
            return ( false );
        
        m_completeTextureUpdateForced = true;
    }
    
    if ( m_overlayTexture != NULL )
    {
        if ( m_useProxyTexture )
        {
            if ( m_proxyTexture == NULL )
            {
                m_proxyTexture = createTexture( m_device, m_texWidth, m_texHeight, false );
                
                if ( m_proxyTexture == NULL )
                    return ( false );
                
                m_completeTextureUpdateForced = true;
            }
        }
        else if ( m_proxyTexture != NULL )
        {
            m_proxyTexture->Release();
            m_proxyTexture = NULL;
        }
        
        if ( ( m_overlayTexture != NULL ) && ( !m_useProxyTexture || ( m_proxyTexture != NULL ) ) )
        {
            if ( m_vertexBuffer == NULL )
            {
                m_vertexBuffer = createVertexBuffer( m_device, m_atlas );
                
                if ( m_vertexBuffer == NULL )
                    return ( false );
            }
            
            m_completeTextureUpdateForced = m_completeTextureUpdateForced || forceCompleteUpdate;
            
            if ( m_useProxyTexture )
            {
                m_device->UpdateTexture( m_proxyTexture, m_overlayTexture );
            }
        }
    }
    else if ( m_proxyTexture != NULL )
    {
        m_proxyTexture->Release();
        m_proxyTexture = NULL;
    }
    
    //__int64 t1 = getSystemMicroTime();
    //loggDTs( "setupTextures took ", t0, t1 );
    
    return ( true );
}

bool OverlayTextureManagerImpl::setupTextures( const unsigned char numTextures, const unsigned short* textureSizes, const unsigned char* numRectangles, const unsigned short* rectangles )
{
    logg( "Building texture atlas... ", false );
    //__int64 t0 = getSystemMicroTime();
    m_atlas->buildAtlas( numTextures, numRectangles, rectangles );
    //__int64 t1 = getSystemMicroTime();
    loggui2( "done. (", m_atlas->getWidth(), m_atlas->getHeight(), false );
    logg( ")", true );
    //loggDTs( ", took ", t0, t1 );
    
    if ( m_vertexBuffer != NULL )
    {
        m_vertexBuffer->Release();
        m_vertexBuffer = NULL;
    }
	
    for ( unsigned char i = 0; i < numTextures; i++ )
    {
        m_sourceTexWidths[i] = *textureSizes++;
        m_sourceTexHeights[i] = *textureSizes++;
        m_sourceTexPitches[i] = (unsigned int)m_sourceTexWidths[i] * 4;
    }
    
    return ( setupD3DTexture( m_atlas->getWidth(), m_atlas->getHeight(), true ) );
}

void OverlayTextureManagerImpl::releaseInternal( const bool intern, const bool released )
{
    if ( ( m_proxyTexture == NULL ) && ( m_overlayTexture == NULL ) && ( m_vertexBuffer == NULL ) )
        return;
    
    if ( intern )
    {
        logg( "Releasing textures..." );
    }
    else
    {
        if ( released )
            logg( "Releasing D3D Device (releasing textures)..." );
        else
            logg( "Resetting D3D Device (releasing textures)..." );
    }
    
    if ( m_proxyTexture != NULL )
    {
        logg( "    Releasing proxy texture... ", false );
        m_proxyTexture->Release();
        m_proxyTexture = NULL;
        logg( "done.", true );
    }
    
    if ( m_overlayTexture != NULL )
    {
        logg( "    Releasing overlay texture... ", false );
        m_device->SetTexture( 0, NULL );
        m_overlayTexture->Release();
        m_overlayTexture = NULL;
        logg( "done.", true );
    }
    
    if ( m_vertexBuffer != NULL )
    {
        logg( "    Releasing vertex buffer... ", false );
        m_device->SetStreamSource( 0, NULL, 0, 0 );
        m_vertexBuffer->Release();
        m_vertexBuffer = NULL;
        logg( "done.", true );
    }
    
    logg( "Successfully released textures." );
}
