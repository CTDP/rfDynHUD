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
package net.ctdp.rfdynhud.widgets.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ModInfo;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.Track;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.BorderWrapper;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.MapTools;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._util.LabelPositioning;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * The {@link MapWidget} renders a map overview of the current track.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class MapWidget extends Widget
{
    private Track track = null;
    private float scale = 1f;
    private final IntProperty baseItemRadius = new IntProperty( this, "itemRadius", "radius", 9, 1, 100, false )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            super.onValueChanged( oldValue, newValue );
            
            if ( getConfiguration() != null )
                updateItemRadius();
            
            forceAndSetDirty( false );
        }
    };
    private int itemRadius = baseItemRadius.getIntValue();
    private void updateItemRadius()
    {
        itemRadius = Math.round( baseItemRadius.getIntValue() * getConfiguration().getGameResolution().getViewportHeight() / 960f );
    }
    private int itemBlackBorderWidth = 2;
    
    private boolean needsBGClear = false;
    
    private final BooleanProperty rotationEnabled = new BooleanProperty( this, "rotationEnabled", false );
    
    private final ColorProperty roadColorSec1 = new ColorProperty( this, "roadColorSec1", "colorSec1", "#000000" );
    private final ColorProperty roadBoundaryColorSec1 = new ColorProperty( this, "roadBoundaryColorSec1", "boundaryColorSec1", "#FFFFFF" );
    private final ColorProperty roadColorSec2 = new ColorProperty( this, "roadColorSec2", "colorSec2", "#000000" );
    private final ColorProperty roadBoundaryColorSec2 = new ColorProperty( this, "roadBoundaryColorSec2", "boundaryColorSec2", "#FFFFFF" );
    private final ColorProperty roadColorSec3 = new ColorProperty( this, "roadColorSec3", "colorSec3", "#000000" );
    private final ColorProperty roadBoundaryColorSec3 = new ColorProperty( this, "roadBoundaryColorSec3", "boundaryColorSec3", "#FFFFFF" );
    private final ColorProperty pitlaneColor = new ColorProperty( this, "pitlaneColor", "pitlaneColor", "#FFFF00" );
    private final IntProperty roadWidth = new IntProperty( this, "roadWidth", "width", 4, 2, 20, false )
    {
        @Override
        protected int fixValue( int value )
        {
            value = super.fixValue( value );
            
            return ( Math.round( value / 2f ) * 2 );
        }
    };
    private float pitlaneRoadWidth = 2f;
    
    private final ColorProperty markColorNormal = new ColorProperty( this, "markColorNormal", "colorNormal", StandardWidgetSet.POSITION_ITEM_COLOR_NORMAL );
    private final ColorProperty markColorLeader = new ColorProperty( this, "markColorLeader", "colorLeader", StandardWidgetSet.POSITION_ITEM_COLOR_LEADER );
    private final ColorProperty markColorMe = new ColorProperty( this, "markColorMe", "colorMe", StandardWidgetSet.POSITION_ITEM_COLOR_ME );
    private final BooleanProperty useMyColorForMe1st = new BooleanProperty( this, "useMyColorForMe1st", false );
    private final ColorProperty markColorNextInFront = new ColorProperty( this, "markColorNextInFront", "colorNextInFront", StandardWidgetSet.POSITION_ITEM_COLOR_NEXT_IN_FRONT );
    private final ColorProperty markColorNextBehind = new ColorProperty( this, "markColorNextBehind", "colorNextBehind", StandardWidgetSet.POSITION_ITEM_COLOR_NEXT_BEHIND );
    
    private final BooleanProperty displayPositionNumbers = new BooleanProperty( this, "displayPosNumbers", true );
    
    private final BooleanProperty displayNameLabels = new BooleanProperty( this, "displayNameLabels", false );
    private final EnumProperty<LabelPositioning> nameLabelPos = new EnumProperty<LabelPositioning>( this, "nameLabelPos", LabelPositioning.BELOW_RIGHT );
    private final FontProperty nameLabelFont = new FontProperty( this, "nameLabelFont", StandardWidgetSet.POSITION_ITEM_FONT_NAME );
    private final ColorProperty nameLabelFontColor = new ColorProperty( this, "nameLabelFontColor", StandardWidgetSet.POSITION_ITEM_COLOR_NORMAL );
    
    private int maxDisplayedVehicles = -1;
    
    private static final int ANTI_ALIAS_RADIUS_OFFSET = 1;
    
    private TransformableTexture[] subTextures = null;
    private VehicleScoringInfo[] vsis = null;
    private int[] itemStates = null;
    private int numVehicles = 0;
    
    private final Point2D.Float position = new Point2D.Float();
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPropertyChanged( Property property, Object oldValue, Object newValue )
    {
        super.onPropertyChanged( property, oldValue, newValue );
        
        needsBGClear = rotationEnabled.getBooleanValue();
        
        if ( itemStates != null )
        {
            for ( int i = 0; i < itemStates.length; i++ )
                itemStates[i] = 0;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedColorValue( String name )
    {
        String result = super.getDefaultNamedColorValue( name );
        
        if ( result != null )
            return ( result );
        
        result = StandardWidgetSet.getDefaultNamedColorValue( name );
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        String result = super.getDefaultNamedFontValue( name );
        
        if ( result != null )
            return ( result );
        
        result = StandardWidgetSet.getDefaultNamedFontValue( name );
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMasterCanvas( boolean isEditorMode )
    {
        return ( !rotationEnabled.getBooleanValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, isEditorMode );
        
        updateItemRadius();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getNeededData()
    {
        return ( Widget.NEEDED_DATA_SCORING );
    }
    
    private final boolean getUseClassScoring()
    {
        return ( getConfiguration().getUseClassScoring() );
    }
    
    private void initTrack( LiveGameData gameData )
    {
        File sceneFolder = gameData.getTrackInfo().getTrackFolder();
        if ( sceneFolder == null )
        {
            if ( gameData.getTrackInfo().getSceneFile() == null )
                Logger.log( "Warning: Unable to read last used track file from PLR file." );
            else
                Logger.log( "Warning: Couldn't read track data from file \"" + gameData.getTrackInfo().getSceneFile() + "\"." );
            
            track = null;
        }
        else
        {
            track = gameData.getTrackInfo().getTrack();
        }
        //track = Track.parseTrackFromAIW( new File( GameFileSystem.INSTANCE.getLocationsFolder(), "4r2009FSone\\Northamptonshire\\NAS_BritishGP\\NAS_BritishGP.AIW" ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTrackChanged( String trackname, LiveGameData gameData, boolean isEditorMode )
    {
        initTrack( gameData );
    }
    
    private void initMaxDisplayedVehicles( boolean isEditorMode, ModInfo modInfo )
    {
        if ( isEditorMode )
            this.maxDisplayedVehicles = 22 + 1;
        else
            this.maxDisplayedVehicles = modInfo.getMaxOpponents() + 1;
        
        this.maxDisplayedVehicles = Math.max( 4, Math.min( maxDisplayedVehicles, 32 ) );
    }
    
    private void updateVSIs( LiveGameData gameData, boolean isEditorMode )
    {
        initMaxDisplayedVehicles( isEditorMode, gameData.getModInfo() );
        
        if ( ( vsis == null ) || ( vsis.length < maxDisplayedVehicles ) )
        {
            vsis = new VehicleScoringInfo[ maxDisplayedVehicles ];
            
            if ( itemStates == null )
            {
                itemStates = new int[ maxDisplayedVehicles ];
            }
            else
            {
                int[] tmpItemStates = new int[ maxDisplayedVehicles ];
                
                System.arraycopy( itemStates, 0, tmpItemStates, 0, itemStates.length );
                itemStates = tmpItemStates;
            }
            
            for ( int i = 0; i < itemStates.length; i++ )
                itemStates[i] = 0;
        }
        
        numVehicles = MapTools.getDisplayedVSIsForMap( gameData.getScoringInfo(), gameData.getScoringInfo().getViewedVehicleScoringInfo(), getUseClassScoring(), true, vsis );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        updateVSIs( gameData, isEditorMode );
    }
    
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight )
    {
        updateItemRadius();
        initMaxDisplayedVehicles( isEditorMode, gameData.getModInfo() );
        
        int numTextures = maxDisplayedVehicles;
        int subTexOff = 0;
        if ( !hasMasterCanvas( isEditorMode ) )
        {
            numTextures++;
            subTexOff++;
        }
        
        if ( ( subTextures == null ) || ( subTextures.length != numTextures ) )
        {
            subTextures = new TransformableTexture[ numTextures ];
        }
        
        if ( !hasMasterCanvas( isEditorMode ) && ( ( subTextures[0] == null ) || ( subTextures[0].getWidth() != widgetInnerWidth ) || ( subTextures[0].getHeight() != widgetInnerHeight ) ) )
        {
            subTextures[0] = TransformableTexture.getOrCreate( getBorder().getWidgetWidth( widgetInnerWidth ), getBorder().getWidgetHeight( widgetInnerHeight ), isEditorMode, subTextures[0], isEditorMode );
        }
        
        if ( subTextures[subTexOff] == null )
            subTextures[subTexOff] = new TransformableTexture( 1, 1, isEditorMode, false );
        
        java.awt.Dimension size = StandardWidgetSet.getPositionItemSize( itemRadius, displayNameLabels.getBooleanValue() ? nameLabelPos.getEnumValue() : null, nameLabelFont.getFont(), nameLabelFont.isAntiAliased() );
        int w = size.width;
        int h = size.height;
        
        if ( ( subTextures[subTextures.length - 1] == null ) || ( subTextures[subTextures.length - 1].getWidth() != w ) || ( subTextures[subTextures.length - 1].getHeight() != h ) )
        {
            for ( int i = 0; i < maxDisplayedVehicles; i++ )
            {
                subTextures[subTexOff + i] = TransformableTexture.getOrCreate( w, h, isEditorMode, subTextures[subTexOff + i], isEditorMode );
                subTextures[subTexOff + i].setVisible( false );
            }
        }
        
        return ( subTextures );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        initMaxDisplayedVehicles( isEditorMode, gameData.getModInfo() );
        
        if ( isEditorMode )
            updateVSIs( gameData, isEditorMode );
        
        initTrack( gameData );
    }
    
    private void drawTrack( Track track, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        Texture2DCanvas tc = texture.getTextureCanvas();
        tc.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        
        if ( track == null )
        {
            tc.setColor( roadColorSec1.getColor() );
            
            tc.drawArc( offsetX + 3, offsetY + 3, width - 6, height - 6, 0, 360 );
            
            tc.setColor( Color.RED );
            tc.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
            Rectangle2D bounds = tc.getFontMetrics().getStringBounds( "Ag", tc );
            tc.drawString( "Couldn't read track data.", offsetX + 3, offsetY + (int)bounds.getHeight() );
            tc.drawString( "Please see the log for more info.", offsetX + 3, offsetY + (int)bounds.getHeight() * 2  );
        }
        else if ( track.getNumWaypoints( false ) > 0 )
        {
            int off2 = ( isFontAntiAliased() ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
            int dia = off2 + itemRadius + itemRadius + off2;
            
            if ( rotationEnabled.getBooleanValue() )
            {
                float xExtend = track.getXExtend( 1.0f );
                float yExtend = track.getZExtend( 1.0f );
                float dia2 = (float)Math.sqrt( xExtend * xExtend + yExtend * yExtend );
                
                int wh = Math.min( width - dia + itemRadius + itemRadius - subTextures[1].getWidth(), height - dia );
                scale = wh / dia2;
                //scale = track.getScale( (int)( wh * 0.9f ), (int)( wh * 0.9f ) );
            }
            else
            {
                int w = width - dia - itemRadius - itemRadius - subTextures[0].getWidth();
                int h = height - dia;
                scale = track.getScale( w, h );
            }
            
            Point p0 = new Point();
            Point p1 = new Point();
            
            int x0 = offsetX + off2 + itemRadius + ( ( width - dia - track.getXExtend( scale ) ) / 2 );
            int y0 = offsetY + off2 + itemRadius + ( ( height - dia - track.getZExtend( scale ) ) / 2 );
            
            if ( rotationEnabled.getBooleanValue() )
            {
                subTextures[0].setTranslation( -getBorder().getInnerLeftWidthWOPadding(), -getBorder().getInnerTopHeightWOPadding() );
                
                //subTextures[0].setRotationCenter( x0 + track.getXExtend( scale ) / 2, y0 + track.getZExtend( scale ) / 2 );
                subTextures[0].setRotationCenter( subTextures[0].getWidth() / 2, subTextures[0].getHeight() / 2 );
            }
            
            Stroke oldStroke = tc.getStroke();
            
            int n = track.getNumWaypoints( false );
            
            int[] xPoints = new int[ n ];
            int[] yPoints = new int[ n ];
            
            track.getWaypointPosition( false, 0, scale, p0 );
            xPoints[0] = x0 + p0.x;
            yPoints[0] = y0 + p0.y;
            byte oldSec = track.getWaypointSector( false, 0 );
            
            int j = 1;
            for ( int i = 1; i < n; i++ )
            {
                track.getWaypointPosition( false, i, scale, p1 );
                byte sec = track.getWaypointSector( false, i );
                
                if ( sec != oldSec )
                {
                    if ( oldSec == 1 )
                        tc.setColor( roadBoundaryColorSec1.getColor() );
                    else if ( oldSec == 2 )
                        tc.setColor( roadBoundaryColorSec2.getColor() );
                    else if ( oldSec == 3 )
                        tc.setColor( roadBoundaryColorSec3.getColor() );
                    tc.setStroke( new BasicStroke( roadWidth.getIntValue() ) );
                    tc.setAntialiazingEnabled( true );
                    
                    tc.drawPolyline( xPoints, yPoints, j );
                    
                    if ( oldSec == 1 )
                        tc.setColor( roadColorSec1.getColor() );
                    else if ( oldSec == 2 )
                        tc.setColor( roadColorSec2.getColor() );
                    else if ( oldSec == 3 )
                        tc.setColor( roadColorSec3.getColor() );
                    tc.setStroke( new BasicStroke( roadWidth.getIntValue() - 1.5f ) );
                    tc.setAntialiazingEnabled( true );
                    
                    tc.drawPolyline( xPoints, yPoints, j );
                    
                    xPoints[0] = xPoints[j - 1];
                    yPoints[0] = yPoints[j - 1];
                    
                    j = 1;
                }
                
                oldSec = sec;
                
                double dsq = ( p0.getX() - p1.getX() ) * ( p0.getX() - p1.getX() ) + ( p0.getY() - p1.getY() ) * ( p0.getY() - p1.getY() );
                
                if ( ( dsq >= 16.0 ) || ( i == n - 1 ) )
                {
                    xPoints[j] = x0 + p1.x;
                    yPoints[j] = y0 + p1.y;
                    
                    j++;
                    
                    Point p = p1;
                    p1 = p0;
                    p0 = p;
                }
            }
            
            if ( j > 0 )
            {
                track.getWaypointPosition( false, 0, scale, p0 );
                xPoints[j] = x0 + p0.x;
                yPoints[j] = y0 + p0.y;
                j++;
                
                if ( oldSec == 1 )
                    tc.setColor( roadBoundaryColorSec1.getColor() );
                else if ( oldSec == 2 )
                    tc.setColor( roadBoundaryColorSec2.getColor() );
                else if ( oldSec == 3 )
                    tc.setColor( roadBoundaryColorSec3.getColor() );
                tc.setStroke( new BasicStroke( roadWidth.getFloatValue() ) );
                tc.setAntialiazingEnabled( true );
                
                tc.drawPolyline( xPoints, yPoints, j );
                
                if ( oldSec == 1 )
                    tc.setColor( roadColorSec1.getColor() );
                else if ( oldSec == 2 )
                    tc.setColor( roadColorSec2.getColor() );
                else if ( oldSec == 3 )
                    tc.setColor( roadColorSec3.getColor() );
                tc.setStroke( new BasicStroke( roadWidth.getIntValue() - 1.5f ) );
                tc.setAntialiazingEnabled( true );
                
                tc.drawPolyline( xPoints, yPoints, j );
                
                j = 0;
            }
            
            tc.setColor( pitlaneColor.getColor() );
            tc.setStroke( new BasicStroke( pitlaneRoadWidth ) );
            tc.setAntialiazingEnabled( true );
            
            n = track.getNumWaypoints( true );
            
            xPoints = new int[ n + 1 ];
            yPoints = new int[ n + 1 ];
            
            int k = 0;
            track.getWaypointPosition( true, k, scale, p0 );
            xPoints[0] = x0 + p0.x;
            yPoints[0] = y0 + p0.y;
            
            j = 1;
            for ( int i = k + 1; i < n; i++ )
            {
                track.getWaypointPosition( true, i, scale, p1 );
                
                double dsq = ( p0.getX() - p1.getX() ) * ( p0.getX() - p1.getX() ) + ( p0.getY() - p1.getY() ) * ( p0.getY() - p1.getY() );
                
                if ( dsq > 50 * 50 )
                {
                    if ( k < i - 1 )
                    {
                        track.getWaypointPosition( true, i - 1, scale, p0 );
                        
                        xPoints[j - 1] = x0 + p0.x;
                        yPoints[j - 1] = y0 + p0.y;
                    }
                    
                    tc.drawPolyline( xPoints, yPoints, j );
                    
                    xPoints[0] = x0 + p1.x;
                    yPoints[0] = y0 + p1.y;
                    
                    j = 1;
                    
                    Point p = p1;
                    p1 = p0;
                    p0 = p;
                }
                else if ( ( dsq >= 16.0 ) || ( i == n - 1 ) )
                {
                    xPoints[j] = x0 + p1.x;
                    yPoints[j] = y0 + p1.y;
                    
                    j++;
                    
                    Point p = p1;
                    p1 = p0;
                    p0 = p;
                    
                    k = i;
                }
            }
            
            track.getWaypointPosition( true, 0, scale, p0 );
            track.getWaypointPosition( true, n - 1, scale, p1 );
            double dsq = ( p0.getX() - p1.getX() ) * ( p0.getX() - p1.getX() ) + ( p0.getY() - p1.getY() ) * ( p0.getY() - p1.getY() );
            if ( dsq <= 16.0 )
            {
                xPoints[j] = x0 + p0.x;
                yPoints[j] = y0 + p0.y;
                j++;
            }
            
            tc.drawPolyline( xPoints, yPoints, j );
            
            tc.setStroke( oldStroke );
        }
        else
        {
            scale = 1f;
        }
    }
    
    @Override
    protected void drawBorder( boolean isEditorMode, BorderWrapper border, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( hasMasterCanvas( isEditorMode ) )
            super.drawBorder( isEditorMode, border, texture, offsetX, offsetY, width, height );
        else
            super.drawBorder( isEditorMode, border, subTextures[0].getTexture(), 0, 0, width, height );
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        if ( hasMasterCanvas( isEditorMode ) )
        {
            super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
            
            drawTrack( track, texture, offsetX, offsetY, width, height );
        }
        else
        {
            if ( isEditorMode && needsBGClear && isRoot )
            {
                texture.clear( offsetX, offsetY, width, height, false, null );
            }
            
            offsetX = getBorder().getInnerLeftWidthWOPadding();
            offsetY = getBorder().getInnerTopHeightWOPadding();
            
            super.drawBackground( gameData, isEditorMode, subTextures[0].getTexture(), offsetX, offsetY, width, height, isRoot );
            
            drawTrack( track, subTextures[0].getTexture(), offsetX, offsetY, width, height );
        }
    }
    
    private final AffineTransform at = new AffineTransform();
    
    @Override
    public void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( isEditorMode && needsBGClear )
        {
            int offX_ = offsetX - getBorder().getInnerLeftWidthWOPadding();
            int offY_ = offsetY - getBorder().getInnerTopHeightWOPadding();
            int w_ = getBorder().getWidgetWidth( width );
            int h_ = getBorder().getWidgetWidth( height );
            
            texture.getTextureCanvas().pushClip( offX_, offY_, w_, h_ );
            
            try
            {
                clearRegion( texture, offX_, offY_, w_, h_ );
            }
            finally
            {
                texture.getTextureCanvas().popClip();
            }
            
            needsBGClear = false;
        }
        
        if ( track != null )
        {
            final ScoringInfo scoringInfo = gameData.getScoringInfo();
            final VehicleScoringInfo viewedVSI = scoringInfo.getViewedVehicleScoringInfo();
            final boolean useClassScoring = getUseClassScoring();
            
            int off2 = ( isFontAntiAliased() ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
            int x0 = off2 - itemRadius + ( ( width - track.getXExtend( scale ) ) / 2 );
            int y0 = off2 - itemRadius + ( ( height - track.getZExtend( scale ) ) / 2 );
            
            short ownPlace = scoringInfo.getOwnPlace( useClassScoring );
            
            final Font font = getFont();
            final boolean posNumberFontAntiAliased = isFontAntiAliased();
            
            int subTexOff = hasMasterCanvas( isEditorMode ) ? 0 : 1;
            
            if ( rotationEnabled.getBooleanValue() )
            {
                float rotation = track.getInterpolatedAngleToRoad( scoringInfo );
                subTextures[0].setRotation( rotation );
                at.setToRotation( rotation, x0 + track.getXExtend( scale ) / 2, y0 + track.getZExtend( scale ) / 2 );
            }
            else
            {
                at.setToIdentity();
            }
            
            for ( int i = 0; i < numVehicles; i++ )
            {
                VehicleScoringInfo vsi = vsis[i];
                if ( vsi != null )
                {
                    short place = vsi.getPlace( useClassScoring );
                    
                    float lapDistance = vsi.getLapDistance();
                    
                    TransformableTexture tt = subTextures[subTexOff + i];
                    subTextures[subTexOff + i].setVisible( true );
                    int itemState = ( place << 0 ) | ( vsi.getDriverId() << 9 );
                    
                    track.getInterpolatedPosition( vsi.isInPits(), lapDistance, scale, position );
                    position.x += x0;
                    position.y += y0;
                    
                    Color color = null;
                    if ( ( place == 1 ) && ( !useClassScoring || ( vsi.getVehicleClassId() == viewedVSI.getVehicleClassId() ) ) )
                    {
                        itemState |= 1 << 26;
                        if ( vsi.isPlayer() && useMyColorForMe1st.getBooleanValue() )
                            color = markColorMe.getColor();
                        else
                            color = markColorLeader.getColor();
                    }
                    else if ( vsi.isPlayer() )
                    {
                        itemState |= 1 << 27;
                        color = markColorMe.getColor();
                    }
                    else if ( ( place == ownPlace - 1 ) && ( !useClassScoring || ( vsi.getVehicleClassId() == viewedVSI.getVehicleClassId() ) ) )
                    {
                        itemState |= 1 << 28;
                        color = markColorNextInFront.getColor();
                    }
                    else if ( ( place == ownPlace + 1 ) && ( !useClassScoring || ( vsi.getVehicleClassId() == viewedVSI.getVehicleClassId() ) ) )
                    {
                        itemState |= 1 << 29;
                        color = markColorNextBehind.getColor();
                    }
                    else
                    {
                        itemState |= 1 << 30;
                        color = markColorNormal.getColor();
                    }
                    
                    if ( itemStates[i] != itemState )
                    {
                        itemStates[i] = itemState;
                        
                        StandardWidgetSet.drawPositionItem( tt.getTexture(), 0, 0, itemRadius, place, color, itemBlackBorderWidth, displayPositionNumbers.getBooleanValue() ? font : null, posNumberFontAntiAliased, getFontColor(), displayNameLabels.getBooleanValue() ? nameLabelPos.getEnumValue() : null, vsi.getDriverNameTLC(), nameLabelFont.getFont(), nameLabelFont.isAntiAliased(), nameLabelFontColor.getColor() );
                    }
                    
                    if ( rotationEnabled.getBooleanValue() )
                        at.transform( position, position );
                    
                    tt.setTranslation( position.x, position.y );
                }
                else
                {
                    subTextures[subTexOff + i].setVisible( false );
                }
            }
            
            for ( int i = numVehicles; i < maxDisplayedVehicles; i++ )
                subTextures[subTexOff + i].setVisible( false );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( rotationEnabled, "Map rotation enabled?" );
        writer.writeProperty( roadColorSec1, "The color used for the road and sector 1 in #RRGGBBAA (hex)." );
        writer.writeProperty( roadBoundaryColorSec1, "The color used for the road boundary and sector 1 in #RRGGBBAA (hex)." );
        writer.writeProperty( roadColorSec2, "The color used for the road and sector 2 in #RRGGBBAA (hex)." );
        writer.writeProperty( roadBoundaryColorSec2, "The color used for the road boundary and sector 2 in #RRGGBBAA (hex)." );
        writer.writeProperty( roadColorSec3, "The color used for the road and sector 3 in #RRGGBBAA (hex)." );
        writer.writeProperty( roadBoundaryColorSec3, "The color used for the road boundary and sector 3 in #RRGGBBAA (hex)." );
        writer.writeProperty( pitlaneColor, "The color used for the pitlane in #RRGGBBAA (hex)." );
        writer.writeProperty( roadWidth, "The width of the roadin absolute pixels." );
        writer.writeProperty( baseItemRadius, "The abstract radius for any displayed driver item." );
        writer.writeProperty( markColorNormal, "The color used for all, but special cars in #RRGGBBAA (hex)." );
        writer.writeProperty( markColorLeader, "The color used for the leader's car in #RRGGBBAA (hex)." );
        writer.writeProperty( markColorMe, "The color used for your own car in #RRGGBBAA (hex)." );
        writer.writeProperty( useMyColorForMe1st, "Use 'markColorMe' for my item when I am at 1st place?" );
        writer.writeProperty( markColorNextInFront, "The color used for the car in front of you in #RRGGBBAA (hex)." );
        writer.writeProperty( markColorNextBehind, "The color used for the car behind you in #RRGGBBAA (hex)." );
        writer.writeProperty( displayPositionNumbers, "Display numbers on the position markers?" );
        writer.writeProperty( displayNameLabels, "Display name label near the position markers?" );
        writer.writeProperty( nameLabelPos, "Positioning of the name labels." );
        writer.writeProperty( nameLabelFont, "Font for the name labels." );
        writer.writeProperty( nameLabelFontColor, "Font color for the name labels." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( rotationEnabled ) );
        else if ( loader.loadProperty( roadColorSec1 ) );
        else if ( loader.loadProperty( roadBoundaryColorSec1 ) );
        else if ( loader.loadProperty( roadColorSec2 ) );
        else if ( loader.loadProperty( roadBoundaryColorSec2 ) );
        else if ( loader.loadProperty( roadColorSec3 ) );
        else if ( loader.loadProperty( roadBoundaryColorSec3 ) );
        else if ( loader.loadProperty( pitlaneColor ) );
        else if ( loader.loadProperty( roadWidth ) );
        else if ( loader.loadProperty( baseItemRadius ) );
        else if ( loader.loadProperty( markColorNormal ) );
        else if ( loader.loadProperty( markColorLeader ) );
        else if ( loader.loadProperty( markColorMe ) );
        else if ( loader.loadProperty( useMyColorForMe1st ) );
        else if ( loader.loadProperty( markColorNextInFront ) );
        else if ( loader.loadProperty( markColorNextBehind ) );
        else if ( loader.loadProperty( displayPositionNumbers ) );
        else if ( loader.loadProperty( displayNameLabels ) );
        else if ( loader.loadProperty( nameLabelPos ) );
        else if ( loader.loadProperty( nameLabelFont ) );
        else if ( loader.loadProperty( nameLabelFontColor ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( rotationEnabled );
        
        propsCont.addGroup( "Road" );
        
        propsCont.addProperty( roadColorSec1 );
        propsCont.addProperty( roadBoundaryColorSec1 );
        propsCont.addProperty( roadColorSec2 );
        propsCont.addProperty( roadBoundaryColorSec2 );
        propsCont.addProperty( roadColorSec3 );
        propsCont.addProperty( roadBoundaryColorSec3 );
        propsCont.addProperty( pitlaneColor );
        propsCont.addProperty( roadWidth );
        
        propsCont.addGroup( "Items" );
        
        propsCont.addProperty( baseItemRadius );
        
        propsCont.addProperty( markColorNormal );
        propsCont.addProperty( markColorLeader );
        propsCont.addProperty( markColorMe );
        propsCont.addProperty( useMyColorForMe1st );
        propsCont.addProperty( markColorNextInFront );
        propsCont.addProperty( markColorNextBehind );
        
        propsCont.addProperty( displayPositionNumbers );
        
        propsCont.addProperty( displayNameLabels );
        //if ( displayNameLabels.getBooleanValue() || forceAll )
        {
            //propsCont.addProperty( nameLabelPos );
            propsCont.addProperty( nameLabelFont );
            propsCont.addProperty( nameLabelFontColor );
        }
    }
    
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        roadWidth.setIntValue( 4 );
        pitlaneRoadWidth = 1f;
        //baseItemRadius.setIntValue( 20 );
        itemRadius = 3;
        itemBlackBorderWidth = 0;
    }
    
    @Override
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.COLOR_INDICATOR + "#00000000" );
    }
    
    public MapWidget( String name )
    {
        super( name, 16f, 24f );
        
        getBorderProperty().setBorder( "" );
        getFontProperty().setFont( StandardWidgetSet.POSITION_ITEM_FONT_NAME );
        getFontColorProperty().setColor( StandardWidgetSet.POSITION_ITEM_FONT_COLOR_NAME );
    }
}
