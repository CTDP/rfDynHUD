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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.table.DefaultTableModel;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.util.RFDHLog;

/**
 * Table model for the image selector.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImageTableModel extends DefaultTableModel
{
    private static final long serialVersionUID = -457944126607540784L;
    
    private static final BufferedImage readFolderImage()
    {
        try
        {
            return ( ImageIO.read( ImageTableModel.class.getClassLoader().getResource( "data/folder.png" ) ) );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
            
            return ( null );
        }
    }
    
    public static final File IMAGE_BASE_FOLDER = RFDynHUDEditor.FILESYSTEM.getImagesFolder();
    
    public static final int ROW_HEIGHT = 50;
    private static final BufferedImage FOLDER_IMAGE = readFolderImage();
    private static final String HIDE_FOLDER_FILENAME = ".hide_from_image_selector";
    
    private File folder;
    
    private final ArrayList<String> folderNames = new ArrayList<String>();
    private final ArrayList<String> imageNames = new ArrayList<String>();
    private final HashMap<String, Image> thumbsCache = new HashMap<String, Image>();
    private final HashMap<String, String> sizeCache = new HashMap<String, String>();
    
    private static void readFoldernames( File folder, List<String> filenames )
    {
        File[] files = folder.listFiles( FolderFileFilter.INSTANCE );
        if ( files != null )
        {
            for ( File f : files )
            {
                if ( !new File( f, HIDE_FOLDER_FILENAME ).exists() )
                    filenames.add( f.getName() );
            }
        }
    }
    
    private static void readFilenames( File folder, List<String> filenames )
    {
        File[] files = folder.listFiles( ImageFileFilter.INSTANCE );
        if ( files != null )
        {
            for ( File f : files )
            {
                filenames.add( f.getName() );
            }
        }
    }
    
    private void createThumbnail( String imageName )
    {
        File file = new File( folder, imageName );
        
        BufferedImage image = null;
        
        try
        {
            image = ImageIO.read( file );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            
            return;
        }
        
        Image thumb;
        
        int maxWidth = 125;
        int maxHeight = ROW_HEIGHT - 3;
        
        if ( ( image.getWidth() > maxWidth ) || ( image.getHeight() > maxHeight ) )
        {
            float scaleX = maxWidth / (float)image.getWidth();
            float scaleY = maxHeight / (float)image.getHeight();
            float scale = Math.min( scaleX, scaleY );
            
            int width = Math.min( Math.round( image.getWidth() * scale ), maxWidth );
            int height = Math.min( Math.round( image.getHeight() * scale ), maxHeight );
            
            thumb = image.getScaledInstance( width, height, Image.SCALE_FAST );
        }
        else
        {
            thumb = image;
        }
        
        thumbsCache.put( imageName, thumb );
        sizeCache.put( imageName, image.getWidth() + "x" + image.getHeight() + "x" + ( image.getColorModel().hasAlpha() ? "32" : "24" ) );
    }
    
    public void setFolder( File folder )
    {
        if ( !folder.exists() )
            folder = IMAGE_BASE_FOLDER;
        
        this.folder = folder;
        
        folderNames.clear();
        readFoldernames( folder, folderNames );
        Collections.sort( folderNames );
        if ( !folder.equals( IMAGE_BASE_FOLDER ) )
            folderNames.add( 0, ".." );
        
        imageNames.clear();
        readFilenames( folder, imageNames );
        Collections.sort( imageNames );
        
        thumbsCache.clear();
        for ( String imageName : imageNames )
        {
            createThumbnail( imageName );
        }
        
        fireTableDataChanged();
        fireTableStructureChanged();
    }
    
    public final File getFolder()
    {
        return ( folder );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount()
    {
        if ( folderNames == null )
            return ( 0 );
        
        return ( folderNames.size() + imageNames.size() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount()
    {
        return ( 3 );
    }
    
    public final int getIndexOf( String imageName )
    {
        int index = Collections.binarySearch( imageNames, imageName );
        
        if ( index < 0 )
            return ( index - folderNames.size() );
        
        return ( folderNames.size() + index );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName( int column )
    {
        /*
        table.getColumnModel().getColumn( 0 ).setHeaderValue( "Thumbnail" );
        table.getColumnModel().getColumn( 1 ).setHeaderValue( "Image name" );
        table.getColumnModel().getColumn( 2 ).setHeaderValue( "Size" );
        */
        
        if ( column == 0 )
            return ( "Thumbnail" );
        
        if ( column == 1 )
            return ( "Image name" );
        
        if ( column == 2 )
            return ( "Size" );
        
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt( int row, int column )
    {
        if ( column == 0 )
        {
            if ( row < folderNames.size() )
                return ( FOLDER_IMAGE );
            
            String imageName = imageNames.get( row - folderNames.size() );
            Image thumb = thumbsCache.get( imageName );
            
            return ( thumb );
        }
        
        if ( column == 1 )
        {
            if ( row < folderNames.size() )
                return ( "[" + folderNames.get( row ) + "]" );
            
            return ( imageNames.get( row - folderNames.size() ) );
        }
        
        if ( column == 2 )
        {
            if ( row < folderNames.size() )
                return ( "" );
            
            String imageName = imageNames.get( row - folderNames.size() );
            String size = sizeCache.get( imageName );
            
            if ( size == null )
                return ( "N/A" );
            
            return ( size );
        }
        
        // Unreachable code!
        throw new IllegalArgumentException();
    }
    
    public File getFileAtRow( int row )
    {
        if ( row < folderNames.size() )
            return ( new File( folder, folderNames.get( row ) ) );
        
        return ( new File( folder, imageNames.get( row - folderNames.size() ) ) );
    }
    
    public ImageTableModel( File initialFolder )
    {
        if ( ( initialFolder != null ) && !initialFolder.exists() )
            initialFolder = IMAGE_BASE_FOLDER;
        
        this.folder = initialFolder;
    }
}
