package net.ctdp.rfdynhud.util;

public class Tools
{
    public static final Number getNumber( String string )
    {
        try
        {
            return ( Integer.parseInt( string ) );
        }
        catch ( NumberFormatException e )
        {
            try
            {
                return ( Double.parseDouble( string ) );
            }
            catch ( NumberFormatException e2 )
            {
                return ( null );
            }
        }
    }
    
    public static final boolean objectsEqual( Object o1, Object o2 )
    {
        if ( o1 == o2 )
            return ( true );
        
        if ( ( o1 == null ) && ( o2 != null ) )
            return ( false );
        
        if ( ( o1 != null ) && ( o2 == null ) )
            return ( false );
        
        return ( o1.equals( o2 ) );
    }
    
    @SuppressWarnings( "unchecked" )
    public static final int compareObjects( Comparable o1, Comparable o2 )
    {
        if ( o1 == o2 )
            return ( 0 );
        
        if ( o1 == null )
            return ( -1 );
        
        if ( o2 == null )
            return ( +1 );
        
        return ( o1.compareTo( o2 ) );
    }
    
    public static final String padLeft( int number, int length, String padStr )
    {
        String s = String.valueOf( number );
        
        while ( s.length() < length )
            s = padStr + s;
        
        return ( s );
    }
}
