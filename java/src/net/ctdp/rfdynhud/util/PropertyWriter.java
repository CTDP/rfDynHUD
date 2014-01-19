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
package net.ctdp.rfdynhud.util;

import java.io.IOException;

import net.ctdp.rfdynhud.properties.Property;

/**
 * Writes properties to a configuration file.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public interface PropertyWriter
{
    public abstract void writeProperty( String key, Object value, boolean isDefaultValue, Boolean quoteValue, String comment ) throws IOException;
    
    public abstract void writeProperty( String key, Object value, boolean isDefaultValue, String comment ) throws IOException;
    
    public abstract void writeProperty( Property property, Boolean quoteValue, String comment ) throws IOException;
    
    public abstract void writeProperty( Property property, String comment ) throws IOException;
}
