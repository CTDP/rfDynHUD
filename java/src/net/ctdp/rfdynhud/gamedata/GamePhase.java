package net.ctdp.rfdynhud.gamedata;

/**
 * 
 * @author Marvin Froehlich
 */
public enum GamePhase
{
    BEFORE_SESSION_HAS_BEGUN, // 0
    RECONNAISSANCE_LAPS, // 1 (race only)
    GRID_WALK_THROUGH, // 2 (race only)
    FORMATION_LAP, // 3 (race only)
    STARTING_LIGHT_COUNTDOWN_HAS_BEGUN, // 4 (race only)
    GREEN_FLAG, // 5
    FULL_COURSE_YELLOW, // 6 (safety car)
    SESSION_STOPPED, // 7
    SESSION_OVER, // 8
    ;
}
