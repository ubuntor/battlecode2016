package deathball;

import battlecode.common.*;

/**
 * Created by samuel on 1/11/16.
 */
public class Guard {
    public static void run(RobotController rc) {
        try {
            // init stuff
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                int heuristic = 0;
                int val = 0;
                MapLocation maxloc = rc.getLocation();
                RobotInfo[] attackable = rc.senseHostileRobots(rc.getLocation(), 2);
                for(int i = 0; i < attackable.length; i++){
                    val = 0;
                    if(attackable[i].type == RobotType.GUARD || attackable[i].type == RobotType.ARCHON){
                        val = 4;
                    } else if(attackable[i].type == RobotType.TTM){
                        val = 5;
                    } else if(attackable[i].type == RobotType.SOLDIER){
                        val = 6;
                    } else if(attackable[i].type == RobotType.TURRET){
                        val = 7;
                    } else if(attackable[i].type == RobotType.VIPER){
                        val = 8;
                    } if (attackable[i].health <= 6) {
                        val *= 2;
                    }
                    if(attackable[i].type == RobotType.FASTZOMBIE || attackable[i].type == RobotType.BIGZOMBIE){
                        val = 1;
                    } else if(attackable[i].type == RobotType.STANDARDZOMBIE) {
                        val = 2;
                    } else if(attackable[i].type == RobotType.RANGEDZOMBIE){
                        val = 3;
                    }
                    if( val > heuristic){
                        maxloc = attackable[i].location;
                        heuristic = val;
                    }
                }
                if(rc.isWeaponReady() && !maxloc.equals(rc.getLocation())){
                    rc.attackLocation(maxloc);
                }
                heuristic = 0;
                maxloc = rc.getLocation();
                RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), 24);
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
                    } if (enemies[i].health <= 12) {
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
                if(rc.isCoreReady()){
                    Direction dirToMove = rc.getLocation().directionTo(maxloc);
                    if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
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
