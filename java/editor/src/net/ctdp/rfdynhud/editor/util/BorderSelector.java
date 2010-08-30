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
package net.ctdp.rfdynhud.editor.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import net.ctdp.rfdynhud.properties.BorderProperty;
import net.ctdp.rfdynhud.properties.FlatWidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.render.BackgroundColorBorderRenderer;
import net.ctdp.rfdynhud.render.BorderCache;
import net.ctdp.rfdynhud.render.BorderRenderer;
import net.ctdp.rfdynhud.render.BorderWrapper;
import net.ctdp.rfdynhud.render.ImageBorderRenderer;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

/**
 * The {@link BorderSelector} provides a dialog to select border images from a certain
 * folder and its subfolders and to define aliases for these borders.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BorderSelector extends DefaultTableModel
{
    private static final long serialVersionUID = -8119644203101916467L;
    
    private static final Color STANDARD_BACKGROUND_COLOR = new Color( 0, 0, 0, 150 );
    
    private final File folder;
    private WidgetsConfiguration widgetsConfig = null;
    private String[] aliases = null;
    private String noAliasSelection = null;
    
    private String startBorder;
    private JTextField valueTextField = null;
    private boolean somethingChanged = false;
    
    private JDialog dialog = null;
    
    public final String getSelectedBorder()
    {
        if ( !somethingChanged )
            return ( null );
        
        return ( valueTextField.getText().trim() );
    }
    
    public final boolean getSomethingChanged()
    {
        return ( somethingChanged );
    }
    
    private static void applyBorder( String borderKey, WidgetsConfiguration widgetsConfig )
    {
        FlatWidgetPropertiesContainer wpc = new FlatWidgetPropertiesContainer();
        
        for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            wpc.clear();
            widget.getProperties( wpc, true );
            
            boolean propFound = false;
            
            for ( Property prop : wpc.getList() )
            {
                if ( prop instanceof BorderProperty )
                {
                    BorderProperty borderProp = (BorderProperty)prop;
                    
                    if ( borderKey.equals( borderProp.getBorderName() ) )
                    {
                        borderProp.refresh();
                        __WPrivilegedAccess.onPropertyChanged( borderProp, borderKey, borderKey, widget );
                        
                        propFound = true;
                    }
                }
            }
            
            if ( propFound )
            {
                widget.forceAndSetDirty( false );
            }
        }
    }
    
    private static boolean verifyIniFile( String name )
    {
        BorderWrapper bw = BorderCache.getBorder( name, null, null, null, null );
        
        return ( bw.hasBorder() );
    }
    
    private static void readFilenames( File folder, String prefix, List<String> filenames )
    {
        for ( File f : folder.listFiles( IniFileFilter.INSTANCE ) )
        {
            if ( f.isDirectory() )
            {
                if ( prefix == null )
                    readFilenames( f, f.getName(), filenames );
                else
                    readFilenames( f, prefix + "/" + f.getName(), filenames );
            }
            else
            {
                String name;
                if ( prefix == null )
                    name = f.getName();
                else
                    name = prefix + "/" + f.getName();
                
                if ( verifyIniFile( name ) )
                    filenames.add( name );
            }
        }
    }
    
    private final HashMap<String, ImageIcon> cache = new HashMap<String, ImageIcon>();
    
    private class BorderEntryCellRenderer extends JPanel implements ListCellRenderer
    {
        private static final long serialVersionUID = -1550354624601303118L;
        
        private JLabel imageLabel;
        private JLabel filenameLabel;
        
        @Override
        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
        {
            if ( isSelected )
            {
                this.setBackground( list.getSelectionBackground() );
            }
            else
            {
                this.setBackground( list.getBackground() );
            }
            
            String filename = (String)value;
            
            BorderWrapper bw = BorderCache.getBorder( filename, null, null, null, null );
            BorderRenderer br = bw.getRenderer();
            
            if ( br instanceof ImageBorderRenderer )
            {
                String imageFilename = ( (ImageBorderRenderer)br ).getImageFilename();
                
                try
                {
                    URL url = new File( folder, imageFilename ).toURI().toURL();
                    
                    ImageIcon icon = cache.get( url );
                    if ( icon == null )
                    {
                        icon = new ImageIcon( url );
                        cache.put( url.toString(), icon );
                    }
                    
                    imageLabel.setIcon( icon );
                }
                catch ( IOException e )
                {
                    Logger.log( e );
                }
            }
            else if ( br instanceof BackgroundColorBorderRenderer )
            {
                TextureImage2D ti = ( (BackgroundColorBorderRenderer)br ).getImage( STANDARD_BACKGROUND_COLOR );
                
                ImageIcon icon = cache.get( "___BACKGROUND___" );
                if ( icon == null )
                {
                    icon = new ImageIcon( ti.getBufferedImage() );
                    cache.put( "___BACKGROUND___", icon );
                }
                
                imageLabel.setIcon( icon );
            }
            
            this.filenameLabel.setText( filename );
            
            return ( this );
        }
        
        public BorderEntryCellRenderer()
        {
            super( new BorderLayout( 5, 5 ) );
            
            this.imageLabel = new JLabel();
            imageLabel.setOpaque( false );
            this.add( imageLabel, BorderLayout.WEST );
            
            this.filenameLabel = new JLabel();
            imageLabel.setOpaque( false );
            this.add( filenameLabel, BorderLayout.CENTER );
        }
    }
    
    private class AliasCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor
    {
        private static final long serialVersionUID = -7951677182802182721L;
        
        private final JTextField textField = new JTextField();
        
        @Override
        public boolean isCellEditable( EventObject e )
        {
            return ( false );
        }
        
        @Override
        public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
        {
            textField.setText( String.valueOf( value ) );
            textField.setEditable( false );
            
            textField.setBackground( table.getBackground() );
            
            return ( textField );
        }
        
        @Override
        public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
        {
            textField.setText( String.valueOf( value ) );
            textField.setEditable( true );
            
            textField.setBackground( table.getBackground() );
            
            return ( textField );
        }
        
        @Override
        public Object getCellEditorValue()
        {
            return ( textField.getText() );
        }
    }
    
    private static void resetBorderPropertyValues( List<Property> list )
    {
        for ( Property prop : list )
        {
            if ( prop instanceof BorderProperty )
            {
                BorderProperty borderProp = (BorderProperty)prop;
                borderProp.setValue( borderProp.getValue() );
            }
        }
    }
    
    private void setAllWidgetsDirty( WidgetsConfiguration widgetsConfig )
    {
        int n = widgetsConfig.getNumWidgets();
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            widget.forceAndSetDirty( false );
            
            propsCont.clear();
            widget.getProperties( propsCont, true );
            
            resetBorderPropertyValues( propsCont.getList() );
        }
    }
    
    private class BorderCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor
    {
        private static final long serialVersionUID = 1948400434095024898L;
        
        private final JComboBox renderCombo = new JComboBox();
        private final JComboBox editCombo = new JComboBox();
        private boolean selEventSuppressed = false;
        private int lastRow = -1;
        
        public int updateFiles()
        {
            Vector<String> files = new Vector<String>();
            readFilenames( folder, null, files );
            Collections.sort( files, String.CASE_INSENSITIVE_ORDER );
            
            renderCombo.setModel( new DefaultComboBoxModel( files ) );
            editCombo.setModel( new DefaultComboBoxModel( files ) );
            
            return ( files.size() );
        }
        
        @Override
        public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
        {
            selEventSuppressed = true;
            renderCombo.setSelectedItem( value );
            selEventSuppressed = false;
            
            //lastRow = row;
            
            return ( renderCombo );
        }
        
        @Override
        public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
        {
            selEventSuppressed = true;
            editCombo.setSelectedItem( value );
            selEventSuppressed = false;
            
            lastRow = row;
            
            return ( editCombo );
        }
        
        @Override
        public Object getCellEditorValue()
        {
            return ( renderCombo.getSelectedItem() );
        }
        
        public BorderCellRenderer()
        {
            super();
            
            renderCombo.setRenderer( new BorderEntryCellRenderer() );
            editCombo.setRenderer( new BorderEntryCellRenderer() );
            
            editCombo.addPopupMenuListener( new PopupMenuListener()
            {
                private Object oldValue = null;
                
                @Override
                public void popupMenuWillBecomeVisible( PopupMenuEvent e )
                {
                    oldValue = ( (JComboBox)e.getSource() ).getSelectedItem();
                }
                
                @Override
                public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
                {
                    if ( oldValue != null )
                    {
                        Object newValue = ( (JComboBox)e.getSource() ).getSelectedItem();
                        
                        //if ( !oldValue.equals( newValue ) )
                        {
                            if ( lastRow == 0 )
                            {
                                noAliasSelection = (String)newValue;
                                
                                //String test = widgetsConfig.getBorderName( valueTextField.getText() );
                                //if ( test == null )
                                    valueTextField.setText( noAliasSelection );
                            }
                            else if ( lastRow >= 0 )
                            {
                                String alias = (String)getValueAt( lastRow, 0 );
                                String border = (String)newValue;
                                widgetsConfig.addBorderAlias( alias, border );
                                setAllWidgetsDirty( widgetsConfig );
                                fireTableDataChanged();
                                
                                valueTextField.setText( alias );
                            }
                            
                            stopCellEditing();
                        }
                    }
                    
                    oldValue = null;
                }
                
                @Override
                public void popupMenuCanceled( PopupMenuEvent e )
                {
                }
            } );
            editCombo.addItemListener( new ItemListener()
            {
                @Override
                public void itemStateChanged( ItemEvent e )
                {
                    if ( selEventSuppressed )
                        return;
                    
                    switch ( e.getStateChange() )
                    {
                        case ItemEvent.DESELECTED:
                            break;
                        case ItemEvent.SELECTED:
                            if ( lastRow == 0 )
                            {
                                noAliasSelection = (String)e.getItem();
                                
                                //String test = widgetsConfig.getBorderName( valueTextField.getText() );
                                //if ( test == null )
                                    valueTextField.setText( noAliasSelection );
                            }
                            else if ( lastRow >= 0 )
                            {
                                String alias = (String)getValueAt( lastRow, 0 );
                                String border = (String)e.getItem();
                                widgetsConfig.addBorderAlias( alias, border );
                                setAllWidgetsDirty( widgetsConfig );
                                fireTableDataChanged();
                            }
                            
                            stopCellEditing();
                            break;
                    }
                    
                    somethingChanged = true;
                }
            } );
        }
    }
    
    private final BorderCellRenderer borderRenderer = new BorderCellRenderer();
    
    private class ButtonCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor
    {
        private static final long serialVersionUID = -2755085918674102181L;
        
        @Override
        public Component getTableCellRendererComponent( final JTable table, Object value, boolean isSelected, boolean hasFocus, final int row, int column )
        {
            if ( row == 0 )
            {
                JPanel dummy = new JPanel();
                
                return ( dummy );
            }
            
            JButton button = new JButton( "Remove" );
            
            button.addActionListener( new ActionListener()
            {
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    Window window = (Window)table.getRootPane().getParent();
                    int result = JOptionPane.showConfirmDialog( window, "Do you really want to remove the selected border alias?", "Remove border alias", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                    if ( result == JOptionPane.YES_OPTION )
                    {
                        widgetsConfig.removeBorderAlias( (String)getValueAt( row, 0 ) );
                        aliases = null;
                        fireTableDataChanged();
                    }
                }
            } );
            
            return ( button );
        }
        
        @Override
        public boolean isCellEditable( EventObject e )
        {
            return ( true );
        }
        
        @Override
        public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
        {
            return ( getTableCellRendererComponent( table, value, isSelected, true, row, column ) );
        }
        
        @Override
        public Object getCellEditorValue()
        {
            return ( "" );
        }
    }
    
    @Override
    public int getRowCount()
    {
        if ( widgetsConfig == null )
            return ( 1 );
        
        return ( 1 + widgetsConfig.getBorderAliases().size() );
    }
    
    @Override
    public Object getValueAt( int row, int column )
    {
        if ( row == 0 )
        {
            if ( column == 0 )
                return ( "<no alias>" );
            
            return ( noAliasSelection );
        }
        
        if ( aliases == null )
        {
            Set<String> aliasesSet = widgetsConfig.getBorderAliases();
            aliases = new String[ aliasesSet.size() ];
            aliasesSet.toArray( aliases );
            Arrays.sort( aliases, String.CASE_INSENSITIVE_ORDER );
        }
        
        String alias = aliases[row - 1];
        
        if ( column == 0 )
            return ( alias );
        
        return ( widgetsConfig.getBorderName( alias ) );
    }
    
    @Override
    public void setValueAt( Object value, int row, int column )
    {
        if ( column == 1 )
        {
            if ( row == 0 )
            {
                noAliasSelection = (String)value;
                
                return;
            }
            
            String alias = (String)getValueAt( row, 0 );
            
            widgetsConfig.addBorderAlias( alias, (String)value );
        }
    }
    
    private JDialog initDialog( Window owner, String title )
    {
        if ( owner instanceof java.awt.Dialog )
            dialog = new JDialog( (java.awt.Dialog)owner, title );
        else if ( owner instanceof java.awt.Frame )
            dialog = new JDialog( (java.awt.Frame)owner, title );
        else
            dialog = new JDialog( owner, title );
        
        dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
        
        return ( dialog );
    }
    
    public String showDialog( Window owner, WidgetsConfiguration widgetsConfig, String selectedBorder )
    {
        this.startBorder = selectedBorder;
        this.widgetsConfig = widgetsConfig;
        
        if ( ( dialog == null ) || ( dialog.getOwner() != owner ) )
        {
            dialog = initDialog( owner, "Select a border or alias..." );
            
            JPanel contentPane = (JPanel)dialog.getContentPane();
            contentPane.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
            contentPane.setLayout( new BorderLayout( 5, 5 ) );
            
            final JTable aliasesTable = new JTable( this );
            aliasesTable.setTableHeader( null );
            aliasesTable.setRowHeight( 2 + 32 + 2 );
            AliasCellRenderer aliasRenderer = new AliasCellRenderer();
            aliasesTable.getColumnModel().addColumn( new TableColumn( 0, 150, aliasRenderer, aliasRenderer ) );
            aliasesTable.getColumnModel().addColumn( new TableColumn( 1, 300, borderRenderer, borderRenderer ) );
            ButtonCellRenderer buttonRenderer = new ButtonCellRenderer();
            aliasesTable.getColumnModel().addColumn( new TableColumn( 2, 100, buttonRenderer, buttonRenderer ) );
            
            contentPane.add( new JScrollPane( aliasesTable ), BorderLayout.CENTER );
            
            aliasesTable.addMouseListener( new MouseAdapter()
            {
                @Override
                public void mouseClicked( MouseEvent e )
                {
                    if ( e.getClickCount() == 2 )
                    {
                        int row = aliasesTable.getSelectedRow();
                        int column = aliasesTable.getSelectedColumn();
                        
                        if ( column == 0 )
                        {
                            if ( row == 0 )
                                valueTextField.setText( (String)aliasesTable.getValueAt( row, 1 ) );
                            else
                                valueTextField.setText( (String)aliasesTable.getValueAt( row, 0 ) );
                        }
                    }
                }
            } );
            
            JPanel footer = new JPanel( new BorderLayout() );
            
            JButton addAlias = new JButton( "Add alias" );
            addAlias.setActionCommand( "" );
            addAlias.addActionListener( new ActionListener()
            {
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    String initialValue = ( (JButton)e.getSource() ).getActionCommand();
                    String aliasName = (String)JOptionPane.showInputDialog( dialog, "Please type the name for the new border alias.", "New border alias", JOptionPane.INFORMATION_MESSAGE, null, null, initialValue );
                    if ( ( aliasName == null ) || ( aliasName.length() == 0 ) )
                    {
                        ( (JButton)e.getSource() ).setActionCommand( "" );
                        return;
                    }
                    
                    String border = BorderSelector.this.widgetsConfig.getBorderName( aliasName );
                    if ( border != null )
                    {
                        JOptionPane.showMessageDialog( dialog, "This alias already exists!", "New border alias (Error)", JOptionPane.ERROR_MESSAGE );
                        ( (JButton)e.getSource() ).setActionCommand( aliasName );
                        actionPerformed( e );
                        return;
                    }
                    
                    ( (JButton)e.getSource() ).setActionCommand( "" );
                    
                    BorderSelector.this.widgetsConfig.addBorderAlias( aliasName, (String)borderRenderer.renderCombo.getModel().getElementAt( 0 ) );
                    aliases = null;
                    fireTableDataChanged();
                    somethingChanged = true;
                }
            } );
            footer.add( addAlias, BorderLayout.WEST );
            
            this.valueTextField = new JTextField( selectedBorder );
            JPanel valueWrapper = new JPanel( new BorderLayout() );
            valueWrapper.add( valueTextField, BorderLayout.CENTER );
            valueWrapper.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
            footer.add( valueWrapper, BorderLayout.CENTER );
            
            JPanel footer2 = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) );
            JButton no = new JButton( "No Border" );
            no.addActionListener( new ActionListener()
            {
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    valueTextField.setText( "" );
                    
                    somethingChanged = somethingChanged || !valueTextField.getText().equals( startBorder );
                    
                    applyBorder( valueTextField.getText(), BorderSelector.this.widgetsConfig );
                    
                    dialog.setVisible( false );
                }
            } );
            footer2.add( no );
            JButton ok = new JButton( "OK" );
            ok.addActionListener( new ActionListener()
            {
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    somethingChanged = somethingChanged || !valueTextField.getText().equals( startBorder );
                    
                    applyBorder( valueTextField.getText(), BorderSelector.this.widgetsConfig );
                    
                    dialog.setVisible( false );
                }
            } );
            footer2.add( ok );
            JButton cancel = new JButton( "Cancel" );
            cancel.addActionListener( new ActionListener()
            {
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    valueTextField.setText( "" );
                    somethingChanged = false;
                    dialog.setVisible( false );
                }
            } );
            footer2.add( cancel );
            
            footer.add( footer2, BorderLayout.EAST );
            
            contentPane.add( footer, BorderLayout.SOUTH );
            
            dialog.setSize( 550, 500 );
            dialog.setLocationRelativeTo( owner );
            dialog.setModal( true );
        }
        
        borderRenderer.updateFiles();
        
        valueTextField.setText( selectedBorder );
        
        String border = widgetsConfig.getBorderName( selectedBorder );
        if ( border == null )
            noAliasSelection = selectedBorder;
        else
            noAliasSelection = border;
        
        dialog.setVisible( true );
        
        return ( getSelectedBorder() );
    }
    
    public String showDialog( Window owner, WidgetsConfiguration widgetsConfig )
    {
        return ( showDialog( owner, widgetsConfig, "" ) );
    }
    
    public BorderSelector( File folder )
    {
        if ( !folder.exists() )
            throw new IllegalArgumentException( "folder \"" + folder.getAbsolutePath() + "\" doesn't exist." );
        
        if ( !folder.isDirectory() )
            throw new IllegalArgumentException( "folder \"" + folder.getAbsolutePath() + "\" is not a folder." );
        
        this.folder = folder;
        
        if ( borderRenderer.updateFiles() == 0 )
            this.noAliasSelection = "";
        else
            this.noAliasSelection = (String)borderRenderer.renderCombo.getModel().getElementAt( 0 );
    }
    
    public BorderSelector( String folder )
    {
        this( new File( folder ) );
    }
}
