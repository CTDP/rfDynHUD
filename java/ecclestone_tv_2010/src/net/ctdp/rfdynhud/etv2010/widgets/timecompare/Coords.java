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
package net.ctdp.rfdynhud.etv2010.widgets.timecompare;

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
    
    public int posCenterA;
    public int dataLeftA;
    public int dataCenterBC;
    public int dataCenterD;
    public int border12;
    public int border23;
    
    public int rowOffset1;
    public int rowOffset2;
    public int widthA;
    public int widthBC;
    public int widthD;
    public int offsetB;
    public int offsetC;
    public int offsetD;
    
    public void update( ETVImages images, int width, int height, int itemGap, Rectangle2D posBounds )
    {
        final int gap = itemGap;
        
        rowHeight = ( height - 2 * gap ) / 3;
        
        //float ldVBL;
        float ldBL;
        int ldBR;
        int ldVBR;
        int dVBL;
        int dBL;
        int dBR;
        int dVBR;
        
        if ( images != null )
        {
            float ldScale = images.getLabeledDataImageScale( rowHeight );
            float dScale = images.getLabeledDataImageScale( rowHeight );
            //ldVBL = images.getLabeledDataVirtualProjectionBorderLeft() * ldScale;
            ldBL = images.getLabeledDataLabelBorderLeft() * ldScale;
            ldBR = (int)( images.getLabeledDataDataBorderRight() * ldScale );
            ldVBR = (int)( images.getLabeledDataVirtualProjectionBorderRight() * ldScale );
            dVBL = (int)( images.getDataVirtualProjectionBorderLeft() * dScale );
            dBL = (int)( images.getDataBorderLeft() * dScale );
            dBR = (int)( images.getDataBorderRight() * dScale );
            dVBR = (int)( images.getDataVirtualProjectionBorderRight() * dScale );
            
            this.posCenterA = images.getLabeledDataCaptionCenterS( ldScale, posBounds );
            this.dataLeftA = images.getLabeledDataDataLeftS( ldScale, posBounds );
        }
        else
        {
            final int triangWidth = ETVUtils.getTriangleWidth( rowHeight );
            //ldVBL = 0;
            ldBL = triangWidth;
            ldBR = triangWidth;
            ldVBR = 0;
            dVBL = 0;
            dBR = triangWidth;
            dBL = triangWidth;
            dVBR = 0;
            
            this.posCenterA = (int)Math.ceil( ldBL ) + (int)( Math.ceil( posBounds.getWidth() ) / 2 );
            this.dataLeftA = (int)Math.ceil( ldBL ) + (int)Math.ceil( posBounds.getWidth() ) + triangWidth;
        }
        
        this.border12 = Math.min( ldBR, dBL );
        this.border23 = Math.min( dBL, dBR );
        
        float _rowOffset = ( ldBL * ( rowHeight + gap ) ) / (float)rowHeight;
        this.rowOffset1 = Math.round( _rowOffset * 1f );
        this.rowOffset2 = Math.round( _rowOffset * 2f );
        
        float width_ = width - _rowOffset * 2f;
        this.widthA = (int)Math.ceil( ( width_ - ( (int)ldVBR + gap + dVBL ) - 2 * ( dVBL + gap + dVBR ) - dVBR ) * 0.34f );
        float _widthBCD = ( ( width_ - ( ldVBR + gap + dVBL ) - 2 * ( dVBL + gap + dVBR ) - dVBR ) * 0.22f );
        
        this.offsetB = widthA - ( border12 / 2 ) + (int)ldVBR + gap + dVBL;
        this.offsetC = widthA + (int)ldVBR + gap + dVBL + (int)_widthBCD - ( border23 / 2 ) + dVBL + gap + dVBR;
        this.offsetD = widthA + (int)ldVBR + gap + dVBL + (int)_widthBCD + dVBL + gap + dVBR + (int)_widthBCD - ( border23 / 2 ) + dVBL + gap + dVBR;
        
        this.widthA += ( border12 / 2 );
        this.widthBC = (int)Math.floor( _widthBCD ) + ( ( border12 + border23 ) / 2 );
        //this.widthD = (int)Math.floor( _widthBCD ) + ( border23 / 2 ) + dVBR;
        this.widthD = (int)Math.floor( width_ - this.offsetD );
        
        if ( images != null )
        {
            this.dataCenterBC = dVBL + images.getDataDataCenter( this.widthBC, rowHeight );
            this.dataCenterD = dVBL + images.getDataDataCenter( this.widthD, rowHeight );
        }
        else
        {
            this.dataCenterBC = this.widthBC / 2;
            this.dataCenterD = this.widthD / 2;
        }
    }
}
