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
package net.ctdp.rfdynhud.widgets.etv2010.fastestlap;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.DelayProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.widgets.etv2010._base.ETVTimingWidgetBase;
import net.ctdp.rfdynhud.widgets.etv2010._util.ETVImages.BGType;
import net.ctdp.rfdynhud.widgets.etv2010._util.ETVUtils;
import net.ctdp.rfdynhud.widgets.etv2010._util.ETVWidgetSet;

/**
 * The {@link ETVFastestLapWidget} displays the current lap time.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ETVFastestLapWidget extends ETVTimingWidgetBase
{
    private final DelayProperty visibleTime = new DelayProperty( "visibleTime", DelayProperty.DisplayUnits.SECONDS, 20 );
    
    private long visibleEnd = Long.MIN_VALUE;
    
    private DrawnString drivernameString = null;
    private DrawnString teamnameString = null;
    private DrawnString captionString = null;
    private DrawnString laptimeString = null;
    
    private static final FloatValue fastestLaptime = new FloatValue();
    
    public ETVFastestLapWidget()
    {
        super( ETVWidgetSet.INSTANCE, ETVWidgetSet.WIDGET_PACKAGE, 26.75f, 5.6f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( visibleTime, "Time in seconds to keep the Widget visible." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( visibleTime ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void addVisibilityPropertiesToContainer( PropertiesContainer propsCont, boolean forceAll )
    {
        super.addVisibilityPropertiesToContainer( propsCont, forceAll );
        
        propsCont.addProperty( visibleTime );
    }
    
    private void updateFastestLap( ScoringInfo scoringInfo )
    {
        Laptime lt = scoringInfo.getFastestLaptime();
        
        if ( ( lt == null ) || !lt.isFinished() )
            fastestLaptime.update( -1f );
        else
            fastestLaptime.update( lt.getLapTime() );
    }
    
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, boolean isEditorMode )
    {
        super.onSessionStarted( sessionType, gameData, isEditorMode );
        
        fastestLaptime.update( -1f );
        visibleEnd = Long.MIN_VALUE;
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        updateFastestLap( gameData.getScoringInfo() );
        visibleEnd = Long.MIN_VALUE;
        
        forceCompleteRedraw( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean updateVisibility( LiveGameData gameData, boolean isEditorMode )
    {
        /*Boolean result = */super.updateVisibility( gameData, isEditorMode );
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        updateFastestLap( gameData.getScoringInfo() );
        
        if ( fastestLaptime.hasChanged() && fastestLaptime.isValid() )
        {
            forceCompleteRedraw( true );
            
            visibleEnd = scoringInfo.getSessionNanos() + visibleTime.getDelayNanos();
            
            return ( true );
        }
        
        return ( ( scoringInfo.getSessionNanos() < visibleEnd ) && fastestLaptime.isValid() );
    }
    
    private final Coords coords = new Coords();
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        Rectangle2D stringBounds = TextureImage2D.getStringBounds( "00y", getFontProperty() );
        int gap = itemGap.getIntValue();
        coords.update( getImages(), width, height, gap );
        
        int vMiddle = ETVUtils.getLabeledDataVMiddle( coords.rowHeight, stringBounds );
        
        drivernameString = dsf.newDrawnString( "drivernameString", coords.rowOffset1 + coords.dataLeftA, 0 * ( coords.rowHeight + gap ) + vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        teamnameString = dsf.newDrawnString( "teamnameString", coords.rowOffset0 + coords.dataLeftA, 1 * ( coords.rowHeight + gap ) + vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        captionString = dsf.newDrawnString( "captionString", coords.rowOffset1 + coords.dataCenterB, 0 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        laptimeString = dsf.newDrawnString( "laptimeString", coords.rowOffset0 + coords.dataCenterB, 1 * ( coords.rowHeight + gap ) + vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), dataColorFastest.getColor() );
        
        forceCompleteRedraw( true );
    }
    
    private void drawStructure( TextureImage2D texture, int offsetX, int offsetY )
    {
        final boolean useImages = this.useImages.getBooleanValue();
        final int gap = itemGap.getIntValue();
        
        // driver name field
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.rowOffset1, offsetY + 0 * ( coords.rowHeight + gap ), coords.mainFieldWidthA, coords.rowHeight, getImages(), BGType.NEUTRAL, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.rowOffset1, offsetY + 0 * ( coords.rowHeight + gap ), coords.mainFieldWidthA, coords.rowHeight, dataBackgroundColor.getColor(), texture, false );
        
        // team name field
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.rowOffset0, offsetY + 1 * ( coords.rowHeight + gap ), coords.mainFieldWidthA, coords.rowHeight, getImages(), BGType.CAPTION, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.rowOffset0, offsetY + 1 * ( coords.rowHeight + gap ), coords.mainFieldWidthA, coords.rowHeight, captionBackgroundColor.getColor(), texture, false );
        
        // "Fastest Lap" caption field
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.mainFieldLeftB + coords.rowOffset1, offsetY + 0 * ( coords.rowHeight + gap ), coords.mainFieldWidthB, coords.rowHeight, getImages(), BGType.CAPTION, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.mainFieldLeftB + coords.rowOffset1, offsetY + 0 * ( coords.rowHeight + gap ), coords.mainFieldWidthB, coords.rowHeight, captionBackgroundColor.getColor(), texture, false );
        
        // lap time field
        if ( useImages )
            ETVUtils.drawDataBackgroundI( offsetX + coords.mainFieldLeftB + coords.rowOffset0, offsetY + 1 * ( coords.rowHeight + gap ), coords.mainFieldWidthB, coords.rowHeight, getImages(), BGType.FASTEST, texture, false );
        else
            ETVUtils.drawDataBackground( offsetX + coords.mainFieldLeftB + coords.rowOffset0, offsetY + 1 * ( coords.rowHeight + gap ), coords.mainFieldWidthB, coords.rowHeight, dataBackgroundColorFastest.getColor(), texture, false );
    }
    
    private static String getShorterTeamName( VehicleInfo vi )
    {
        String tn1 = vi.getTeamNameCleaned();
        String tn2 = vi.getFullTeamName();
        
        if ( tn1.length() < tn2.length() )
            return ( tn1 );
        
        return ( tn2 );
    }
    
    @Override
    public void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( needsCompleteRedraw )
        {
            drawStructure( texture, offsetX, offsetY );
            
            VehicleScoringInfo vsi = gameData.getScoringInfo().getFastestLapVSI();
            
            String teamName;
            if ( vsi.isPlayer() )
            {
                teamName = getShorterTeamName( gameData.getVehicleInfo() );
            }
            else if ( vsi.getVehicleInfo() != null )
            {
                teamName = getShorterTeamName( vsi.getVehicleInfo() );
            }
            else
            {
                teamName = vsi.getVehicleClass();
            }
            if ( teamName == null )
            {
                teamName = vsi.getVehicleClass();
            }
            
            drivernameString.draw( offsetX, offsetY, vsi.getDriverNameShort( getShowNamesInAllUppercase() ), texture, false );
            
            texture.getTextureCanvas().pushClip( offsetX + coords.rowOffset0 + coords.dataLeftA, offsetY + 1 * ( coords.rowHeight + itemGap.getIntValue() ), coords.dataWidthA, coords.rowHeight, true );
            teamnameString.draw( offsetX, offsetY, teamName, texture, false );
            texture.getTextureCanvas().popClip();
            captionString.draw( offsetX, offsetY, Loc.caption_fastestLap, texture, false );;
            laptimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsLaptimeString( fastestLaptime.getValue() ), texture, false );;
        }
    }
}
