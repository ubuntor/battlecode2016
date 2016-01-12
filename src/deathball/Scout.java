package deathball;

import battlecode.common.*;

/**
 * Created by samuel on 1/11/16.
 */
public class Scout {
    public static void run(RobotController rc) {
		MapLocation home = rc.getLocation();
		Direction dirToMove = Direction.EAST;
        int spiralCount = 1;
        int stepCount = 7;
        try {
            // init stuff
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), 53);
                        for (int i = 0; i < enemies.length; i++) {
                            if(enemies[i].team == rc.getTeam().opponent() && enemies[i].type == RobotType.ARCHON){
                                rc.broadcastMessageSignal(0, 0, 10000);
                                rc.broadcastMessageSignal(enemies[i].location.x, enemies[i].location.y, 10000);
                            }
                            if(enemies[i].team == rc.getTeam().opponent() && enemies[i].type != RobotType.SCOUT){
                                rc.broadcastMessageSignal(0, 0, 900);
                                rc.broadcastMessageSignal(enemies[i].location.x, enemies[i].location.y, 900);
                            }
                            if(rc.getLocation().distanceSquaredTo(enemies[i].location) >= 13){
                                if (rc.isCoreReady()) {
                                    // Check the rubble in that direction
                                    if (rc.canMove(enemies[i].location.directionTo(rc.getLocation()))) {
                                        // Move
                                        rc.canMove(enemies[i].location.directionTo(rc.getLocation()));
                                    }
                                }
                            }
                        }
                        MapLocation[] parts = rc.sensePartLocations(106);
                        MapLocation close = home;
                        for (int i = 0; i < parts.length; i++) {
                            if(i ==0){
                                close = parts[i];
                            }
                            else if(parts[i].distanceSquaredTo(home)<=close.distanceSquaredTo(home)){
                                close = parts[i];
                            }
                        }
                        if(!close.equals(home)){
                            rc.broadcastMessageSignal(1, 0, 106);
                            rc.broadcastMessageSignal(close.x, close.y, 106);
                        }
                        if (rc.isCoreReady()) {
                            // Check the rubble in that direction
                            if (rc.canMove(dirToMove)) {
                                // Move
                                rc.move(dirToMove);
                                stepCount--;
                            }
                            else{
                                spiralCount++;
                                stepCount = spiralCount * 7 - stepCount;
                                dirToMove = dirToMove.rotateRight();
                                dirToMove = dirToMove.rotateRight();
                            }
                        }
                        if (stepCount == 0) {
                            spiralCount++;
                            stepCount = spiralCount * 7;
                            dirToMove = dirToMove.rotateRight();
                            dirToMove = dirToMove.rotateRight();
                        }
                        Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
