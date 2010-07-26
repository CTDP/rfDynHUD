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
package net.ctdp.rfdynhud.editor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;

import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

public class WidgetMenu extends JMenu
{
    private static final long serialVersionUID = 2448705699133558571L;
    
    //private final WidgetPackage widgetPackage;
    //private final int pathIndex;
    
    private static Icon getResizedIcon( Icon icon )
    {
        if ( ( icon == null ) || ( ( icon.getIconWidth() <= WidgetMenuItem.ICON_WIDTH ) && ( icon.getIconHeight() <= WidgetMenuItem.ICON_HEIGHT ) ) )
            return ( icon );
        
        float scaleX = (float)WidgetMenuItem.ICON_WIDTH / icon.getIconWidth();
        float scaleY = (float)WidgetMenuItem.ICON_HEIGHT / icon.getIconHeight();
        float scale = Math.min( scaleX, scaleY );
        
        BufferedImage bi0 = new BufferedImage( icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
        BufferedImage bi1 = new BufferedImage( (int)( icon.getIconWidth() * scale ), (int)( icon.getIconHeight() * scale ), BufferedImage.TYPE_INT_ARGB );
        
        Graphics2D g20 = bi0.createGraphics();
        g20.setBackground( new Color( 0, 0, 0, 0 ) );
        g20.clearRect( 0, 0, bi0.getWidth(), bi0.getHeight() );
        
        icon.paintIcon( null, g20, 0, 0 );
        
        Graphics2D g21 = bi1.createGraphics();
        g21.setBackground( new Color( 0, 0, 0, 0 ) );
        g21.clearRect( 0, 0, bi1.getWidth(), bi1.getHeight() );
        
        g21.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
        g21.drawImage( bi0, 0, 0, bi1.getWidth(), bi1.getHeight(), null );
        
        return ( new ImageIcon( bi1 ) );
    }
    
    public WidgetMenu( String name, WidgetPackage widgetPackage, int pathIndex )
    {
        super( name );
        
        //this.widgetPackage = widgetPackage;
        //this.pathIndex = pathIndex;
        
        Icon icon = null;
        if ( widgetPackage != null )
        {
            if ( widgetPackage.getIcons() != null )
            {
                Icon[] icons = widgetPackage.getIcons();
                
                if ( icons.length >= pathIndex )
                {
                    icon = icons[pathIndex];
                }
            }
        }
        
        if ( icon != null )
            this.setIcon( getResizedIcon( icon ) );
    }
}
