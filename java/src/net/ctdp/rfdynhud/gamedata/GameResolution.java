package net.ctdp.rfdynhud.gamedata;

public final class GameResolution
{
    private int resX = 1280;
    private int resY = 1024;
    
    private int vpX = 0;
    private int vpY = 0;
    private int vpW = 1280;
    private int vpH = 1024;
    
    boolean setResolution( int resX, int resY )
    {
        if ( ( resX != this.resX ) || ( resY != this.resY ) )
        {
            this.resX = resX;
            this.resY = resY;
            
            return ( true );
        }
        
        return ( false );
    }
    
    public final int getResX()
    {
        return ( resX );
    }
    
    public final int getResY()
    {
        return ( resY );
    }
    
    boolean setViewport( int x, int y, int w, int h )
    {
        if ( ( x != this.vpX ) || ( y != this.vpY ) || ( w != this.vpW ) || ( h != this.vpH ) )
        {
            this.vpX = x;
            this.vpY = y;
            this.vpW = w;
            this.vpH = h;
            
            return ( true );
        }
        
        return ( false );
    }
    
    public final int getViewportX()
    {
        return ( vpX );
    }
    
    public final int getViewportY()
    {
        return ( vpY );
    }
    
    public final int getViewportWidth()
    {
        return ( vpW );
    }
    
    public final int getViewportHeight()
    {
        return ( vpH );
    }
    
    public final String getResolutionString()
    {
        return ( resX + "x" + resY );
    }
    
    public final String getViewportString()
    {
        return ( vpX + ", " + vpY + ", " + vpW + "x" + vpH );
    }
    
    public GameResolution( int resX, int resY )
    {
        this.resX = resX;
        this.resY = resY;
        
        setViewport( 0, 0, resX, resY );
    }
    
    public GameResolution()
    {
    }
}
