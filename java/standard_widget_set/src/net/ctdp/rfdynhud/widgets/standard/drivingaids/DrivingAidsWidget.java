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
package net.ctdp.rfdynhud.widgets.standard.drivingaids;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ctdp.rfdynhud.gamedata.DrivingAids;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.FloatProperty;
import net.ctdp.rfdynhud.properties.ImagePropertyWithTexture;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.ListProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link DrivingAidsWidget} displays driving aids' states.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DrivingAidsWidget extends Widget implements DrivingAids.DrivingAidStateChangeListener
{
    private static class AidStateValue
    {
        final Integer state;
        final String stateString;
        final String caption;
        
        @Override
        public String toString()
        {
            return ( caption );
        }
        
        AidStateValue( Integer state, String stateString, String caption )
        {
            this.state = state;
            this.stateString = stateString;
            this.caption = caption;
        }
        
        static final AidStateValue never = new AidStateValue( null, "N", "Never" );
        static final AidStateValue always = new AidStateValue( null, "A", "Always" );
    }
    
    private static enum Alignment
    {
        TOP_LEFT,
        CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        ;
    }
    
    private final FloatProperty columns = new FloatProperty( "columns", 2, 1, Integer.MAX_VALUE );
    private final IntProperty gap = new IntProperty( "gap", 5 );
    private final EnumProperty<Alignment> alignment = new EnumProperty<Alignment>( "alignment", Alignment.TOP_LEFT );
    
    private final StringProperty stateVisibilities = new StringProperty( "stateVisibilities", "" );
    
    private ListProperty<AidStateValue, List<AidStateValue>>[] stateVisibilities_ = null;
    private String stateVisibilitiesToParse = null;
    
    private final ImagePropertyWithTexture imageBackgroundImageOff = new ImagePropertyWithTexture( "iconBackgroundOff", null, "standard/drivingaid_background_off.png", false, true );
    private final ImagePropertyWithTexture imageBackgroundImageLow = new ImagePropertyWithTexture( "iconBackgroundLow", null, "standard/drivingaid_background_low.png", false, true );
    private final ImagePropertyWithTexture imageBackgroundImageMedium = new ImagePropertyWithTexture( "iconBackgroundMedium", null, "standard/drivingaid_background_medium.png", false, true );
    private final ImagePropertyWithTexture imageBackgroundImageHigh = new ImagePropertyWithTexture( "iconBackgroundHigh", null, "standard/drivingaid_background_high.png", false, true );
    
    private TextureImage2D[][] icons = null;
    private int iconSize = 0;
    private int innerIconSize = 0;
    private int innerIconOffset = 0;
    private boolean[] stateChanged = null;
    
    private int numRows = -1;
    
    public DrivingAidsWidget()
    {
        super( StandardWidgetSet.INSTANCE, StandardWidgetSet.WIDGET_PACKAGE, 8.125f, 34.417f );
        
        getBorderProperty().setBorder( null );
        getBackgroundProperty().setColorValue( "#00000000" );
        setPadding( 0 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( columns, "Number of columns to display" );
        writer.writeProperty( gap, "Gap between the icons" );
        writer.writeProperty( alignment, "Alignment of icons" );
        
        if ( ( stateVisibilities_ != null ) && ( stateVisibilities_.length > 0 ) )
        {
            StringBuilder sb = new StringBuilder();
            
            for ( int i = 0; i < stateVisibilities_.length; i++ )
            {
                if ( i > 0 )
                    sb.append( "," );
                
                sb.append( stateVisibilities_[i].getValue().stateString );
            }
            
            stateVisibilities.setValue( sb.toString() );
            writer.writeProperty( stateVisibilities, "Aid state visibilities" );
        }
        
        writer.writeProperty( imageBackgroundImageOff, "Background image for an aid's 'off' state" );
        writer.writeProperty( imageBackgroundImageLow, "Background image for an aid's 'low' state" );
        writer.writeProperty( imageBackgroundImageMedium, "Background image for an aid's 'medium' state" );
        writer.writeProperty( imageBackgroundImageHigh, "Background image for an aid's 'high' state" );
    }
    
    private void parseStateVisibilities()
    {
        String[] tokens = stateVisibilitiesToParse.split( "," );
        stateVisibilitiesToParse = null;
        
        if ( tokens.length != stateVisibilities_.length )
            return;
        
        for ( int i = 0; i < tokens.length; i++ )
        {
            stateVisibilities_[i].setValue( stateVisibilities_[i].getDefaultValue() );
            
            for ( AidStateValue asv : stateVisibilities_[i].getList() )
            {
                if ( asv.stateString.equals( tokens[i] ) )
                {
                    stateVisibilities_[i].setValue( asv );
                    break;
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader, LiveGameData gameData )
    {
        super.loadProperty( loader, gameData );
        
        if ( loader.loadProperty( columns ) );
        else if ( loader.loadProperty( gap ) );
        else if ( loader.loadProperty( alignment ) );
        else if ( loader.loadProperty( stateVisibilities ) && !stateVisibilities.getValue().isEmpty() )
        {
            stateVisibilitiesToParse = stateVisibilities.getValue();
            if ( stateVisibilities_ != null )
                parseStateVisibilities();
        }
        else if ( loader.loadProperty( imageBackgroundImageOff ) );
        else if ( loader.loadProperty( imageBackgroundImageLow ) );
        else if ( loader.loadProperty( imageBackgroundImageMedium ) );
        else if ( loader.loadProperty( imageBackgroundImageHigh ) );
    }
    
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addProperty( imageBackgroundImageOff );
        propsCont.addProperty( imageBackgroundImageLow );
        propsCont.addProperty( imageBackgroundImageMedium );
        propsCont.addProperty( imageBackgroundImageHigh );
        
        propsCont.addGroup( "Misc" );
        
        propsCont.addProperty( columns );
        propsCont.addProperty( gap );
        propsCont.addProperty( alignment );
        
        if ( ( stateVisibilities_ != null ) && ( stateVisibilities_.length > 0 ) )
        {
            propsCont.addGroup( "Visibility and states" );
            
            for ( int i = 0; i < stateVisibilities_.length; i++ )
                propsCont.addProperty( stateVisibilities_[i] );
        }
    }
    
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        final DrivingAids drivingAids = gameData.getDrivingAids();
        
        int numAids = drivingAids.getNumAids();
        //int numRows = (int)Math.ceil( numAids / columns.getFloatValue() );
        
        int smallerSize = Math.min( widgetInnerWidth, widgetInnerHeight );
        
        iconSize = (int)Math.floor( ( smallerSize - gap.getFloatValue() * ( columns.getFloatValue() - 1 ) ) / columns.getFloatValue() );
        innerIconSize = iconSize * 3 / 4;
        innerIconOffset = ( iconSize - innerIconSize ) / 2;
        
        imageBackgroundImageOff.updateSize( iconSize, iconSize, isEditorMode );
        imageBackgroundImageLow.updateSize( iconSize, iconSize, isEditorMode );
        imageBackgroundImageMedium.updateSize( iconSize, iconSize, isEditorMode );
        imageBackgroundImageHigh.updateSize( iconSize, iconSize, isEditorMode );
        
        if ( icons == null )
            icons = new TextureImage2D[ numAids ][];
        
        stateChanged = new boolean[ numAids ];
        
        for ( int i = 0; i < numAids; i++ )
        {
            int numStates = drivingAids.getNumStates( i );
            icons[i] = new TextureImage2D[ numStates ];
            
            for ( int j = 0; j < numStates; j++ )
            {
                icons[i][j] = drivingAids.getAidIcon( i, j ).getScaledTextureImage( innerIconSize, innerIconSize, icons[i][j], isEditorMode );
            }
            
            stateChanged[i] = true;
        }
    }
    
    @Override
    protected void onWidgetAttached( WidgetsConfiguration config, LiveGameData gameData )
    {
        gameData.getDrivingAids().registerListener( this );
    }
    
    @Override
    protected void onWidgetDetached( WidgetsConfiguration config, LiveGameData gameData )
    {
        gameData.getDrivingAids().unregisterListener( this );
    }
    
    @SuppressWarnings( { "unchecked", "cast" } )
    private void initStateVisibilityProperties( DrivingAids aids )
    {
        if ( stateVisibilities_ == null )
        {
            int n = aids.getNumAids();
            //stateVisibilities_ = new ListProperty<AidStateValue, List<AidStateValue>>[ n ];
            stateVisibilities_ = (ListProperty<AidStateValue, List<AidStateValue>>[])new ListProperty[ n ];
            
            for ( int i = 0; i < n; i++ )
            {
                List<AidStateValue> values = new ArrayList<AidStateValue>();
                values.add( AidStateValue.never );
                values.add( AidStateValue.always );
                
                for ( int s = aids.getMinState( i ); s <= aids.getMaxState( i ); s++ )
                {
                    values.add( new AidStateValue( s, String.valueOf( s ), "If not " + aids.getAidStateName( i, s ) ) );
                }
                
                stateVisibilities_[i] = new ListProperty<AidStateValue, List<AidStateValue>>( aids.getAidName( i ), AidStateValue.always, values )
                {
                    @Override
                    protected void onValueChanged( AidStateValue oldValue, AidStateValue newValue )
                    {
                        forceCompleteRedraw( false );
                    }
                };
            }
        }
        
        if ( stateVisibilitiesToParse != null )
            parseStateVisibilities();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        initStateVisibilityProperties( gameData.getDrivingAids() );
    }
    
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode )
    {
    }
    
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, boolean isEditorMode, boolean isPaused )
    {
    }
    
    @Override
    public void onDrivingAidsUpdated( LiveGameData gameData, boolean isEditorMode )
    {
    }
    
    private void countRows( DrivingAids drivingAids, boolean isEditorMode )
    {
        numRows = -1;
        
        if ( alignment.getValue() == Alignment.CENTER )
        {
            numRows = 0;
            int column = 0;
            
            int numAids = drivingAids.getNumAids();
            
            for ( int i = 0; i < numAids; i++ )
            {
                AidStateValue asv = stateVisibilities_[i].getValue();
                
                if ( asv == AidStateValue.never )
                    continue;
                
                if ( !isEditorMode && ( asv != AidStateValue.always ) )
                {
                    if ( asv.state.intValue() == drivingAids.getAidState( i ) )
                        continue;
                }
                
                if ( numRows == 0 )
                    numRows = 1;
                
                column++;
                
                if ( column >= columns.getIntValue() )
                {
                    column = 0;
                    numRows++;
                }
            }
        }
    }
    
    @Override
    public void onDrivingAidStateChanged( LiveGameData gameData, int aidIndex, int oldState, int newState )
    {
        if ( stateChanged != null )
            stateChanged[aidIndex] = true;
        
        //if ( ( stateVisibilities_ != null ) && ( stateVisibilities_.length == gameData.getDrivingAids().getNumAids() ) )
        {
            //if ( ( stateVisibilities_[aidIndex].getValue() != AidStateValue.never ) && ( stateVisibilities_[aidIndex].getValue() != AidStateValue.always ) )
                forceCompleteRedraw( false );
        }
        
        countRows( gameData.getDrivingAids(), false );
        
        /*
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < gameData.getDrivingAids().getNumAids(); i++ )
        {
            if ( i > 0 )
                sb.append( ", " );
            
            sb.append( gameData.getDrivingAids().getAidName( i ) );
            sb.append( ": " );
            sb.append( gameData.getDrivingAids().getAidState( i ) + " - " + gameData.getDrivingAids().getAidStateName( i ) );
        }
        
        log( sb.toString() );
        */
    }
    
    private TextureImage2D getIconBackground( int state )
    {
        switch ( state )
        {
            case 0:
                return ( imageBackgroundImageOff.getTexture() );
            case 1:
                return ( imageBackgroundImageLow.getTexture() );
            case 2:
                return ( imageBackgroundImageMedium.getTexture() );
            case 3:
            default:
                return ( imageBackgroundImageHigh.getTexture() );
        }
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final DrivingAids drivingAids = gameData.getDrivingAids();
        int numAids = drivingAids.getNumAids();
        
        Alignment align = alignment.getValue();
        
        int x = 0;
        int y = 0;
        int column = 0;
        
        if ( ( align == Alignment.CENTER ) && ( ( numRows < 0 ) || isEditorMode ) )
            countRows( drivingAids, isEditorMode );
        
        for ( int i = 0; i < numAids; i++ )
        {
            AidStateValue asv = stateVisibilities_[i].getValue();
            
            if ( asv == AidStateValue.never )
                continue;
            
            if ( !isEditorMode && ( asv != AidStateValue.always ) )
            {
                if ( asv.state.intValue() == drivingAids.getAidState( i ) )
                    continue;
            }
            
            if ( stateChanged[i] || needsCompleteRedraw )
            {
                int x2 = x;
                int y2 = y;
                
                if ( getInnerSize().getEffectiveWidth() > getInnerSize().getEffectiveHeight() )
                {
                    x2 = y;
                    y2 = x;
                }
                
                int x3 = x2;
                int y3 = y2;
                if ( align == Alignment.CENTER )
                {
                    if ( getInnerSize().getEffectiveWidth() > getInnerSize().getEffectiveHeight() )
                        x3 = ( getInnerSize().getEffectiveWidth() - ( numRows * iconSize ) - ( numRows * gap.getIntValue() - gap.getIntValue() ) ) / 2 + x2;
                    else
                        y3 = ( getInnerSize().getEffectiveHeight() - ( numRows * iconSize ) - ( numRows * gap.getIntValue() - gap.getIntValue() ) ) / 2 + y2;
                }
                else if ( ( align == Alignment.TOP_RIGHT ) || ( align == Alignment.BOTTOM_RIGHT ) )
                {
                    x3 = getInnerSize().getEffectiveWidth() - x2 - iconSize;
                }
                
                int x4 = x3;
                int y4 = y3;
                if ( ( align == Alignment.BOTTOM_LEFT ) || ( align == Alignment.BOTTOM_RIGHT ) )
                {
                    y4 = getInnerSize().getEffectiveHeight() - y3 - iconSize;
                }
                
                clearBackgroundRegion( texture, offsetX, offsetY, x4, y4, iconSize, iconSize, true, null );
                
                int state = drivingAids.getAidState( i );
                int state2 = state;
                int numStates = drivingAids.getNumStates( i );
                if ( numStates == 2 )
                    state2 = ( state == 0 ) ? 0 : 3;
                else if ( numStates == 3 )
                    state2 = ( state == 0 ) ? 0 : ( ( state == 1 ) ? 1 : 3 );
                
                TextureImage2D background = getIconBackground( state2 );
                
                if ( background != null )
                    texture.drawImage( background, offsetX + x4, offsetY + y4, false, null );
                
                texture.drawImage( icons[i][state], offsetX + x4 + innerIconOffset, offsetY + y4 + innerIconOffset, false, null );
                
                stateChanged[i] = false;
            }
            
            column++;
            
            if ( column < columns.getIntValue() )
            {
                x += iconSize;
                x += gap.getIntValue();
            }
            else
            {
                column = 0;
                
                x = 0;
                y += iconSize;
                y += gap.getIntValue();
            }
        }
    }
}
