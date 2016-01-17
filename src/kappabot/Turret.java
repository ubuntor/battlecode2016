package kappabot;

import battlecode.common.*;

public class Turret {
	public static RobotController rc = RobotPlayer.rc;
	public static boolean fixed = false;
	
    public static void run() throws GameActionException {
    	RobotInfo[] robots = rc.senseNearbyRobots();

    	if (rc.getType() == RobotType.TURRET) {
    		if (!fixed) {
	    		boolean adjacent = false;
	    		int enemies = 0;
		    	for (RobotInfo r : robots) {
		    		if (r.team == rc.getTeam().opponent()) {
		    			enemies++;
		    		} else if (r.team == Team.ZOMBIE) {
		    			enemies++;
		    		} else {
		    			if (r.type == RobotType.ARCHON && r.location.distanceSquaredTo(rc.getLocation()) <= 2) {
		    				adjacent = true;
		    			}
		    		}
		    	}
		    	if (!adjacent) fixed = true;
		    	if (!fixed && enemies == 0) {
		    		rc.pack();
		    	}
		    	if (rc.isCoreReady()) {
			    	if (enemies > 0) {
			    		// attack shit
			    	} else {
			    		for (Direction d : Direction.values()) {
			    			if (rc.canMove(d)) {
			    				rc.move(d);
			    				break;
			    			}
			    		}
			    	}
		    	}
    		} else {
    	    	for (RobotInfo r : robots) {
    	    		if (r.team == rc.getTeam().opponent()) {

    	    		} else if (r.team == Team.ZOMBIE) {

    	    		}
    	    	}
    		}
    	} else {
    		
    	}
    }
}
