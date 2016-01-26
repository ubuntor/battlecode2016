package team259.guardrush;

import battlecode.common.*;

import java.util.Random;

/**
 * Created by samuel on 1/11/16.
 */
public class Turret {
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
                // may waste bytecodes???
                switch (rc.getType()) {
                    case TURRET:
                        //attack
                        MapLocation currLoc = rc.getLocation();
                        RobotInfo[] hostile = rc.senseHostileRobots(currLoc, 24);
                        RobotInfo bestTarget = null;
                        double maxExpected = 0;
                        for (int i = 0; i < hostile.length; i++) {
                            if (!rc.canAttackLocation(hostile[i].location))
                                continue;
                            int dist = currLoc.distanceSquaredTo(hostile[i].location);
                            double tempExpected = 0;
                            if (hostile[i].type.equals(RobotType.SOLDIER) && dist <= 13) {
                                tempExpected += 2 / hostile[i].health;
                            } else if (hostile[i].type.equals(RobotType.GUARD) && dist <= 2) {
                                tempExpected += 1.5 / hostile[i].health;
                            } else if (hostile[i].type.equals(RobotType.VIPER) && dist <= 20) {
                                tempExpected += (8.0 / 3) / hostile[i].health;
                            } else if (hostile[i].type.equals(RobotType.TURRET) && dist <= 48) {
                                tempExpected += (13.0 / 3) / hostile[i].health;
                            } else if (hostile[i].type.equals(RobotType.RANGEDZOMBIE) && dist <= 13) {
                                tempExpected += 3 / hostile[i].health;
                            } else if (hostile[i].type.equals(RobotType.STANDARDZOMBIE) && dist <= 2) {
                                tempExpected += 1.25 / hostile[i].health;
                            } else if (hostile[i].type.equals(RobotType.FASTZOMBIE) && dist <= 2) {
                                tempExpected += 3 / hostile[i].health;
                            } else if (hostile[i].type.equals(RobotType.BIGZOMBIE) && dist <= 2) {
                                tempExpected += (25.0 / 3) / hostile[i].health;
                            }
                            if (tempExpected > maxExpected || bestTarget == null ||
                                    (tempExpected == maxExpected && dist > bestTarget.location.distanceSquaredTo(currLoc))) {
                                maxExpected = tempExpected;
                                bestTarget = hostile[i];
                            }
                        }
                        if (bestTarget != null && rc.isWeaponReady())
                            rc.attackLocation(bestTarget.location);

                        if (hostile.length == 0 || bestTarget == null)
                            rc.pack();
                        break;
                    case TTM:
                        currLoc = rc.getLocation();
                        //RUN AWAY
                        hostile = rc.senseHostileRobots(currLoc, 5);
                        if (rc.isCoreReady()) {
                            if (hostile.length != 0) {
                                int enemyDistance = 999;
                                closestEnemy = null;
                                for (int i = 0; i < hostile.length; i++) {
                                    if (hostile[i].location.distanceSquaredTo(rc.getLocation()) < enemyDistance) {
                                        closestEnemy = hostile[i];
                                        enemyDistance = hostile[i].location.distanceSquaredTo(rc.getLocation());
                                    }
                                }
                                if (closestEnemy != null) {
                                    dirToMove = closestEnemy.location.directionTo(rc.getLocation());
                                    if (rc.canMove(dirToMove)) {
                                        // Move away
                                        rc.move(dirToMove);
                                    } else {
                                        if (rc.canMove(dirToMove.rotateLeft()))
                                            rc.move(dirToMove.rotateLeft());
                                        else if (rc.canMove(dirToMove.rotateRight()))
                                            rc.move(dirToMove.rotateRight());
                                        else if (rc.canMove(dirToMove.rotateLeft().rotateLeft()))
                                            rc.move(dirToMove.rotateLeft().rotateLeft());
                                        else if (rc.canMove(dirToMove.rotateRight().rotateRight()))
                                            rc.move(dirToMove.rotateRight().rotateRight());
                                        else if (rc.canMove(dirToMove.rotateLeft().rotateLeft().rotateLeft()))
                                            rc.move(dirToMove.rotateLeft().rotateLeft().rotateLeft());
                                        else if (rc.canMove(dirToMove.rotateRight().rotateRight().rotateRight()))
                                            rc.move(dirToMove.rotateRight().rotateRight().rotateRight());
                                        // if we still can't move then we're fucked lol
                                    }
                                }
                            }
                        }
                        if (hostile.length == 0) {
                            hostile = rc.senseHostileRobots(currLoc, rc.getType().sensorRadiusSquared);
                            if (hostile.length != 0) {
                                rc.unpack();
                            }
                        }


                        //patrol/move
                        if (rc.isCoreReady()) {
                            dirToMove = directions[rand.nextInt(8)];
                            //move to target
                            if (cycle == 0) {
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
                            }
                            break;
                        }
                    default:
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
