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
package net.ctdp.rfdynhud.widgets.etv2010.fastestlap;

import net.ctdp.rfdynhud.widgets.etv2010._util.ETVImages;
import net.ctdp.rfdynhud.widgets.etv2010._util.ETVUtils;

/**
 * This class keeps values for the different fields' coordinates and sizes.
 * 
 * @author Marvin Froehlich (CTDP)
 */
class Coords
{
    public int rowHeight;
    
    public int mainFieldWidthA;
    public int mainFieldWidthB;
    
    public int mainFieldLeftB;
    
    public int dataLeftA;
    public int dataCenterB;
    
    public final int rowOffset0 = 0;
    public int rowOffset1;
    
    public void update( ETVImages images, int width, int height, int itemGap )
    {
        final int gap = itemGap;
        
        this.rowHeight = ( height - 1 * gap ) / 2;
        
        int dBL;
        int dBR;
        int dVBR;
        int dVBL;
        
        if ( images != null )
        {
            float dScale = images.getDataImageScale( rowHeight );
            dBL = (int)( images.getDataBorderLeft() * dScale );
            dBR = (int)( images.getDataBorderRight() * dScale );
            dVBL = (int)( images.getDataVirtualProjectionBorderLeft() * dScale );
            dVBR = (int)( images.getDataVirtualProjectionBorderRight() * dScale );
            
            this.dataLeftA = images.getDataDataLeftS( dScale );
        }
        else
        {
            final int triangWidth = ETVUtils.getTriangleWidth( rowHeight );
            dBL = triangWidth;
            dBR = triangWidth;
            dVBL = 0;
            dVBR = 0;
            
            this.dataLeftA = triangWidth;
        }
        
        int border12 = Math.min( dBL, dBR );
        
        this.rowOffset1 = Math.round( ( dBL * ( rowHeight + gap ) ) / (float)rowHeight );
        
        this.mainFieldWidthA = ( width - rowOffset1 ) * 65 / 100;
        this.mainFieldLeftB = mainFieldWidthA + dVBR - ( border12 / 1 ) + dVBL - 1 + gap;
        this.mainFieldWidthB = width - rowOffset1 - mainFieldLeftB;
        
        if ( images != null )
        {
            float dScale = images.getDataImageScale( rowHeight );
            
            this.dataCenterB = width - mainFieldWidthB + images.getDataDataCenterS( dScale, mainFieldWidthB );
        }
        else
        {
            final int triangWidth = ETVUtils.getTriangleWidth( rowHeight );
            
            this.dataCenterB = width - mainFieldWidthB + triangWidth + ( mainFieldWidthB - triangWidth - triangWidth ) / 2;
        }
    }
}
