package rush;

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
                        RobotInfo[] bots = rc.senseNearbyRobots(2, Team.NEUTRAL);
                        if(bots.length > 0){
                            rc.activate(bots[0].location);
                        }
                    }
                    //build
                    if(rc.isCoreReady()) {
                        if (rc.getTeamParts() >= RobotType.SOLDIER.partCost) {
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

                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
