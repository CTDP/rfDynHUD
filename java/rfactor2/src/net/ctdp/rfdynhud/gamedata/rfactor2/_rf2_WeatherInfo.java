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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemVect3;
import net.ctdp.rfdynhud.gamedata.WeatherInfo;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf2_WeatherInfo extends WeatherInfo
{
    private final _rf2_ScoringInfo scoringInfo;
    
    @Override
    protected void updateDataImpl( Object userObject, long timestamp )
    {
    }
    
    @Override
    public void readFromStream( InputStream in, EditorPresets editorPresets ) throws IOException
    {
        final long now = System.nanoTime();
        
        //readFromStreamImpl( in );
        
        prepareDataUpdate( editorPresets, now );
        
        if ( editorPresets != null )
        {
            applyEditorPresets( editorPresets );
        }
        
        onDataUpdated( editorPresets, now );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDefaultValues( EditorPresets editorPresets )
    {
        /*
        InputStream in = null;
        
        try
        {
            in = DEFAULT_VALUES.openStream();
            
            readFromStream( in, editorPresets );
        }
        catch ( IOException e )
        {
            RFDHLog.exception( e );
        }
        finally
        {
            StreamUtils.closeStream( in );
        }
        */
    }
    
    @Override
    public void writeToStream( OutputStream out ) throws IOException
    {
        //out.write( buffer, 0, BUFFER_SIZE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getCloudDarkness()
    {
        return ( scoringInfo._getCloudDarkness() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getRainingSeverity()
    {
        return ( scoringInfo._getRainingSeverity() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getAmbientTemperatureK()
    {
        return ( scoringInfo._getAmbientTemperatureK() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getTrackTemperatureK()
    {
        return ( scoringInfo._getTrackTemperatureK() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TelemVect3 getWindSpeedMS( TelemVect3 speed )
    {
        scoringInfo._getWindSpeedMS( speed );
        
        return ( speed );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getOnPathWetness()
    {
        return ( scoringInfo._getOnPathWetness() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getOffPathWetness()
    {
        return ( scoringInfo._getOffPathWetness() );
    }
    
    _rf2_WeatherInfo( LiveGameData gameData )
    {
        super( gameData );
        
        this.scoringInfo = (_rf2_ScoringInfo)gameData.getScoringInfo();
    }
}
