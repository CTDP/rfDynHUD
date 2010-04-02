package net.ctdp.rfdynhud.editor.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import net.ctdp.rfdynhud.RFDynHUD;

public class AboutPage extends JDialog
{
    private static final long serialVersionUID = 3670847857825791395L;
    
    private Component createInfoPanel()
    {
        StringBuilder sb = new StringBuilder( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" );
        sb.append( "<html>\n");
        sb.append( "<head>\n");
        sb.append( "<title>About rfDynHUD Editor</title>\n");
        sb.append( "<style type=\"text/css\">\n");
        sb.append( "p, li { font-family: Arial; font-size: 12pt; }\n");
        sb.append( "</style>\n");
        sb.append( "</head>\n");
        sb.append( "<body>\n" );
        sb.append( "<html>\n<body>\n" );
        
        sb.append( "<p style=\"margin-top: 0;\">" );
        sb.append( "Editor for rfDynHUD v" + RFDynHUD.VERSION.toString() + " plugin for a dynamic rFactor HUD." );
        sb.append( "</p>" );
        
        sb.append( "<p>" );
        sb.append( "Copyright &copy; by CTDP, Author: Marvin Fr&ouml;hlich<br />" );
        sb.append( "Special Thanks to Marcel Offermans" );
        sb.append( "</p>" );
        
        sb.append( "<p>" );
        sb.append( "Please visit us at <a href=\"http://www.ctdp.net/\">http://www.ctdp.net/</a>." );
        sb.append( "</p>" );
        
        sb.append( "</body>\n</html>\n" );
        
        JEditorPane p = new JEditorPane( "text/html", sb.toString() );
        p.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        p.setEditable( false );
        
        JScrollPane sp = new JScrollPane( p );
        
        sp.setPreferredSize( new Dimension( 300, 150 ) );
        
        return ( sp );
    }
    
    public AboutPage( JFrame parent )
    {
        super( parent, "About rfDynHUD (Editor)", true );
        
        JPanel cp = (JPanel)this.getContentPane();
        
        cp.setLayout( new BorderLayout( 5, 5 ) );
        
        ImageIcon icon = new ImageIcon( AboutPage.class.getResource( "/data/config/images/ctdp-fat-1994.png" ) );
        JLabel label = new JLabel( icon );
        cp.add( label, BorderLayout.WEST );
        
        cp.add( createInfoPanel(), BorderLayout.CENTER );
        
        JPanel buttons = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JButton btnClose = new JButton( "Close" );
        btnClose.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                AboutPage.this.dispose();
            }
        } );
        buttons.add( btnClose );
        
        this.add( buttons, BorderLayout.SOUTH );
        
        this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }
    
    public static AboutPage showAboutPage( final JFrame parent )
    {
        final AboutPage ap = new AboutPage( parent );
        
        ap.addWindowListener( new WindowAdapter()
        {
            private boolean shot = false;
            
            public void windowOpened( WindowEvent e )
            {
                if ( shot )
                    return;
                
                ap.pack();
                ap.setLocationRelativeTo( parent );
                
                shot = true;
            }
        } );
        
        ap.setVisible( true );
        
        return ( ap );
    }
}
