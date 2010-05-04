package net.ctdp.rfdynhud.etv2010.widgets._base;

import java.io.IOException;

import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.widget.Widget;

public abstract class ETVWidgetBase extends Widget
{
    protected final ColorProperty captionBackgroundColor = new ColorProperty( this, "captionBgColor", ETVUtils.ETV_STYLE_CAPTION_BACKGROUND_COLOR );
    protected final ColorProperty captionColor = new ColorProperty( this, "captionColor", ETVUtils.ETV_STYLE_CAPTION_FONT_COLOR );
    
    @Override
    public String getWidgetPackage()
    {
        return ( ETVUtils.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedColorValue( String name )
    {
        String result = super.getDefaultNamedColorValue( name );
        
        if ( result != null )
            return ( result );
        
        return ( ETVUtils.getDefaultNamedColorValue( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        String result = super.getDefaultNamedFontValue( name );
        
        if ( result != null )
            return ( result );
        
        return ( ETVUtils.getDefaultNamedFontValue( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( captionBackgroundColor, "The background color for the \"Lap\" caption." );
        writer.writeProperty( captionColor, "The font color for the \"Lap\" caption." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( captionBackgroundColor.loadProperty( key, value ) );
        else if ( captionColor.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Colors and Fonts" );
        
        propsCont.addProperty( captionBackgroundColor );
        propsCont.addProperty( captionColor );
    }
    
    /*
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    */
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    public ETVWidgetBase( String name, float width, float height )
    {
        super( name, width, true, height, true );
        
        getBackgroundColorProperty().setValue( ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR );
        getFontColorProperty().setValue( ETVUtils.ETV_STYLE_DATA_FONT_COLOR );
        getFontProperty().setValue( ETVUtils.ETV_STYLE_FONT );
    }
}
