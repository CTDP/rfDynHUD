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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * The {@link ImageSelector} provides a dialog to select images from a certain
 * folder and its subfolders.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImageSelector extends JPanel
{
    private static final long serialVersionUID = 2419977725722070480L;
    
    private JList createList( File folder )
    {
        final JList list = new JList( new ImageListModel( folder ) );
        list.setFixedCellHeight( 50 );
        list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        list.setCellRenderer( new ListItem() );
        list.addListSelectionListener( new ListSelectionListener()
        {
            @Override
            public void valueChanged( ListSelectionEvent e )
            {
                JList list = (JList)e.getSource();
                Object value = list.getSelectedValue();
                
                selectedFile = (String)value;
            }
        } );
        
        return ( list );
    }
    
    private final JList list;
    
    private final File folder;
    
    private String selectedFile = null;
    
    public void update()
    {
        ( (ImageListModel)list.getModel() ).update();
    }
    
    public void setSelectedFile( String name )
    {
        if ( name.equals( "" ) )
        {
            this.selectedFile = "";
            return;
        }
        
        File file = new File( folder, name.replace( '/', File.separatorChar ) );
        if ( !file.exists() )
        {
            this.selectedFile = null;
            return;
        }
        
        if ( file.isDirectory() )
        {
            new IllegalArgumentException( "The given file is a directory" ).printStackTrace();
        }
        
        this.selectedFile = name;
        
        ImageListModel model = (ImageListModel)list.getModel();
        
        int selIndex = ( selectedFile == null ) ? -1 : model.getIndexOf( selectedFile );
        if ( selIndex >= 0 )
        {
            list.setSelectedIndex( selIndex );
            list.scrollRectToVisible( list.getCellBounds( selIndex, selIndex ) );
        }
    }
    
    public String getSelectedFile()
    {
        return ( selectedFile );
    }
    
    //private static HashMap<String, BufferedImage> cache = new HashMap<String, BufferedImage>();
    //private static HashMap<String, Long> cache2 = new HashMap<String, Long>();
    
    private BufferedImage getImageFromCache( String name )
    {
        File file = new File( folder, name );
        BufferedImage bi = null;//cache.get( name );
        /*
        if ( bi != null )
        {
            Long lastModified = cache2.get( name );
            
            if ( lastModified.longValue() != file.lastModified() )
                bi = null;
        }
        
        if ( bi == null )
        */
        {
            try
            {
                bi = ImageIO.read( file );
                //cache.put( name, bi );
                //cache2.put( name, file.lastModified() );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
        
        return ( bi );
    }
    
    private class ItemCanvas extends JPanel
    {
        private static final long serialVersionUID = 1L;
        
        private final String value;
        
        @Override
        public void paintComponent( Graphics g )
        {
            BufferedImage bi = getImageFromCache( value );
            
            if ( bi != null )
            {
                int height = getHeight();
                int width = bi.getWidth() * height / bi.getHeight();
                
                g.drawImage( bi, getWidth() - width, 0, width, height, null );
            }
        }
        
        public ItemCanvas( String value )
        {
            this.value = value;
        }
    }
    
    private class ListItem implements ListCellRenderer
    {
        private JPanel panel = null;
        private JLabel label = null;
        private JLabel label2 = null;
        private JPanel canvas = null;
        
        @Override
        public Component getListCellRendererComponent( JList list, final Object value, int index, boolean isSelected, boolean cellHasFocus )
        {
            //if ( panel == null )
            {
                panel = new JPanel( new BorderLayout() );
                label = new JLabel();
                label.setHorizontalAlignment( JLabel.RIGHT );
                label2 = new JLabel();
                label2.setPreferredSize( new Dimension( 100, 24 ) );
                label2.setMinimumSize( new Dimension( 100, 24 ) );
                label2.setBorder( new EmptyBorder( 0, 10, 0, 10 ) );
                canvas = new ItemCanvas( String.valueOf( value ) );
                
                panel.add( label, BorderLayout.WEST );
                panel.add( canvas, BorderLayout.CENTER );
                panel.add( label2, BorderLayout.EAST );
            }
            
            if ( isSelected )
            {
                panel.setBackground( list.getSelectionBackground() );
                panel.setForeground( list.getSelectionForeground() );
            }
            else
            {
                panel.setBackground( list.getBackground() );
                panel.setForeground( list.getForeground() );
            }
            
            String name = String.valueOf( value );
            label.setText( name );
            
            BufferedImage bi = getImageFromCache( name );
            if ( bi != null )
            {
                label2.setText( bi.getWidth() + "x" + bi.getHeight() + "x" + ( bi.getColorModel().hasAlpha() ? "32" : "24" ) );
            }
            
            return ( panel );
        }
    }
    
    private String showDialog( Window owner, boolean noImageAllowed )
    {
        update();
        
        final JDialog dialog;
        if ( owner instanceof Frame )
            dialog = new JDialog( (Frame)owner );
        else if ( owner instanceof Dialog )
            dialog = new JDialog( (Dialog)owner );
        else
            dialog = new JDialog( owner );
        
        dialog.setTitle( "Select an image..." );
        
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
                    selectedFile = "";
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
                selectedFile = null;
                dialog.setVisible( false );
            }
        } );
        footer3.add( cancel );
        footer.add( footer3, BorderLayout.EAST );
        
        contentPane.add( footer, BorderLayout.SOUTH );
        
        dialog.setSize( 380, 500 );
        dialog.setLocationRelativeTo( owner );
        dialog.setModal( true );
        dialog.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowOpened( WindowEvent e )
            {
                setSelectedFile( getSelectedFile() );
            }
        } );
        
        list.addMouseListener( new MouseAdapter()
        {
            @Override
            public void mouseClicked( MouseEvent e )
            {
                if ( ( e.getButton() == MouseEvent.BUTTON1 ) && ( e.getClickCount() == 2 ) )
                {
                    for ( int i = 0; i < list.getModel().getSize(); i++ )
                    {
                        Rectangle r = list.getCellBounds( i, i );
                        if ( r.contains( e.getPoint() ) )
                        {
                            selectedFile = (String)list.getSelectedValue();
                            dialog.setVisible( false );
                            break;
                        }
                    }
                }
            }
        } );
        
        dialog.setVisible( true );
        
        return ( selectedFile );
    }
    
    public String showDialog( Window owner, String selectedFile, boolean noImageAllowed )
    {
        setSelectedFile( selectedFile );
        
        return ( showDialog( owner, noImageAllowed ) );
    }
    
    public ImageSelector( File folder, String selectedFile )
    {
        super( new BorderLayout() );
        
        if ( !folder.exists() )
            throw new IllegalArgumentException( "folder \"" + folder.getAbsolutePath() + "\" doesn't exist." );
        
        if ( !folder.isDirectory() )
            throw new IllegalArgumentException( "folder \"" + folder.getAbsolutePath() + "\" is not a folder." );
        
        this.folder = folder;
        
        this.list = createList( folder );
        
        this.add( new JScrollPane( list ), BorderLayout.CENTER );
        
        if ( selectedFile != null )
            setSelectedFile( selectedFile );
    }
    
    public ImageSelector( File folder )
    {
        this( folder, null );
    }
    
    public ImageSelector( String folder )
    {
        this( new File( folder ) );
    }
}
