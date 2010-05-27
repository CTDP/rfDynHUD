/*	Direct3D9 Device */

#include "d3d9dev.h"

#include "d3d9.h"
#include "d3dx9core.h"

#include "overlay_texture.h"
#include "logging.h"

//#define D3DHOOK_TEXTURES // comment this to disable texture hooking

OverlayTextureManagerImpl* overlayTextureManager = NULL;
D3DManager* manager = NULL;

unsigned int device_count = 0;

unsigned short* viewport0 = (unsigned short*)malloc( 4 * sizeof( unsigned short ) );
unsigned short* viewport = (unsigned short*)malloc( 4 * sizeof( unsigned short ) );

hkIDirect3DDevice9::hkIDirect3DDevice9( IDirect3DDevice9** ppReturnedDeviceInterface, D3DPRESENT_PARAMETERS* pPresentParam, IDirect3D9* pIDirect3D9 )
{
    if ( ++device_count == 1 )
    {
        resX = (unsigned short)pPresentParam->BackBufferWidth;
        resY = (unsigned short)pPresentParam->BackBufferHeight;
        
	    viewport0[0] = 0;
	    viewport0[1] = 0;
	    viewport0[2] = resX;
	    viewport0[3] = resY;
        
        m_pD3Ddev = *ppReturnedDeviceInterface; 
	    *ppReturnedDeviceInterface = this;
	    m_pD3Dint = pIDirect3D9;
        
	    manager = new D3DManager();
        
        colorDepth = 0;
        switch ( pPresentParam->BackBufferFormat )
        {
            case D3DFMT_A2R10G10B10:
            case D3DFMT_A8R8G8B8:
            case D3DFMT_X8R8G8B8:
                colorDepth = 32;
                break;
            case D3DFMT_A1R5G5B5:
            case D3DFMT_X1R5G5B5:
            case D3DFMT_R5G6B5:
                colorDepth = 16;
                break;
        }
        
        
        overlayTextureManager = new OverlayTextureManagerImpl( m_pD3Ddev, resX, resY );
        
        manager->initialize( m_pD3Ddev, resX, resY, colorDepth, pPresentParam->Windowed == TRUE, (unsigned short)pPresentParam->FullScreen_RefreshRateInHz, pPresentParam->hDeviceWindow, overlayTextureManager );
    }
    
	m_refCount = 1;
}

unsigned int lastNumScenes = 1;
unsigned int currNumScenes = 0;
bool viewportSet = false;

HRESULT APIENTRY hkIDirect3DDevice9::BeginScene() 
{
    viewportSet = false;
    
    return ( m_pD3Ddev->BeginScene() );
}

HRESULT APIENTRY hkIDirect3DDevice9::EndScene()
{
    //if ( ++currNumScenes == lastNumScenes )
    if ( ++currNumScenes == 1 )
    {
        manager->renderOverlay( m_pD3Ddev, resX, resY, viewportSet ? viewport : viewport0, colorDepth, overlayTextureManager );
    }
    
    return ( m_pD3Ddev->EndScene() );
}

