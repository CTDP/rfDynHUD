package net.ctdp.rfdynhud.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * The {@link NumberUtil} keeps static methods to format numbers.
 * 
 * @author Marvin Froehlich
 */
public class NumberUtil
{
    private static final NumberFormat formatter = DecimalFormat.getNumberInstance( Locale.US );
    
    public static final String pad2( int number )
    {
        if ( ( number > 9 ) || ( number < 0 ) )
            return ( String.valueOf( number ) );
        
        return ( "0" + String.valueOf( number ) );
    }
    
    /**
     * Formats the given float to a String with the specified number of decimal places.
     * 
     * @param f
     * @param numDecPlaces
     * 
     * @return the formatted String.
     */
    public static final String formatFloat( float f, int numDecPlaces, boolean forceFractions )
    {
        if ( numDecPlaces == 0 )
        {
            return ( String.valueOf( Math.round( f ) ) );
        }
        
        /*
        int p = (int)Math.pow( 10, numDecPlaces );
        
        int i = (int)f;
        String s = String.valueOf( i ) + ".";
        
        f -= i;
        f *= p;
        i = Math.round( f );
        
        s += String.valueOf( i );
        
        return ( s );
        */
        
        formatter.setMaximumFractionDigits( numDecPlaces );
        formatter.setMinimumFractionDigits( forceFractions ? numDecPlaces : 0 );
        return ( formatter.format( f ) );
    }
    
    /**
     * Gets the next greater power-of-two for the given number.
     * 
     * @param v
     * 
     * @return the next greater power-of-two for the given number.
     */
    public static final int roundUpPower2( int v )
    {
        switch ( Integer.bitCount( v ) )
        {
            case 0:
                return ( 1 );
            case 1:
                return ( v );
            default:
                return ( Integer.highestOneBit( v ) << 1 );
        }
    }
    
    public static final String delta( int i )
    {
        if ( i >= 0 )
            return ( "+" + i );
        
        return ( String.valueOf( i ) );
    }
    
    public static final String delta( float i )
    {
        if ( i >= 0 )
            return ( "+" + i );
        
        return ( String.valueOf( i ) );
    }
}
