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
package net.ctdp.rfdynhud.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * Handles some decisions on {@link Widget} import.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImportDecisionsWindow extends JDialog
{
    private static final long serialVersionUID = 53544231913396148L;
    
    private JRadioButton rdoUseDestAliases;
    private JRadioButton rdoRenameAliases;
    private JRadioButton rdoConvertToLocal;
    private JRadioButton rdoOverwriteDest;
    
    private boolean cancelPressed = true;
    
    public final void setDecision( WidgetImportManager.ImportDecision decision )
    {
        rdoUseDestAliases.setSelected( decision == WidgetImportManager.ImportDecision.USE_DESTINATION_ALIASES );
        rdoRenameAliases.setSelected( decision == WidgetImportManager.ImportDecision.RENAME_ALIASES );
        rdoConvertToLocal.setSelected( decision == WidgetImportManager.ImportDecision.CONVERT_TO_LOCAL );
        rdoOverwriteDest.setSelected( decision == WidgetImportManager.ImportDecision.OVERWRITE_ALIASES );
    }
    
    public final WidgetImportManager.ImportDecision getDecision()
    {
        if ( cancelPressed )
            return ( null );
        
        if ( rdoUseDestAliases.isSelected() )
            return ( WidgetImportManager.ImportDecision.USE_DESTINATION_ALIASES );
        
        if ( rdoRenameAliases.isSelected() )
            return ( WidgetImportManager.ImportDecision.RENAME_ALIASES );
        
        if ( rdoConvertToLocal.isSelected() )
            return ( WidgetImportManager.ImportDecision.CONVERT_TO_LOCAL );
        
        if ( rdoOverwriteDest.isSelected() )
            return ( WidgetImportManager.ImportDecision.OVERWRITE_ALIASES );
        
        return ( null );
    }
    
    public ImportDecisionsWindow( JDialog parent )
    {
        super( parent, "Import name conflicts", true );
        
        setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE );
        
        getContentPane().setLayout( new BorderLayout() );
        
        JPanel main = new JPanel();
        
        String message = "<html><body><p>The border, background colors and/or fonts in the imported Widget use names, that have a different meaning in the destination configuration.</p><p style='margin-top: 5px;'>What do you want to do?</p></body></html>";
        
        JPanel infoPanel = new JPanel( new BorderLayout() );
        infoPanel.setBorder( new TitledBorder( "" ) );
        JLabel infoLabel = new JLabel( message );
        infoLabel.setBorder( new EmptyBorder( 5, 10, 10, 10 ) );
        infoLabel.setPreferredSize( new Dimension( 250, 100 ) );
        infoPanel.add( infoLabel, BorderLayout.CENTER );
        
        main.add( infoPanel, BorderLayout.CENTER );
        
        
        JPanel decisionsPanel = new JPanel( new BorderLayout() );
        decisionsPanel.setBorder( new TitledBorder( "" ) );
        
        JPanel innerDecPanel = new JPanel();
        innerDecPanel.setBorder( new EmptyBorder( 5, 10, 10, 10 ) );
        innerDecPanel.setPreferredSize( new Dimension( 250, 390 ) );
        innerDecPanel.setLayout( new BoxLayout( innerDecPanel, BoxLayout.Y_AXIS ) );
        
        ButtonGroup buttonGroup = new ButtonGroup();
        
        rdoUseDestAliases = new JRadioButton( "<html><body>Use destination<p style='margin-top: 5px;'>The imported Widget will use the names/aliases as defined in the destination configuration.<p style='margin-top: 5px;'>It may/will look different to the imported Widget, but integrates well into the destination configuration</p></body></html>" );
        rdoUseDestAliases.setVerticalTextPosition( SwingConstants.TOP );
        rdoUseDestAliases.setPreferredSize( new Dimension( 250, 155 ) );
        buttonGroup.add( rdoUseDestAliases );
        innerDecPanel.add( rdoUseDestAliases );
        
        rdoRenameAliases = new JRadioButton( "<html><body>Rename aliases<p style='margin-top: 5px;'>Renames all conflicting names/aliases to new free names/aliases.</p></body></html>" );
        rdoRenameAliases.setVerticalTextPosition( SwingConstants.TOP );
        rdoRenameAliases.setPreferredSize( new Dimension( 250, 50 ) );
        buttonGroup.add( rdoRenameAliases );
        innerDecPanel.add( rdoRenameAliases );
        
        rdoConvertToLocal = new JRadioButton( "<html><body>Convert to local<p style='margin-top: 5px;'>Converts all conflicting names/aliases to direct colors/fonts/etc.</p></body></html>" );
        rdoConvertToLocal.setVerticalTextPosition( SwingConstants.TOP );
        rdoConvertToLocal.setPreferredSize( new Dimension( 250, 50 ) );
        buttonGroup.add( rdoConvertToLocal );
        innerDecPanel.add( rdoConvertToLocal );
        
        rdoOverwriteDest = new JRadioButton( "<html><body>Overwrite aliases in destination configuration<p style='margin-top: 5px;'>Changes the values of names/aliases in the destination configuration to what they are in the source configuration.</p><p style='margin-top: 5px; color: red;'>This affects all Widgets in the destination configuration, that use these names/aliases.</p></body></html>" );
        rdoOverwriteDest.setVerticalTextPosition( SwingConstants.TOP );
        rdoOverwriteDest.setPreferredSize( new Dimension( 250, 155 ) );
        buttonGroup.add( rdoOverwriteDest );
        innerDecPanel.add( rdoOverwriteDest );
        
        decisionsPanel.add( innerDecPanel, BorderLayout.CENTER );
        
        main.add( decisionsPanel, BorderLayout.SOUTH );
        
        getContentPane().add( main, BorderLayout.CENTER );
        
        
        JPanel buttons = new JPanel( new FlowLayout( FlowLayout.RIGHT, 0, 0 ) );
        buttons.setBorder( new EmptyBorder( 15, 10, 10, 10 ) );
        
        final JButton okButton = new JButton( "Ok" );
        okButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                cancelPressed = false;
                ImportDecisionsWindow.this.setVisible( false );
            }
        } );
        buttons.add( okButton );
        
        buttons.add( Box.createHorizontalStrut( 5 ) );
        
        final JButton cancelButton = new JButton( "Cancel" );
        cancelButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                cancelPressed = true;
                ImportDecisionsWindow.this.setVisible( false );
            }
        } );
        buttons.add( cancelButton );
        
        getContentPane().add( buttons, BorderLayout.SOUTH );
        
        setSize( 300, 615 );
        setLocationRelativeTo( parent );
        
        addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowOpened( WindowEvent e )
            {
                ImportDecisionsWindow.this.removeWindowListener( this );
                
                okButton.requestFocus();
            }
        } );
    }
}
