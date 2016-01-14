package rush;

import battlecode.common.*;

import java.util.Random;
import java.lang.Math;

/**
 * Created by samuel on 1/11/16.
 */
public class Soldier {
    public static void run(RobotController rc) {
        Random rand = new Random(rc.getID());
        int heuristic = -1;
        int val;
        int distance;
        Direction dirToMove;
        RobotInfo closestEnemy = null;
        RobotInfo[] attackable;
        RobotInfo[] enemies;
        MapLocation maxloc;
        MapLocation[] targets = rc.getInitialArchonLocations(rc.getTeam().opponent());
        int targetNum = 0;
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        try {
            // init stuff
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                //attack
                attackable = rc.senseHostileRobots(rc.getLocation(), 13);
                if(attackable.length > 0) {
                    maxloc = rc.getLocation();
                    for (int i = 0; i < attackable.length; i++) {
                        val = 0;
                        if (attackable[i].type == RobotType.GUARD || attackable[i].type == RobotType.ARCHON) {
                            val = 4;
                        } else if (attackable[i].type == RobotType.TTM) {
                            val = 5;
                        } else if (attackable[i].type == RobotType.SOLDIER) {
                            val = 6;
                        } else if (attackable[i].type == RobotType.TURRET) {
                            val = 7;
                        } else if (attackable[i].type == RobotType.VIPER) {
                            val = 8;
                        }
                        if (attackable[i].health <= 8) {
                            val *= 2;
                        }
                        if (attackable[i].type == RobotType.FASTZOMBIE || attackable[i].type == RobotType.BIGZOMBIE) {
                            val = 1;
                        } else if (attackable[i].type == RobotType.STANDARDZOMBIE) {
                            val = 2;
                        } else if (attackable[i].type == RobotType.RANGEDZOMBIE) {
                            val = 3;
                        }
                        if (val > heuristic) {
                            maxloc = attackable[i].location;
                            heuristic = val;
                        }
                    }
                    if (rc.isWeaponReady() && !maxloc.equals(rc.getLocation())) {
                        rc.attackLocation(maxloc);
                    }
                }
                //micro
                else {
                    enemies = rc.senseHostileRobots(rc.getLocation(), 24);
                    if(enemies.length > 0) {
                        distance = 999;
                        for (int i = 0; i < enemies.length; i++) {
                            if (enemies[i].team.equals(rc.getTeam().opponent()) && enemies[i].location.distanceSquaredTo(rc.getLocation()) < distance) {
                                closestEnemy = enemies[i];
                                distance = enemies[i].location.distanceSquaredTo(rc.getLocation());
                            }
                        }
                        //run away
                        if (!closestEnemy.equals(null) && distance <= rc.getType().attackRadiusSquared) {
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
                                // if we still can't move then we're fucked lol
                            }
                        }
                        //run to
                        else if (!closestEnemy.equals(null) && distance > rc.getType().attackRadiusSquared) {
                            dirToMove = rc.getLocation().directionTo(closestEnemy.location);
                            if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                                // Too much rubble, so I should clear it
                                rc.clearRubble(dirToMove);
                                // Check if I can move in this direction
                            } else if (rc.canMove(dirToMove)) {
                                // Move
                                rc.move(dirToMove);
                            }
                        }
                    }
                    //patrol
                    dirToMove = directions[rand.nextInt(8)];
                    //move to target
                    if(rc.getLocation().equals(targets[targetNum])){
                        targetNum++;
                    }
                    else if(targetNum < targets.length) {
                        dirToMove = rc.getLocation().directionTo(targets[targetNum]);
                    }
                    if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                        // Too much rubble, so I should clear it
                        rc.clearRubble(dirToMove);
                        // Check if I can move in this direction
                    } else if (rc.canMove(dirToMove)) {
                        // Move
                        rc.move(dirToMove);
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
