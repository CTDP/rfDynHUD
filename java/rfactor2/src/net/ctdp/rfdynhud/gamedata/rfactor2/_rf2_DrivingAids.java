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
package net.ctdp.rfdynhud.gamedata.rfactor2;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.jagatoo.util.streams.StreamUtils;

import net.ctdp.rfdynhud.gamedata.ByteUtil;
import net.ctdp.rfdynhud.gamedata.DrivingAids;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.util.RFDHLog;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf2_DrivingAids extends DrivingAids
{
    private static final int NUM_AIDS = 13;
    
    private static final int OFFSET_TRACTION_CONTROL = 0;
    private static final int OFFSET_ANTI_LOCK_BRAKES = OFFSET_TRACTION_CONTROL + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_STABILITY_CONTROL = OFFSET_ANTI_LOCK_BRAKES + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_AUTO_SHIFT = OFFSET_STABILITY_CONTROL + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_AUTO_CLUTCH = OFFSET_AUTO_SHIFT + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_INVULNERABILITY = OFFSET_AUTO_CLUTCH + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_OPPOSITE_LOCK = OFFSET_INVULNERABILITY + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_STEERING_HELP = OFFSET_OPPOSITE_LOCK + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_BRAKING_HELP = OFFSET_STEERING_HELP + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_SPIN_RECOVERY = OFFSET_BRAKING_HELP + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_AUTO_PIT = OFFSET_SPIN_RECOVERY + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_AUTO_LIFT = OFFSET_AUTO_PIT + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_AUTO_BLIP = OFFSET_AUTO_LIFT + ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_FUEL_MULT = OFFSET_AUTO_BLIP + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_TIRE_MULT = OFFSET_FUEL_MULT + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_MECHANICAL_FAILURES = OFFSET_TIRE_MULT + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_ALLOW_PITCREW_PUSH = OFFSET_MECHANICAL_FAILURES + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_REPEAT_SHIFTS = OFFSET_ALLOW_PITCREW_PUSH + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_HOLD_CLUTCH = OFFSET_REPEAT_SHIFTS + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_AUTO_REVERSE = OFFSET_HOLD_CLUTCH + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_ALTERNATE_NEUTRAL = OFFSET_AUTO_REVERSE + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_MANUAL_SHIFT_OVERRIDE_TIME = OFFSET_ALTERNATE_NEUTRAL + ByteUtil.SIZE_CHAR + 3 * ByteUtil.SIZE_CHAR; // Because of special packing, we need to skip four bytes to get to next float into the next four byte slot.
    private static final int OFFSET_AUTO_SHIFT_OVERRIDE_TIME = OFFSET_MANUAL_SHIFT_OVERRIDE_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_SPEED_SESITIVE_STEERING = OFFSET_AUTO_SHIFT_OVERRIDE_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_STEER_RATIO_SPEED = OFFSET_SPEED_SESITIVE_STEERING + ByteUtil.SIZE_FLOAT;
    
    private static final int EXPANSION_SIZE = 0 * ByteUtil.SIZE_CHAR;
    
    private static final int BUFFER_SIZE = OFFSET_STEER_RATIO_SPEED + ByteUtil.SIZE_FLOAT + EXPANSION_SIZE;
    
    private final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    private static native void fetchData( final long sourceBufferAddress, final int sourceBufferSize, final byte[] targetBuffer );
    
    @Override
    protected void updateDataImpl( Object userObject, long timestamp )
    {
        _rf2_DataAddressKeeper ak = (_rf2_DataAddressKeeper)userObject;
        
        fetchData( ak.getBufferAddress(), ak.getBufferSize(), buffer );
    }
    
    private void readFromStreamImpl( InputStream in ) throws IOException
    {
        int offset = 0;
        int bytesToRead = BUFFER_SIZE;
        
        while ( bytesToRead > 0 )
        {
            int n = in.read( buffer, offset, bytesToRead );
            
            if ( n < 0 )
                throw new IOException();
            
            offset += n;
            bytesToRead -= n;
        }
    }
    
    @Override
    public void readFromStream( InputStream in, boolean isEditorMode ) throws IOException
    {
        final long now = System.nanoTime();
        
        prepareDataUpdate( null, now );
        
        readFromStreamImpl( in );
        
        onDataUpdated( null, now, isEditorMode );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void readDefaultValues( boolean isEditorMode ) throws IOException
    {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream( this.getClass().getPackage().getName().replace( '.', '/' ) + "/data/game_data/driving_aids" );
        
        try
        {
            readFromStream( in, isEditorMode );
        }
        finally
        {
            if ( in != null )
                StreamUtils.closeStream( in );
        }
    }
    
    @Override
    public void writeToStream( OutputStream out ) throws IOException
    {
        out.write( buffer, 0, BUFFER_SIZE );
    }
    
    public final byte getTractionControl()
    {
        // unsigned char mTractionControl;  // 0 (off) - 3 (high)
        
        return ( ByteUtil.readByte( buffer, OFFSET_TRACTION_CONTROL ) );
    }
    
    public final byte getAntiLockBrakes()
    {
        // unsigned char mAntiLockBrakes;   // 0 (off) - 2 (high)
        
        return ( ByteUtil.readByte( buffer, OFFSET_ANTI_LOCK_BRAKES ) );
    }
    
    public final byte getStabilityControl()
    {
        // unsigned char mStabilityControl; // 0 (off) - 2 (high)
        
        return ( ByteUtil.readByte( buffer, OFFSET_STABILITY_CONTROL ) );
    }
    
    public final byte getAutoShift()
    {
        // unsigned char mAutoShift;        // 0 (off), 1 (upshifts), 2 (downshifts), 3 (all)
        
        return ( ByteUtil.readByte( buffer, OFFSET_AUTO_SHIFT ) );
    }
    
    public final boolean getAutoClutch()
    {
        // unsigned char mAutoClutch;       // 0 (off), 1 (on)
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_AUTO_CLUTCH ) );
    }
    
    public final boolean getInvulnerability()
    {
        // unsigned char mInvulnerable;     // 0 (off), 1 (on)
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_INVULNERABILITY ) );
    }
    
    public final boolean getOppositeLock()
    {
        // unsigned char mOppositeLock;     // 0 (off), 1 (on)
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_OPPOSITE_LOCK ) );
    }
    
    public final byte getSteeringHelp()
    {
        // unsigned char mSteeringHelp;     // 0 (off) - 3 (high)
        
        return ( ByteUtil.readByte( buffer, OFFSET_STEERING_HELP ) );
    }
    
    public final byte getBrakingHelp()
    {
        // unsigned char mBrakingHelp;      // 0 (off) - 2 (high)
        
        return ( ByteUtil.readByte( buffer, OFFSET_BRAKING_HELP ) );
    }
    
    public final boolean getSpinRecovery()
    {
        // unsigned char mSpinRecovery;     // 0 (off), 1 (on)
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_SPIN_RECOVERY ) );
    }
    
    public final boolean getAutoPit()
    {
        // unsigned char mAutoPit;          // 0 (off), 1 (on)
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_AUTO_PIT ) );
    }
    
    public final boolean getAutoLift()
    {
        // unsigned char mAutoLift;         // 0 (off), 1 (on)
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_AUTO_LIFT ) );
    }
    
    public final boolean getAutoBlip()
    {
        // unsigned char mAutoBlip;         // 0 (off), 1 (on)
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_AUTO_BLIP ) );
    }
    
    public final float getFuelMultiplier()
    {
        // unsigned char mFuelMult;         // fuel multiplier (0x-7x)
        
        return ( ByteUtil.readByte( buffer, OFFSET_FUEL_MULT ) );
    }
    
    public final float getTireWearMultiplier()
    {
        // unsigned char mTireMult;         // tire wear multiplier (0x-7x)
        
        return ( ByteUtil.readByte( buffer, OFFSET_TIRE_MULT ) );
    }
    
    public final byte getMechanicalFailureSetting()
    {
        // unsigned char mMechFail;         // mechanical failure setting; 0 (off), 1 (normal), 2 (timescaled)
        
        return ( ByteUtil.readByte( buffer, OFFSET_MECHANICAL_FAILURES ) );
    }
    
    public final boolean getAllowPitCrewPush()
    {
        // unsigned char mAllowPitcrewPush; // 0 (off), 1 (on)
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_ALLOW_PITCREW_PUSH ) );
    }
    
    public final byte getRepeatShiftsPrevention()
    {
        // unsigned char mRepeatShifts;     // accidental repeat shift prevention (0-5; see PLR file)
        
        return ( ByteUtil.readByte( buffer, OFFSET_REPEAT_SHIFTS ) );
    }
    
    public final boolean getHoldClutch()
    {
        // unsigned char mHoldClutch;       // for auto-shifters at start of race: 0 (off), 1 (on)
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_HOLD_CLUTCH ) );
    }
    
    public final boolean getAutoReverse()
    {
        // unsigned char mAutoReverse;      // 0 (off), 1 (on)
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_AUTO_REVERSE ) );
    }
    
    public final boolean getAlternateNeutral()
    {
        // unsigned char mAlternateNeutral; // Whether shifting up and down simultaneously equals neutral
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_ALTERNATE_NEUTRAL ) );
    }
    
    public final float getManualShiftOverrideTime()
    {
        // float mManualShiftOverrideTime;  // time before auto-shifting can resume after recent manual shift
        
        return ( ByteUtil.readFloat( buffer, OFFSET_MANUAL_SHIFT_OVERRIDE_TIME ) );
    }
    
    public final float getAutoShiftOverrideTime()
    {
        // float mAutoShiftOverrideTime;    // time before manual shifting can resume after recent auto shift
        
        return ( ByteUtil.readFloat( buffer, OFFSET_AUTO_SHIFT_OVERRIDE_TIME ) );
    }
    
    public final float getSpeedSensitiveSteering()
    {
        // float mSpeedSensitiveSteering;   // 0.0 (off) - 1.0
        
        return ( ByteUtil.readFloat( buffer, OFFSET_SPEED_SESITIVE_STEERING ) );
    }
    
    public final float getSteerRatioSpeed()
    {
        // float mSteerRatioSpeed;          // speed (m/s) under which lock gets expanded to full
        
        return ( ByteUtil.readFloat( buffer, OFFSET_STEER_RATIO_SPEED ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getNumAids()
    {
        return ( NUM_AIDS );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getAidIndexTractionControl()
    {
        return ( 0 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getAidIndexTractionAntiLockBrakes()
    {
        return ( 1 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getAidIndexAutoShift()
    {
        return ( 2 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getAidIndexInvulnerability()
    {
        return ( 3 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final String getAidName( int index )
    {
        // TODO: Translate!
        
        switch ( index )
        {
            case 0: // mTractionControl;  // 0 (off) - 3 (high)
                return ( "Traction Control" );
            case 1: // mAntiLockBrakes;   // 0 (off) - 2 (high)
                return ( "Anti Lock Brakes" );
            case 2: // mStabilityControl; // 0 (off) - 2 (high)
                return ( "Stability Control" );
            case 3: // mAutoShift;        // 0 (off), 1 (upshifts), 2 (downshifts), 3 (all)
                return ( "Auto Shift" );
            case 4: // mAutoClutch;       // 0 (off), 1 (on)
                return ( "Auto Clutch" );
            case 5: // mInvulnerable;     // 0 (off), 1 (on)
                return ( "Invulnerability" );
            case 6: // mOppositeLock;     // 0 (off), 1 (on)
                return ( "Opposite Lock" );
            case 7: // mSteeringHelp;     // 0 (off) - 3 (high)
                return ( "Steering Help" );
            case 8: // mBrakingHelp;      // 0 (off) - 2 (high)
                return ( "Braking Help" );
            case 9: // mSpinRecovery;     // 0 (off), 1 (on)
                return ( "Sping Recovery" );
            case 10: // mAutoPit;          // 0 (off), 1 (on)
                return ( "Auto Pit" );
            case 11: // mAutoLift;         // 0 (off), 1 (on)
                return ( "Auto Lift" );
            case 12: // mAutoBlip;         // 0 (off), 1 (on)
                return ( "Auto Blip" );
        }
        
        throw new IllegalArgumentException( "There is no aid with the index " + index + ". Only " + getNumAids() + " available." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getAidState( int index )
    {
        return ( ByteUtil.readByte( buffer, OFFSET_TRACTION_CONTROL + index ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getAidStateName( int index, int state )
    {
        // TODO: Translate!
        
        switch ( index )
        {
            case 0: // mTractionControl;  // 0 (off) - 3 (high)
                switch ( state )
                {
                    case 0:
                        return ( "Off" );
                    case 1:
                        return ( "Low" );
                    case 2:
                        return ( "Medium" );
                    case 3:
                        return ( "High" );
                }
            case 1: // mAntiLockBrakes;   // 0 (off) - 2 (high)
                switch ( state )
                {
                    case 0:
                        return ( "Off" );
                    case 1:
                        return ( "Low" );
                    case 2:
                        return ( "High" );
                }
            case 2: // mStabilityControl; // 0 (off) - 2 (high)
                switch ( state )
                {
                    case 0:
                        return ( "Off" );
                    case 1:
                        return ( "Low" );
                    case 2:
                        return ( "High" );
                }
            case 3: // mAutoShift;        // 0 (off), 1 (upshifts), 2 (downshifts), 3 (all)
                switch ( state )
                {
                    case 0:
                        return ( "Off" );
                    case 1:
                        return ( "Up" );
                    case 2:
                        return ( "Down" );
                    case 3:
                        return ( "All" );
                }
            case 4: // mAutoClutch;       // 0 (off), 1 (on)
                return ( state == 0 ? "Off" : "On" );
            case 5: // mInvulnerable;     // 0 (off), 1 (on)
                return ( state == 0 ? "Off" : "On" );
            case 6: // mOppositeLock;     // 0 (off), 1 (on)
                return ( state == 0 ? "Off" : "On" );
            case 7: // mSteeringHelp;     // 0 (off) - 3 (high)
                switch ( state )
                {
                    case 0:
                        return ( "Off" );
                    case 1:
                        return ( "Low" );
                    case 2:
                        return ( "Medium" );
                    case 3:
                        return ( "High" );
                }
            case 8: // mBrakingHelp;      // 0 (off) - 2 (high)
                switch ( state )
                {
                    case 0:
                        return ( "Off" );
                    case 1:
                        return ( "Low" );
                    case 3:
                        return ( "High" );
                }
            case 9: // mSpinRecovery;     // 0 (off), 1 (on)
                return ( state == 0 ? "Off" : "On" );
            case 10: // mAutoPit;          // 0 (off), 1 (on)
                return ( state == 0 ? "Off" : "On" );
            case 11: // mAutoLift;         // 0 (off), 1 (on)
                return ( state == 0 ? "Off" : "On" );
            case 12: // mAutoBlip;         // 0 (off), 1 (on)
                return ( state == 0 ? "Off" : "On" );
        }
        
        throw new IllegalArgumentException( "There is no aid with the index " + index + ". Only " + getNumAids() + " available." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMinState( int index )
    {
        switch ( index )
        {
            case 0: // mTractionControl;  // 0 (off) - 3 (high)
                return ( 0 );
            case 1: // mAntiLockBrakes;   // 0 (off) - 2 (high)
                return ( 0 );
            case 2: // mStabilityControl; // 0 (off) - 2 (high)
                return ( 0 );
            case 3: // mAutoShift;        // 0 (off), 1 (upshifts), 2 (downshifts), 3 (all)
                return ( 0 );
            case 4: // mAutoClutch;       // 0 (off), 1 (on)
                return ( 0 );
            case 5: // mInvulnerable;     // 0 (off), 1 (on)
                return ( 0 );
            case 6: // mOppositeLock;     // 0 (off), 1 (on)
                return ( 0 );
            case 7: // mSteeringHelp;     // 0 (off) - 3 (high)
                return ( 0 );
            case 8: // mBrakingHelp;      // 0 (off) - 2 (high)
                return ( 0 );
            case 9: // mSpinRecovery;     // 0 (off), 1 (on)
                return ( 0 );
            case 10: // mAutoPit;          // 0 (off), 1 (on)
                return ( 0 );
            case 11: // mAutoLift;         // 0 (off), 1 (on)
                return ( 0 );
            case 12: // mAutoBlip;         // 0 (off), 1 (on)
                return ( 0 );
        }
        
        throw new IllegalArgumentException( "There is no aid with the index " + index + ". Only " + getNumAids() + " available." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMaxState( int index )
    {
        switch ( index )
        {
            case 0: // mTractionControl;  // 0 (off) - 3 (high)
                return ( 3 );
            case 1: // mAntiLockBrakes;   // 0 (off) - 2 (high)
                return ( 2 );
            case 2: // mStabilityControl; // 0 (off) - 2 (high)
                return ( 2 );
            case 3: // mAutoShift;        // 0 (off), 1 (upshifts), 2 (downshifts), 3 (all)
                return ( 3 );
            case 4: // mAutoClutch;       // 0 (off), 1 (on)
                return ( 1 );
            case 5: // mInvulnerable;     // 0 (off), 1 (on)
                return ( 1 );
            case 6: // mOppositeLock;     // 0 (off), 1 (on)
                return ( 1 );
            case 7: // mSteeringHelp;     // 0 (off) - 3 (high)
                return ( 3 );
            case 8: // mBrakingHelp;      // 0 (off) - 2 (high)
                return ( 1 );
            case 9: // mSpinRecovery;     // 0 (off), 1 (on)
                return ( 1 );
            case 10: // mAutoPit;          // 0 (off), 1 (on)
                return ( 1 );
            case 11: // mAutoLift;         // 0 (off), 1 (on)
                return ( 1 );
            case 12: // mAutoBlip;         // 0 (off), 1 (on)
                return ( 1 );
        }
        
        throw new IllegalArgumentException( "There is no aid with the index " + index + ". Only " + getNumAids() + " available." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getNumStates( int index )
    {
        switch ( index )
        {
            case 0: // mTractionControl;  // 0 (off) - 3 (high)
                return ( 4 );
            case 1: // mAntiLockBrakes;   // 0 (off) - 2 (high)
                return ( 3 );
            case 2: // mStabilityControl; // 0 (off) - 2 (high)
                return ( 3 );
            case 3: // mAutoShift;        // 0 (off), 1 (upshifts), 2 (downshifts), 3 (all)
                return ( 4 );
            case 4: // mAutoClutch;       // 0 (off), 1 (on)
                return ( 2 );
            case 5: // mInvulnerable;     // 0 (off), 1 (on)
                return ( 2 );
            case 6: // mOppositeLock;     // 0 (off), 1 (on)
                return ( 2 );
            case 7: // mSteeringHelp;     // 0 (off) - 3 (high)
                return ( 4 );
            case 8: // mBrakingHelp;      // 0 (off) - 2 (high)
                return ( 3 );
            case 9: // mSpinRecovery;     // 0 (off), 1 (on)
                return ( 2 );
            case 10: // mAutoPit;          // 0 (off), 1 (on)
                return ( 2 );
            case 11: // mAutoLift;         // 0 (off), 1 (on)
                return ( 2 );
            case 12: // mAutoBlip;         // 0 (off), 1 (on)
                return ( 2 );
        }
        
        throw new IllegalArgumentException( "There is no aid with the index " + index + ". Only " + getNumAids() + " available." );
    }
    
    private static final ImageTemplate getIcon( String filename )
    {
        java.net.URL resource = _rf2_DrivingAids.class.getClassLoader().getResource( _rf2_DrivingAids.class.getPackage().getName().replace( '.', '/' ) + "/data/drivingaids_images/" + filename + ".png" );
        
        try
        {
            if ( resource == null )
            {
                RFDHLog.error( "Driving aid image \"" + filename + "\" not found." );
                
                return ( null );
            }
            
            BufferedImage bi = ImageIO.read( resource );
            
            return ( new ImageTemplate( filename, bi ) );
        }
        catch ( IOException e )
        {
            RFDHLog.error( e );
            
            return ( null );
        }
    }
    
    private static final ImageTemplate icon_traction_control_off = getIcon( "tc-off" );
    private static final ImageTemplate icon_traction_control_low = getIcon( "tc-low" );
    private static final ImageTemplate icon_traction_control_medium = getIcon( "tc-medium" );
    private static final ImageTemplate icon_traction_control_high = getIcon( "tc-high" );
    private static final ImageTemplate icon_anti_lock_brakes_off = getIcon( "als-off" );
    private static final ImageTemplate icon_anti_lock_brakes_low = getIcon( "als-low" );
    private static final ImageTemplate icon_anti_lock_brakes_high = getIcon( "als-high" );
    private static final ImageTemplate icon_stability_control_off = getIcon( "stab_ctrl-off" );
    private static final ImageTemplate icon_stability_control_low = getIcon( "stab_ctrl-low" );
    private static final ImageTemplate icon_stability_control_high = getIcon( "stab_ctrl-high" );
    private static final ImageTemplate icon_auto_shift_off = getIcon( "auto_shift-off" );
    private static final ImageTemplate icon_auto_shift_up = getIcon( "auto_shift-up" );
    private static final ImageTemplate icon_auto_shift_down = getIcon( "auto_shift-down" );
    private static final ImageTemplate icon_auto_shift_all = getIcon( "auto_shift-all" );
    private static final ImageTemplate icon_auto_clutch_off = getIcon( "auto_clutch-off" );
    private static final ImageTemplate icon_auto_clutch_on = getIcon( "auto_clutch-on" );
    private static final ImageTemplate icon_invulnerability_off = getIcon( "invulnerability-off" );
    private static final ImageTemplate icon_invulnerability_on = getIcon( "invulnerability-on" );
    private static final ImageTemplate icon_opposite_lock_off = getIcon( "opposite_lock-off" );
    private static final ImageTemplate icon_opposite_lock_on = getIcon( "opposite_lock-on" );
    private static final ImageTemplate icon_steering_help_off = getIcon( "steering_help-off" );
    private static final ImageTemplate icon_steering_help_low = getIcon( "steering_help-low" );
    private static final ImageTemplate icon_steering_help_medium = getIcon( "steering_help-medium" );
    private static final ImageTemplate icon_steering_help_high = getIcon( "steering_help-high" );
    private static final ImageTemplate icon_braking_help_off = getIcon( "braking_help-off" );
    private static final ImageTemplate icon_braking_help_low = getIcon( "braking_help-low" );
    private static final ImageTemplate icon_braking_help_high = getIcon( "braking_help-high" );
    private static final ImageTemplate icon_spin_recovery_off = getIcon( "spin_recovery-off" );
    private static final ImageTemplate icon_spin_recovery_on = getIcon( "spin_recovery-on" );
    private static final ImageTemplate icon_auto_pit_off = getIcon( "auto_pit-off" );
    private static final ImageTemplate icon_auto_pit_on = getIcon( "auto_pit-on" );
    private static final ImageTemplate icon_auto_lift_off = getIcon( "auto_lift-off" );
    private static final ImageTemplate icon_auto_lift_on = getIcon( "auto_lift-on" );
    private static final ImageTemplate icon_auto_blip_off = getIcon( "auto_blip-off" );
    private static final ImageTemplate icon_auto_blip_on = getIcon( "auto_blip-on" );
    
    private static final ImageTemplate getIcon( int index, int state )
    {
        switch ( index )
        {
            case 0: // mTractionControl;  // 0 (off) - 3 (high)
                switch ( state )
                {
                    case 0:
                        return ( icon_traction_control_off );
                    case 1:
                        return ( icon_traction_control_low );
                    case 2:
                        return ( icon_traction_control_medium );
                    case 3:
                        return ( icon_traction_control_high );
                }
            case 1: // mAntiLockBrakes;   // 0 (off) - 2 (high)
                switch ( state )
                {
                    case 0:
                        return ( icon_anti_lock_brakes_off );
                    case 1:
                        return ( icon_anti_lock_brakes_low );
                    case 2:
                        return ( icon_anti_lock_brakes_high );
                }
            case 2: // mStabilityControl; // 0 (off) - 2 (high)
                switch ( state )
                {
                    case 0:
                        return ( icon_stability_control_off );
                    case 1:
                        return ( icon_stability_control_low );
                    case 2:
                        return ( icon_stability_control_high );
                }
            case 3: // mAutoShift;        // 0 (off), 1 (upshifts), 2 (downshifts), 3 (all)
                switch ( state )
                {
                    case 0:
                        return ( icon_auto_shift_off );
                    case 1:
                        return ( icon_auto_shift_up );
                    case 2:
                        return ( icon_auto_shift_down );
                    case 3:
                        return ( icon_auto_shift_all );
                }
            case 4: // mAutoClutch;       // 0 (off), 1 (on)
                return ( state == 0 ? icon_auto_clutch_off : icon_auto_clutch_on );
            case 5: // mInvulnerable;     // 0 (off), 1 (on)
                return ( state == 0 ? icon_invulnerability_off : icon_invulnerability_on );
            case 6: // mOppositeLock;     // 0 (off), 1 (on)
                return ( state == 0 ? icon_opposite_lock_off : icon_opposite_lock_on );
            case 7: // mSteeringHelp;     // 0 (off) - 3 (high)
                switch ( state )
                {
                    case 0:
                        return ( icon_steering_help_off );
                    case 1:
                        return ( icon_steering_help_low );
                    case 2:
                        return ( icon_steering_help_medium );
                    case 3:
                        return ( icon_steering_help_high );
                }
            case 8: // mBrakingHelp;      // 0 (off) - 2 (high)
                switch ( state )
                {
                    case 0:
                        return ( icon_braking_help_off );
                    case 1:
                        return ( icon_braking_help_low );
                    case 3:
                        return ( icon_braking_help_high );
                }
            case 9: // mSpinRecovery;     // 0 (off), 1 (on)
                return ( state == 0 ? icon_spin_recovery_off : icon_spin_recovery_on );
            case 10: // mAutoPit;          // 0 (off), 1 (on)
                return ( state == 0 ? icon_auto_pit_off : icon_auto_pit_on );
            case 11: // mAutoLift;         // 0 (off), 1 (on)
                return ( state == 0 ? icon_auto_lift_off : icon_auto_lift_on );
            case 12: // mAutoBlip;         // 0 (off), 1 (on)
                return ( state == 0 ? icon_auto_blip_off : icon_auto_blip_on );
        }
        
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ImageTemplate getAidIcon( int index, int state )
    {
        ImageTemplate icon = getIcon( index, state );
        
        if ( icon == null )
            RFDHLog.error( "No icon found for driving aid \"" + getAidName( index ) + "\" (index: " + index + ") and state " + state + "." );
        
        return ( icon );
    }
    
    _rf2_DrivingAids( LiveGameData gameData )
    {
        super( gameData );
    }
}
