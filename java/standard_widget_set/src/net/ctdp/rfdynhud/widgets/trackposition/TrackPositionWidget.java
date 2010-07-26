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
package net.ctdp.rfdynhud.widgets.trackposition;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ModInfo;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.MapTools;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.LabelPositioning;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * The {@link TrackPositionWidget} displays all driving vehicles on a line.
 * 
 * @author Marvin Froehlich
 */
public class TrackPositionWidget extends Widget
{
    private static final int LINE_THICKNESS = 1;
    private static final int BASE_LINE_PADDING = 30;
    
    private int baseItemRadius = 9;
    private int itemRadius = baseItemRadius;
    
    private final ColorProperty lineColor = new ColorProperty( this, "lineColor", "#FFFFFF" );
    
    private final ColorProperty markColorNormal = new ColorProperty( this, "markColorNormal", StandardWidgetSet.POSITION_ITEM_COLOR_NORMAL );
    private final ColorProperty markColorLeader = new ColorProperty( this, "markColorLeader", StandardWidgetSet.POSITION_ITEM_COLOR_LEADER );
    private final ColorProperty markColorMe = new ColorProperty( this, "markColorMe", StandardWidgetSet.POSITION_ITEM_COLOR_ME );
    private final BooleanProperty useMyColorForMe1st = new BooleanProperty( this, "useMyColorForMe1st", false );
    private final ColorProperty markColorNextInFront = new ColorProperty( this, "markColorNextInFront", StandardWidgetSet.POSITION_ITEM_COLOR_NEXT_IN_FRONT );
    private final ColorProperty markColorNextBehind = new ColorProperty( this, "markColorNextBehind", StandardWidgetSet.POSITION_ITEM_COLOR_NEXT_BEHIND );
    
    private final BooleanProperty displayPositionNumbers = new BooleanProperty( this, "displayPosNumbers", true );
    
    private final BooleanProperty displayNameLabels = new BooleanProperty( this, "displayNameLabels", false );
    private final EnumProperty<LabelPositioning> nameLabelPos = new EnumProperty<LabelPositioning>( this, "nameLabelPos", LabelPositioning.BELOW );
    private final FontProperty nameLabelFont = new FontProperty( this, "nameLabelFont", StandardWidgetSet.POSITION_ITEM_FONT_NAME );
    private final ColorProperty nameLabelFontColor = new ColorProperty( this, "nameLabelFontColor", StandardWidgetSet.POSITION_ITEM_COLOR_NORMAL );
    
    private int maxDisplayedVehicles = -1;
    
    private static final int ANTI_ALIAS_RADIUS_OFFSET = 1;
    
    private TransformableTexture[] itemTextures = null;
    private int[] itemTextureOffsetsY = null;
    private VehicleScoringInfo[] vsis = null;
    private int[] itemStates = null;
    private int numVehicles = -1;
    
    private int lineLength = 0;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getVersion()
    {
        return ( composeVersion( 1, 1, 0 ) );
    }
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
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
    
    private final boolean getUseClassScoring()
    {
        return ( getConfiguration().getUseClassScoring() );
    }
    
    public void setItemRadius( int radius )
    {
        this.baseItemRadius = radius;
        
        forceAndSetDirty();
    }
    
    private void initMaxDisplayedVehicles( boolean isEditorMode, ModInfo modInfo )
    {
        if ( isEditorMode )
            this.maxDisplayedVehicles = 22 + 1;
        else
            this.maxDisplayedVehicles = modInfo.getMaxOpponents() + 1;
        
        this.maxDisplayedVehicles = Math.max( 4, Math.min( maxDisplayedVehicles, 32 ) );
    }
    
