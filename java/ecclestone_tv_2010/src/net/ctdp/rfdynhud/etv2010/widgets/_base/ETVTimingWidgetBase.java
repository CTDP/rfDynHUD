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

import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;

public abstract class ETVTimingWidgetBase extends ETVWidgetBase
{
    protected final ColorProperty captionBackgroundColor1st = new ColorProperty( this, "captionBgColor1st", ETVUtils.ETV_STYLE_CAPTION_BACKGROUND_COLOR_1ST );
    protected final ColorProperty dataBackgroundColor1st = new ColorProperty( this, "dataBgColor1st", ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR_1ST );
    protected final ColorProperty dataBackgroundColorFastest = new ColorProperty( this, "dataBgColorFastest", ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR_FASTEST );
    protected final ColorProperty dataBackgroundColorFaster = new ColorProperty( this, "dataBgColorFaster", ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR_FASTER );
    protected final ColorProperty dataBackgroundColorSlower = new ColorProperty( this, "dataBgColorSlower", ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR_SLOWER );
    protected final ColorProperty dataColorFastest = new ColorProperty( this, "dataColorFastest", ETVUtils.ETV_STYLE_DATA_FONT_COLOR_FASTEST );
    protected final ColorProperty dataColorFaster = new ColorProperty( this, "dataColorFaster", ETVUtils.ETV_STYLE_DATA_FONT_COLOR_FASTER );
    protected final ColorProperty dataColorSlower = new ColorProperty( this, "dataColorSlower", ETVUtils.ETV_STYLE_DATA_FONT_COLOR_SLOWER );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
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
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( captionBackgroundColor1st.loadProperty( key, value ) );
        else if ( dataBackgroundColor1st.loadProperty( key, value ) );
        else if ( dataBackgroundColorFastest.loadProperty( key, value ) );
        else if ( dataBackgroundColorFaster.loadProperty( key, value ) );
        else if ( dataBackgroundColorSlower.loadProperty( key, value ) );
        else if ( dataColorFastest.loadProperty( key, value ) );
        else if ( dataColorFaster.loadProperty( key, value ) );
        else if ( dataColorSlower.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addProperty( captionBackgroundColor1st );
        propsCont.addProperty( dataBackgroundColor1st );
        propsCont.addProperty( dataBackgroundColorFastest );
        propsCont.addProperty( dataBackgroundColorFaster );
        propsCont.addProperty( dataBackgroundColorSlower );
        propsCont.addProperty( dataColorFastest );
        propsCont.addProperty( dataColorFaster );
        propsCont.addProperty( dataColorSlower );
    }
    
    public ETVTimingWidgetBase( String name, float width, float height )
    {
        super( name, width, height );
    }
}