HRESULT APIENTRY hkIDirect3DDevice9::Present( CONST RECT* pSourceRect, CONST RECT* pDestRect, HWND hDestWindowOverride, CONST RGNDATA* pDirtyRegion )
{
    /*
    if ( !overlayRendered )
    {
        //loggui2( "overlayRendered delayed: ", lastNumScenes, currNumScenes );
        m_pD3Ddev->BeginScene();
        manager->renderOverlay( m_pD3Ddev, resX, resY, colorDepth, overlayTextureManager );
        m_pD3Ddev->EndScene();
    }
    */
    
    lastNumScenes = currNumScenes;
    currNumScenes = 0;
    return ( m_pD3Ddev->Present( pSourceRect, pDestRect, hDestWindowOverride, pDirtyRegion ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::Reset( D3DPRESENT_PARAMETERS* pPresentParam )
{
    overlayTextureManager->release( false );
    manager->preReset( m_pD3Ddev );
    
    HRESULT hRet = m_pD3Ddev->Reset( pPresentParam );
    
    if ( SUCCEEDED( hRet ) )
    {
        resX = (unsigned short)pPresentParam->BackBufferWidth;
        resY = (unsigned short)pPresentParam->BackBufferHeight;
        
        colorDepth = 0;
        switch ( pPresentParam->BackBufferFormat )
        {
            case D3DFMT_A2R10G10B10:
            case D3DFMT_A8R8G8B8:
            case D3DFMT_X8R8G8B8:
                colorDepth = 32;
                break;
            case D3DFMT_A1R5G5B5:
            case D3DFMT_X1R5G5B5:
            case D3DFMT_R5G6B5:
                colorDepth = 16;
                break;
        }
        
        manager->postReset( m_pD3Ddev, resX, resY, colorDepth, pPresentParam->Windowed == TRUE, (unsigned short)pPresentParam->FullScreen_RefreshRateInHz, pPresentParam->hDeviceWindow );
    }
    
    return ( hRet );
}

ULONG APIENTRY hkIDirect3DDevice9::Release()
{
    if ( --m_refCount == 0 )
    {
        overlayTextureManager->release( true );
        delete( overlayTextureManager );
        overlayTextureManager = NULL;
        manager->release( m_pD3Ddev );
        delete( manager );
        manager = NULL;
    }
    
    return ( m_pD3Ddev->Release() );
}

HRESULT APIENTRY hkIDirect3DDevice9::QueryInterface( REFIID riid, LPVOID* ppvObj )
{
    return ( m_pD3Ddev->QueryInterface( riid, ppvObj ) );
    /*
    *ppvObj = NULL;
    
	// call this to increase AddRef at original object
	// and to check if such an interface is there
    
	HRESULT hRes = m_pD3Ddev->QueryInterface( riid, ppvObj ); 
    
	if ( hRes == NOERROR ) // if OK, send our "fake" address
	{
		*ppvObj = this;
	}
	
	return ( hRes );
    */
}

HRESULT APIENTRY TransmitData( int number )
{
    return ( S_OK );
}

ULONG APIENTRY hkIDirect3DDevice9::AddRef()
{
    m_refCount++;
    
    return ( m_pD3Ddev->AddRef() );
}

HRESULT APIENTRY hkIDirect3DDevice9::BeginStateBlock()
{
    return ( m_pD3Ddev->BeginStateBlock() );
}

HRESULT APIENTRY hkIDirect3DDevice9::Clear( DWORD Count, CONST D3DRECT* pRects, DWORD Flags, D3DCOLOR Color, float Z, DWORD Stencil )
{
    return ( m_pD3Ddev->Clear( Count, pRects, Flags, Color, Z, Stencil ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::ColorFill( IDirect3DSurface9* pSurface, CONST RECT* pRect, D3DCOLOR color )
{    
    return ( m_pD3Ddev->ColorFill( pSurface, pRect, color ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateAdditionalSwapChain( D3DPRESENT_PARAMETERS* pPresentationParameters, IDirect3DSwapChain9** ppSwapChain )
{
    return ( m_pD3Ddev->CreateAdditionalSwapChain( pPresentationParameters, ppSwapChain ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateCubeTexture( UINT EdgeLength, UINT Levels, DWORD Usage, D3DFORMAT Format, D3DPOOL Pool, IDirect3DCubeTexture9** ppCubeTexture, HANDLE* pSharedHandle )
{
    return ( m_pD3Ddev->CreateCubeTexture( EdgeLength, Levels, Usage, Format, Pool, ppCubeTexture, pSharedHandle ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateDepthStencilSurface( UINT Width, UINT Height, D3DFORMAT Format, D3DMULTISAMPLE_TYPE MultiSample, DWORD MultisampleQuality, BOOL Discard, IDirect3DSurface9** ppSurface, HANDLE* pSharedHandle )
{
    return ( m_pD3Ddev->CreateDepthStencilSurface( Width, Height, Format, MultiSample, MultisampleQuality, Discard, ppSurface, pSharedHandle ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateIndexBuffer( UINT Length, DWORD Usage, D3DFORMAT Format, D3DPOOL Pool, IDirect3DIndexBuffer9** ppIndexBuffer, HANDLE* pSharedHandle )
{
    return ( m_pD3Ddev->CreateIndexBuffer( Length, Usage, Format, Pool, ppIndexBuffer,pSharedHandle ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateOffscreenPlainSurface( UINT Width, UINT Height, D3DFORMAT Format, D3DPOOL Pool, IDirect3DSurface9** ppSurface, HANDLE* pSharedHandle )
{
    return ( m_pD3Ddev->CreateOffscreenPlainSurface( Width, Height, Format, Pool, ppSurface, pSharedHandle ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreatePixelShader( CONST DWORD* pFunction, IDirect3DPixelShader9** ppShader )
{
    return ( m_pD3Ddev->CreatePixelShader( pFunction, ppShader ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateQuery( D3DQUERYTYPE Type, IDirect3DQuery9** ppQuery )
{
    return ( m_pD3Ddev->CreateQuery( Type, ppQuery ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateRenderTarget( UINT Width, UINT Height, D3DFORMAT Format, D3DMULTISAMPLE_TYPE MultiSample, DWORD MultisampleQuality, BOOL Lockable, IDirect3DSurface9** ppSurface, HANDLE* pSharedHandle )
{
    return ( m_pD3Ddev->CreateRenderTarget( Width, Height, Format, MultiSample, MultisampleQuality, Lockable, ppSurface, pSharedHandle ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateStateBlock( D3DSTATEBLOCKTYPE Type, IDirect3DStateBlock9** ppSB )
{
    return ( m_pD3Ddev->CreateStateBlock( Type, ppSB ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateTexture( UINT Width, UINT Height, UINT Levels, DWORD Usage, D3DFORMAT Format, D3DPOOL Pool, IDirect3DTexture9** ppTexture, HANDLE* pSharedHandle )
{
    HRESULT ret = m_pD3Ddev->CreateTexture( Width, Height, Levels, Usage, Format, Pool, ppTexture, pSharedHandle );

#ifdef D3DHOOK_TEXTURES
    if ( ret == D3D_OK )
    {
        // TODO: Why is this instance stored nowhere?
        new hkIDirect3DTexture9( ppTexture, this, Width, Height, Format );
    }
#endif
    
    return ( ret );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateVertexBuffer( UINT Length, DWORD Usage, DWORD FVF, D3DPOOL Pool, IDirect3DVertexBuffer9** ppVertexBuffer, HANDLE* pSharedHandle )
{
    return ( m_pD3Ddev->CreateVertexBuffer( Length, Usage, FVF, Pool, ppVertexBuffer, pSharedHandle ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateVertexDeclaration( CONST D3DVERTEXELEMENT9* pVertexElements, IDirect3DVertexDeclaration9** ppDecl )
{
    return ( m_pD3Ddev->CreateVertexDeclaration( pVertexElements, ppDecl ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateVertexShader( CONST DWORD* pFunction, IDirect3DVertexShader9** ppShader )
{
    return ( m_pD3Ddev->CreateVertexShader( pFunction, ppShader ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::CreateVolumeTexture( UINT Width, UINT Height, UINT Depth, UINT Levels, DWORD Usage, D3DFORMAT Format, D3DPOOL Pool, IDirect3DVolumeTexture9** ppVolumeTexture, HANDLE* pSharedHandle )
{
    return ( m_pD3Ddev->CreateVolumeTexture( Width, Height, Depth, Levels, Usage, Format, Pool, ppVolumeTexture, pSharedHandle ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::DeletePatch( UINT Handle )
{
    return ( m_pD3Ddev->DeletePatch( Handle) );
}

HRESULT APIENTRY hkIDirect3DDevice9::DrawIndexedPrimitive( D3DPRIMITIVETYPE Type, INT BaseVertexIndex, UINT MinVertexIndex, UINT NumVertices, UINT startIndex, UINT primCount )
{
    return ( m_pD3Ddev->DrawIndexedPrimitive( Type, BaseVertexIndex, MinVertexIndex, NumVertices, startIndex, primCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::DrawIndexedPrimitiveUP( D3DPRIMITIVETYPE PrimitiveType, UINT MinIndex, UINT NumVertices, UINT PrimitiveCount, CONST void* pIndexData, D3DFORMAT IndexDataFormat, CONST void* pVertexStreamZeroData, UINT VertexStreamZeroStride )
{    
    return ( m_pD3Ddev->DrawIndexedPrimitiveUP( PrimitiveType, MinIndex, NumVertices, PrimitiveCount, pIndexData, IndexDataFormat, pVertexStreamZeroData, VertexStreamZeroStride ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::DrawPrimitive( D3DPRIMITIVETYPE PrimitiveType, UINT StartVertex, UINT PrimitiveCount )
{
    return ( m_pD3Ddev->DrawPrimitive( PrimitiveType, StartVertex, PrimitiveCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::DrawPrimitiveUP( D3DPRIMITIVETYPE PrimitiveType, UINT PrimitiveCount, CONST void* pVertexStreamZeroData, UINT VertexStreamZeroStride )
{
    return ( m_pD3Ddev->DrawPrimitiveUP( PrimitiveType, PrimitiveCount, pVertexStreamZeroData, VertexStreamZeroStride ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::DrawRectPatch( UINT Handle, CONST float* pNumSegs, CONST D3DRECTPATCH_INFO* pRectPatchInfo )
{
    return ( m_pD3Ddev->DrawRectPatch( Handle, pNumSegs, pRectPatchInfo ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::DrawTriPatch( UINT Handle, CONST float* pNumSegs, CONST D3DTRIPATCH_INFO* pTriPatchInfo )
{
    return ( m_pD3Ddev->DrawTriPatch( Handle, pNumSegs, pTriPatchInfo ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::EndStateBlock( IDirect3DStateBlock9** ppSB )
{
    return ( m_pD3Ddev->EndStateBlock( ppSB ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::EvictManagedResources() 
{
    return ( m_pD3Ddev->EvictManagedResources() );
}

UINT APIENTRY hkIDirect3DDevice9::GetAvailableTextureMem()
{
    return ( m_pD3Ddev->GetAvailableTextureMem() );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetBackBuffer( UINT iSwapChain, UINT iBackBuffer, D3DBACKBUFFER_TYPE Type, IDirect3DSurface9** ppBackBuffer )
{
    return ( m_pD3Ddev->GetBackBuffer( iSwapChain, iBackBuffer, Type, ppBackBuffer ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetClipPlane( DWORD Index, float* pPlane )
{
    return ( m_pD3Ddev->GetClipPlane( Index, pPlane ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetClipStatus( D3DCLIPSTATUS9* pClipStatus )
{
    return ( m_pD3Ddev->GetClipStatus( pClipStatus ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetCreationParameters( D3DDEVICE_CREATION_PARAMETERS* pParameters )
{
    return ( m_pD3Ddev->GetCreationParameters( pParameters ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetCurrentTexturePalette( UINT* pPaletteNumber )
{
    return ( m_pD3Ddev->GetCurrentTexturePalette( pPaletteNumber ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetDepthStencilSurface( IDirect3DSurface9** ppZStencilSurface )
{
    return ( m_pD3Ddev->GetDepthStencilSurface( ppZStencilSurface ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetDeviceCaps( D3DCAPS9* pCaps )
{
    return ( m_pD3Ddev->GetDeviceCaps( pCaps ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetDirect3D( IDirect3D9** ppD3D9 )
{
    HRESULT hRet = m_pD3Ddev->GetDirect3D( ppD3D9 );
    if ( SUCCEEDED( hRet ) )
        *ppD3D9 = m_pD3Dint;
            
    return ( hRet );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetDisplayMode( UINT iSwapChain, D3DDISPLAYMODE* pMode )
{
    return ( m_pD3Ddev->GetDisplayMode( iSwapChain, pMode ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetFrontBufferData( UINT iSwapChain, IDirect3DSurface9* pDestSurface )
{
    return ( m_pD3Ddev->GetFrontBufferData( iSwapChain, pDestSurface ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetFVF( DWORD* pFVF )
{
    return ( m_pD3Ddev->GetFVF( pFVF ) );
}

void APIENTRY hkIDirect3DDevice9::GetGammaRamp( UINT iSwapChain, D3DGAMMARAMP* pRamp )
{
    m_pD3Ddev->GetGammaRamp( iSwapChain, pRamp );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetIndices( IDirect3DIndexBuffer9** ppIndexData )
{
    return ( m_pD3Ddev->GetIndices( ppIndexData ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetLight( DWORD Index, D3DLIGHT9* pLight )
{
    return ( m_pD3Ddev->GetLight( Index, pLight ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetLightEnable( DWORD Index, BOOL* pEnable )
{
    return ( m_pD3Ddev->GetLightEnable( Index, pEnable ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetMaterial( D3DMATERIAL9* pMaterial )
{
    return ( m_pD3Ddev->GetMaterial( pMaterial ) );
}

float APIENTRY hkIDirect3DDevice9::GetNPatchMode() 
{
    return ( m_pD3Ddev->GetNPatchMode() );
}

unsigned int APIENTRY hkIDirect3DDevice9::GetNumberOfSwapChains() 
{
    return ( m_pD3Ddev->GetNumberOfSwapChains() );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetPaletteEntries( UINT PaletteNumber, PALETTEENTRY* pEntries )
{
    return ( m_pD3Ddev->GetPaletteEntries( PaletteNumber, pEntries ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetPixelShader( IDirect3DPixelShader9** ppShader )
{
    return ( m_pD3Ddev->GetPixelShader( ppShader ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetPixelShaderConstantB( UINT StartRegister, BOOL* pConstantData, UINT BoolCount )
{
    return ( m_pD3Ddev->GetPixelShaderConstantB( StartRegister, pConstantData, BoolCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetPixelShaderConstantF( UINT StartRegister, float* pConstantData, UINT Vector4fCount )
{
    return ( m_pD3Ddev->GetPixelShaderConstantF( StartRegister, pConstantData, Vector4fCount) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetPixelShaderConstantI( UINT StartRegister, int* pConstantData, UINT Vector4iCount )
{
    return ( m_pD3Ddev->GetPixelShaderConstantI( StartRegister, pConstantData, Vector4iCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetRasterStatus( UINT iSwapChain, D3DRASTER_STATUS* pRasterStatus )
{
    return ( m_pD3Ddev->GetRasterStatus( iSwapChain, pRasterStatus ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetRenderState( D3DRENDERSTATETYPE State, DWORD* pValue )
{
    return ( m_pD3Ddev->GetRenderState( State, pValue ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetRenderTarget( DWORD RenderTargetIndex, IDirect3DSurface9** ppRenderTarget )
{
    return ( m_pD3Ddev->GetRenderTarget( RenderTargetIndex, ppRenderTarget ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetRenderTargetData( IDirect3DSurface9* pRenderTarget, IDirect3DSurface9* pDestSurface )
{
    return ( m_pD3Ddev->GetRenderTargetData( pRenderTarget, pDestSurface ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetSamplerState( DWORD Sampler, D3DSAMPLERSTATETYPE Type, DWORD* pValue )
{
    return ( m_pD3Ddev->GetSamplerState( Sampler, Type, pValue ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetScissorRect( RECT* pRect )
{
    return ( m_pD3Ddev->GetScissorRect( pRect ) );
}

BOOL APIENTRY hkIDirect3DDevice9::GetSoftwareVertexProcessing() 
{
    return ( m_pD3Ddev->GetSoftwareVertexProcessing() );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetStreamSource( UINT StreamNumber, IDirect3DVertexBuffer9** ppStreamData, UINT* OffsetInBytes, UINT* pStride )
{
    return ( m_pD3Ddev->GetStreamSource( StreamNumber, ppStreamData, OffsetInBytes, pStride) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetStreamSourceFreq( UINT StreamNumber, UINT* Divider )
{
    return ( m_pD3Ddev->GetStreamSourceFreq( StreamNumber, Divider ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetSwapChain( UINT iSwapChain, IDirect3DSwapChain9** pSwapChain )
{
    return ( m_pD3Ddev->GetSwapChain( iSwapChain, pSwapChain ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetTexture( DWORD Stage, IDirect3DBaseTexture9** ppTexture )
{
    return ( m_pD3Ddev->GetTexture( Stage, ppTexture ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetTextureStageState( DWORD Stage, D3DTEXTURESTAGESTATETYPE Type, DWORD* pValue )
{
    return ( m_pD3Ddev->GetTextureStageState( Stage, Type, pValue ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetTransform( D3DTRANSFORMSTATETYPE State, D3DMATRIX* pMatrix )
{
    return ( m_pD3Ddev->GetTransform( State, pMatrix ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetVertexDeclaration( IDirect3DVertexDeclaration9** ppDecl )
{
    return ( m_pD3Ddev->GetVertexDeclaration( ppDecl ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetVertexShader( IDirect3DVertexShader9** ppShader )
{
    return ( m_pD3Ddev->GetVertexShader( ppShader ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetVertexShaderConstantB( UINT StartRegister, BOOL* pConstantData, UINT BoolCount )
{
    return ( m_pD3Ddev->GetVertexShaderConstantB( StartRegister, pConstantData, BoolCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetVertexShaderConstantF( UINT StartRegister, float* pConstantData, UINT Vector4fCount )
{
    return ( m_pD3Ddev->GetVertexShaderConstantF( StartRegister, pConstantData, Vector4fCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetVertexShaderConstantI( UINT StartRegister, int* pConstantData, UINT Vector4iCount )
{
    return ( m_pD3Ddev->GetVertexShaderConstantI( StartRegister, pConstantData, Vector4iCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::GetViewport( D3DVIEWPORT9* pViewport )
{
    return ( m_pD3Ddev->GetViewport( pViewport ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::LightEnable( DWORD LightIndex, BOOL bEnable )
{
    return ( m_pD3Ddev->LightEnable( LightIndex, bEnable ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::MultiplyTransform( D3DTRANSFORMSTATETYPE State, CONST D3DMATRIX* pMatrix )
{
    return ( m_pD3Ddev->MultiplyTransform( State, pMatrix ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::ProcessVertices( UINT SrcStartIndex, UINT DestIndex, UINT VertexCount, IDirect3DVertexBuffer9* pDestBuffer, IDirect3DVertexDeclaration9* pVertexDecl, DWORD Flags )
{
    return ( m_pD3Ddev->ProcessVertices( SrcStartIndex, DestIndex, VertexCount, pDestBuffer,pVertexDecl, Flags ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetClipPlane( DWORD Index, CONST float* pPlane )
{
    return ( m_pD3Ddev->SetClipPlane( Index, pPlane ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetClipStatus( CONST D3DCLIPSTATUS9* pClipStatus )
{
    return ( m_pD3Ddev->SetClipStatus( pClipStatus ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetCurrentTexturePalette( UINT PaletteNumber )
{
    return ( m_pD3Ddev->SetCurrentTexturePalette( PaletteNumber ) );
}

void APIENTRY hkIDirect3DDevice9::SetCursorPosition( int X, int Y, DWORD Flags )
{
    m_pD3Ddev->SetCursorPosition( X, Y, Flags );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetCursorProperties( UINT XHotSpot, UINT YHotSpot, IDirect3DSurface9* pCursorBitmap )
{
    return ( m_pD3Ddev->SetCursorProperties( XHotSpot, YHotSpot, pCursorBitmap ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetDepthStencilSurface( IDirect3DSurface9* pNewZStencil )
{
    return ( m_pD3Ddev->SetDepthStencilSurface( pNewZStencil ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetDialogBoxMode( BOOL bEnableDialogs )
{
    return ( m_pD3Ddev->SetDialogBoxMode( bEnableDialogs ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetFVF( DWORD FVF )
{
    return ( m_pD3Ddev->SetFVF( FVF ) );
}

void APIENTRY hkIDirect3DDevice9::SetGammaRamp( UINT iSwapChain, DWORD Flags, CONST D3DGAMMARAMP* pRamp )
{
    m_pD3Ddev->SetGammaRamp( iSwapChain, Flags, pRamp );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetIndices( IDirect3DIndexBuffer9* pIndexData )
{
    return ( m_pD3Ddev->SetIndices( pIndexData ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetLight( DWORD Index, CONST D3DLIGHT9* pLight )
{
    return ( m_pD3Ddev->SetLight( Index, pLight ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetMaterial( CONST D3DMATERIAL9* pMaterial )
{    
    return ( m_pD3Ddev->SetMaterial( pMaterial ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetNPatchMode( float nSegments )
{    
    return ( m_pD3Ddev->SetNPatchMode( nSegments ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetPaletteEntries( UINT PaletteNumber, CONST PALETTEENTRY* pEntries )
{
    return ( m_pD3Ddev->SetPaletteEntries( PaletteNumber, pEntries ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetPixelShader( IDirect3DPixelShader9* pShader )
{
    return ( m_pD3Ddev->SetPixelShader( pShader ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetPixelShaderConstantB( UINT StartRegister, CONST BOOL* pConstantData, UINT  BoolCount )
{
    return ( m_pD3Ddev->SetPixelShaderConstantB( StartRegister, pConstantData, BoolCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetPixelShaderConstantF( UINT StartRegister, CONST float* pConstantData, UINT Vector4fCount )
{
    return ( m_pD3Ddev->SetPixelShaderConstantF( StartRegister, pConstantData, Vector4fCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetPixelShaderConstantI( UINT StartRegister, CONST int* pConstantData, UINT Vector4iCount )
{
    return ( m_pD3Ddev->SetPixelShaderConstantI( StartRegister, pConstantData, Vector4iCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetRenderState( D3DRENDERSTATETYPE State, DWORD Value )
{
    return ( m_pD3Ddev->SetRenderState( State, Value ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetRenderTarget( DWORD RenderTargetIndex, IDirect3DSurface9* pRenderTarget )
{
    return ( m_pD3Ddev->SetRenderTarget( RenderTargetIndex, pRenderTarget ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetSamplerState( DWORD Sampler, D3DSAMPLERSTATETYPE Type, DWORD Value )
{
    return ( m_pD3Ddev->SetSamplerState( Sampler, Type, Value ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetScissorRect( CONST RECT* pRect )
{
    return ( m_pD3Ddev->SetScissorRect( pRect ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetSoftwareVertexProcessing( BOOL bSoftware )
{
    return ( m_pD3Ddev->SetSoftwareVertexProcessing( bSoftware ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetStreamSource( UINT StreamNumber, IDirect3DVertexBuffer9* pStreamData, UINT OffsetInBytes, UINT Stride )
{
    return m_pD3Ddev->SetStreamSource( StreamNumber, pStreamData, OffsetInBytes, Stride );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetStreamSourceFreq( UINT StreamNumber, UINT Divider )
{    
    return ( m_pD3Ddev->SetStreamSourceFreq( StreamNumber, Divider ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetTexture( DWORD Stage, IDirect3DBaseTexture9* pTexture )
{
#ifdef D3DHOOK_TEXTURES
    IDirect3DDevice9* dev = NULL;
    if ( ( pTexture != NULL ) && ( ( (hkIDirect3DTexture9*)pTexture )->GetDevice( &dev ) == D3D_OK ) )
    {
        if ( dev == this )
            return ( m_pD3Ddev->SetTexture( Stage, ( (hkIDirect3DTexture9*)pTexture )->m_D3Dtex ) );
    }
#endif
    
    return ( m_pD3Ddev->SetTexture( Stage, pTexture ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetTextureStageState( DWORD Stage, D3DTEXTURESTAGESTATETYPE Type, DWORD Value )
{
    return ( m_pD3Ddev->SetTextureStageState( Stage, Type, Value ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetTransform( D3DTRANSFORMSTATETYPE State, CONST D3DMATRIX* pMatrix )
{
    return ( m_pD3Ddev->SetTransform( State, pMatrix ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetVertexDeclaration( IDirect3DVertexDeclaration9* pDecl )
{
    return ( m_pD3Ddev->SetVertexDeclaration( pDecl ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetVertexShader( IDirect3DVertexShader9* pShader )
{
    return ( m_pD3Ddev->SetVertexShader( pShader ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetVertexShaderConstantB( UINT StartRegister, CONST BOOL* pConstantData, UINT  BoolCount )
{
    return ( m_pD3Ddev->SetVertexShaderConstantB( StartRegister, pConstantData, BoolCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetVertexShaderConstantF( UINT StartRegister, CONST float* pConstantData, UINT Vector4fCount )
{
    return ( m_pD3Ddev->SetVertexShaderConstantF( StartRegister, pConstantData, Vector4fCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetVertexShaderConstantI( UINT StartRegister, CONST int* pConstantData, UINT Vector4iCount )
{
    return ( m_pD3Ddev->SetVertexShaderConstantI( StartRegister, pConstantData, Vector4iCount ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::SetViewport( CONST D3DVIEWPORT9* pViewport )
{
    if ( pViewport->X > 0 )
    {
	    viewport[0] = (unsigned short)pViewport->X;
	    viewport[1] = (unsigned short)pViewport->Y;
	    viewport[2] = (unsigned short)pViewport->Width;
	    viewport[3] = (unsigned short)pViewport->Height;
        
        viewportSet = true;
    }
    
    return ( m_pD3Ddev->SetViewport( pViewport ) );
}

BOOL APIENTRY hkIDirect3DDevice9::ShowCursor( BOOL bShow )
{
    return ( m_pD3Ddev->ShowCursor( bShow ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::StretchRect( IDirect3DSurface9* pSourceSurface, CONST RECT* pSourceRect, IDirect3DSurface9* pDestSurface, CONST RECT* pDestRect, D3DTEXTUREFILTERTYPE Filter )
{
    return ( m_pD3Ddev->StretchRect( pSourceSurface, pSourceRect, pDestSurface, pDestRect, Filter ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::TestCooperativeLevel() 
{
    return ( m_pD3Ddev->TestCooperativeLevel() );
}

HRESULT APIENTRY hkIDirect3DDevice9::UpdateSurface( IDirect3DSurface9* pSourceSurface, CONST RECT* pSourceRect, IDirect3DSurface9* pDestinationSurface, CONST POINT* pDestPoint )
{
    return ( m_pD3Ddev->UpdateSurface( pSourceSurface, pSourceRect, pDestinationSurface, pDestPoint ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::UpdateTexture( IDirect3DBaseTexture9* pSourceTexture, IDirect3DBaseTexture9* pDestinationTexture )
{
    return ( m_pD3Ddev->UpdateTexture( pSourceTexture, pDestinationTexture ) );
}

HRESULT APIENTRY hkIDirect3DDevice9::ValidateDevice( DWORD* pNumPasses )
{
    return ( m_pD3Ddev->ValidateDevice( pNumPasses ) );
}
