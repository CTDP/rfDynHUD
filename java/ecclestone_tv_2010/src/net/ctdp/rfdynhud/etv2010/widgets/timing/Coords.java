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
package net.ctdp.rfdynhud.etv2010.widgets.timing;

import java.awt.geom.Rectangle2D;

import net.ctdp.rfdynhud.etv2010.widgets._util.ETVImages;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;

/**
 * This class keeps values for the different fields' coordinates and sizes.
 * 
 * @author Marvin Froehlich (CTDP)
 */
class Coords
{
    public int gap;
    public int rowHeight;
    
    public int bigPosWidth;
    public int bigPosCenter;
    
    public int mainWidth;
    public int mainFieldWidthA;
    public int mainFieldWidthB;
    
    public int posCenterA;
    public int dataRightA;
    public int dataRightB;
    
    public int rowOffset1;
    public int rowOffset2;
    
    public void update( ETVImages images, int width, int height, Rectangle2D bigPosBounds, Rectangle2D posBounds )
    {
        this.gap = ETVUtils.ITEM_GAP;
        
        this.rowHeight = ( height - 2 * gap ) / 3;
        
        float ldBL;
        int dBL;
        int dBR;
        
        if ( images != null )
        {
            float ldScale = images.getLabeledDataImageScale( rowHeight );
            float dScale = images.getLabeledDataImageScale( rowHeight );
            ldBL = images.getLabeledDataLabelBorderLeft() * ldScale;
            dBL = (int)( images.getDataBorderLeft() * dScale );
            dBR = (int)( images.getDataBorderRight() * dScale );
            
            int bigPosBorderLeft = images.getBigPositionCaptionLeft( rowHeight + gap + rowHeight );
            this.bigPosWidth = images.getBigPositionWidth( rowHeight + gap + rowHeight, bigPosBounds );
            this.bigPosCenter = images.getBigPositionCaptionCenter( rowHeight + gap + rowHeight, bigPosBounds );
            this.mainWidth = width - bigPosWidth + bigPosBorderLeft - gap;
            
            this.posCenterA = images.getLabeledDataCaptionCenterS( ldScale, posBounds );
        }
        else
        {
            ldBL = ETVUtils.TRIANGLE_WIDTH;
            dBL = ETVUtils.TRIANGLE_WIDTH;
            dBR = ETVUtils.TRIANGLE_WIDTH;
            
            int bigPosBorder = ETVUtils.TRIANGLE_WIDTH  + ( ETVUtils.TRIANGLE_WIDTH * ( rowHeight + gap + rowHeight + gap ) ) / ( rowHeight + gap + rowHeight );
            int bigPosNumWidth = (int)Math.ceil( bigPosBounds.getWidth() );
            this.bigPosWidth = bigPosBorder + bigPosNumWidth + bigPosBorder;
            this.bigPosCenter = bigPosBorder + bigPosNumWidth / 2;
            this.mainWidth = width - bigPosWidth + bigPosBorder - gap;
            
            this.posCenterA = (int)Math.ceil( ldBL ) + (int)( Math.ceil( posBounds.getWidth() ) / 2 );
        }
        
        this.rowOffset1 = Math.round( ( ldBL * ( rowHeight + gap ) ) / (float)rowHeight );
        this.rowOffset2 = Math.round( ( ldBL * ( rowHeight + gap ) ) / (float)rowHeight + ( dBL * ( rowHeight + gap ) ) / (float)rowHeight );
        
        this.mainWidth = Math.min( mainWidth, width - bigPosWidth - gap + dBR + (int)( (float)rowHeight + ( dBR * ( rowHeight + gap ) ) / (float)rowHeight ) ) - gap; // TODO: remove the -gap
        
        this.mainFieldWidthA = mainWidth - rowOffset2 + rowOffset1 - dBL;
        this.mainFieldWidthB = mainWidth - rowOffset2;
        
        if ( images != null )
        {
            this.dataRightA = images.getLabeledDataDataRight( mainFieldWidthA, rowHeight );
            this.dataRightB = images.getDataDataRight( mainFieldWidthB, rowHeight );
        }
        else
        {
            this.dataRightA = mainFieldWidthA - ETVUtils.TRIANGLE_WIDTH;
            this.dataRightB = mainFieldWidthB - ETVUtils.TRIANGLE_WIDTH;
        }
    }
}
