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
package net.ctdp.rfdynhud.plugins.datasender;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Connects to the editor via a socket and sends/receives data.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Communicator extends net.ctdp.rfdynhud.plugins.AbstractCommunicator
{
    @SuppressWarnings( "unused" )
    private final DataSenderPlugin plugin;
    
    @Override
    protected boolean readDatagram( final int code, DataInputStream in ) throws IOException
    {
        return ( false );
    }
    
    public Communicator( DataSenderPlugin plugin, int port, String password )
    {
        super( plugin, port, password );
        
        this.plugin = plugin;
    }
}
