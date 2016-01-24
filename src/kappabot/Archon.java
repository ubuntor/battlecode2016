package kappabot;

import battlecode.common.*;

public class Archon {
	public static RobotController rc = RobotPlayer.rc;
	public static int id = -1, archons;
	public static int turretPerimeter = 0;
	public static MapLocation[] enemies;
	public static MapLocation target;
	
    public static void run() throws GameActionException {
    	if (rc.getRoundNum() == 0) {
    		MapLocation[] f = rc.getInitialArchonLocations(rc.getTeam());
    		archons = f.length;
    		for (int i = 0; i < archons; i++) {
    			if (rc.getLocation().equals(f[i])) {
    				id = i;
    				break;
    			}
    		}
    		rc.setIndicatorString(0, "ARCHON #" + id);
    		
    		enemies = rc.getInitialArchonLocations(rc.getTeam().opponent());

    		target = f[0];
    	}
    	
		int p = 0;
		rc.setIndicatorString(1, "Perimeter size: " + p);
    }
}
