package net.ctdp.rfdynhud.render;

/**
 * The manager provides static final fields for the used byte order.
 * 
 * @author Marvin Froehlich
 */
public class ByteOrderManager
{
    public static final int RED = ByteOrderInitializer.offsetRed;
    public static final int GREEN = ByteOrderInitializer.offsetGreen;
    public static final int BLUE = ByteOrderInitializer.offsetBlue;
    public static final int ALPHA = ByteOrderInitializer.offsetAlpha;
    
    public static void dump()
    {
        System.out.println( "Byte order: RED = " + RED + ", GREEN = " + GREEN + ", BLUE = " + BLUE + ", ALPHA = " + ALPHA );
    }
}
