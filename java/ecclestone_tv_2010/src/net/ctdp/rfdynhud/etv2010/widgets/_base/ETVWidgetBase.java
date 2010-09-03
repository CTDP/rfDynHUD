/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.etv2010.widgets._base;

import java.io.IOException;

import net.ctdp.rfdynhud.etv2010.widgets._util.ETVImages;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

public abstract class ETVWidgetBase extends Widget
{
    protected final BooleanProperty useImages = new BooleanProperty( this, "useImages", true )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            images = null;
        }
    };
    
    protected final StringProperty imagesIni = new StringProperty( this, "imagesIni", "ecclestone_tv_2010/etv_2010_images.ini" )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            images = null;
        }
    };
    
    private ETVImages images = null;
    
    protected final ColorProperty captionBackgroundColor = new ColorProperty( this, "captionBgColor", ETVUtils.ETV_STYLE_CAPTION_BACKGROUND_COLOR );
    protected final ColorProperty captionColor = new ColorProperty( this, "captionColor", ETVUtils.ETV_STYLE_CAPTION_FONT_COLOR );
    protected final ColorProperty dataBackgroundColor = new ColorProperty( this, "dataBgColor", ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR );
    
    @Override
    public WidgetPackage getWidgetPackage()
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
    
    protected final ETVImages getImages()
    {
        if ( !useImages.getBooleanValue() )
            return ( null );
        
        if ( images == null )
        {
            images = new ETVImages( imagesIni.getStringValue() );
        }
        
        return ( images );
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
        writer.writeProperty( dataBackgroundColor, "The background color for the data fields." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( captionBackgroundColor ) );
        else if ( loader.loadProperty( captionColor ) );
        else if ( loader.loadProperty( dataBackgroundColor ) );
    }
    
    /**
     * 
     * @param propsCont
     * @param forceAll
     */
    protected void getPropertiesCaptionBG( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        if ( forceAll || !useImages.getBooleanValue() )
        {
            propsCont.addProperty( captionBackgroundColor );
        }
    }
    
    /**
     * 
     * @param propsCont
     * @param forceAll
     */
    protected void getPropertiesCaption( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        getPropertiesCaptionBG( propsCont, forceAll );
        
        propsCont.addProperty( captionColor );
    }
    
    /**
     * 
     * @param propsCont
     * @param forceAll
     */
    protected void getPropertiesDataBG( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        if ( forceAll || !useImages.getBooleanValue() )
        {
            propsCont.addProperty( dataBackgroundColor );
        }
    }
    
    /**
     * 
     * @param propsCont
     * @param forceAll
     */
    protected void getPropertiesData( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        getPropertiesDataBG( propsCont, forceAll );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Images, Colors and Fonts" );
        
        propsCont.addProperty( useImages );
        
        if ( forceAll || useImages.getBooleanValue() )
        {
            propsCont.addProperty( imagesIni );
        }
        
        getPropertiesCaption( propsCont, forceAll );
        getPropertiesData( propsCont, forceAll );
    }
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    /*
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    */
    
    @Override
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.COLOR_INDICATOR + "#00000000" );
    }
    
    public ETVWidgetBase( String name, float width, float height )
    {
        super( name, width, true, height, true );
        
        getFontColorProperty().setValue( ETVUtils.ETV_STYLE_DATA_FONT_COLOR );
        getFontProperty().setValue( ETVUtils.ETV_STYLE_FONT );
    }
}
