package net.ctdp.rfdynhud.gamedata;

/**
 * 
 * @author Marvin Froehlich
 */
public class TelemVect3
{
    float x, y, z;
    
    public final float getX()
    {
        return ( x );
    }
    
    public final float getY()
    {
        return ( y );
    }
    
    public final float getZ()
    {
        return ( z );
    }
    
    public final float getLengthSquared()
    {
        return ( x * x + y * y + z * z );
    }
    
    public final float getLength()
    {
        return ( (float)Math.sqrt( getLengthSquared() ) );
    }
    
    public final float getDistanceToSquared( TelemVect3 pos2 )
    {
        float d = 0.0f;
        float tmp = this.x - pos2.x;
        d += tmp * tmp;
        tmp = this.y - pos2.y;
        d += tmp * tmp;
        tmp = this.z - pos2.z;
        d += tmp * tmp;
        
        return ( d );
    }
    
    public final float getDistanceTo( TelemVect3 pos2 )
    {
        return ( (float)Math.sqrt( getDistanceToSquared( pos2 ) ) );
    }
    
    public final float getDistanceToSquared( float x, float y, float z )
    {
        float d = 0.0f;
        float tmp = this.x - x;
        d += tmp * tmp;
        tmp = this.y - y;
        d += tmp * tmp;
        tmp = this.z - z;
        d += tmp * tmp;
        
        return ( d );
    }
    
    public final float getDistanceTo( float x, float y, float z )
    {
        return ( (float)Math.sqrt( getDistanceToSquared( x, y, z ) ) );
    }
    
    public final float getDistanceXZToSquared( TelemVect3 pos2 )
    {
        float d = 0.0f;
        float tmp = this.x - pos2.x;
        d += tmp * tmp;
        tmp = this.z - pos2.z;
        d += tmp * tmp;
        
        return ( d );
    }
    
    public final float getDistanceXZTo( TelemVect3 pos2 )
    {
        return ( (float)Math.sqrt( getDistanceXZToSquared( pos2 ) ) );
    }
    
    public final float getDistanceXZToSquared( float x, float z )
    {
        float d = 0.0f;
        float tmp = this.x - x;
        d += tmp * tmp;
        tmp = this.z - z;
        d += tmp * tmp;
        
        return ( d );
    }
    
    public final float getDistanceXZTo( float x, float z )
    {
        return ( (float)Math.sqrt( getDistanceXZToSquared( x, z ) ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( this.getClass().getSimpleName() + "( " + getX() + ", " + getY() + ", " + getZ() + " )" );
    }
    
    public TelemVect3()
    {
    }
}
