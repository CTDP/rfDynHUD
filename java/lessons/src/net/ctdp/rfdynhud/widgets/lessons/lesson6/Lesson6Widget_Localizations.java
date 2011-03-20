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
package net.ctdp.rfdynhud.widgets.lessons.lesson6;

import java.awt.Font;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.lessons._util.LessonsWidgetSet;

/**
 * This Widget demonstrates the use of localizations.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Lesson6Widget_Localizations extends Widget
{
    private DrawnString ds = null;
    
    private final FloatValue v = new FloatValue( -1f, 0.1f );
    
    public Lesson6Widget_Localizations()
    {
        super( LessonsWidgetSet.INSTANCE, LessonsWidgetSet.WIDGET_PACKAGE, 14.0f, 5.0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        getFontProperty().setFont( "Dialog", Font.PLAIN, 7, false, true );
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        v.reset();
    }
    
    private static final String getTempUnits( MeasurementUnits measurementUnits )
    {
        /*
         * We need to decide, which units are being used
         * and return the appropriate localization string.
         */
        
        if ( measurementUnits == MeasurementUnits.IMPERIAL )
            return ( Loc.temperature_units_IMPERIAL );
        
        return ( Loc.temperature_units_METRIC );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
    }
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        final MeasurementUnits measurementUnits = gameData.getProfileInfo().getMeasurementUnits();
        
        /*
         * We set the caption as the prefix and units string as a postfix.
         */
        ds = drawnStringFactory.newDrawnString( "ds", 0, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), Loc.mytext_caption + ": ", getTempUnits( measurementUnits ) );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        v.update( gameData.getTelemetryData().getTireTemperature( Wheel.FRONT_LEFT ) );
        
        if ( needsCompleteRedraw || ( clock.c() && v.hasChanged() ) )
        {
            String tireTempFL = NumberUtil.formatFloat( v.getValue(), 1, true );
            
            ds.draw( offsetX, offsetY, tireTempFL, texture );
        }
    }
}
