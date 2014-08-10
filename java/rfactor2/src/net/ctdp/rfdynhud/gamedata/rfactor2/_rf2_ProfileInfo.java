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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import net.ctdp.rfdynhud.gamedata.ProfileInfo;
import net.ctdp.rfdynhud.input.InputMappings;
import net.ctdp.rfdynhud.input.KnownInputActions;
import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.json.JSONParser;
import org.jagatoo.util.json.ParseMeta;
import org.jagatoo.util.json.TempList;

/**
 * Model of the current player's profile information
 * 
 * @author Marvin Froehlich
 */
class _rf2_ProfileInfo extends ProfileInfo
{
    private static final FileFilter DIRECTORY_FILE_FILTER = new FileFilter()
    {
        @Override
        public boolean accept( File pathname )
        {
            return ( pathname.isDirectory() );
        }
    };
    
    private final _rf2_GameFileSystem fileSystem;
    
    private File profileFolder = null;
    private File plrFile = null;
    private long plrLastModified = -1L;
    
    private File lastUsedTrackFile = null;
    
    private Integer formationLapFlag = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isValid()
    {
        return ( plrFile != null );
    }
    
    private File findPLRFile()
    {
        File[] profileCandidates = fileSystem.getGameUserDataFolder().listFiles( DIRECTORY_FILE_FILTER );
        
        if ( profileCandidates == null )
            return ( null );
        
        File plrFile = null;
        for ( File p : profileCandidates )
        {
            File plr = new File( p, p.getName() + ".JSON" );
            if ( plr.exists() && plr.isFile() )
            {
                if ( plrFile == null )
                    plrFile = plr;
                else if ( plr.lastModified() > plrFile.lastModified() )
                    plrFile = plr;
            }
        }
        
        return ( plrFile );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset()
    {
        super.reset();
        
        profileFolder = null;
        plrFile = null;
        
        lastUsedTrackFile = null;
        formationLapFlag = 1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateImpl()
    {
        File plrFile = findPLRFile();
        
        if ( plrFile == null )
        {
            RFDHLog.error( "ERROR: No Profile with PLR file found under \"" + fileSystem.getGameUserDataFolder().getAbsolutePath() + "\". Plugin unusable!" );
            
            reset();
            return ( false );
        }
        
        if ( ( this.plrFile != null ) && plrFile.equals( this.plrFile ) && ( plrFile.lastModified() == plrLastModified ) )
        {
            return ( true );
        }
        
        reset();
        
        RFDHLog.printlnEx( "INFO: Using PLR file \"" + plrFile.getAbsolutePath() + "\"" );
        
        try
        {
            new JSONParser()
            {
                @Override
                protected Object newObject( ParseMeta meta, String name )
                {
                    return ( name );
                }
                
                @Override
                protected void addToObject( ParseMeta meta, String objectName, Object object, String childName, Object childValue )
                {
                    if ( childName.equalsIgnoreCase( "RaceCast Email" ) )
                        System.out.println( ">>>>>>>>>> " + meta.getStackSize() + ", " + objectName + ", " + childName );
                    
                    if ( meta.getStackSize() == 2 )
                    {
                        final String setting = objectName;
                        
                        if ( setting.equalsIgnoreCase( "SCENE" ) )
                        {
                            if ( childName.equalsIgnoreCase( "Scene File" ) )
                            {
                                lastUsedTrackFile = new File( (String)childValue );
                                
                                if ( !lastUsedTrackFile.isAbsolute() )
                                    lastUsedTrackFile = new File( fileSystem.getGameFolder(), (String)childValue );
                            }
                        }
                        else if ( setting.equalsIgnoreCase( "DRIVER" ) )
                        {
                            /*
                            if ( childName.equalsIgnoreCase( "Vehicle File" ) )
                            {
                                vehFile = new File( fileSystem.getGameFolder(), (String)childValue );
                            }
                            */
                            if ( childName.equalsIgnoreCase( "Team" ) )
                            {
                                teamName = (String)childValue;
                            }
                            else if ( childName.equalsIgnoreCase( "Nationality" ) )
                            {
                                nationality = (String)childValue;
                            }
                            else if ( childName.equalsIgnoreCase( "Birth Date" ) )
                            {
                                birthDate = (String)childValue;
                            }
                            else if ( childName.equalsIgnoreCase( "Location" ) )
                            {
                                location = (String)childValue;
                            }
                            else if ( childName.equalsIgnoreCase( "Game Description" ) )
                            {
                                String value = ( (String)childValue ).trim();
                                if ( value.toLowerCase().endsWith( ".rfm" ) )
                                    modName = value.substring( 0, value.length() - 4 );
                                else
                                    modName = (String)childValue;
                            }
                            else if ( childName.equalsIgnoreCase( "Helmet" ) )
                            {
                                helmet = (String)childValue;
                            }
                            else if ( childName.equalsIgnoreCase( "Unique ID" ) )
                            {
                                uniqueID = ( (Number)childValue ).intValue();
                            }
                            else if ( childName.equalsIgnoreCase( "Starting Driver" ) )
                            {
                                startingDriver = ( (Number)childValue ).intValue();
                            }
                            else if ( childName.equalsIgnoreCase( "AI Controls Driver" ) )
                            {
                                aiControlsDriver = ( (Number)childValue ).intValue();
                            }
                            else if ( childName.equalsIgnoreCase( "Driver Hotswap Delay" ) )
                            {
                                driverHotswapDelay = ( (Number)childValue ).floatValue();
                            }
                        }
                        else if ( setting.equalsIgnoreCase( "Game Options" ) )
                        {
                            if ( childName.equalsIgnoreCase( "MULTI Race Length" ) )
                            {
                                multiRaceLength = ( (Number)childValue ).floatValue();
                            }
                            else if ( childName.equalsIgnoreCase( "Show Extra Lap" ) )
                            {
                                if ( childValue instanceof Number )
                                {
                                    showCurrentLap = ( ( (Number)childValue ).intValue() != 0 );
                                }
                                else if ( childValue instanceof Boolean )
                                {
                                    showCurrentLap = (Boolean)childValue;
                                }
                                else
                                {
                                    try
                                    {
                                        int index = Integer.parseInt( String.valueOf( childValue ) );
                                        
                                        showCurrentLap = ( index != 0 );
                                    }
                                    catch ( Throwable t )
                                    {
                                        RFDHLog.debug( "Unable to parse \"Show Extra Lap\" from PLR file. Defaulting to 'true'." );
                                        showCurrentLap = true;
                                    }
                                }
                            }
                            else if ( childName.equalsIgnoreCase( "Measurement Units" ) )
                            {
                                try
                                {
                                    int index = ( (Number)childValue ).intValue();
                                    
                                    measurementUnits = MeasurementUnits.values()[index];
                                }
                                catch ( Throwable t )
                                {
                                    RFDHLog.debug( "Unable to parse \"Measurement Units\" from PLR file. Defaulting to METRIC." );
                                    measurementUnits = MeasurementUnits.METRIC;
                                }
                            }
                            else if ( childName.equalsIgnoreCase( "Speed Units" ) )
                            {
                                if ( childValue instanceof Number )
                                {
                                    int index = ( (Number)childValue ).intValue();
                                    
                                    speedUnits = SpeedUnits.values()[index];
                                }
                                else if ( childValue instanceof Boolean )
                                {
                                    Boolean bool = (Boolean)childValue;
                                    
                                    speedUnits = SpeedUnits.values()[bool ? 1 : 0];
                                }
                                else
                                {
                                    RFDHLog.debug( "Unable to parse \"Speed Units\" from PLR file. Defaulting to 'KMH'." );
                                    speedUnits = SpeedUnits.KMH;
                                }
                            }
                        }
                        else if ( setting.equalsIgnoreCase( "Race Conditions" ) )
                        {
                            if ( childName.equalsIgnoreCase( "MULTI Reconnaissance" ) )
                            {
                                numReconLaps = ( (Number)childValue ).intValue();
                            }
                            else if ( childName.equalsIgnoreCase( "MULTI Formation Lap" ) )
                            {
                                formationLapFlag = ( (Number)childValue ).intValue();
                            }
                        }
                    }
                }
                
                @Override
                protected Object[] newArray( ParseMeta meta, String name, int size )
                {
                    return ( null );
                }
                
                @Override
                protected int addToArray( ParseMeta meta, TempList list, String arrayName, Object value )
                {
                    return ( 0 );
                }
            }.parse( plrFile, "UTF-8" );
            
            this.plrFile = plrFile;
            this.profileFolder = plrFile.getParentFile();
            this.plrLastModified = plrFile.lastModified();
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        return ( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final File getProfileFolder()
    {
        return ( profileFolder );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final File getProfileFile()
    {
        return ( plrFile );
    }
    
    protected final File getLastUsedSceneFile()
    {
        // TODO
        return ( lastUsedTrackFile );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validateInputBindings( final InputMappings mappings )
    {
        File controller_ini = new File( getProfileFolder(), "Controller.JSON" );
        if ( !controller_ini.exists() )
            return ( null );
        
        final ArrayList<String> warnings = new ArrayList<String>();
        
        try
        {
            new JSONParser()
            {
                @Override
                protected Object newObject( ParseMeta meta, String name )
                {
                    return ( name );
                }
                
                private boolean equals( Object childValue, int a, int b )
                {
                    if ( !( childValue instanceof Integer[] ) )
                        return ( false );
                    
                    Integer[] value = (Integer[])childValue;
                    
                    if ( value.length != 2 )
                        return ( false );
                    
                    if ( value[0] != a )
                        return ( false );
                    
                    if ( value[1] != b )
                        return ( false );
                    
                    return ( true );
                }
                
                @Override
                protected void addToObject( ParseMeta meta, String objectName, Object object, String childName, Object childValue )
                {
                    if ( meta.getStackSize() == 2 )
                    {
                        if ( objectName.equalsIgnoreCase( "Input" ) )
                        {
                            if ( childName.equalsIgnoreCase( "Control - Increment Boost" ) && !equals( childValue, 0, 89 ) )
                            {
                                if ( !mappings.isActionMapped( KnownInputActions.IncBoost ) )
                                {
                                    String message = "No Input Binding for IncBoost, but bound in rFactor.";
                                    RFDHLog.exception( "Warning: ", message );
                                    warnings.add( message );
                                }
                            }
                            else if ( childName.equalsIgnoreCase( "Control - Decrement Boost" ) && !equals( childValue, 0, 89 ) )
                            {
                                if ( !mappings.isActionMapped( KnownInputActions.DecBoost ) )
                                {
                                    String message = "No Input Binding for DecBoost, but bound in rFactor.";
                                    RFDHLog.exception( "Warning: ", message );
                                    warnings.add( message );
                                }
                            }
                            else if ( childName.equalsIgnoreCase( "Control - Temporary Boost" ) && !equals( childValue, 0, 89 ) )
                            {
                                if ( !mappings.isActionMapped( KnownInputActions.TempBoost ) )
                                {
                                    String message = "No Input Binding for TempBoost, but bound in rFactor.";
                                    RFDHLog.exception( "Warning: ", message );
                                    warnings.add( message );
                                }
                            }
                        }
                    }
                }
                
                @Override
                protected Object[] newArray( ParseMeta meta, String name, int size )
                {
                    if ( meta.getStackSize() == 2 )
                    {
                        if ( meta.getNameStackObject( 1 ).equalsIgnoreCase( "Input" ) )
                            return ( new Object[ size ] );
                    }
                    
                    return ( null );
                }
                
                /*
                @Override
                protected int addToArray( ParseMeta meta, TempList list, String arrayName, Object value )
                {
                    return ( 0 );
                }
                */
            }.parse( controller_ini, "UTF-8" );
        }
        catch ( IOException e )
        {
            RFDHLog.exception( e );
        }
        catch ( ParsingException e )
        {
            RFDHLog.exception( e );
        }
        
        if ( warnings.isEmpty() )
            return ( null );
        
        String[] warnings2 = warnings.toArray( new String[ warnings.size() + 3 ] );
        warnings2[warnings2.length - 3] = "Engine wear display will be wrong.";
        warnings2[warnings2.length - 2] = "Edit the Input Bindings in the editor (Tools->Edit Input Bindings)";
        warnings2[warnings2.length - 1] = "and add proper bindings.";
        
        return ( warnings2 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final Boolean getFormationLap()
    {
        if ( formationLapFlag == null )
            return ( null );
        
        // 0=standing start, 1=formation lap & standing start, 2=lap behind safety car & rolling start, 3=use track default, 4=fast rolling start
        switch ( formationLapFlag.intValue() )
        {
            case 0:
                return ( false );
            case 1:
                return ( true );
            case 2:
                return ( true );
            case 3:
                return ( null );
            case 4:
                return ( true );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    /**
     * Gets the currently used CCH file.
     * 
     * @return the currently used CCH file.
     */
    protected final File getCCHFileImpl()
    {
        if ( profileFolder == null )
            return ( null );
        
        if ( modName == null )
            return ( null );
        
        return ( new File( profileFolder, modName + ".cch" ) );
    }
    
    /**
     * Create a new instance.
     * 
     * @param fileSystem
     */
    public _rf2_ProfileInfo( _rf2_GameFileSystem fileSystem )
    {
        this.fileSystem = fileSystem;
    }
}
