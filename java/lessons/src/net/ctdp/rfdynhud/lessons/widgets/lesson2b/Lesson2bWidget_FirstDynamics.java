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
package net.ctdp.rfdynhud.lessons.widgets.lesson2b;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.lessons.widgets._util.LessonsWidgetSet;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;

/**
 * This Widget shows another dynamic text.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Lesson2bWidget_FirstDynamics extends Widget
{
    private DrawnString ds = null;
    
    /*
     * We use this nice class to easily check, if the value has changed.
     * An invalid value is defined as -1 and we want to value to be
     * identified as changed when it has changed by 0.1.
     * 
     * There are value classes for all basic types, that you should
     * also have a look at (BoolValue, EnumValue, IntValue, LongValue and StringValue).
     */
    private final FloatValue v = new FloatValue( -1f, 0.1f );
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( LessonsWidgetSet.WIDGET_PACKAGE );
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        /*
         * Whenever we enter the cockpit, we need to reset our value, so that it is guaranteed
         * to be detected as changed next time.
         */
        v.reset();
    }
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        ds = drawnStringFactory.newDrawnString( "ds", 0, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        /*
         * This time we add one more check.
         * Since we render less dynamic content, we only need to redraw when the value has actually changed.
         * But we still need to restrict the redraw to the clock.
         * 
         * So the text is redrawn when the Widget is completely redrawn or the value has changed AND the clock is 'true'.
         * The order is important, since the FloatValue is marked as unchanged after the test.
         * So every time, the clock is 'false', the test must not be done.
         * 
         * You can change this behavior by passing a 'false' parameter to the hasChanged() method.
         * Then you have to manually set the value to unchanged by invoking the setUnchanged() method
         * on the FloatValue object. But we use the default behavior here.
         */
        
        /*
         * First we need to update our FloatValue with some data.
         * Tire temperature getters provide the same system of unit selections like all
         * other temperature- and velocity methods. We let rFactor decide about the units again.
         */
        v.update( gameData.getTelemetryData().getTireTemperature( Wheel.FRONT_LEFT ) );
        
        if ( needsCompleteRedraw || ( clock.c() && v.hasChanged() ) )
        {
            /*
             * Here we can use getScalarVelocityKPH() or getScalarVelocityMPH() to explicitly use these units
             * or use getScalarVelocity() to use rFactor's setting to decide about that.
             * 
             * Of course we don't want all the fractions to be displayed, so we use a utility class
             * to nicely format the float value with only one fraction digit.
             */
            String tireTempFL = NumberUtil.formatFloat( v.getValue(), 1, true );
            
            ds.draw( offsetX, offsetY, tireTempFL, texture );
        }
    }
    
    public Lesson2bWidget_FirstDynamics()
    {
        super( 5.0f, 5.0f );
    }
}
