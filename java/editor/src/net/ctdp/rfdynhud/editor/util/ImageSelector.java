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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

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
 * @author Marvin Froehlich
 */
public class ImageSelector
{
    private JDialog dialog = null;
    private final File folder;
    
    private String selectedFile = null;
    
    /*
    public void setSelectedFile( File file )
    {
        if ( !file.getAbsolutePath().startsWith( folder.getAbsolutePath() ) )
            throw new IllegalArgumentException( "The given file is not in the search folder." );
        
        if ( !file.exists() )
            this.selectedFile = null;
        else
            this.selectedFile = file;
    }
    
    public void setSelectedFile( String filename )
    {
        File file = new File( filename );
        if ( file.isAbsolute() )
            setSelectedFile( file );
        else
            setSelectedFile( new File( folder, filename ) );
    }
    
    public final File getSelectedFile()
    {
        return ( selectedFile );
    }
    */
    
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
    }
    
    public String getSelectedFile()
    {
        return ( selectedFile );
    }
    
    private void readFilenames( File folder, String prefix, List<String> filenames )
    {
        for ( File f : folder.listFiles( ImageFileFilter.INSTANCE ) )
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
                if ( prefix == null )
                    filenames.add( f.getName() );
                else
                    filenames.add( prefix + "/" + f.getName() );
            }
        }
    }
    
    private HashMap<String, BufferedImage> cache = new HashMap<String, BufferedImage>();
    
    private BufferedImage getImageFromCache( String name )
    {
        BufferedImage bi = cache.get( name );
        if ( bi == null )
        {
            File file = new File( folder, name );
            try
            {
                bi = ImageIO.read( file );
                cache.put( name, bi );
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
        final Vector<String> files = new Vector<String>();
        readFilenames( folder, null, files );
        Collections.sort( files );
        
        final JList list = new JList( files );
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
        
        if ( owner instanceof Frame )
            dialog = new JDialog( (Frame)owner );
        else if ( owner instanceof Dialog )
            dialog = new JDialog( (Dialog)owner );
        else
            dialog = new JDialog( owner );
        
        dialog.setTitle( "Select an image..." );
        
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout( new BorderLayout() );
        
        contentPane.add( new JScrollPane( list ), BorderLayout.CENTER );
        
        JPanel footer = new JPanel( new BorderLayout() );
        if ( noImageAllowed )
        {
            JPanel footer2 = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 5 ) );
            JButton no = new JButton( "No Image" );
            no.addActionListener( new ActionListener()
            {
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
            public void actionPerformed( ActionEvent e )
            {
                dialog.setVisible( false );
            }
        } );
        footer3.add( ok );
        JButton cancel = new JButton( "Cancel" );
        cancel.addActionListener( new ActionListener()
        {
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
                int selIndex = ( selectedFile == null ) ? -1 : Collections.binarySearch( files, selectedFile );
                if ( selIndex >= 0 )
                {
                    list.setSelectedIndex( selIndex );
                    list.scrollRectToVisible( list.getCellBounds( selIndex, selIndex ) );
                }
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
    
    public ImageSelector( File folder )
    {
        if ( !folder.exists() )
            throw new IllegalArgumentException( "folder \"" + folder.getAbsolutePath() + "\" doesn't exist." );
        
        if ( !folder.isDirectory() )
            throw new IllegalArgumentException( "folder \"" + folder.getAbsolutePath() + "\" is not a folder." );
        
        this.folder = folder;
    }
    
    public ImageSelector( String folder )
    {
        this( new File( folder ) );
    }
}
