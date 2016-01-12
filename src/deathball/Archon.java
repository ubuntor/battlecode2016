package deathball;

import battlecode.common.*;
import java.util.Random;

/**
 * Created by samuel on 1/11/16.
 */
public class Archon {
    public static void run(RobotController rc) {
        int mode = 0;
        try {
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
                int priority = -1;
                int lvl = 0;
                Signal msg;
                Signal[] inbox = rc.emptySignalQueue();
                for(int i = 0; i <inbox.length; i++){
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
                            msg = inbox[i]
                        }
                    }
                }

                if(mode == 0) {
                    Direction dirToBuild = Direction.NORTH_WEST;
                    RobotType typeToBuild = RobotType.GUARD;
                    if (Math.random() > 0.6) {
                        typeToBuild = RobotType.SOLDIER;
                    }
                    if (rc.getTeamParts() >= 30) {
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
