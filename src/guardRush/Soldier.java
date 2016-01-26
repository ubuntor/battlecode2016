package guardRush;

import battlecode.common.*;

import java.util.Random;
import java.lang.Math;

/**
 * Created by samuel on 1/11/16.
 */
public class Soldier {
    public static void run(RobotController rc) {
        Random rand = new Random(rc.getID());
        int distance;
        int cycle = 0;
        Direction dirToMove;
        RobotInfo closestEnemy = null;
        RobotInfo[] attackable;
        RobotInfo[] enemies;
        MapLocation toAttack;
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
                //attack
                if(rc.isWeaponReady()){
                    attackable = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
                    if (attackable.length > 0) {
                        toAttack = findWeakest(attackable);
                        rc.attackLocation(toAttack);
                    }
                }

                if(rc.isCoreReady()) {
                    //micro
                    enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
                    if (enemies.length > 0) {
                        distance = 999;
                        for (RobotInfo r : enemies) {
                            if (r.location.distanceSquaredTo(rc.getLocation()) < distance) {
                                closestEnemy = r;
                                distance = r.location.distanceSquaredTo(rc.getLocation());
                            }
                        }

                        //run away
                        if (rc.isCoreReady() && distance <= rc.getType().attackRadiusSquared) {
                            dirToMove = closestEnemy.location.directionTo(rc.getLocation());
                            if (rc.canMove(dirToMove)) {
                                // Move away
                                rc.move(dirToMove);
                            } else if (rc.canMove(dirToMove.rotateLeft())) {
                                rc.move(dirToMove.rotateLeft());
                            } else if (rc.canMove(dirToMove.rotateRight())){
                                rc.move(dirToMove.rotateRight());
                            } else if (rc.canMove(dirToMove.rotateLeft().rotateLeft())){
                                rc.move(dirToMove.rotateLeft().rotateLeft());
                            } else if (rc.canMove(dirToMove.rotateRight().rotateRight())){
                                rc.move(dirToMove.rotateRight().rotateRight());
                            } else if (rc.canMove(dirToMove.rotateLeft().rotateLeft().rotateLeft())) {
                                rc.move(dirToMove.rotateLeft().rotateLeft().rotateLeft());
                            } else if (rc.canMove(dirToMove.rotateRight().rotateRight().rotateRight())) {
                                rc.move(dirToMove.rotateRight().rotateRight().rotateRight());
                            } else if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                            // Too much rubble, so I should clear it
                                rc.clearRubble(dirToMove);
                            }
                            // if we still can't move then we're fucked lol
                        }

                        //run to
                        if (rc.isCoreReady() && distance > rc.getType().attackRadiusSquared) {
                            dirToMove = rc.getLocation().directionTo(closestEnemy.location);
                            if (rc.canMove(dirToMove)) {
                                // Move
                                rc.move(dirToMove);
                            } else if (rc.canMove(dirToMove.rotateLeft())) {
                                rc.move(dirToMove.rotateLeft());
                            } else if (rc.canMove(dirToMove.rotateRight())) {
                                rc.move(dirToMove.rotateRight());
                            } else if (rc.canMove(dirToMove.rotateLeft().rotateLeft())){
                                rc.move(dirToMove.rotateLeft().rotateLeft());
                            } else if (rc.canMove(dirToMove.rotateRight().rotateRight())){
                                rc.move(dirToMove.rotateRight().rotateRight());
                            } else if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                                // Too much rubble, so I should clear it
                                rc.clearRubble(dirToMove);
                                // Check if I can move in this direction
                            }
                        }
                    }

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
                        }
                    }
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static MapLocation findWeakest(RobotInfo[] listOfRobots) {
        double weakestSoFar = -100;
        MapLocation weakestLocation = null;
        for (RobotInfo r : listOfRobots) {
            double weakness = r.maxHealth - r.health;
            if (r.type == RobotType.TURRET){
                weakness *= 6;
            }
            else if (r.type == RobotType.VIPER){
                weakness *= 5;
            }
            else if (r.type == RobotType.SOLDIER){
                weakness *= 4;
            }
            else if (r.type == RobotType.GUARD){
                weakness *= 3;
            }
            else if (r.type == RobotType.ARCHON || r.type ==  RobotType.SCOUT){
                weakness *= 2;
            }
            else if (r.type == RobotType.ZOMBIEDEN){
                weakness /= 2;
            }
            if (weakness > weakestSoFar) {
                weakestLocation = r.location;
                weakestSoFar = weakness;
            }
        }
        return weakestLocation;
    }
}
