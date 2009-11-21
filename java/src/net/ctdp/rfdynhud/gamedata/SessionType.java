package net.ctdp.rfdynhud.gamedata;

/**
 * 
 * @author Marvin Froehlich
 */
public enum SessionType
{
    TEST_DAY, // 0
    PRACTICE1, // 1
    PRACTICE2, // 2
    PRACTICE3, // 3
    PRACTICE4, // 4
    QUALIFYING, // 5
    WARMUP, // 6
    RACE, // 7
    ;
    
    public static final String PRACTICE_WILDCARD = "PRACTICE";
    
    public final boolean isTestDay()
    {
        return ( this == TEST_DAY );
    }
    
    public final boolean isPractice()
    {
        return ( ( this == PRACTICE1 ) || ( this == PRACTICE2 ) || ( this == PRACTICE3 ) || ( this == PRACTICE4 ) );
    }
    
    public final boolean isRace()
    {
        return ( this == RACE );
    }
}
