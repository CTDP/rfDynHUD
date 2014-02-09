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
package net.ctdp.rfdynhud.properties;

import java.io.IOException;
import java.util.List;

import net.ctdp.rfdynhud.util.PropertyWriter;

/**
 * Interface for all classes, that keep properties.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractPropertiesKeeper implements PropertiesKeeper
{
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
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
    }
    
    public static final void setKeeper( Property property, PropertiesKeeper keeper, boolean force )
    {
        property.setKeeper( keeper, force );
    }
    
    public static final void setKeeper( Property property, PropertiesKeeper keeper )
    {
        property.setKeeper( keeper, false );
    }
    
    public static final void attachKeeper( PropertiesKeeper keeper, boolean force )
    {
        FlatPropertiesContainer pc = new FlatPropertiesContainer();
        
        keeper.getProperties( pc, true );
        List<Property> list = pc.getList();
        
        for ( int i = 0; i < list.size(); i++ )
        {
            if ( list.get( i ) != null )
                list.get( i ).setKeeper( keeper, force );
        }
    }
    
    public static final void attachKeeper( PropertiesKeeper keeper )
    {
        attachKeeper( keeper, false );
    }
}
