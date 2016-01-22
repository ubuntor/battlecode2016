package team259;

import battlecode.common.*;
/**
 * Created by allen on 1/19/16.
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
                        Signal[] sigs = rc.emptySignalQueue();
                        MapLocation attackLoc = null;;
                        for(Signal s : sigs){
                           if(s.getTeam() == rc.getTeam()){
                              int[] msg = s.getMessage();
                              if(msg != null && msg[0] == Utils.TURRET_ATTACK){
                                 int[] loc = Utils.unpack2(msg[1]);
                            	 attackLoc = new MapLocation(loc[0], loc[1]); 
                                 break;
                              }
                           }
                        }
                        if(rc.isWeaponReady()){
                        	if(attackLoc != null && rc.canAttackLocation(attackLoc))
                        		rc.attackLocation(attackLoc);
                        	else {
                        		MapLocation currLoc = rc.getLocation();
                        		RobotInfo[] hostile = rc.senseHostileRobots(currLoc, 24);
                        		RobotInfo bestTarget = null;
            					double maxExpected = 0;
            					for(int i = 0; i < hostile.length; i++){
            						if(!rc.canAttackLocation(hostile[i].location))
            							continue;
            						int dist = currLoc.distanceSquaredTo(hostile[i].location);
            						double tempExpected = 0;
            						if (hostile[i].type.equals(RobotType.SOLDIER) && dist <= 13){
            							tempExpected += 2 / hostile[i].health;
            						} else if (hostile[i].type.equals(RobotType.GUARD) && dist <= 2){
            							tempExpected += 1.5 / hostile[i].health;
            						} else if (hostile[i].type.equals(RobotType.VIPER) && dist <= 20){
            							tempExpected += (8.0/3) / hostile[i].health;
            						} else if (hostile[i].type.equals(RobotType.TURRET) && dist <= 48){
            							tempExpected += (13.0/3) / hostile[i].health;
            						} else if (hostile[i].type.equals(RobotType.RANGEDZOMBIE) && dist <= 13){
            							tempExpected += 3 / hostile[i].health;
            						} else if (hostile[i].type.equals(RobotType.STANDARDZOMBIE) && dist <= 2){
            							tempExpected += 1.25 / hostile[i].health;
            						} else if (hostile[i].type.equals(RobotType.FASTZOMBIE) && dist <= 2){
            							tempExpected += 3 / hostile[i].health;
            						} else if (hostile[i].type.equals(RobotType.BIGZOMBIE) && dist <= 2){
            							tempExpected += (25.0/3) / hostile[i].health;
            						}
            						if(tempExpected > maxExpected || bestTarget == null ||
            								(tempExpected == maxExpected && dist > bestTarget.location.distanceSquaredTo(currLoc))){
            							maxExpected = tempExpected;
            							bestTarget = hostile[i];
            						}
            					}
            					if(bestTarget != null)
            						rc.attackLocation(bestTarget.location);
                        	}
                        }
                        break;
                    case TTM:
                        rc.unpack();
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
