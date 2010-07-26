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
package net.ctdp.rfdynhud.widgets.widget;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.ctdp.rfdynhud.util.Logger;

/**
 * The {@link WidgetPackage} class encapsulates properties for a {@link Widget}'s virtual package.
 * 
 * @author Marvin Froehlich
 */
public class WidgetPackage implements Comparable<WidgetPackage>
{
    private final String name;
    private final URL[] iconURLs;
    private Icon[] icons = null;
    
    /**
     * Gets the package's name.
     * 
     * @return the package's name.
     */
    public final String getName()
    {
        return ( name );
    }
    
    private static Icon[] createIconArray( URL[] iconURLs )
    {
        if ( ( iconURLs == null ) || ( iconURLs.length == 0 ) )
            return ( null );
        
        Icon[] icons = new Icon[ iconURLs.length ];
        
        for ( int i = 0; i < iconURLs.length; i++ )
        {
            if ( iconURLs[i] == null )
            {
                icons[i] = null;
            }
            else
            {
                try
                {
                    icons[i] = new ImageIcon( ImageIO.read( iconURLs[i] ) );
                }
                catch ( IOException e )
                {
                    Logger.log( e );
                    
                    icons[i] = null;
                }
            }
        }
        
        return ( icons );
    }
    
    /**
     * Gets the package's icons to be displayed in the editor.
     * 
     * @return the package's icon for the editor or <code>null</code>.
     */
    public final Icon[] getIcons()
    {
        if ( icons == null )
        {
            icons = createIconArray( iconURLs );
        }
        
        return ( icons );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( !( o instanceof WidgetPackage ) )
            return ( false );
        
        return ( name.equals( ( (WidgetPackage)o ).name ) );
    }
    
    @Override
    public int compareTo( WidgetPackage o )
    {
        return ( String.CASE_INSENSITIVE_ORDER.compare( this.name, o.name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ( name.hashCode() );
    }
    
    /**
     * Creates a new {@link WidgetPackage} instance.
     * 
     * @param name the package name. This can be <code>null</code> or an empty string to denote the root of the menu or a slash separated path.
     * @param iconURLs
     */
    public WidgetPackage( String name, URL... iconURLs )
    {
        this.name = ( name == null ) ? "" : name;
        this.iconURLs = ( iconURLs == null || iconURLs.length == 0 ) ? null : iconURLs;
    }
    
    private static URL[] createURLsArray( File[] iconFiles )
    {
        if ( ( iconFiles == null ) || ( iconFiles.length == 0 ) )
            return ( null );
        
        URL[] urls = new URL[ iconFiles.length ];
        
        for ( int i = 0; i < iconFiles.length; i++ )
        {
            if ( iconFiles[i] == null )
            {
                urls[i] = null;
            }
            else
            {
                try
                {
                    urls[i] = iconFiles[i].toURI().toURL();
                }
                catch ( MalformedURLException e )
                {
                    Logger.log( e );
                    
                    urls[i] = null;
                }
            }
        }
        
        return ( urls );
    }
    
    /**
     * Creates a new {@link WidgetPackage} instance.
     * 
     * @param name the package name. This can be <code>null</code> or an empty string to denote the root of the menu or a slash separated path.
     * @param iconFiles
     */
    public WidgetPackage( String name, File... iconFiles )
    {
        this( name, createURLsArray( iconFiles ) );
    }
    
    /**
     * Creates a new {@link WidgetPackage} instance.
     * 
     * @param name the package name. This can be <code>null</code> or an empty string to denote the root of the menu or a slash separated path.
     */
    public WidgetPackage( String name )
    {
        this( name, (URL[])null );
    }
}
