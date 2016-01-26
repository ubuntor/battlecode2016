package team259.guardrush;

import battlecode.common.*;

/**
 * Created by samuel on 1/11/16.
 */
public class Scout {
    public static void run(RobotController rc) {
        try {
            // init stuff
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                //targeting
                int heuristic = -1;
                MapLocation maxloc = rc.getLocation();
                int val = 0;
                RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), 53);
                for(int i = 0; i < enemies.length; i++){
                    val = 0;
                    if(enemies[i].type == RobotType.GUARD || enemies[i].type == RobotType.ARCHON){
                        val = 4;
                    } else if(enemies[i].type == RobotType.TTM){
                        val = 5;
                    } else if(enemies[i].type == RobotType.SOLDIER){
                        val = 6;
                    } else if(enemies[i].type == RobotType.TURRET){
                        val = 7;
                    } else if(enemies[i].type == RobotType.VIPER){
                        val = 8;
                    } if (enemies[i].health <= 28) {
                        val *= 2;
                    }
                    if(enemies[i].type == RobotType.FASTZOMBIE || enemies[i].type == RobotType.BIGZOMBIE){
                        val = 1;
                    } else if(enemies[i].type == RobotType.STANDARDZOMBIE) {
                        val = 2;
                    } else if(enemies[i].type == RobotType.RANGEDZOMBIE){
                        val = 3;
                    }
                    if( val > heuristic){
                        maxloc = enemies[i].location;
                        heuristic = val;
                    }
                }
                if(!maxloc.equals(rc.getLocation())) {
                    rc.broadcastMessageSignal(maxloc.x, maxloc.y, 106);
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
