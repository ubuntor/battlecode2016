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
        int distance;
        Direction dirToMove;
        RobotInfo closestEnemy = null;
        RobotInfo[] attackable;
        RobotInfo[] enemies;
        MapLocation toAttack;
        MapLocation[] targets = rc.getInitialArchonLocations(rc.getTeam().opponent());
        int targetNum = 0;
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        try {
            distance = 999;
            for(int i = 0; i < targets.length; i++){
                if(targets[i].distanceSquaredTo(rc.getLocation()) < distance){
                    targetNum = i;
                    distance = targets[i].distanceSquaredTo(rc.getLocation());
                }
            }
            // init stuff
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                //attack
                attackable = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
                if (attackable.length > 0 && rc.isWeaponReady()) {
                    toAttack = findWeakest(attackable);
                    rc.attackLocation(toAttack);
                }
                else {
                    //micro
                    enemies = rc.senseHostileRobots(rc.getLocation(), 24);
                    if (enemies.length > 0) {
                        distance = 999;
                        for (int i = 0; i < enemies.length; i++) {
                            if (enemies[i].team.equals(rc.getTeam().opponent()) && enemies[i].location.distanceSquaredTo(rc.getLocation()) < distance) {
                                closestEnemy = enemies[i];
                                distance = enemies[i].location.distanceSquaredTo(rc.getLocation());
                            }
                        }
                        //run to
                        if (!closestEnemy.equals(null) && distance > rc.getType().attackRadiusSquared) {
                            dirToMove = rc.getLocation().directionTo(closestEnemy.location);
                            if (rc.canMove(dirToMove)) {
                                // Move
                                rc.move(dirToMove);
                            }else if(rc.canMove(dirToMove.rotateLeft())){
                                rc.move(dirToMove.rotateLeft());
                            }
                            else if(rc.canMove(dirToMove.rotateRight())){
                                rc.move(dirToMove.rotateRight());
                            }
                            else if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                                // Too much rubble, so I should clear it
                                rc.clearRubble(dirToMove);
                                // Check if I can move in this direction
                            }
                        }
                    }
                    //patrol/move
                    dirToMove = directions[rand.nextInt(8)];
                    //move to target
                    if(rc.getLocation().equals(targets[targetNum])){
                        targetNum++;
                        targetNum = targetNum % targets.length;
                    }
                    else if(targetNum < targets.length) {
                        dirToMove = rc.getLocation().directionTo(targets[targetNum]);
                    }
                    if (rc.canMove(dirToMove)) {
                        // Move
                        rc.move(dirToMove);
                    }else if(rc.canMove(dirToMove.rotateLeft())){
                        rc.move(dirToMove.rotateLeft());
                    }
                    else if(rc.canMove(dirToMove.rotateRight())){
                        rc.move(dirToMove.rotateRight());
                    }
                    else if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                        // Too much rubble, so I should clear it
                        rc.clearRubble(dirToMove);
                        // Check if I can move in this direction
                    }
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static MapLocation findWeakest(RobotInfo[] listOfRobots){
        double weakestSoFar = -100;
        MapLocation weakestLocation = null;
        for(RobotInfo r:listOfRobots){
            double weakness = r.maxHealth-r.health;
            if(weakness>weakestSoFar){
                weakestLocation = r.location;
                weakestSoFar=weakness;
            }
        }
        return weakestLocation;
    }
}
