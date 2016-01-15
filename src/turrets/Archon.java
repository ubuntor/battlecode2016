package turrets;

import battlecode.common.*;

import java.util.Random;

/**
 * Created by samuel on 1/11/16.
 */
public class Archon {
    public static void run(RobotController rc) {
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        Direction dirToBuild;
        Direction dirToMove;
        Random rand = new Random(rc.getID());
        MapLocation[] friendlyArchons = rc.getInitialArchonLocations(rc.getTeam());
        int mode = 0;
        RobotType typeToBuild;
        try {
            if(rc.getLocation().equals(friendlyArchons[0])){
                mode = 0; //homebase
            }
            else{
                mode = 1; //not homebase
            }
            // Any code here gets executed exactly once at the beginning of the game.
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                if(mode != 0){
                    //neutrals
                    RobotInfo[] bots = rc.senseNearbyRobots(2, Team.NEUTRAL);
                    for (int i = 0; i < bots.length; i++) {
                        rc.activate(bots[i].location);
                    }
                    //move
                    dirToMove = rc.getLocation().directionTo(friendlyArchons[0]);
                    if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                        // Too much rubble, so I should clear it
                        if(rc.canMove(dirToMove.rotateLeft())){
                            rc.move(dirToMove.rotateLeft());
                        }
                        else if(rc.canMove(dirToMove.rotateRight())){
                            rc.move(dirToMove.rotateRight());
                        }
                        else {
                            rc.clearRubble(dirToMove);
                        }
                        // Check if I can move in this direction
                    } else if (rc.canMove(dirToMove)) {
                        // Move
                        rc.move(dirToMove);
                    }else if(rc.canMove(dirToMove.rotateLeft())){
                        rc.move(dirToMove.rotateLeft());
                    }
                    else if(rc.canMove(dirToMove.rotateRight())){
                        rc.move(dirToMove.rotateRight());
                    }
                }
                //build
                if(mode == 0) {
                    dirToBuild = directions[0];
                    if (rc.getTeamParts() >= RobotType.TURRET.partCost) {
                        typeToBuild = RobotType.TURRET;
                        /*if (count % 4 == 0) {
                            typeToBuild = RobotType.SCOUT;
                        }*/
                        for (int i = 0; i < 8; i++) {
                            // If possible, build in this direction
                            if (rc.canBuild(dirToBuild, typeToBuild)) {
                                rc.build(dirToBuild, typeToBuild);
                                break;
                            } else {
                                dirToBuild = dirToBuild.rotateRight();
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
