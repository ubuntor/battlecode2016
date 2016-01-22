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
        MapLocation[] targets = rc.getInitialArchonLocations(rc.getTeam().opponent());
        int targetNum = 0;
        int distance;
        try {
            distance = 999;
            for(int i = 0; i < targets.length; i++){
                if(targets[i].distanceSquaredTo(rc.getLocation()) < distance){
                    targetNum = i;
                    distance = targets[i].distanceSquaredTo(rc.getLocation());
                }
            }
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
                            if (enemies[i].location.distanceSquaredTo(rc.getLocation()) < enemyDistance && !enemies[i].type.equals(RobotType.ARCHON)) {
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
                                else if (rc.canMove(dirToMove.rotateLeft().rotateLeft().rotateLeft()))
                                    rc.move(dirToMove.rotateLeft().rotateLeft().rotateLeft());
                                else if (rc.canMove(dirToMove.rotateRight().rotateRight().rotateRight()))
                                    rc.move(dirToMove.rotateRight().rotateRight().rotateRight());
                                else if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                                    // Too much rubble, so I should clear it
                                    rc.clearRubble(dirToMove);
                                }
                                // if we still can't move then we're fucked lol
                            }
                        }
                    }
                    else {
                        //neutrals
                        RobotInfo[] neutrals = rc.senseNearbyRobots(2, Team.NEUTRAL);
                        RobotInfo bestToActivate = null;
                        int prio = -1;
                        for(RobotInfo r : neutrals){
                            if(!rc.isCoreReady())
                                break;
                            if(r.type.equals(RobotType.ARCHON))
                                rc.activate(r.location);
                            else if(r.type.equals(RobotType.TURRET) || r.type.equals(RobotType.TTM)){
                                bestToActivate = r;
                                prio = 5;
                            } else if(r.type.equals(RobotType.VIPER) && prio < 4){
                                bestToActivate = r;
                                prio = 4;
                            } else if(r.type.equals(RobotType.SOLDIER) && prio < 3){
                                bestToActivate = r;
                                prio = 3;
                            } else if(r.type.equals(RobotType.GUARD) && prio < 2){
                                bestToActivate = r;
                                prio = 2;
                            } else if(prio < 1){
                                bestToActivate = r;
                                prio = 1;
                            }
                        }
                        if(bestToActivate != null && rc.isCoreReady())
                            rc.activate(bestToActivate.location);

                        //build
                        if(rc.isCoreReady()) {
                            dirToBuild = rc.getLocation().directionTo(targets[targetNum]);
                            if ((rc.getRoundNum() == 0 || rand.nextInt(100) >= 25) && rc.getTeamParts() >= RobotType.VIPER.partCost){
                                for (int i = 0; i < 8; i++) {
                                    if (rc.canBuild(dirToBuild, RobotType.VIPER)) {
                                        rc.build(dirToBuild, RobotType.VIPER);
                                        break;
                                    } else {
                                        dirToBuild.rotateRight();
                                    }
                                }
                            }
                            else if (rc.getTeamParts() >= RobotType.GUARD.partCost) {
                                for (int i = 0; i < 8; i++) {
                                    if (rc.canBuild(dirToBuild, RobotType.GUARD)) {
                                        rc.build(dirToBuild, RobotType.GUARD);
                                        break;
                                    } else {
                                        dirToBuild.rotateRight();
                                    }
                                }
                            }

                            //healing
                            RobotInfo[] allies = rc.senseNearbyRobots(35, rc.getTeam());
                            int mostHurt = -1;
                            for(int i = 0; i < allies.length; i++){
                                if(allies[i].ID == rc.getID() || allies[i].location.distanceSquaredTo(rc.getLocation()) > 24)
                                    continue;
                                if(allies[i].type != RobotType.ARCHON && allies[i].health < allies[i].maxHealth &&
                                        (mostHurt == -1 || allies[i].health < allies[mostHurt].health))
                                    mostHurt = i;
                            }
                            if(mostHurt >= 0 && rc.isCoreReady())
                                rc.repair(allies[mostHurt].location);

                            if(rc.isCoreReady()) {
                                if (rc.getLocation().distanceSquaredTo(targets[targetNum]) <= 2) {
                                    targetNum++;
                                    targetNum = targetNum % targets.length;
                                }
                                if (rc.canMove(dirToBuild)) {
                                    // Move
                                    rc.move(dirToBuild);
                                } else if (rc.canMove(dirToBuild.rotateLeft())) {
                                    rc.move(dirToBuild.rotateLeft());
                                } else if (rc.canMove(dirToBuild.rotateRight())) {
                                    rc.move(dirToBuild.rotateRight());
                                } else if (rc.senseRubble(rc.getLocation().add(dirToBuild)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                                    // Too much rubble, so I should clear it
                                    rc.clearRubble(dirToBuild);
                                    // Check if I can move in this direction
                                }
                            }
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
