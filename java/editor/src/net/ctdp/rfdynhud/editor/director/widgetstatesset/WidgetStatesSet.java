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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ctdp.rfdynhud.editor.director.DirectorManager;
import net.ctdp.rfdynhud.editor.director.DriverCapsule;
import net.ctdp.rfdynhud.editor.director.widgetstate.WidgetState;
import net.ctdp.rfdynhud.editor.util.DefaultPropertyWriter;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertiesKeeper;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.properties.__PropsPrivilegedAccess;
import net.ctdp.rfdynhud.util.PropertyWriter;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetStatesSet implements PropertiesKeeper, Comparable<WidgetStatesSet>
{
    private final StringProperty name = new StringProperty( "name", "" );
    
    private final ArrayList<WidgetState> states = new ArrayList<WidgetState>();
    
    public void setName( String name )
    {
        this.name.setStringValue( name );
    }
    
    public void addState( WidgetState ws )
    {
        states.add( ws );
    }
    
    public WidgetState addState( List<DriverCapsule> driversList, DirectorManager manager )
    {
        WidgetState ws = new WidgetState( driversList, manager );
        states.add( ws );
        ws.setName( WidgetState.class.getSimpleName() + states.size() );
        
        return ( ws );
    }
    
    public void removeState( WidgetState ws )
    {
        states.remove( ws );
    }
    
    public final int getNumStates()
    {
        return ( states.size() );
    }
    
    public final WidgetState getState( int index )
    {
        return ( states.get( index ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( name.getValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        return ( super.equals( o ) );
    }
    
    @Override
    public int compareTo( WidgetStatesSet wss )
    {
        return ( this.name.getStringValue().compareToIgnoreCase( wss.name.getStringValue() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ( name.getValue().hashCode() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPropertyChanged( Property property, Object oldValue, Object newValue )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        ( (DefaultPropertyWriter)writer ).getIniWriter().writeGroup( "WidgetStatesSet" );
        
        writer.writeProperty( name, null );
        
        for ( int i = 0; i < states.size(); i++ )
        {
            ( (DefaultPropertyWriter)writer ).getIniWriter().writeGroup( "WidgetState" );
            
            states.get( i ).saveProperties( writer );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        if ( loader.loadProperty( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( name );
        
        for ( int i = 0; i < states.size(); i++ )
        {
            propsCont.addGroup( states.get( i ), true );
            
            states.get( i ).getProperties( propsCont, forceAll );
        }
    }
    
    public WidgetStatesSet()
    {
        __PropsPrivilegedAccess.attachKeeper( this, false );
    }
}