    private void updateVSIs( LiveGameData gameData, EditorPresets editorPresets )
    {
        initMaxDisplayedVehicles( editorPresets != null, gameData.getModInfo() );
        
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
    public void onScoringInfoUpdated( LiveGameData gameData, EditorPresets editorPresets )
    {
        updateVSIs( gameData, editorPresets );
    }
    
    private void initSubTextures( boolean isEditorMode, ModInfo modInfo )
    {
        initMaxDisplayedVehicles( isEditorMode, modInfo );
        
        if ( ( itemTextures == null ) || ( itemTextures.length != maxDisplayedVehicles ) )
        {
            itemTextures = new TransformableTexture[ maxDisplayedVehicles ];
        }
        
        itemRadius = Math.round( baseItemRadius * getConfiguration().getGameResolution().getViewportHeight() / 960f );
        
        if ( itemTextures[0] == null )
            itemTextures[0] = new TransformableTexture( 1, 1, isEditorMode );
        
        java.awt.Dimension size = StandardWidgetSet.getPositionItemSize( itemTextures[0].getTexture(), itemRadius, displayNameLabels.getBooleanValue() ? nameLabelPos.getEnumValue() : null, nameLabelFont.getFont(), nameLabelFont.isAntiAliased() );
        int w = size.width;
        int h = size.height;
        
        if ( ( itemTextures[0].getWidth() == w ) && ( itemTextures[0].getHeight() == h ) )
            return;
        
        for ( int i = 0; i < maxDisplayedVehicles; i++ )
        {
            itemTextures[i] = new TransformableTexture( w, h, isEditorMode );
            itemTextures[i].setVisible( false );
        }
    }
    
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        initSubTextures( editorPresets != null, gameData.getModInfo() );
        
        return ( itemTextures );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        initMaxDisplayedVehicles( isEditorMode, gameData.getModInfo() );
        
        if ( isEditorMode )
            updateVSIs( gameData, editorPresets );
        
        initSubTextures( isEditorMode, gameData.getModInfo() );
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        super.clearBackground( gameData, editorPresets, texture, offsetX, offsetY, width, height );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        texCanvas.setAntialiazingEnabled( true );
        
        int lineHeight = LINE_THICKNESS;
        int linePadding = (int)( BASE_LINE_PADDING * getConfiguration().getGameResolution().getViewportHeight() / (float)1200 );
        lineLength = width - 2 * linePadding;
        
        texCanvas.setColor( lineColor.getColor() );
        
        texCanvas.fillRect( offsetX + linePadding, offsetY + ( height - lineHeight ) / 2, lineLength, lineHeight );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        final VehicleScoringInfo viewedVSI = scoringInfo.getViewedVehicleScoringInfo();
        final boolean useClassScoring = getUseClassScoring();
        
        int linePadding = (int)( BASE_LINE_PADDING * getConfiguration().getGameResolution().getViewportWidth() / 1920f );
        
        int off2 = ( isFontAntiAliased() ? ANTI_ALIAS_RADIUS_OFFSET : 0 );
        
        short ownPlace = scoringInfo.getOwnPlace( useClassScoring );
        
        final Font font = getFont();
        final boolean posNumberFontAntiAliased = isFontAntiAliased();
        
        int n = Math.min( scoringInfo.getNumVehicles(), maxDisplayedVehicles );
        
        if ( ( itemTextureOffsetsY == null ) || ( itemTextureOffsetsY.length < itemTextures.length ) )
        {
            itemTextureOffsetsY = new int[ itemTextures.length ];
        }
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            VehicleScoringInfo vsi = vsis[i];
            if ( vsi != null )
            {
                short place = vsi.getPlace( useClassScoring );
                
                TransformableTexture tt = itemTextures[i];
                itemTextures[i].setVisible( true );
                int itemState = ( place << 0 ) | ( vsi.getDriverId() << 9 );
                
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
                    
                    itemTextureOffsetsY[i] = StandardWidgetSet.drawPositionItem( tt.getTexture(), 0, 0, itemRadius, place, color, true, displayPositionNumbers.getBooleanValue() ? font : null, posNumberFontAntiAliased, getFontColor(), displayNameLabels.getBooleanValue() ? nameLabelPos.getEnumValue() : null, vsi.getDriverNameTLC(), nameLabelFont.getFont(), nameLabelFont.isAntiAliased(), nameLabelFontColor.getColor() );
                }
                
                int yOff3 = vsi.isInPits() ? -3 : 0;
                
                tt.setTranslation( linePadding + off2 + vsi.getNormalizedLapDistance() * lineLength - itemRadius, off2 + height / 2 - itemRadius - itemTextureOffsetsY[i] + yOff3 );
            }
            else
            {
                itemTextures[i].setVisible( false );
            }
        }
        
        for ( int i = n; i < maxDisplayedVehicles; i++ )
            itemTextures[i].setVisible( false );
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( lineColor, "Color for the base line." );
        writer.writeProperty( "itemRadius", baseItemRadius, "The abstract radius for any displayed driver item." );
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
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( lineColor.loadProperty( key, value ) );
        else if ( key.equals( "itemRadius" ) )
            this.baseItemRadius = Integer.parseInt( value );
        else if ( markColorNormal.loadProperty( key, value ) );
        else if ( markColorLeader.loadProperty( key, value ) );
        else if ( markColorMe.loadProperty( key, value ) );
        else if ( useMyColorForMe1st.loadProperty( key, value ) );
        else if ( markColorNextInFront.loadProperty( key, value ) );
        else if ( markColorNextBehind.loadProperty( key, value ) );
        else if ( displayPositionNumbers.loadProperty( key, value ) );
        else if ( displayNameLabels.loadProperty( key, value ) );
        else if ( nameLabelPos.loadProperty( key, value ) );
        else if ( nameLabelFont.loadProperty( key, value ) );
        else if ( nameLabelFontColor.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( lineColor );
        
        propsCont.addProperty( new Property( this, "itemRadius", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setItemRadius( (Integer)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( baseItemRadius );
            }
        } );
        
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
            propsCont.addProperty( nameLabelPos );
            propsCont.addProperty( nameLabelFont );
            propsCont.addProperty( nameLabelFontColor );
        }
    }
    
    public TrackPositionWidget( String name )
    {
        super( name, 35.0f, 5.859375f );
        
        getFontProperty().setFont( StandardWidgetSet.POSITION_ITEM_FONT_NAME );
        getFontColorProperty().setColor( StandardWidgetSet.POSITION_ITEM_FONT_COLOR_NAME );
    }
}
