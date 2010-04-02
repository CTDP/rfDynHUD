package net.ctdp.rfdynhud.editor.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.RFactorTools;
import net.ctdp.rfdynhud.util.StringUtil;

public class HelpWindow extends JDialog
{
    private static final long serialVersionUID = -8011875510400863665L;
    
    private JCheckBox cbAlwaysShowOnStartup;
    private JScrollPane sp;
    
    public final boolean getAlwaysShowOnStartup()
    {
        return ( cbAlwaysShowOnStartup.isSelected() );
    }
    
    private Component createInfoPanel()
    {
        String s = "Unable to load readme";
        try
        {
            s = StringUtil.loadString( new File( RFactorTools.CONFIG_PATH, "readme.html" ).toURI().toURL() );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        JEditorPane p = new JEditorPane( "text/html", s );
        p.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
        p.setEditable( false );
        
        sp = new JScrollPane( p );
        
        sp.setPreferredSize( new Dimension( 800, 600 ) );
        
        return ( sp );
    }
    
    public HelpWindow( JFrame parent, boolean alwaysShowOnStartup )
    {
        super( parent, "rfDynHUD (Editor) Help", true );
        
        JPanel cp = (JPanel)this.getContentPane();
        
        cp.setLayout( new BorderLayout( 5, 5 ) );
        
        cp.add( createInfoPanel(), BorderLayout.CENTER );
        
        JPanel buttons = new JPanel( new BorderLayout() );
        cbAlwaysShowOnStartup = new JCheckBox( "Always show this window on startup", alwaysShowOnStartup );
        buttons.add( cbAlwaysShowOnStartup, BorderLayout.WEST );
        
        JPanel p = new JPanel();
        buttons.add( p, BorderLayout.CENTER );
        
        JButton btnClose = new JButton( "Close" );
        btnClose.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                HelpWindow.this.dispose();
            }
        } );
        buttons.add( btnClose, BorderLayout.EAST );
        
        this.add( buttons, BorderLayout.SOUTH );
        
        this.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
    }
    
    public static HelpWindow showHelpWindow( final JFrame parent, boolean alwaysShowOnStartup )
    {
        final HelpWindow hw = new HelpWindow( parent, alwaysShowOnStartup );
        
        hw.addWindowListener( new WindowAdapter()
        {
            private boolean shot = false;
            
            public void windowOpened( WindowEvent e )
            {
                if ( shot )
                    return;
                
                hw.pack();
                hw.setLocationRelativeTo( parent );
                hw.sp.getVerticalScrollBar().setValue( 0 );
                
                shot = true;
            }
        } );
        
        hw.setVisible( true );
        
        return ( hw );
    }
}
