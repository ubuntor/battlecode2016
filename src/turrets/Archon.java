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
        Direction nextDir = directions[0]; //NORTH
        Direction dirToMove = null;
        Random rand = new Random(rc.getID());
        int fate = rand.nextInt(1000);
        int mode = 0;
        int destx = rc.getLocation().x;
        int desty = rc.getLocation().y;
        int count = 1;
        try {
            Signal[] inbox = rc.emptySignalQueue();
            for(int i = 0; i < inbox.length; i++){
                if(inbox[i].getTeam() == rc.getTeam()){
                    mode = 1;
                    destx = inbox[i].getLocation().x;
                    desty = inbox[i].getLocation().y;
                    rc.broadcastMessageSignal(inbox[i].getLocation().x, inbox[i].getLocation().y, 140);
                }
            }
            //homebase
            if(mode == 0){
                rc.broadcastMessageSignal(rc.getLocation().x, rc.getLocation().y, 140);
            }
            // Any code here gets executed exactly once at the beginning of the game.
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                //move
                MapLocation target = new MapLocation(destx, desty);
                if(!rc.getLocation().equals(target)){
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
                //build
                int built = 0;
                Direction dirToBuild = nextDir;
                if(mode == 0) {
                    if (rc.getTeamParts() >= 125) {
                        RobotType typeToBuild = RobotType.TURRET;
                        if(count%4 == 0){
                            typeToBuild = RobotType.SCOUT;
                        }
                        for (int i = 0; i < 8; i++) {
                            // If possible, build in this direction
                            if (rc.canBuild(dirToBuild, typeToBuild)) {
                                rc.build(dirToBuild, typeToBuild);
                                nextDir = nextDir.rotateRight();
                                built = 1;
                                count++;
                                break;
                            } else {
                                dirToBuild = dirToBuild.rotateRight();
                            }
                        }
                        if(built == 0){
                            dirToBuild = nextDir;
                            for(int i = 0; i < 8; i++){
                                if(rc.isLocationOccupied(rc.getLocation().add(dirToBuild))){
                                    RobotInfo bot = rc.senseRobotAtLocation(rc.getLocation().add(dirToBuild));
                                    if(bot.team.equals(rc.getTeam()) && (bot.type.equals(RobotType.TURRET) || bot.type.equals(RobotType.SCOUT))){
                                        rc.broadcastMessageSignal(bot.ID,0,2);
                                    }else if(bot.team.equals(rc.getTeam()) && bot.type.equals(RobotType.ARCHON)){
                                        rc.broadcastMessageSignal(bot.ID,0,2);
                                    }
                                }

                                else{
                                    dirToBuild = dirToBuild.rotateRight();
                                }
                            }
                        }
                    }
                }
                //neutrals
                RobotInfo[] bots = rc.senseNearbyRobots(2, Team.NEUTRAL);
                for (int i = 0; i < bots.length; i++) {
                    rc.activate(bots[i].location);
                }
                //enemies
                RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), 53);
                for (int i = 0; i < enemies.length; i++) {
                    if (rc.getLocation().distanceSquaredTo(enemies[i].location) <= 26) {
                        if (rc.isCoreReady()) {
                            // Check the rubble in that direction
                            Direction optimalAwayDir = enemies[i].location.directionTo(rc.getLocation());
                            if (rc.canMove(optimalAwayDir)) {
                                // Move away
                                rc.move(optimalAwayDir);
                            } else {
                                if (rc.canMove(optimalAwayDir.rotateLeft()))
                                    rc.move(optimalAwayDir.rotateLeft());
                                else if (rc.canMove(optimalAwayDir.rotateRight()))
                                    rc.move(optimalAwayDir.rotateRight());
                                else if (rc.canMove(optimalAwayDir.rotateLeft().rotateLeft()))
                                    rc.move(optimalAwayDir.rotateLeft().rotateLeft());
                                else if (rc.canMove(optimalAwayDir.rotateRight().rotateRight()))
                                    rc.move(optimalAwayDir.rotateRight().rotateRight());
                                // if we still can't move then we're fucked lol
                            }
                        }
                    }
                }
                //read
                //scrap
                MapLocation[] scrap = rc.sensePartLocations(2);
                if(scrap.length != 0){
                    dirToMove = rc.getLocation().directionTo(scrap[0]);
                    if (rc.senseRubble(scrap[0]) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
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
