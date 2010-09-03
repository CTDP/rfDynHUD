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
    public int gap;
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
    
    public void update( ETVImages images, int width, int height, Rectangle2D posBounds )
    {
        this.gap = ETVUtils.ITEM_GAP;
        
        rowHeight = ( height - 2 * gap ) / 3;
        
        float ldBL;
        int ldBR;
        int dBR;
        int dBL;
        
        if ( images != null )
        {
            float ldScale = images.getLabeledDataImageScale( rowHeight );
            float dScale = images.getLabeledDataImageScale( rowHeight );
            ldBL = images.getLabeledDataLabelBorderLeft() * ldScale;
            ldBR = (int)( images.getLabeledDataDataBorderRight() * ldScale );
            dBR = (int)( images.getDataBorderRight() * dScale );
            //int dBR2 = (int)( images.getDataBorderRight() * dScale / 2 );
            dBL = (int)( images.getDataBorderLeft() * dScale );
            //int dBL2 = (int)( images.getDataBorderLeft() * dScale / 2 );
            
            this.posCenterA = images.getLabeledDataCaptionCenterS( ldScale, posBounds );
            this.dataLeftA = images.getLabeledDataDataLeftS( ldScale, posBounds );
        }
        else
        {
            ldBL = ETVUtils.TRIANGLE_WIDTH;
            ldBR = ETVUtils.TRIANGLE_WIDTH;
            dBR = ETVUtils.TRIANGLE_WIDTH;
            dBL = ETVUtils.TRIANGLE_WIDTH;
            
            this.posCenterA = (int)Math.ceil( ldBL ) + (int)( Math.ceil( posBounds.getWidth() ) / 2 );
            this.dataLeftA = (int)Math.ceil( ldBL ) + (int)Math.ceil( posBounds.getWidth() ) + ETVUtils.TRIANGLE_WIDTH;
        }
        
        this.border12 = Math.min( ldBR, dBL );
        this.border23 = Math.min( dBL, dBR );
        
        float _rowOffset = ( ldBL * ( rowHeight + gap ) ) / (float)rowHeight;
        this.rowOffset1 = Math.round( _rowOffset * 1f );
        this.rowOffset2 = Math.round( _rowOffset * 2f );
        
        int width_ = (int)( width - _rowOffset * 2 );
        this.widthA = (int)( ( width_ - 3 * gap ) * 0.34f );
        int _widthBCD = (int)( ( width_ - 3 * gap ) * 0.22f );
        
        this.offsetB = widthA - ( border12 / 2 ) + gap;
        this.offsetC = widthA + gap + _widthBCD - ( border23 / 2 ) + gap;
        this.offsetD = widthA + gap + _widthBCD + gap + _widthBCD - ( border12 / 2 ) + gap;
        
        this.widthA += ( border12 / 2 );
        this.widthBC = _widthBCD + ( ( border12 + border23 ) / 2 );
        this.widthD = _widthBCD + ( border23 / 2 );
        
        if ( images != null )
        {
            this.dataCenterBC = images.getDataDataCenter( this.widthBC, rowHeight );
            this.dataCenterD = images.getDataDataCenter( this.widthD, rowHeight );
        }
        else
        {
            this.dataCenterBC = this.widthBC / 2;
            this.dataCenterD = this.widthD / 2;
        }
    }
}
