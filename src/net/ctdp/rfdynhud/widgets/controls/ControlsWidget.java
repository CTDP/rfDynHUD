package net.ctdp.rfdynhud.widgets.controls;

import java.awt.Color;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.editor.properties.PropertyEditorType;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.openmali.vecmath2.util.ColorUtils;

/**
 * The {@link ControlsWidget} displays clutch, brake and throttle.
 * 
 * @author Marvin Froehlich
 */
public class ControlsWidget extends Widget
{
    private boolean displayClutch = true;
    private boolean displayBrake = true;
    private boolean displayThrottle = true;
    
    private String clutchColorKey = "#0000FF";
    private Color clutchColor = null;
    private String brakeColorKey = "#FF0000";
    private Color brakeColor = null;
    private String throttleColorKey = "#00FF00";
    private Color throttleColor = null;
    
    private TransformableTexture texClutch = null;
    private TransformableTexture texBrake = null;
    private TransformableTexture texThrottle = null;
    
    private int gap = 10;
    
    public void setDisplayClutch( boolean display )
    {
        this.displayClutch = display;
        
        texClutch = null;
        texBrake = null;
        texThrottle = null;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayClutch()
    {
        return ( displayClutch );
    }
    
    public void setClutchColor( String color )
    {
        this.clutchColorKey = color;
        this.clutchColor = null;
        
        forceAndSetDirty();
    }
    
    public final void setClutchColor( Color color )
    {
        setClutchColor( ColorUtils.colorToHex( color ) );
    }
    
    public final void setClutchColor( int red, int green, int blue )
    {
        setClutchColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getClutchColor()
    {
        clutchColor = ColorProperty.getColorFromColorKey( clutchColorKey, clutchColor, getConfiguration() );
        
        return ( clutchColor );
    }
    
    public void setDisplayBrake( boolean display )
    {
        this.displayBrake = display;
        
        texClutch = null;
        texBrake = null;
        texThrottle = null;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayBrake()
    {
        return ( displayBrake );
    }
    
    public void setBrakeColor( String color )
    {
        this.brakeColorKey = color;
        this.brakeColor = null;
        
        forceAndSetDirty();
    }
    
    public final void setBrakeColor( Color color )
    {
        setBrakeColor( ColorUtils.colorToHex( color ) );
    }
    
    public final void setBrakeColor( int red, int green, int blue )
    {
        setBrakeColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getBrakeColor()
    {
        brakeColor = ColorProperty.getColorFromColorKey( brakeColorKey, brakeColor, getConfiguration() );
        
        return ( brakeColor );
    }
    
    public void setDisplayThrottle( boolean display )
    {
        this.displayThrottle = display;
        
        texClutch = null;
        texBrake = null;
        texThrottle = null;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayThrottle()
    {
        return ( displayThrottle );
    }
    
    public void setThrottleColor( String color )
    {
        this.throttleColorKey = color;
        this.throttleColor = null;
        
        forceAndSetDirty();
    }
    
    public final void setThrottleColor( Color color )
    {
        setThrottleColor( ColorUtils.colorToHex( color ) );
    }
    
    public final void setThrottleColor( int red, int green, int blue )
    {
        setThrottleColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final Color getThrottleColor()
    {
        throttleColor = ColorProperty.getColorFromColorKey( throttleColorKey, throttleColor, getConfiguration() );
        
        return ( throttleColor );
    }
    
    public void setGap( int gap )
    {
        this.gap = gap;
        
        forceAndSetDirty();
    }
    
    public final int getGap()
    {
        return ( gap );
    }
    
    private int initSubTextures( int widgetInnerWidth, int widgetInnerHeight )
    {
        int numBars = 0;
        if ( getDisplayClutch() )
            numBars++;
        if ( getDisplayBrake() )
            numBars++;
        if ( getDisplayThrottle() )
            numBars++;
        
        if ( numBars == 0 )
        {
            texClutch = null;
            texBrake = null;
            texThrottle = null;
            
            return ( 0 );
        }
        
        final int gap = getGap();
        final int w = ( widgetInnerWidth - 6 + gap ) / numBars - gap;
        final int h = widgetInnerHeight - 6;
        
        int left = 3;
        if ( getDisplayClutch() && ( ( texClutch == null ) || ( texClutch.getWidth() != w ) || ( texClutch.getHeight() != h ) ) )
        {
            texClutch = new TransformableTexture( w, h, left, 3, 0, 0, 0f, 1f, 1f );
            left += w + gap;
        }
        
        if ( getDisplayBrake() && ( ( texBrake == null ) || ( texBrake.getWidth() != w ) || ( texBrake.getHeight() != h ) ) )
        {
            texBrake = new TransformableTexture( w, h, left, 3, 0, 0, 0f, 1f, 1f );
            left += w + gap;
        }
        
        if ( getDisplayThrottle() && ( ( texThrottle == null ) || ( texThrottle.getWidth() != w ) || ( texThrottle.getHeight() != h ) ) )
        {
            texThrottle = new TransformableTexture( w, h, left, 3, 0, 0, 0f, 1f, 1f );
            left += w + gap;
        }
        
        return ( numBars );
    }
    
    @Override
    public TransformableTexture[] getSubTextures( boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight )
    {
        int numBars = initSubTextures( widgetInnerWidth, widgetInnerHeight );
        
        if ( numBars == 0 )
            return ( null );
        
        TransformableTexture[] texs = new TransformableTexture[ numBars ];
        
        int i = 0;
        if ( getDisplayClutch() )
            texs[i++] = texClutch;
        if ( getDisplayBrake() )
            texs[i++] = texBrake;
        if ( getDisplayThrottle() )
            texs[i++] = texThrottle;
        
        return ( texs );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onBoundInputStateChanged( boolean isEditorMode, InputAction action, boolean state, int modifierMask )
    {
    }
    
    @Override
    public void onRealtimeExited( boolean isEditorMode, LiveGameData gameData )
    {
        super.onRealtimeExited( isEditorMode, gameData );
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
        initSubTextures( width, height );
        
        if ( getDisplayClutch() )
            texClutch.getTexture().clear( getClutchColor(), true, null );
        
        if ( getDisplayBrake() )
            texBrake.getTexture().clear( getBrakeColor(), true, null );
        
        if ( getDisplayThrottle() )
            texThrottle.getTexture().clear( getThrottleColor(), true, null );
    }
    
    @Override
    protected void drawWidget( boolean isEditorMode, long sessionNanos, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, boolean needsCompleteRedraw )
    {
        final TelemetryData telemData = gameData.getTelemetryData();
        float uClutch = isEditorMode ? 0.5f : telemData.getUnfilteredClutch();
        float uBrake = isEditorMode ? 0.2f : telemData.getUnfilteredBrake();
        float uThrottle = telemData.getUnfilteredThrottle();
        
        final int h = getDisplayThrottle() ? texThrottle.getHeight() : ( getDisplayBrake() ? texBrake.getHeight() : ( getDisplayClutch() ? texClutch.getHeight() : 0 ) );
        int clutch = (int)( h * uClutch );
        int brake = (int)( h * uBrake );
        int throttle = (int)( h * uThrottle );
        
        if ( getDisplayClutch() )
            texClutch.setClipRect( 0, h - clutch, texClutch.getWidth(), clutch, true );
        if ( getDisplayBrake() )
            texBrake.setClipRect( 0, h - brake, texBrake.getWidth(), brake, true );
        if ( getDisplayThrottle() )
            texThrottle.setClipRect( 0, h - throttle, texThrottle.getWidth(), throttle, true );
        
        if ( isEditorMode )
        {
            if ( getDisplayClutch() )
                texClutch.drawInEditor( texCanvas, offsetX, offsetY );
            if ( getDisplayBrake() )
                texBrake.drawInEditor( texCanvas, offsetX, offsetY );
            if ( getDisplayThrottle() )
                texThrottle.drawInEditor( texCanvas, offsetX, offsetY );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( "displayClutch", getDisplayClutch(), "Display the clutch bar?" );
        writer.writeProperty( "clutchColor", clutchColorKey, "The color used for the clutch bar in the format #RRGGBB (hex)." );
        writer.writeProperty( "displayBrake", getDisplayBrake(), "Display the brake bar?" );
        writer.writeProperty( "brakeColor", brakeColorKey, "The color used for the brake bar in the format #RRGGBB (hex)." );
        writer.writeProperty( "displayThrottle", getDisplayThrottle(), "Display the throttle bar?" );
        writer.writeProperty( "throttleColor", throttleColorKey, "The color used for the throttle bar in the format #RRGGBB (hex)." );
        writer.writeProperty( "gap", getGap(), "Gap between the bars" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( key.equals( "displayClutch" ) )
            this.displayClutch = Boolean.parseBoolean( value );
        
        else if ( key.equals( "clutchColor" ) )
            this.clutchColorKey = value;
        
        else if ( key.equals( "displayBrake" ) )
            this.displayBrake = Boolean.parseBoolean( value );
        
        else if ( key.equals( "brakeColor" ) )
            this.brakeColorKey = value;
        
        else if ( key.equals( "displayThrottle" ) )
            this.displayThrottle = Boolean.parseBoolean( value );
        
        else if ( key.equals( "throttleColor" ) )
            this.throttleColorKey = value;
        
        else if ( key.equals( "gap" ) )
            this.gap = Integer.parseInt( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( FlaggedList propsList )
    {
        super.getProperties( propsList );
        
        FlaggedList props = new FlaggedList( "Specific", true );
        
        props.add( new Property( "displayClutch", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayClutch( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayClutch() );
            }
        } );
        
        props.add( new ColorProperty( "clutchColor", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setClutchColor( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( clutchColorKey );
            }
        } );
        
        props.add( new Property( "displayBrake", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayBrake( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayBrake() );
            }
        } );
        
        props.add( new ColorProperty( "brakeColor", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setBrakeColor( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( brakeColorKey );
            }
        } );
        
        props.add( new Property( "displayThrottle", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayThrottle( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayThrottle() );
            }
        } );
        
        props.add( new ColorProperty( "throttleColor", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setThrottleColor( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( throttleColorKey );
            }
        } );
        
        props.add( new Property( "gap", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setGap( (Integer)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getGap() );
            }
        } );
        
        propsList.add( props );
    }
    
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    
    public ControlsWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.099f, Size.PERCENT_OFFSET + 0.165f );
    }
}
