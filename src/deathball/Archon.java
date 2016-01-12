package deathball;

import battlecode.common.*;

/**
 * Created by samuel on 1/11/16.
 */
public class Archon {
    public static void run(RobotController rc) {
		int[] status = {0,0,0,0,0,0,0};
        int mode = 0;
        try {
            Direction dirToBuild = directions[0];
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
                Direction dirToBuild = Direction.NORTH_WEST;
                    RobotType typeToBuild = RobotType.GUARD;
                    if (rc.getTeamParts() >= 30) {
                        for (int i = 0; i < 8; i++) {
                            if (status[i] == 0) {
                                // If possible, build in this direction
                                if (rc.canBuild(dirToBuild, typeToBuild)) {
                                    rc.build(dirToBuild, typeToBuild);
                                    status[i] = 1;
                                    break;
                                } else {
                                    if (rc.canMove(dirToBuild.opposite())) {
                                        // Move
                                        rc.move(dirToBuild.opposite());
                                    }
                                }
                            } else{
                                dirToBuild = dirToBuild.rotateRight();
                                if (typeToBuild == RobotType.GUARD) {
                                    typeToBuild = RobotType.SOLDIER;
                                } else {
                                    typeToBuild = RobotType.GUARD;
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
