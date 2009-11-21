package net.ctdp.rfdynhud.widgets._util;

/**
 * Provides static methods to deal with timing.
 * 
 * @author Marvin Froehlich
 */
public class TimingUtil
{
    private static final String pad2( int i )
    {
        if ( i >= 10 )
            return ( String.valueOf( i ) );
        
        return ( "0" + String.valueOf( i ) );
    }
    
    private static final String pad3( int i )
    {
        if ( i >= 100 )
            return ( String.valueOf( i ) );
        
        if ( i >= 10 )
            return ( "0" + String.valueOf( i ) );
        
        return ( "00" + String.valueOf( i ) );
    }
    
    /**
     * Gets a formatted String from the given seconds.
     * 
     * @param seconds
     * @param forceAllFields
     * @param padHighest if true, the highest displayed field (i.e. hours or minuts) will be padded with a zero
     * @param showMillis
     * 
     * @return a formatted String from the given seconds.
     */
    public static String getTimeAsString( float seconds, boolean forceAllFields, boolean padHighest, boolean showMillis )
    {
        String str = "";
        
        if ( seconds < 0 )
        {
            str += "-";
            seconds *= -1f;
        }
        
        int hours = (int)( seconds / 3600f );
        
        if ( ( hours > 0 ) || forceAllFields )
        {
            if ( padHighest )
                str += pad2( hours ) + ":";
            else
                str += String.valueOf( hours ) + ":";
        }
        
        int minutes = (int)( ( seconds / 60f ) % 60f );
        
        if ( ( hours > 0 ) || ( minutes > 0 ) || forceAllFields )
        {
            if ( ( hours > 0 ) || padHighest )
                str += pad2( minutes ) + ":";
            else
                str += minutes + ":";
        }
        
        int restSeconds = showMillis ? (int)( seconds % 60f ) : Math.round( seconds % 60f );
        
        if ( ( hours > 0 ) || ( minutes > 0 ) || padHighest )
            str += pad2( restSeconds );
        else
            str += String.valueOf( restSeconds );
        
        if ( showMillis )
        {
            int millis = Math.round( ( seconds * 1000f ) % 1000f );
            
            str += "." + pad3( millis );
        }
        
        return ( str );
    }
    
    /**
     * Gets a formatted String from the given seconds.
     * 
     * @param seconds
     * @param forceAllFields
     * @param showMillis
     * 
     * @return a formatted String from the given seconds.
     */
    public static String getTimeAsString( float seconds, boolean forceAllFields, boolean showMillis )
    {
        return ( getTimeAsString( seconds, forceAllFields, true, showMillis ) );
    }
    
    /**
     * Gets a formatted String from the given seconds.
     * 
     * @param seconds
     * @param showMillis
     * 
     * @return a formatted String from the given seconds.
     */
    public static String getTimeAsString( float seconds, boolean showMillis )
    {
        return ( getTimeAsString( seconds, false, showMillis ) );
    }
    
    public static String getTimeAsGapString( float seconds )
    {
        if ( seconds >= 0f )
            return ( "+" + getTimeAsString( seconds, false, false, true ) );
        
        return ( getTimeAsString( seconds, false, false, true ) );
    }
}
