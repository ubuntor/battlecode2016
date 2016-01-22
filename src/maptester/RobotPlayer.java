package maptester;

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) {
        // You can instantiate variables here.
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        int distance;
        int targetNum = 0;
        int sensorRange = (int)Math.sqrt(rc.getType().sensorRadiusSquared);
        MapLocation[] targets = rc.getInitialArchonLocations(rc.getTeam().opponent());
        MapLocation straightTarget = rc.getLocation();
        MapLocation moveTo = rc.getLocation();

        if (rc.getType() == RobotType.ARCHON) {
            try {
                distance = 999;
                for (int i = 0; i < targets.length; i++) {
                    if (targets[i].distanceSquaredTo(rc.getLocation()) < distance) {
                        targetNum = i;
                        distance = targets[i].distanceSquaredTo(rc.getLocation());
                    }
                }
                //System.out.println(targets.length);
            } catch (Exception e) {
                // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
                // Caught exceptions will result in a bytecode penalty.
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            while (true) {
                // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
                // at the end of it, the loop will iterate once per game round.
                try {
                    if(rc.isCoreReady()){
                        MapLocation[] tangents = new MapLocation[5];
                        MapLocation rightTarget1;
                        MapLocation rightTarget2;
                        MapLocation leftTarget1;
                        MapLocation leftTarget2;
                        straightTarget = straightTarget.add(rc.getLocation().directionTo(targets[targetNum]));
                        //System.out.println("Ready");
                        for(int i = 0; i < sensorRange; i++){
                            if(!rc.canSense(straightTarget)){
                                straightTarget = rc.getLocation();
                                break;
                            }
                            if(rc.senseRubble(straightTarget) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH){
                                break;
                            }
                            else if(!straightTarget.equals(rc.getLocation()) && rc.isLocationOccupied(straightTarget)){
                                break;
                            }
                            else if(!rc.canSense(straightTarget.add(rc.getLocation().directionTo(targets[targetNum])))){
                                break;
                            }
                            else{
                                straightTarget = straightTarget.add(rc.getLocation().directionTo(targets[targetNum]));
                            }
                        }
                        System.out.println(straightTarget.toString());
                        //System.out.println(rc.getLocation().distanceSquaredTo(straightTarget));
                        tangents[0] = straightTarget;
                        rightTarget1 = straightTarget.add(rc.getLocation().directionTo(straightTarget).rotateRight());
                        for(int i = 0; i < sensorRange; i++){
                            if(!rc.canSense(rightTarget1)){
                                rightTarget1 = straightTarget;
                                break;
                            }
                            else if(rc.senseRubble(rightTarget1) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH){
                                break;
                            }
                            else if(!rightTarget1.equals(rc.getLocation()) && rc.isLocationOccupied(rightTarget1)){
                                break;
                            }
                            else if(!rc.canSense(rightTarget1.add(rc.getLocation().directionTo(rightTarget1)))){
                                break;
                            }
                            else{
                                rightTarget1 = rightTarget1.add(rc.getLocation().directionTo(rightTarget1));
                            }
                        }
                        tangents[1] = rightTarget1;
                        rightTarget2 = straightTarget.add(rc.getLocation().directionTo(straightTarget).rotateRight().rotateRight());
                        for(int i = 0; i < sensorRange; i++){
                            if(!rc.canSense(rightTarget2)){
                                rightTarget2 = straightTarget;
                                break;
                            }
                            else if(rc.senseRubble(rightTarget2) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH){
                                break;
                            }
                            else if(!rightTarget2.equals(rc.getLocation()) && rc.isLocationOccupied(rightTarget2)){
                                break;
                            }
                            else if(!rc.canSense(rightTarget2.add(rc.getLocation().directionTo(rightTarget2)))){
                                break;
                            }
                            else{
                                rightTarget2 = rightTarget2.add(rc.getLocation().directionTo(rightTarget2));
                            }
                        }
                        tangents[2] = rightTarget2;
                        leftTarget1 = straightTarget.add(rc.getLocation().directionTo(straightTarget).rotateLeft());
                        for(int i = 0; i < sensorRange; i++){
                            if(!rc.canSense(leftTarget1)){
                                leftTarget1 = straightTarget;
                                break;
                            }
                            else if(rc.senseRubble(leftTarget1) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH){
                                break;
                            }
                            else if(!leftTarget1.equals(rc.getLocation()) && rc.isLocationOccupied(leftTarget1)){
                                break;
                            }
                            else if(!rc.canSense(leftTarget1.add(rc.getLocation().directionTo(leftTarget1)))){
                                break;
                            }
                            else{
                                leftTarget1 = leftTarget1.add(rc.getLocation().directionTo(leftTarget1));
                            }
                        }
                        tangents[3] = leftTarget1;
                        leftTarget2 = straightTarget.add(rc.getLocation().directionTo(straightTarget).rotateLeft().rotateLeft());
                        for(int i = 0; i < sensorRange; i++){
                            if(!rc.canSense(leftTarget2)){
                                leftTarget2 = straightTarget;
                                break;
                            }
                            else if(rc.senseRubble(leftTarget2) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH){
                                break;
                            }
                            else if(!leftTarget2.equals(rc.getLocation()) && rc.isLocationOccupied(leftTarget2)){
                                break;
                            }
                            else if(!rc.canSense(leftTarget2.add(rc.getLocation().directionTo(leftTarget2)))){
                                break;
                            }
                            else{
                                leftTarget2 = leftTarget2.add(rc.getLocation().directionTo(leftTarget2));
                            }
                        }
                        tangents[4] = leftTarget2;

                        distance = 999;
                        //System.out.println();
                        for (int i = 0; i < tangents.length; i++) {
                            //System.out.println(tangents[i].toString());
                            if (tangents[i].distanceSquaredTo(targets[targetNum]) < distance) {
                                moveTo = tangents[i];
                                distance = tangents[i].distanceSquaredTo(targets[targetNum]);
                            }
                        }
                        Direction dirToMove = rc.getLocation().directionTo(moveTo);
                        //System.out.println(dirToMove.toString());
                        if (rc.canMove(dirToMove)) {
                            // Move
                            rc.move(dirToMove);
                        } else if (rc.canMove(dirToMove.rotateLeft())) {
                            rc.move(dirToMove.rotateLeft());
                        } else if (rc.canMove(dirToMove.rotateRight())) {
                            rc.move(dirToMove.rotateRight());
                        } else if (rc.canMove(dirToMove.rotateLeft().rotateLeft())) {
                            rc.move(dirToMove.rotateLeft());
                        } else if (rc.canMove(dirToMove.rotateRight().rotateRight())) {
                            rc.move(dirToMove.rotateRight());
                        } else if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
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
    }
}
