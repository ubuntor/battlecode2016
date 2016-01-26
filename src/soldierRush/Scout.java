package soldierRush;

import battlecode.common.*;

import java.util.Random;

/**
 * Created by samuel on 1/11/16.
 */
public class Scout {
    public static void run(RobotController rc) {
        Random rand = new Random(rc.getID());
        int distance;
        int cycle = 0;
        Direction dirToMove;
        MapLocation originalTarget = rc.getLocation();
        MapLocation[] targets = rc.getInitialArchonLocations(rc.getTeam().opponent());
        int targetNum = 0;
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        try {
            distance = 999;
            for (int i = 0; i < targets.length; i++) {
                if (targets[i].distanceSquaredTo(rc.getLocation()) < distance) {
                    targetNum = i;
                    distance = targets[i].distanceSquaredTo(rc.getLocation());
                }
            }
            originalTarget = targets[targetNum];
            // init stuff
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                //patrol/move
                if (rc.isCoreReady()) {
                    dirToMove = directions[rand.nextInt(8)];
                    //move to target
                    if(cycle == 0) {
                        if (rc.getLocation().distanceSquaredTo(targets[targetNum]) <= 2) {
                            targetNum++;
                            targetNum = targetNum % targets.length;
                            if (originalTarget == targets[targetNum]) {
                                    cycle = 1;
                            }
                        } else if (targetNum < targets.length) {
                            dirToMove = rc.getLocation().directionTo(targets[targetNum]);
                        }
                    }
                    if (rc.canMove(dirToMove)) {
                        // Move
                        rc.move(dirToMove);
                    } else if (rc.canMove(dirToMove.rotateLeft())) {
                        rc.move(dirToMove.rotateLeft());
                    } else if (rc.canMove(dirToMove.rotateRight())) {
                        rc.move(dirToMove.rotateRight());
                    } else if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                        // Too much rubble, so I should clear it
                        rc.clearRubble(dirToMove);
                        // Check if I can move in this direction
                    } else if (rc.senseRubble(rc.getLocation().add(dirToMove.rotateLeft())) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                        // Too much rubble, so I should clear it
                        rc.clearRubble(dirToMove.rotateLeft());
                    } else if (rc.senseRubble(rc.getLocation().add(dirToMove.rotateRight())) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                        // Too much rubble, so I should clear it
                        rc.clearRubble(dirToMove.rotateRight());
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
