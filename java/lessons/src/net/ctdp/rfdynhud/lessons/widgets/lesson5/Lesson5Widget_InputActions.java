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
package net.ctdp.rfdynhud.lessons.widgets.lesson5;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.lessons.widgets._util.LessonsWidgetSet;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;

/**
 * This Widget demonstrates the use of custom InputActions.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Lesson5Widget_InputActions extends Widget
{
    /*
     * This is your InputAction to decrement the value.
     * Don't forget to write some documentation for this action as we did in the doc subfolder.
	 * Setting the second parameter 'true' defines, that the action reacts on key-pressed events.
	 * Alternatively, setting this parameter to 'false', will fire on key-release events and
	 * setting it 'null' will fire both events.
     */
    private static final InputAction MY_DEC_ACTION = new InputAction( "MyDecAction", true );
    
    /*
     * This is your InputAction to increment the value.
     * Don't forget to write some documentation for this action as we did in the doc subfolder.
     */
    private static final InputAction MY_INC_ACTION = new InputAction( "MyIncAction", true );
    
    private DrawnString ds = null;
    
    private final IntValue value = new IntValue();
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( LessonsWidgetSet.WIDGET_PACKAGE );
    }
    
    @Override
    public InputAction[] getInputActions()
    {
        return ( new InputAction[] { MY_DEC_ACTION, MY_INC_ACTION } );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.onBoundInputStateChanged( action, state, modifierMask, when, gameData, isEditorMode );
        
        if ( action == MY_DEC_ACTION )
        {
            /*
             * Since we defined our actions to only accept the pressed state,
             * we don't need to ask for the state here.
             */
            
            value.update( value.getValue() - 1 );
        }
        else if ( action == MY_INC_ACTION )
        {
            value.update( value.getValue() + 1 );
        }
        
        return ( result );
    }
    
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int width, int height )
    {
        /*
         * Just to play around with the parameters we define the text to be drawn at the center location this time.
         */
        int h = TextureImage2D.getStringHeight( "0", getFont(), isFontAntiAliased() );
        ds = drawnStringFactory.newDrawnString( "ds", width / 2, ( height - h ) / 2, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        value.update( 100 );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw || ( clock.c() && value.hasChanged() ) )
        {
            ds.draw( offsetX, offsetY, value.getValueAsString(), texture );
        }
    }
    
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
    }
    
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
    }
    
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
    }
    
    public Lesson5Widget_InputActions()
    {
        super( 5.0f, 5.0f );
    }
}
