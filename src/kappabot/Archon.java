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
    	
    	if (!rc.canSenseLocation(target)) {
    		Utils.moveTowards(target);
    	} else {
    		int p = 0;
    		RobotInfo[] robots = rc.senseNearbyRobots();
    		for (RobotInfo r : robots) {
    			if (r.team == rc.getTeam().opponent()) continue;
    			if (r.team == Team.ZOMBIE) continue;
    			
    			if (r.location.distanceSquaredTo(rc.getLocation()) <= 5) p++;
    		}
    		rc.setIndicatorString(1, "Perimeter size: " + p);
    		
    		if (rc.isCoreReady()) {
    			if (p < 5) {
    				// establish perimeter
    				for (Direction d : Direction.values()) {
    					if (rc.canBuild(d,RobotType.TURRET)) {
    						rc.build(d,RobotType.TURRET);
    					}
    				}
    			}
    		}
    	}
    }
}
