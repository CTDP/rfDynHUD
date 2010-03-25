package org.jagatoo.util.ini;

public class IniLine
{
    private int lineNr = -1;
    private String line = "";
    private String currentGroup = null;
    private String key = null;
    private String value = null;
    private String comment = null;
    private Boolean isValid = null;
    
    void reset()
    {
        lineNr = -1;
        line = "";
        currentGroup = null;
        key = null;
        value = null;
        comment = null;
        isValid = null;
    }
    
    void setLine( int lineNr, String currentGroup, String line )
    {
        this.lineNr = lineNr;
        this.currentGroup= currentGroup;
        this.line = ( line == null ) ? line : line.trim();
    }
    
    public final int getLineNr()
    {
        return ( lineNr );
    }
    
    public final String getLine()
    {
        return ( line );
    }
    
    public final boolean isEmpty()
    {
        return ( ( line == null ) || ( line.length() == 0 ) );
    }
    
    String setCurrentGroup( String group )
    {
        this.currentGroup = group;
        
        return ( this.currentGroup );
    }
    
    public final String getCurrentGroup()
    {
        return ( currentGroup );
    }
    
    String setKey( String key )
    {
        this.key = key;
        
        return ( this.key );
    }
    
    public final String getKey()
    {
        return ( key );
    }
    
    String setValue( String value )
    {
        this.value = value;
        
        return ( this.value );
    }
    
    public final String getValue()
    {
        return ( value );
    }
    
    String setComment( String comment )
    {
        this.comment = comment;
        
        return ( this.comment );
    }
    
    public final String getComment()
    {
        return ( comment );
    }
    
    Boolean setValid( Boolean valid )
    {
        this.isValid = valid;
        
        return ( this.isValid );
    }
    
    public final Boolean isValid()
    {
        return ( isValid );
    }
}
