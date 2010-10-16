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
package net.ctdp.rfdynhud.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jagatoo.util.classes.ClassSearcher;
import org.jagatoo.util.classes.PackageSearcher;
import org.jagatoo.util.classes.SuperClassCriterium;

import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * Utility mathods for {@link Widget}s.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetTools
{
    /**
     * Finds all publicly available and non-abstract {@link Widget} classes.
     * 
     * @return all non-abstract {@link Widget} classes in the classpath.
     */
    @SuppressWarnings( "unchecked" )
    public static List<Class<Widget>> findWidgetClasses()
    {
        List<String> packages = PackageSearcher.findPackages( "*widgets*" );
        ArrayList<String> tmp = new ArrayList<String>();
        
        for ( String pkg : packages )
        {
            if ( !pkg.startsWith( "net.ctdp.rfdynhud.widgets.hidden" ) )
                tmp.add( pkg );
        }
        packages = tmp;
        
        List<Class<?>> classes_ = ClassSearcher.findClasses( new SuperClassCriterium( Widget.class, false ), packages.toArray( new String[ packages.size() ] ) );
        ArrayList<Class<Widget>> classes = new ArrayList<Class<Widget>>();
        
        for ( Class<?> c : classes_ )
        {
            if ( !c.getPackage().getName().startsWith( "net.ctdp.rfdynhud.widgets.hidden" ) )
                classes.add( (Class<Widget>)c );
        }
        
        Collections.sort( classes, new Comparator<Class<Widget>>()
        {
            @Override
            public int compare( Class<Widget> o1, Class<Widget> o2 )
            {
                return ( String.CASE_INSENSITIVE_ORDER.compare( o1.getSimpleName(), o2.getSimpleName() ) );
            }
        } );
        
        //return ( classes.toArray( new Class[ classes.size() ] ) );
        return ( classes );
    }
    
    public static final Comparator<Widget> WIDGET_Y_X_COMPARATOR = new Comparator<Widget>()
    {
        @Override
        public int compare( Widget w1, Widget w2 )
        {
            int y1 = w1.getPosition().getEffectiveY();
            int y2 = w2.getPosition().getEffectiveY();
            
            if ( y1 < y2 )
                return ( -1 );
            
            if ( y1 > y2 )
                return ( +1 );
            
            int x1 = w1.getPosition().getEffectiveX();
            int x2 = w2.getPosition().getEffectiveX();
            
            if ( x1 < x2 )
                return ( -1 );
            
            if ( x1 > x2 )
                return ( +1 );
            
            return ( 0 );
        }
    };
}
