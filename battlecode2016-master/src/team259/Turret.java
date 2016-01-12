package team259;

import battlecode.common.*;

/**
 * Created by samuel on 1/11/16.
 */
public class Turret {
    public static void run(RobotController rc) {
        try {
            // init stuff
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                // may waste bytecodes???
                switch (rc.getType()) {
                    case TURRET:
                        // turret main stuff
                        break;
                    case TTM:
                        // ttm main stuff
                        break;
                    default:
                        System.out.println("uwotm8");
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
