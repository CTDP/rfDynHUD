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
package net.ctdp.rfdynhud.editor.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.gui.awt_swing.GUITools;

/**
 * The {@link ImageSelector} provides a dialog to select images from a certain
 * folder and its subfolders.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImageSelector extends JPanel
{
    private static final long serialVersionUID = 2419977725722070480L;
    
    private final ImageTable table;
    private final ImageTableModel model;
    
    private String selectedFile = null;
    
    private JDialog dialog = null;
    private JDialog preview = null;
    
    public static interface DoubleClickSelectionListener
    {
        public void onImageSelectedByDoubleClick( String imageName );
        
        public void onDialogCloseRequested();
    }
    
    private final List<DoubleClickSelectionListener> doubleClickSelectionListeners = new ArrayList<ImageSelector.DoubleClickSelectionListener>();
    
    public void addDoubleClickSelectionListener( DoubleClickSelectionListener l )
    {
        this.doubleClickSelectionListeners.add( l );
    }
    
    public void removeDoubleClickSelectionListener( DoubleClickSelectionListener l )
    {
        this.doubleClickSelectionListeners.remove( l );
    }
    
    private void updateColumnWidths()
    {
        int rc = model.getRowCount();
        
        if ( rc == 0 )
            return;
        
        int maxWidth = 0;
        
        for ( int i = 0; i < rc; i++ )
        {
            Image thumb = (Image)model.getValueAt( i, 0 );
            if ( thumb != null )
            {
                maxWidth = Math.max( maxWidth, thumb.getWidth( null ) );
            }
        }
        
        table.getColumnModel().getColumn( 0 ).setMinWidth( maxWidth );
        table.getColumnModel().getColumn( 0 ).setMaxWidth( maxWidth );
        table.getColumnModel().getColumn( 0 ).setWidth( maxWidth );
        table.getColumnModel().getColumn( 0 ).setResizable( false );
        
        maxWidth = 0;
        FontMetrics metrics = table.getFontMetrics( table.getFont().deriveFont( Font.BOLD ) );
        
        for ( int i = 0; i < rc; i++ )
        {
            String size = (String)model.getValueAt( i, 2 );
            if ( size != null )
            {
                maxWidth = Math.max( maxWidth, (int)Math.ceil( metrics.getStringBounds( size, null ).getWidth() ) );
            }
        }
        
        maxWidth += PaddingCellRenderer.PADDING + 2 + PaddingCellRenderer.PADDING;
        
        table.getColumnModel().getColumn( 2 ).setMinWidth( maxWidth );
        table.getColumnModel().getColumn( 2 ).setMaxWidth( maxWidth );
        table.getColumnModel().getColumn( 2 ).setWidth( maxWidth );
        table.getColumnModel().getColumn( 2 ).setResizable( false );
    }
    
    public void update()
    {
        table.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
        
        if ( ( selectedFile == null ) || ( selectedFile.length() == 0 ) )
            model.setFolder( ImageTableModel.IMAGE_BASE_FOLDER );
        else
            model.setFolder( new File( ImageTableModel.IMAGE_BASE_FOLDER, selectedFile.replace( '/', File.separatorChar ) ).getParentFile() );
        
        updateColumnWidths();
        
        table.setCursor( Cursor.getDefaultCursor() );
    }
    
    private void setPreview( File imageFile )
    {
        if ( preview != null )
        {
            ( (ImagePreviewPanel)preview.getContentPane() ).setImage( imageFile );
            preview.getContentPane().repaint();
        }
    }
    
    public void setSelectedFile( String name )
    {
        if ( name.equals( "" ) )
        {
            this.selectedFile = "";
            update();
            setPreview( null );
            return;
        }
        
        File file = new File( ImageTableModel.IMAGE_BASE_FOLDER, name.replace( '/', File.separatorChar ) );
        if ( !file.exists() )
        {
            this.selectedFile = null;
            update();
            setPreview( null );
            return;
        }
        
        if ( file.isDirectory() )
        {
            RFDHLog.exception( "WARNING: The given file is a directory." );
            this.selectedFile = null;
            update();
            setPreview( null );
            return;
        }
        
        this.selectedFile = name;
        update();
        this.selectedFile = name;
        
        int selIndex = ( selectedFile == null ) ? -1 : model.getIndexOf( file.getName() );
        if ( selIndex >= 0 )
        {
            table.getSelectionModel().setSelectionInterval( selIndex, selIndex );
            table.scrollRectToVisible( table.getCellRect( selIndex, 0, false ) );
            
            setPreview( model.getFileAtRow( selIndex ) );
        }
        else
        {
            setPreview( null );
        }
    }
    
    public String getSelectedFile()
    {
        return ( selectedFile );
    }
    
    private void executeRowSelected()
    {
        int selIndex = table.getSelectedRow();
        
        if ( selIndex >= 0 )
        {
            File file = model.getFileAtRow( selIndex );
            
            if ( file.isDirectory() )
            {
                selectedFile = null;
                try
                {
                    model.setFolder( file.getCanonicalFile() );
                }
                catch ( IOException ex )
                {
                    model.setFolder( file );
                }
                
                updateColumnWidths();
            }
            else
            {
                for ( int i = 0; i < doubleClickSelectionListeners.size(); i++ )
                {
                    doubleClickSelectionListeners.get( i ).onImageSelectedByDoubleClick( selectedFile );
                }
            }
        }
    }
    
    private ImageTable createTable( File folder )
    {
        ImageTable t = new ImageTable( new ImageTableModel( folder ) );
        t.setRowHeight( ImageTableModel.ROW_HEIGHT );
        t.setShowVerticalLines( false );
        t.getTableHeader().setReorderingAllowed( false );
        t.setIntercellSpacing( new java.awt.Dimension( 0, 1 ) );
        
        t.getColumnModel().getColumn( 1 ).setMinWidth( 10 );
        
        t.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        
        t.getSelectionModel().addListSelectionListener( new ListSelectionListener()
        {
            @Override
            public void valueChanged( ListSelectionEvent e )
            {
                if ( !e.getValueIsAdjusting() )
                {
                    int selIndex = table.getSelectedRow();
                    if ( selIndex >= 0 )
                    {
                        File file = model.getFileAtRow( selIndex );
                        
                        if ( file.isFile() )
                        {
                            selectedFile = file.getAbsolutePath().substring( ImageTableModel.IMAGE_BASE_FOLDER.getAbsolutePath().length() + 1 ).replace( '\\', '/' );
                            setPreview( file );
                        }
                        else
                        {
                            selectedFile = null;
                            setPreview( null );
                        }
                    }
                    else
                    {
                        selectedFile = null;
                        setPreview( null );
                    }
                }
            }
        } );
        
        
        t.addMouseListener( new MouseAdapter()
        {
            @Override
            public void mouseClicked( MouseEvent e )
            {
                if ( ( e.getButton() == MouseEvent.BUTTON1 ) && ( e.getClickCount() == 2 ) )
                {
                    executeRowSelected();
                }
            }
        } );
        
        t.addKeyListener( new KeyAdapter()
        {
            @Override
            public void keyPressed( KeyEvent e )
            {
                if ( e.getKeyCode() == KeyEvent.VK_ENTER )
                {
                    e.consume();
                    
                    executeRowSelected();
                }
                else if ( e.getKeyCode() == KeyEvent.VK_ESCAPE )
                {
                    e.consume();
                    
                    for ( int i = 0; i < doubleClickSelectionListeners.size(); i++ )
                    {
                        doubleClickSelectionListeners.get( i ).onDialogCloseRequested();
                    }
                }
            }
        } );
        
        return ( t );
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
    
    private long nextPreviewLocFixTime = -1L;
    private long nextDialogLocFixTime = -1L;
    
    private void updatePreviewLocationAndSize( JDialog dialog )
    {
        if ( System.nanoTime() < nextPreviewLocFixTime )
            return;
        
        preview.setSize( dialog.getHeight(), dialog.getHeight() );
        
        Rectangle desktopRes = GUITools.getCurrentScreenBounds();
        
        if ( dialog.getX() < desktopRes.x + desktopRes.width - dialog.getX() - dialog.getWidth() )
            preview.setLocation( dialog.getX() + dialog.getWidth() + 3, dialog.getY() );
        else
            preview.setLocation( dialog.getX() - preview.getWidth() - 3, dialog.getY() );
        
        nextDialogLocFixTime = System.nanoTime() + 500000000L;
    }
    
    private void updateDialogLocation( JDialog dialog )
    {
        if ( System.nanoTime() < nextDialogLocFixTime )
            return;
        
        Rectangle desktopRes = GUITools.getCurrentScreenBounds();
        
        if ( preview.getX() - 3 - dialog.getWidth() < desktopRes.width - desktopRes.x - preview.getX() - preview.getWidth() - 3 - dialog.getWidth() )
            dialog.setLocation( preview.getX() + preview.getWidth() + 3, preview.getY() );
        else
            dialog.setLocation( preview.getX() - 3 - dialog.getWidth(), preview.getY() );
        
        
        nextPreviewLocFixTime = System.nanoTime() + 500000000L;
    }
    
    public void setPreviewVisible( boolean visible )
    {
        if ( preview != null )
            preview.setVisible( visible );
    }
    
    public void createPreview( final JDialog dialog )
    {
        preview = new JDialog( dialog, "Preview", false );
        preview.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
        preview.setSize( 500, 500 );
        preview.setContentPane( new ImagePreviewPanel() );
        
        updatePreviewLocationAndSize( dialog );
        
        dialog.addComponentListener( new ComponentAdapter()
        {
            @Override
            public void componentMoved( ComponentEvent e )
            {
                updatePreviewLocationAndSize( dialog );
            }
            
            @Override
            public void componentResized( ComponentEvent e )
            {
                updatePreviewLocationAndSize( dialog );
            }
        } );
        
        preview.addComponentListener( new ComponentAdapter()
        {
            @Override
            public void componentMoved( ComponentEvent e )
            {
                updateDialogLocation( dialog );
            }
        } );
        
        preview.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                ImageSelector.this.selectedFile = null;
                dialog.setVisible( false );
            }
        } );
    }
    
    public String showDialog( Window owner, String selectedFile, boolean noImageAllowed )
    {
        if ( ( dialog == null ) || ( dialog.getOwner() != owner ) )
        {
            dialog = initDialog( owner, "Select an image..." );
            
            dialog.addWindowListener( new WindowAdapter()
            {
                @Override
                public void windowClosing( WindowEvent e )
                {
                    ImageSelector.this.selectedFile = null;
                    dialog.setVisible( false );
                }
            } );
            
            Container contentPane = dialog.getContentPane();
            contentPane.setLayout( new BorderLayout() );
            
            contentPane.add( this, BorderLayout.CENTER );
            
            JPanel footer = new JPanel( new BorderLayout() );
            if ( noImageAllowed )
            {
                JPanel footer2 = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 5 ) );
                JButton no = new JButton( "No Image" );
                no.addActionListener( new ActionListener()
                {
                    @Override
                    public void actionPerformed( ActionEvent e )
                    {
                        ImageSelector.this.selectedFile = "";
                        dialog.setVisible( false );
                    }
                } );
                footer2.add( no );
                footer.add( footer2, BorderLayout.WEST );
            }
            JPanel footer3 = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 5 ) );
            JButton ok = new JButton( "OK" );
            ok.addActionListener( new ActionListener()
            {
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    dialog.setVisible( false );
                }
            } );
            footer3.add( ok );
            JButton cancel = new JButton( "Cancel" );
            cancel.addActionListener( new ActionListener()
            {
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    ImageSelector.this.selectedFile = null;
                    dialog.setVisible( false );
                }
            } );
            footer3.add( cancel );
            footer.add( footer3, BorderLayout.EAST );
            
            contentPane.add( footer, BorderLayout.SOUTH );
            
            dialog.setSize( 380, 500 );
            dialog.setLocationRelativeTo( owner );
            dialog.setLocation( dialog.getX() - 250, dialog.getY() );
            dialog.setModal( true );
            
            createPreview( dialog );
        }
        
        setSelectedFile( selectedFile );
        
        table.requestFocus();
        
        setPreviewVisible( true );
        dialog.setVisible( true );
        
        return ( ImageSelector.this.selectedFile );
    }
    
    public String showDialog( Window owner, boolean noImageAllowed )
    {
        return ( showDialog( owner, null, noImageAllowed ) );
    }
    
    public ImageSelector()
    {
        super( new BorderLayout() );
        
        /*
        if ( !folder.exists() )
            throw new IllegalArgumentException( "folder \"" + folder.getAbsolutePath() + "\" doesn't exist." );
        
        if ( !folder.isDirectory() )
            throw new IllegalArgumentException( "folder \"" + folder.getAbsolutePath() + "\" is not a folder." );
        */
        
        this.table = createTable( ImageTableModel.IMAGE_BASE_FOLDER );
        this.model = table.getModel();
        
        this.add( new JScrollPane( table ), BorderLayout.CENTER );
        
        addDoubleClickSelectionListener( new DoubleClickSelectionListener()
        {
            @Override
            public void onImageSelectedByDoubleClick( String imageName )
            {
                if ( dialog != null )
                    dialog.setVisible( false );
            }
            
            @Override
            public void onDialogCloseRequested()
            {
                selectedFile = null;
                
                if ( dialog != null )
                    dialog.setVisible( false );
            }
        } );
    }
}
