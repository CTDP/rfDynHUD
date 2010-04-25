package net.ctdp.rfdynhud.input;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.jagatoo.util.classes.ClassSearcher;
import org.jagatoo.util.classes.PackageSearcher;
import org.jagatoo.util.classes.SuperClassCriterium;

public class KnownInputActions
{
    public static final InputAction TogglePlugin = new InputAction( "TogglePlugin", true, false, null );
    public static final InputAction ToggleWidgetVisibility = new InputAction( "ToggleWidgetVisibility", true );
    public static final InputAction IncBoost = new InputAction( "IncBoost", true, false, null );
    public static final InputAction DecBoost = new InputAction( "DecBoost", true, false, null );
    public static final InputAction TempBoost = new InputAction( "TempBoost", null, false, null );
    public static final InputAction ResetFuelConsumption = __GDPrivilegedAccess.INPUT_ACTION_RESET_FUEL_CONSUMPTION;
    public static final InputAction ResetTopSpeeds = __GDPrivilegedAccess.INPUT_ACTION_RESET_TOPSPEEDS;
    
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
            addAction( IncBoost );
            addAction( DecBoost );
            addAction( TempBoost );
            addAction( ResetFuelConsumption );
            addAction( ResetTopSpeeds );
            
            try
            {
                List<String> packages = PackageSearcher.findPackages( "*widgets*" );
                List<Class<?>> classes = ClassSearcher.findClasses( new SuperClassCriterium( Widget.class, false ), packages.toArray( new String[ packages.size() ] ) );
                
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
                                addAction( ia );
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
