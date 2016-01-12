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
                    if (rc.getLocation().distanceSquaredTo(enemies[i].location) <= 26) {
                        if (rc.isCoreReady()) {
                            // Check the rubble in that direction
                        	Direction optimalAwayDir = enemies[i].location.directionTo(rc.getLocation());
                            if (rc.canMove(optimalAwayDir)) {
                                // Move away
                            	rc.move(optimalAwayDir);
                            } else {
                            	if (rc.canMove(optimalAwayDir.rotateLeft()))
                            		rc.move(optimalAwayDir.rotateLeft());
                            	else if (rc.canMove(optimalAwayDir.rotateRight()))
                            		rc.move(optimalAwayDir.rotateRight());
                        		else if (rc.canMove(optimalAwayDir.rotateLeft().rotateLeft()))
                        			rc.move(optimalAwayDir.rotateLeft().rotateLeft());
                        		else if (rc.canMove(optimalAwayDir.rotateRight().rotateRight()))
                        			rc.move(optimalAwayDir.rotateRight().rotateRight());
                            	// if we still can't move then we're fucked lol
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

                if (rc.isCoreReady() && rc.getHealth() > 50) { 
                	// we have to be healthy before doing this spiral shit
                    // Check the rubble in that direction
                    if (rc.canMove(dirToMove)) {
                        // Move
                        rc.move(dirToMove);
                        stepCount--;
                    } else {
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

                if (neutrals.length > 0) {
                    RobotInfo closen = neutrals[0];
                    int mind = closen.location.distanceSquaredTo(home);
                    for (int i = 0; i < neutrals.length; i++) {
                        int nd = neutrals[i].location.distanceSquaredTo(home);
                        if (nd < mind) {
                            closen = neutrals[i];
                            mind = nd;
                        }
                    }
                    rc.broadcastMessageSignal(1, 0, 106);
                    rc.broadcastMessageSignal(closen.location.x, closen.location.y, 106);
                }

                MapLocation[] parts = rc.sensePartLocations(106);

                if (parts.length > 0) {
                    MapLocation close = parts[0];
                    int mind = close.distanceSquaredTo(home);
                    for (int i = 0; i < parts.length; i++) {
                        int nd = parts[i].distanceSquaredTo(home);
                        if (nd < mind) {
                            close = parts[i];
                            mind = nd;
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
