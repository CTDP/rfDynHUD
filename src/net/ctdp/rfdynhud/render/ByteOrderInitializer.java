package net.ctdp.rfdynhud.render;

/**
 * This class must be initialized at the very beginning to feed the {@link ByteOrderManager}
 * with the correct values.
 * 
 * @author Marvin Froehlich
 */
public class ByteOrderInitializer
{
    static int offsetRed = -1;
    static int offsetGreen = -1;
    static int offsetBlue = -1;
    static int offsetAlpha = -1;
    
    public static void setByteOrder( int offsetRed, int offsetGreen, int offsetBlue, int offsetAlpha )
    {
        ByteOrderInitializer.offsetRed = offsetRed;
        ByteOrderInitializer.offsetGreen = offsetGreen;
        ByteOrderInitializer.offsetBlue = offsetBlue;
        ByteOrderInitializer.offsetAlpha = offsetAlpha;
    }
}
