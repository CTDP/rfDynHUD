package net.ctdp.rfdynhud.editor.properties;

public abstract class PosSizeProperty extends Property
{
    private static final String FLIP_TEXT = "flip";
    private static final String FLIP_TOOLTIP = "Flips the Widget's width from absolute to screen-size-relative.";
    
    private static final String PERC_TEXT = "%";
    private static final String PERC_TOOLTIP = "Converts this property to percentages.";
    
    private static final String PX_TEXT = "px";
    private static final String PX_TOOLTIP = "Converts this property to pixels.";
    
    private final boolean isSizeProp;
    
    public final boolean isSizeProp()
    {
        return ( isSizeProp );
    }
    
    public String getButton1Text( boolean isPerc )
    {
        if ( isSizeProp )
            return ( FLIP_TEXT );
        
        if ( isPerc )
            return ( PX_TEXT );
        
        return ( PERC_TEXT );
    }
    
    public String getButton1Tooltip( boolean isPerc )
    {
        if ( isSizeProp )
            return ( FLIP_TOOLTIP );
        
        if ( isPerc )
            return ( PX_TOOLTIP );
        
        return ( PERC_TOOLTIP );
    }
    
    public String getButton2Text( boolean isPerc )
    {
        if ( isPerc )
            return ( PX_TEXT );
        
        return ( PERC_TEXT );
    }
    
    public String getButton2Tooltip( boolean isPerc )
    {
        if ( isPerc )
            return ( PX_TOOLTIP );
        
        return ( PERC_TOOLTIP );
    }
    
    public void onButton2Clicked( Object button )
    {
    }
    
    public PosSizeProperty( String key, boolean readonly, boolean isSizeProp )
    {
        super( key, readonly, PropertyEditorType.POS_SIZE, null, null );
        
        this.isSizeProp = isSizeProp;
    }
    
    public PosSizeProperty( String key, boolean isSizeProp )
    {
        this( key, false, isSizeProp );
    }
}
