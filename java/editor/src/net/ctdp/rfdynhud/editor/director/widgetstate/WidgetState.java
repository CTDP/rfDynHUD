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
package net.ctdp.rfdynhud.editor.director.widgetstate;

import java.io.IOException;
import java.util.List;

import net.ctdp.rfdynhud.editor.director.DirectorManager;
import net.ctdp.rfdynhud.editor.director.DriverCapsule;
import net.ctdp.rfdynhud.editor.director.EffectiveWidgetState;
import net.ctdp.rfdynhud.properties.AbstractPropertiesKeeper;
import net.ctdp.rfdynhud.properties.DelayProperty;
import net.ctdp.rfdynhud.properties.DelayProperty.DisplayUnits;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.GenericPropertiesIterator;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.ListProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertiesKeeper;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.properties.TimeProperty;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * Describes a {@link Widget}'s state to be sent to the game.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetState implements PropertiesKeeper
{
    public static enum VisibleType
    {
        AUTO,
        NEXT_LAP,
        TIME_BASED,
        NEVER,
        ;
    }
    
    private final DirectorManager manager;
    
    private final StringProperty name;
    private final StringProperty widgetName = new StringProperty( "widgetName", "widgetName" );
    private final EnumProperty<VisibleType> visibleType = new EnumProperty<VisibleType>( "visibleType", VisibleType.AUTO );
    private final TimeProperty visibleStart = new TimeProperty( "visibleStart", "00:00:00" )
    {
        @Override
        public String getButtonText()
        {
            return ( "Now" );
        }
        
        @Override
        public void onButtonClicked( Object button )
        {
            setNanoValue( manager.getSessionTime() );
        }
    };
    private final DelayProperty visibleTime = new DelayProperty( "visibleTime", DisplayUnits.SECONDS, 20, 0, Integer.MAX_VALUE );
    private final ListProperty<DriverCapsule, List<DriverCapsule>> forDriver;
    private final ListProperty<DriverCapsule, List<DriverCapsule>> compareDriver;
    private final IntProperty posX = new IntProperty( "posX", -10000 );
    private final IntProperty posY = new IntProperty( "posY", -10000 );
    
    private final EffectiveWidgetState effectiveState = new EffectiveWidgetState();
    
    public final boolean owns( Property property )
    {
        GenericPropertiesIterator it = new GenericPropertiesIterator( this, false );
        
        while ( it.hasNext() )
        {
            Property p = it.next();
            
            if ( p == property )
                return ( true );
        }
        
        return ( false );
    }
    
    public void setName( String name )
    {
        this.name.setValue( name );
    }
    
    public final String getName()
    {
        return ( name.getValue() );
    }
    
    public void setWidgetName( String widgetName )
    {
        this.widgetName.setStringValue( widgetName );
    }
    
    public final String getWidgetName()
    {
        return ( widgetName.getValue() );
    }
    
    public void setVisibleType( VisibleType visibleType )
    {
        this.visibleType.setEnumValue( visibleType );
    }
    
    public final VisibleType getVisibleType()
    {
        return ( visibleType.getValue() );
    }
    
    public void setVisibleStart( long visibleStart )
    {
        this.visibleStart.setNanoValue( visibleStart );
    }
    
    public final long getVisibleStart()
    {
        return ( visibleStart.getNanoValue() );
    }
    
    public void setVisibleTime( long visibleTime )
    {
        this.visibleTime.setIntValue( (int)( visibleTime / 1000000000L ) );
    }
    
    public final long getVisibleTime()
    {
        return ( visibleTime.getDelayNanos() );
    }
    
    public void setForDriver( DriverCapsule dc )
    {
        this.forDriver.setSelectedValue( dc );
    }
    
    public final DriverCapsule getForDriver()
    {
        return ( forDriver.getValue() );
    }
    
    public void setCompareDriver( DriverCapsule dc )
    {
        this.compareDriver.setSelectedValue( dc );
    }
    
    public final DriverCapsule getCompareDriver()
    {
        return ( compareDriver.getSelectedValue() );
    }
    
    public void setPosX( int posX )
    {
        this.posX.setIntValue( posX );
    }
    
    public final int getPosX()
    {
        return ( posX.getIntValue() );
    }
    
    public void setPosY( int posY )
    {
        this.posY.setIntValue( posY );
    }
    
    public final int getPosY()
    {
        return ( posY.getIntValue() );
    }
    
    public final EffectiveWidgetState getEffectiveState()
    {
        return ( effectiveState );
    }
    
    public EffectiveWidgetState applyToEffective()
    {
        effectiveState.setWidgetName( getWidgetName() );
        effectiveState.setVisibleType( getVisibleType() );
        if ( getVisibleType() == VisibleType.NEXT_LAP )
            effectiveState.setVisibleStart( Long.MIN_VALUE );
        else
            effectiveState.setVisibleStart( getVisibleStart() );
        effectiveState.setVisibleTime( getVisibleTime() );
        effectiveState.setForDriver( getForDriver() );
        effectiveState.setCompareDriver( getCompareDriver() );
        effectiveState.setPosX( getPosX() );
        effectiveState.setPosY( getPosY() );
        
        return ( effectiveState );
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
        writer.writeProperty( name, null );
        writer.writeProperty( widgetName, null );
        writer.writeProperty( visibleType, null );
        writer.writeProperty( visibleStart, null );
        writer.writeProperty( visibleTime, null );
        //writer.writeProperty( forDriver, null );
        //writer.writeProperty( compareDriver, null );
        writer.writeProperty( posX, null );
        writer.writeProperty( posY, null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        if ( loader.loadProperty( name ) );
        else if ( loader.loadProperty( widgetName ) );
        else if ( loader.loadProperty( visibleType ) );
        else if ( loader.loadProperty( visibleStart ) );
        else if ( loader.loadProperty( visibleTime ) );
        //else if ( loader.loadProperty( forDriver ) );
        //else if ( loader.loadProperty( compareDriver ) );
        else if ( loader.loadProperty( posX ) );
        else if ( loader.loadProperty( posY ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( name );
        propsCont.addProperty( widgetName );
        propsCont.addProperty( visibleType );
        if ( forceAll || ( visibleType.getValue() == VisibleType.TIME_BASED ) )
        {
            propsCont.addProperty( visibleStart );
        }
        if ( forceAll || ( visibleType.getValue() == VisibleType.NEXT_LAP ) || ( visibleType.getValue() == VisibleType.TIME_BASED ) )
        {
            propsCont.addProperty( visibleTime );
        }
        //if ( forceAll || ( visibleType.getValue() == VisibleType.NEXT_LAP ) )
        {
            propsCont.addProperty( forDriver );
            propsCont.addProperty( compareDriver );
        }
        propsCont.addProperty( posX );
        propsCont.addProperty( posY );
    }
    
    public WidgetState( List<DriverCapsule> driversList, final DirectorManager manager )
    {
        this.manager = manager;
        
        this.name = new StringProperty( "name", "WidgetState" )
        {
            @Override
            public String getButtonText()
            {
                return ( "Del" );
            }
            
            @Override
            public void onButtonClicked( Object button )
            {
                manager.removeWidgetState( WidgetState.this );
            }
        };
        
        this.forDriver = new ListProperty<DriverCapsule, List<DriverCapsule>>( "forDriver", DriverCapsule.DEFAULT_DRIVER, driversList );
        this.compareDriver = new ListProperty<DriverCapsule, List<DriverCapsule>>( "compareDriver", DriverCapsule.DEFAULT_DRIVER, driversList );
        
        AbstractPropertiesKeeper.attachKeeper( this );
    }
}
