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
package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.render.BorderCache;
import net.ctdp.rfdynhud.render.BorderWrapper;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class BorderProperty extends Property
{
    public static final String DEFAULT_BORDER_NAME = "StandardBorder";
    
    private final WidgetsConfiguration widgetsConf;
    
    private String borderName;
    private BorderWrapper border = null;
    
    private final IntProperty paddingTop;
    private final IntProperty paddingLeft;
    private final IntProperty paddingRight;
    private final IntProperty paddingBottom;
    
    public static String getDefaultBorderValue( String name )
    {
        if ( name.equals( DEFAULT_BORDER_NAME ) )
            return ( "backgroundcolor_border.ini" );
        
        return ( null );
    }
    
    /**
     * 
     * @param oldValue
     * @param newValue
     */
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    public void refresh()
    {
        this.border = null;
    }
    
    public void setBorder( String borderName )
    {
        if ( ( ( borderName == null ) && ( this.borderName == null ) ) || ( ( borderName != null ) && borderName.equals( this.borderName ) ) )
            return;
        
        String oldValue = this.borderName;
        this.borderName = borderName;
        this.border = null;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( oldValue, borderName );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, borderName, widget );
    }
    
    public final String getBorderName()
    {
        return ( borderName );
    }
    
    public final BorderWrapper getBorder()
    {
        if ( border == null )
        {
            if ( ( borderName == null ) || borderName.equals( "" ) )
            {
                border = new BorderWrapper( null, null, paddingTop, paddingLeft, paddingRight, paddingBottom );
            }
            else
            {
                final WidgetsConfiguration widgetsConf = ( widget != null ) ? widget.getConfiguration() : this.widgetsConf;
                
                String borderValue = widgetsConf.getBorderName( borderName );
                
                if ( ( borderValue == null ) && ( widget != null ) )
                {
                    String borderValue2 = widget.getDefaultBorderValue( borderName );
                    if ( borderValue2 != null )
                    {
                        borderValue = borderValue2;
                        widgetsConf.addBorderAlias( borderName, borderValue );
                    }
                }
                
                if ( borderValue == null )
                    borderValue = borderName;
                
                border = BorderCache.getBorder( borderValue, paddingTop, paddingLeft, paddingRight, paddingBottom );
            }
        }
        
        return ( border );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setBorder( String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( borderName );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            setValue( value );
            
            return ( true );
        }
        
        return ( false );
    }
    
    private BorderProperty( WidgetsConfiguration widgetsConf, Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly, IntProperty paddingTop, IntProperty paddingLeft, IntProperty paddingRight, IntProperty paddingBottom )
    {
        super( widget, propertyName, nameForDisplay, readonly, PropertyEditorType.BORDER, null, null );
        
        this.widgetsConf = widgetsConf;
        this.borderName = defaultValue;
        
        this.paddingTop = paddingTop;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        this.paddingBottom = paddingBottom;
    }
    
    public BorderProperty( WidgetsConfiguration widgetsConf, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( widgetsConf, null, propertyName, nameForDisplay, defaultValue, readonly, null, null, null, null );
    }
    
    public BorderProperty( WidgetsConfiguration widgetsConf, String propertyName, String nameForDisplay, String defaultValue )
    {
        this( widgetsConf, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public BorderProperty( WidgetsConfiguration widgetsConf, String propertyName, String defaultValue, boolean readonly )
    {
        this( widgetsConf, propertyName, propertyName, defaultValue, readonly );
    }
    
    public BorderProperty( WidgetsConfiguration widgetsConf, String propertyName, String defaultValue )
    {
        this( widgetsConf, propertyName, defaultValue, false );
    }
    
    public BorderProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( null, widget, propertyName, nameForDisplay, defaultValue, readonly, null, null, null, null );
    }
    
    public BorderProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public BorderProperty( Widget widget, String propertyName, String defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public BorderProperty( Widget widget, String propertyName, String defaultValue, IntProperty paddingTop, IntProperty paddingLeft, IntProperty paddingRight, IntProperty paddingBottom )
    {
        this( null, widget, propertyName, propertyName, defaultValue, false, paddingTop, paddingLeft, paddingRight, paddingBottom );
    }
    
    public BorderProperty( Widget widget, String propertyName, String defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
    
    /*
    public static final BorderWrapper getBorderFromBorderName( String borderName, BorderWrapper border, WidgetsConfiguration widgetsConfig )
    {
        if ( border == null )
        {
            if ( ( borderName == null ) || borderName.equals( "" ) )
            {
                border = new BorderWrapper( null, null );
            }
            else
            {
                String borderName_ = widgetsConfig.getBorderName( borderName );
                
                if ( borderName_ == null )
                    border = BorderCache.getBorder( borderName );
                else
                    border = BorderCache.getBorder( borderName_ );
            }
        }
        
        return ( border );
    }
    */
}
