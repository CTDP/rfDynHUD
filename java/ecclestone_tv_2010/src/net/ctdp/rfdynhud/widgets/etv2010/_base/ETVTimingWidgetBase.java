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
package net.ctdp.rfdynhud.widgets.etv2010._base;

import java.io.IOException;

import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.widgets.etv2010._util.ETVUtils;

public abstract class ETVTimingWidgetBase extends ETVWidgetBase
{
    protected final ColorProperty captionBackgroundColor1st = new ColorProperty( "captionBgColor1st", ETVUtils.ETV_CAPTION_BACKGROUND_COLOR_1ST );
    protected final ColorProperty dataBackgroundColor1st = new ColorProperty( "dataBgColor1st", ETVUtils.ETV_DATA_BACKGROUND_COLOR_1ST );
    protected final ColorProperty dataBackgroundColorFastest = new ColorProperty( "dataBgColorFastest", ETVUtils.ETV_DATA_BACKGROUND_COLOR_FASTEST );
    protected final ColorProperty dataBackgroundColorFaster = new ColorProperty( "dataBgColorFaster", ETVUtils.ETV_DATA_BACKGROUND_COLOR_FASTER );
    protected final ColorProperty dataBackgroundColorSlower = new ColorProperty( "dataBgColorSlower", ETVUtils.ETV_DATA_BACKGROUND_COLOR_SLOWER );
    protected final ColorProperty dataColorFastest = new ColorProperty( "dataColorFastest", ETVUtils.ETV_DATA_FONT_COLOR_FASTEST );
    protected final ColorProperty dataColorFaster = new ColorProperty( "dataColorFaster", ETVUtils.ETV_DATA_FONT_COLOR_FASTER );
    protected final ColorProperty dataColorSlower = new ColorProperty( "dataColorSlower", ETVUtils.ETV_DATA_FONT_COLOR_SLOWER );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( captionBackgroundColor1st, "The background color for the \"Position\" caption for first place." );
        writer.writeProperty( dataBackgroundColor1st, "The background color for the data area, for first place." );
        writer.writeProperty( dataBackgroundColorFastest, "The background color for the data area, if a driver made the absolute fastest lap." );
        writer.writeProperty( dataBackgroundColorFaster, "The background color for the data area, if a negative gap is displayed." );
        writer.writeProperty( dataBackgroundColorSlower, "The background color for the data area, if a positive gap is displayed." );
        writer.writeProperty( dataColorFastest, "The font color for the data area, if a driver made the absolute fastest lap." );
        writer.writeProperty( dataColorFaster, "The font color for the data area, if a negative gap is displayed." );
        writer.writeProperty( dataColorSlower, "The font color for the data area, if a positive gap is displayed." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( captionBackgroundColor1st ) );
        else if ( loader.loadProperty( dataBackgroundColor1st ) );
        else if ( loader.loadProperty( dataBackgroundColorFastest ) );
        else if ( loader.loadProperty( dataBackgroundColorFaster ) );
        else if ( loader.loadProperty( dataBackgroundColorSlower ) );
        else if ( loader.loadProperty( dataColorFastest ) );
        else if ( loader.loadProperty( dataColorFaster ) );
        else if ( loader.loadProperty( dataColorSlower ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void getPropertiesCaptionBG( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getPropertiesCaptionBG( propsCont, forceAll );
        
        if ( forceAll || !useImages.getBooleanValue() )
        {
            propsCont.addProperty( captionBackgroundColor1st );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void getPropertiesDataBG( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getPropertiesDataBG( propsCont, forceAll );
        
        if ( forceAll || !useImages.getBooleanValue() )
        {
            propsCont.addProperty( dataBackgroundColor1st );
            propsCont.addProperty( dataBackgroundColorFastest );
            propsCont.addProperty( dataBackgroundColorFaster );
            propsCont.addProperty( dataBackgroundColorSlower );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void getPropertiesData( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getPropertiesData( propsCont, forceAll );
        
        propsCont.addProperty( dataColorFastest );
        propsCont.addProperty( dataColorFaster );
        propsCont.addProperty( dataColorSlower );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
    }
    
    public ETVTimingWidgetBase( float width, float height )
    {
        super( width, height );
    }
}
