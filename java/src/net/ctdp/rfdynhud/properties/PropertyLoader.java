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

import org.jagatoo.util.versioning.Version;

/**
 * The {@link PropertyLoader} loads properties from a configuration file.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public interface PropertyLoader
{
    /**
     * Gets the currently loaded property key.
     * 
     * @return the currently loaded property key.
     */
    public String getCurrentKey();
    
    /**
     * Gets the currently loaded property value.
     * 
     * @return the currently loaded property value.
     */
    public String getCurrentValue();
    
    /**
     * Gets the rfDynHUD version, the source file was written by.
     * 
     * @return the rfDynHUD version, the source file was written by.
     */
    public Version getSourceVersion();
    
    /**
     * Attempts to load the value into the passed property.
     * 
     * @param property
     * 
     * @return <code>true</code>, if and only if the property accepted the key and value.
     */
    public boolean loadProperty( Property property );
}
