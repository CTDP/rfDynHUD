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

import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link Property} serves as a general data container and adapter.
 * You can use it to put data into a GUI component and live update it.
 * 
 * @author Marvin Froehlich
 */
public abstract class Property
{
    protected final WidgetsConfiguration widgetsConfig;
    protected final Widget widget;
    
    private final String name;
    private final String nameForDisplay;
    private final boolean readonly;
    private final PropertyEditorType editorType;
    private final String buttonText;
    private final String buttonTooltip;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    public final String getPropertyName()
    {
        return ( name );
    }
    
    public String getNameForDisplay()
    {
        return ( nameForDisplay );
    }
    
    public final boolean isReadOnly()
    {
        return ( readonly );
    }
    
    public final PropertyEditorType getEditorType()
    {
        return ( editorType );
    }
    
    public String getButtonText()
    {
        return ( buttonText );
    }
    
    public String getButtonTooltip()
    {
        return ( buttonTooltip );
    }
    
    public abstract void setValue( Object value );
    
    public abstract Object getValue();
    
    protected void onValueChanged()
    {
        if ( widgetsConfig != null )
        {
            for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
                widgetsConfig.getWidget( i ).forceAndSetDirty();
        }
        
        if ( widget != null )
            widget.forceAndSetDirty();
    }
    
    /**
     * 
     * @param button
     */
    public void onButtonClicked( Object button )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( this.getClass().getSimpleName() + "( \"" + getPropertyName() + "\" = \"" + String.valueOf( getValue() ) + "\" )" );
    }
    
    /**
     * 
     * @param widgetsConfig
     * @param name
     * @param nameForDisplay
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    protected Property( WidgetsConfiguration widgetsConfig, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.widgetsConfig = widgetsConfig;
        this.widget = null;
        this.name = name;
        this.nameForDisplay = nameForDisplay;
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
    }
    
    /**
     * 
     * @param widget
     * @param name
     * @param nameForDisplay
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    public Property( Widget widget, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.widgetsConfig = null;
        this.widget = widget;
        this.name = name;
        this.nameForDisplay = ( nameForDisplay == null ) ? name : nameForDisplay;
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
    }
    
    public Property( Widget widget, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType )
    {
        this( widget, name, nameForDisplay, readonly, editorType, null, null );
    }
    
    public Property( Widget widget, String name, String nameForDisplay, PropertyEditorType editorType )
    {
        this( widget, name, nameForDisplay, false, editorType, null, null );
    }
    
    /**
     * 
     * @param name
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    public Property( Widget widget, String name, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this( widget, name, name, readonly, editorType, buttonText, buttonTooltip );
    }
    
    public Property( Widget widget, String name, boolean readonly, PropertyEditorType editorType )
    {
        this( widget, name, readonly, editorType, null, null );
    }
    
    public Property( Widget widget, String name, PropertyEditorType editorType )
    {
        this( widget, name, false, editorType, null, null );
    }
}
