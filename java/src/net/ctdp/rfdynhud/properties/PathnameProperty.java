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

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

import org.jagatoo.util.io.FileUtils;

/**
 * The {@link PathnameProperty} serves for customizing a simple String value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class PathnameProperty extends Property
{
    private final File base;
    private final String basePath;
    
    private String value = null;
    private File file = null;
    
    /**
     * Invoked when the property's value has changed.
     * 
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    private boolean setFilenameValue( String value, boolean triggerOnChange )
    {
        String oldValue = this.value;
        
        File file = new File( value );
        
        if ( file.isAbsolute() )
        {
            if ( file.equals( this.file ) )
                return ( false );
            
            file = FileUtils.getCanonicalFile( file );
            
            this.file = file;
            
            if ( file.getAbsolutePath().startsWith( basePath ) )
                this.value = file.getAbsolutePath().substring( basePath.length() ).replace( '\\', '/' );
            else
                this.value = file.getAbsolutePath().replace( '\\', '/' );
        }
        else
        {
            file = new File( base, value );
            
            if ( file.equals( this.file ) )
                return ( false );
            
            this.file = file;
            this.value = value.replace( '\\', '/' );
        }
        
        if ( widget != null )
            widget.forceAndSetDirty( true );
        
        if ( triggerOnChange )
        {
            triggerCommonOnValueChanged( oldValue, value );
            if ( getTriggerOnValueChangedBeforeAttachedToConfig() || ( ( getWidget() != null ) && ( getWidget().getConfiguration() != null ) ) )
                onValueChanged( oldValue, value );
        }
        
        return ( true );
    }
    
    /**
     * Sets the property's new value.
     * 
     * @param value the new value
     * 
     * @return changed?
     */
    public boolean setFilenameValue( String value )
    {
        return ( setFilenameValue( value, true ) );
    }
    
    /**
     * Gets the property's current value.
     * 
     * @return the property's current value.
     */
    public final String getFilenameValue()
    {
        return ( value );
    }
    
    /**
     * Gets the property's current value.
     * 
     * @return the property's current value.
     */
    public final File getFileValue()
    {
        return ( file );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setFilenameValue( String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue()
    {
        return ( getFilenameValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        setValue( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onButtonClicked( Object button )
    {
        JComponent c = (JComponent)button;
        JFrame frame = (JFrame)c.getRootPane().getParent();
        
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory( this.file.getParentFile() );
        fc.setSelectedFile( this.file );
        
        fc.setMultiSelectionEnabled( false );
        fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        
        if ( fc.showOpenDialog( frame ) != JFileChooser.APPROVE_OPTION )
            return;
        
        setFilenameValue( fc.getSelectedFile().getAbsolutePath() );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param base the base folder
     * @param readonly read only property?
     */
    protected PathnameProperty( Widget widget, String name, String nameForDisplay, String defaultValue, File base, boolean readonly )
    {
        super( widget, name, nameForDisplay, readonly, PropertyEditorType.FILENAME, "...", "Browse file..." );
        
        this.base = base;
        this.basePath = FileUtils.getCanonicalFile( base ).getAbsolutePath() + File.separator;
        
        setFilenameValue( defaultValue, false );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    protected PathnameProperty( Widget widget, String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( widget, name, nameForDisplay, defaultValue, GameFileSystem.INSTANCE.getConfigFolder(), readonly );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public PathnameProperty( Widget widget, String name, String nameForDisplay, String defaultValue )
    {
        this( widget, name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public PathnameProperty( Widget widget, String name, String defaultValue, boolean readonly )
    {
        this( widget, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public PathnameProperty( Widget widget, String name, String defaultValue )
    {
        this( widget, name, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param base the base folder
     * @param readonly read only property?
     */
    protected PathnameProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, String defaultValue, File base, boolean readonly )
    {
        super( w2pf, name, nameForDisplay, readonly, PropertyEditorType.FILENAME, "...", "Browse file..." );
        
        this.base = base;
        this.basePath = FileUtils.getCanonicalFile( base ).getAbsolutePath() + File.separator;
        
        setFilenameValue( defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    protected PathnameProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( w2pf, name, nameForDisplay, defaultValue, GameFileSystem.INSTANCE.getConfigFolder(), readonly );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public PathnameProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, String defaultValue )
    {
        this( w2pf, name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public PathnameProperty( WidgetToPropertyForwarder w2pf, String name, String defaultValue, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public PathnameProperty( WidgetToPropertyForwarder w2pf, String name, String defaultValue )
    {
        this( w2pf, name, defaultValue, false );
    }
}
