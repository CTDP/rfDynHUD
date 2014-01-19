/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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
package net.ctdp.rfdynhud.widgets.base.widget;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

import org.jagatoo.util.classes.ClassSearcher;
import org.jagatoo.util.classes.SuperClassCriterium;

/**
 * Creates {@link Widget} instances by class name.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetFactory
{
    private static List<Class<Widget>> widgetClasses = null;
    private static Map<String, Class<Widget>> widgetClassNameClassMap = null;
    
    private static Collection<String> excludedJars = null;
    
    public static void setExcludedJars( Collection<String> excludedJars )
    {
        WidgetFactory.excludedJars = excludedJars;
    }
    
    private static void findWidgetSetJars( File folder, List<URL> jars )
    {
        for ( File f : folder.listFiles() )
        {
            if ( f.isDirectory() )
            {
                if ( !f.getName().equals( ".svn" ) )
                    findWidgetSetJars( f, jars );
            }
            else
            {
                String filenameLC = f.getAbsolutePath().toLowerCase();
                
                if ( filenameLC.endsWith( ".jar" ) )
                {
                    boolean accepted = true;
                    
                    if ( excludedJars != null )
                    {
                        for ( String exclJar : excludedJars )
                        {
                            if ( !accepted )
                                break;
                            
                            if ( filenameLC.endsWith( exclJar ) )
                                accepted = false;
                        }
                    }
                    
                    if ( accepted )
                    {
                        try
                        {
                            jars.add( f.toURI().toURL() );
                        }
                        catch ( Throwable t )
                        {
                            RFDHLog.exception( t );
                        }
                    }
                }
            }
        }
    }
    
    private static URL[] findWidgetSetJars( File widgetSetsFolder )
    {
        ArrayList<URL> jars = new ArrayList<URL>();
        
        for ( File f : widgetSetsFolder.listFiles() )
        {
            if ( f.isDirectory() && !f.getName().equals( ".svn" ) )
            {
                findWidgetSetJars( f, jars );
            }
        }
        
        URL[] urls = new URL[ jars.size() ];
        
        return ( jars.toArray( urls ) );
    }
    
    /**
     * Finds all publicly available and non-abstract {@link Widget} classes.
     * 
     * @param widgetSetsFolder
     * 
     * @return all non-abstract {@link Widget} classes in the classpath.
     */
    @SuppressWarnings( "unchecked" )
    private static List<Class<Widget>> findWidgetClasses( File widgetSetsFolder )
    {
        List<Class<?>> classes_;
        if ( ( widgetSetsFolder != null ) && widgetSetsFolder.exists() )
        {
            URLClassLoader classLoader = new URLClassLoader( findWidgetSetJars( widgetSetsFolder ), Widget.class.getClassLoader() );
            
            try
            {
                classes_ = ClassSearcher.findClasses( true, classLoader, new SuperClassCriterium( Widget.class, false ) );
            }
            catch ( IOException e )
            {
                RFDHLog.exception( e );
                classes_ = null;
            }
        }
        else
        {
            classes_ = ClassSearcher.findClasses( new SuperClassCriterium( Widget.class, false ) );
        }
        
        ArrayList<Class<Widget>> classes = new ArrayList<Class<Widget>>();
        for ( int i = 0; i < classes_.size(); i++ )
        {
            Class<Widget> clazz = (Class<Widget>)classes_.get( i );
            
            if ( !clazz.isAnnotationPresent( Hidden.class ) )
                classes.add( clazz );
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
    
    public static void init( File widgetSetsFolder )
    {
        if ( widgetClasses != null )
            return;
        
        widgetClasses = findWidgetClasses( widgetSetsFolder );
        
        widgetClassNameClassMap = new HashMap<String, Class<Widget>>();
        for ( Class<Widget> clazz : widgetClasses )
        {
            widgetClassNameClassMap.put( clazz.getName(), clazz );
        }
    }
    
    @SuppressWarnings( "unchecked" )
    public static final Class<Widget>[] getWidgetClasses()
    {
        return ( widgetClasses.toArray( new Class[ widgetClasses.size() ] ) );
    }
    
    private static final Throwable getRootCause( Throwable t )
    {
        if ( t.getCause() == null )
            return ( t );
        
        return ( getRootCause( t.getCause() ) );
    }
    
    /**
     * Creates a new {@link Widget} instance.
     * 
     * @param clazz the {@link Widget} class
     * @param name the name to be given to the new {@link Widget} or <code>null</code>
     * @param widgetsConfig the {@link WidgetsConfiguration} to search a free name in
     * @param loadingAssembled
     * 
     * @return the create {@link Widget} or <code>null</code> on error.
     */
    private static Widget createWidget( Class<Widget> clazz, String name, WidgetsConfiguration widgetsConfig, boolean loadingAssembled )
    {
        Widget widget = null;
        
        try
        {
            if ( loadingAssembled )
                widget = clazz.getConstructor( boolean.class ).newInstance( false );
            else
                widget = clazz.getConstructor().newInstance();
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( getRootCause( t ) );
            
            return ( null );
        }
        
        if ( name != null )
        {
            widget.setName( name );
        }
        else if ( widgetsConfig != null )
        {
            widget.setName( widgetsConfig.findFreeName( clazz.getSimpleName() ) );
        }
        
        return ( widget );
    }
    
    private static String getOldClassName( String className )
    {
        if ( className.startsWith( "net.ctdp.rfdynhud." ) )
        {
            if ( className.startsWith( "etv2010.widgets.", "net.ctdp.rfdynhud.".length() ) )
                return ( "net.ctdp.rfdynhud.widgets.etv2010." + className.substring( "net.ctdp.rfdynhud.etv2010.widgets.".length() ) );
            
            if ( className.startsWith( "widgets.", "net.ctdp.rfdynhud.".length() ) )
            {
                if ( className.startsWith( "controls.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "dashboard.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "fuel.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "fuelneedle.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "image.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "map.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "misc.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "revmeter.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "rideheight.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "speedo.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "standings.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "startinglight.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "temperatures.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "timecomp.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "timing.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "trackposition.", "net.ctdp.rfdynhud.widgets.".length() )
                  || className.startsWith( "wear.", "net.ctdp.rfdynhud.widgets.".length() )
                   )
                    return ( "net.ctdp.rfdynhud.widgets.standard." + className.substring( "net.ctdp.rfdynhud.widgets.".length() ) );
            }
        }
        
        return ( null );
    }
    
    /**
     * Gets the {@link Widget} {@link Class} instance.
     * 
     * @param className the {@link Widget} class name
     * 
     * @return the {@link Widget} {@link Class} instance or <code>null</code> on error.
     */
    public static Class<Widget> getWidgetClass( String className )
    {
        Class<Widget> clazz = widgetClassNameClassMap.get( className );
        
        if ( clazz == null )
        {
            // branch for backwards compatiblity with old package names...
            
            String oldClassName = getOldClassName( className );
            
            if ( oldClassName == null )
            {
                return ( null );
            }
            
            clazz = widgetClassNameClassMap.get( oldClassName );
        }
        
        return ( clazz );
    }
    
    /**
     * Creates a new {@link Widget} instance.
     * 
     * @param className the {@link Widget} class name
     * @param name the name to be given to the new {@link Widget} or <code>null</code>
     * @param widgetsConfig the {@link WidgetsConfiguration} to search a free name in
     * 
     * @return the create {@link Widget} or <code>null</code> on error.
     */
    private static Widget createWidget( String className, String name, WidgetsConfiguration widgetsConfig )
    {
        Class<Widget> clazz = getWidgetClass( className );
        
        if ( clazz == null )
            return ( null );
        
        return ( createWidget( clazz, name, widgetsConfig, false ) );
    }
    
    /**
     * Creates a new {@link Widget} instance.
     * 
     * @param clazz the {@link Widget} class
     * @param name the name to be given to the new {@link Widget} or <code>null</code>
     * 
     * @return the create {@link Widget} or <code>null</code> on error.
     */
    public static Widget createWidget( Class<Widget> clazz, String name )
    {
        if ( name == null )
            throw new IllegalArgumentException( "name must not be null" );
        
        return ( createWidget( clazz, name, null, false ) );
    }
    
    /**
     * Creates a new {@link Widget} instance.
     * 
     * @param clazz the {@link Widget} class
     * @param widgetsConfig the {@link WidgetsConfiguration} to search a free name in
     * 
     * @return the create {@link Widget} or <code>null</code> on error.
     */
    public static Widget createWidget( Class<Widget> clazz, WidgetsConfiguration widgetsConfig )
    {
        if ( widgetsConfig == null )
            throw new IllegalArgumentException( "widgetsConfig must not be null" );
        
        return ( createWidget( clazz, null, widgetsConfig, false ) );
    }
    
    /**
     * Creates a new {@link Widget} instance.
     * 
     * @param className the {@link Widget} class name
     * @param name the name to be given to the new {@link Widget} or <code>null</code>
     * 
     * @return the create {@link Widget} or <code>null</code> on error.
     */
    public static Widget createWidget( String className, String name )
    {
        if ( name == null )
            throw new IllegalArgumentException( "name must not be null" );
        
        return ( createWidget( className, name, null ) );
    }
    
    /**
     * Creates a new {@link Widget} instance.
     * 
     * @param className the {@link Widget} class name
     * @param widgetsConfig the {@link WidgetsConfiguration} to search a free name in
     * 
     * @return the create {@link Widget} or <code>null</code> on error.
     */
    public static Widget createWidget( String className, WidgetsConfiguration widgetsConfig )
    {
        if ( widgetsConfig == null )
            throw new IllegalArgumentException( "widgetsConfig must not be null" );
        
        return ( createWidget( className, null, widgetsConfig ) );
    }
    
    /**
     * Creates a new {@link Widget} instance.
     * 
     * @param className the {@link Widget} class name
     * @param name the name to be given to the new {@link Widget} or <code>null</code>
     * 
     * @return the create {@link Widget} or <code>null</code> on error.
     */
    public static AbstractAssembledWidget createAssembledWidget( String className, String name )
    {
        if ( name == null )
            throw new IllegalArgumentException( "name must not be null" );
        
        Class<Widget> clazz = getWidgetClass( className );
        
        if ( clazz == null )
            return ( null );
        
        if ( !AbstractAssembledWidget.class.isAssignableFrom( clazz ) )
        {
            RFDHLog.error( "ERROR: The given class " + className + " is not a sub class of " + AbstractAssembledWidget.class.getName() + "." );
            
            return ( null );
        }
        
        return ( (AbstractAssembledWidget)createWidget( clazz, name, null, true ) );
    }
    
    private WidgetFactory()
    {
    }
}
