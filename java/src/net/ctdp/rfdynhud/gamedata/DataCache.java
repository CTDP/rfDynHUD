package net.ctdp.rfdynhud.gamedata;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.util.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class DataCache implements LiveGameData.GameDataUpdateListener
{
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final SAXParserFactory SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
    
    private static class VersionException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        
        public VersionException( String message )
        {
            super( message );
        }
        
        public static final void checkVersion( String version, int maxMajor, int maxMinor, int maxRevision ) throws VersionException
        {
            int[] ver = null;
            try
            {
                String[] ss = version.split( "\\." );
                ver = new int[ ss.length ];
                for ( int i = 0; i < ss.length; i++ )
                    ver[i] = Integer.parseInt( ss[i] );
            }
            catch ( Throwable t )
            {
                throw new VersionException( "Error parsing version from configuration file." );
            }
            
            int[] testVer = { maxMajor, maxMinor, maxRevision };
            
            boolean isNewer = false;
            
            for ( int i = 0; i < ver.length && !isNewer; i++ )
            {
                int tv = ( testVer.length > i ) ? testVer[i] : 0;
                
                if ( ver[i] > tv )
                    isNewer = true;
            }
            
            if ( isNewer )
                throw new VersionException( "The configuration file has a newer format than this version of the Mod Deriver is able to handle." );
        }
    }
    
    private final HashMap<String, Float> fuelUsages = new HashMap<String, Float>();
    private final HashMap<String, Laptime> fastestLaptimes = new HashMap<String, Laptime>();
    
    final Float getFuelUsage( String teamName )
    {
        return ( fuelUsages.get( teamName ) );
    }
    
    final Laptime getFastestLaptime( String teamName )
    {
        return ( fastestLaptimes.get( teamName ) );
    }
    
    private static final boolean checkSessionType( ScoringInfo scoringInfo )
    {
        SessionType sessionType = scoringInfo.getSessionType();
        
        if ( sessionType == SessionType.TEST_DAY )
            return ( true );
        
        if ( sessionType == SessionType.PRACTICE1 )
            return ( scoringInfo.getNumVehicles() == 1 );
        
        return ( false );
    }
    
    private static File getCacheFile( String modName, String trackName, boolean createFolder )
    {
        File cacheFolder = GameFileSystem.INSTANCE.getCacheFolder();
        
        if ( cacheFolder == null )
            return ( null );
        
        cacheFolder = new File( new File( cacheFolder, "data" ), modName );
        if ( createFolder )
        {
            try
            {
                cacheFolder.mkdirs();
            }
            catch ( Throwable t )
            {
                Logger.log( "Warning: Failed to create cache folder. Data-Cache deactivated." );
            }
        }
        else if ( !cacheFolder.exists() )
        {
            return ( null );
        }
        
        return ( new File( cacheFolder, trackName + ".xml" ) );
    }
    
    private static File getCacheFile( LiveGameData gameData, boolean createFolder )
    {
        return ( getCacheFile( gameData.getModInfo().getName(), gameData.getScoringInfo().getTrackName(), createFolder ) );
    }
    
    private void loadFromCache( File cacheFile )
    {
        org.xml.sax.helpers.DefaultHandler handler = new org.xml.sax.helpers.DefaultHandler()
        {
            private int level = 0;
            
            private String currentVehicle = null;
            
            @Override
            public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
            {
                //System.out.println( "startElement( " + uri + ", " + localName + ", " + qName + ", " + attributes + " )" );
                
                if ( ( level == 0 ) && qName.equals( "CachedData" ) )
                {
                    VersionException.checkVersion( attributes.getValue( "version" ), 1, 1, 0 );
                }
                else if ( ( level == 1 ) && qName.equals( "VehicleData" ) )
                {
                    currentVehicle = attributes.getValue( "vehicle" );
                }
                else if ( ( level == 2 ) && qName.equals( "FuelUsage" ) )
                {
                    float avgFuelUsage = Float.parseFloat( attributes.getValue( "average" ) );
                    fuelUsages.put( currentVehicle, avgFuelUsage );
                }
                else if ( ( level == 2 ) && qName.equals( "FastestLap" ) )
                {
                    //String type = attributes.getValue( "type" );
                    float sector1 = Float.parseFloat( attributes.getValue( "sector1" ) );
                    float sector2 = Float.parseFloat( attributes.getValue( "sector2" ) );
                    float sector3 = Float.parseFloat( attributes.getValue( "sector3" ) );
                    float lap = Float.parseFloat( attributes.getValue( "lap" ) );
                    
                    Laptime laptime = new Laptime( 0, sector1, sector2, sector3, false, false, true );
                    laptime.laptime = lap;
                    
                    fastestLaptimes.put( currentVehicle, laptime );
                }
                
                level++;
            }
            
            @Override
            public void characters( char[] data, int start, int length ) throws SAXException
            {
                //System.out.println( "data: " + new String( data, start, length ) );
            }
            
            @Override
            public void endElement( String uri, String localName, String qName ) throws SAXException
            {
                //System.out.println( "endElement( " + uri + ", " + localName + ", " + qName + " )" );
                
                level--;
                
                if ( ( level == 1 ) && qName.equals( "VehicleData" ) )
                {
                    currentVehicle = null;
                }
            }
            
            @Override
            public void warning( SAXParseException e ) throws SAXException
            {
                //System.err.println( "Warning at: " + getCurrentPathAsString() );
                
                Logger.log( e );
            }
            
            @Override
            public void error( SAXParseException e ) throws SAXException
            {
                //System.err.println( "Error at: " + getCurrentPathAsString() );
                
                Logger.log( e );
            }
            
            @Override
            public void fatalError( SAXParseException e ) throws SAXException
            {
                //System.err.println( "Warning at: " + getCurrentPathAsString() );
                
                Logger.log( e );
            }
        };
        
        BufferedInputStream in = null;
        
        try
        {
            in = new BufferedInputStream( new FileInputStream( cacheFile ) );
            
            SAXParser saxParser = SAX_PARSER_FACTORY.newSAXParser();
            saxParser.parse( in, handler );
        }
        catch ( ParserConfigurationException e )
        {
            Logger.log( e );
        }
        catch ( SAXException e )
        {
            Logger.log( e );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
        catch ( VersionException e )
        {
            Logger.log( "Error: " + e.getMessage() );
        }
        finally
        {
            if ( in != null )
                try { in.close(); } catch ( IOException e ) {}
        }
    }
    
    static Float loadFuelUsageFromCache( final String modName, final String trackName, final String teamName )
    {
        File cacheFile = getCacheFile( modName, trackName, false );
        
        if ( cacheFile == null )
            return ( null );
        
        final Float[] result = { null };
        
        org.xml.sax.helpers.DefaultHandler handler = new org.xml.sax.helpers.DefaultHandler()
        {
            private int level = 0;
            
            private String currentVehicle = null;
            
            @Override
            public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
            {
                //System.out.println( "startElement( " + uri + ", " + localName + ", " + qName + ", " + attributes + " )" );
                
                if ( ( level == 0 ) && qName.equals( "CachedData" ) )
                {
                    VersionException.checkVersion( attributes.getValue( "version" ), 1, 1, 0 );
                }
                else if ( ( level == 1 ) && qName.equals( "VehicleData" ) )
                {
                    currentVehicle = attributes.getValue( "vehicle" );
                }
                else if ( ( level == 2 ) && qName.equals( "FuelUsage" ) )
                {
                    float avgFuelUsage = Float.parseFloat( attributes.getValue( "average" ) );
                    if ( teamName.equals( currentVehicle ) )
                        result[0] = avgFuelUsage;
                }
                
                level++;
            }
            
            @Override
            public void characters( char[] data, int start, int length ) throws SAXException
            {
                //System.out.println( "data: " + new String( data, start, length ) );
            }
            
            @Override
            public void endElement( String uri, String localName, String qName ) throws SAXException
            {
                //System.out.println( "endElement( " + uri + ", " + localName + ", " + qName + " )" );
                
                level--;
                
                if ( ( level == 1 ) && qName.equals( "VehicleData" ) )
                {
                    currentVehicle = null;
                }
            }
            
            @Override
            public void warning( SAXParseException e ) throws SAXException
            {
                //System.err.println( "Warning at: " + getCurrentPathAsString() );
                
                Logger.log( e );
            }
            
            @Override
            public void error( SAXParseException e ) throws SAXException
            {
                //System.err.println( "Error at: " + getCurrentPathAsString() );
                
                Logger.log( e );
            }
            
            @Override
            public void fatalError( SAXParseException e ) throws SAXException
            {
                //System.err.println( "Warning at: " + getCurrentPathAsString() );
                
                Logger.log( e );
            }
        };
        
        BufferedInputStream in = null;
        
        try
        {
            in = new BufferedInputStream( new FileInputStream( cacheFile ) );
            
            SAXParser saxParser = SAX_PARSER_FACTORY.newSAXParser();
            saxParser.parse( in, handler );
        }
        catch ( ParserConfigurationException e )
        {
            Logger.log( e );
        }
        catch ( SAXException e )
        {
            Logger.log( e );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
        catch ( VersionException e )
        {
            Logger.log( "Error: " + e.getMessage() );
        }
        finally
        {
            if ( in != null )
                try { in.close(); } catch ( IOException e ) {}
        }
        
        return ( result[0] );
    }
    
    @Override
    public void onSessionStarted( LiveGameData gameData, EditorPresets editorPresets )
    {
        fuelUsages.clear();
        fastestLaptimes.clear();
        
        if ( !checkSessionType( gameData.getScoringInfo() ) )
            return;
        
        File cacheFile = getCacheFile( gameData, false );
        
        if ( ( cacheFile == null ) || !cacheFile.exists() )
            return;
        
        loadFromCache( cacheFile );
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets ) {}
    
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, EditorPresets editorPresets, boolean isPaused ) {}
    
    private void storeToCache( File cacheFile )
    {
        ArrayList<String> vehicleNames = new ArrayList<String>( fuelUsages.keySet() );
        Collections.sort( vehicleNames );
        
        BufferedWriter bw = null;
        
        try
        {
            bw = new BufferedWriter( new FileWriter( cacheFile ) );
            
            bw.write( XML_HEADER );
            bw.newLine();
            bw.newLine();
            
            bw.write( "<CachedData version=\"1.1.0\">" );
            bw.newLine();
            
            for ( String vehicleName : vehicleNames )
            {
                float fuelUsage = fuelUsages.get( vehicleName );
                Laptime laptime = fastestLaptimes.get( vehicleName );
                
                bw.write( "    <VehicleData vehicle=\"" + vehicleName + "\">" );
                bw.newLine();
                bw.write( "        <FuelUsage average=\"" + fuelUsage + "\" />" );
                bw.newLine();
                if ( laptime != null )
                {
                    bw.write( "        <FastestLap type=\"normal\" sector1=\"" + laptime.getSector1() + "\" sector2=\"" + laptime.getSector2() + "\" sector3=\"" + laptime.getSector3() + "\" lap=\"" + laptime.getLapTime() + "\" />" );
                    bw.newLine();
                }
                bw.write( "    </VehicleData>" );
                bw.newLine();
            }
            
            bw.write( "</CachedData>" );
            bw.newLine();
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
        finally
        {
            if ( bw != null )
                try { bw.close(); } catch ( IOException e ) {}
        }
    }
    
    @Override
    public void onRealtimeExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        if ( !checkSessionType( gameData.getScoringInfo() ) )
            return;
        
        VehicleScoringInfo player = gameData.getScoringInfo().getPlayersVehicleScoringInfo();
        //String teamName = player.getVehicleName();
        String teamName = gameData.getProfileInfo().getTeamName();
        
        float avgFuelUsage = FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.getAverage();
        if ( avgFuelUsage > 0f )
            fuelUsages.put( teamName, avgFuelUsage );
        
        fastestLaptimes.put( teamName, player.getFastestLaptime() );
        
        File cacheFile = getCacheFile( gameData, true );
        
        if ( cacheFile == null )
            return;
        
        storeToCache( cacheFile );
    }
    
    private DataCache()
    {
    }
    
    static final DataCache INSTANCE = new DataCache();
}
