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
     * {@inheritDoc}
     */
    @Override
    protected void onKeeperSet()
    {
        super.onKeeperSet();
        
        onValueChanged( null, getValue() );
    }
    
    /**
     * Invoked when the property's value has changed.
     * 
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    /**
     * Invoked when the property's value has been set.
     * 
     * @param value the new value
     */
    void onValueSet( String value )
    {
    }
    
    protected final boolean setFilenameValue( String value, boolean firstTime )
    {
        String oldValue = firstTime ? null : this.value;
        
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
        
        onValueSet( this.value );
        
        if ( !firstTime )
        {
            triggerKeepersOnPropertyChanged( oldValue, value );
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
    public final boolean setFilenameValue( String value )
    {
        return ( setFilenameValue( value, false ) );
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
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param base the base folder
     * @param readonly read only property?
     */
    protected PathnameProperty( String name, String nameForDisplay, String defaultValue, File base, boolean readonly )
    {
        super( name, nameForDisplay, readonly, PropertyEditorType.FILENAME, "...", "Browse file..." );
        
        this.base = base;
        this.basePath = FileUtils.getCanonicalFile( base ).getAbsolutePath() + File.separator;
        
        setFilenameValue( defaultValue, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    protected PathnameProperty( String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( name, nameForDisplay, defaultValue, GameFileSystem.INSTANCE.getConfigFolder(), readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public PathnameProperty( String name, String nameForDisplay, String defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public PathnameProperty( String name, String defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public PathnameProperty( String name, String defaultValue )
    {
        this( name, defaultValue, false );
    }
}
