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
package net.ctdp.rfdynhud.render;

public class BorderMeasures
{
    private int leftWidth;
    private int rightWidth;
    private int topHeight;
    private int bottomHeight;
    
    private int opaqueLeftWidth;
    private int opaqueRightWidth;
    private int opaqueTopHeight;
    private int opaqueBottomHeight;
    
    private int innerLeftWidth;
    private int innerRightWidth;
    private int innerTopHeight;
    private int innerBottomHeight;
    
    private int llUpperHeight = 0;
    private int llRightWidth = 0;
    private int lrLeftWidth = 0;
    private int lrUpperHeight = 0;
    private int urLowerHeight = 0;
    private int urLeftWidth = 0;
    private int ulRightWidth = 0;
    private int ulLowerHeight = 0;
    
    public void setBottomHeight( int bh )
    {
        this.bottomHeight = bh;
    }
    
    public final int getBottomHeight()
    {
        return ( bottomHeight );
    }
    
    public void setRightWidth( int rw )
    {
        this.rightWidth = rw;
    }
    
    public final int getRightWidth()
    {
        return ( rightWidth );
    }
    
    public void setTopHeight( int th )
    {
        this.topHeight = th;
    }
    
    public final int getTopHeight()
    {
        return ( topHeight );
    }
    
    public void setLeftWidth( int lw )
    {
        this.leftWidth = lw;
    }
    
    public final int getLeftWidth()
    {
        return ( leftWidth );
    }
    
    public void setOpaqueBottomHeight( int bh )
    {
        this.opaqueBottomHeight = bh;
    }
    
    public final int getOpaqueBottomHeight()
    {
        return ( opaqueBottomHeight );
    }
    
    public void setOpaqueRightWidth( int rw )
    {
        this.opaqueRightWidth = rw;
    }
    
    public final int getOpaqueRightWidth()
    {
        return ( opaqueRightWidth );
    }
    
    public void setOpaqueTopHeight( int th )
    {
        this.opaqueTopHeight = th;
    }
    
    public final int getOpaqueTopHeight()
    {
        return ( opaqueTopHeight );
    }
    
    public void setOpaqueLeftWidth( int lw )
    {
        this.opaqueLeftWidth = lw;
    }
    
    public final int getOpaqueLeftWidth()
    {
        return ( opaqueLeftWidth );
    }
    
    public void setInnerBottomHeight( int bh )
    {
        this.innerBottomHeight = bh;
    }
    
    public final int getInnerBottomHeight()
    {
        return ( innerBottomHeight );
    }
    
    public void setInnerRightWidth( int rw )
    {
        this.innerRightWidth = rw;
    }
    
    public final int getInnerRightWidth()
    {
        return ( innerRightWidth );
    }
    
    public void setInnerTopHeight( int th )
    {
        this.innerTopHeight = th;
    }
    
    public final int getInnerTopHeight()
    {
        return ( innerTopHeight );
    }
    
    public void setInnerLeftWidth( int lw )
    {
        this.innerLeftWidth = lw;
    }
    
    public final int getInnerLeftWidth()
    {
        return ( innerLeftWidth );
    }
    
    public void setLLupperHeight( int value )
    {
        this.llUpperHeight = value;
    }
    
    public final int getLLupperHeight()
    {
        return ( llUpperHeight );
    }
    
    public void setLLrightWidth( int value )
    {
        this.llRightWidth = value;
    }
    
    public final int getLLrightWidth()
    {
        return ( llRightWidth );
    }
    
    public void setLRleftWidth( int value )
    {
        this.lrLeftWidth = value;
    }
    
    public final int getLRleftWidth()
    {
        return ( lrLeftWidth );
    }
    
    public void setLRupperHeight( int value )
    {
        this.lrUpperHeight = value;
    }
    
    public final int getLRupperHeight()
    {
        return ( lrUpperHeight );
    }
    
    public void setURlowerHeight( int value )
    {
        this.urLowerHeight = value;
    }
    
    public final int getURlowerHeight()
    {
        return ( urLowerHeight );
    }
    
    public void setURleftWidth( int value )
    {
        this.urLeftWidth = value;
    }
    
    public final int getURleftWidth()
    {
        return ( urLeftWidth );
    }
    
    public void setULrightWidth( int value )
    {
        this.ulRightWidth = value;
    }
    
    public final int getULrightWidth()
    {
        return ( ulRightWidth );
    }
    
    public void setULlowerHeight( int value )
    {
        this.ulLowerHeight = value;
    }
    
    public final int getULlowerHeight()
    {
        return ( ulLowerHeight );
    }
    
    public BorderMeasures()
    {
    }
}
