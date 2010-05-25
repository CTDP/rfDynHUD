package net.ctdp.rfdynhud.widgets;

public final class GameResolution
{
    private int resX = 1280;
    private int resY = 1024;
    
    boolean set( int resX, int resY )
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
    
    public final String getResString()
    {
        return ( resX + "x" + resY );
    }
    
    public GameResolution( int resX, int resY )
    {
        set( resX, resY );
    }
    
    public GameResolution()
    {
    }
}
