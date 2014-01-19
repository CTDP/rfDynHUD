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
package net.ctdp.rfdynhud.widgets.etv2010._util;

import java.awt.geom.Rectangle2D;
import java.io.File;

import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.util.TextureManager;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * This class encapsulates the images and assotiated properties used to paint the textured ETV widgets.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ETVImages
{
    public static enum BGType
    {
        CAPTION,
        POSITION_FIRST,
        FASTEST,
        //FASTEST_VS_1ST,
        FASTER,
        NEUTRAL,
        //NEUTRAL_VS_1ST,
        SLOWER,
        //SLOWER_VS_1ST,
        LABEL_YELLOW,
        LABEL_RED,
        ;
    }
    
    private ImageTemplate big_position_neutral_image = null;
    private ImageTemplate big_position_first_image = null;
    
    private int big_position_virtual_projection_border_left = 0;
    private int big_position_border_left = 0;
    private int big_position_border_right = 0;
    private int big_position_virtual_projection_border_right = 0;
    
    private ImageTemplate compare_fastest_image = null;
    private ImageTemplate compare_faster_image = null;
    private ImageTemplate compare_neutral_image = null;
    private ImageTemplate compare_slower_image = null;
    
    private int compare_virtual_projection_border_left = 0;
    private int compare_position_border_left = 0;
    private int compare_position_width = 0;
    private int compare_separator_width = 0;
    private int compare_data_border_right = 0;
    private int compare_virtual_projection_border_right = 0;
    
    private ImageTemplate data_caption_image = null;
    private ImageTemplate data_fastest_image = null;
    private ImageTemplate data_faster_image = null;
    private ImageTemplate data_neutral_image = null;
    private ImageTemplate data_slower_image = null;
    
    private int data_virtual_projection_border_left = 0;
    private int data_border_left = 0;
    private int data_border_right = 0;
    private int data_virtual_projection_border_right = 0;
    
    private ImageTemplate labeled_data_first_image = null;
    private ImageTemplate labeled_data_neutral_image = null;
    private ImageTemplate labeled_data_red_image = null;
    private ImageTemplate labeled_data_yellow_image = null;
    
    private int labeled_virtual_projection_border_left = 0;
    private int labeled_data_label_border_left = 0;
    private int labeled_data_label_width = 0;
    private int labeled_data_separator_width = 0;
    private int labeled_data_data_border_right = 0;
    private int labeled_virtual_projection_border_right = 0;
    
    public final ImageTemplate getBigPositionImage( boolean first )
    {
        if ( first )
            return ( big_position_first_image );
        
        return ( big_position_neutral_image );
    }
    
    public final int getBigPositionVirtualProjectionBorderLeft()
    {
        return ( big_position_virtual_projection_border_left );
    }
    
    public final int getBigPositionBorderLeft()
    {
        return ( big_position_border_left );
    }
    
    public final int getBigPositionBorderRight()
    {
        return ( big_position_border_right );
    }
    
    public final int getBigPositionVirtualProjectionBorderRight()
    {
        return ( big_position_virtual_projection_border_right );
    }
    
    public final ImageTemplate getCompareImage( BGType type )
    {
        switch ( type )
        {
            case FASTEST:
                return ( compare_fastest_image );
            case FASTER:
                return ( compare_faster_image );
            case SLOWER:
                return ( compare_slower_image );
            case NEUTRAL:
            case POSITION_FIRST:
            default:
                return ( compare_neutral_image );
        }
    }
    
    public final int getCompareVirtualProjectionBorderLeft()
    {
        return ( compare_virtual_projection_border_left );
    }
    
    public final int getComparePositionBorderLeft()
    {
        return ( compare_position_border_left );
    }
    
    public final int getComparePositionWidth()
    {
        return ( compare_position_width );
    }
    
    public final int getCompareSeparatorWidth()
    {
        return ( compare_separator_width );
    }
    
    public final int getCompareDataBorderRight()
    {
        return ( compare_data_border_right );
    }
    
    public final int getCompareVirtualProjectionBorderRight()
    {
        return ( compare_virtual_projection_border_right );
    }
    
    public final ImageTemplate getDataImage( BGType type )
    {
        switch ( type )
        {
            case CAPTION:
                return ( data_caption_image );
            case FASTEST:
                return ( data_fastest_image );
            case FASTER:
                return ( data_faster_image );
            case SLOWER:
                return ( data_slower_image );
            case NEUTRAL:
            default:
                return ( data_neutral_image );
        }
    }
    
    public final int getDataVirtualProjectionBorderLeft()
    {
        return ( data_virtual_projection_border_left );
    }
    
    public final int getDataBorderLeft()
    {
        return ( data_border_left );
    }
    
    public final int getDataBorderRight()
    {
        return ( data_border_right );
    }
    
    public final int getDataVirtualProjectionBorderRight()
    {
        return ( data_virtual_projection_border_right );
    }
    
    public final ImageTemplate getLabeledDataImage( BGType type )
    {
        switch ( type )
        {
            case POSITION_FIRST:
                return ( labeled_data_first_image );
            case FASTEST:
                return ( compare_fastest_image );
            case FASTER:
                return ( compare_faster_image );
            case SLOWER:
                return ( compare_slower_image );
            case LABEL_YELLOW:
                return ( labeled_data_yellow_image );
            case LABEL_RED:
                return ( labeled_data_red_image );
            case NEUTRAL:
            default:
                return ( labeled_data_neutral_image );
        }
    }
    
    public final int getLabeledDataVirtualProjectionBorderLeft()
    {
        return ( labeled_virtual_projection_border_left );
    }
    
    public final int getLabeledDataLabelBorderLeft()
    {
        return ( labeled_data_label_border_left );
    }
    
    public final int getLabeledDataLabelWidth()
    {
        return ( labeled_data_label_width );
    }
    
    public final int getLabeledDataSeparatorWidth()
    {
        return ( labeled_data_separator_width );
    }
    
    public final int getLabeledDataDataBorderRight()
    {
        return ( labeled_data_data_border_right );
    }
    
    public final int getLabeledDataVirtualProjectionBorderRight()
    {
        return ( labeled_virtual_projection_border_right );
    }
    
    public final float getBigPositionImageScale( int height )
    {
        final ImageTemplate it = getBigPositionImage( false );
        final float scale = (float)height / (float)it.getBaseHeight();
        
        return ( scale );
    }
    
    public final int getBigPositionCaptionLeftS( float scale )
    {
        int captionLeft = Math.round( ( getBigPositionBorderLeft() ) * scale );
        
        return ( captionLeft );
    }
    
    public final int getBigPositionCaptionLeft( int height )
    {
        final float scale = getBigPositionImageScale( height );
        
        return ( getBigPositionCaptionLeftS( scale ) );
    }
    
    public final int getBigPositionWidthS( float scale, int captionWidth )
    {
        int result = Math.round( ( getBigPositionBorderLeft() + getBigPositionBorderRight() ) * scale ) + captionWidth;
        
        return ( result );
    }
    
    public final int getBigPositionWidth( int height, int captionWidth )
    {
        final float scale = getBigPositionImageScale( height );
        
        return ( getBigPositionWidthS( scale, captionWidth ) );
    }
    
    public final int getBigPositionWidthS( float scale, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( getBigPositionWidthS( scale, capWidth ) );
    }
    
    public final int getBigPositionWidth( int height, Rectangle2D captionBounds )
    {
        final float scale = getBigPositionImageScale( height );
        
        return ( getBigPositionWidthS( scale, captionBounds ) );
    }
    
    public final int getBigPositionCaptionCenterS( float scale, int captionWidth )
    {
        int captionCenter = Math.round( ( ( getBigPositionBorderLeft() ) * scale ) + captionWidth / 2 );
        
        return ( captionCenter );
    }
    
    public final int getBigPositionCaptionCenter( int height, int captionWidth )
    {
        final float scale = getBigPositionImageScale( height );
        
        return ( getBigPositionCaptionCenterS( scale, captionWidth ) );
    }
    
    public final int getBigPositionCaptionCenterS( float scale, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( getBigPositionCaptionCenterS( scale, capWidth ) );
    }
    
    public final int getBigPositionCaptionCenter( int height, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( getBigPositionCaptionCenter( height, capWidth ) );
    }
    
    public final int getBigPositionCaptionRightS( float scale, int captionWidth )
    {
        int captionCenter = Math.round( ( ( getBigPositionBorderLeft() ) * scale ) + captionWidth );
        
        return ( captionCenter );
    }
    
    public final int getBigPositionCaptionRight( int height, int captionWidth )
    {
        final float scale = getBigPositionImageScale( height );
        
        return ( getBigPositionCaptionRightS( scale, captionWidth ) );
    }
    
    public final int getBigPositionCaptionRightS( float scale, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( getBigPositionCaptionRightS( scale, capWidth ) );
    }
    
    public final int getBigPositionCaptionRight( int height, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( getBigPositionCaptionRight( height, capWidth ) );
    }
    
    public final float getDataImageScale( int height )
    {
        final ImageTemplate it = getDataImage( ETVImages.BGType.NEUTRAL );
        final float scale = (float)height / (float)it.getBaseHeight();
        
        return ( scale );
    }
    
    public final int getDataDataWidthS( float scale, int width )
    {
        int dataAreaWidth = width - Math.round( ( getDataBorderLeft() + getDataBorderRight() ) * scale );
        
        return ( dataAreaWidth );
    }
    
    public final int getDataDataWidth( int width, int height )
    {
        final float scale = getDataImageScale( height );
        
        return ( getDataDataWidthS( scale, width ) );
    }
    
    public final int getDataDataLeftS( float scale )
    {
        int dataAreaLeft = Math.round( ( getDataBorderLeft() ) * scale );
        
        return ( dataAreaLeft );
    }
    
    public final int getDataDataLeft( int height )
    {
        final float scale = getDataImageScale( height );
        
        return ( getDataDataLeftS( scale ) );
    }
    
    public final int getDataDataCenterS( float scale, int width )
    {
        int dataAreaCenter = getDataDataWidthS( scale, width ) / 2;
        
        return ( dataAreaCenter );
    }
    
    public final int getDataDataCenter( int width, int height )
    {
        final float scale = getDataImageScale( height );
        
        int dataAreaCenter = getDataDataLeftS( scale ) + getDataDataWidthS( scale, width ) / 2;
        
        return ( dataAreaCenter );
    }
    
    public final int getDataDataRightS( float scale, int width )
    {
        int dataAreaRight = width - Math.round( ( getDataBorderRight() ) * scale );
        
        return ( dataAreaRight );
    }
    
    public final int getDataDataRight( int width, int height )
    {
        final float scale = getDataImageScale( height );
        
        return ( getDataDataRightS( scale, width ) );
    }
    
    public final float getLabeledDataImageScale( int height )
    {
        final ImageTemplate it = getLabeledDataImage( ETVImages.BGType.NEUTRAL );
        final float scale = (float)height / (float)it.getBaseHeight();
        
        return ( scale );
    }
    
    public final int getLabeledDataDataWidthS( float scale, int width, int captionWidth )
    {
        int dataAreaWidth = width - Math.round( ( getLabeledDataLabelBorderLeft() + getLabeledDataSeparatorWidth() + getLabeledDataDataBorderRight() ) * scale ) - captionWidth;
        
        return ( dataAreaWidth );
    }
    
    public final int getLabeledDataDataWidthS( float scale, int width, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( getLabeledDataDataWidthS( scale, width, capWidth ) );
    }
    
    public final int getLabeledDataDataWidth( int width, int height, Rectangle2D captionBounds )
    {
        final float scale = getLabeledDataImageScale( height );
        
        return ( getLabeledDataDataWidthS( scale, width, captionBounds ) );
    }
    
    public final int getLabeledDataDataLeftS( float scale, int captionWidth )
    {
        int dataAreaLeft = Math.round( ( getLabeledDataLabelBorderLeft() + getLabeledDataSeparatorWidth() ) * scale ) + captionWidth;
        
        return ( dataAreaLeft );
    }
    
    public final int getLabeledDataDataLeftS( float scale, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( getLabeledDataDataLeftS( scale, capWidth ) );
    }
    
    public final int getLabeledDataDataLeft( int height, Rectangle2D captionBounds )
    {
        final float scale = getLabeledDataImageScale( height );
        
        return ( getLabeledDataDataLeftS( scale, captionBounds ) );
    }
    
    public final int getLabeledDataDataCenterS( float scale, int width, int captionWidth )
    {
        int dataAreaWidth = getLabeledDataDataWidthS( scale, width, captionWidth );
        int dataAreaCenter = width - Math.round( ( getLabeledDataDataBorderRight() ) * scale ) - dataAreaWidth / 2;
        
        return ( dataAreaCenter );
    }
    
    public final int getLabeledDataDataCenterS( float scale, int width, Rectangle2D captionBounds )
    {
        int dataAreaWidth = getLabeledDataDataWidthS( scale, width, captionBounds );
        int dataAreaCenter = width - Math.round( ( getLabeledDataDataBorderRight() ) * scale ) - dataAreaWidth / 2;
        
        return ( dataAreaCenter );
    }
    
    public final int getLabeledDataDataCenter( int width, int height, Rectangle2D captionBounds )
    {
        final float scale = getLabeledDataImageScale( height );
        
        return ( getLabeledDataDataCenterS( scale, width, captionBounds ) );
    }
    
    public final int getLabeledDataDataRightS( float scale, int width )
    {
        int dataAreaRight = width - Math.round( ( getLabeledDataDataBorderRight() ) * scale );
        
        return ( dataAreaRight );
    }
    
    public final int getLabeledDataDataRight( int width, int height )
    {
        final float scale = getLabeledDataImageScale( height );
        
        return ( getLabeledDataDataRightS( scale, width ) );
    }
    
    public final int getLabeledDataCaptionLeftS( float scale )
    {
        int captionLeft = Math.round( ( getLabeledDataLabelBorderLeft() ) * scale );
        
        return ( captionLeft );
    }
    
    public final int getLabeledDataCaptionLeft( int height )
    {
        final float scale = getLabeledDataImageScale( height );
        
        return ( getLabeledDataCaptionLeftS( scale ) );
    }
    
    public final int getLabeledDataCaptionCenterS( float scale, int captionWidth )
    {
        int captionRight = Math.round( ( getLabeledDataLabelBorderLeft() ) * scale ) + ( captionWidth / 2 );
        
        return ( captionRight );
    }
    
    public final int getLabeledDataCaptionCenterS( float scale, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( getLabeledDataCaptionCenterS( scale, capWidth ) );
    }
    
    public final int getLabeledDataCaptionCenter( int height, Rectangle2D captionBounds )
    {
        final float scale = getLabeledDataImageScale( height );
        
        return ( getLabeledDataCaptionCenterS( scale, captionBounds ) );
    }
    
    public final int getLabeledDataCaptionRightS( float scale, int captionWidth )
    {
        int captionRight = Math.round( ( getLabeledDataLabelBorderLeft() ) * scale ) + captionWidth;
        
        return ( captionRight );
    }
    
    public final int getLabeledDataCaptionRightS( float scale, Rectangle2D captionBounds )
    {
        int capWidth = (int)Math.ceil( captionBounds.getWidth() );
        
        return ( getLabeledDataCaptionRightS( scale, capWidth ) );
    }
    
    public final int getLabeledDataCaptionRight( int height, Rectangle2D captionBounds )
    {
        final float scale = getLabeledDataImageScale( height );
        
        return ( getLabeledDataCaptionRightS( scale, captionBounds ) );
    }
    
    private void load( File iniFile )
    {
        big_position_neutral_image = TextureManager.getMissingImage();
        big_position_first_image = TextureManager.getMissingImage();
        
        compare_fastest_image = TextureManager.getMissingImage();
        compare_faster_image = TextureManager.getMissingImage();
        compare_neutral_image = TextureManager.getMissingImage();
        compare_slower_image = TextureManager.getMissingImage();
        
        data_caption_image = TextureManager.getMissingImage();
        data_fastest_image = TextureManager.getMissingImage();
        data_faster_image = TextureManager.getMissingImage();
        data_neutral_image = TextureManager.getMissingImage();
        data_slower_image = TextureManager.getMissingImage();
        
        labeled_data_first_image = TextureManager.getMissingImage();
        labeled_data_neutral_image = TextureManager.getMissingImage();
        labeled_data_red_image = TextureManager.getMissingImage();
        labeled_data_yellow_image = TextureManager.getMissingImage();
        
        try
        {
            if ( iniFile == null )
                throw new IllegalArgumentException( "iniFile must not be null." );
            
            if ( iniFile.exists() )
            {
                final String folder;
                if ( iniFile.getParentFile() == null )
                    folder = "";
                else
                    folder = iniFile.getParentFile().getPath() + File.separator;
                
                new AbstractIniParser()
                {
                    @Override
                    protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                    {
                        try
                        {
                            if ( group == null )
                            {
                            }
                            else if ( group.equals( "big_position" ) )
                            {
                                if ( key.equals( "neutral_image" ) )
                                    big_position_neutral_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "first_image" ) )
                                    big_position_first_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "virtual_projection_border_left" ) )
                                    big_position_virtual_projection_border_left = Integer.parseInt( value );
                                else if ( key.equals( "border_left" ) )
                                    big_position_border_left = Integer.parseInt( value );
                                else if ( key.equals( "border_right" ) )
                                    big_position_border_right = Integer.parseInt( value );
                                else if ( key.equals( "virtual_projection_border_right" ) )
                                    big_position_virtual_projection_border_right = Integer.parseInt( value );
                            }
                            else if ( group.equals( "compare" ) )
                            {
                                if ( key.equals( "fastest_image" ) )
                                    compare_fastest_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "faster_image" ) )
                                    compare_faster_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "neutral_image" ) )
                                    compare_neutral_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "slower_image" ) )
                                    compare_slower_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "virtual_projection_border_left" ) )
                                    compare_virtual_projection_border_left = Integer.parseInt( value );
                                else if ( key.equals( "position_border_left" ) )
                                    compare_position_border_left = Integer.parseInt( value );
                                else if ( key.equals( "position_width" ) )
                                    compare_position_width = Integer.parseInt( value );
                                else if ( key.equals( "separator_width" ) )
                                    compare_separator_width = Integer.parseInt( value );
                                else if ( key.equals( "data_border_right" ) )
                                    compare_data_border_right = Integer.parseInt( value );
                                else if ( key.equals( "virtual_projection_border_right" ) )
                                    compare_virtual_projection_border_right = Integer.parseInt( value );
                            }
                            else if ( group.equals( "data" ) )
                            {
                                if ( key.equals( "caption_image" ) )
                                    data_caption_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "fastest_image" ) )
                                    data_fastest_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "faster_image" ) )
                                    data_faster_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "neutral_image" ) )
                                    data_neutral_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "slower_image" ) )
                                    data_slower_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "virtual_projection_border_left" ) )
                                    data_virtual_projection_border_left = Integer.parseInt( value );
                                else if ( key.equals( "border_left" ) )
                                    data_border_left = Integer.parseInt( value );
                                else if ( key.equals( "border_right" ) )
                                    data_border_right = Integer.parseInt( value );
                                else if ( key.equals( "virtual_projection_border_right" ) )
                                    data_virtual_projection_border_right = Integer.parseInt( value );
                            }
                            else if ( group.equals( "labeled_data" ) )
                            {
                                if ( key.equals( "position_first_image" ) )
                                    labeled_data_first_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "neutral_image" ) )
                                    labeled_data_neutral_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "yellow_image" ) )
                                    labeled_data_yellow_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "red_image" ) )
                                    labeled_data_red_image = TextureManager.getImage( folder + value );
                                else if ( key.equals( "virtual_projection_border_left" ) )
                                    labeled_virtual_projection_border_left = Integer.parseInt( value );
                                else if ( key.equals( "label_border_left" ) )
                                    labeled_data_label_border_left = Integer.parseInt( value );
                                else if ( key.equals( "label_width" ) )
                                    labeled_data_label_width = Integer.parseInt( value );
                                else if ( key.equals( "separator_width" ) )
                                    labeled_data_separator_width = Integer.parseInt( value );
                                else if ( key.equals( "data_border_right" ) )
                                    labeled_data_data_border_right = Integer.parseInt( value );
                                else if ( key.equals( "virtual_projection_border_right" ) )
                                    labeled_virtual_projection_border_right = Integer.parseInt( value );
                            }
                        }
                        catch ( Throwable t )
                        {
                            RFDHLog.exception( t );
                        }
                        
                        return ( true );
                    }
                }.parse( iniFile );
            }
            else
            {
                //throw new FileNotFoundException( "iniFile \"" + iniFile.getAbsolutePath() + "\" not found." );
                RFDHLog.error( "ERROR: ini file \"" + iniFile.getAbsolutePath() + "\" not found." );
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
    }
    
    public ETVImages( File iniFile )
    {
        load( iniFile );
    }
}
