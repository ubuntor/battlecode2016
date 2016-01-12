package deathball;

import battlecode.common.*;

import java.util.Random;

/**
 * Created by samuel on 1/11/16.
 */
public class Archon {
    public static void run(RobotController rc) {
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        Random rand = new Random(rc.getID());
        int fate = rand.nextInt(1000);
        int mode = 0;
        int destx = 0;
        int desty = 0;
        try {
            destx = rc.getLocation().x;
            desty = rc.getLocation().y;
            Direction dirToBuild = Direction.NORTH;
            for (int i = 0; i < 8; i++) {
                // If possible, build in this direction
                if (rc.canBuild(dirToBuild, RobotType.SCOUT)) {
                    rc.build(dirToBuild, RobotType.SCOUT);
                    break;
                } else {
                    // Rotate the direction to try
                    dirToBuild = dirToBuild.rotateRight();
                }
            }
            //int ID = rc.getID();
            //MapLocation home = rc.getLocation();
            //MapLocation loc = home.add(Direction.NORTH);
            //RobotInfo scoutInfo = rc.senseRobotAtLocation(loc);
            //int scoutID = scoutInfo.ID;
            // Any code here gets executed exactly once at the beginning of the game.
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                //build
                if (rc.getTeamParts() >= 30) {
                    Direction dirToBuild = Direction.NORTH;
                    RobotType typeToBuild = RobotType.GUARD;
                    if (Math.random() * rc.getRoundNum() / 3000 >= 0.95 && rc.getTeamParts() >= 40) {
                        typeToBuild = RobotType.SCOUT;
                    }
                    else if (Math.random() > 0.75) {
                        typeToBuild = RobotType.SOLDIER;
                    }
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
                    } else {
                        int x = enemies[i].location.x;
                        int y = enemies[i].location.y;
                        rc.broadcastMessageSignal(9, 9, 70); //attaaack
                        rc.broadcastMessageSignal(x, y, 70);
                        if (rc.getTeamParts() <= 30) {
                            destx = x;
                            desty = y;
                            mode = 2; //combat mode
                        }
                    }
                }
                //read
                Signal msg = null;
                int priority = -1;
                int lvl = 0;
                Signal[] inbox = rc.emptySignalQueue();
                for (int i = 0; i < inbox.length; i++) {
                    lvl = 0;
                    if (inbox[i].getTeam() == rc.getTeam()) {
                        if (inbox[i].getMessage()[0] == 0 && inbox[i].getMessage()[1] == 0) {
                            lvl = 4; //enemy
                        } else if (inbox[i].getMessage()[0] == 1 && inbox[i].getMessage()[1] == 0) {
                            lvl = 3; //parts
                        } else if (inbox[i].getMessage()[0] == 0 && inbox[i].getMessage()[1] == 1) {
                            lvl = 2; //zombie
                        } else if (lvl > priority) {
                            msg = inbox[i];
                            priority = lvl;
                        }
                    }
                }
                if (msg != null) {
                    int x = msg.getMessage()[0];
                    int y = msg.getMessage()[1];
                    rc.broadcastMessageSignal(9, 9, 70); //attack command
                    rc.broadcastMessageSignal(x, y, 70);
                    if ((priority == 4 || priority == 3) && rc.getTeamParts() <= 30) {
                        destx = x;
                        desty = y;
                        mode = 2; //combat mode
                    }

                }
                if (mode == 2) {
                    bots = rc.senseNearbyRobots(2, rc.getTeam());
                    for (int i = 0; i < bots.length; i++) {
                        if (bots[i].health < bots[i].maxHealth) {
                            rc.repair(bots[i].location);
                        }
                    }
                    if (rc.isCoreReady()) {
                        // Check the rubble in that direction
                        MapLocation loc = new MapLocation(destx, desty);
                        if (rc.canMove(rc.getLocation().directionTo(loc))) {
                            // Move
                            rc.move(rc.getLocation().directionTo(loc));
                        }
                    }
                }
                //scrap
                MapLocation[] scrap = rc.sensePartLocations(35);
                MapLocation closest = rc.getLocation();
                double distance = 999.000;
                for(int i = 0; i < scrap.length; i++){
                    if(scrap[i].distanceSquaredTo(rc.getLocation()) <= distance){
                        closest = scrap[i];
                        distance = scrap[i].distanceSquaredTo(rc.getLocation());
                    }
                }
                Direction dirToMove;
                if(!closest.equals(rc.getLocation())){
                    dirToMove = rc.getLocation().directionTo(closest);
                    if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                        // Too much rubble, so I should clear it
                        rc.clearRubble(dirToMove);
                        // Check if I can move in this direction
                    } else if (rc.canMove(dirToMove)) {
                        // Move
                        rc.move(dirToMove);
                    }
                    rc.broadcastMessageSignal(9, 9, 70);
                    rc.broadcastMessageSignal(closest.x, closest.y, 70);
                }
                else {
                    dirToMove = directions[fate % 8];
                }
                // Check the rubble in that direction

                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
