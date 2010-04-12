package net.ctdp.rfdynhud.render;

public class __RenderPrivilegedAccess
{
    public static TransformableTexture createMainTexture( int width, int height )
    {
        return ( TransformableTexture.createMainTexture( width, height ) );
    }
    
    public static final void setLastModified( long lastModified, ImageTemplate it )
    {
        it.lastModified = lastModified;
    }
    
    public static final long getLastModified( ImageTemplate it )
    {
        return ( it.lastModified );
    }
    
    public static final void setFileSize( long fileSize, ImageTemplate it )
    {
        it.fileSize = fileSize;
    }
    
    public static final long getFileSize( ImageTemplate it )
    {
        return ( it.fileSize );
    }
}
