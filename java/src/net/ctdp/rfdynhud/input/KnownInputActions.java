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
package net.ctdp.rfdynhud.input;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.WidgetTools;
import net.ctdp.rfdynhud.widgets.widget.Widget;

public class KnownInputActions
{
    private static URL getDocURL( String actionName )
    {
        return ( KnownInputActions.class.getClassLoader().getResource( KnownInputActions.class.getPackage().getName().replace( '.', '/' ) + "/doc/" + actionName + ".html" ) );
    }
    
    private static URL getDocURL( InputAction action, Widget widget )
    {
        return ( widget.getClass().getClassLoader().getResource( widget.getClass().getPackage().getName().replace( '.', '/' ) + "/doc/" + action.getName() + ".html" ) );
    }
    
    public static final InputAction TogglePlugin = new InputAction( "TogglePlugin", true, false, null, getDocURL( "TogglePlugin" ) );
    public static final InputAction ToggleWidgetVisibility = new InputAction( "ToggleWidgetVisibility", true, true, null, getDocURL( "ToggleWidgetVisibility" ) );
    public static final InputAction ToggleFixedViewedVehicle = new InputAction( "ToggleFixedViewedVehicle", true, false, null, getDocURL( "ToggleFixedViewedVehicle" ) );
    public static final InputAction IncBoost = new InputAction( "IncBoost", true, false, null, getDocURL( "IncBoost" ) );
    public static final InputAction DecBoost = new InputAction( "DecBoost", true, false, null, getDocURL( "DecBoost" ) );
    public static final InputAction TempBoost = new InputAction( "TempBoost", null, false, null, getDocURL( "TempBoost" ) );
    public static final InputAction ResetFuelConsumption = __GDPrivilegedAccess.INPUT_ACTION_RESET_FUEL_CONSUMPTION;
    public static final InputAction ResetTopSpeeds = __GDPrivilegedAccess.INPUT_ACTION_RESET_TOPSPEEDS;
    public static final InputAction ResetLaptimesCache = __GDPrivilegedAccess.INPUT_ACTION_RESET_LAPTIMES_CACHE;
    
    private static HashMap<String, InputAction> knownActions = null;
    
    private static void addAction( InputAction action )
    {
        knownActions.put( action.getName(), action );
    }
    
    private static final void init()
    {
        if ( knownActions == null )
        {
            knownActions = new HashMap<String, InputAction>();
            
            addAction( TogglePlugin );
            addAction( ToggleWidgetVisibility );
            addAction( ToggleFixedViewedVehicle );
            addAction( IncBoost );
            addAction( DecBoost );
            addAction( TempBoost );
            addAction( ResetFuelConsumption );
            addAction( ResetTopSpeeds );
            addAction( ResetLaptimesCache );
            
            try
            {
                List<Class<Widget>> classes = WidgetTools.findWidgetClasses();
                
                for ( int i = 0; i < classes.size(); i++ )
                {
                    Class<?> c = classes.get( i );
                    
                    Widget widget = (Widget)c.getConstructor( String.class ).newInstance( "" );
                    
                    InputAction[] ias = widget.getInputActions();
                    
                    if ( ias != null )
                    {
                        for ( int j = 0; j < ias.length; j++ )
                        {
                            InputAction ia = ias[j];
                            if ( ia != null )
                            {
                                ia.setDoc( getDocURL( ia, widget ) );
                                
                                addAction( ia );
                            }
                        }
                    }
                }
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    public static final InputAction get( String name )
    {
        init();
        
        return ( knownActions.get( name ) );
    }
    
    public static final InputAction[] getAll()
    {
        init();
        
        InputAction[] actions = new InputAction[ knownActions.size() ];
        int i = 0;
        for ( InputAction action : knownActions.values() )
        {
            actions[i++] = action;
        }
        
        Arrays.sort( actions );
        
        return ( actions );
    }
}
