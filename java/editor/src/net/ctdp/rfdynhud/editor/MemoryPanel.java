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
package net.ctdp.rfdynhud.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.jagatoo.util.Tools;

/**
 * The memory panel displays information about free VM memory.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class MemoryPanel extends JPanel implements Runnable
{
    private static final long serialVersionUID = -4571334691231404766L;
    
    public static final long UPDATE_DELAY = 500L;
    
    private final EditorRunningIndicator runningIndicator;
    
    private long maxMemory = -1L;
    private long totalMemory = -1L;
    private long usedMemory = 0L;
    private long freeMemory = 0L;
    
    public final long getMaxMemory()
    {
        return ( maxMemory );
    }
    
    public final long getTotalMemory()
    {
        return ( totalMemory );
    }
    
    public final long getUsedMemory()
    {
        return ( usedMemory );
    }
    
    public final long getFreeMemory()
    {
        return ( freeMemory );
    }
    
    private boolean updateMemInfo()
    {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        boolean result = ( ( freeMemory != this.freeMemory ) || ( totalMemory != this.totalMemory ) );
        
        this.maxMemory = maxMemory;
        this.totalMemory = totalMemory;
        this.freeMemory = freeMemory;
        this.usedMemory = usedMemory;
        
        return ( result );
    }
    
    @Override
    public void run()
    {
        while ( !runningIndicator.isEditorRunning() )
        {
            try { Thread.sleep( 50L ); } catch ( InterruptedException e ) {}
        }
        
        while ( runningIndicator.isEditorRunning() )
        {
            if ( updateMemInfo() )
            {
                this.setToolTipText( "Memory: used: " + Tools.formatBytes( usedMemory ) + ", free: " + Tools.formatBytes( freeMemory ) + ", total: " + Tools.formatBytes( totalMemory ) + ", max: " + Tools.formatBytes( maxMemory ) );
                
                this.repaint();
            }
            
            try { Thread.sleep( UPDATE_DELAY ); } catch ( InterruptedException e ) {}
        }
    }
    
    @Override
    protected void paintComponent( Graphics g )
    {
        int w = getWidth();
        int h = getHeight();
        
        Graphics2D g2 = (Graphics2D)g;
        
        g2.setBackground( this.getBackground() );
        g2.clearRect( 0, 0, w, h );
        
        if ( totalMemory > 0L )
        {
            double percent = (double)usedMemory / (double)totalMemory;
            
            g2.setColor( Color.RED );
            g2.fillRect( 0, 0, (int)Math.round( percent * w ), h );
            
            String ps = String.valueOf( (int)Math.round( percent * 100.0 ) + "%" );
            Rectangle2D bounds = g2.getFontMetrics().getStringBounds( ps, g2 );
            
            g2.setColor( Color.BLACK );
            g2.drawString( ps, ( w - (int)bounds.getWidth() ) / 2, ( h - (int)bounds.getHeight() ) / 2 - (int)bounds.getY() );
        }
    }
    
    public MemoryPanel( EditorRunningIndicator runningIndicator )
    {
        super( new BorderLayout() );
        
        this.runningIndicator = runningIndicator;
        
        this.addMouseListener( new MouseAdapter()
        {
            @Override
            public void mouseClicked( MouseEvent e )
            {
                if ( e.getButton() == MouseEvent.BUTTON1 )
                {
                    System.gc();
                }
            }
        } );
        
        new Thread( this ).start();
    }
}
