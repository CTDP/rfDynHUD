/**
 * This piece of code has been provided by and with kind
 * permission of INFOLOG GmbH from Germany.
 * It is released under the terms of the GPL, but INFOLOG
 * is still permitted to use it in closed source software.
 */
package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

/**
 * @author Marvin Froehlich (CTDP) (aka Qudus)
 */
public class KeyRenderLabel extends JLabel
{
    private static final long serialVersionUID = 1L;
    
    private int level = 0;
    private boolean[] lastInGroup = null;
    
    public void setLevel( int level )
    {
        this.level = level;
    }
    
    public void setLastInGroup( boolean[] lig )
    {
        this.lastInGroup = lig;
    }
    
    @Override
    protected void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        
        if ( level > 0 )
        {
            Graphics2D g2 = (Graphics2D)g;
            
            Color oldColor = g2.getColor();
            g2.setColor( Color.BLACK );
            
            int indent = 14;
            
            for ( int i = 1; i <= level; i++ )
            {
                int x = i * indent - ( indent / 2 );
                
                boolean lig = lastInGroup[i];
                if ( i < level )
                    lig = lig && lastInGroup[level];
                
                if ( lig )
                {
                    g2.drawLine( x, 0, x, getHeight() - 4 );
                    g2.drawLine( x, getHeight() - 4, x + 2, getHeight() - 4 );
                }
                else
                {
                    g2.drawLine( x, 0, x, getHeight() );
                }
            }
            
            g2.setColor( oldColor );
        }
    }
    
    public KeyRenderLabel()
    {
        super();
    }
}
