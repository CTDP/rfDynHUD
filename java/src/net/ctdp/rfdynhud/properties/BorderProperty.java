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
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * The {@link BorderProperty} serves for customizing a border.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BorderProperty extends Property
{
    public static final String DEFAULT_BORDER_NAME = "StandardBorder";
    
    private String borderAlias;
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
     * {@inheritDoc}
     */
    @Override
    protected void onKeeperSet()
    {
        super.onKeeperSet();
        
        onValueChanged( null, getValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    boolean setKeeper( PropertiesKeeper keeper, boolean force )
    {
        boolean result = super.setKeeper( keeper, force );
        
        if ( result )
        {
            if ( paddingTop != null )
                paddingTop.setKeeper( keeper, force );
            if ( paddingLeft != null )
                paddingLeft.setKeeper( keeper, force );
            if ( paddingRight != null )
                paddingRight.setKeeper( keeper, force );
            if ( paddingBottom != null )
                paddingBottom.setKeeper( keeper, force );
        }
        
        return ( result );
    }
    
    /**
     * Invoked when the value has changed.
     * 
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    /**
     * Invoked when the value has been set.
     * 
     * @param value the new value
     */
    void onValueSet( String value )
    {
    }
    
    /**
     * Must be called after border aliases have changed in the editor.
     */
    public void refresh()
    {
        this.border = null;
    }
    
    /**
     * Sets the property's value.
     * 
     * @param borderAliasOrName the new border
     * @param firstTime
     * 
     * @return changed?
     */
    protected final boolean setBorder( String borderAliasOrName, boolean firstTime )
    {
        if ( ( ( borderAliasOrName == null ) && ( this.borderAlias == null ) ) || ( ( borderAliasOrName != null ) && borderAliasOrName.equals( this.borderAlias ) ) )
            return ( false );
        
        String oldValue = firstTime ? null : this.borderAlias;
        this.borderAlias = borderAliasOrName;
        this.border = null;
        
        onValueSet( this.borderAlias );
        
        if ( !firstTime )
        {
            triggerKeepersOnPropertyChanged( oldValue, borderAliasOrName );
            onValueChanged( oldValue, borderAliasOrName );
        }
        
        return ( true );
    }
    
    /**
     * Sets the property's value.
     * 
     * @param borderAliasOrName the new border
     * 
     * @return changed?
     */
    public final boolean setBorder( String borderAliasOrName )
    {
        return ( setBorder( borderAliasOrName, false ) );
    }
    
    /**
     * Gets the property's current value.
     * 
     * @return the property's current value.
     */
    public final String getBorderAlias()
    {
        return ( borderAlias );
    }
    
    /**
     * Gets the selected border.
     * 
     * @return the selected border.
     */
    public final BorderWrapper getBorder()
    {
        if ( border == null )
        {
            if ( ( borderAlias == null ) || borderAlias.equals( "" ) )
            {
                border = new BorderWrapper( null, null, paddingTop, paddingLeft, paddingRight, paddingBottom );
            }
            else
            {
                final Widget widget = (Widget)getKeeper();
                final WidgetsConfiguration widgetsConf = widget.getConfiguration();
                
                String borderValue = widgetsConf.getBorderName( borderAlias );
                
                if ( borderValue == null )
                {
                    String borderValue2 = widget.getDefaultBorderValue( borderAlias );
                    if ( borderValue2 != null )
                    {
                        borderValue = borderValue2;
                        widgetsConf.addBorderAlias( borderAlias, borderValue );
                    }
                }
                
                if ( borderValue == null )
                    borderValue = borderAlias;
                
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
    public String getValue()
    {
        return ( borderAlias );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueForConfigurationFile()
    {
        if ( borderAlias == null )
            return ( "N/A" );
        
        return ( super.getValueForConfigurationFile() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        if ( ( value == null ) || value.equals( "N/A" ) )
            setBorder( null );
        else
            setBorder( value );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     * @param paddingTop
     * @param paddingLeft
     * @param paddingRight
     * @param paddingBottom
     */
    public BorderProperty( String name, String nameForDisplay, String defaultValue, boolean readonly, IntProperty paddingTop, IntProperty paddingLeft, IntProperty paddingRight, IntProperty paddingBottom )
    {
        super( name, nameForDisplay, readonly, PropertyEditorType.BORDER, null, null );
        
        this.paddingTop = paddingTop;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        this.paddingBottom = paddingBottom;
        
        setBorder( defaultValue, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param paddingTop top padding property
     * @param paddingLeft left padding property
     * @param paddingRight right padding property
     * @param paddingBottom bottom padding property
     */
    public BorderProperty( String name, String defaultValue, IntProperty paddingTop, IntProperty paddingLeft, IntProperty paddingRight, IntProperty paddingBottom )
    {
        this( name, null, defaultValue, false, paddingTop, paddingLeft, paddingRight, paddingBottom );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public BorderProperty( String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( name, nameForDisplay, defaultValue, readonly, null, null, null, null );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public BorderProperty( String name, String nameForDisplay, String defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public BorderProperty( String name, String defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public BorderProperty( String name, String defaultValue )
    {
        this( name, defaultValue, false );
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
