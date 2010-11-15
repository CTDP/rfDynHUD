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
package net.ctdp.rfdynhud.editor.director.widgetstatesset;

import java.util.List;

import net.ctdp.rfdynhud.editor.director.DirectorManager;
import net.ctdp.rfdynhud.editor.director.DriverCapsule;
import net.ctdp.rfdynhud.editor.director.widgetstate.WidgetState;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.versioning.Version;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class StatesSetsLoader extends AbstractIniParser implements PropertyLoader
{
    private final DirectorManager manager;
    private final List<DriverCapsule> driversList;
    
    private String keyPrefix = null;
    
    private String currentKey = null;
    private String currentValue = null;
    
    private String effectiveKey = null;
    
    private Version sourceVersion = null;
    
    public void setKeyPrefix( String prefix )
    {
        this.keyPrefix = prefix;
        
        if ( keyPrefix == null )
            effectiveKey = currentKey;
        else
            effectiveKey = currentKey.substring( keyPrefix.length() );
    }
    
    @Override
    public String getCurrentKey()
    {
        return ( effectiveKey );
    }
    
    @Override
    public String getCurrentValue()
    {
        return ( currentValue );
    }
    
    @Override
    public Version getSourceVersion()
    {
        return ( sourceVersion );
    }
    
    @Override
    public boolean loadProperty( Property property )
    {
        if ( property.isMatchingKey( effectiveKey ) )
        {
            property.loadValue( this, currentValue );
            
            return ( true );
        }
        
        return ( false );
    }
    
    private boolean isInHeader = false;
    private WidgetStatesSet currentStatesSet = null;
    private WidgetState currentState = null;
    
    @Override
    protected boolean onGroupParsed( int lineNr, String group ) throws ParsingException
    {
        isInHeader = false;
        
        if ( group.equals( "HEADER" ) )
        {
            isInHeader = true;
        }
        else if ( group.equals( "WidgetStatesSet" ) )
        {
            if ( currentState != null )
                currentStatesSet.addState( currentState );
            
            if ( currentStatesSet != null )
                manager.addWidgetStatesSet( currentStatesSet );
            
            currentStatesSet = new WidgetStatesSet();
            
            currentState = null;
        }
        else if ( group.equals( "WidgetState" ) )
        {
            if ( currentState != null )
                currentStatesSet.addState( currentState );
            
            currentState = new WidgetState( driversList, manager );
        }
        
        return ( true );
    }
    
    @Override
    protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
    {
        currentKey = key;
        currentValue = value;
        setKeyPrefix( keyPrefix );
        
        if ( isInHeader )
        {
            if ( key.equals( "formatVersion" ) )
                this.sourceVersion = Version.parseVersion( value );
        }
        else if ( currentState != null )
        {
            currentState.loadProperty( this );
        }
        else if ( currentStatesSet != null )
        {
            currentStatesSet.loadProperty( this );
        }
        
        return ( true );
    }
    
    @Override
    protected void onParsingFinished()
    {
        super.onParsingFinished();
        
        if ( currentState != null )
        {
            currentStatesSet.addState( currentState );
        }
        
        currentState = null;
        
        if ( currentStatesSet != null )
        {
            manager.addWidgetStatesSet( currentStatesSet );
        }
        
        currentStatesSet = null;
    }
    
    public StatesSetsLoader( DirectorManager manager, List<DriverCapsule> driversList )
    {
        this.manager = manager;
        this.driversList = driversList;
    }
}
