package team259;

import battlecode.common.*;

import java.util.Random;
import java.lang.Math;

/**
 * Created by samuel on 1/11/16.
 */
public class Guard {
    public static void run(RobotController rc) {
        Random rand = new Random(rc.getID());
        int fate = rand.nextInt(1000);
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        try {
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                int heuristic = -1;
                int val = 0;
                MapLocation maxloc = rc.getLocation();
                RobotInfo[] attackable = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
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
                    if (attackable[i].health <= 6) {
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
                else {
                    heuristic = -1;
                    maxloc = rc.getLocation();
                    RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), 24);
                    for (int i = 0; i < enemies.length; i++) {
                        val = 0;
                        if (enemies[i].type == RobotType.GUARD || enemies[i].type == RobotType.ARCHON) {
                            val = 4;
                        } else if (enemies[i].type == RobotType.TTM) {
                            val = 5;
                        } else if (enemies[i].type == RobotType.SOLDIER) {
                            val = 6;
                        } else if (enemies[i].type == RobotType.TURRET) {
                            val = 7;
                        } else if (enemies[i].type == RobotType.VIPER) {
                            val = 8;
                        }
                        if (enemies[i].health <= 12) {
                            val *= 2;
                        }
                        if (enemies[i].type == RobotType.FASTZOMBIE || enemies[i].type == RobotType.BIGZOMBIE) {
                            val = 1;
                        } else if (enemies[i].type == RobotType.STANDARDZOMBIE) {
                            val = 2;
                        } else if (enemies[i].type == RobotType.RANGEDZOMBIE) {
                            val = 3;
                        }
                        if (val > heuristic) {
                            maxloc = enemies[i].location;
                            heuristic = val;
                        }
                    }
                    if (rc.isCoreReady()) {
                        Direction dirToMove = rc.getLocation().directionTo(maxloc);
                        if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                            // Too much rubble, so I should clear it
                            rc.clearRubble(dirToMove);
                            // Check if I can move in this direction
                        } else if (rc.canMove(dirToMove)) {
                            // Move
                            rc.move(dirToMove);
                        }
                    }

                    //patrol
                    Direction dirToMove = directions[fate % 8];
                    // Check the rubble in that direction
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
