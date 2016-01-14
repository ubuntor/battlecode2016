package perry;

import battlecode.common.*;

import java.util.Random;

/**
 * Created by samuel on 1/11/16.
 */
public class Archon {
    public static void run(RobotController rc) {
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        Direction dirToMove;
        Direction dirToBuild;
        Random rand = new Random(rc.getID());
        int destx = rc.getLocation().x;
        int desty = rc.getLocation().y;
        int buildStatus = 0;
        int targetSet = 0; //none
        int turretID = 0;
        int scoutID = 0;
        try {
            // Any code here gets executed exactly once at the beginning of the game.
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                if(rc.isCoreReady()) {
                    //run the fuck away if you see an enemy
                    int neutralDistance = 999;
                    RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), 53);
                    if(enemies.length > 0){
                        int enemyDistance = 999;
                        RobotInfo closestEnemy = null;
                        for (int i = 0; i < enemies.length; i++) {
                            if (enemies[i].location.distanceSquaredTo(rc.getLocation()) < enemyDistance) {
                                closestEnemy = enemies[i];
                                enemyDistance = enemies[i].location.distanceSquaredTo(rc.getLocation());
                            }
                        }
                        if (!closestEnemy.equals(null)) {
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
                    }
                    else {
                        //neutrals
                        RobotInfo[] bots = rc.senseNearbyRobots(53, Team.NEUTRAL);
                        if(bots.length > 0){
                            RobotInfo closestNeutral = null;
                            for(int i = 0; i < bots.length; i++) {
                                if (bots[i].team.equals(Team.NEUTRAL) && bots[i].location.distanceSquaredTo(rc.getLocation()) < neutralDistance) {
                                    if (bots[i].location.distanceSquaredTo(rc.getLocation()) <= 2) {
                                        rc.activate(bots[i].location);
                                    } else if (bots[i].location.distanceSquaredTo(rc.getLocation()) < neutralDistance) {
                                        closestNeutral = bots[i];
                                        neutralDistance = bots[i].location.distanceSquaredTo(rc.getLocation());
                                    }
                                }
                            }
                            if (!closestNeutral.equals(null) && targetSet <= 2) {
                                destx = closestNeutral.location.x;
                                desty = closestNeutral.location.y;
                                targetSet = 2; //neutral
                            }
                        }
                    }
                    //build
                    if(rc.isCoreReady()) {
                        if (buildStatus == 0 && rc.getTeamParts() >= (RobotType.TURRET.partCost + RobotType.SCOUT.partCost)) {
                            dirToBuild = directions[rand.nextInt(8)];
                            for (int i = 0; i < 8; i++) {
                                if (rc.canBuild(dirToBuild, RobotType.TURRET)) {
                                    rc.build(dirToBuild, RobotType.TURRET);
                                    turretID = rc.senseRobotAtLocation(rc.getLocation().add(dirToBuild)).ID;
                                    buildStatus = 1; //turret needs spotter
                                    break;
                                } else {
                                    dirToBuild.rotateRight();
                                }
                            }
                        } else if (buildStatus == 1 && rc.getTeamParts() >= RobotType.SCOUT.partCost) {
                            dirToBuild = directions[rand.nextInt(8)];
                            for (int i = 0; i < 8; i++) {
                                if (rc.canBuild(dirToBuild, RobotType.SCOUT)) {
                                    rc.build(dirToBuild, RobotType.SCOUT);
                                    scoutID = rc.senseRobotAtLocation(rc.getLocation().add(dirToBuild)).ID;
                                    buildStatus = 0;
                                    //ADD BROADCAST
                                    break;
                                } else {
                                    dirToBuild.rotateRight();
                                }
                            }
                        } else if (buildStatus == 0 && rc.getTeamParts() >= RobotType.SOLDIER.partCost) {
                            dirToBuild = directions[rand.nextInt(8)];
                            for (int i = 0; i < 8; i++) {
                                if (rc.canBuild(dirToBuild, RobotType.SOLDIER)) {
                                    rc.build(dirToBuild, RobotType.SOLDIER);
                                    break;
                                } else {
                                    dirToBuild.rotateRight();
                                }
                            }
                        }
                    }
                    //move towards objective
                    if(rc.isCoreReady()) {
                        if (closestEnemy.equals(null) && targetSet > 0) {
                            MapLocation target = new MapLocation(destx, desty);
                            if (rc.getLocation().equals(target)) {
                                targetSet = 0;
                                break;
                            } else {
                                dirToMove = rc.getLocation().directionTo(target);
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
                    }
                    //scrap
                    if(rc.isCoreReady()) {
                        int distance = 999;
                        MapLocation closestScrap = null;
                        MapLocation[] scrap = rc.sensePartLocations(53);
                        for (int i = 0; i < scrap.length; i++) {
                            if (scrap[i].distanceSquaredTo(rc.getLocation()) < distance) {
                                closestScrap = scrap[i];
                                distance = scrap[i].distanceSquaredTo(rc.getLocation());
                            }
                        }
                        if (!closestScrap.equals(null) && targetSet <= 1) {
                            destx = closestScrap.x;
                            desty = closestScrap.y;
                            targetSet = 1; //scrap
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
}
