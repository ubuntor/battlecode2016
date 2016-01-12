package team259;

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
                if(enemies.length > 0){
                for (int i = 0; i < enemies.length; i++) {
                    if (rc.getLocation().distanceSquaredTo(enemies[i].location) <= 26) {
                        if (rc.isCoreReady()) {
                            // Check the rubble in that direction
                            Direction dir = enemies[i].location.directionTo(rc.getLocation());
                            for(int j = 0; j < 8; j++) {
                                if (rc.canMove(dir)) {
                                    // Move
                                    rc.move(dir);
                                    break;
                                } else {
                                    dir = dir.rotateRight();
                                }
                            }
                        }
                    }
                    if (enemies[i].team == rc.getTeam().opponent() && enemies[i].type != RobotType.SCOUT) {
                        rc.broadcastMessageSignal(0, 0, 106); //first 0 indicates hostile, second 0 indicates other player
                        rc.broadcastMessageSignal(enemies[i].location.x, enemies[i].location.y, 106);
                        rc.broadcastMessageSignal(9, 9, 106); //DAVAI
                        rc.broadcastMessageSignal(enemies[i].location.x, enemies[i].location.y, 106);
                    } else if (enemies[i].type == RobotType.ZOMBIEDEN) {
                        rc.broadcastMessageSignal(0, 0, 106);
                        rc.broadcastMessageSignal(enemies[i].location.x, enemies[i].location.y, 106);
                    } else {
                        rc.broadcastMessageSignal(0, 1, 106);
                        rc.broadcastMessageSignal(enemies[i].location.x, enemies[i].location.y, 106);
                    }
                }
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

                RobotInfo[] neutrals = rc.senseNearbyRobots(rc.getLocation(), 53, Team.NEUTRAL);
                if(neutrals.length > 0) {
                    RobotInfo closen = null;
                    for (int i = 0; i < neutrals.length; i++) {
                        if (i == 0) {
                            closen = neutrals[i];
                        } else if (neutrals[i].location.distanceSquaredTo(home) <= closen.location.distanceSquaredTo(home)) {
                            closen = neutrals[i];
                        }
                    }
                    if (!closen.location.equals(home)) {
                        rc.broadcastMessageSignal(1, 0, 106);
                        rc.broadcastMessageSignal(closen.location.x, closen.location.y, 106);
                    }
                }

                MapLocation[] parts = rc.sensePartLocations(106);
                if(parts.length > 0) {
                    MapLocation close = home;
                    for (int i = 0; i < parts.length; i++) {
                        if (i == 0) {
                            close = parts[i];
                        } else if (parts[i].distanceSquaredTo(home) <= close.distanceSquaredTo(home)) {
                            close = parts[i];
                        }
                    }
                    if (!close.equals(home)) {
                        rc.broadcastMessageSignal(1, 0, 106);
                        rc.broadcastMessageSignal(close.x, close.y, 106);
                    }
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
