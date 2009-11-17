package net.ctdp.rfdynhud.widgets.fuel;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.editor.properties.FontProperty;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.editor.properties.PropertyEditorType;
import net.ctdp.rfdynhud.gamedata.FuelUsageRecorder;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.ByteOrderManager;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.FontUtils;
import net.ctdp.rfdynhud.widgets._util.IntValue;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.ValidityTest;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.DrawnString.Alignment;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.openmali.vecmath2.util.ColorUtils;

/**
 * The {@link FuelWidget} displays fuel information like current fuel load, fuel usage per lap,
 * and computed fuel for scheduled pitstops.
 * 
 * @author Marvin Froehlich
 */
public class FuelWidget extends Widget
{
    private static final InputAction INPUT_ACTION_INC_PITSTOP = new InputAction( "IncPitstopAction" );
    private static final InputAction INPUT_ACTION_DEC_PITSTOP = new InputAction( "DecPitstopAction" );
    
    private String fontKey2 = "StandardFont3";
    private Font font2 = null;
    
    private String fuelFontKey = "StandardFont";
    private Font fuelFont = null;
    
    private DrawnString fuelHeaderString = null;
    
    private int fuelBarLeftOffset = 4;
    private final Size fuelBarWidth;
    
    private String fuelFontColorKey = "#2828FF";
    private Color fuelFontColor = null;
    
    private boolean roundUpRemainingLaps = false;
    private int fuelSafetyPlanning = 2;
    
    private DrawnString fuelLoadString1 = null;
    private DrawnString fuelLoadString2 = null;
    private DrawnString fuelLoadString3 = null;
    
    private DrawnString fuelUsageHeaderString = null;
    private DrawnString fuelUsageLastLapHeaderString = null;
    private DrawnString fuelUsageAvgHeaderString = null;
    
    private DrawnString fuelUsageOneLapString = null;
    private DrawnString fuelUsageAvgString = null;
    
    private DrawnString nextPitstopHeaderString = null;
    private DrawnString nextPitstopLapString = null;
    private DrawnString nextPitstopFuelString = null;
    
    private int oldFuelRelevantLaps = -1;
    
    private static final byte[] colorFuel = new byte[ 4 ];
    static
    {
        colorFuel[ByteOrderManager.RED] = (byte)84;
        colorFuel[ByteOrderManager.GREEN] = (byte)118;
        colorFuel[ByteOrderManager.BLUE] = (byte)11;
        colorFuel[ByteOrderManager.ALPHA] = (byte)255;
    }
    
    /*
    private static final class StintFuel
    {
        public final float stintLength;
        public final float fuelConsumption;
        
        public StintFuel( float stintLength, float fuelConsumption )
        {
            this.stintLength = stintLength;
            this.fuelConsumption = fuelConsumption;
        }
    }
    */
    
    private int oldNextPitstopLapCorrection = -1;
    private int nextPitstopLapCorrection = 0;
    private int nextPitstopFuelLapsCorrection = 0;
    private final IntValue pitstopFuel = new IntValue( ValidityTest.GREATER_THAN, 0 );
    private final IntValue stintLength = new IntValue( ValidityTest.GREATER_THAN, 0 );
    
