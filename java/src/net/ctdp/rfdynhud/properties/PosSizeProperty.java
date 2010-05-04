package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.widgets.widget.Widget;

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
    
    public abstract boolean isPercentage();
    
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
    
    public PosSizeProperty( Widget widget, String name, String nameFordisplay, boolean readonly, boolean isSizeProp )
    {
        super( widget, name, nameFordisplay, readonly, PropertyEditorType.POS_SIZE, null, null );
        
        this.isSizeProp = isSizeProp;
    }
    
    public PosSizeProperty( Widget widget, String name, String nameFordisplay, boolean isSizeProp )
    {
        this( widget, name, nameFordisplay, false, isSizeProp );
    }
}
