package deathball;

import battlecode.common.*;
import java.util.Random;

/**
 * Created by samuel on 1/11/16.
 */
public class Archon {
    public static void run(RobotController rc) {
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
                int ID = rc.getID();
                MapLocation home = rc.getLocation();
                MapLocation loc = home.add(Direction.NORTH);
                RobotInfo scoutInfo = rc.senseRobotAtLocation(loc);
                int scoutID = scoutInfo.ID;
                // Any code here gets executed exactly once at the beginning of the game.
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), 53);
                for (int i = 0; i < enemies.length; i++) {
                    if (rc.getLocation().distanceSquaredTo(enemies[i].location) <= 15) {
                        if (rc.isCoreReady()) {
                            // Check the rubble in that direction
                            if (rc.canMove(enemies[i].location.directionTo(rc.getLocation()))) {
                                // Move
                                rc.move(enemies[i].location.directionTo(rc.getLocation()));
                            }
                        }
                    }
                }
                Signal msg = null;
                int priority = -1;
                int lvl = 0;
                Signal[] inbox = rc.emptySignalQueue();
                for(int i = 0; i <inbox.length; i++){
                    lvl = 0;
                    if(inbox[i].getTeam() == rc.getTeam()){
                        if(inbox[i].getMessage()[0] == 0 && inbox[i].getMessage()[1] == 0){
                            lvl = 4;
                        }
                        else if(inbox[i].getMessage()[0] == 0 && inbox[i].getMessage()[1] == 1){
                            lvl = 3;
                        }
                        else if(inbox[i].getMessage()[0] == 1 && inbox[i].getMessage()[1] == 0){
                            lvl = 2;
                        }
                        else if(lvl > priority){
                            msg = inbox[i];
                        }
                    }
                }
                if(msg != null){
                    int x = msg.getMessage()[0];
                    int y = msg.getMessage()[1];
                    rc.broadcastMessageSignal(9, 9, 70);
                    rc.broadcastMessageSignal(x, y, 70);
                    if(lvl == 4 || rc.getTeamParts() <= 30){
                        destx = x;
                        desty = y;
                        mode = 2; //combat mode
                    }
                }
                if(mode == 2){
                    RobotInfo[] bots = rc.senseNearbyRobots(2, rc.getTeam());
                    for(int i = 0; i < bots.length; i++){
                        if(bots[i].health < bots[i].maxHealth){
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
                if (rc.getTeamParts() >= 30) {
                    Direction dirToBuild = Direction.NORTH_WEST;
                    RobotType typeToBuild = RobotType.GUARD;
                    if (Math.random() > 0.95 && rc.getTeamParts() >= 40) {
                        typeToBuild = RobotType.SCOUT;
                    }
                    if (Math.random() > 0.75) {
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
                    Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