    //private float fuelAtLapStart = -1f;
    //private float lastFuelUsage = -1f;
    private int oldFuel = -1;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public InputAction[] getInputActions()
    {
        return ( new InputAction[] { INPUT_ACTION_INC_PITSTOP, INPUT_ACTION_DEC_PITSTOP } );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void bake()
    {
        super.bake();
        
        fuelBarWidth.bake();
    }
    
    public void setFont2( String font )
    {
        this.fontKey2 = font;
        this.font2 = null;
        
        forceAndSetDirty();
    }
    
    public final void setFont2( Font font, boolean virtual )
    {
        setFont2( FontUtils.getFontString( font, virtual ) );
    }
    
    public final Font getFont2()
    {
        font2 = FontProperty.getFontFromFontKey( fontKey2, font2, getConfiguration() );
        
        return ( font2 );
    }
    
    public void setFuelFont( String font )
    {
        this.fuelFontKey = font;
        this.fuelFont = null;
        
        forceAndSetDirty();
    }
    
    public final void setFuelFont( Font font, boolean virtual )
    {
        setFuelFont( FontUtils.getFontString( font, virtual ) );
    }
    
    public final Font getFuelFont()
    {
        fuelFont = FontProperty.getFontFromFontKey( fuelFontKey, fuelFont, getConfiguration() );
        
        return ( fuelFont );
    }
    
    /**
     * Sets the {@link Widget}'s font color.
     * 
     * @param color the color as hex string
     */
    public void setFuelFontColor( String color )
    {
        this.fuelFontColorKey = color;
        this.fuelFontColor = null;
        
        forceAndSetDirty();
    }
    
    /**
     * Sets the {@link Widget}'s font color.
     * 
     * @param color
     */
    public final void setFuelFontColor( Color color )
    {
        setFuelFontColor( ColorUtils.colorToHex( color ) );
    }
    
    /**
     * Sets the {@link Widget}'s font color.
     * 
     * @param red
     * @param green
     * @param blue
     */
    public final void setFuelFontColor( int red, int green, int blue )
    {
        setFuelFontColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    /**
     * Gets the {@link Widget}'s font color.
     * 
     * @return the {@link Widget}'s font color.
     */
    public final Color getFuelFontColor()
    {
        fuelFontColor = ColorProperty.getColorFromColorKey( fuelFontColorKey, fuelFontColor, getConfiguration() );
        
        return ( fuelFontColor );
    }
    
    public void setRoundUpRemainingLaps( boolean round )
    {
        this.roundUpRemainingLaps = round;
        
        forceAndSetDirty();
    }
    
    public final boolean getRoundUpRemainingLaps()
    {
        return ( roundUpRemainingLaps );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( boolean isEditorMode, SessionType sessionType, LiveGameData gameData )
    {
        super.onSessionStarted( isEditorMode, sessionType, gameData );
        
        this.oldFuelRelevantLaps = -1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( boolean isEditorMode, LiveGameData gameData )
    {
        super.onRealtimeEntered( isEditorMode, gameData );
        
        nextPitstopLapCorrection = 0;
        nextPitstopFuelLapsCorrection = 0;
        oldNextPitstopLapCorrection = ( ( nextPitstopLapCorrection + Short.MAX_VALUE / 2 ) << 16 ) | ( nextPitstopFuelLapsCorrection + Short.MAX_VALUE / 2 );
        pitstopFuel.reset();
        
        stintLength.reset();
        oldFuel = -1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPitsExited( LiveGameData gameData )
    {
        super.onPitsExited( gameData );
        
        if ( stintLength.getValue() < 1 )
        {
            nextPitstopLapCorrection = 0;
            nextPitstopFuelLapsCorrection = 0;
        }
        
        stintLength.reset();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onBoundInputStateChanged( boolean isEditorMode, InputAction action, boolean state, int modifierMask )
    {
        if ( action == INPUT_ACTION_INC_PITSTOP )
        {
            if ( ( modifierMask & InputAction.MODIFIER_MASK_SHIFT ) == 0 )
            {
                if ( nextPitstopLapCorrection < 0 )
                {
                    this.nextPitstopLapCorrection++;
                    this.nextPitstopFuelLapsCorrection--;
                }
            }
            else
            {
                this.nextPitstopFuelLapsCorrection++;
            }
        }
        else if ( action == INPUT_ACTION_DEC_PITSTOP )
        {
            if ( ( modifierMask & InputAction.MODIFIER_MASK_SHIFT ) == 0 )
            {
                this.nextPitstopLapCorrection--;
                this.nextPitstopFuelLapsCorrection++;
            }
            else
            {
                this.nextPitstopFuelLapsCorrection--;
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean isEditorMode, long sessionNanos, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean isEditorMode, long sessionNanos, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        final java.awt.Font font = getFont();
        final java.awt.Font font2 = getFont2();
        final java.awt.Font fuelFont = getFuelFont();
        final java.awt.Color fontColor = getFontColor();
        final java.awt.Color fuelFontColor = getFuelFontColor();
        
        int left = 2;
        int top = -2;
        
        fuelHeaderString = new DrawnString( left, top, Alignment.LEFT, false, font, fontColor, "Fuel: (", null, ")" );
        
        int fuelBarWidth = this.fuelBarWidth.getEffectiveWidth();
        int fuelBarCenter = left + fuelBarLeftOffset + ( fuelBarWidth / 2 );
        
        fuelLoadString1 = new DrawnString( fuelBarCenter, 0, Alignment.CENTER, false, fuelFont, fuelFontColor, null, null, "L" );
        fuelLoadString2 = new DrawnString( null, fuelLoadString1, fuelBarCenter, 0, Alignment.CENTER, false, fuelFont, fuelFontColor, null, null, "kg" );
        fuelLoadString3 = new DrawnString( null, fuelLoadString2, fuelBarCenter, 0, Alignment.CENTER, false, font2, fuelFontColor, null, null, null );
        
        int rightLeft = left + fuelBarLeftOffset + fuelBarWidth + 2;
        
        fuelUsageHeaderString = new DrawnString( null, fuelHeaderString, rightLeft, 0, Alignment.LEFT, false, font, fontColor, "Usage:", null, null );
        fuelUsageLastLapHeaderString = new DrawnString( null, fuelUsageHeaderString, rightLeft + 50, 2, Alignment.CENTER, false, font, fontColor, "Last lap", null, null );
        fuelUsageAvgHeaderString = new DrawnString( null, fuelUsageHeaderString, rightLeft + 135, 2, Alignment.CENTER, false, font, fontColor, "avg.", null, null );
        
        fuelUsageOneLapString = new DrawnString( null, fuelUsageLastLapHeaderString, rightLeft + 50, 2, Alignment.CENTER, false, font, fontColor, null, null, null );
        fuelUsageAvgString = new DrawnString( null, fuelUsageAvgHeaderString, rightLeft + 135, 2, Alignment.CENTER, false, font, fontColor, null, null, null );
        
        nextPitstopHeaderString = new DrawnString( null, fuelUsageOneLapString, rightLeft, 7, Alignment.LEFT, false, font, fontColor, "Next Pitstop:", null, null );
        nextPitstopLapString = new DrawnString( null, nextPitstopHeaderString, rightLeft + 10, 2, Alignment.LEFT, false, font2, fontColor, "Lap ", null, null );
        nextPitstopFuelString = new DrawnString( null, nextPitstopLapString, rightLeft + 10, 0, Alignment.LEFT, false, font2, fontColor, "Fuel: ", null, null );
    }
    
    private void drawFuel( float fuel, int tankSize, TextureImage2D image, int x, int y, int height )
    {
        int w = fuelBarWidth.getEffectiveWidth();
        int h = height;
        
        float normFuel = fuel / tankSize;
        
        Color awtColor = new Color( colorFuel[ByteOrderManager.RED] & 0xFF, colorFuel[ByteOrderManager.GREEN] & 0xFF, colorFuel[ByteOrderManager.BLUE] & 0xFF );
        
        int barHeight = Math.min( (int)( h * normFuel ), h );
        
        if ( normFuel < 1.0f )
        {
            image.clear( Color.BLACK, x, y, w, h - barHeight, false, null );
        }
        
        image.clear( awtColor, x, y + h - barHeight, w, barHeight, false, null );
        
        image.markDirty( x, y, w, h );
    }
    
    @Override
    protected void drawWidget( boolean isEditorMode, long sessionNanos, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, boolean needsCompleteRedraw )
    {
        final TextureImage2D image = texCanvas.getImage();
        final java.awt.Color backgroundColor = getBackgroundColor();
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        TelemetryData telemData = gameData.getTelemetryData();
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        final int tankSize = (int)gameData.getPhysics().getFuelRange().getMaxValue();
        
        if ( needsCompleteRedraw )
        {
            fuelHeaderString.draw( offsetX, offsetY, String.valueOf( tankSize ) + " Liters max", backgroundColor, image );
            fuelUsageHeaderString.draw( offsetX, offsetY, "", backgroundColor, image );
            fuelUsageLastLapHeaderString.draw( offsetX, offsetY, "", backgroundColor, image );
            fuelUsageAvgHeaderString.draw( offsetX, offsetY, "", backgroundColor, image );
            //nextPitstopHeaderString.draw( offsetX, offsetY, "", backgroundColor, image );
        }
        
        float fuel = isEditorMode ? ( tankSize * 3f / 4f ) : telemData.getFuel();
        float avgFuelUsage = FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.getAverage();
        
        int fuel_ = Math.round( fuel * 10f );
        if ( needsCompleteRedraw || ( clock1 && ( fuel_ != oldFuel ) ) )
        {
            oldFuel = fuel_;
            
            int fuelY = fuelHeaderString.getAbsY() + fuelHeaderString.getMaxHeight( true ) + 0;
            int fuelHeight = height - fuelY - 4;
            drawFuel( fuel, tankSize, image, offsetX + fuelBarLeftOffset, offsetY + fuelY, fuelHeight );
            
            String string = NumberUtil.formatFloat( fuel, 1, true );
            fuelLoadString1.draw( offsetX, offsetY + fuelY, string, (Color)null, image );
            string = NumberUtil.formatFloat( fuel * gameData.getPhysics().getWeightOfOneLiterOfFuel(), 1, true );
            fuelLoadString2.draw( offsetX, offsetY + fuelY, string, (Color)null, image );
            
            if ( !isEditorMode && ( avgFuelUsage > 0f ) )
            {
                if ( getRoundUpRemainingLaps() )
                    string = NumberUtil.formatFloat( ( fuel / avgFuelUsage ) + ( vsi.getStintLength() - (int)vsi.getStintLength() ), 1, true ) + "Laps";
                else
                    string = NumberUtil.formatFloat( fuel / avgFuelUsage, 1, true ) + "Laps";
            }
            else
            {
                string = "N/A";
            }
            fuelLoadString3.draw( offsetX, offsetY + fuelY, string, (Color)null, image );
        }
        
        stintLength.update( (int)vsi.getStintLength() );
        int fuelRelevantLaps = FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.getFuelRelevantLaps();
        
        if ( isEditorMode || ( fuelRelevantLaps == 0 ) )
        {
            if ( isEditorMode || ( fuelRelevantLaps != oldFuelRelevantLaps ) )
            {
                this.oldFuelRelevantLaps = fuelRelevantLaps;
                
                fuelUsageOneLapString.draw( offsetX, offsetY, "N/A", backgroundColor, image );
                fuelUsageAvgString.draw( offsetX, offsetY, "N/A", backgroundColor, image );
            }
        }
        else if ( needsCompleteRedraw || stintLength.hasChanged() )
        {
            float lastFuelUsage = FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.getLastLap();
            
            String string;
            if ( lastFuelUsage > 0f )
                string = NumberUtil.formatFloat( lastFuelUsage, 3, true ) + "L";
            else
                string = "N/A";
            fuelUsageOneLapString.draw( offsetX, offsetY, string, backgroundColor, image );
            
            string = NumberUtil.formatFloat( avgFuelUsage, 3, true ) + "L";
            fuelUsageAvgString.draw( offsetX, offsetY, string, backgroundColor, image );
        }
        
        if ( needsCompleteRedraw )
        {
            nextPitstopHeaderString.draw( offsetX, offsetY, "", backgroundColor, image );
        }
        
        int nextPitstopLap = -1;
        int pitstopFuel_ = -1;
        int pitstopLaps = -1;
        if ( !isEditorMode && ( avgFuelUsage > 0f ) )
        {
            int currLap = vsi.getCurrentLap();
            boolean isRace = scoringInfo.getSessionType().isRace();
            int totalLaps = scoringInfo.getMaxLaps(); // compute for time based races
            
            int remainingFuelLaps = (int)Math.floor( ( fuel / avgFuelUsage ) + ( vsi.getStintLength() - (int)vsi.getStintLength() ) );
            nextPitstopLap = vsi.getLapsCompleted() + remainingFuelLaps + nextPitstopLapCorrection;
            
            if ( nextPitstopLap < currLap )
            {
                int delta = currLap - nextPitstopLap;
                
                nextPitstopLapCorrection += delta;
                nextPitstopFuelLapsCorrection -= delta;
                nextPitstopLap = vsi.getLapsCompleted() + remainingFuelLaps + nextPitstopLapCorrection;
            }
            
            int nextPitstopIndex = Math.min( gameData.getScoringInfo().getPlayersVehicleScoringInfo().getNumPitstopsMade() + 1, gameData.getSetup().getGeneral().getNumPitstops() );
            float pitstopFuel0 = gameData.getSetup().getGeneral().getFuel( nextPitstopIndex );
            pitstopLaps = (int)Math.floor( pitstopFuel0 / avgFuelUsage );
            pitstopFuel_ = (int)Math.ceil( ( pitstopLaps + nextPitstopFuelLapsCorrection ) * avgFuelUsage );
            
            while ( pitstopFuel_ < avgFuelUsage )
            {
                nextPitstopFuelLapsCorrection++;
                pitstopFuel_ = (int)Math.ceil( ( pitstopLaps + nextPitstopFuelLapsCorrection ) * avgFuelUsage );
            }
            
            while ( ( pitstopFuel_ > tankSize ) || ( isRace && ( nextPitstopLap + pitstopLaps + nextPitstopFuelLapsCorrection > totalLaps ) ) )
            {
                nextPitstopFuelLapsCorrection--;
                pitstopFuel_ = (int)Math.ceil( ( pitstopLaps + nextPitstopFuelLapsCorrection ) * avgFuelUsage );
            }
        }
        
        pitstopFuel.update( pitstopFuel_ );
        
        int tmp = ( ( nextPitstopLapCorrection + Short.MAX_VALUE / 2 ) << 16 ) | ( nextPitstopFuelLapsCorrection + Short.MAX_VALUE / 2 );
        
        if ( needsCompleteRedraw || ( tmp != oldNextPitstopLapCorrection ) || pitstopFuel.hasChanged() )
        {
            oldNextPitstopLapCorrection = tmp;
            
            String postfix1 = " (" + NumberUtil.delta( nextPitstopLapCorrection ) + ")";
            String postfix2 = NumberUtil.delta( nextPitstopFuelLapsCorrection );
            
            if ( avgFuelUsage > 0f )
            {
                if ( pitstopFuel.isValid() )
                {
                    String string = String.valueOf( nextPitstopLap ) + postfix1;
                    nextPitstopLapString.draw( offsetX, offsetY, string, backgroundColor, image );
                    
                    string = String.valueOf( pitstopFuel.getValue() + fuelSafetyPlanning ) + "L (" + ( pitstopLaps + nextPitstopFuelLapsCorrection ) + "Laps," + postfix2 + ")";
                    nextPitstopFuelString.draw( offsetX, offsetY, string, backgroundColor, image );
                }
                else
                {
                    nextPitstopLapString.draw( offsetX, offsetY, "-", backgroundColor, image );
                    nextPitstopFuelString.draw( offsetX, offsetY, "-", backgroundColor, image );
                }
            }
            else
            {
                nextPitstopLapString.draw( offsetX, offsetY, "N/A" + postfix1, backgroundColor, image );
                nextPitstopFuelString.draw( offsetX, offsetY, "N/A (" + postfix2 + " Laps)", backgroundColor, image );
            }
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( "font2", fontKey2, "The used (smaller) font." );
        writer.writeProperty( "fuelFont", fuelFontKey, "The used font for fuel load." );
        writer.writeProperty( "fuelFontColor", fuelFontColorKey, "The color to use for fuel load in the format #RRGGBB (hex)." );
        writer.writeProperty( "roundUpRemainingLaps", getRoundUpRemainingLaps(), "Round up remaining fuel laps to include the current lap?" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( key.equals( "font2" ) )
            this.fontKey2 = value;
        
        else if ( key.equals( "fuelFont" ) )
            this.fuelFontKey = value;
        
        else if ( key.equals( "fuelFontColor" ) )
            this.fuelFontColorKey = value;
        
        else if ( key.equals( "roundUpRemainingLaps" ) )
            this.roundUpRemainingLaps = Boolean.parseBoolean( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( FlaggedList propsList )
    {
        super.getProperties( propsList );
        
        FlaggedList superProps = (FlaggedList)propsList.get( propsList.size() - 1 );
        
        superProps.add( new FontProperty( "font2", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setFont2( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( fontKey2 );
            }
        } );
        
        FlaggedList props = new FlaggedList( "Specific", true );
        
        props.add( new FontProperty( "fuelFont", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setFuelFont( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( fuelFontKey );
            }
        } );
        
        props.add( new ColorProperty( "fuelFontColor", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setFuelFontColor( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( fuelFontColorKey );
            }
        } );
        
        props.add( new Property( "roundUpRemainingLaps", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setRoundUpRemainingLaps( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getRoundUpRemainingLaps() );
            }
        } );
        
        propsList.add( props );
    }
    
    public FuelWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.178f, Size.PERCENT_OFFSET + 0.135f );
        
        this.fuelBarWidth = new Size( Size.PERCENT_OFFSET + 0.26f, 0, this );
    }
}
