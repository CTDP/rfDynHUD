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

import net.ctdp.rfdynhud.util.PropertyWriter;

/**
 * Interface for all classes, that keep properties.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public interface PropertiesKeeper
{
    /**
     * Invoked when a kept {@link Property} has changed.
     * 
     * @param property the changed property
     * @param oldValue the old value
     * @param newValue the new value
     */
    public void onPropertyChanged( Property property, Object oldValue, Object newValue );
    
    /**
     * Saves all settings to the config file.
     * 
     * @param writer the widgets configuration writer to write properties to
     * 
     * @throws IOException if something went wrong
     */
    public void saveProperties( PropertyWriter writer ) throws IOException;
    
    /**
     * Loads (and parses) a certain property from a config file.
     * 
     * @param loader the property loader to load properties from
     */
    public void loadProperty( PropertyLoader loader );
    
    /**
     * Puts all editable properties to the editor.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link PropertiesKeeper} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link PropertiesKeeper}'s situation have to be added, some can be ignored.
     */
    public void getProperties( PropertiesContainer propsCont, boolean forceAll );
}
