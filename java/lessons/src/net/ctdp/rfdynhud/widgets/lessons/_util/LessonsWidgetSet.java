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
package net.ctdp.rfdynhud.widgets.lessons._util;

import java.awt.Font;

import net.ctdp.rfdynhud.util.FontUtils;
import net.ctdp.rfdynhud.util.StringMapping;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetSet;

/**
 * This {@link WidgetSet} implementation serves as a utility class and keeps the {@link WidgetPackage} instance for each Lesson {@link Widget}.
 * Each Lesson {@link Widget} must also get a reference to an instance of this class.
 * 
 * @author Marvin Froehlich
 */
public class LessonsWidgetSet extends WidgetSet
{
    /*
     * Each Lesson {@link Widget} must also get a reference to an instance of this class.
     * Hence we keep a static one here.
     */
    public static final LessonsWidgetSet INSTANCE = new LessonsWidgetSet();
    
    /*
     * This is a virtual package, that you can use to group your Widgets in the editor.
     * 
     * The value should always come from a static place like we do it here
     * to make sure, that it is exactly the same for all contained Widgets.
     * Though only the package name is the really important part.
     */
    public static final WidgetPackage WIDGET_PACKAGE = new WidgetPackage( INSTANCE, "CTDP/Lessons", WidgetPackage.CTDP_ICON, INSTANCE.getIcon( "net/ctdp/rfdynhud/widgets/lessons/lessons.png" ) );
    
    public static final StringMapping MY_FONT_COLOR = new StringMapping( "MyFontColor", "#FF0000" );
    
    public static final StringMapping MY_FONT = new StringMapping( "MyFont", FontUtils.getFontString( "Monospaced", Font.BOLD, 13, true, true ) );
    
    @Override
    public String getDefaultNamedColorValue( String name )
    {
        if ( name.equals( MY_FONT_COLOR.getKey() ) )
            return ( MY_FONT_COLOR.getValue() );
        
        return ( null );
    }
    
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        if ( name.equals( MY_FONT.getKey() ) )
            return ( MY_FONT.getValue() );
        
        return ( null );
    }
    
    private LessonsWidgetSet()
    {
        /*
         * This is nothing special and also not complicated.
         * This simply creates a version id, which can be used
         * to cheaply compare different versions of this WidgetPackage.
         * Make sure to increase the version number, at least
         * if you release a new revision of your Widget set with
         * more or less significant changes.
         */
        super( composeVersion( 1, 2, 0 ) );
    }
}
