package turrets;

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
                        int heuristic = -1;
                        int val = 0;
                        MapLocation maxloc = rc.getLocation();
                        RobotInfo[] attackable = rc.senseHostileRobots(rc.getLocation(), 13);
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
                            } if (attackable[i].health <= 28) {
                                val *= 2;
                            }
                            if(attackable[i].type == RobotType.FASTZOMBIE || attackable[i].type == RobotType.BIGZOMBIE){
                                val = 1;
                            } else if(attackable[i].type == RobotType.STANDARDZOMBIE) {
                                val = 2;
                            } else if(attackable[i].type == RobotType.RANGEDZOMBIE){
                                val = 3;
                            }
                            if( val > heuristic && rc.canAttackLocation(attackable[i].location)){
                                maxloc = attackable[i].location;
                                heuristic = val;
                            }
                        }
                        if(rc.isWeaponReady() && !maxloc.equals(rc.getLocation())){
                            rc.attackLocation(maxloc);
                        }
                        Signal[] inbox = rc.emptySignalQueue();
                        int attack = 0;
                        RobotInfo bot = null;
                        for(int i = 0; i < inbox.length; i++){
                            if(inbox[i].getTeam().equals(rc.getTeam())) {
                                int ID = inbox[i].getID();
                                if(rc.canSense(inbox[i].getLocation())) {
                                    bot = rc.senseRobot(ID);
                                }
                                else{
                                    attack = 1;
                                }
                                if ((!bot.type.equals(RobotType.ARCHON)) && (!bot.type.equals(null))) {
                                    attack = 1;
                                }
                            }
                            if(attack == 1){
                                rc.attackLocation(inbox[i].getLocation());
                                break;
                            }
                        }
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
