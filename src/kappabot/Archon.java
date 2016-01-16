package kappabot;

import battlecode.common.*;

public class Archon {
	public static RobotController rc = RobotPlayer.rc;
	public static int id = -1;
	public static Signal[] signals;
	public static MapLocation[] enemies;
	public static MapLocation target;
	
    public static void run() throws GameActionException {
    	if (rc.getRoundNum() == 0) {
    		MapLocation[] f = rc.getInitialArchonLocations(rc.getTeam());
    		for (int i = 0; i < f.length; i++) {
    			if (rc.getLocation().equals(f[i])) {
    				id = i;
    				break;
    			}
    		}
    		rc.setIndicatorString(0, "ARCHON #" + id);
    		
    		enemies = rc.getInitialArchonLocations(rc.getTeam().opponent());
    		target = enemies[id];
    	}
    	
    	Utils.moveTowards(target);
    }
}
