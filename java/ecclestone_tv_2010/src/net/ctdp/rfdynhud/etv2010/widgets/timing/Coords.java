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
    
    public void update( ETVImages images, int width, int height, int itemGap, int itemGap2, Rectangle2D bigPosBounds, Rectangle2D posBounds )
    {
        final int gap = itemGap;
        
        this.rowHeight = ( height - 2 * gap ) / 3;
        
        float ldBL;
        int dBL;
        int dBR;
        int dVBR;
        int bpVBL;
        
        if ( images != null )
        {
            float ldScale = images.getLabeledDataImageScale( rowHeight );
            float dScale = images.getLabeledDataImageScale( rowHeight );
            ldBL = images.getLabeledDataLabelBorderLeft() * ldScale;
            dBL = (int)( images.getDataBorderLeft() * dScale );
            dBR = (int)( images.getDataBorderRight() * dScale );
            dVBR = 0;//(int)( images.getDataVirtualProjectionBorderRight() * dScale );
            bpVBL = (int)( images.getBigPositionVirtualProjectionBorderLeft() * dScale );
            
            int bigPosBorderLeft = images.getBigPositionCaptionLeft( rowHeight + gap + rowHeight );
            this.bigPosWidth = images.getBigPositionWidth( rowHeight + gap + rowHeight, bigPosBounds ) - bpVBL;
            this.bigPosCenter = images.getBigPositionCaptionCenter( rowHeight + gap + rowHeight, bigPosBounds );
            this.mainWidth = width - bigPosWidth + bigPosBorderLeft - itemGap2 - dVBR - bpVBL;
            
            this.posCenterA = images.getLabeledDataCaptionCenterS( ldScale, posBounds );
        }
        else
        {
            final int triangWidth = ETVUtils.getTriangleWidth( rowHeight );
            ldBL = triangWidth;
            dBL = triangWidth;
            dBR = triangWidth;
            dVBR = 0;
            bpVBL = 0;
            
            int bigPosBorder = triangWidth  + ( triangWidth * ( rowHeight + gap + rowHeight + gap ) ) / ( rowHeight + gap + rowHeight );
            int bigPosNumWidth = (int)Math.ceil( bigPosBounds.getWidth() );
            this.bigPosWidth = bigPosBorder + bigPosNumWidth + bigPosBorder - bpVBL;
            this.bigPosCenter = bigPosBorder + bigPosNumWidth / 2;
            this.mainWidth = width - bigPosWidth + bigPosBorder - itemGap2 - dVBR - bpVBL;
            
            this.posCenterA = (int)Math.ceil( ldBL ) + (int)( Math.ceil( posBounds.getWidth() ) / 2 );
        }
        
        this.rowOffset1 = Math.round( ( ldBL * ( rowHeight + gap ) ) / (float)rowHeight );
        this.rowOffset2 = Math.round( ( ldBL * ( rowHeight + gap ) ) / (float)rowHeight + ( dBL * ( rowHeight + gap ) ) / (float)rowHeight );
        
        this.mainWidth = Math.min( mainWidth, width - bigPosWidth - itemGap2 + dBR + (int)( (float)rowHeight + ( dBR * ( rowHeight + gap ) ) / (float)rowHeight ) );
        
        this.mainFieldWidthA = mainWidth - rowOffset2 + rowOffset1 - dBL;
        this.mainFieldWidthB = mainWidth - rowOffset2;
        
        if ( images != null )
        {
            this.dataRightA = images.getLabeledDataDataRight( mainFieldWidthA, rowHeight );
            this.dataRightB = images.getDataDataRight( mainFieldWidthB, rowHeight );
        }
        else
        {
            final int triangWidth = ETVUtils.getTriangleWidth( rowHeight );
            this.dataRightA = mainFieldWidthA - triangWidth;
            this.dataRightB = mainFieldWidthB - triangWidth;
        }
    }
}
