package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

/**
 * @author Marvin Froehlich (aka Qudus)
 */
public class KeyRenderLabel extends JLabel
{
    private static final long serialVersionUID = 1L;
    
    private int level = 0;
    
    public void setLevel( int level )
    {
        this.level = level;
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
            
            for ( int i = 0; i < level; i++ )
            {
                int x = ( i + 1 ) * indent - ( indent / 2 );
                
                g2.drawLine( x, 0, x, getHeight() );
            }
            
            g2.setColor( oldColor );
        }
    }
    
    public KeyRenderLabel()
    {
        super();
    }
}
