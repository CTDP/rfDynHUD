package net.ctdp.rfdynhud.editor.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    
    private class ListItem implements ListCellRenderer
    {
        private JPanel panel = null;
        private JLabel label = null;
        private JPanel canvas = null;
        
        public Component getListCellRendererComponent( JList list, final Object value, int index, boolean isSelected, boolean cellHasFocus )
        {
            //if ( panel == null )
            {
                panel = new JPanel( new BorderLayout() );
                label = new JLabel();
                canvas = new JPanel()
                {
                    private static final long serialVersionUID = 1L;
                    
                    @Override
                    public void paintComponent( Graphics g )
                    {
                        BufferedImage bi = cache.get( String.valueOf( value ) );
                        if ( bi == null )
                        {
                            File file = new File( folder, String.valueOf( value ) );
                            try
                            {
                                bi = ImageIO.read( file );
                                cache.put( String.valueOf( value ), bi );
                            }
                            catch ( IOException e )
                            {
                                e.printStackTrace();
                            }
                        }
                        
                        if ( bi != null )
                        {
                            int height = getHeight();
                            int width = bi.getWidth() * height / bi.getHeight();
                            
                            g.drawImage( bi, getWidth() - width, 0, width, height, null );
                        }
                    }
                };
                
                panel.add( label, BorderLayout.WEST );
                panel.add( canvas, BorderLayout.CENTER );
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
        
        dialog.setSize( 300, 500 );
        dialog.setLocationRelativeTo( owner );
        dialog.setModal( true );
        dialog.addWindowListener( new WindowAdapter()
        {
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
