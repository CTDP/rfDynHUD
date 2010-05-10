package net.ctdp.rfdynhud.widgets;

public final class GameResolution
{
    private int resX = 1280;
    private int resY = 1024;
    
    void set( int resX, int resY )
    {
        this.resX = resX;
        this.resY = resY;
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
