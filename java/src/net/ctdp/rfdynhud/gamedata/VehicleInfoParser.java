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
package net.ctdp.rfdynhud.gamedata;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jagatoo.util.errorhandling.ParsingException;

/**
 * Parses {@link VehicleInfo} from a .VEH file.
 * 
 * @author Marvin Froehlich (CTDP)
 */
abstract class VehicleInfoParser
{
    protected final String filename;
    protected final VehicleInfo info;
    
    /**
     * Parses the given file.
     * 
     * @param file
     * 
     * @throws IOException
     * @throws ParsingException
     */
    public abstract void parse( File file ) throws IOException, ParsingException;
    
    /**
     * Parses the given file.
     * 
     * @param url
     * 
     * @throws IOException
     * @throws ParsingException
     */
    public abstract void parse( URL url ) throws IOException, ParsingException;
    
    protected VehicleInfoParser( String filename, VehicleInfo info )
    {
        this.filename = filename;
        this.info = info;
    }
}
